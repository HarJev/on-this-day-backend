package com.onthisday.content;

public class InvalidTimezoneException extends RuntimeException {

  public InvalidTimezoneException(String message) {
    super(message);
  }

  public InvalidTimezoneException(String message, Throwable cause) {
    super(message, cause);
  }
}
