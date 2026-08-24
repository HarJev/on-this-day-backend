package com.onthisday.platform.lambda;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.onthisday.platform.http.HttpResponse;

public final class ApiGatewayHttpResponseAdapter {

  public APIGatewayV2HTTPResponse adapt(HttpResponse response) {
    return APIGatewayV2HTTPResponse.builder()
        .withStatusCode(response.statusCode())
        .withHeaders(response.headers())
        .withBody(response.body())
        .build();
  }
}
