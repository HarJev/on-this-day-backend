package com.onthisday.platform.content;

public record ApiEventSummaryResponse(
    String id,
    String title,
    String year,
    String historicalDate,
    String dateNote) {}
