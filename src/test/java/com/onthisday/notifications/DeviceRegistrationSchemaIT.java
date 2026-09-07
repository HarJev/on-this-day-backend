package com.onthisday.notifications;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class DeviceRegistrationSchemaIT {

  @Container
  static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("on_this_day")
          .withUsername("on_this_day")
          .withPassword("on_this_day");

  @BeforeAll
  static void migrateDatabase() {
    Flyway.configure()
        .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
        .locations("classpath:db/migration")
        .load()
        .migrate();
  }

  @Test
  void allowsValidDeviceRegistration() {
    assertDoesNotThrow(
        () ->
            inTransaction(
                connection ->
                    insertRegistration(
                        connection, "fcm-token", "ios", "America/Jamaica", "authorized")));
  }

  @Test
  void rejectsBlankToken() {
    assertThrows(
        SQLException.class,
        () ->
            inTransaction(
                connection ->
                    insertRegistration(connection, " ", "ios", "America/Jamaica", "authorized")));
  }

  @Test
  void rejectsInvalidPlatform() {
    assertThrows(
        SQLException.class,
        () ->
            inTransaction(
                connection ->
                    insertRegistration(connection, "bad-platform", "web", "America/Jamaica", "authorized")));
  }

  @Test
  void rejectsInvalidPermissionStatus() {
    assertThrows(
        SQLException.class,
        () ->
            inTransaction(
                connection ->
                    insertRegistration(connection, "bad-status", "ios", "America/Jamaica", "unknown")));
  }

  private static void insertRegistration(
      Connection connection, String token, String platform, String timezone, String permissionStatus)
      throws SQLException {
    try (var statement =
        connection.prepareStatement(
            """
            INSERT INTO device_registration (
              token,
              platform,
              timezone,
              notification_permission_status
            )
            VALUES (?, ?, ?, ?)
            """)) {
      statement.setString(1, token);
      statement.setString(2, platform);
      statement.setString(3, timezone);
      statement.setString(4, permissionStatus);
      statement.executeUpdate();
    }
  }

  private static void inTransaction(SqlWork work) throws SQLException {
    try (var connection =
        DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())) {
      connection.setAutoCommit(false);
      try {
        work.run(connection);
        connection.commit();
      } catch (SQLException | RuntimeException exception) {
        connection.rollback();
        throw exception;
      }
    }
  }

  @FunctionalInterface
  private interface SqlWork {
    void run(Connection connection) throws SQLException;
  }
}
