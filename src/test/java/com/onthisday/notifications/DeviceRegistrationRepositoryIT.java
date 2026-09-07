package com.onthisday.notifications;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class DeviceRegistrationRepositoryIT {

  @Container
  static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("on_this_day")
          .withUsername("on_this_day")
          .withPassword("on_this_day");

  private static DataSource dataSource;

  @BeforeAll
  static void migrateDatabase() {
    var postgresDataSource = new PGSimpleDataSource();
    postgresDataSource.setUrl(POSTGRES.getJdbcUrl());
    postgresDataSource.setUser(POSTGRES.getUsername());
    postgresDataSource.setPassword(POSTGRES.getPassword());
    dataSource = postgresDataSource;

    Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").load().migrate();
  }

  @Test
  void upsertsRegistrationByToken() throws SQLException {
    var repository = new JdbcDeviceRegistrationRepository(dataSource);

    repository.upsert(
        new DeviceRegistration(
            "same-token",
            DevicePlatform.IOS,
            "America/Jamaica",
            NotificationPermissionStatus.AUTHORIZED));
    repository.upsert(
        new DeviceRegistration(
            "same-token",
            DevicePlatform.ANDROID,
            "America/New_York",
            NotificationPermissionStatus.DENIED));

    try (var connection = dataSource.getConnection();
        var statement =
            connection.prepareStatement(
                """
                SELECT platform, timezone, notification_permission_status, enabled, COUNT(*) OVER () AS row_count
                FROM device_registration
                WHERE token = ?
                """)) {
      statement.setString(1, "same-token");
      try (var resultSet = statement.executeQuery()) {
        resultSet.next();
        assertEquals("android", resultSet.getString("platform"));
        assertEquals("America/New_York", resultSet.getString("timezone"));
        assertEquals("denied", resultSet.getString("notification_permission_status"));
        assertEquals(true, resultSet.getBoolean("enabled"));
        assertEquals(1, resultSet.getInt("row_count"));
      }
    }
  }

  @Test
  void deleteExistingTokenRemovesRegistration() throws SQLException {
    var repository = new JdbcDeviceRegistrationRepository(dataSource);
    repository.upsert(
        new DeviceRegistration(
            "delete-token",
            DevicePlatform.IOS,
            "America/Jamaica",
            NotificationPermissionStatus.AUTHORIZED));

    repository.deleteByToken("delete-token");

    assertEquals(0, countToken("delete-token"));
  }

  @Test
  void deleteMissingTokenSucceeds() {
    var repository = new JdbcDeviceRegistrationRepository(dataSource);

    repository.deleteByToken("missing-token");
  }

  @Test
  void findsOnlyEnabledRegistrationsThatCanReceiveNotifications() throws SQLException {
    var repository = new JdbcDeviceRegistrationRepository(dataSource);
    repository.upsert(
        new DeviceRegistration(
            "authorized-token",
            DevicePlatform.IOS,
            "America/Jamaica",
            NotificationPermissionStatus.AUTHORIZED));
    repository.upsert(
        new DeviceRegistration(
            "provisional-token",
            DevicePlatform.IOS,
            "America/Jamaica",
            NotificationPermissionStatus.PROVISIONAL));
    repository.upsert(
        new DeviceRegistration(
            "denied-token",
            DevicePlatform.ANDROID,
            "UTC",
            NotificationPermissionStatus.DENIED));
    repository.upsert(
        new DeviceRegistration(
            "disabled-token",
            DevicePlatform.ANDROID,
            "UTC",
            NotificationPermissionStatus.AUTHORIZED));
    disableToken("disabled-token");

    var eligible = repository.findEligibleForNotifications();

    assertEquals(
        List.of("authorized-token", "provisional-token"),
        eligible.stream().map(DeviceRegistration::token).toList());
  }

  private static int countToken(String token) throws SQLException {
    try (var connection = dataSource.getConnection();
        var statement =
            connection.prepareStatement("SELECT COUNT(*) FROM device_registration WHERE token = ?")) {
      statement.setString(1, token);
      try (var resultSet = statement.executeQuery()) {
        resultSet.next();
        return resultSet.getInt(1);
      }
    }
  }

  private static void disableToken(String token) throws SQLException {
    try (var connection = dataSource.getConnection();
        var statement =
            connection.prepareStatement(
                "UPDATE device_registration SET enabled = FALSE WHERE token = ?")) {
      statement.setString(1, token);
      statement.executeUpdate();
    }
  }
}
