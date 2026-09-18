package com.onthisday.ingestion.media;

import com.onthisday.ingestion.ContentValidationError;
import java.util.List;

/** Raised when a dry-run cannot prove that every staged asset is publishable. */
public final class OwnedImageValidationException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private final List<ContentValidationError> validationErrors;

  public OwnedImageValidationException(String message, Throwable cause) {
    super(message, cause);
    this.validationErrors = List.of();
  }

  public OwnedImageValidationException(List<ContentValidationError> validationErrors) {
    super("Owned image manifest validation failed.");
    this.validationErrors = List.copyOf(validationErrors);
  }

  public List<ContentValidationError> validationErrors() {
    return validationErrors;
  }
}
