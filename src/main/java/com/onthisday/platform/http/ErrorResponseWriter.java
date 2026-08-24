package com.onthisday.platform.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class ErrorResponseWriter {

  private final ObjectMapper objectMapper;

  public ErrorResponseWriter(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public HttpResponse json(int statusCode, String code, String message) {
    try {
      return HttpResponse.json(statusCode, objectMapper.writeValueAsString(new ErrorResponse(code, message)));
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Failed to serialize error response.", exception);
    }
  }
}
