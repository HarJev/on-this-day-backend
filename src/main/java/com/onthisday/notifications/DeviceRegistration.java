package com.onthisday.notifications;

public record DeviceRegistration(
    String token,
    DevicePlatform platform,
    String timezone,
    NotificationPermissionStatus notificationPermissionStatus) {

  /**
   * Push tokens are a few hundred characters at most (FCM about 160, APNs 64). The cap keeps an
   * anonymous caller from filling the database with oversized keys.
   */
  public static final int MAX_TOKEN_LENGTH = 1024;

  public DeviceRegistration {
    if (token == null || token.isBlank()) {
      throw new InvalidDeviceRegistrationException("Token is required.");
    }
    if (token.length() > MAX_TOKEN_LENGTH) {
      throw new InvalidDeviceRegistrationException("Token is too long.");
    }
    if (platform == null) {
      throw new InvalidDeviceRegistrationException("Platform is required.");
    }
    if (timezone == null || timezone.isBlank()) {
      throw new InvalidDeviceRegistrationException("Timezone is required.");
    }
    if (notificationPermissionStatus == null) {
      throw new InvalidDeviceRegistrationException("Notification permission status is required.");
    }
  }
}
