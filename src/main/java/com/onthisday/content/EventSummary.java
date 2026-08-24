package com.onthisday.content;

import static com.onthisday.content.ContentChecks.requireNonBlank;

public record EventSummary(
    String id,
    String title,
    String year,
    String historicalDate,
    String dateNote) {

  public EventSummary {
    id = requireNonBlank(id, "id");
    title = requireNonBlank(title, "title");
    year = requireNonBlank(year, "year");
    historicalDate = requireNonBlank(historicalDate, "historicalDate");
  }
}
