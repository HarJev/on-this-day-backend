package com.onthisday.notifications;

import java.time.Instant;
import java.time.LocalDate;

/** Records the scheduled daily notification for each device and recipient-local date. */
public interface NotificationDeliveryRepository {

  /**
   * Atomically takes ownership of the send for one device and local date. Succeeds when no row
   * exists, the last attempt was retryable, or an earlier claim's lease ended before {@code now}.
   * Fails when the date was already sent or another run holds an unexpired claim, so two runs
   * never send to the same device and date at the same time.
   */
  boolean claim(String token, LocalDate localDate, String eventId, Instant now, Instant leaseUntil);

  void markSent(String token, LocalDate localDate);

  void markRetryable(String token, LocalDate localDate, String errorCode);

  /** Deletes rows for local dates before {@code cutoff} and returns how many were removed. */
  int deleteBefore(LocalDate cutoff);
}
