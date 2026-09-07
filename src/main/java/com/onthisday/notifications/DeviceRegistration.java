package com.onthisday.notifications;

public record DeviceRegistration(
    String token,
    DevicePlatform platform,
    String timezone,
    NotificationPermissionStatus notificationPermissionStatus) {

  public DeviceRegistration {
    if (token == null || token.isBlank()) {
      throw new InvalidDeviceRegistrationException("Token is required.");
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
