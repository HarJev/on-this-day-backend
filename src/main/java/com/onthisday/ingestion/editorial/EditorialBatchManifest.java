package com.onthisday.ingestion.editorial;

import java.util.List;

/** Selects the already-reviewed canonical content allowed into one staging import. */
public record EditorialBatchManifest(
    int schemaVersion,
    String batchId,
    String candidateFile,
    String reviewLedgerFile,
    List<String> eventIds,
    List<String> monthDays,
    List<String> quizPackFilenames) {}
