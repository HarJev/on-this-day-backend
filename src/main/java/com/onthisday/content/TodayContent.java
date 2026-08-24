package com.onthisday.content;

import java.util.List;
import java.util.Objects;

public record TodayContent(
    ContentDate date,
    FeaturedEvent featuredEvent,
    List<EventSummary> additionalEvents) {

  public TodayContent {
    date = Objects.requireNonNull(date, "date must not be null");
    featuredEvent = Objects.requireNonNull(featuredEvent, "featuredEvent must not be null");
    additionalEvents = List.copyOf(additionalEvents);
  }
}
