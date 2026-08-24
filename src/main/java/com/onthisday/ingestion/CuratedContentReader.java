package com.onthisday.ingestion;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public class CuratedContentReader {

  private final ObjectMapper objectMapper;

  public CuratedContentReader(ObjectMapper objectMapper) {
    this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
  }

  public CuratedContent readDefault() {
    return read(ContentFileLocations.EVENTS, ContentFileLocations.DAILY_EVENTS);
  }

  public CuratedContent read(Path contentDir) {
    Objects.requireNonNull(contentDir, "contentDir must not be null");

    var eventsPath = contentDir.resolve("events.json").normalize();
    var dailyEventsPath = contentDir.resolve("daily-events.json").normalize();
    return read(eventsPath, dailyEventsPath);
  }

  public CuratedContent read(Path eventsPath, Path dailyEventsPath) {
    Objects.requireNonNull(eventsPath, "eventsPath must not be null");
    Objects.requireNonNull(dailyEventsPath, "dailyEventsPath must not be null");

    try {
      return new CuratedContent(
          objectMapper.readValue(eventsPath.toFile(), CuratedEventsFile.class),
          objectMapper.readValue(dailyEventsPath.toFile(), CuratedDailyEventsFile.class));
    } catch (IOException exception) {
      throw new ContentImportException("Could not read curated content JSON.", exception);
    }
  }
}
