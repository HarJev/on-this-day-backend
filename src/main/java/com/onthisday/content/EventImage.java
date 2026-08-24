package com.onthisday.content;

import static com.onthisday.content.ContentChecks.requireNonBlank;

import java.net.URI;
import java.util.Objects;

public record EventImage(
    URI url,
    String altText,
    String source,
    URI sourceUrl,
    String attribution,
    String creator,
    String license,
    URI licenseUrl) {

  public EventImage {
    url = Objects.requireNonNull(url, "url must not be null");
    altText = requireNonBlank(altText, "altText");
  }
}
