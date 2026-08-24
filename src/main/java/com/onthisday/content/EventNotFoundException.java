package com.onthisday.content;

public class EventNotFoundException extends RuntimeException {

  public EventNotFoundException(String eventId) {
    super("Event not found: " + eventId);
  }
}
