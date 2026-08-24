package com.onthisday.content;

import static com.onthisday.content.ContentChecks.requireNonBlank;

import java.net.URI;
import java.util.Objects;

public record EventSource(String name, URI url) {

  public EventSource {
    name = requireNonBlank(name, "name");
    url = Objects.requireNonNull(url, "url must not be null");
  }
}
