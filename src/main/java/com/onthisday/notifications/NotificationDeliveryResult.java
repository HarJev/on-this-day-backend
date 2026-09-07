package com.onthisday.notifications;

import java.util.Objects;

public record NotificationDeliveryResult(NotificationDeliveryStatus status, String errorCode) {

  public NotificationDeliveryResult {
    status = Objects.requireNonNull(status, "status must not be null");
  }

  public static NotificationDeliveryResult success() {
    return new NotificationDeliveryResult(NotificationDeliveryStatus.SUCCESS, null);
  }
}
