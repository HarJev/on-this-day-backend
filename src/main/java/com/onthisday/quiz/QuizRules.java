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
  private static final Map<Integer, Integer> DAILY_TOTAL_TIMER_SECONDS =
      Map.of(5, 120, 10, 240, 20, 480);
  private static final Map<Integer, Map<QuestionType, Integer>> QUESTION_TYPE_TARGETS =
      Map.of(
          5,
          Map.of(
              QuestionType.MULTIPLE_CHOICE, 2,
              QuestionType.TRUE_FALSE, 1,
              QuestionType.IMAGE_IDENTIFICATION, 1,
              QuestionType.CHRONOLOGICAL_ORDERING, 1),
          10,
          Map.of(
              QuestionType.MULTIPLE_CHOICE, 5,
              QuestionType.TRUE_FALSE, 2,
              QuestionType.IMAGE_IDENTIFICATION, 2,
              QuestionType.CHRONOLOGICAL_ORDERING, 1),
          20,
          Map.of(
              QuestionType.MULTIPLE_CHOICE, 12,
              QuestionType.TRUE_FALSE, 3,
              QuestionType.IMAGE_IDENTIFICATION, 3,
              QuestionType.CHRONOLOGICAL_ORDERING, 2));
  private static final Map<Integer, Map<QuizDifficulty, Integer>> DIFFICULTY_TARGETS =
      Map.of(
          5,
          Map.of(
              QuizDifficulty.EASY, 1,
              QuizDifficulty.MEDIUM, 3,
              QuizDifficulty.HARD, 1),
          10,
          Map.of(
              QuizDifficulty.EASY, 3,
              QuizDifficulty.MEDIUM, 5,
              QuizDifficulty.HARD, 2),
          20,
          Map.of(
              QuizDifficulty.EASY, 5,
              QuizDifficulty.MEDIUM, 11,
              QuizDifficulty.HARD, 4));

  private QuizRules() {}

  public static List<Integer> questionCounts() {
    return QUESTION_COUNTS;
  }

  public static Map<QuestionType, Integer> quickPlayTimerDefaultsSeconds() {
    return QUICK_PLAY_TIMER_DEFAULTS_SECONDS;
  }

  public static int dailyTotalTimerSeconds(int questionCount) {
    return requireDailyTimer(questionCount);
  }

  public static Map<QuestionType, Integer> questionTypeTargets(int questionCount) {
    return requireSupportedCount(QUESTION_TYPE_TARGETS, questionCount, "question type");
  }

  public static Map<QuizDifficulty, Integer> difficultyTargets(int questionCount) {
    return requireSupportedCount(DIFFICULTY_TARGETS, questionCount, "difficulty");
  }

  public static List<Integer> supportedQuestionCounts(int publishedQuestionCount) {
    if (publishedQuestionCount < 0) {
      throw new InvalidQuizDefinitionException("publishedQuestionCount must not be negative");
    }
    return QUESTION_COUNTS.stream().filter(count -> count <= publishedQuestionCount).toList();
  }

  public static void requireSupportedQuestionCount(int questionCount) {
    if (!QUESTION_COUNTS.contains(questionCount)) {
      throw new InvalidQuizRequestException("questionCount must be one of 5, 10, or 20");
    }
  }

  private static <K> Map<K, Integer> requireSupportedCount(
      Map<Integer, Map<K, Integer>> targets, int questionCount, String targetName) {
    var result = targets.get(questionCount);
    if (result == null) {
      throw new InvalidQuizRequestException(
          targetName + " targets are available only for question counts 5, 10, or 20");
    }
    return result;
  }

  private static int requireDailyTimer(int questionCount) {
    var result = DAILY_TOTAL_TIMER_SECONDS.get(questionCount);
    if (result == null) {
      throw new InvalidQuizRequestException(
          "Daily timer is available only for question counts 5, 10, or 20");
    }
    return result;
  }
}
