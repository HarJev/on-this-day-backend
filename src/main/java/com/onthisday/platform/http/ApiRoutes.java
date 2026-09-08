package com.onthisday.platform.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onthisday.content.HistoricalEventRepository;
import com.onthisday.content.TodayContentRepository;
import com.onthisday.content.TodayContentService;
import com.onthisday.notifications.DeviceRegistrationRepository;
import com.onthisday.notifications.DeviceRegistrationService;
import com.onthisday.platform.content.EventDetailHandler;
import com.onthisday.platform.content.TodayContentHandler;
import com.onthisday.platform.health.HealthHandler;
import com.onthisday.platform.notifications.DeleteDeviceHandler;
import com.onthisday.platform.notifications.RegisterDeviceHandler;
import com.onthisday.platform.quiz.DailyQuizHandler;
import com.onthisday.platform.quiz.QuizApiServices;
import com.onthisday.platform.quiz.QuizCatalogHandler;
import com.onthisday.platform.quiz.QuickPlayQuizHandler;
import java.time.Clock;
import java.util.HashMap;

public final class ApiRoutes {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private ApiRoutes() {}

  public static HttpRouter create(
      TodayContentRepository todayContentRepository,
      HistoricalEventRepository historicalEventRepository,
      DeviceRegistrationRepository deviceRegistrationRepository,
      Clock clock) {
    return create(
        todayContentRepository,
        historicalEventRepository,
        deviceRegistrationRepository,
        clock,
        null);
  }

  public static HttpRouter create(
      TodayContentRepository todayContentRepository,
      HistoricalEventRepository historicalEventRepository,
      DeviceRegistrationRepository deviceRegistrationRepository,
      Clock clock,
      QuizApiServices quizApiServices) {
    var routes = new HashMap<HttpRouter.RouteKey, HttpRoute>();
    var deviceRegistrationService = new DeviceRegistrationService(deviceRegistrationRepository);
    routes.put(new HttpRouter.RouteKey(HttpMethod.GET, "/v1/health"), new HealthHandler(OBJECT_MAPPER));
    routes.put(
        new HttpRouter.RouteKey(HttpMethod.GET, "/v1/days/today"),
        new TodayContentHandler(new TodayContentService(todayContentRepository, clock), OBJECT_MAPPER));
    routes.put(
        new HttpRouter.RouteKey(HttpMethod.GET, "/v1/events/{eventId}"),
        new EventDetailHandler(historicalEventRepository, OBJECT_MAPPER));
    routes.put(
        new HttpRouter.RouteKey(HttpMethod.POST, "/v1/devices"),
        new RegisterDeviceHandler(deviceRegistrationService, OBJECT_MAPPER));
    routes.put(
        new HttpRouter.RouteKey(HttpMethod.DELETE, "/v1/devices/{token}"),
        new DeleteDeviceHandler(deviceRegistrationService, OBJECT_MAPPER));
    if (quizApiServices != null) {
      routes.put(
          new HttpRouter.RouteKey(HttpMethod.GET, "/v1/quizzes/catalog"),
          new QuizCatalogHandler(quizApiServices.catalogService(), OBJECT_MAPPER));
      routes.put(
          new HttpRouter.RouteKey(HttpMethod.POST, "/v1/quizzes/quick-play"),
          new QuickPlayQuizHandler(quizApiServices.quickPlayService(), OBJECT_MAPPER));
      routes.put(
          new HttpRouter.RouteKey(HttpMethod.GET, "/v1/quizzes/daily"),
          new DailyQuizHandler(quizApiServices.dailyService(), OBJECT_MAPPER));
    }
    return new HttpRouter(routes);
  }

  public static HttpRouter healthOnly() {
    return new HttpRouter(
        java.util.Map.of(
            new HttpRouter.RouteKey(HttpMethod.GET, "/v1/health"), new HealthHandler(OBJECT_MAPPER)));
  }
}
