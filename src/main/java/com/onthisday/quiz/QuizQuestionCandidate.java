package com.onthisday.quiz;

import static com.onthisday.quiz.QuizChecks.requireSlug;
import static com.onthisday.quiz.QuizChecks.requireNonNull;


public record QuizQuestionCandidate(
    String questionId, QuestionType type, QuizDifficulty difficulty) {

  public QuizQuestionCandidate {
    questionId = requireSlug(questionId, "questionId");
    type = requireNonNull(type, "type");
    difficulty = requireNonNull(difficulty, "difficulty");
  }
}
