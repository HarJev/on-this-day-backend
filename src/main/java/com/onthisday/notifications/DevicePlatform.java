package com.onthisday.notifications;

public enum DevicePlatform {
  IOS("ios"),
  ANDROID("android");

  private final String value;

  DevicePlatform(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }

  public static DevicePlatform fromValue(String value) {
    for (var platform : values()) {
      if (platform.value.equals(value)) {
        return platform;
      }
    }
    throw new InvalidDeviceRegistrationException("Invalid platform.");
  }
}
