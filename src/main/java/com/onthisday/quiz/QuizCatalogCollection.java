package com.onthisday.quiz;

import static com.onthisday.quiz.QuizChecks.requireNonNull;

public record QuizCatalogCollection(QuizCollection collection, QuizSelectionAvailability availability) {

  public QuizCatalogCollection {
    collection = requireNonNull(collection, "collection");
    availability = requireNonNull(availability, "availability");
  }
}
