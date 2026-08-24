package com.onthisday.platform.http;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.onthisday.content.ContentDate;
import com.onthisday.content.EventSource;
import com.onthisday.content.EventSummary;
import com.onthisday.content.FeaturedEvent;
import com.onthisday.content.HistoricalEvent;
import com.onthisday.content.TodayContent;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ApiRoutesTest {

  @Test
  void registersTodayContentRouteWithInjectedRepository() {
    var router =
        ApiRoutes.create(
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
            Clock.fixed(Instant.parse("2026-08-23T03:30:00Z"), ZoneOffset.UTC));

    var response =
        router.route(
            new HttpRequest(
                HttpMethod.GET,
                "/v1/days/today",
                Map.of("timezone", "America/Jamaica"),
                Map.of(),
                Map.of(),
                ""));

    assertEquals(200, response.statusCode());
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
        response.body());
  }

  @Test
  void registersEventDetailRouteWithInjectedRepository() {
    var router =
        ApiRoutes.create(
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
            Clock.fixed(Instant.parse("2026-08-23T03:30:00Z"), ZoneOffset.UTC));

    var response =
        router.route(
            new HttpRequest(
                HttpMethod.GET,
                "/v1/events/battle-of-bosworth-field-1485",
                Map.of(),
                Map.of(),
                Map.of(),
                ""));

    assertEquals(200, response.statusCode());
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
        response.body());
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
}
