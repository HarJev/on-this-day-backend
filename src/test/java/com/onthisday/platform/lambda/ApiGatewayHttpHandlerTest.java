package com.onthisday.platform.lambda;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.onthisday.content.ContentDate;
import com.onthisday.content.EventSource;
import com.onthisday.content.EventSummary;
import com.onthisday.content.FeaturedEvent;
import com.onthisday.content.HistoricalEvent;
import com.onthisday.content.TodayContent;
import com.onthisday.notifications.DeviceRegistration;
import com.onthisday.notifications.DeviceRegistrationRepository;
import com.onthisday.platform.http.ApiRoutes;
import com.onthisday.platform.http.HttpMethod;
import com.onthisday.platform.http.HttpResponse;
import com.onthisday.platform.http.HttpRoute;
import com.onthisday.platform.http.HttpRouter;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
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
    var response = new ApiGatewayHttpHandler(ApiRoutes.healthOnly()).handleRequest(event("GET", "/v1/health"), null);

    assertEquals(200, response.getStatusCode());
    assertEquals("application/json", response.getHeaders().get("content-type"));
    assertEquals("{\"status\":\"ok\"}", response.getBody());
  }

  @Test
  void injectedHandlerServesTodayContentRoute() {
    var handler =
        new ApiGatewayHttpHandler(
            date ->
                new TodayContent(
                    new ContentDate(date.getMonthValue(), date.getDayOfMonth(), "Aug 22"),
                    new FeaturedEvent(
                        "battle-of-bosworth-field-1485",
                        "Richard III is defeated at the Battle of Bosworth Field",
                        "1485",
                        "August 22, 1485",
                        "The battle ended the Wars of the Roses.",
                        "A king died in battle 541 years ago today",
                        "Richard III's defeat at Bosworth changed England forever.",
                        null,
                        null),
                    List.of(
                        new EventSummary(
                            "cook-claims-eastern-australia-1770",
                            "James Cook claims eastern Australia for Britain",
                            "1770",
                            "August 22, 1770",
                            null))),
            eventId -> eventDetail(),
            new RecordingDeviceRegistrationRepository(),
            Clock.fixed(Instant.parse("2026-08-23T03:30:00Z"), ZoneOffset.UTC));

    var event = event("GET", "/v1/days/today");
    event.setQueryStringParameters(Map.of("timezone", "America/Jamaica"));
    var response = handler.handleRequest(event, null);

    assertEquals(200, response.getStatusCode());
    assertEquals("application/json", response.getHeaders().get("content-type"));
    assertEquals(
        "{\"date\":{\"month\":8,\"day\":22,\"displayDate\":\"Aug 22\"},"
            + "\"featuredEvent\":{\"id\":\"battle-of-bosworth-field-1485\","
            + "\"title\":\"Richard III is defeated at the Battle of Bosworth Field\","
            + "\"year\":\"1485\","
            + "\"historicalDate\":\"August 22, 1485\","
            + "\"summary\":\"The battle ended the Wars of the Roses.\","
            + "\"notificationTitle\":\"A king died in battle 541 years ago today\","
            + "\"notificationBody\":\"Richard III's defeat at Bosworth changed England forever.\","
            + "\"image\":null,"
            + "\"dateNote\":null},"
            + "\"additionalEvents\":[{\"id\":\"cook-claims-eastern-australia-1770\","
            + "\"title\":\"James Cook claims eastern Australia for Britain\","
            + "\"year\":\"1770\","
            + "\"historicalDate\":\"August 22, 1770\","
            + "\"dateNote\":null}]}",
        response.getBody());
  }

  @Test
  void injectedHandlerServesEventDetailRoute() {
    var handler =
        new ApiGatewayHttpHandler(
            date ->
                new TodayContent(
                    new ContentDate(date.getMonthValue(), date.getDayOfMonth(), "Aug 22"),
                    new FeaturedEvent(
                        "battle-of-bosworth-field-1485",
                        "Richard III is defeated at the Battle of Bosworth Field",
                        "1485",
                        "August 22, 1485",
                        "The battle ended the Wars of the Roses.",
                        "A king died in battle 541 years ago today",
                        "Richard III's defeat at Bosworth changed England forever.",
                        null,
                        null),
                    List.of()),
            eventId -> eventDetail(),
            new RecordingDeviceRegistrationRepository(),
            Clock.fixed(Instant.parse("2026-08-23T03:30:00Z"), ZoneOffset.UTC));

    var response =
        handler.handleRequest(event("GET", "/v1/events/battle-of-bosworth-field-1485"), null);

    assertEquals(200, response.getStatusCode());
    assertEquals(
        "{\"id\":\"battle-of-bosworth-field-1485\","
            + "\"title\":\"Richard III is defeated at the Battle of Bosworth Field\","
            + "\"year\":\"1485\","
            + "\"historicalDate\":\"August 22, 1485\","
            + "\"summary\":\"The battle ended the Wars of the Roses.\","
            + "\"description\":\"A concise description of why it mattered.\","
            + "\"sources\":[{\"name\":\"Encyclopaedia Britannica\","
            + "\"url\":\"https://www.britannica.com/\"}],"
            + "\"primaryImage\":null,"
            + "\"images\":[],"
            + "\"dateNote\":null}",
        response.getBody());
  }

  @Test
  void returnsRouteNotFoundForMissingPath() {
    var response = new ApiGatewayHttpHandler(ApiRoutes.healthOnly()).handleRequest(event("GET", "/missing"), null);

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

  private static HistoricalEvent eventDetail() {
    return new HistoricalEvent(
        "battle-of-bosworth-field-1485",
        "Richard III is defeated at the Battle of Bosworth Field",
        "1485",
        "August 22, 1485",
        "The battle ended the Wars of the Roses.",
        "A concise description of why it mattered.",
        List.of(new EventSource("Encyclopaedia Britannica", URI.create("https://www.britannica.com/"))),
        null,
        List.of(),
        null);
  }

  private static final class RecordingDeviceRegistrationRepository implements DeviceRegistrationRepository {

    @Override
    public void upsert(DeviceRegistration registration) {}

    @Override
    public void deleteByToken(String token) {}

    @Override
    public List<DeviceRegistration> findEligibleForNotifications() {
      return List.of();
    }
  }
}
