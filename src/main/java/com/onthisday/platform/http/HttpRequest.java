package com.onthisday.platform.http;

import java.util.Map;
import java.util.Optional;

public record HttpRequest(
    HttpMethod method,
    String path,
    Map<String, String> queryParameters,
    Map<String, String> pathParameters,
    Map<String, String> headers,
    String body) {

  public HttpRequest {
    method = method == null ? HttpMethod.UNKNOWN : method;
    path = path == null ? "" : path;
    queryParameters = Map.copyOf(queryParameters == null ? Map.of() : queryParameters);
    pathParameters = Map.copyOf(pathParameters == null ? Map.of() : pathParameters);
    headers = Map.copyOf(headers == null ? Map.of() : headers);
    body = body == null ? "" : body;
  }

  public Optional<String> queryParameter(String name) {
    return Optional.ofNullable(queryParameters.get(name));
  }

  public Optional<String> pathParameter(String name) {
    return Optional.ofNullable(pathParameters.get(name));
  }

  public Optional<String> header(String name) {
    return Optional.ofNullable(headers.get(name));
  }
}
