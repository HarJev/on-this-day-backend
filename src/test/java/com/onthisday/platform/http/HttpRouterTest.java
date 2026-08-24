package com.onthisday.platform.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
