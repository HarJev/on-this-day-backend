package com.onthisday.content;

import static com.onthisday.content.ContentChecks.copyNonEmpty;
import static com.onthisday.content.ContentChecks.requireNonBlank;

import java.util.List;

public record HistoricalEvent(
    String id,
    String title,
    String year,
    String historicalDate,
    String summary,
    String description,
    List<EventSource> sources,
    EventImage primaryImage,
    List<EventImage> images,
    String dateNote) {

  public HistoricalEvent {
    id = requireNonBlank(id, "id");
    title = requireNonBlank(title, "title");
    year = requireNonBlank(year, "year");
    historicalDate = requireNonBlank(historicalDate, "historicalDate");
    summary = requireNonBlank(summary, "summary");
    description = requireNonBlank(description, "description");
    sources = copyNonEmpty(sources, "sources");
    images = List.copyOf(images);
  }
}
