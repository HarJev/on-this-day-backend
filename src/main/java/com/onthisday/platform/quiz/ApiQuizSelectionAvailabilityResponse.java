package com.onthisday.platform.quiz;

import java.util.List;

public record ApiQuizSelectionAvailabilityResponse(
    int publishedQuestionCount, List<Integer> supportedQuestionCounts) {}
