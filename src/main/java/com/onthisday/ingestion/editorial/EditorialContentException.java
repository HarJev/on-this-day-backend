package com.onthisday.ingestion.editorial;

public class EditorialContentException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public EditorialContentException(String message) {
    super(message);
  }

  public EditorialContentException(String message, Throwable cause) {
    super(message, cause);
  }
}
