package com.onthisday.quiz;

import static com.onthisday.quiz.QuizChecks.requireNonBlank;
import static com.onthisday.quiz.QuizChecks.requireSlug;

public record QuizOption(String id, String text, int displayOrder, boolean correct) {

  public QuizOption {
    id = requireSlug(id, "id");
    text = requireNonBlank(text, "text");
    if (displayOrder < 1 || displayOrder > 4) {
      throw new InvalidQuizDefinitionException("displayOrder must be between 1 and 4");
    }
  }
}
