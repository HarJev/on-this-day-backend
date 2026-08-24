package com.onthisday.platform.runtime;

import java.util.Map;

public record DatabaseConfig(
    String jdbcUrl, String user, String password, int connectTimeoutSeconds, int socketTimeoutSeconds) {

  public static final String DB_JDBC_URL = "DB_JDBC_URL";
  public static final String DB_USER = "DB_USER";
  public static final String DB_PASSWORD = "DB_PASSWORD";
  public static final String DB_CONNECT_TIMEOUT_SECONDS = "DB_CONNECT_TIMEOUT_SECONDS";
  public static final String DB_SOCKET_TIMEOUT_SECONDS = "DB_SOCKET_TIMEOUT_SECONDS";
  public static final int DEFAULT_CONNECT_TIMEOUT_SECONDS = 5;
  public static final int DEFAULT_SOCKET_TIMEOUT_SECONDS = 10;

  public DatabaseConfig(String jdbcUrl, String user, String password) {
    this(jdbcUrl, user, password, DEFAULT_CONNECT_TIMEOUT_SECONDS, DEFAULT_SOCKET_TIMEOUT_SECONDS);
  }

  public DatabaseConfig {
    jdbcUrl = requireNonBlank(jdbcUrl, DB_JDBC_URL);
    user = requireNonBlank(user, DB_USER);
    password = requireNonBlank(password, DB_PASSWORD);
    connectTimeoutSeconds = requirePositive(connectTimeoutSeconds, DB_CONNECT_TIMEOUT_SECONDS);
    socketTimeoutSeconds = requirePositive(socketTimeoutSeconds, DB_SOCKET_TIMEOUT_SECONDS);
  }

  public static DatabaseConfig fromEnvironment() {
    return from(System.getenv());
  }

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
            DEFAULT_SOCKET_TIMEOUT_SECONDS));
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
