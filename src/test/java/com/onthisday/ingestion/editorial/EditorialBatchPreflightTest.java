package com.onthisday.ingestion.editorial;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.onthisday.ingestion.CuratedContentValidator;
import com.onthisday.ingestion.quiz.QuizContentValidator;
import org.junit.jupiter.api.Test;

class EditorialBatchPreflightTest {

  @Test
  void selectsAndValidatesAnApprovedSevenDayAndTwentyQuestionBatchBeforeImport() {
    var result = preflight().validate(
        EditorialWorkflowFixtures.approvedBatch(),
        EditorialWorkflowFixtures.sevenDayHistoricalContent(),
        EditorialWorkflowFixtures.twentyQuestionQuizContent());

    assertTrue(result.valid());
    assertTrue(result.historicalContent().dailyEventsFile().days().size() == 7);
    assertTrue(result.quizContent().questionPacks().getFirst().file().questions().size() == 20);
  }

  @Test
  void refusesSelectedCanonicalContentWithoutApproval() {
    var batch = EditorialWorkflowFixtures.approvedBatch();
    var entries = new java.util.ArrayList<>(batch.reviewLedger().entries());
    entries.removeFirst();
    var invalidBatch = new EditorialBatch(
        batch.manifest(), batch.candidates(), new EditorialReviewLedger(1, batch.manifest().batchId(), entries));

    var result = preflight().validate(
        invalidBatch,
        EditorialWorkflowFixtures.sevenDayHistoricalContent(),
        EditorialWorkflowFixtures.twentyQuestionQuizContent());

    assertFalse(result.valid());
    assertTrue(result.approvalErrors().stream().anyMatch(error -> error.contains("no approved review entry")));
  }

  private static EditorialBatchPreflight preflight() {
    return new EditorialBatchPreflight(
        new EditorialReviewValidator(),
        new CuratedContentValidator(),
        new QuizContentValidator(),
        new EditorialBatchSelector());
  }
}
