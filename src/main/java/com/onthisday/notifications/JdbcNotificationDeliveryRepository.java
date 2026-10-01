package com.onthisday.notifications;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JdbcNotificationDeliveryRepository implements NotificationDeliveryRepository {

  private static final Logger LOG =
      LoggerFactory.getLogger(JdbcNotificationDeliveryRepository.class);

  // ON CONFLICT ... DO UPDATE locks the existing row and re-checks the WHERE clause against its
  // committed state, so of two overlapping runs only one can turn a row into its own claim.
  private static final String CLAIM_SQL =
      """
      INSERT INTO notification_delivery (token, local_date, status, event_id, claimed_until)
      VALUES (?, ?, 'claimed', ?, ?)
      ON CONFLICT (token, local_date) DO UPDATE SET
        status = 'claimed',
        event_id = EXCLUDED.event_id,
        attempt_count = notification_delivery.attempt_count + 1,
        claimed_until = EXCLUDED.claimed_until,
        updated_at = now()
      WHERE notification_delivery.status = 'retryable_failure'
         OR (notification_delivery.status = 'claimed' AND notification_delivery.claimed_until < ?)
      RETURNING attempt_count
      """;

  private final DataSource dataSource;

  public JdbcNotificationDeliveryRepository(DataSource dataSource) {
    this.dataSource = Objects.requireNonNull(dataSource, "dataSource must not be null");
  }

  @Override
  public boolean claim(
      String token, LocalDate localDate, String eventId, Instant now, Instant leaseUntil) {
    try (var connection = dataSource.getConnection();
        var statement = connection.prepareStatement(CLAIM_SQL)) {
      statement.setString(1, token);
      statement.setDate(2, Date.valueOf(localDate));
      statement.setString(3, eventId);
      statement.setTimestamp(4, Timestamp.from(leaseUntil));
      statement.setTimestamp(5, Timestamp.from(now));
      try (var resultSet = statement.executeQuery()) {
        return resultSet.next();
      }
    } catch (SQLException exception) {
      LOG.warn("notification_delivery_claim_failed", exception);
      throw new IllegalStateException("Failed to claim notification delivery.", exception);
    }
  }

  @Override
  public void markSent(String token, LocalDate localDate) {
    update(
        """
        UPDATE notification_delivery
        SET status = 'sent', claimed_until = NULL, last_error_code = NULL, updated_at = now()
        WHERE token = ? AND local_date = ? AND status = 'claimed'
        """,
        token,
        localDate,
        null,
        "markSent");
  }

  @Override
  public void markRetryable(String token, LocalDate localDate, String errorCode) {
    update(
        """
        UPDATE notification_delivery
        SET status = 'retryable_failure', claimed_until = NULL, last_error_code = ?,
            updated_at = now()
        WHERE token = ? AND local_date = ? AND status = 'claimed'
        """,
        token,
        localDate,
        errorCode == null ? "UNKNOWN" : errorCode,
        "markRetryable");
  }

  @Override
  public int deleteBefore(LocalDate cutoff) {
    try (var connection = dataSource.getConnection();
        var statement =
            connection.prepareStatement("DELETE FROM notification_delivery WHERE local_date < ?")) {
      statement.setDate(1, Date.valueOf(cutoff));
      return statement.executeUpdate();
    } catch (SQLException exception) {
      LOG.warn("notification_delivery_prune_failed", exception);
      throw new IllegalStateException("Failed to prune notification deliveries.", exception);
    }
  }

  private void update(
      String sql, String token, LocalDate localDate, String errorCode, String operation) {
    try (var connection = dataSource.getConnection();
        var statement = connection.prepareStatement(sql)) {
      var index = 1;
      if (errorCode != null) {
        statement.setString(index++, errorCode);
      }
      statement.setString(index++, token);
      statement.setDate(index, Date.valueOf(localDate));
      statement.executeUpdate();
    } catch (SQLException exception) {
      LOG.warn("notification_delivery_update_failed operation={}", operation, exception);
      throw new IllegalStateException("Failed to update notification delivery.", exception);
    }
  }
}
