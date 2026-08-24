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
}
