package com.onthisday.ingestion.quiz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ProductionQuizContentTest {

  private static final Map<String, Integer> EXPECTED_TYPE_COUNTS =
      Map.of(
          "multiple_choice", 36,
          "true_false", 9,
          "image_identification", 9,
          "chronological_ordering", 6);
  private static final Map<String, Integer> EXPECTED_DIFFICULTY_COUNTS =
      Map.of("easy", 15, "medium", 33, "hard", 12);
  private static final Map<String, Integer> EXPECTED_COLLECTION_COUNTS =
      Map.of(
          "ancient-history", 15,
          "ancient-rome", 6,
          "wars-and-conflicts", 20,
          "leaders-and-power", 16,
          "revolutions", 10,
          "world-wars", 10,
          "science-and-innovation", 12,
          "exploration-and-exchange", 10,
          "society-culture-and-ideas", 10);

  @Test
  void canonicalQuestionBankMeetsReviewedQ7Contract() {
    var content =
        new QuizContentReader(new ObjectMapper()).read(Path.of("content/quizzes"));
    var validation = new QuizContentValidator(true).validate(content);
    assertTrue(validation.valid(), () -> "Validation errors: " + validation.errors());

    var questions =
        content.questionPacks().stream().flatMap(pack -> pack.file().questions().stream()).toList();
    assertEquals(6, content.questionPacks().size());
    assertEquals(60, questions.size());
    assertEquals(60, questions.stream().map(CuratedQuizQuestionJson::id).distinct().count());
    assertTrue(questions.stream().allMatch(question -> "published".equals(question.publicationState())));
    assertTrue(questions.stream().allMatch(question -> !question.collectionIds().isEmpty()));

    assertEquals(EXPECTED_TYPE_COUNTS, counts(questions.stream().map(CuratedQuizQuestionJson::type).toList()));
    assertEquals(
        EXPECTED_DIFFICULTY_COUNTS,
        counts(questions.stream().map(CuratedQuizQuestionJson::difficulty).toList()));
    assertEquals(
        EXPECTED_COLLECTION_COUNTS,
        counts(questions.stream().flatMap(question -> question.collectionIds().stream()).toList()));

    var images = questions.stream().filter(question -> question.image() != null).toList();
    assertEquals(9, images.size());
    for (var question : images) {
      var image = question.image();
      assertTrue(image.url().startsWith("https://upload.wikimedia.org/"));
      assertTrue(image.sourceUrl().startsWith("https://commons.wikimedia.org/wiki/File:"));
      assertFalse(image.url().equals(image.sourceUrl()));
      assertFalse(image.attribution().isBlank());
      assertFalse(image.license().isBlank());
      assertTrue(image.licenseUrl().startsWith("https://"));
      var correctAnswer =
          question.options().stream()
              .filter(option -> option.id().equals(question.correctOptionId()))
              .findFirst()
              .orElseThrow()
              .text()
              .toLowerCase();
      assertFalse(
          image.altText().toLowerCase().contains(correctAnswer),
          () -> "Image alt text reveals the answer for " + question.id());
    }

    assertTrue(
        questions.stream()
            .flatMap(question -> question.sources().stream())
            .noneMatch(source -> source.url().contains("wikipedia.org")));
  }

  private static Map<String, Integer> counts(Iterable<String> values) {
    var counts = new HashMap<String, Integer>();
    values.forEach(value -> counts.merge(value, 1, Integer::sum));
    return Map.copyOf(counts);
  }
}
