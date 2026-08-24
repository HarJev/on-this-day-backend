package com.onthisday.ingestion;

import java.nio.file.Path;

public final class ContentFileLocations {

  public static final Path EVENTS = Path.of("content", "events.json");
  public static final Path DAILY_EVENTS = Path.of("content", "daily-events.json");

  private ContentFileLocations() {}
}
