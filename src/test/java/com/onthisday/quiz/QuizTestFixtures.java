package com.onthisday.quiz;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

final class QuizTestFixtures {

  private QuizTestFixtures() {}

  static List<QuizQuestionCandidate> balancedCandidates(int copies) {
    var candidates = new ArrayList<QuizQuestionCandidate>();
    add(candidates, QuestionType.MULTIPLE_CHOICE, QuizDifficulty.EASY, 3 * copies);
    add(candidates, QuestionType.MULTIPLE_CHOICE, QuizDifficulty.MEDIUM, 7 * copies);
    add(candidates, QuestionType.MULTIPLE_CHOICE, QuizDifficulty.HARD, 2 * copies);
    add(candidates, QuestionType.TRUE_FALSE, QuizDifficulty.EASY, copies);
    add(candidates, QuestionType.TRUE_FALSE, QuizDifficulty.MEDIUM, copies);
    add(candidates, QuestionType.TRUE_FALSE, QuizDifficulty.HARD, copies);
    add(candidates, QuestionType.IMAGE_IDENTIFICATION, QuizDifficulty.EASY, copies);
    add(candidates, QuestionType.IMAGE_IDENTIFICATION, QuizDifficulty.MEDIUM, 2 * copies);
    add(candidates, QuestionType.CHRONOLOGICAL_ORDERING, QuizDifficulty.MEDIUM, copies);
    add(candidates, QuestionType.CHRONOLOGICAL_ORDERING, QuizDifficulty.HARD, copies);
    return List.copyOf(candidates);
  }

  static QuizQuestion question(QuizQuestionCandidate candidate) {
    var id = candidate.questionId();
    return switch (candidate.type()) {
      case MULTIPLE_CHOICE ->
          new MultipleChoiceQuestion(
              id,
              candidate.difficulty(),
              QuestionPublicationState.PUBLISHED,
              "Prompt " + id,
              "Explanation " + id,
              sources(),
              options());
      case TRUE_FALSE ->
          new TrueFalseQuestion(
              id,
              candidate.difficulty(),
              QuestionPublicationState.PUBLISHED,
              "Prompt " + id,
              "Explanation " + id,
              sources(),
              List.of(
                  new QuizOption("true", "True", 1, true),
                  new QuizOption("false", "False", 2, false)));
      case IMAGE_IDENTIFICATION ->
          new ImageIdentificationQuestion(
              id,
              candidate.difficulty(),
              QuestionPublicationState.PUBLISHED,
              "Prompt " + id,
              "Explanation " + id,
              sources(),
              image(),
              options());
      case CHRONOLOGICAL_ORDERING ->
          new ChronologicalOrderingQuestion(
              id,
              candidate.difficulty(),
              QuestionPublicationState.PUBLISHED,
              "Prompt " + id,
              "Explanation " + id,
              sources(),
              orderingItems());
    };
  }

  static List<QuizOption> options() {
    return List.of(
        new QuizOption("option-1", "Option 1", 1, true),
        new QuizOption("option-2", "Option 2", 2, false),
        new QuizOption("option-3", "Option 3", 3, false),
        new QuizOption("option-4", "Option 4", 4, false));
  }

  static List<ChronologicalOrderingItem> orderingItems() {
    return List.of(
        new ChronologicalOrderingItem("item-1", "Item 1", 1),
        new ChronologicalOrderingItem("item-2", "Item 2", 2),
        new ChronologicalOrderingItem("item-3", "Item 3", 3),
        new ChronologicalOrderingItem("item-4", "Item 4", 4));
  }

  private static void add(
      List<QuizQuestionCandidate> candidates,
      QuestionType type,
      QuizDifficulty difficulty,
      int count) {
    for (var index = 1; index <= count; index++) {
      candidates.add(
          new QuizQuestionCandidate(
              type.value().replace('_', '-') + "-" + difficulty.value() + "-" + index,
              type,
              difficulty));
    }
  }

  private static List<QuizSource> sources() {
    return List.of(new QuizSource("Source", URI.create("https://example.com/source")));
  }

  private static QuizImage image() {
    return new QuizImage(
        URI.create("https://example.com/image.jpg"),
        "Alt text",
        "Archive",
        URI.create("https://example.com/image-source"),
        "Archive",
        null,
        "Public domain",
        URI.create("https://example.com/license"));
  }
}
