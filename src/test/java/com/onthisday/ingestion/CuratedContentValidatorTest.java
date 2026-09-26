package com.onthisday.ingestion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class CuratedContentValidatorTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final CuratedContentValidator validator = new CuratedContentValidator();

  @Test
  void validatesCuratedFixtureWithWarningsOnly() throws IOException {
    var result = validateFixture("valid");

    assertTrue(result.valid(), () -> "Unexpected validation errors: " + result.errors());
    assertTrue(result.errors().isEmpty());
    assertEquals(
        Set.of(
            "editorial exception declared",
            "featured event has no primary image"),
        warningMessages(result));
  }

  @Test
  void appliesTheFourTotalEventFloorAndEditorialExceptions() throws IOException {
    var events = readEvents("valid");
    var eventsWithFourTotal =
        new CuratedEventsFile(
            List.of(
                events.events().get(0),
                events.events().get(1),
                events.events().get(2),
                additionalEvent("additional-event-1903")));

    var atFloor =
        validator.validate(
            eventsWithFourTotal,
            new CuratedDailyEventsFile(
                List.of(
                    day(
                        List.of(
                            "additional-event-1901",
                            "additional-event-1902",
                            "additional-event-1903"),
                        null))));
    assertTrue(atFloor.valid());
    assertFalse(warningMessages(atFloor).contains("day has fewer than 4 total events"));

    var declaredException =
        validator.validate(
            events,
            new CuratedDailyEventsFile(
                List.of(
                    day(
                        List.of("additional-event-1901", "additional-event-1902"),
                        "Only three strong events are available after editorial review."))));
    assertTrue(declaredException.valid());
    assertTrue(warningMessages(declaredException).contains("editorial exception declared"));

    var blankException =
        validator.validate(
            events,
            new CuratedDailyEventsFile(
                List.of(
                    day(List.of("additional-event-1901", "additional-event-1902"), "  "))));
    assertFalse(blankException.valid());
    assertContainsError(blankException, "editorialException must not be blank when present");

    var staleException =
        validator.validate(
            eventsWithFourTotal,
            new CuratedDailyEventsFile(
                List.of(
                    day(
                        List.of(
                            "additional-event-1901",
                            "additional-event-1902",
                            "additional-event-1903"),
                        "The exception is no longer needed."))));
    assertTrue(staleException.valid());
    assertTrue(
        warningMessages(staleException)
            .contains("editorial exception declared for a day that meets the four-event floor"));
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
  void validatesTopLevelCuratedContent() {
    var reader = new CuratedContentReader(objectMapper);
    var content = reader.readDefault();

    var result = validator.validate(content.eventsFile(), content.dailyEventsFile());

    assertTrue(result.valid(), () -> "Unexpected validation errors: " + result.errors());
    assertTrue(result.errors().isEmpty());
    assertTrue(
        result.warnings().stream()
            .allMatch(
                warning ->
                    warning.message().equals("featured event has no primary image")
                        || warning.message().equals("day has fewer than 4 total events")));
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

  private static CuratedDayJson day(List<String> additionalEventIds, String editorialException) {
    return new CuratedDayJson(
        1, 2, "featured-event-1900", additionalEventIds, editorialException);
  }

  private static CuratedEventJson additionalEvent(String id) {
    return new CuratedEventJson(
        id,
        "An additional event happens",
        "1903",
        "January 2, 1903",
        null,
        "A concise summary for an additional event.",
        "A concise description for an additional event.",
        null,
        null,
        List.of(new CuratedSourceJson("Example Source", "https://example.com/" + id)),
        List.of());
  }

  private static void assertContainsError(ContentValidationResult result, String message) {
    assertTrue(
        result.errors().stream().anyMatch(error -> error.message().equals(message)),
        () -> "Expected error message not found: " + message + "\nActual errors: " + result.errors());
  }
}
