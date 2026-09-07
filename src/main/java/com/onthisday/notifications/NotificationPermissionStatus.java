package com.onthisday.notifications;

public enum NotificationPermissionStatus {
  AUTHORIZED("authorized"),
  PROVISIONAL("provisional"),
  DENIED("denied"),
  NOT_DETERMINED("not_determined");

  private final String value;

  NotificationPermissionStatus(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }

  public static NotificationPermissionStatus fromValue(String value) {
    for (var status : values()) {
      if (status.value.equals(value)) {
        return status;
      }
    }
    throw new InvalidDeviceRegistrationException("Invalid notification permission status.");
  }
}
