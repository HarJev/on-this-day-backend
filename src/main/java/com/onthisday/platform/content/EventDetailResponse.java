package com.onthisday.platform.content;

import java.util.List;

public record EventDetailResponse(
    String id,
    String title,
    String year,
    String historicalDate,
    String summary,
    String description,
    List<ApiEventSourceResponse> sources,
    ApiEventImageResponse primaryImage,
    List<ApiEventImageResponse> images,
    String dateNote) {

  public EventDetailResponse {
    sources = List.copyOf(sources);
    images = List.copyOf(images);
  }
}
