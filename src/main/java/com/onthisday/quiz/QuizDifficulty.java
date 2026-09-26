package com.onthisday.quiz;

public enum QuizDifficulty {
  EASY("easy"),
  MEDIUM("medium"),
  HARD("hard");

  private final String value;

  QuizDifficulty(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }

  public static QuizDifficulty fromValue(String value) {
    for (var difficulty : values()) {
      if (difficulty.value.equals(value)) {
        return difficulty;
      }
    }
    throw new InvalidQuizDefinitionException("Unsupported quiz difficulty: " + value);
  }
}
