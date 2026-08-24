package com.onthisday.platform.http;

import java.util.Map;

public record HttpResponse(int statusCode, Map<String, String> headers, String body) {

  public static final String JSON_CONTENT_TYPE = "application/json";

  public HttpResponse {
    headers = Map.copyOf(headers == null ? Map.of() : headers);
    body = body == null ? "" : body;
  }

  public static HttpResponse json(int statusCode, String body) {
    return new HttpResponse(statusCode, Map.of("content-type", JSON_CONTENT_TYPE), body);
  }
}
