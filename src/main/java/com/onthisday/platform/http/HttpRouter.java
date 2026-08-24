package com.onthisday.platform.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class HttpRouter {

  private final Map<RouteKey, HttpRoute> routes;
  private final List<TemplateRoute> templateRoutes;
  private final ErrorResponseWriter errorResponseWriter;

  public HttpRouter(Map<RouteKey, HttpRoute> routes) {
    this(routes, new ErrorResponseWriter(new ObjectMapper()));
  }

  public HttpRouter(Map<RouteKey, HttpRoute> routes, ErrorResponseWriter errorResponseWriter) {
    this.routes = Map.copyOf(routes == null ? Map.of() : routes);
    this.templateRoutes = templateRoutes(routes);
    this.errorResponseWriter = errorResponseWriter;
  }

  public HttpResponse route(HttpRequest request) {
    var route = routes.get(new RouteKey(request.method(), request.path()));
    if (route != null) {
      return route.handle(request);
    }

    for (var templateRoute : templateRoutes) {
      var match = templateRoute.match(request);
      if (match != null) {
        var pathParameters = new HashMap<>(request.pathParameters());
        pathParameters.putAll(match.pathParameters());
        return templateRoute.route()
            .handle(
                new HttpRequest(
                    request.method(),
                    request.path(),
                    request.queryParameters(),
                    pathParameters,
                    request.headers(),
                    request.body()));
      }
    }

    return errorResponseWriter.json(404, "route_not_found", "Route not found.");
  }

  private static List<TemplateRoute> templateRoutes(Map<RouteKey, HttpRoute> routes) {
    var templateRoutes = new ArrayList<TemplateRoute>();
    for (var entry : (routes == null ? Map.<RouteKey, HttpRoute>of() : routes).entrySet()) {
      var routeKey = entry.getKey();
      if (isTemplate(routeKey.path())) {
        templateRoutes.add(new TemplateRoute(routeKey.method(), RouteTemplate.parse(routeKey.path()), entry.getValue()));
      }
    }
    return List.copyOf(templateRoutes);
  }

  private static boolean isTemplate(String path) {
    return path != null && (path.contains("{") || path.contains("}"));
  }

  public record RouteKey(HttpMethod method, String path) {
    public RouteKey {
      method = method == null ? HttpMethod.UNKNOWN : method;
      path = path == null ? "" : path;
    }
  }

  private record TemplateRoute(HttpMethod method, RouteTemplate template, HttpRoute route) {

    private TemplateMatch match(HttpRequest request) {
      if (method != request.method()) {
        return null;
      }

      return template.match(request.path());
    }
  }

  private record RouteTemplate(String path, List<TemplateSegment> segments) {

    private static RouteTemplate parse(String path) {
      var segments = splitPath(path).stream().map(TemplateSegment::parse).toList();
      var names = new java.util.HashSet<String>();
      for (var segment : segments) {
        if (segment.name() != null && !names.add(segment.name())) {
          throw new IllegalArgumentException("Duplicate route template parameter: " + segment.name());
        }
      }
      return new RouteTemplate(path, segments);
    }

    private TemplateMatch match(String candidatePath) {
      var candidateSegments = splitPath(candidatePath);
      if (candidateSegments.size() != segments.size()) {
        return null;
      }

      var pathParameters = new HashMap<String, String>();
      for (var index = 0; index < segments.size(); index++) {
        var segment = segments.get(index);
        var candidateSegment = candidateSegments.get(index);
        if (segment.name() == null) {
          if (!Objects.equals(segment.value(), candidateSegment)) {
            return null;
          }
        } else {
          if (candidateSegment.isBlank()) {
            return null;
          }
          pathParameters.put(segment.name(), candidateSegment);
        }
      }

      return new TemplateMatch(pathParameters);
    }
  }

  private record TemplateSegment(String value, String name) {

    private static TemplateSegment parse(String value) {
      var opens = value.indexOf('{');
      var closes = value.indexOf('}');
      if (opens < 0 && closes < 0) {
        return new TemplateSegment(value, null);
      }
      if (opens != 0 || closes != value.length() - 1 || value.indexOf('{', opens + 1) >= 0) {
        throw new IllegalArgumentException("Malformed route template segment: " + value);
      }

      var name = value.substring(1, value.length() - 1);
      if (name.isBlank()) {
        throw new IllegalArgumentException("Route template parameter name must not be blank.");
      }

      return new TemplateSegment(null, name);
    }
  }

  private record TemplateMatch(Map<String, String> pathParameters) {

    private TemplateMatch {
      pathParameters = Map.copyOf(pathParameters);
    }
  }

  private static List<String> splitPath(String path) {
    var normalized = path == null ? "" : path;
    if (normalized.equals("/")) {
      return List.of();
    }

    var start = normalized.startsWith("/") ? 1 : 0;
    var end = normalized.endsWith("/") ? normalized.length() - 1 : normalized.length();
    if (start >= end) {
      return List.of();
    }
    return List.of(normalized.substring(start, end).split("/"));
  }
}
