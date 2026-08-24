package com.onthisday.platform.lambda;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ApiGatewayHttpRequestAdapterTest {

  @Test
  void preservesApiGatewayRequestData() {
    var event = new APIGatewayV2HTTPEvent();
    var requestContext = new APIGatewayV2HTTPEvent.RequestContext();
    var http = new APIGatewayV2HTTPEvent.RequestContext.Http();
    http.setMethod("GET");
    http.setPath("/v1/days/today");
    requestContext.setHttp(http);
    event.setRequestContext(requestContext);
    event.setQueryStringParameters(Map.of("timezone", "America/Jamaica"));
    event.setPathParameters(Map.of("eventId", "battle-of-bosworth-field-1485"));
    event.setHeaders(Map.of("x-request-id", "request-1"));
    event.setBody("{\"hello\":\"world\"}");

    var request = new ApiGatewayHttpRequestAdapter().adapt(event);

    assertEquals("GET", request.method().name());
    assertEquals("/v1/days/today", request.path());
    assertEquals("America/Jamaica", request.queryParameter("timezone").orElseThrow());
    assertEquals(
        "battle-of-bosworth-field-1485", request.pathParameter("eventId").orElseThrow());
    assertEquals("request-1", request.header("x-request-id").orElseThrow());
    assertEquals("{\"hello\":\"world\"}", request.body());
  }

  @Test
  void toleratesNullEvent() {
    var request = new ApiGatewayHttpRequestAdapter().adapt(null);

    assertEquals("UNKNOWN", request.method().name());
    assertEquals("", request.path());
    assertEquals("", request.body());
  }
}
