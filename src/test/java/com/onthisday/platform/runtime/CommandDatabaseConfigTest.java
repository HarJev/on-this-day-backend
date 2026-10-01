package com.onthisday.platform.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import org.junit.jupiter.api.Test;

class CommandDatabaseConfigTest {

  private static final String URL = "jdbc:postgresql://db.example:6543/postgres";
  private static final ParameterReader NO_SSM =
      name -> {
        throw new AssertionError("SSM must not be read");
      };
  private static final CommandDatabaseConfig.PasswordPrompt NO_PROMPT =
      user -> {
        throw new AssertionError("must not prompt");
      };

  @Test
  void readsTheProductionPasswordFromSsmWithVerifiedTls() {
    var config =
        CommandDatabaseConfig.resolve(
            Map.of(
                "DB_JDBC_URL", URL,
                "DB_USER", "postgres.ref",
                "DB_PASSWORD_SSM_PARAMETER", "/on-this-day/prod/db-admin-password"),
            name -> name.equals("/on-this-day/prod/db-admin-password") ? "from-ssm" : null,
            NO_PROMPT);

    assertEquals("from-ssm", config.password());
    assertEquals("verify-full", config.sslMode());
    assertEquals(DatabaseConfig.SUPABASE_ROOT_CERT, config.sslRootCert());
  }

  @Test
  void usesThePlainPasswordForLocalDatabases() {
    var config =
        CommandDatabaseConfig.resolve(
            Map.of("DB_JDBC_URL", "jdbc:postgresql://localhost/on_this_day", "DB_USER", "local", "DB_PASSWORD", "local-pw"),
            NO_SSM,
            NO_PROMPT);

    assertEquals("local-pw", config.password());
    assertNull(config.sslMode());
  }

  @Test
  void promptsWhenNoPasswordIsConfigured() {
    var config =
        CommandDatabaseConfig.resolve(
            Map.of("DB_JDBC_URL", URL, "DB_USER", "postgres.ref"),
            NO_SSM,
            user -> user.equals("postgres.ref") ? "typed".toCharArray() : null);

    assertEquals("typed", config.password());
  }

  @Test
  void failsWithoutATerminalOrPassword() {
    var exception =
        assertThrows(
            ConfigurationException.class,
            () ->
                CommandDatabaseConfig.resolve(
                    Map.of("DB_JDBC_URL", URL, "DB_USER", "postgres.ref"), NO_SSM, user -> null));

    assertEquals(
        "No database password. Set DB_PASSWORD_SSM_PARAMETER, or run from a terminal to be prompted.",
        exception.getMessage());
  }

  @Test
  void reportsAMissingUrlBeforePrompting() {
    assertThrows(
        ConfigurationException.class,
        () -> CommandDatabaseConfig.resolve(Map.of("DB_USER", "postgres.ref"), NO_SSM, NO_PROMPT));
  }
}
