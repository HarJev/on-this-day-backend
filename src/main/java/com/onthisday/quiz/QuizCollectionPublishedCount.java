package com.onthisday.quiz;

import static com.onthisday.quiz.QuizChecks.requireNonNull;

public record QuizCollectionPublishedCount(QuizCollection collection, int publishedQuestionCount) {

  public QuizCollectionPublishedCount {
    collection = requireNonNull(collection, "collection");
    if (publishedQuestionCount < 0) {
      throw new InvalidQuizDefinitionException("publishedQuestionCount must not be negative");
    }
  }
}
