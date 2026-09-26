package com.onthisday.quiz;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

final class QuizChecks {

  private static final Pattern SLUG = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");

  private QuizChecks() {}

  static String requireNonBlank(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      throw new InvalidQuizDefinitionException(fieldName + " must not be blank");
    }
    return value;
  }

  static String requireNullableNonBlank(String value, String fieldName) {
    if (value != null && value.isBlank()) {
      throw new InvalidQuizDefinitionException(fieldName + " must not be blank when present");
    }
    return value;
  }

  static String requireSlug(String value, String fieldName) {
    requireNonBlank(value, fieldName);
    if (!SLUG.matcher(value).matches()) {
      throw new InvalidQuizDefinitionException(fieldName + " must be a lowercase slug");
    }
    return value;
  }

  static URI requireHttps(URI value, String fieldName) {
    requireNonNull(value, fieldName);
    if (!value.isAbsolute()
        || !"https".equalsIgnoreCase(value.getScheme())
        || value.getHost() == null) {
      throw new InvalidQuizDefinitionException(fieldName + " must be an absolute HTTPS URI");
    }
    return value;
  }

  static <T> T requireNonNull(T value, String fieldName) {
    if (value == null) {
      throw new InvalidQuizDefinitionException(fieldName + " must not be null");
    }
    return value;
  }

  static <T> List<T> copyNonEmpty(List<T> values, String fieldName) {
    if (values == null) {
      throw new InvalidQuizDefinitionException(fieldName + " must not be null");
    }
    var copy = List.copyOf(values);
    if (copy.isEmpty()) {
      throw new InvalidQuizDefinitionException(fieldName + " must not be empty");
    }
    return copy;
  }

  static List<QuizOption> requireChoiceOptions(
      List<QuizOption> values, int expectedCount, boolean trueFalse) {
    var options = copyNonEmpty(values, "options");
    if (options.size() != expectedCount) {
      throw new InvalidQuizDefinitionException(
          "options must contain exactly " + expectedCount + " items");
    }

    Set<String> ids = new HashSet<>();
    Set<Integer> orders = new HashSet<>();
    var correctCount = 0;
    for (var option : options) {
      if (!ids.add(option.id())) {
        throw new InvalidQuizDefinitionException("option IDs must be unique");
      }
      if (!orders.add(option.displayOrder())) {
        throw new InvalidQuizDefinitionException("option display orders must be unique");
      }
      if (option.correct()) {
        correctCount++;
      }
    }

    if (correctCount != 1) {
      throw new InvalidQuizDefinitionException("options must contain exactly one correct answer");
    }
    for (var position = 1; position <= expectedCount; position++) {
      if (!orders.contains(position)) {
        throw new InvalidQuizDefinitionException("option display orders must be contiguous from 1");
      }
    }

    if (trueFalse) {
      requireBooleanOption(options, "true", "True", 1);
      requireBooleanOption(options, "false", "False", 2);
    }
    return options;
  }

  static List<ChronologicalOrderingItem> requireOrderingItems(
      List<ChronologicalOrderingItem> values) {
    var items = copyNonEmpty(values, "items");
    if (items.size() != 4) {
      throw new InvalidQuizDefinitionException("items must contain exactly 4 entries");
    }

    Set<String> ids = new HashSet<>();
    Set<Integer> positions = new HashSet<>();
    for (var item : items) {
      if (!ids.add(item.id())) {
        throw new InvalidQuizDefinitionException("item IDs must be unique");
      }
      if (!positions.add(item.correctPosition())) {
        throw new InvalidQuizDefinitionException("item positions must be unique");
      }
    }
    for (var position = 1; position <= 4; position++) {
      if (!positions.contains(position)) {
        throw new InvalidQuizDefinitionException("item positions must be contiguous from 1");
      }
    }
    return items;
  }

  private static void requireBooleanOption(
      List<QuizOption> options, String id, String text, int displayOrder) {
    var found =
        options.stream()
            .anyMatch(
                option ->
                    option.id().equals(id)
                        && option.text().equals(text)
                        && option.displayOrder() == displayOrder);
    if (!found) {
      throw new InvalidQuizDefinitionException(
          "true/false options must be True at position 1 and False at position 2");
    }
  }
}
