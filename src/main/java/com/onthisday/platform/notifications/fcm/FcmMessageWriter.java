package com.onthisday.platform.notifications.fcm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onthisday.notifications.NotificationMessage;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class FcmMessageWriter {

  public static final String REDACTED_TOKEN = "<redacted>";

  private final ObjectMapper objectMapper;

  public FcmMessageWriter(ObjectMapper objectMapper) {
    this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
  }

  public String write(String recipientToken, NotificationMessage message) {
    if (recipientToken == null || recipientToken.isBlank()) {
      throw new IllegalArgumentException("recipientToken must not be blank");
    }
    Objects.requireNonNull(message, "message must not be null");

    var fcmMessage = new LinkedHashMap<String, Object>();
    fcmMessage.put("token", recipientToken);
    fcmMessage.put("notification", Map.of("title", message.title(), "body", message.body()));
    fcmMessage.put("data", Map.of("eventId", message.eventId()));
    if (message.collapseKey() != null) {
      // A retried send after an unconfirmed attempt replaces the earlier notification on the
      // device rather than showing a duplicate.
      fcmMessage.put("android", Map.of("collapse_key", message.collapseKey()));
      fcmMessage.put(
          "apns", Map.of("headers", Map.of("apns-collapse-id", message.collapseKey())));
    }
    var payload = Map.of("message", fcmMessage);
    try {
      return objectMapper.writeValueAsString(payload);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Could not serialize FCM message.", exception);
    }
  }

  public String writeRedacted(NotificationMessage message) {
    return write(REDACTED_TOKEN, message);
  }
}
