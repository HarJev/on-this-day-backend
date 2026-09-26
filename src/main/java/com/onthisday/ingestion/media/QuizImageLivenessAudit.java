package com.onthisday.ingestion.media;

import com.onthisday.ingestion.quiz.CuratedQuizContent;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Performs the network-only release gate for published quiz images.
 *
 * <p>This is intentionally separate from JSON validation and imports: a source host outage should
 * block release review, not make content parsing or database transactions perform network I/O.
 */
public final class QuizImageLivenessAudit {

  private static final int BUFFER_SIZE = 8 * 1024;

  private final HttpClient client;
  private final QuizImageLivenessAuditConfiguration configuration;

  public QuizImageLivenessAudit() {
    this(QuizImageLivenessAuditConfiguration.defaults());
  }

  public QuizImageLivenessAudit(QuizImageLivenessAuditConfiguration configuration) {
    this(
        HttpClient.newBuilder()
            .connectTimeout(configuration.connectTimeout())
            .followRedirects(HttpClient.Redirect.NEVER)
            .build(),
        configuration);
  }

  QuizImageLivenessAudit(HttpClient client, QuizImageLivenessAuditConfiguration configuration) {
    if (client == null) {
      throw new IllegalArgumentException("client must be present");
    }
    if (configuration == null) {
      throw new IllegalArgumentException("configuration must be present");
    }
    this.client = client;
    this.configuration = configuration;
  }

  public QuizImageLivenessReport audit(CuratedQuizContent content) {
    if (content == null) {
      throw new IllegalArgumentException("content must be present");
    }
    var images =
        content.questionPacks().stream()
            .flatMap(pack -> pack.file().questions().stream())
            .filter(question -> "published".equals(question.publicationState()))
            .filter(question -> question.image() != null)
            .map(question -> new PublishedQuizImage(question.id(), URI.create(question.image().url())))
            .toList();
    return auditImages(images);
  }

  QuizImageLivenessReport auditImages(List<PublishedQuizImage> images) {
    if (images == null) {
      throw new IllegalArgumentException("images must be present");
    }
    if (images.isEmpty()) {
      return new QuizImageLivenessReport(List.of());
    }

    ExecutorService executor = Executors.newFixedThreadPool(configuration.maxConcurrency());
    try {
      List<Future<QuizImageLivenessResult>> futures = new ArrayList<>();
      for (var image : images) {
        futures.add(executor.submit(() -> auditImage(image)));
      }
      var results = new ArrayList<QuizImageLivenessResult>();
      for (var future : futures) {
        try {
          results.add(future.get());
        } catch (InterruptedException exception) {
          Thread.currentThread().interrupt();
          results.add(failed("unknown", null, 0, "audit interrupted"));
        } catch (java.util.concurrent.ExecutionException exception) {
          results.add(failed("unknown", null, 0, "audit task failed"));
        }
      }
      return new QuizImageLivenessReport(results);
    } finally {
      executor.shutdownNow();
    }
  }

  private QuizImageLivenessResult auditImage(PublishedQuizImage image) {
    var startedAt = System.nanoTime();
    try {
      var request =
          HttpRequest.newBuilder(image.url())
              .GET()
              .timeout(configuration.requestTimeout())
              .header("User-Agent", configuration.userAgent())
              .header("Accept", "image/jpeg, image/png, image/*;q=0.8")
              .build();
      var response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
      var durationMillis = elapsedMillis(startedAt);
      if (isRedirect(response.statusCode())) {
        close(response.body());
        return failed(image.questionId(), image.url(), durationMillis, response.statusCode(), null, null,
            "redirect response");
      }
      if (response.statusCode() != 200) {
        close(response.body());
        return failed(image.questionId(), image.url(), durationMillis, response.statusCode(), null, null,
            "non-200 response");
      }
      var contentType = response.headers().firstValue("Content-Type").orElse("");
      if (!isImageContentType(contentType)) {
        close(response.body());
        return failed(image.questionId(), image.url(), durationMillis, response.statusCode(), contentType, null,
            "non-image content type");
      }
      var byteSize = countBytes(response.body());
      if (byteSize > configuration.maxEncodedBytes()) {
        return failed(image.questionId(), image.url(), elapsedMillis(startedAt), response.statusCode(), contentType,
            byteSize, "asset exceeds encoded-byte limit");
      }
      return new QuizImageLivenessResult(
          image.questionId(), image.url(), true, response.statusCode(), contentType, byteSize,
          elapsedMillis(startedAt), null);
    } catch (HttpTimeoutException exception) {
      return failed(image.questionId(), image.url(), elapsedMillis(startedAt), "request timed out");
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      return failed(image.questionId(), image.url(), elapsedMillis(startedAt), "request interrupted");
    } catch (IOException exception) {
      return failed(image.questionId(), image.url(), elapsedMillis(startedAt), "image host unreachable");
    } catch (IllegalArgumentException exception) {
      return failed(image.questionId(), image.url(), elapsedMillis(startedAt), "invalid image URL");
    }
  }

  private long countBytes(InputStream body) throws IOException {
    try (body) {
      long byteSize = 0;
      var buffer = new byte[BUFFER_SIZE];
      int read;
      while ((read = body.read(buffer)) != -1) {
        byteSize += read;
        if (byteSize > configuration.maxEncodedBytes()) {
          return byteSize;
        }
      }
      return byteSize;
    }
  }

  private static boolean isRedirect(int statusCode) {
    return statusCode >= 300 && statusCode < 400;
  }

  private static boolean isImageContentType(String contentType) {
    return contentType.toLowerCase(Locale.ROOT).startsWith("image/");
  }

  private static long elapsedMillis(long startedAt) {
    return Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
  }

  private static void close(InputStream body) {
    try (body) {
      // The body is deliberately discarded after headers prove this rendition is not release-ready.
    } catch (IOException ignored) {
      // The final result already names the release failure without exposing transport internals.
    }
  }

  private static QuizImageLivenessResult failed(
      String questionId, URI url, long durationMillis, String failure) {
    return failed(questionId, url, durationMillis, null, null, null, failure);
  }

  private static QuizImageLivenessResult failed(
      String questionId,
      URI url,
      long durationMillis,
      Integer statusCode,
      String contentType,
      Long byteSize,
      String failure) {
    return new QuizImageLivenessResult(
        questionId, url, false, statusCode, contentType, byteSize, durationMillis, failure);
  }

  record PublishedQuizImage(String questionId, URI url) {
    PublishedQuizImage {
      if (questionId == null || questionId.isBlank()) {
        throw new IllegalArgumentException("questionId must not be blank");
      }
      if (url == null) {
        throw new IllegalArgumentException("url must be present");
      }
    }
  }
}
