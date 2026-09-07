package com.onthisday.quiz;

import static com.onthisday.quiz.QuizChecks.requireNonNull;

import java.util.HashSet;
import java.util.List;

public record PlayableQuizQuestion(
    QuizQuestion question, List<String> presentationOrderIds) {

  public PlayableQuizQuestion {
    question = requireNonNull(question, "question");
    if (presentationOrderIds == null) {
      throw new InvalidQuizDefinitionException("presentationOrderIds must not be null");
    }
    presentationOrderIds = List.copyOf(presentationOrderIds);

    var expectedIds = expectedIds(question);
    if (presentationOrderIds.size() != expectedIds.size()
        || new HashSet<>(presentationOrderIds).size() != presentationOrderIds.size()
        || !new HashSet<>(presentationOrderIds).equals(new HashSet<>(expectedIds))) {
      throw new InvalidQuizDefinitionException(
          "presentationOrderIds must contain every answer ID exactly once");
    }
    if (question instanceof TrueFalseQuestion
        && !presentationOrderIds.equals(List.of("true", "false"))) {
      throw new InvalidQuizDefinitionException(
          "true/false presentation order must be true then false");
    }
  }

  private static List<String> expectedIds(QuizQuestion question) {
    return switch (question) {
      case MultipleChoiceQuestion choice -> choice.options().stream().map(QuizOption::id).toList();
      case TrueFalseQuestion trueFalse -> trueFalse.options().stream().map(QuizOption::id).toList();
      case ImageIdentificationQuestion image -> image.options().stream().map(QuizOption::id).toList();
      case ChronologicalOrderingQuestion ordering ->
          ordering.items().stream().map(ChronologicalOrderingItem::id).toList();
    };
  }
}
