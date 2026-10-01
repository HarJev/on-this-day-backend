package com.onthisday.platform.runtime;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Database connection settings.
 *
 * <p>Locally, everything comes from environment variables, including {@code DB_PASSWORD}.
 * Deployed, {@code DB_PASSWORD_SSM_PARAMETER} names an SSM SecureString that holds the password,
 * so it never sits in a function's configuration or Terraform state. Naming that parameter also
 * switches on the production TLS rule: the connection must use {@code sslmode=verify-full}
 * against the Supabase CA bundled in the artifact, and anything weaker is refused.
 */
public record DatabaseConfig(
    String jdbcUrl,
    String user,
    String password,
    int connectTimeoutSeconds,
    int socketTimeoutSeconds,
    String sslMode,
    String sslRootCert) {

  public static final String DB_JDBC_URL = "DB_JDBC_URL";
  public static final String DB_USER = "DB_USER";
  public static final String DB_PASSWORD = "DB_PASSWORD";
  public static final String DB_PASSWORD_SSM_PARAMETER = "DB_PASSWORD_SSM_PARAMETER";
  public static final String DB_CONNECT_TIMEOUT_SECONDS = "DB_CONNECT_TIMEOUT_SECONDS";
  public static final String DB_SOCKET_TIMEOUT_SECONDS = "DB_SOCKET_TIMEOUT_SECONDS";
  public static final String DB_SSL_MODE = "DB_SSL_MODE";
  public static final String DB_SSL_ROOT_CERT = "DB_SSL_ROOT_CERT";
  public static final int DEFAULT_CONNECT_TIMEOUT_SECONDS = 5;
  public static final int DEFAULT_SOCKET_TIMEOUT_SECONDS = 10;
  public static final String VERIFY_FULL = "verify-full";
  /** Supabase's root CA, downloaded from the project's Database Settings page. */
  public static final String SUPABASE_ROOT_CERT = "classpath:certs/supabase-prod-ca-2021.crt";

  private static final Set<String> SSL_MODES =
      Set.of("disable", "allow", "prefer", "require", "verify-ca", VERIFY_FULL);

  public DatabaseConfig(String jdbcUrl, String user, String password) {
    this(jdbcUrl, user, password, DEFAULT_CONNECT_TIMEOUT_SECONDS, DEFAULT_SOCKET_TIMEOUT_SECONDS);
  }

  public DatabaseConfig(
      String jdbcUrl, String user, String password, int connectTimeoutSeconds, int socketTimeoutSeconds) {
    this(jdbcUrl, user, password, connectTimeoutSeconds, socketTimeoutSeconds, null, null);
  }

  public DatabaseConfig {
    jdbcUrl = requireNonBlank(jdbcUrl, DB_JDBC_URL);
    user = requireNonBlank(user, DB_USER);
    password = requireNonBlank(password, DB_PASSWORD);
    connectTimeoutSeconds = requirePositive(connectTimeoutSeconds, DB_CONNECT_TIMEOUT_SECONDS);
    socketTimeoutSeconds = requirePositive(socketTimeoutSeconds, DB_SOCKET_TIMEOUT_SECONDS);
    sslMode = blankToNull(sslMode);
    sslRootCert = blankToNull(sslRootCert);
    if (sslMode != null && !SSL_MODES.contains(sslMode)) {
      throw new ConfigurationException("Unknown " + DB_SSL_MODE + ": " + sslMode);
    }
  }

  public static DatabaseConfig fromEnvironment() {
    return from(System.getenv());
  }

  /** Reads every setting, including the password, from plain values. */
  public static DatabaseConfig from(Map<String, String> values) {
    var env = values == null ? Map.<String, String>of() : values;
    return new DatabaseConfig(
        env.get(DB_JDBC_URL),
        env.get(DB_USER),
        env.get(DB_PASSWORD),
        optionalPositiveInt(
            env.get(DB_CONNECT_TIMEOUT_SECONDS),
            DB_CONNECT_TIMEOUT_SECONDS,
            DEFAULT_CONNECT_TIMEOUT_SECONDS),
        optionalPositiveInt(
            env.get(DB_SOCKET_TIMEOUT_SECONDS),
            DB_SOCKET_TIMEOUT_SECONDS,
            DEFAULT_SOCKET_TIMEOUT_SECONDS),
        env.get(DB_SSL_MODE),
        env.get(DB_SSL_ROOT_CERT));
  }

  /**
   * Resolves the settings for a deployed function or a production command. When {@code
   * DB_PASSWORD_SSM_PARAMETER} is set, the password is read from SSM, {@code DB_PASSWORD} is
   * ignored, and verified TLS is required. Otherwise this is {@link #from(Map)}.
   */
  public static DatabaseConfig resolve(Map<String, String> values, ParameterReader reader) {
    var env = values == null ? Map.<String, String>of() : values;
    var parameterName = blankToNull(env.get(DB_PASSWORD_SSM_PARAMETER));
    if (parameterName == null) {
      return from(env);
    }
    var resolved = new HashMap<>(env);
    resolved.put(DB_PASSWORD, reader.readDecrypted(parameterName));
    return requireVerifiedTls(from(withProductionTlsDefaults(resolved)));
  }

  /** True when this connection must verify the server certificate and host name. */
  public boolean verifiesServer() {
    return VERIFY_FULL.equals(sslMode) && sslRootCert != null;
  }

  @Override
  public String toString() {
    // Never print the password.
    return "DatabaseConfig[user=" + user + ", sslMode=" + sslMode + "]";
  }

  private static Map<String, String> withProductionTlsDefaults(Map<String, String> env) {
    env.putIfAbsent(DB_SSL_MODE, VERIFY_FULL);
    env.putIfAbsent(DB_SSL_ROOT_CERT, SUPABASE_ROOT_CERT);
    return env;
  }

  private static DatabaseConfig requireVerifiedTls(DatabaseConfig config) {
    if (!config.verifiesServer()) {
      throw new ConfigurationException(
          "Production database connections require "
              + DB_SSL_MODE
              + "="
              + VERIFY_FULL
              + " and a root certificate.");
    }
    return config;
  }

  private static String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }

  private static String requireNonBlank(String value, String name) {
    if (value == null || value.isBlank()) {
      throw new ConfigurationException("Missing required environment variable: " + name);
    }
    return value;
  }

  private static int optionalPositiveInt(String value, String name, int defaultValue) {
    if (value == null || value.isBlank()) {
      return defaultValue;
    }

    try {
      return requirePositive(Integer.parseInt(value), name);
    } catch (NumberFormatException exception) {
      throw new ConfigurationException("Environment variable must be a positive integer: " + name, exception);
    }
  }

  private static int requirePositive(int value, String name) {
    if (value <= 0) {
      throw new ConfigurationException("Environment variable must be a positive integer: " + name);
    }
    return value;
  }
}
