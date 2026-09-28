package com.onthisday.quiz;

import java.util.Objects;

/**
 * A published question with a reviewed link to an event curated for one calendar date.
 * {@code featured} is true when the linked event is that date's featured event.
 */
public record DateLinkedQuizCandidate(QuizQuestionCandidate candidate, boolean featured) {

  public DateLinkedQuizCandidate {
    Objects.requireNonNull(candidate, "candidate must not be null");
  }
}
