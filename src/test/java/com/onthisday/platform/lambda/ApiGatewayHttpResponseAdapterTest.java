package com.onthisday.platform.lambda;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.onthisday.platform.http.HttpResponse;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ApiGatewayHttpResponseAdapterTest {

  @Test
  void adaptsInternalResponseToApiGatewayResponse() {
    var response =
        new ApiGatewayHttpResponseAdapter()
            .adapt(new HttpResponse(201, Map.of("content-type", "application/json"), "{\"ok\":true}"));

    assertEquals(201, response.getStatusCode());
    assertEquals("application/json", response.getHeaders().get("content-type"));
    assertEquals("{\"ok\":true}", response.getBody());
  }
}
