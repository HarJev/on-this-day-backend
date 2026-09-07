package com.onthisday.platform.notifications.fcm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
  void dryRunPayloadRedactsCompleteToken() {
    var json =
        writer.writeRedacted(new NotificationMessage("A title", "A body", "stable-event-id"));

    assertFalse(json.contains("device-token"));
    assertEquals(true, json.contains(FcmMessageWriter.REDACTED_TOKEN));
  }
}
