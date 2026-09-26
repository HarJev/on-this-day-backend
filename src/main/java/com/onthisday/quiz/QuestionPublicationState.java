package com.onthisday.quiz;

public enum QuestionPublicationState {
  DRAFT("draft"),
  PUBLISHED("published"),
  RETIRED("retired");

  private final String value;

  QuestionPublicationState(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }

  public static QuestionPublicationState fromValue(String value) {
    for (var state : values()) {
      if (state.value.equals(value)) {
        return state;
      }
    }
    throw new InvalidQuizDefinitionException("Unsupported publication state: " + value);
  }
}
