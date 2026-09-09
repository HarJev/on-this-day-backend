package com.onthisday.ingestion.quiz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onthisday.ingestion.ContentValidationResult;
import com.onthisday.ingestion.ContentValidationWarning;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class QuizContentValidatorTest {

  private final QuizContentReader reader = new QuizContentReader(new ObjectMapper());
  private final QuizContentValidator validator = new QuizContentValidator();

  @Test
  void validatesFixtureContainingAllFourQuestionTypesWithWarningsOnly() {
    var result = validator.validate(reader.read(Path.of("src/test/resources/ingestion/quiz/valid")));

    assertTrue(result.valid());
    assertTrue(result.errors().isEmpty());
    assertEquals(
        Set.of(
            "question has no collection membership",
            "collection ancient-history has fewer than 5 published questions",
            "collection spaceflight has fewer than 5 published questions"),
        warningMessages(result));
  }

  @Test
  void emptyContentIsValidWithNoPublishedQuestionWarning() {
    var result =
        validator.validate(reader.read(Path.of("src/test/resources/ingestion/quiz/empty")));

    assertTrue(result.valid());
    assertEquals(Set.of("no published quiz questions exist"), warningMessages(result));
  }

  @Test
  void reportsCollectionErrors() {
    var result = validateInvalidFixture();

    assertFalse(result.valid());
    assertContainsError(result, "collections.json:$.schemaVersion", "schemaVersion must be 1");
    assertContainsError(result, "collections.json:$.collections[0].id", "id must use lowercase letters, numbers, and hyphens");
    assertContainsError(result, "collections.json:$.collections[0].name", "collection name is required");
    assertContainsError(result, "collections.json:$.collections[0].group", "collection group is unsupported");
    assertContainsError(result, "collections.json:$.collections", "duplicate collection id: Bad Collection");
  }

  @Test
  void reportsChoiceQuestionErrors() {
    var result = validateInvalidFixture();

    assertContainsError(result, "questions/001-invalid.json:$.questions[0].id", "id must use lowercase letters, numbers, and hyphens");
    assertContainsError(result, "questions/001-invalid.json:$.questions[0].difficulty", "difficulty is unsupported");
    assertContainsError(result, "questions/001-invalid.json:$.questions[0].publicationState", "publicationState is unsupported");
    assertContainsError(result, "questions/001-invalid.json:$.questions[0].prompt", "prompt is required");
    assertContainsError(result, "questions/001-invalid.json:$.questions[0].explanation", "explanation is required");
    assertContainsError(result, "questions/001-invalid.json:$.questions[0].items", "choice question must not include items");
    assertContainsError(result, "questions/001-invalid.json:$.questions[0].correctOrderItemIds", "choice question must not include correctOrderItemIds");
    assertContainsError(result, "questions/001-invalid.json:$.questions[0].image", "only image_identification questions may include image");
    assertContainsError(result, "questions/001-invalid.json:$.questions[0].options", "options must contain exactly 4 entries");
    assertContainsError(result, "questions/001-invalid.json:$.questions[0].options[0].text", "option text is required");
    assertContainsError(result, "questions/001-invalid.json:$.questions[0].options[1].id", "duplicate option id: one");
    assertContainsError(result, "questions/001-invalid.json:$.questions[0].correctOptionId", "correctOptionId must reference an option");
  }

  @Test
  void reportsSourceCollectionAndImageErrors() {
    var result = validateInvalidFixture();

    assertContainsError(result, "questions/001-invalid.json:$.questions[0].sources[0].displayName", "source displayName is required");
    assertContainsError(result, "questions/001-invalid.json:$.questions[0].sources[0].url", "source url must be an absolute HTTPS URL");
    assertContainsError(result, "questions/001-invalid.json:$.questions[0].sources[1].url", "duplicate source url: http://example.com/source");
    assertContainsError(result, "questions/001-invalid.json:$.questions[0].collectionIds[0]", "collection id references an unknown collection");
    assertContainsError(result, "questions/001-invalid.json:$.questions[0].collectionIds[1]", "duplicate collection reference: missing-collection");
    assertContainsError(result, "questions/001-invalid.json:$.questions[0].image.url", "image url must be an absolute HTTPS URL");
    assertContainsError(result, "questions/001-invalid.json:$.questions[0].image.creator", "image creator must not be blank when present");
  }

  @Test
  void reportsTrueFalseAndChronologicalErrors() {
    var result = validateInvalidFixture();

    assertContainsError(result, "questions/001-invalid.json:$.questions[1].options[0]", "true_false option 1 must be id true with text True");
    assertContainsError(result, "questions/001-invalid.json:$.questions[1].options[1]", "true_false option 2 must be id false with text False");
    assertContainsError(result, "questions/001-invalid.json:$.questions[2].image", "image_identification question must include image");
    assertContainsError(result, "questions/001-invalid.json:$.questions[3].options", "chronological_ordering question must not include options");
    assertContainsError(result, "questions/001-invalid.json:$.questions[3].correctOptionId", "chronological_ordering question must not include correctOptionId");
    assertContainsError(result, "questions/001-invalid.json:$.questions[3].items", "items must contain exactly 4 entries");
    assertContainsError(result, "questions/001-invalid.json:$.questions[3].items[1].id", "duplicate item id: a");
    assertContainsError(result, "questions/001-invalid.json:$.questions[3].correctOrderItemIds", "correctOrderItemIds must contain exactly 4 entries");
    assertContainsError(result, "questions/001-invalid.json:$.questions[3].correctOrderItemIds[1]", "correctOrderItemId must reference an item");
    assertContainsError(result, "questions/001-invalid.json:$.questions[3].correctOrderItemIds[2]", "duplicate correctOrderItemId: a");
    assertContainsError(result, "questions", "duplicate question id: broken-true-false");
  }

  @Test
  void distributionWarningsAreOptInAndSuppressedForTinyFixtures() {
    var result =
        new QuizContentValidator(true).validate(reader.read(Path.of("src/test/resources/ingestion/quiz/valid")));

    assertTrue(result.valid());
    assertFalse(warningMessages(result).contains("published questions omit type multiple_choice"));
  }

  private ContentValidationResult validateInvalidFixture() {
    return validator.validate(reader.read(Path.of("src/test/resources/ingestion/quiz/invalid")));
  }

  private static Set<String> warningMessages(ContentValidationResult result) {
    return result.warnings().stream().map(ContentValidationWarning::message).collect(Collectors.toSet());
  }

  private static void assertContainsError(ContentValidationResult result, String path, String message) {
    assertTrue(
        result.errors().stream().anyMatch(error -> error.path().equals(path) && error.message().equals(message)),
        () -> "Expected error not found: " + path + " " + message + "\nActual errors: " + result.errors());
  }
}
