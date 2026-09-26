package com.onthisday.quiz;

public enum QuestionType {
  MULTIPLE_CHOICE("multiple_choice"),
  TRUE_FALSE("true_false"),
  IMAGE_IDENTIFICATION("image_identification"),
  CHRONOLOGICAL_ORDERING("chronological_ordering");

  private final String value;

  QuestionType(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }

  public static QuestionType fromValue(String value) {
    for (var type : values()) {
      if (type.value.equals(value)) {
        return type;
      }
    }
    throw new InvalidQuizDefinitionException("Unsupported question type: " + value);
  }
}
