package com.onthisday.notifications;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Objects;

/**
 * When the daily notification is scheduled, in each device's local time. A device becomes due at
 * {@code sendAt}; a run that is late, or a retryable failure, can still send until {@code cutoff},
 * after which that local date is skipped for the device.
 *
 * <p>{@code claimLease} must be longer than the notification function's timeout, so a claim can
 * only expire once the run that took it has stopped.
 */
public record NotificationSchedule(
    LocalTime sendAt, LocalTime cutoff, Duration claimLease, int retentionDays) {

  public static final NotificationSchedule DEFAULT =
      new NotificationSchedule(LocalTime.of(10, 0), LocalTime.of(12, 0), Duration.ofMinutes(15), 30);

  public NotificationSchedule {
    Objects.requireNonNull(sendAt, "sendAt must not be null");
    Objects.requireNonNull(cutoff, "cutoff must not be null");
    Objects.requireNonNull(claimLease, "claimLease must not be null");
    if (!sendAt.isBefore(cutoff)) {
      throw new IllegalArgumentException("sendAt must be before cutoff");
    }
    if (claimLease.isNegative() || claimLease.isZero()) {
      throw new IllegalArgumentException("claimLease must be positive");
    }
    if (retentionDays < 2) {
      throw new IllegalArgumentException("retentionDays must be at least 2");
    }
  }

  public boolean isWithinWindow(LocalTime localTime) {
    return !localTime.isBefore(sendAt) && localTime.isBefore(cutoff);
  }
}
