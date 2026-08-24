package com.onthisday.platform.content;

import com.onthisday.content.EventImage;

final class ApiEventImageResponseMapper {

  private ApiEventImageResponseMapper() {}

  static ApiEventImageResponse toResponse(EventImage image) {
    if (image == null) {
      return null;
    }

    return new ApiEventImageResponse(
        image.url(),
        image.altText(),
        image.source(),
        image.sourceUrl(),
        image.attribution(),
        image.creator(),
        image.license(),
        image.licenseUrl());
  }
}
