package com.onthisday.platform.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onthisday.content.HistoricalEventRepository;
import com.onthisday.content.TodayContentRepository;
import com.onthisday.platform.http.ErrorResponseWriter;
import com.onthisday.platform.http.ApiRoutes;
import com.onthisday.platform.http.HttpRouter;
import com.onthisday.platform.runtime.RuntimeApiComposition;
import java.time.Clock;

public final class ApiGatewayHttpHandler
    implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

  private final ApiGatewayHttpRequestAdapter requestAdapter;
  private final ApiGatewayHttpResponseAdapter responseAdapter;
  private final ErrorResponseWriter errorResponseWriter;
  private final HttpRouter router;

  public ApiGatewayHttpHandler() {
    this(RuntimeApiComposition.createRouterFromEnvironment());
  }

  public ApiGatewayHttpHandler(
      TodayContentRepository todayContentRepository,
      HistoricalEventRepository historicalEventRepository,
      Clock clock) {
    this(ApiRoutes.create(todayContentRepository, historicalEventRepository, clock));
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

}
