package com.onthisday.ingestion.editorial;

import java.util.List;

public record EditorialReviewEntry(
    String candidateId,
    String canonicalId,
    String reviewStatus,
    String reviewer,
    String reviewedOn,
    List<EditorialSourceCheck> sourceChecks,
    String imageRightsStatus,
    List<String> regions,
    List<String> eras,
    List<String> calendarDays,
    String notes) {}
