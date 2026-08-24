package com.onthisday.ingestion;

public record ContentValidationWarning(String path, String message) {

  public ContentValidationWarning {
    if (path == null || path.isBlank()) {
      throw new IllegalArgumentException("path must not be blank");
    }
    if (message == null || message.isBlank()) {
      throw new IllegalArgumentException("message must not be blank");
    }
  }
}
