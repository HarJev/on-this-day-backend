package com.onthisday.quiz;

import java.util.List;
import java.util.Map;

/** Shared Quiz v0.1.0 limits and timer defaults. */
public final class QuizRules {

  private static final List<Integer> QUESTION_COUNTS = List.of(5, 10, 20);
  private static final Map<QuestionType, Integer> QUICK_PLAY_TIMER_DEFAULTS_SECONDS =
      Map.of(
          QuestionType.MULTIPLE_CHOICE, 20,
          QuestionType.TRUE_FALSE, 20,
          QuestionType.IMAGE_IDENTIFICATION, 30,
          QuestionType.CHRONOLOGICAL_ORDERING, 45);

  private QuizRules() {}

  public static List<Integer> questionCounts() {
    return QUESTION_COUNTS;
  }

  public static Map<QuestionType, Integer> quickPlayTimerDefaultsSeconds() {
    return QUICK_PLAY_TIMER_DEFAULTS_SECONDS;
  }

  public static List<Integer> supportedQuestionCounts(int publishedQuestionCount) {
    if (publishedQuestionCount < 0) {
      throw new InvalidQuizDefinitionException("publishedQuestionCount must not be negative");
    }
    return QUESTION_COUNTS.stream().filter(count -> count <= publishedQuestionCount).toList();
  }
}
