package com.onthisday.platform.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class HttpRequestTest {

  @Test
  void defensivelyCopiesRequestMaps() {
    var query = new HashMap<>(Map.of("timezone", "America/Jamaica"));
    var request =
        new HttpRequest(HttpMethod.GET, "/v1/days/today", query, Map.of(), Map.of(), "");

    query.put("timezone", "Etc/UTC");

    assertEquals("America/Jamaica", request.queryParameter("timezone").orElseThrow());
    assertThrows(
        UnsupportedOperationException.class,
        () -> request.queryParameters().put("timezone", "Etc/UTC"));
  }

  @Test
  void normalizesNullValues() {
    var request = new HttpRequest(null, null, null, null, null, null);

    assertEquals(HttpMethod.UNKNOWN, request.method());
    assertEquals("", request.path());
    assertTrue(request.queryParameters().isEmpty());
    assertTrue(request.pathParameters().isEmpty());
    assertTrue(request.headers().isEmpty());
    assertEquals("", request.body());
  }
}
