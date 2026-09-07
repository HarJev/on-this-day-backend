package com.onthisday.platform.notifications.fcm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onthisday.notifications.NotificationDeliveryResult;
import com.onthisday.notifications.NotificationDeliveryStatus;
import com.onthisday.notifications.NotificationMessage;
import com.onthisday.notifications.NotificationSender;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FcmNotificationSender implements NotificationSender {

  private static final Logger LOG = LoggerFactory.getLogger(FcmNotificationSender.class);
  private static final Pattern PROJECT_ID = Pattern.compile("[A-Za-z0-9._-]+");

  private final HttpClient httpClient;
  private final FcmAccessTokenProvider accessTokenProvider;
  private final FcmMessageWriter messageWriter;
  private final ObjectMapper objectMapper;
  private final URI sendUri;

  public FcmNotificationSender(
      String projectId, FcmAccessTokenProvider accessTokenProvider, ObjectMapper objectMapper) {
    this(
        HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build(),
        projectId,
        accessTokenProvider,
        objectMapper);
  }

  FcmNotificationSender(
      HttpClient httpClient,
      String projectId,
      FcmAccessTokenProvider accessTokenProvider,
      ObjectMapper objectMapper) {
    if (projectId == null || !PROJECT_ID.matcher(projectId).matches()) {
      throw new IllegalArgumentException("Firebase project ID is required.");
    }
    this.httpClient = Objects.requireNonNull(httpClient, "httpClient must not be null");
    this.accessTokenProvider =
        Objects.requireNonNull(accessTokenProvider, "accessTokenProvider must not be null");
    this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
    this.messageWriter = new FcmMessageWriter(objectMapper);
    this.sendUri =
        URI.create("https://fcm.googleapis.com/v1/projects/" + projectId + "/messages:send");
  }

  @Override
  public NotificationDeliveryResult send(String recipientToken, NotificationMessage message) {
    var startedAt = System.nanoTime();
    try {
      var request =
          HttpRequest.newBuilder(sendUri)
              .timeout(Duration.ofSeconds(20))
              .header("Authorization", "Bearer " + accessTokenProvider.getAccessToken())
              .header("Content-Type", "application/json; charset=UTF-8")
              .POST(HttpRequest.BodyPublishers.ofString(messageWriter.write(recipientToken, message)))
              .build();
      var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() >= 200 && response.statusCode() < 300) {
        LOG.info("fcm_send_end status=success durationMs={}", durationMs(startedAt));
        return NotificationDeliveryResult.success();
      }

      var errorCode = findErrorCode(response.body());
      var status = FcmErrorClassifier.classify(response.statusCode(), errorCode);
      LOG.warn(
          "fcm_send_end status={} httpStatus={} errorCode={} durationMs={}",
          status,
          response.statusCode(),
          errorCode,
          durationMs(startedAt));
      return new NotificationDeliveryResult(status, errorCode);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      LOG.warn("fcm_send_interrupted durationMs={}", durationMs(startedAt));
      return new NotificationDeliveryResult(
          NotificationDeliveryStatus.TRANSIENT_FAILURE, "interrupted");
    } catch (IOException exception) {
      LOG.warn("fcm_send_io_failure durationMs={}", durationMs(startedAt), exception);
      return new NotificationDeliveryResult(
          NotificationDeliveryStatus.TRANSIENT_FAILURE, "io_failure");
    }
  }

  private String findErrorCode(String responseBody) {
    try {
      var root = objectMapper.readTree(responseBody);
      var error = root.path("error");
      for (JsonNode detail : error.path("details")) {
        var code = detail.path("errorCode").asText();
        if (!code.isBlank()) {
          return code;
        }
      }
      var status = error.path("status").asText();
      return status.isBlank() ? "UNKNOWN" : status;
    } catch (IOException exception) {
      return "UNKNOWN";
    }
  }

  private static long durationMs(long startedAt) {
    return (System.nanoTime() - startedAt) / 1_000_000;
  }
}
