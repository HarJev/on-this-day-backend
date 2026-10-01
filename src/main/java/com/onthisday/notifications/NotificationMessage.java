package com.onthisday.notifications;

/**
 * A notification for one featured event. {@code collapseKey} is optional; when present, devices
 * replace an earlier notification with the same key instead of showing a second one.
 */
public record NotificationMessage(String title, String body, String eventId, String collapseKey) {

  public NotificationMessage {
    title = requireNonBlank(title, "title");
    body = requireNonBlank(body, "body");
    eventId = requireNonBlank(eventId, "eventId");
    if (collapseKey != null) {
      collapseKey = requireNonBlank(collapseKey, "collapseKey");
    }
  }

  public NotificationMessage(String title, String body, String eventId) {
    this(title, body, eventId, null);
  }

  private static String requireNonBlank(String value, String name) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(name + " must not be blank");
    }
    return value;
  }
}
