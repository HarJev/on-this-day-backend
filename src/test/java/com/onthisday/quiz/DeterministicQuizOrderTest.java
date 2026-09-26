package com.onthisday.quiz;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class DeterministicQuizOrderTest {

  @Test
  void ordersByTheCompleteUnsignedSha256DigestWithStableNamespace() {
    var candidates =
        List.of(
            candidate("gamma"), candidate("alpha"), candidate("delta"), candidate("beta"));

    var ordered =
        DeterministicQuizOrder.candidates(LocalDate.of(2026, 9, 7), candidates);

    assertEquals(
        List.of("delta", "beta", "alpha", "gamma"),
        ordered.stream().map(QuizQuestionCandidate::questionId).toList());
    assertEquals("on-this-day:daily-selection:v1:", DeterministicQuizOrder.DAILY_SELECTION_NAMESPACE);
    assertEquals(
        "on-this-day:daily-presentation:v1:",
        DeterministicQuizOrder.DAILY_PRESENTATION_NAMESPACE);
  }

  private static QuizQuestionCandidate candidate(String id) {
    return new QuizQuestionCandidate(
        id, QuestionType.MULTIPLE_CHOICE, QuizDifficulty.MEDIUM);
  }
}
