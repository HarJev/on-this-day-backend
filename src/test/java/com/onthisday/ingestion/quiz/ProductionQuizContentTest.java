package com.onthisday.ingestion.quiz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ProductionQuizContentTest {

  @Test
  void canonicalQuestionBankMeetsProductionContentInvariants() {
    var content =
        new QuizContentReader(new ObjectMapper()).read(Path.of("content/quizzes"));
    var validation = new QuizContentValidator(true).validate(content);
    assertTrue(validation.valid(), () -> "Validation errors: " + validation.errors());

    var questions =
        content.questionPacks().stream().flatMap(pack -> pack.file().questions().stream()).toList();
    assertTrue(content.questionPacks().size() >= 6);
    assertEquals(questions.size(), questions.stream().map(CuratedQuizQuestionJson::id).distinct().count());
    // Retirement is explicit in content (see QUIZ_SCHEMA.md), so the canonical bank may hold
    // retired questions alongside published ones, but never drafts.
    assertTrue(
        questions.stream()
            .allMatch(question -> Set.of("published", "retired").contains(question.publicationState())));
    assertTrue(
        questions.stream().filter(question -> "published".equals(question.publicationState())).count()
            >= 60);
    assertTrue(questions.stream().allMatch(question -> !question.collectionIds().isEmpty()));

    assertTrue(questions.stream().map(CuratedQuizQuestionJson::type).distinct().count() == 4);
    assertTrue(questions.stream().map(CuratedQuizQuestionJson::difficulty).distinct().count() == 3);

    var images = questions.stream().filter(question -> question.image() != null).toList();
    for (var question : images) {
      var image = question.image();
      assertTrue(image.url().startsWith("https://"));
      assertTrue(image.sourceUrl().startsWith("https://"));
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

}
