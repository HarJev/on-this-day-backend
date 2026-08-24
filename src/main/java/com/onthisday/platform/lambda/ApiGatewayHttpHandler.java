package com.onthisday.platform.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onthisday.platform.health.HealthHandler;
import com.onthisday.platform.http.ErrorResponseWriter;
import com.onthisday.platform.http.HttpMethod;
import com.onthisday.platform.http.HttpRouter;
import java.util.Map;

public final class ApiGatewayHttpHandler
    implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

  private final ApiGatewayHttpRequestAdapter requestAdapter;
  private final ApiGatewayHttpResponseAdapter responseAdapter;
  private final ErrorResponseWriter errorResponseWriter;
  private final HttpRouter router;

  public ApiGatewayHttpHandler() {
    this(defaultRouter());
  }

  ApiGatewayHttpHandler(HttpRouter router) {
    this.requestAdapter = new ApiGatewayHttpRequestAdapter();
    this.responseAdapter = new ApiGatewayHttpResponseAdapter();
    this.errorResponseWriter = new ErrorResponseWriter(new ObjectMapper());
    this.router = router;
  }

  @Override
  public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
    try {
      var request = requestAdapter.adapt(event);
      var response = router.route(request);
      return responseAdapter.adapt(response);
    } catch (Exception exception) {
      return responseAdapter.adapt(
          errorResponseWriter.json(500, "internal_error", "Internal server error."));
    }
  }

  private static HttpRouter defaultRouter() {
    var objectMapper = new ObjectMapper();
    return new HttpRouter(
        Map.of(
            new HttpRouter.RouteKey(HttpMethod.GET, "/v1/health"),
            new HealthHandler(objectMapper)));
  }
}
