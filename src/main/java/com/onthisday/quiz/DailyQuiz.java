package com.onthisday.quiz;

import static com.onthisday.quiz.QuizChecks.requireNonNull;

import java.time.LocalDate;
import java.util.List;

public record DailyQuiz(LocalDate date, List<PlayableQuizQuestion> questions) {

  public DailyQuiz {
    date = requireNonNull(date, "date");
    if (questions == null || questions.isEmpty()) {
      throw new InvalidQuizDefinitionException("questions must not be empty");
    }
    questions = List.copyOf(questions);
    if (!QuizRules.questionCounts().contains(questions.size())) {
      throw new InvalidQuizDefinitionException("questions must contain 5, 10, or 20 entries");
    }
  }
}
