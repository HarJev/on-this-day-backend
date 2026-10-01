package com.onthisday.notifications;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/** Mirrors the claim rules of {@link JdbcNotificationDeliveryRepository} for unit tests. */
final class InMemoryNotificationDeliveryRepository implements NotificationDeliveryRepository {

  record Row(String status, String eventId, int attempts, Instant claimedUntil, String errorCode) {}

  final Map<String, Row> rows = new LinkedHashMap<>();
  boolean failMarkSent;

  @Override
  public synchronized boolean claim(
      String token, LocalDate localDate, String eventId, Instant now, Instant leaseUntil) {
    var key = key(token, localDate);
    var existing = rows.get(key);
    if (existing == null) {
      rows.put(key, new Row("claimed", eventId, 1, leaseUntil, null));
      return true;
    }
    var reclaimable =
        existing.status().equals("retryable_failure")
            || (existing.status().equals("claimed") && existing.claimedUntil().isBefore(now));
    if (!reclaimable) {
      return false;
    }
    rows.put(key, new Row("claimed", eventId, existing.attempts() + 1, leaseUntil, existing.errorCode()));
    return true;
  }

  @Override
  public synchronized void markSent(String token, LocalDate localDate) {
    if (failMarkSent) {
      throw new IllegalStateException("simulated crash after Firebase accepted the message");
    }
    var row = rows.get(key(token, localDate));
    rows.put(key(token, localDate), new Row("sent", row.eventId(), row.attempts(), null, null));
  }

  @Override
  public synchronized void markRetryable(String token, LocalDate localDate, String errorCode) {
    var row = rows.get(key(token, localDate));
    rows.put(
        key(token, localDate),
        new Row("retryable_failure", row.eventId(), row.attempts(), null, errorCode));
  }

  @Override
  public synchronized int deleteBefore(LocalDate cutoff) {
    var before = rows.size();
    rows.keySet().removeIf(key -> LocalDate.parse(key.substring(key.lastIndexOf('|') + 1)).isBefore(cutoff));
    return before - rows.size();
  }

  void removeToken(String token) {
    rows.keySet().removeIf(key -> key.startsWith(token + "|"));
  }

  Row row(String token, LocalDate localDate) {
    return rows.get(key(token, localDate));
  }

  static String key(String token, LocalDate localDate) {
    return token + "|" + localDate;
  }
}
