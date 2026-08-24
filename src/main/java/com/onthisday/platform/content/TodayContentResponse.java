package com.onthisday.platform.content;

import java.util.List;

public record TodayContentResponse(
    ApiContentDateResponse date,
    ApiFeaturedEventResponse featuredEvent,
    List<ApiEventSummaryResponse> additionalEvents) {

  public TodayContentResponse {
    additionalEvents = List.copyOf(additionalEvents);
  }
}
