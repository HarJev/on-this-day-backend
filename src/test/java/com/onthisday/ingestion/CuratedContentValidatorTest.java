package com.onthisday.ingestion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class CuratedContentValidatorTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final CuratedContentValidator validator = new CuratedContentValidator();

  @Test
  void validatesCuratedFixtureWithWarningsOnly() throws IOException {
    var result = validateFixture("valid");

    assertTrue(result.valid());
    assertTrue(result.errors().isEmpty());
    assertEquals(
        Set.of(
            "day has fewer than 6 additional events",
            "featured event has no primary image"),
        warningMessages(result));
  }

  @Test
  void reportsMissingAndInvalidEventFields() throws IOException {
    var result = validateFixture("invalid");

    assertFalse(result.valid());
    assertContainsError(result, "id must use lowercase letters, numbers, and hyphens");
    assertContainsError(result, "title is required");
    assertContainsError(result, "year is required");
    assertContainsError(result, "historicalDate is required");
    assertContainsError(result, "dateNote must not be blank when present");
    assertContainsError(result, "summary is required");
    assertContainsError(result, "description is required");
    assertContainsError(result, "duplicate event id: Bad ID");
  }

  @Test
  void reportsSourceAndImageValidationErrors() throws IOException {
    var result = validateFixture("invalid");

    assertContainsError(result, "event must have at least one source");
    assertContainsError(result, "image url must be an absolute HTTPS URL");
    assertContainsError(result, "image altText is required");
    assertContainsError(result, "image source is required");
    assertContainsError(result, "image creator must not be blank when present");
    assertContainsError(result, "image attribution is required");
    assertContainsError(result, "image license is required");
    assertContainsError(result, "image licenseUrl must be an absolute HTTPS URL");
    assertContainsError(result, "event must not have more than one primary image");
  }

  @Test
  void reportsDailyValidationErrors() throws IOException {
    var result = validateFixture("invalid");

    assertContainsError(result, "month/day must be a valid calendar date");
    assertContainsError(result, "duplicate day entry: 2/30");
    assertContainsError(result, "featured event must have notificationTitle and notificationBody");
    assertContainsError(result, "featuredEventId references an unknown event");
    assertContainsError(result, "featured event must not also be listed as additional");
    assertContainsError(result, "duplicate additional event id: unknown-event");
    assertContainsError(result, "additional event id references an unknown event");
  }

  @Test
  void exposesIntendedTopLevelContentLocations() {
    assertEquals(Path.of("content", "events.json"), ContentFileLocations.EVENTS);
    assertEquals(Path.of("content", "daily-events.json"), ContentFileLocations.DAILY_EVENTS);
  }

  @Test
  void validatesTopLevelAugustTwentyTwoContent() {
    var reader = new CuratedContentReader(objectMapper);
    var content = reader.readDefault();

    var result = validator.validate(content.eventsFile(), content.dailyEventsFile());

    assertTrue(result.valid());
    assertTrue(result.errors().isEmpty());
    assertTrue(result.warnings().isEmpty());
  }

  private ContentValidationResult validateFixture(String fixtureName) throws IOException {
    return validator.validate(readEvents(fixtureName), readDailyEvents(fixtureName));
  }

  private CuratedEventsFile readEvents(String fixtureName) throws IOException {
    try (var inputStream =
        getClass().getResourceAsStream("/ingestion/" + fixtureName + "/events.json")) {
      return objectMapper.readValue(inputStream, CuratedEventsFile.class);
    }
  }

  private CuratedDailyEventsFile readDailyEvents(String fixtureName) throws IOException {
    try (var inputStream =
        getClass().getResourceAsStream("/ingestion/" + fixtureName + "/daily-events.json")) {
      return objectMapper.readValue(inputStream, CuratedDailyEventsFile.class);
    }
  }

  private static Set<String> warningMessages(ContentValidationResult result) {
    return result.warnings().stream().map(ContentValidationWarning::message).collect(Collectors.toSet());
  }

  private static void assertContainsError(ContentValidationResult result, String message) {
    assertTrue(
        result.errors().stream().anyMatch(error -> error.message().equals(message)),
        () -> "Expected error message not found: " + message + "\nActual errors: " + result.errors());
  }
}
