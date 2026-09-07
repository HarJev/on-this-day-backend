package com.onthisday.quiz;

import static com.onthisday.quiz.QuizChecks.requireNonBlank;
import static com.onthisday.quiz.QuizChecks.requireNonNull;
import static com.onthisday.quiz.QuizChecks.requireSlug;


public record QuizCollection(String id, String name, CollectionGroup group) {

  public QuizCollection {
    id = requireSlug(id, "id");
    name = requireNonBlank(name, "name");
    group = requireNonNull(group, "group");
  }
}
