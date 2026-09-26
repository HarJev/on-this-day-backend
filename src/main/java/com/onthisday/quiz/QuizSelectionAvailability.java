package com.onthisday.quiz;

import java.util.List;

public record QuizSelectionAvailability(
    int publishedQuestionCount, List<Integer> supportedQuestionCounts) {

  public QuizSelectionAvailability {
    if (publishedQuestionCount < 0) {
      throw new InvalidQuizDefinitionException("publishedQuestionCount must not be negative");
    }
    if (supportedQuestionCounts == null) {
      throw new InvalidQuizDefinitionException("supportedQuestionCounts must not be null");
    }
    supportedQuestionCounts = List.copyOf(supportedQuestionCounts);
  }
}
