package com.onthisday.ingestion.editorial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ContentCoverageReporterTest {

  @Test
  void reportsLeapDayCoverageReviewStatusesAndExplicitAdjacentDayOverlap() {
    var report =
        new ContentCoverageReporter()
            .report(
                EditorialWorkflowFixtures.sevenDayHistoricalContent(),
                EditorialWorkflowFixtures.twentyQuestionQuizContent(),
                List.of(EditorialWorkflowFixtures.approvedBatch().reviewLedger()));

    var historical = section(report, "historical");
    var quiz = section(report, "quiz");

    assertTrue(strings(historical, "missingMonthDays").contains("02-29"));
    assertTrue(list(historical, "belowFourTotalEvents").isEmpty());
    assertEquals(28, list(historical, "sourceLinks").size());
    assertTrue(list(historical, "sourceLinks").stream().allMatch(item -> "verified_supporting".equals(item.get("status"))));
    assertEquals(20, ((Map<?, ?>) quiz.get("publishedByType")).get("multiple_choice"));
    assertFalse(list(quiz, "adjacentDayQuestionOverlap").isEmpty());
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> section(Map<String, Object> report, String name) {
    return (Map<String, Object>) report.get(name);
  }

  @SuppressWarnings("unchecked")
  private static List<Map<String, Object>> list(Map<String, Object> section, String name) {
    return (List<Map<String, Object>>) section.get(name);
  }

  @SuppressWarnings("unchecked")
  private static List<String> strings(Map<String, Object> section, String name) {
    return (List<String>) section.get(name);
  }
}
