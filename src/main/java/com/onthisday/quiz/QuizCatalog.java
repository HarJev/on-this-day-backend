package com.onthisday.quiz;

import static com.onthisday.quiz.QuizChecks.requireNonNull;

import java.util.List;

public record QuizCatalog(
    List<Integer> questionCounts,
    QuickPlayTimerDefaults quickPlayTimerDefaults,
    QuizSelectionAvailability mixed,
    List<QuizCatalogCollection> collections) {

  public QuizCatalog {
    if (questionCounts == null) {
      throw new InvalidQuizDefinitionException("questionCounts must not be null");
    }
    questionCounts = List.copyOf(questionCounts);
    quickPlayTimerDefaults = requireNonNull(quickPlayTimerDefaults, "quickPlayTimerDefaults");
    mixed = requireNonNull(mixed, "mixed");
    if (collections == null) {
      throw new InvalidQuizDefinitionException("collections must not be null");
    }
    collections = List.copyOf(collections);
  }
}
