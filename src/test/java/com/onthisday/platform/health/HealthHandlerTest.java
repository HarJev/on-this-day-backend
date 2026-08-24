package com.onthisday.platform.health;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onthisday.platform.http.HttpMethod;
import com.onthisday.platform.http.HttpRequest;
import java.util.Map;
import org.junit.jupiter.api.Test;

class HealthHandlerTest {

  @Test
  void returnsOkHealthResponse() {
    var handler = new HealthHandler(new ObjectMapper());

    var response = handler.handle(new HttpRequest(HttpMethod.GET, "/v1/health", Map.of(), Map.of(), Map.of(), ""));

    assertEquals(200, response.statusCode());
    assertEquals("application/json", response.headers().get("content-type"));
    assertEquals("{\"status\":\"ok\"}", response.body());
  }
}
