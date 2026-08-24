package com.onthisday.ingestion;

import java.util.List;

public record CuratedEventJson(
    String id,
    String title,
    String year,
    String historicalDate,
    String dateNote,
    String summary,
    String description,
    String notificationTitle,
    String notificationBody,
    List<CuratedSourceJson> sources,
    List<CuratedImageJson> images) {}
