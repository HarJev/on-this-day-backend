package com.onthisday.platform.runtime;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Database settings for operator commands (migrations and content imports), so a production
 * password never appears in shell history or process arguments.
 *
 * <p>{@code DB_JDBC_URL} and {@code DB_USER} come from the environment. The password comes from,
 * in order: the SSM SecureString named by {@code DB_PASSWORD_SSM_PARAMETER} (which also requires
 * verified TLS, as in {@link DatabaseConfig#resolve}); {@code DB_PASSWORD}, for local databases;
 * or a hidden prompt on the terminal.
 */
public final class CommandDatabaseConfig {

  /** Asks the operator for the password without echoing it; null when there is no terminal. */
  @FunctionalInterface
  public interface PasswordPrompt {
    char[] readPassword(String user);
  }

  private CommandDatabaseConfig() {}

  public static DatabaseConfig fromEnvironment() {
    return fromEnvironment(System.getenv());
  }

  public static DatabaseConfig fromEnvironment(Map<String, String> environment) {
    return resolve(environment, new SsmParameterReader(), CommandDatabaseConfig::consolePrompt);
  }

  public static DatabaseConfig resolve(
      Map<String, String> values, ParameterReader reader, PasswordPrompt prompt) {
    Objects.requireNonNull(reader, "reader must not be null");
    Objects.requireNonNull(prompt, "prompt must not be null");
    var env = values == null ? Map.<String, String>of() : values;
    if (isSet(env.get(DatabaseConfig.DB_PASSWORD_SSM_PARAMETER)) || isSet(env.get(DatabaseConfig.DB_PASSWORD))) {
      return DatabaseConfig.resolve(env, reader);
    }
    var user = env.get(DatabaseConfig.DB_USER);
    if (!isSet(env.get(DatabaseConfig.DB_JDBC_URL)) || !isSet(user)) {
      // Report the missing URL or user before asking for anything.
      return DatabaseConfig.from(env);
    }
    var password = prompt.readPassword(user);
    if (password == null || password.length == 0) {
      throw new ConfigurationException(
          "No database password. Set "
              + DatabaseConfig.DB_PASSWORD_SSM_PARAMETER
              + ", or run from a terminal to be prompted.");
    }
    var resolved = new HashMap<>(env);
    resolved.put(DatabaseConfig.DB_PASSWORD, new String(password));
    Arrays.fill(password, '\0');
    return DatabaseConfig.from(resolved);
  }

  private static char[] consolePrompt(String user) {
    var console = System.console();
    return console == null ? null : console.readPassword("Database password for %s: ", user);
  }

  private static boolean isSet(String value) {
    return value != null && !value.isBlank();
  }
}
