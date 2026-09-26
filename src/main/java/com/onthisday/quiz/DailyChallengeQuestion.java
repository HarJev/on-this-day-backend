package com.onthisday.quiz;

import static com.onthisday.quiz.QuizChecks.requireSlug;

public record DailyChallengeQuestion(int position, String questionId) {

  public DailyChallengeQuestion {
    if (position < 1 || position > 20) {
      throw new InvalidQuizDefinitionException("position must be between 1 and 20");
    }
    questionId = requireSlug(questionId, "questionId");
  }
}
