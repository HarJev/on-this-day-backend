package com.onthisday.ingestion.editorial;

import java.util.List;

/** Human review evidence for a batch. It is intentionally separate from public content JSON. */
public record EditorialReviewLedger(int schemaVersion, String batchId, List<EditorialReviewEntry> entries) {}
