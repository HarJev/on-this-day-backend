package com.onthisday.ingestion;

import java.util.List;

public class ContentImportException extends RuntimeException {

  private final List<ContentValidationError> validationErrors;

  public ContentImportException(String message) {
    super(message);
    this.validationErrors = List.of();
  }

  public ContentImportException(String message, Throwable cause) {
    super(message, cause);
    this.validationErrors = List.of();
  }

  public ContentImportException(List<ContentValidationError> validationErrors) {
    super("Curated content validation failed.");
    this.validationErrors = List.copyOf(validationErrors);
  }

  public List<ContentValidationError> validationErrors() {
    return validationErrors;
  }
}
