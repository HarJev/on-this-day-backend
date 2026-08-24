package com.onthisday.platform.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

public final class HttpRouter {

  private final Map<RouteKey, HttpRoute> routes;
  private final ErrorResponseWriter errorResponseWriter;

  public HttpRouter(Map<RouteKey, HttpRoute> routes) {
    this(routes, new ErrorResponseWriter(new ObjectMapper()));
  }

  public HttpRouter(Map<RouteKey, HttpRoute> routes, ErrorResponseWriter errorResponseWriter) {
    this.routes = Map.copyOf(routes == null ? Map.of() : routes);
    this.errorResponseWriter = errorResponseWriter;
  }

  public HttpResponse route(HttpRequest request) {
    var route = routes.get(new RouteKey(request.method(), request.path()));
    if (route == null) {
      return errorResponseWriter.json(404, "route_not_found", "Route not found.");
    }

    return route.handle(request);
  }

  public record RouteKey(HttpMethod method, String path) {
    public RouteKey {
      method = method == null ? HttpMethod.UNKNOWN : method;
      path = path == null ? "" : path;
    }
  }
}
