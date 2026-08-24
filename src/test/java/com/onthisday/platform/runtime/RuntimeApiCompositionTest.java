package com.onthisday.platform.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.onthisday.platform.http.HttpMethod;
import com.onthisday.platform.http.HttpRequest;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RuntimeApiCompositionTest {

  @Test
  void buildsRuntimeRouterWithHealthRouteWithoutOpeningDatabaseConnection() {
    var router =
        RuntimeApiComposition.createRouter(
            new DatabaseConfig(
                "jdbc:postgresql://localhost:5432/on_this_day", "on_this_day", "secret"),
            Clock.fixed(Instant.parse("2026-08-22T12:00:00Z"), ZoneOffset.UTC));

    var response =
        router.route(new HttpRequest(HttpMethod.GET, "/v1/health", Map.of(), Map.of(), Map.of(), ""));

    assertEquals(200, response.statusCode());
    assertEquals("{\"status\":\"ok\"}", response.body());
  }
}
