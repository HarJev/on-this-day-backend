package com.onthisday.notifications;

public class InvalidDeviceRegistrationException extends RuntimeException {

  public InvalidDeviceRegistrationException(String message) {
    super(message);
  }

  public InvalidDeviceRegistrationException(String message, Throwable cause) {
    super(message, cause);
  }
}
