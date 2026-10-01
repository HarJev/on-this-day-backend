package com.onthisday.platform.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class DatabaseConfigTest {

  @Test
  void readsRequiredDatabaseSettings() {
    var config =
        DatabaseConfig.from(
            Map.of(
                DatabaseConfig.DB_JDBC_URL,
                "jdbc:postgresql://localhost:5432/on_this_day",
                DatabaseConfig.DB_USER,
                "on_this_day",
                DatabaseConfig.DB_PASSWORD,
                "secret"));

    assertEquals("jdbc:postgresql://localhost:5432/on_this_day", config.jdbcUrl());
    assertEquals("on_this_day", config.user());
    assertEquals("secret", config.password());
    assertEquals(5, config.connectTimeoutSeconds());
    assertEquals(10, config.socketTimeoutSeconds());
  }

  @Test
  void readsOptionalTimeoutSettings() {
    var config =
        DatabaseConfig.from(
            Map.of(
                DatabaseConfig.DB_JDBC_URL,
                "jdbc:postgresql://localhost:5432/on_this_day",
                DatabaseConfig.DB_USER,
                "on_this_day",
                DatabaseConfig.DB_PASSWORD,
                "secret",
                DatabaseConfig.DB_CONNECT_TIMEOUT_SECONDS,
                "3",
                DatabaseConfig.DB_SOCKET_TIMEOUT_SECONDS,
                "7"));

    assertEquals(3, config.connectTimeoutSeconds());
    assertEquals(7, config.socketTimeoutSeconds());
  }

  @Test
  void rejectsMissingDatabaseSettings() {
    assertThrows(ConfigurationException.class, () -> DatabaseConfig.from(Map.of()));
    assertThrows(
        ConfigurationException.class,
        () ->
            DatabaseConfig.from(
                Map.of(
                    DatabaseConfig.DB_JDBC_URL,
                    " ",
                    DatabaseConfig.DB_USER,
                    "on_this_day",
                    DatabaseConfig.DB_PASSWORD,
                    "secret")));
  }

  @Test
  void rejectsInvalidTimeoutSettings() {
    assertThrows(
        ConfigurationException.class,
        () ->
            DatabaseConfig.from(
                Map.of(
                    DatabaseConfig.DB_JDBC_URL,
                    "jdbc:postgresql://localhost:5432/on_this_day",
                    DatabaseConfig.DB_USER,
                    "on_this_day",
                    DatabaseConfig.DB_PASSWORD,
                    "secret",
                    DatabaseConfig.DB_CONNECT_TIMEOUT_SECONDS,
                    "0")));
    assertThrows(
        ConfigurationException.class,
        () ->
            DatabaseConfig.from(
                Map.of(
                    DatabaseConfig.DB_JDBC_URL,
                    "jdbc:postgresql://localhost:5432/on_this_day",
                    DatabaseConfig.DB_USER,
                    "on_this_day",
                    DatabaseConfig.DB_PASSWORD,
                    "secret",
                    DatabaseConfig.DB_SOCKET_TIMEOUT_SECONDS,
                    "slow")));
  }

  @Test
  void productionPasswordComesFromSsmAndRequiresVerifiedTls() {
    var config =
        DatabaseConfig.resolve(
            Map.of(
                DatabaseConfig.DB_JDBC_URL, "jdbc:postgresql://db.example:6543/postgres",
                DatabaseConfig.DB_USER, "runtime",
                DatabaseConfig.DB_PASSWORD, "ignored",
                DatabaseConfig.DB_PASSWORD_SSM_PARAMETER, "/on-this-day/prod/db-password"),
            name -> "from-ssm");

    assertEquals("from-ssm", config.password());
    assertEquals("verify-full", config.sslMode());
    assertEquals(DatabaseConfig.SUPABASE_ROOT_CERT, config.sslRootCert());
    assertTrue(config.verifiesServer());
  }

  @Test
  void productionRefusesWeakerTls() {
    for (var mode : new String[] {"disable", "prefer", "require", "verify-ca"}) {
      assertThrows(
          ConfigurationException.class,
          () ->
              DatabaseConfig.resolve(
                  Map.of(
                      DatabaseConfig.DB_JDBC_URL, "jdbc:postgresql://db.example:6543/postgres",
                      DatabaseConfig.DB_USER, "runtime",
                      DatabaseConfig.DB_PASSWORD_SSM_PARAMETER, "/on-this-day/prod/db-password",
                      DatabaseConfig.DB_SSL_MODE, mode),
                  name -> "from-ssm"),
          mode);
    }
  }

  @Test
  void localSettingsDoNotReadSsmOrRequireTls() {
    var config =
        DatabaseConfig.resolve(
            Map.of(
                DatabaseConfig.DB_JDBC_URL, "jdbc:postgresql://localhost:5432/on_this_day",
                DatabaseConfig.DB_USER, "on_this_day",
                DatabaseConfig.DB_PASSWORD, "secret"),
            name -> {
              throw new AssertionError("SSM must not be read");
            });

    assertEquals("secret", config.password());
    assertNull(config.sslMode());
    assertFalse(config.verifiesServer());
  }

  @Test
  void rejectsUnknownSslMode() {
    assertThrows(
        ConfigurationException.class,
        () ->
            DatabaseConfig.from(
                Map.of(
                    DatabaseConfig.DB_JDBC_URL, "jdbc:postgresql://localhost:5432/on_this_day",
                    DatabaseConfig.DB_USER, "on_this_day",
                    DatabaseConfig.DB_PASSWORD, "secret",
                    DatabaseConfig.DB_SSL_MODE, "verify_full")));
  }

  @Test
  void toStringNeverIncludesThePassword() {
    var config = new DatabaseConfig("jdbc:postgresql://localhost/on_this_day", "on_this_day", "hunter2");

    assertFalse(config.toString().contains("hunter2"));
  }
}
