package com.onthisday.ingestion.editorial;

import java.util.List;

public record EditorialCandidate(
    String candidateId, String kind, String proposedCanonicalId, String workingClaim, List<String> sourceUrls) {}
