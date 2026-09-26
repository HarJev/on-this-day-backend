package com.onthisday.ingestion.editorial;

public record EditorialBatch(
    EditorialBatchManifest manifest,
    EditorialCandidatesFile candidates,
    EditorialReviewLedger reviewLedger) {}
