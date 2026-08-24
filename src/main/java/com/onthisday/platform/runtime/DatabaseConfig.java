package com.onthisday.platform.runtime;

import java.util.Map;

public record DatabaseConfig(String jdbcUrl, String user, String password) {

  public static final String DB_JDBC_URL = "DB_JDBC_URL";
  public static final String DB_USER = "DB_USER";
  public static final String DB_PASSWORD = "DB_PASSWORD";

  public DatabaseConfig {
    jdbcUrl = requireNonBlank(jdbcUrl, DB_JDBC_URL);
    user = requireNonBlank(user, DB_USER);
    password = requireNonBlank(password, DB_PASSWORD);
  }

  public static DatabaseConfig fromEnvironment() {
    return from(System.getenv());
  }

  public static DatabaseConfig from(Map<String, String> values) {
    var env = values == null ? Map.<String, String>of() : values;
    return new DatabaseConfig(env.get(DB_JDBC_URL), env.get(DB_USER), env.get(DB_PASSWORD));
  }

  private static String requireNonBlank(String value, String name) {
    if (value == null || value.isBlank()) {
      throw new ConfigurationException("Missing required environment variable: " + name);
    }
    return value;
  }
}
