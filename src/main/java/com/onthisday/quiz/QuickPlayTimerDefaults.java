package com.onthisday.quiz;

import static com.onthisday.quiz.QuizChecks.requireNonNull;

import java.util.EnumMap;
import java.util.Map;

public record QuickPlayTimerDefaults(Map<QuestionType, Integer> secondsByQuestionType) {

  public QuickPlayTimerDefaults {
    requireNonNull(secondsByQuestionType, "secondsByQuestionType");
    var copy = new EnumMap<QuestionType, Integer>(QuestionType.class);
    copy.putAll(secondsByQuestionType);
    for (var type : QuestionType.values()) {
      var seconds = copy.get(type);
      if (seconds == null || seconds <= 0) {
        throw new InvalidQuizDefinitionException("timer seconds must be positive for " + type.value());
      }
    }
    secondsByQuestionType = Map.copyOf(copy);
  }

  public int secondsFor(QuestionType type) {
    return secondsByQuestionType.get(requireNonNull(type, "type"));
  }
}
