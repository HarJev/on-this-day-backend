package com.onthisday.content;

import java.util.List;

final class ContentChecks {

  private ContentChecks() {}

  static String requireNonBlank(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(fieldName + " must not be blank");
    }
    return value;
  }

  static <T> List<T> copyNonEmpty(List<T> values, String fieldName) {
    var copy = List.copyOf(values);
    if (copy.isEmpty()) {
      throw new IllegalArgumentException(fieldName + " must not be empty");
    }
    return copy;
  }
}
