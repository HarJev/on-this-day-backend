package com.onthisday.content;

import static com.onthisday.content.ContentChecks.requireNonBlank;

public record FeaturedEvent(
    String id,
    String title,
    String year,
    String historicalDate,
    String summary,
    String notificationTitle,
    String notificationBody,
    EventImage image,
    String dateNote) {

  public FeaturedEvent {
    id = requireNonBlank(id, "id");
    title = requireNonBlank(title, "title");
    year = requireNonBlank(year, "year");
    historicalDate = requireNonBlank(historicalDate, "historicalDate");
    summary = requireNonBlank(summary, "summary");
    notificationTitle = requireNonBlank(notificationTitle, "notificationTitle");
    notificationBody = requireNonBlank(notificationBody, "notificationBody");
  }
}
