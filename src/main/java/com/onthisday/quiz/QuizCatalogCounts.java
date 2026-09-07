package com.onthisday.quiz;

import java.util.List;

public record QuizCatalogCounts(int mixedPublishedQuestionCount, List<QuizCollectionPublishedCount> collections) {

  public QuizCatalogCounts {
    if (mixedPublishedQuestionCount < 0) {
      throw new InvalidQuizDefinitionException("mixedPublishedQuestionCount must not be negative");
    }
    if (collections == null) {
      throw new InvalidQuizDefinitionException("collections must not be null");
    }
    collections = List.copyOf(collections);
  }
}
