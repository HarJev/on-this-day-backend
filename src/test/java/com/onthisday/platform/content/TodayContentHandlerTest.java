package com.onthisday.platform.content;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onthisday.content.ContentDate;
import com.onthisday.content.ContentUnavailableException;
import com.onthisday.content.EventImage;
import com.onthisday.content.EventSummary;
import com.onthisday.content.FeaturedEvent;
import com.onthisday.content.TodayContent;
import com.onthisday.content.TodayContentRepository;
import com.onthisday.content.TodayContentService;
import com.onthisday.platform.http.HttpMethod;
import com.onthisday.platform.http.HttpRequest;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.MonthDay;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TodayContentHandlerTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Test
  void returnsTodayContentJson() throws Exception {
    var handler = handler(date -> todayContent());

    var response = handler.handle(request(Map.of("timezone", "America/Jamaica")));
    JsonNode body = OBJECT_MAPPER.readTree(response.body());

    assertEquals(200, response.statusCode());
    assertEquals("application/json", response.headers().get("content-type"));
    assertEquals(8, body.get("date").get("month").intValue());
    assertEquals(22, body.get("date").get("day").intValue());
    assertEquals("Aug 22", body.get("date").get("displayDate").textValue());
    assertEquals("battle-of-bosworth-field-1485", body.get("featuredEvent").get("id").textValue());
    assertEquals(
        "Richard III is defeated at the Battle of Bosworth Field",
        body.get("featuredEvent").get("title").textValue());
    assertEquals("1485", body.get("featuredEvent").get("year").textValue());
    assertEquals("August 22, 1485", body.get("featuredEvent").get("historicalDate").textValue());
    assertEquals("The battle ended the Wars of the Roses.", body.get("featuredEvent").get("summary").textValue());
    assertEquals(
        "A king died in battle 541 years ago today",
        body.get("featuredEvent").get("notificationTitle").textValue());
    assertEquals(
        "Richard III's defeat at Bosworth changed England forever.",
        body.get("featuredEvent").get("notificationBody").textValue());
    assertEquals("Traditional date.", body.get("featuredEvent").get("dateNote").textValue());
    assertEquals("https://example.com/image.jpg", body.get("featuredEvent").get("image").get("url").textValue());
    assertEquals(
        "https://commons.wikimedia.org/",
        body.get("featuredEvent").get("image").get("sourceUrl").textValue());
    assertEquals(1, body.get("additionalEvents").size());
    assertEquals("cook-claims-eastern-australia-1770", body.get("additionalEvents").get(0).get("id").textValue());
  }

  @Test
  void mapsMissingTimezoneToBadRequest() {
    var response = handler(date -> todayContent()).handle(request(Map.of()));

    assertEquals(400, response.statusCode());
    assertEquals("{\"code\":\"invalid_timezone\",\"message\":\"Invalid timezone.\"}", response.body());
  }

  @Test
  void mapsBlankTimezoneToBadRequest() {
    var response = handler(date -> todayContent()).handle(request(Map.of("timezone", " ")));

    assertEquals(400, response.statusCode());
    assertEquals("{\"code\":\"invalid_timezone\",\"message\":\"Invalid timezone.\"}", response.body());
  }

  @Test
  void mapsInvalidTimezoneToBadRequest() {
    var response = handler(date -> todayContent()).handle(request(Map.of("timezone", "Not/AZone")));

    assertEquals(400, response.statusCode());
    assertEquals("{\"code\":\"invalid_timezone\",\"message\":\"Invalid timezone.\"}", response.body());
  }

  @Test
  void mapsContentUnavailableToServiceUnavailable() {
    var response =
        handler(
                date -> {
                  throw new ContentUnavailableException("missing");
                })
            .handle(request(Map.of("timezone", "America/Jamaica")));

    assertEquals(503, response.statusCode());
    assertEquals(
        "{\"code\":\"content_unavailable\",\"message\":\"Content temporarily unavailable.\"}",
        response.body());
  }

  private static TodayContentHandler handler(TodayContentRepository repository) {
    return new TodayContentHandler(
        new TodayContentService(
            repository, Clock.fixed(Instant.parse("2026-08-23T03:30:00Z"), ZoneOffset.UTC)),
        OBJECT_MAPPER);
  }

  private static HttpRequest request(Map<String, String> queryParameters) {
    return new HttpRequest(HttpMethod.GET, "/v1/days/today", queryParameters, Map.of(), Map.of(), "");
  }

  private static TodayContent todayContent() {
    return new TodayContent(
        new ContentDate(8, 22, "Aug 22"),
        new FeaturedEvent(
            "battle-of-bosworth-field-1485",
            "Richard III is defeated at the Battle of Bosworth Field",
            "1485",
            "August 22, 1485",
            "The battle ended the Wars of the Roses.",
            "A king died in battle 541 years ago today",
            "Richard III's defeat at Bosworth changed England forever.",
            new EventImage(
                URI.create("https://example.com/image.jpg"),
                "Richard III at Bosworth.",
                "Wikimedia Commons",
                URI.create("https://commons.wikimedia.org/"),
                "Edmund Blair Leighton",
                "Edmund Blair Leighton",
                "Public Domain Mark 1.0",
                URI.create("https://creativecommons.org/publicdomain/mark/1.0/")),
            "Traditional date."),
        List.of(
            new EventSummary(
                "cook-claims-eastern-australia-1770",
                "James Cook claims eastern Australia for Britain",
                "1770",
                "August 22, 1770",
                null)));
  }
}
