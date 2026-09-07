package com.onthisday.quiz;

import static com.onthisday.quiz.QuizChecks.requireNonBlank;
import static com.onthisday.quiz.QuizChecks.requireSlug;

public record ChronologicalOrderingItem(String id, String text, int correctPosition) {

  public ChronologicalOrderingItem {
    id = requireSlug(id, "id");
    text = requireNonBlank(text, "text");
    if (correctPosition < 1 || correctPosition > 4) {
      throw new InvalidQuizDefinitionException("correctPosition must be between 1 and 4");
    }
  }
}
