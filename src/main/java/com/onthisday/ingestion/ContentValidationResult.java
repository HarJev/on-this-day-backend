package com.onthisday.ingestion;

import java.util.List;

public record ContentValidationResult(
    List<ContentValidationError> errors,
    List<ContentValidationWarning> warnings) {

  public ContentValidationResult {
    errors = List.copyOf(errors);
    warnings = List.copyOf(warnings);
  }

  public boolean valid() {
    return errors.isEmpty();
  }
}
