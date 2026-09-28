package com.onthisday.content;

import java.util.Objects;

public record RecentDay(int daysAgo, ContentDate date, EventSummary featuredEvent) {

  public RecentDay {
    if (daysAgo < 1) {
      throw new IllegalArgumentException("daysAgo must be at least 1");
    }
    date = Objects.requireNonNull(date, "date must not be null");
    featuredEvent = Objects.requireNonNull(featuredEvent, "featuredEvent must not be null");
  }
}
