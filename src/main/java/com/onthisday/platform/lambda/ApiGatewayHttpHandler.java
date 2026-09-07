package com.onthisday.platform.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onthisday.content.HistoricalEventRepository;
import com.onthisday.content.TodayContentRepository;
import com.onthisday.notifications.DeviceRegistrationRepository;
import com.onthisday.platform.http.ApiRoutes;
import com.onthisday.platform.http.ErrorResponseWriter;
import com.onthisday.platform.http.HttpRouter;
import com.onthisday.platform.runtime.RuntimeApiComposition;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ApiGatewayHttpHandler
    implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

  private static final Logger LOG = LoggerFactory.getLogger(ApiGatewayHttpHandler.class);
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final ApiGatewayHttpRequestAdapter requestAdapter;
  private final ApiGatewayHttpResponseAdapter responseAdapter;
  private final ErrorResponseWriter errorResponseWriter;
  private final HttpRouter router;

  public ApiGatewayHttpHandler() {
    this(createRuntimeRouter());
  }

  public ApiGatewayHttpHandler(
      TodayContentRepository todayContentRepository,
      HistoricalEventRepository historicalEventRepository,
      DeviceRegistrationRepository deviceRegistrationRepository,
      Clock clock) {
    this(ApiRoutes.create(
        todayContentRepository, historicalEventRepository, deviceRegistrationRepository, clock));
  }

  ApiGatewayHttpHandler(HttpRouter router) {
    this.requestAdapter = new ApiGatewayHttpRequestAdapter();
    this.responseAdapter = new ApiGatewayHttpResponseAdapter();
    this.errorResponseWriter = new ErrorResponseWriter(OBJECT_MAPPER);
    this.router = router;
  }

  private static HttpRouter createRuntimeRouter() {
    var startedAt = System.nanoTime();
    LOG.info("lambda_handler_init_start");
    // Safe for warm containers and future SnapStart: this builds lightweight routing,
    // config, and DataSource objects, but does not open a database connection.
    var router = RuntimeApiComposition.createRouterFromEnvironment();
    var durationMs = (System.nanoTime() - startedAt) / 1_000_000;
    LOG.info("lambda_handler_init_end durationMs={}", durationMs);
    return router;
  }

  @Override
  public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
    var startedAt = System.nanoTime();
    var method = method(event);
    var path = path(event);
    var requestId = context == null ? "" : context.getAwsRequestId();
    var statusCode = 500;
    LOG.info("lambda_request_start method={} path={} requestId={}", method, path, requestId);

    try {
      var request = requestAdapter.adapt(event);
      var response = router.route(request);
      statusCode = response.statusCode();
      return responseAdapter.adapt(response);
    } catch (Exception exception) {
      LOG.error(
          "lambda_unexpected_exception method={} path={} requestId={}",
          method,
          path,
          requestId,
          exception);
      return responseAdapter.adapt(
          errorResponseWriter.json(500, "internal_error", "Internal server error."));
    } finally {
      var durationMs = (System.nanoTime() - startedAt) / 1_000_000;
      LOG.info(
          "lambda_request_end method={} path={} requestId={} statusCode={} durationMs={}",
          method,
          path,
          requestId,
          statusCode,
          durationMs);
    }
  }

  private static String method(APIGatewayV2HTTPEvent event) {
    var http = event == null || event.getRequestContext() == null ? null : event.getRequestContext().getHttp();
    return http == null || http.getMethod() == null ? "" : http.getMethod();
  }

  private static String path(APIGatewayV2HTTPEvent event) {
    var http = event == null || event.getRequestContext() == null ? null : event.getRequestContext().getHttp();
    return http == null || http.getPath() == null ? "" : http.getPath();
  }
}
