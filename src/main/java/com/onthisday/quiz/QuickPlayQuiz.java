package com.onthisday.quiz;

import java.util.List;
import java.util.Optional;

public record QuickPlayQuiz(
    Optional<QuizCollection> collection, List<PlayableQuizQuestion> questions) {

  public QuickPlayQuiz {
    if (collection == null) {
      throw new InvalidQuizDefinitionException("collection must not be null");
    }
    if (questions == null || questions.isEmpty()) {
      throw new InvalidQuizDefinitionException("questions must not be empty");
    }
    questions = List.copyOf(questions);
    if (!QuizRules.questionCounts().contains(questions.size())) {
      throw new InvalidQuizDefinitionException("questions must contain 5, 10, or 20 entries");
    }
  }
}
