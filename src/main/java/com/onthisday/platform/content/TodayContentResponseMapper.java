package com.onthisday.platform.content;

import com.onthisday.content.TodayContent;

public final class TodayContentResponseMapper {

  public TodayContentResponse toResponse(TodayContent content) {
    var date = content.date();
    var featuredEvent = content.featuredEvent();
    var additionalEvents =
        content.additionalEvents().stream()
            .map(
                event ->
                    new ApiEventSummaryResponse(
                        event.id(), event.title(), event.year(), event.historicalDate(), event.dateNote()))
            .toList();

    return new TodayContentResponse(
        new ApiContentDateResponse(date.month(), date.day(), date.displayDate()),
        new ApiFeaturedEventResponse(
            featuredEvent.id(),
            featuredEvent.title(),
            featuredEvent.year(),
            featuredEvent.historicalDate(),
            featuredEvent.summary(),
            featuredEvent.notificationTitle(),
            featuredEvent.notificationBody(),
            ApiEventImageResponseMapper.toResponse(featuredEvent.image()),
            featuredEvent.dateNote()),
        additionalEvents);
  }
}
