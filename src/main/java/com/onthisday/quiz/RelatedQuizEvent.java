package com.onthisday.quiz;

/** Editorially linked history, for post-answer navigation only. */
public record RelatedQuizEvent(String id, String title, String year) {
  public RelatedQuizEvent {
    QuizChecks.requireSlug(id, "event id");
    QuizChecks.requireNonBlank(title, "event title");
    QuizChecks.requireNonBlank(year, "event year");
  }
}
