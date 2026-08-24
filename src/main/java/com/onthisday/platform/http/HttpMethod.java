package com.onthisday.platform.http;

import java.util.Locale;

public enum HttpMethod {
  GET,
  POST,
  DELETE,
  UNKNOWN;

  public static HttpMethod from(String value) {
    if (value == null || value.isBlank()) {
      return UNKNOWN;
    }

    try {
      return HttpMethod.valueOf(value.toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException exception) {
      return UNKNOWN;
    }
  }
}
