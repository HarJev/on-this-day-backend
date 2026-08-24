package com.onthisday.platform.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class HttpRouterTest {

  @Test
  void routesExactMethodAndPath() {
    var router =
        new HttpRouter(
            Map.of(
                new HttpRouter.RouteKey(HttpMethod.GET, "/test"),
                request -> HttpResponse.json(200, "{\"ok\":true}")));

    var response = router.route(new HttpRequest(HttpMethod.GET, "/test", Map.of(), Map.of(), Map.of(), ""));

    assertEquals(200, response.statusCode());
    assertEquals("{\"ok\":true}", response.body());
  }

  @Test
  void doesNotMatchDifferentMethodOrPath() {
    var router =
        new HttpRouter(
            Map.of(
                new HttpRouter.RouteKey(HttpMethod.GET, "/test"),
                request -> HttpResponse.json(200, "{\"ok\":true}")));

    var response =
        router.route(new HttpRequest(HttpMethod.POST, "/test", Map.of(), Map.of(), Map.of(), ""));

    assertEquals(404, response.statusCode());
    assertEquals("{\"code\":\"route_not_found\",\"message\":\"Route not found.\"}", response.body());
  }

  @Test
  void routesTemplateAndCapturesSingleSegmentPathParameter() {
    var router =
        new HttpRouter(
            Map.of(
                new HttpRouter.RouteKey(HttpMethod.GET, "/v1/events/{eventId}"),
                request -> HttpResponse.json(200, "{\"eventId\":\"" + request.pathParameter("eventId").orElse("") + "\"}")));

    var response =
        router.route(
            new HttpRequest(
                HttpMethod.GET,
                "/v1/events/battle-of-bosworth-field-1485",
                Map.of(),
                Map.of(),
                Map.of(),
                ""));

    assertEquals(200, response.statusCode());
    assertEquals("{\"eventId\":\"battle-of-bosworth-field-1485\"}", response.body());
  }

  @Test
  void exactRouteWinsOverTemplateRoute() {
    var routes = new LinkedHashMap<HttpRouter.RouteKey, HttpRoute>();
    routes.put(
        new HttpRouter.RouteKey(HttpMethod.GET, "/v1/events/{eventId}"),
        request -> HttpResponse.json(200, "{\"route\":\"template\"}"));
    routes.put(
        new HttpRouter.RouteKey(HttpMethod.GET, "/v1/events/special"),
        request -> HttpResponse.json(200, "{\"route\":\"exact\"}"));
    var router = new HttpRouter(routes);

    var response =
        router.route(new HttpRequest(HttpMethod.GET, "/v1/events/special", Map.of(), Map.of(), Map.of(), ""));

    assertEquals(200, response.statusCode());
    assertEquals("{\"route\":\"exact\"}", response.body());
  }

  @Test
  void templateCapturesWinOverExistingPathParameterByName() {
    var router =
        new HttpRouter(
            Map.of(
                new HttpRouter.RouteKey(HttpMethod.GET, "/v1/events/{eventId}"),
                request -> HttpResponse.json(200, "{\"eventId\":\"" + request.pathParameter("eventId").orElse("") + "\"}")));

    var response =
        router.route(
            new HttpRequest(
                HttpMethod.GET,
                "/v1/events/from-template",
                Map.of(),
                Map.of("eventId", "from-adapter", "other", "kept"),
                Map.of(),
                ""));

    assertEquals(200, response.statusCode());
    assertEquals("{\"eventId\":\"from-template\"}", response.body());
  }

  @Test
  void templateDoesNotMatchDifferentMethodMissingExtraOrDifferentStaticSegments() {
    var router =
        new HttpRouter(
            Map.of(
                new HttpRouter.RouteKey(HttpMethod.GET, "/v1/events/{eventId}"),
                request -> HttpResponse.json(200, "{\"ok\":true}")));

    assertEquals(
        404,
        router.route(new HttpRequest(HttpMethod.POST, "/v1/events/a", Map.of(), Map.of(), Map.of(), ""))
            .statusCode());
    assertEquals(
        404,
        router.route(new HttpRequest(HttpMethod.GET, "/v1/events", Map.of(), Map.of(), Map.of(), ""))
            .statusCode());
    assertEquals(
        404,
        router.route(new HttpRequest(HttpMethod.GET, "/v1/events/a/b", Map.of(), Map.of(), Map.of(), ""))
            .statusCode());
    assertEquals(
        404,
        router.route(new HttpRequest(HttpMethod.GET, "/v1/other/a", Map.of(), Map.of(), Map.of(), ""))
            .statusCode());
  }

  @Test
  void rejectsMalformedTemplatesAtConstruction() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new HttpRouter(
                Map.of(new HttpRouter.RouteKey(HttpMethod.GET, "/v1/events/{}"), request -> HttpResponse.json(200, ""))));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new HttpRouter(
                Map.of(
                    new HttpRouter.RouteKey(HttpMethod.GET, "/v1/events/{eventId"),
                    request -> HttpResponse.json(200, ""))));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new HttpRouter(
                Map.of(
                    new HttpRouter.RouteKey(HttpMethod.GET, "/v1/events/event-{eventId}"),
                    request -> HttpResponse.json(200, ""))));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new HttpRouter(
                Map.of(
                    new HttpRouter.RouteKey(HttpMethod.GET, "/v1/events/{id}/sources/{id}"),
                    request -> HttpResponse.json(200, ""))));
  }

  @Test
  void defensivelyCopiesRouteMap() {
    var routes = new java.util.HashMap<HttpRouter.RouteKey, HttpRoute>();
    var router = new HttpRouter(routes);

    routes.put(
        new HttpRouter.RouteKey(HttpMethod.GET, "/test"),
        request -> HttpResponse.json(200, "{\"ok\":true}"));

    var response = router.route(new HttpRequest(HttpMethod.GET, "/test", Map.of(), Map.of(), Map.of(), ""));

    assertEquals(404, response.statusCode());
    assertThrows(
        UnsupportedOperationException.class,
        () -> router.route(new HttpRequest(HttpMethod.GET, "/test", Map.of(), Map.of(), Map.of(), "")).headers()
            .put("x-test", "value"));
  }
}
