package com.onthisday.ingestion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onthisday.ingestion.editorial.EditorialContentReader;
import com.onthisday.ingestion.editorial.EditorialReviewValidator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class ClosedBetaRunwayDraftTest {

  private static final Path BATCHES = Path.of("editorial", "batches");

  @Test
  void validatesRunwayDraftsAgainstTheirEditorialRecordsAndCanonicalContent() throws IOException {
    var objectMapper = new ObjectMapper();
    var canonical = new CuratedContentReader(objectMapper).read(Path.of("content"));
    var events = new ArrayList<>(canonical.eventsFile().events());
    var days = new ArrayList<>(canonical.dailyEventsFile().days());

    try (var batchPaths = Files.list(BATCHES)) {
      for (var batchPath : batchPaths
          .filter(ClosedBetaRunwayDraftTest::isRunwayBatch)
          .sorted()
          .toList()) {
        validateBatch(objectMapper, batchPath, events, days);
      }
    }

    var validation =
        new CuratedContentValidator()
            .validate(new CuratedEventsFile(events), new CuratedDailyEventsFile(days));

    assertTrue(
        validation.valid(),
        () ->
            validation.errors().stream()
                .map(error -> error.path() + ": " + error.message())
                .collect(Collectors.joining(System.lineSeparator())));
  }

  private static void validateBatch(
      ObjectMapper objectMapper,
      Path batchPath,
      List<CuratedEventJson> allEvents,
      List<CuratedDayJson> allDays)
      throws IOException {
    var batch = new EditorialContentReader(objectMapper).readBatch(batchPath.resolve("batch.json"));
    var review = new EditorialReviewValidator().validate(batch);
    assertTrue(review.valid(), () -> batchPath + " has invalid candidate or review data");

    var draftEvents =
        objectMapper.readValue(batchPath.resolve("draft-events.json").toFile(), CuratedEventsFile.class);
    var draftDays =
        objectMapper.readValue(
            batchPath.resolve("draft-daily-events.json").toFile(), CuratedDailyEventsFile.class);

    var eventsById =
        draftEvents.events().stream()
            .collect(Collectors.toMap(CuratedEventJson::id, event -> event));
    var candidatesById =
        batch.candidates().candidates().stream()
            .collect(
                Collectors.toMap(
                    candidate -> candidate.proposedCanonicalId(), candidate -> candidate));
    var ledgerById =
        batch.reviewLedger().entries().stream()
            .collect(Collectors.toMap(entry -> entry.canonicalId(), entry -> entry));

    assertEquals(eventsById.keySet(), candidatesById.keySet(), () -> batchPath + " candidates drift from drafts");
    assertEquals(eventsById.keySet(), ledgerById.keySet(), () -> batchPath + " ledger drifts from drafts");

    for (var event : draftEvents.events()) {
      var sourceUrls = event.sources().stream().map(CuratedSourceJson::url).toList();
      assertEquals(sourceUrls, candidatesById.get(event.id()).sourceUrls(), () -> event.id() + " candidate sources drift");
      assertEquals(
          sourceUrls,
          ledgerById.get(event.id()).sourceChecks().stream().map(check -> check.url()).toList(),
          () -> event.id() + " ledger sources drift");
    }

    var draftMonthDays =
        draftDays.days().stream()
            .map(day -> String.format("%02d-%02d", day.month(), day.day()))
            .collect(Collectors.toSet());
    assertEquals(
        new HashSet<>(batch.manifest().monthDays()),
        draftMonthDays,
        () -> batchPath + " manifest days drift from draft days");

    var referencedIds = new HashSet<String>();
    for (var day : draftDays.days()) {
      referencedIds.add(day.featuredEventId());
      referencedIds.addAll(day.additionalEventIds());
    }
    assertEquals(eventsById.keySet(), referencedIds, () -> batchPath + " daily assignments drift from drafts");

    allEvents.addAll(draftEvents.events());
    allDays.addAll(draftDays.days());
  }

  private static boolean isRunwayBatch(Path path) {
    // A promoted batch keeps its ledger but no longer has drafts: its events are canonical.
    return path.getFileName().toString().matches("2026-(10|11|12)-\\d{2}-\\d{2}-historical-events")
        && Files.exists(path.resolve("draft-events.json"));
  }
}
