package com.onthisday.platform.quiz;

import java.util.List;

public record ApiQuizCatalogResponse(
    List<Integer> questionCounts,
    ApiQuickPlayTimerDefaultsResponse quickPlayTimerDefaultsSeconds,
    ApiQuizSelectionAvailabilityResponse mixed,
    List<ApiQuizCatalogCollectionResponse> collections) {}
