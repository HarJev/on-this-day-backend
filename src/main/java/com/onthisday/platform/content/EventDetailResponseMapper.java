package com.onthisday.platform.content;

import com.onthisday.content.HistoricalEvent;

public final class EventDetailResponseMapper {

  public EventDetailResponse toResponse(HistoricalEvent event) {
    return new EventDetailResponse(
        event.id(),
        event.title(),
        event.year(),
        event.historicalDate(),
        event.summary(),
        event.description(),
        event.sources().stream().map(source -> new ApiEventSourceResponse(source.name(), source.url())).toList(),
        ApiEventImageResponseMapper.toResponse(event.primaryImage()),
        event.images().stream().map(ApiEventImageResponseMapper::toResponse).toList(),
        event.dateNote());
  }
}
