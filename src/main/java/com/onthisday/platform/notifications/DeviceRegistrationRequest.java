package com.onthisday.platform.notifications;

import com.onthisday.notifications.DevicePlatform;
import com.onthisday.notifications.DeviceRegistration;
import com.onthisday.notifications.InvalidDeviceRegistrationException;
import com.onthisday.notifications.NotificationPermissionStatus;

public record DeviceRegistrationRequest(
    String token, String platform, String timezone, String notificationPermissionStatus) {

  public DeviceRegistration toRegistration() {
    if (token == null || token.isBlank()) {
      throw new InvalidDeviceRegistrationException("Token is required.");
    }
    if (platform == null || platform.isBlank()) {
      throw new InvalidDeviceRegistrationException("Platform is required.");
    }
    if (timezone == null || timezone.isBlank()) {
      throw new InvalidDeviceRegistrationException("Timezone is required.");
    }
    if (notificationPermissionStatus == null || notificationPermissionStatus.isBlank()) {
      throw new InvalidDeviceRegistrationException("Notification permission status is required.");
    }

    return new DeviceRegistration(
        token,
        DevicePlatform.fromValue(platform),
        timezone,
        NotificationPermissionStatus.fromValue(notificationPermissionStatus));
  }
}
