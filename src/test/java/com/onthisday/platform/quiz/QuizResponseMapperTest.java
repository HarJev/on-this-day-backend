package com.onthisday.platform.quiz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onthisday.quiz.ChronologicalOrderingItem;
import com.onthisday.quiz.ChronologicalOrderingQuestion;
import com.onthisday.quiz.ImageIdentificationQuestion;
import com.onthisday.quiz.MultipleChoiceQuestion;
import com.onthisday.quiz.PlayableQuizQuestion;
import com.onthisday.quiz.QuestionPublicationState;
import com.onthisday.quiz.QuizDifficulty;
import com.onthisday.quiz.QuizImage;
import com.onthisday.quiz.QuizOption;
import com.onthisday.quiz.QuizSource;
import com.onthisday.quiz.TrueFalseQuestion;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class QuizResponseMapperTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private final QuizResponseMapper mapper = new QuizResponseMapper();

  @Test
  void mapsAllQuestionShapesInPresentationOrderAndPreservesAnswers() throws Exception {
    var multipleChoice = multipleChoice("multiple-choice");
    var trueFalse = trueFalse();
    var image = image();
    var ordering = ordering();
    var response =
        mapper.toQuickPlayResponse(
            new com.onthisday.quiz.QuickPlayQuiz(
                Optional.empty(),
                List.of(
                    new PlayableQuizQuestion(
                        multipleChoice, List.of("option-4", "option-1", "option-2", "option-3")),
                    new PlayableQuizQuestion(trueFalse, List.of("true", "false")),
                    new PlayableQuizQuestion(
                        image, List.of("option-3", "option-2", "option-1", "option-4")),
                    new PlayableQuizQuestion(
                        ordering, List.of("item-4", "item-2", "item-3", "item-1")),
                    new PlayableQuizQuestion(multipleChoice("extra-choice"), List.of("option-1", "option-2", "option-3", "option-4")))));

    var root = OBJECT_MAPPER.readTree(OBJECT_MAPPER.writeValueAsString(response));
    var questions = root.path("questions");
    assertEquals("multiple_choice", questions.get(0).path("type").asText());
    assertEquals("option-4", questions.get(0).path("options").get(0).path("id").asText());
    assertEquals("option-1", questions.get(0).path("correctOptionId").asText());
    assertEquals(20, questions.get(0).path("timeLimitSeconds").asInt());
    assertEquals("true", questions.get(1).path("options").get(0).path("id").asText());
    assertEquals("image_identification", questions.get(2).path("type").asText());
    assertEquals("https://example.com/image-source", questions.get(2).path("image").path("sourceUrl").asText());
    assertEquals("chronological_ordering", questions.get(3).path("type").asText());
    assertEquals("item-4", questions.get(3).path("items").get(0).path("id").asText());
    assertEquals("item-1", questions.get(3).path("correctOrderItemIds").get(0).asText());
    assertFalse(questions.get(3).has("options"));
  }

  @Test
  void formatsDailyDateInEnglishAndOmitsPerQuestionTimeLimit() throws Exception {
    var question = multipleChoice("daily-choice");
    var response =
        mapper.toDailyResponse(
            new com.onthisday.quiz.DailyQuiz(
                LocalDate.of(2026, 8, 24),
                List.of(
                    new PlayableQuizQuestion(question, List.of("option-1", "option-2", "option-3", "option-4")),
                    new PlayableQuizQuestion(multipleChoice("daily-choice-2"), List.of("option-1", "option-2", "option-3", "option-4")),
                    new PlayableQuizQuestion(multipleChoice("daily-choice-3"), List.of("option-1", "option-2", "option-3", "option-4")),
                    new PlayableQuizQuestion(multipleChoice("daily-choice-4"), List.of("option-1", "option-2", "option-3", "option-4")),
                    new PlayableQuizQuestion(multipleChoice("daily-choice-5"), List.of("option-1", "option-2", "option-3", "option-4")))));

    var root = OBJECT_MAPPER.readTree(OBJECT_MAPPER.writeValueAsString(response));
    assertEquals("daily-2026-08-24", root.path("challengeId").asText());
    assertEquals("Aug 24", root.path("date").path("displayDate").asText());
    assertEquals(120, root.path("timer").path("durationSeconds").asInt());
    assertFalse(root.path("questions").get(0).has("timeLimitSeconds"));
  }

  private static MultipleChoiceQuestion multipleChoice(String id) {
    return new MultipleChoiceQuestion(
        id,
        QuizDifficulty.EASY,
        QuestionPublicationState.PUBLISHED,
        "Prompt",
        "Explanation",
        sources(),
        options());
  }

  private static TrueFalseQuestion trueFalse() {
    return new TrueFalseQuestion(
        "true-false",
        QuizDifficulty.MEDIUM,
        QuestionPublicationState.PUBLISHED,
        "Prompt",
        "Explanation",
        sources(),
        List.of(new QuizOption("true", "True", 1, true), new QuizOption("false", "False", 2, false)));
  }

  private static ImageIdentificationQuestion image() {
    return new ImageIdentificationQuestion(
        "image-question",
        QuizDifficulty.MEDIUM,
        QuestionPublicationState.PUBLISHED,
        "Prompt",
        "Explanation",
        sources(),
        new QuizImage(
            URI.create("https://example.com/image.jpg"),
            "Alt text",
            "Archive",
            URI.create("https://example.com/image-source"),
            "Attribution",
            "Creator",
            "Public domain",
            URI.create("https://example.com/license")),
        options());
  }

  private static ChronologicalOrderingQuestion ordering() {
    return new ChronologicalOrderingQuestion(
        "ordering-question",
        QuizDifficulty.HARD,
        QuestionPublicationState.PUBLISHED,
        "Prompt",
        "Explanation",
        sources(),
        List.of(
            new ChronologicalOrderingItem("item-1", "Item 1", 1),
            new ChronologicalOrderingItem("item-2", "Item 2", 2),
            new ChronologicalOrderingItem("item-3", "Item 3", 3),
            new ChronologicalOrderingItem("item-4", "Item 4", 4)));
  }

  private static List<QuizOption> options() {
    return List.of(
        new QuizOption("option-1", "Option 1", 1, true),
        new QuizOption("option-2", "Option 2", 2, false),
        new QuizOption("option-3", "Option 3", 3, false),
        new QuizOption("option-4", "Option 4", 4, false));
  }

  private static List<QuizSource> sources() {
    return List.of(new QuizSource("Source", URI.create("https://example.com/source")));
  }
}
