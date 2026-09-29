package com.onthisday.platform.http;

import com.onthisday.quiz.QuizEventLinkRepository;
import com.onthisday.content.HistoricalEventRepository;
import com.onthisday.content.RecentDaysService;
import com.onthisday.content.TodayContentRepository;
import com.onthisday.content.TodayContentService;
import com.onthisday.notifications.DeviceRegistrationRepository;
import com.onthisday.notifications.DeviceRegistrationService;
import com.onthisday.platform.content.EventDetailHandler;
import com.onthisday.platform.content.RecentDaysHandler;
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
    var objectMapper = JsonMapperFactory.create();
    var deviceRegistrationService = new DeviceRegistrationService(deviceRegistrationRepository);
    routes.put(new HttpRouter.RouteKey(HttpMethod.GET, "/v1/health"), new HealthHandler(objectMapper));
    routes.put(
        new HttpRouter.RouteKey(HttpMethod.GET, "/v1/days/today"),
        new TodayContentHandler(new TodayContentService(todayContentRepository, clock), objectMapper));
    routes.put(
        new HttpRouter.RouteKey(HttpMethod.GET, "/v1/days/recent"),
        new RecentDaysHandler(new RecentDaysService(todayContentRepository, clock), objectMapper));
    routes.put(
        new HttpRouter.RouteKey(HttpMethod.GET, "/v1/events/{eventId}"),
        new EventDetailHandler(
            historicalEventRepository, objectMapper,
            quizApiServices == null ? QuizEventLinkRepository.empty() : quizApiServices.eventLinks()));
    routes.put(
        new HttpRouter.RouteKey(HttpMethod.POST, "/v1/devices"),
        new RegisterDeviceHandler(deviceRegistrationService, objectMapper));
    routes.put(
        new HttpRouter.RouteKey(HttpMethod.DELETE, "/v1/devices/{token}"),
        new DeleteDeviceHandler(deviceRegistrationService, objectMapper));
    if (quizApiServices != null) {
      routes.put(
          new HttpRouter.RouteKey(HttpMethod.GET, "/v1/quizzes/catalog"),
          new QuizCatalogHandler(quizApiServices.catalogService(), objectMapper));
      routes.put(
          new HttpRouter.RouteKey(HttpMethod.POST, "/v1/quizzes/quick-play"),
          new QuickPlayQuizHandler(
              quizApiServices.quickPlayService(), objectMapper, quizApiServices.eventLinks()));
      routes.put(
          new HttpRouter.RouteKey(HttpMethod.GET, "/v1/quizzes/daily"),
          new DailyQuizHandler(
              quizApiServices.dailyService(), objectMapper, quizApiServices.eventLinks()));
    }
    return new HttpRouter(routes);
  }

  public static HttpRouter healthOnly() {
    return new HttpRouter(
        java.util.Map.of(
            new HttpRouter.RouteKey(HttpMethod.GET, "/v1/health"),
            new HealthHandler(JsonMapperFactory.create())));
  }
}
