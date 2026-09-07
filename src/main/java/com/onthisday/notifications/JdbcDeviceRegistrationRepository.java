package com.onthisday.notifications;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JdbcDeviceRegistrationRepository implements DeviceRegistrationRepository {

  private static final Logger LOG = LoggerFactory.getLogger(JdbcDeviceRegistrationRepository.class);

  private final DataSource dataSource;

  public JdbcDeviceRegistrationRepository(DataSource dataSource) {
    this.dataSource = Objects.requireNonNull(dataSource, "dataSource must not be null");
  }

  @Override
  public void upsert(DeviceRegistration registration) {
    LOG.info("device_registration_upsert_start platform={}", registration.platform().value());
    var startedAt = System.nanoTime();
    try (var connection = dataSource.getConnection();
        var statement =
            connection.prepareStatement(
                """
                INSERT INTO device_registration (
                  token,
                  platform,
                  timezone,
                  notification_permission_status
                )
                VALUES (?, ?, ?, ?)
                ON CONFLICT (token) DO UPDATE SET
                  platform = EXCLUDED.platform,
                  timezone = EXCLUDED.timezone,
                  notification_permission_status = EXCLUDED.notification_permission_status,
                  enabled = TRUE,
                  updated_at = now(),
                  last_registered_at = now()
                """)) {
      statement.setString(1, registration.token());
      statement.setString(2, registration.platform().value());
      statement.setString(3, registration.timezone());
      statement.setString(4, registration.notificationPermissionStatus().value());
      statement.executeUpdate();
      LOG.info("device_registration_upsert_end durationMs={}", durationMs(startedAt));
    } catch (SQLException exception) {
      LOG.warn("device_registration_upsert_failed platform={}", registration.platform().value(), exception);
      throw new IllegalStateException("Failed to upsert device registration.", exception);
    }
  }

  @Override
  public void deleteByToken(String token) {
    LOG.info("device_registration_delete_start");
    var startedAt = System.nanoTime();
    try (var connection = dataSource.getConnection();
        var statement = connection.prepareStatement("DELETE FROM device_registration WHERE token = ?")) {
      statement.setString(1, token);
      statement.executeUpdate();
      LOG.info("device_registration_delete_end durationMs={}", durationMs(startedAt));
    } catch (SQLException exception) {
      LOG.warn("device_registration_delete_failed", exception);
      throw new IllegalStateException("Failed to delete device registration.", exception);
    }
  }

  @Override
  public List<DeviceRegistration> findEligibleForNotifications() {
    LOG.info("device_registration_find_eligible_start");
    var startedAt = System.nanoTime();
    try (var connection = dataSource.getConnection();
        var statement =
            connection.prepareStatement(
                """
                SELECT token, platform, timezone, notification_permission_status
                FROM device_registration
                WHERE enabled = TRUE
                  AND notification_permission_status IN ('authorized', 'provisional')
                ORDER BY timezone, token
                """);
        var resultSet = statement.executeQuery()) {
      var registrations = new ArrayList<DeviceRegistration>();
      while (resultSet.next()) {
        registrations.add(
            new DeviceRegistration(
                resultSet.getString("token"),
                DevicePlatform.fromValue(resultSet.getString("platform")),
                resultSet.getString("timezone"),
                NotificationPermissionStatus.fromValue(
                    resultSet.getString("notification_permission_status"))));
      }
      LOG.info(
          "device_registration_find_eligible_end count={} durationMs={}",
          registrations.size(),
          durationMs(startedAt));
      return List.copyOf(registrations);
    } catch (SQLException exception) {
      LOG.warn("device_registration_find_eligible_failed", exception);
      throw new IllegalStateException("Failed to load eligible device registrations.", exception);
    }
  }

  private static long durationMs(long startedAt) {
    return (System.nanoTime() - startedAt) / 1_000_000;
  }
}
