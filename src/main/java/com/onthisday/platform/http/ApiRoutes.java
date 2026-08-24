package com.onthisday.platform.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onthisday.content.HistoricalEventRepository;
import com.onthisday.content.TodayContentRepository;
import com.onthisday.content.TodayContentService;
import com.onthisday.platform.content.EventDetailHandler;
import com.onthisday.platform.content.TodayContentHandler;
import com.onthisday.platform.health.HealthHandler;
import java.time.Clock;
import java.util.HashMap;

public final class ApiRoutes {

  private ApiRoutes() {}

  public static HttpRouter create(
      TodayContentRepository todayContentRepository, HistoricalEventRepository historicalEventRepository, Clock clock) {
    var objectMapper = new ObjectMapper();
    var routes = new HashMap<HttpRouter.RouteKey, HttpRoute>();
    routes.put(new HttpRouter.RouteKey(HttpMethod.GET, "/v1/health"), new HealthHandler(objectMapper));
    routes.put(
        new HttpRouter.RouteKey(HttpMethod.GET, "/v1/days/today"),
        new TodayContentHandler(new TodayContentService(todayContentRepository, clock), objectMapper));
    routes.put(
        new HttpRouter.RouteKey(HttpMethod.GET, "/v1/events/{eventId}"),
        new EventDetailHandler(historicalEventRepository, objectMapper));
    return new HttpRouter(routes);
  }

  public static HttpRouter healthOnly() {
    var objectMapper = new ObjectMapper();
    return new HttpRouter(
        java.util.Map.of(
            new HttpRouter.RouteKey(HttpMethod.GET, "/v1/health"), new HealthHandler(objectMapper)));
  }
}
