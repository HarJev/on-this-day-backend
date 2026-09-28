package com.onthisday.platform.content;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onthisday.content.ContentUnavailableException;
import com.onthisday.content.EventSummary;
import com.onthisday.content.RecentDaysService;
import com.onthisday.content.TodayContent;
import com.onthisday.content.TodayContentRepository;
import com.onthisday.platform.http.HttpMethod;
import com.onthisday.platform.http.HttpRequest;
import java.time.Clock;
import java.time.Instant;
import java.time.MonthDay;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RecentDaysHandlerTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Test
  void returnsRecentDaysJsonNewestFirst() throws Exception {
    var repository = new FakeRepository();
    var response = handler(repository).handle(request(Map.of("timezone", "America/Jamaica")));

    assertEquals(200, response.statusCode());
    assertEquals("application/json", response.headers().get("content-type"));
    assertEquals(6, repository.requestedDates.size());
    assertEquals(
        "{\"days\":[{\"daysAgo\":1,"
            + "\"date\":{\"month\":8,\"day\":21,\"displayDate\":\"Aug 21\"},"
            + "\"featuredEvent\":{\"id\":\"event-8-21\",\"title\":\"Title\",\"year\":\"1485\","
            + "\"historicalDate\":\"August 1485\",\"dateNote\":null}},"
            + "{\"daysAgo\":3,"
            + "\"date\":{\"month\":8,\"day\":19,\"displayDate\":\"Aug 19\"},"
            + "\"featuredEvent\":{\"id\":\"event-8-19\",\"title\":\"Title\",\"year\":\"1485\","
            + "\"historicalDate\":\"August 1485\",\"dateNote\":null}}]}",
        response.body());
  }

  @Test
  void passesRequestedDaysToService() {
    var repository = new FakeRepository();

    var response =
        handler(repository).handle(request(Map.of("timezone", "America/Jamaica", "days", "3")));

    assertEquals(200, response.statusCode());
    assertEquals(List.of(MonthDay.of(8, 21), MonthDay.of(8, 20)), repository.requestedDates);
  }

  @Test
  void mapsInvalidDaysToBadRequest() {
    for (var days : List.of("", "abc", "1", "15", "2.5")) {
      var response =
          handler(new FakeRepository())
              .handle(request(Map.of("timezone", "America/Jamaica", "days", days)));

      assertEquals(400, response.statusCode(), "days=" + days);
      assertEquals(
          "{\"code\":\"invalid_days\",\"message\":\"Invalid number of days.\"}", response.body());
    }
  }

  @Test
  void mapsMissingOrInvalidTimezoneToBadRequest() {
    for (var query : List.<Map<String, String>>of(Map.of(), Map.of("timezone", "Not/AZone"))) {
      var response = handler(new FakeRepository()).handle(request(query));

      assertEquals(400, response.statusCode());
      assertEquals("{\"code\":\"invalid_timezone\",\"message\":\"Invalid timezone.\"}", response.body());
    }
  }

  @Test
  void mapsContentUnavailableToServiceUnavailable() {
    var repository =
        new FakeRepository() {
          @Override
          public Map<MonthDay, EventSummary> findFeaturedEventSummaries(List<MonthDay> dates) {
            throw new ContentUnavailableException("database down");
          }
        };

    var response = handler(repository).handle(request(Map.of("timezone", "America/Jamaica")));

    assertEquals(503, response.statusCode());
    assertEquals(
        "{\"code\":\"content_unavailable\",\"message\":\"Content temporarily unavailable.\"}",
        response.body());
  }

  private static RecentDaysHandler handler(TodayContentRepository repository) {
    return new RecentDaysHandler(
        new RecentDaysService(
            repository, Clock.fixed(Instant.parse("2026-08-23T03:30:00Z"), ZoneOffset.UTC)),
        OBJECT_MAPPER);
  }

  private static HttpRequest request(Map<String, String> queryParameters) {
    return new HttpRequest(HttpMethod.GET, "/v1/days/recent", queryParameters, Map.of(), Map.of(), "");
  }

  private static class FakeRepository implements TodayContentRepository {

    List<MonthDay> requestedDates;

    @Override
    public TodayContent getTodayContent(MonthDay date) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Map<MonthDay, EventSummary> findFeaturedEventSummaries(List<MonthDay> dates) {
      requestedDates = List.copyOf(dates);
      var odd = MonthDay.of(8, 19);
      var summaries = new java.util.HashMap<MonthDay, EventSummary>();
      for (var date : List.of(MonthDay.of(8, 21), odd)) {
        if (dates.contains(date)) {
          summaries.put(
              date,
              new EventSummary(
                  "event-" + date.getMonthValue() + "-" + date.getDayOfMonth(),
                  "Title",
                  "1485",
                  "August 1485",
                  null));
        }
      }
      return summaries;
    }
  }
}
