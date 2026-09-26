package com.onthisday.ingestion.media;

import com.onthisday.ingestion.quiz.CuratedQuizContent;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Performs the network-only release gate for published quiz images.
 *
 * <p>This is intentionally separate from JSON validation and imports: a source host outage should
 * block release review, not make content parsing or database transactions perform network I/O.
 *
 * <p>Each image gets one deadline ({@link QuizImageLivenessAuditConfiguration#requestTimeout()})
 * that covers both the response headers and the streamed body, matching the mobile client's
 * per-image preparation ceiling.
 */
public final class QuizImageLivenessAudit {

  /** Media types the mobile quiz-image gate accepts (see QUIZ_AUTHORING_GUIDE.md). */
  private static final Set<String> ACCEPTED_MEDIA_TYPES = Set.of("image/jpeg", "image/png");

  private static final byte[] JPEG_SIGNATURE = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
  private static final byte[] PNG_SIGNATURE = {
    (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
  };
  private static final int SIGNATURE_LENGTH = PNG_SIGNATURE.length;

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
      // Results keep input order so reports are deterministic across runs.
      var results = new ArrayList<QuizImageLivenessResult>();
      var interrupted = false;
      for (var index = 0; index < futures.size(); index++) {
        var image = images.get(index);
        var future = futures.get(index);
        if (interrupted) {
          future.cancel(true);
          results.add(failed(image, 0, "audit interrupted"));
          continue;
        }
        try {
          results.add(future.get());
        } catch (InterruptedException exception) {
          Thread.currentThread().interrupt();
          interrupted = true;
          future.cancel(true);
          results.add(failed(image, 0, "audit interrupted"));
        } catch (ExecutionException exception) {
          results.add(failed(image, 0, "audit task failed"));
        }
      }
      return new QuizImageLivenessReport(results);
    } finally {
      executor.shutdownNow();
      awaitShutdown(executor);
    }
  }

  private QuizImageLivenessResult auditImage(PublishedQuizImage image) {
    var startedAt = System.nanoTime();
    CompletableFuture<HttpResponse<BodyObservation>> exchange = null;
    try {
      var request =
          HttpRequest.newBuilder(image.url())
              .GET()
              .timeout(configuration.requestTimeout())
              .header("User-Agent", configuration.userAgent())
              .header("Accept", "image/jpeg, image/png;q=0.9")
              .build();
      exchange = client.sendAsync(request, this::bodyHandler);
      var response =
          exchange.get(configuration.requestTimeout().toMillis(), TimeUnit.MILLISECONDS);
      return classify(image, response, elapsedMillis(startedAt));
    } catch (TimeoutException exception) {
      cancel(exchange);
      return failed(image, elapsedMillis(startedAt), "request timed out");
    } catch (InterruptedException exception) {
      cancel(exchange);
      Thread.currentThread().interrupt();
      return failed(image, elapsedMillis(startedAt), "request interrupted");
    } catch (ExecutionException exception) {
      var cause = exception.getCause();
      if (cause instanceof HttpTimeoutException) {
        return failed(image, elapsedMillis(startedAt), "request timed out");
      }
      if (cause instanceof IllegalArgumentException) {
        return failed(image, elapsedMillis(startedAt), "invalid image URL");
      }
      return failed(image, elapsedMillis(startedAt), "image host unreachable");
    } catch (IllegalArgumentException exception) {
      return failed(image, elapsedMillis(startedAt), "invalid image URL");
    }
  }

  private QuizImageLivenessResult classify(
      PublishedQuizImage image, HttpResponse<BodyObservation> response, long durationMillis) {
    var status = response.statusCode();
    var contentType = response.headers().firstValue("Content-Type").orElse(null);
    var body = response.body();
    if (isRedirect(status)) {
      return failed(image, durationMillis, status, contentType, null, "redirect response");
    }
    if (status != 200) {
      return failed(image, durationMillis, status, contentType, null, "non-200 response");
    }
    var mediaType = mediaType(contentType);
    if (!ACCEPTED_MEDIA_TYPES.contains(mediaType)) {
      return failed(image, durationMillis, status, contentType, null, "non-image content type");
    }
    if (body.exceededLimit()) {
      return failed(
          image, durationMillis, status, contentType, body.byteSize(),
          "asset exceeds encoded-byte limit");
    }
    if (!matchesSignature(mediaType, body.signature())) {
      return failed(
          image, durationMillis, status, contentType, body.byteSize(),
          "content does not match declared image type");
    }
    return new QuizImageLivenessResult(
        image.questionId(), image.url(), true, status, contentType, body.byteSize(), durationMillis,
        null);
  }

  /** Streams only bodies worth measuring; everything else is cancelled without being read. */
  private HttpResponse.BodySubscriber<BodyObservation> bodyHandler(
      HttpResponse.ResponseInfo info) {
    var mediaType = mediaType(info.headers().firstValue("Content-Type").orElse(null));
    var measure = info.statusCode() == 200 && ACCEPTED_MEDIA_TYPES.contains(mediaType);
    return new BoundedCountingSubscriber(measure ? configuration.maxEncodedBytes() : -1);
  }

  private static String mediaType(String contentType) {
    if (contentType == null) {
      return "";
    }
    var separator = contentType.indexOf(';');
    var type = separator >= 0 ? contentType.substring(0, separator) : contentType;
    return type.trim().toLowerCase(Locale.ROOT);
  }

  private static boolean matchesSignature(String mediaType, byte[] signature) {
    var expected = "image/png".equals(mediaType) ? PNG_SIGNATURE : JPEG_SIGNATURE;
    return signature.length >= expected.length
        && Arrays.equals(signature, 0, expected.length, expected, 0, expected.length);
  }

  private static boolean isRedirect(int statusCode) {
    return statusCode >= 300 && statusCode < 400;
  }

  private static long elapsedMillis(long startedAt) {
    return Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
  }

  private static void cancel(CompletableFuture<?> exchange) {
    if (exchange != null) {
      exchange.cancel(true);
    }
  }

  private static void awaitShutdown(ExecutorService executor) {
    try {
      executor.awaitTermination(5, TimeUnit.SECONDS);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
    }
  }

  private static QuizImageLivenessResult failed(
      PublishedQuizImage image, long durationMillis, String failure) {
    return failed(image, durationMillis, null, null, null, failure);
  }

  private static QuizImageLivenessResult failed(
      PublishedQuizImage image,
      long durationMillis,
      Integer statusCode,
      String contentType,
      Long byteSize,
      String failure) {
    return new QuizImageLivenessResult(
        image.questionId(), image.url(), false, statusCode, contentType, byteSize, durationMillis,
        failure);
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

  /** What was observed about a body without retaining it. */
  record BodyObservation(long byteSize, boolean exceededLimit, byte[] signature) {}

  /**
   * Counts streamed bytes, keeps only the leading signature bytes, and cancels the stream as soon
   * as the limit is exceeded. A negative limit discards the body immediately.
   */
  static final class BoundedCountingSubscriber
      implements HttpResponse.BodySubscriber<BodyObservation> {

    private final long maxBytes;
    private final CompletableFuture<BodyObservation> result = new CompletableFuture<>();
    private final byte[] signature = new byte[SIGNATURE_LENGTH];
    private Flow.Subscription subscription;
    private long byteSize;
    private int signatureLength;

    BoundedCountingSubscriber(long maxBytes) {
      this.maxBytes = maxBytes;
    }

    @Override
    public CompletionStage<BodyObservation> getBody() {
      return result;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
      this.subscription = subscription;
      if (maxBytes < 0) {
        subscription.cancel();
        result.complete(new BodyObservation(0, false, new byte[0]));
        return;
      }
      subscription.request(1);
    }

    @Override
    public void onNext(List<ByteBuffer> buffers) {
      if (result.isDone()) {
        return;
      }
      for (var buffer : buffers) {
        var remaining = buffer.remaining();
        while (signatureLength < SIGNATURE_LENGTH && buffer.hasRemaining()) {
          signature[signatureLength++] = buffer.get();
        }
        byteSize += remaining;
      }
      if (byteSize > maxBytes) {
        subscription.cancel();
        result.complete(observation(true));
        return;
      }
      subscription.request(1);
    }

    @Override
    public void onError(Throwable throwable) {
      result.completeExceptionally(throwable);
    }

    @Override
    public void onComplete() {
      result.complete(observation(false));
    }

    private BodyObservation observation(boolean exceeded) {
      return new BodyObservation(byteSize, exceeded, Arrays.copyOf(signature, signatureLength));
    }
  }
}
