package com.onthisday.platform.lambda;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.onthisday.platform.http.HttpMethod;
import com.onthisday.platform.http.HttpRequest;
import java.util.Map;

public final class ApiGatewayHttpRequestAdapter {

  public HttpRequest adapt(APIGatewayV2HTTPEvent event) {
    if (event == null) {
      return new HttpRequest(HttpMethod.UNKNOWN, "", Map.of(), Map.of(), Map.of(), "");
    }

    var http = event.getRequestContext() == null ? null : event.getRequestContext().getHttp();

    return new HttpRequest(
        HttpMethod.from(http == null ? null : http.getMethod()),
        http == null ? "" : http.getPath(),
        event.getQueryStringParameters(),
        event.getPathParameters(),
        event.getHeaders(),
        event.getBody());
  }
}
