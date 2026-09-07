package com.onthisday.notifications;

public record NotificationMessage(String title, String body, String eventId) {

  public NotificationMessage {
    title = requireNonBlank(title, "title");
    body = requireNonBlank(body, "body");
    eventId = requireNonBlank(eventId, "eventId");
  }

  private static String requireNonBlank(String value, String name) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(name + " must not be blank");
    }
    return value;
  }
}
