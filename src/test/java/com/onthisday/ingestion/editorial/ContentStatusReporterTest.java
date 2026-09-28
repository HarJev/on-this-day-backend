package com.onthisday.ingestion.editorial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onthisday.ingestion.CuratedContentReader;
import com.onthisday.ingestion.editorial.ContentStatusReporter.BatchState;
import com.onthisday.ingestion.quiz.QuizContentReader;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ContentStatusReporterTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Test
  void reportsReviewStateAnswerPositionsAndFeaturedImagesWithoutADatabase() {
    var batch = EditorialWorkflowFixtures.approvedBatch();
    var historical = EditorialWorkflowFixtures.sevenDayHistoricalContent();
    var report =
        new ContentStatusReporter()
            .report(
                historical,
                EditorialWorkflowFixtures.twentyQuestionQuizContent(),
                List.of(
                    new BatchState(
                        batch.manifest().batchId(),
                        batch.reviewLedger(),
                        historical.eventsFile().events().subList(0, 1),
                        List.of())),
                Optional.empty());

    var editorial = section(report, "editorial");
    assertEquals(Map.of("checked", false), report.get("database"));
    assertEquals(List.of(historical.eventsFile().events().get(0).id()), editorial.get("draftEventsAlreadyCanonical"));
    assertEquals(List.of(), editorial.get("canonicalEventsWithoutApprovedReview"));
    assertEquals(20, ((Map<?, ?>) section(report, "quizAnswerPositions").get("multiple_choice")).values().stream().mapToInt(value -> (Integer) value).sum());
    assertEquals(3, section(section(report, "canonical"), "fingerprints").size());
  }

  @Test
  void fingerprintsIgnoreFileOrderButNotContent() {
    var fingerprints = Map.of("a", "1", "b", "2");
    assertEquals(
        ContentFingerprints.aggregate(fingerprints),
        ContentFingerprints.aggregate(new java.util.TreeMap<>(Map.of("b", "2", "a", "1"))));
    assertFalse(ContentFingerprints.aggregate(fingerprints).equals(ContentFingerprints.aggregate(Map.of("a", "1", "b", "3"))));
  }

  @Test
  void readsEveryCheckedInEditorialBatch() throws Exception {
    var batches = ContentStatusCommand.batches(OBJECT_MAPPER, Path.of("editorial/batches"));
    var report =
        new ContentStatusReporter()
            .report(
                new CuratedContentReader(OBJECT_MAPPER).read(Path.of("content")),
                new QuizContentReader(OBJECT_MAPPER).read(Path.of("content/quizzes")),
                batches,
                Optional.empty());

    assertFalse(batches.isEmpty());
    assertTrue(batches.stream().anyMatch(batch -> !batch.draftEvents().isEmpty()));
    assertEquals(batches.size(), ((List<?>) section(report, "editorial").get("batches")).size());
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> section(Map<String, Object> report, String name) {
    return (Map<String, Object>) report.get(name);
  }
}
