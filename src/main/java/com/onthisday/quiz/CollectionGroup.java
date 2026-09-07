package com.onthisday.quiz;

public enum CollectionGroup {
  TOPIC("topic"),
  HISTORICAL_PERIOD("historical_period"),
  CIVILIZATION("civilization"),
  CONFLICT_OR_MOVEMENT("conflict_or_movement");

  private final String value;

  CollectionGroup(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }

  public static CollectionGroup fromValue(String value) {
    for (var group : values()) {
      if (group.value.equals(value)) {
        return group;
      }
    }
    throw new InvalidQuizDefinitionException("Unsupported collection group: " + value);
  }
}
