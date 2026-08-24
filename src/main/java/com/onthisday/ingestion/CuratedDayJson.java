package com.onthisday.ingestion;

import java.util.List;

public record CuratedDayJson(
    Integer month,
    Integer day,
    String featuredEventId,
    List<String> additionalEventIds) {}
