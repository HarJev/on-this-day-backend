package com.onthisday.platform.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}
