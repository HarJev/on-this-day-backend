package com.onthisday.ingestion;

import java.util.Objects;

public record CuratedContent(
    CuratedEventsFile eventsFile,
    CuratedDailyEventsFile dailyEventsFile) {

  public CuratedContent {
    eventsFile = Objects.requireNonNull(eventsFile, "eventsFile must not be null");
    dailyEventsFile = Objects.requireNonNull(dailyEventsFile, "dailyEventsFile must not be null");
  }
}
