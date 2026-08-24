package com.onthisday.platform.http;

@FunctionalInterface
public interface HttpRoute {
  HttpResponse handle(HttpRequest request);
}
