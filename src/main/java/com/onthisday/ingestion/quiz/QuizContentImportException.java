package com.onthisday.ingestion.quiz;

import com.onthisday.ingestion.ContentValidationError;
import java.util.List;

public class QuizContentImportException extends RuntimeException {

  private final List<ContentValidationError> validationErrors;

  public QuizContentImportException(String message) {
    super(message);
    this.validationErrors = List.of();
  }

  public QuizContentImportException(String message, Throwable cause) {
    super(message, cause);
    this.validationErrors = List.of();
  }

  public QuizContentImportException(List<ContentValidationError> validationErrors) {
    super("Curated quiz content validation failed.");
    this.validationErrors = List.copyOf(validationErrors);
  }

  public List<ContentValidationError> validationErrors() {
    return validationErrors;
  }
}
