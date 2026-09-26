package com.onthisday.ingestion.editorial;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class EditorialReviewValidatorTest {

  @Test
  void acceptsAnApprovedSevenDayAndTwentyQuestionBatch() {
    var result = new EditorialReviewValidator().validate(EditorialWorkflowFixtures.approvedBatch());

    assertTrue(result.valid());
  }

  @Test
  void rejectsAnApprovedEntryWhoseSourceWasNotDated() {
    var batch = EditorialWorkflowFixtures.approvedBatch();
    var first = batch.reviewLedger().entries().getFirst();
    var invalidEntry =
        new EditorialReviewEntry(
            first.candidateId(),
            first.canonicalId(),
            first.reviewStatus(),
            first.reviewer(),
            first.reviewedOn(),
            java.util.List.of(
                new EditorialSourceCheck(
                    first.sourceChecks().getFirst().url(),
                    "verified_supporting",
                    "",
                    first.sourceChecks().getFirst().note())),
            first.imageRightsStatus(),
            first.regions(),
            first.eras(),
            first.calendarDays(),
            first.notes());
    var entries = new java.util.ArrayList<>(batch.reviewLedger().entries());
    entries.set(0, invalidEntry);
    var invalidBatch =
        new EditorialBatch(
            batch.manifest(), batch.candidates(), new EditorialReviewLedger(1, batch.manifest().batchId(), entries));

    var result = new EditorialReviewValidator().validate(invalidBatch);

    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(error -> error.path().endsWith(".checkedOn")));
  }

  @Test
  void strictReaderRejectsUnknownFields(@TempDir Path tempDirectory) throws Exception {
    var manifest = tempDirectory.resolve("batch.json");
    Files.writeString(
        manifest,
        """
        {"schemaVersion":1,"batchId":"fixture-batch","candidateFile":"candidates.json","reviewLedgerFile":"review-ledger.json","unexpected":true}
        """);
    Files.copy(Path.of("src/test/resources/editorial/valid/candidates.json"), tempDirectory.resolve("candidates.json"));
    Files.copy(Path.of("src/test/resources/editorial/valid/review-ledger.json"), tempDirectory.resolve("review-ledger.json"));

    assertThrows(
        EditorialContentException.class,
        () -> new EditorialContentReader(new ObjectMapper()).readBatch(manifest));
  }
}
