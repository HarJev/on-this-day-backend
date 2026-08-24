package com.onthisday.platform.health;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onthisday.platform.http.HttpRequest;
import com.onthisday.platform.http.HttpResponse;
import com.onthisday.platform.http.HttpRoute;

public final class HealthHandler implements HttpRoute {

  private final ObjectMapper objectMapper;

  public HealthHandler(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public HttpResponse handle(HttpRequest request) {
    try {
      return HttpResponse.json(200, objectMapper.writeValueAsString(new HealthResponse("ok")));
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Failed to serialize health response.", exception);
    }
  }
}
