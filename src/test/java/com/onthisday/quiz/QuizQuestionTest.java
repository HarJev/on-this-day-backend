package com.onthisday.quiz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class QuizQuestionTest {

  @Test
  void createsEachQuestionShapeWithItsFixedType() {
    var multipleChoice = multipleChoiceQuestion(options(4));
    var trueFalse =
        new TrueFalseQuestion(
            "printing-before-gutenberg",
            QuizDifficulty.MEDIUM,
            QuestionPublicationState.PUBLISHED,
            "Movable type existed before Gutenberg.",
            "It was developed in East Asia centuries earlier.",
            sources(),
            trueFalseOptions());
    var imageIdentification =
        new ImageIdentificationQuestion(
            "identify-bosworth",
            QuizDifficulty.MEDIUM,
            QuestionPublicationState.DRAFT,
            "Which battle is shown?",
            "The painting depicts the Battle of Bosworth Field.",
            sources(),
            image(),
            options(4));
    var chronological =
        new ChronologicalOrderingQuestion(
            "order-spaceflight-milestones",
            QuizDifficulty.HARD,
            QuestionPublicationState.RETIRED,
            "Put these milestones in order.",
            "They occurred between 1957 and 1969.",
            sources(),
            orderingItems());

    assertEquals(QuestionType.MULTIPLE_CHOICE, multipleChoice.type());
    assertEquals(QuestionType.TRUE_FALSE, trueFalse.type());
    assertEquals(QuestionType.IMAGE_IDENTIFICATION, imageIdentification.type());
    assertEquals(QuestionType.CHRONOLOGICAL_ORDERING, chronological.type());
  }

  @Test
  void defensivelyCopiesQuestionChildren() {
    var mutableOptions = new ArrayList<>(options(4));
    var mutableSources = new ArrayList<>(sources());
    var question =
        new MultipleChoiceQuestion(
            "copied-question",
            QuizDifficulty.EASY,
            QuestionPublicationState.DRAFT,
            "A prompt",
            "An explanation",
            mutableSources,
            mutableOptions);

    mutableOptions.clear();
    mutableSources.clear();

    assertEquals(4, question.options().size());
    assertEquals(1, question.sources().size());
    assertThrows(UnsupportedOperationException.class, () -> question.options().clear());
  }

  @Test
  void rejectsChoiceQuestionWithoutExactlyFourOptions() {
    assertThrows(InvalidQuizDefinitionException.class, () -> multipleChoiceQuestion(options(3)));
  }

  @Test
  void rejectsChoiceQuestionWithoutExactlyOneCorrectOption() {
    var options =
        List.of(
            new QuizOption("one", "One", 1, true),
            new QuizOption("two", "Two", 2, true),
            new QuizOption("three", "Three", 3, false),
            new QuizOption("four", "Four", 4, false));

    assertThrows(InvalidQuizDefinitionException.class, () -> multipleChoiceQuestion(options));
  }

  @Test
  void rejectsDuplicateOrNonContiguousOptionOrder() {
    var duplicateOrder =
        List.of(
            new QuizOption("one", "One", 1, true),
            new QuizOption("two", "Two", 1, false),
            new QuizOption("three", "Three", 3, false),
            new QuizOption("four", "Four", 4, false));

    assertThrows(InvalidQuizDefinitionException.class, () -> multipleChoiceQuestion(duplicateOrder));
  }

  @Test
  void rejectsMalformedTrueFalseOptions() {
    var malformed =
        List.of(
            new QuizOption("yes", "Yes", 1, true),
            new QuizOption("no", "No", 2, false));

    assertThrows(
        InvalidQuizDefinitionException.class,
        () ->
            new TrueFalseQuestion(
                "malformed-boolean",
                QuizDifficulty.EASY,
                QuestionPublicationState.DRAFT,
                "A prompt",
                "An explanation",
                sources(),
                malformed));
  }

  @Test
  void rejectsImageQuestionWithoutCompleteImage() {
    assertThrows(
        InvalidQuizDefinitionException.class,
        () ->
            new ImageIdentificationQuestion(
                "missing-image",
                QuizDifficulty.EASY,
                QuestionPublicationState.DRAFT,
                "A prompt",
                "An explanation",
                sources(),
                null,
                options(4)));

    assertThrows(
        InvalidQuizDefinitionException.class,
        () ->
            new QuizImage(
                URI.create("http://example.com/image.jpg"),
                "Alt text",
                "Archive",
                URI.create("https://example.com/source"),
                "Attribution",
                null,
                "Public domain",
                URI.create("https://example.com/license")));
  }

  @Test
  void rejectsOrderingQuestionWithoutFourDistinctContiguousItems() {
    var duplicatePosition =
        List.of(
            new ChronologicalOrderingItem("one", "One", 1),
            new ChronologicalOrderingItem("two", "Two", 2),
            new ChronologicalOrderingItem("three", "Three", 2),
            new ChronologicalOrderingItem("four", "Four", 4));

    assertThrows(
        InvalidQuizDefinitionException.class,
        () ->
            new ChronologicalOrderingQuestion(
                "bad-ordering",
                QuizDifficulty.HARD,
                QuestionPublicationState.DRAFT,
                "A prompt",
                "An explanation",
                sources(),
                duplicatePosition));
  }

  @Test
  void rejectsMissingCommonQuestionData() {
    assertThrows(
        InvalidQuizDefinitionException.class,
        () ->
            new MultipleChoiceQuestion(
                "Not A Slug",
                QuizDifficulty.EASY,
                QuestionPublicationState.DRAFT,
                " ",
                "An explanation",
                sources(),
                options(4)));
    assertThrows(
        InvalidQuizDefinitionException.class,
        () ->
            new MultipleChoiceQuestion(
                "missing-sources",
                QuizDifficulty.EASY,
                QuestionPublicationState.DRAFT,
                "A prompt",
                "An explanation",
                List.of(),
                options(4)));
  }

  private static MultipleChoiceQuestion multipleChoiceQuestion(List<QuizOption> options) {
    return new MultipleChoiceQuestion(
        "capital-of-aztec-empire",
        QuizDifficulty.EASY,
        QuestionPublicationState.PUBLISHED,
        "Which city was the Aztec capital?",
        "Tenochtitlan was the capital.",
        sources(),
        options);
  }

  private static List<QuizOption> options(int count) {
    var options = new ArrayList<QuizOption>();
    for (var index = 1; index <= count; index++) {
      options.add(new QuizOption("option-" + index, "Option " + index, index, index == 1));
    }
    return options;
  }

  private static List<QuizOption> trueFalseOptions() {
    return List.of(
        new QuizOption("true", "True", 1, true),
        new QuizOption("false", "False", 2, false));
  }

  private static List<ChronologicalOrderingItem> orderingItems() {
    return List.of(
        new ChronologicalOrderingItem("one", "One", 1),
        new ChronologicalOrderingItem("two", "Two", 2),
        new ChronologicalOrderingItem("three", "Three", 3),
        new ChronologicalOrderingItem("four", "Four", 4));
  }

  private static List<QuizSource> sources() {
    return List.of(
        new QuizSource(
            "Encyclopaedia Britannica", URI.create("https://www.britannica.com/")));
  }

  private static QuizImage image() {
    return new QuizImage(
        URI.create("https://example.com/image.jpg"),
        "A historical image",
        "Example Archive",
        URI.create("https://example.com/source"),
        "Example Archive",
        null,
        "Public domain",
        URI.create("https://example.com/license"));
  }
}
