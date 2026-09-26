package com.onthisday.quiz;

/** Indicates that persisted quiz content could not be read as a complete playable quiz. */
public class QuizUnavailableException extends RuntimeException {

  public QuizUnavailableException(String message) {
    super(message);
  }

  public QuizUnavailableException(String message, Throwable cause) {
    super(message, cause);
  }
}
