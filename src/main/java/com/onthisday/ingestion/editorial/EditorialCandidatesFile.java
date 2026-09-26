package com.onthisday.ingestion.editorial;

import java.util.List;

/** Research material; this file is never an input to a canonical importer. */
public record EditorialCandidatesFile(int schemaVersion, String batchId, List<EditorialCandidate> candidates) {}
