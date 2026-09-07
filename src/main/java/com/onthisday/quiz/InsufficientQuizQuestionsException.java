package com.onthisday.quiz;

public class InsufficientQuizQuestionsException extends RuntimeException {

  public InsufficientQuizQuestionsException(int requestedCount, int availableCount) {
    super(
        "Requested "
            + requestedCount
            + " quiz questions, but only "
            + availableCount
            + " are available.");
  }
}
