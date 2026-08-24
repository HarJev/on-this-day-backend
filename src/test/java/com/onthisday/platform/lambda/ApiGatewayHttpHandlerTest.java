package com.onthisday.platform.lambda;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.onthisday.platform.http.HttpMethod;
import com.onthisday.platform.http.HttpResponse;
import com.onthisday.platform.http.HttpRoute;
import com.onthisday.platform.http.HttpRouter;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ApiGatewayHttpHandlerTest {

  @Test
  void routesApiGatewayRequestThroughInternalRouter() {
    var routes =
        Map.of(
            new HttpRouter.RouteKey(HttpMethod.GET, "/test"),
            (HttpRoute) request -> HttpResponse.json(200, "{\"path\":\"" + request.path() + "\"}"));
    var handler = new ApiGatewayHttpHandler(new HttpRouter(routes));

    var response = handler.handleRequest(event("GET", "/test"), null);

    assertEquals(200, response.getStatusCode());
    assertEquals("application/json", response.getHeaders().get("content-type"));
    assertEquals("{\"path\":\"/test\"}", response.getBody());
  }

  @Test
  void defaultHandlerServesHealthRoute() {
    var response = new ApiGatewayHttpHandler().handleRequest(event("GET", "/v1/health"), null);

    assertEquals(200, response.getStatusCode());
    assertEquals("application/json", response.getHeaders().get("content-type"));
    assertEquals("{\"status\":\"ok\"}", response.getBody());
  }

  @Test
  void returnsRouteNotFoundForMissingPath() {
    var response = new ApiGatewayHttpHandler().handleRequest(event("GET", "/missing"), null);

    assertEquals(404, response.getStatusCode());
    assertEquals("{\"code\":\"route_not_found\",\"message\":\"Route not found.\"}", response.getBody());
  }

  @Test
  void catchesUnexpectedFailuresAsInternalErrors() {
    var routes =
        Map.of(
            new HttpRouter.RouteKey(HttpMethod.GET, "/test"),
            (HttpRoute)
                request -> {
                  throw new IllegalStateException("boom");
                });
    var handler = new ApiGatewayHttpHandler(new HttpRouter(routes));

    var response = handler.handleRequest(event("GET", "/test"), null);

    assertEquals(500, response.getStatusCode());
    assertEquals(
        "{\"code\":\"internal_error\",\"message\":\"Internal server error.\"}", response.getBody());
  }

  private APIGatewayV2HTTPEvent event(String method, String path) {
    var event = new APIGatewayV2HTTPEvent();
    var requestContext = new APIGatewayV2HTTPEvent.RequestContext();
    var http = new APIGatewayV2HTTPEvent.RequestContext.Http();
    http.setMethod(method);
    http.setPath(path);
    requestContext.setHttp(http);
    event.setRequestContext(requestContext);
    return event;
  }
}
