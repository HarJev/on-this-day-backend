package com.onthisday.platform.notifications.fcm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onthisday.notifications.NotificationMessage;
import org.junit.jupiter.api.Test;

class FcmMessageWriterTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final FcmMessageWriter writer = new FcmMessageWriter(objectMapper);

  @Test
  void writesNotificationAndStableEventIdDataPayload() throws Exception {
    var json =
        writer.write(
            "device-token", new NotificationMessage("A title", "A body", "stable-event-id"));
    var message = objectMapper.readTree(json).path("message");

    assertEquals("device-token", message.path("token").asText());
    assertEquals("A title", message.path("notification").path("title").asText());
    assertEquals("A body", message.path("notification").path("body").asText());
    assertEquals("stable-event-id", message.path("data").path("eventId").asText());
  }

  @Test
  void collapseKeyMakesARepeatReplaceTheEarlierNotification() throws Exception {
    var json =
        writer.write(
            "device-token",
            new NotificationMessage("A title", "A body", "event-id", "daily-2026-10-01"));
    var message = objectMapper.readTree(json).path("message");

    assertEquals("daily-2026-10-01", message.path("android").path("collapse_key").asText());
    assertEquals(
        "daily-2026-10-01",
        message.path("apns").path("headers").path("apns-collapse-id").asText());
  }

  @Test
  void omitsPlatformOverridesWithoutACollapseKey() throws Exception {
    var message =
        objectMapper
            .readTree(writer.write("device-token", new NotificationMessage("T", "B", "event-id")))
            .path("message");

    assertTrue(message.path("android").isMissingNode());
    assertTrue(message.path("apns").isMissingNode());
  }

  @Test
  void dryRunPayloadRedactsCompleteToken() {
    var json =
        writer.writeRedacted(new NotificationMessage("A title", "A body", "stable-event-id"));

    assertFalse(json.contains("device-token"));
    assertEquals(true, json.contains(FcmMessageWriter.REDACTED_TOKEN));
  }
}
