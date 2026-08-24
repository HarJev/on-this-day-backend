package com.onthisday.content;

public class ContentUnavailableException extends RuntimeException {

  public ContentUnavailableException(String message) {
    super(message);
  }

  public ContentUnavailableException(String message, Throwable cause) {
    super(message, cause);
  }
}
