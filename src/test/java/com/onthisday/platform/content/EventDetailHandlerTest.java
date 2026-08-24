package com.onthisday.platform.content;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onthisday.content.ContentUnavailableException;
import com.onthisday.content.EventImage;
import com.onthisday.content.EventNotFoundException;
import com.onthisday.content.EventSource;
import com.onthisday.content.HistoricalEvent;
import com.onthisday.platform.http.HttpMethod;
import com.onthisday.platform.http.HttpRequest;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EventDetailHandlerTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Test
  void returnsEventDetailJson() throws Exception {
    var handler = new EventDetailHandler(eventId -> eventDetail(), OBJECT_MAPPER);

    var response = handler.handle(request(Map.of("eventId", "battle-of-bosworth-field-1485")));
    JsonNode body = OBJECT_MAPPER.readTree(response.body());

    assertEquals(200, response.statusCode());
    assertEquals("application/json", response.headers().get("content-type"));
    assertEquals("battle-of-bosworth-field-1485", body.get("id").textValue());
    assertEquals("Richard III is defeated at the Battle of Bosworth Field", body.get("title").textValue());
    assertEquals("1485", body.get("year").textValue());
    assertEquals("August 22, 1485", body.get("historicalDate").textValue());
    assertEquals("The battle ended the Wars of the Roses.", body.get("summary").textValue());
    assertEquals("A concise description of why it mattered.", body.get("description").textValue());
    assertEquals("Traditional date.", body.get("dateNote").textValue());
    assertEquals(1, body.get("sources").size());
    assertEquals("Encyclopaedia Britannica", body.get("sources").get(0).get("name").textValue());
    assertEquals("https://www.britannica.com/", body.get("sources").get(0).get("url").textValue());
    assertEquals("https://example.com/image.jpg", body.get("primaryImage").get("url").textValue());
    assertEquals("https://commons.wikimedia.org/", body.get("primaryImage").get("sourceUrl").textValue());
    assertEquals(1, body.get("images").size());
    assertEquals("https://example.com/image.jpg", body.get("images").get(0).get("url").textValue());
  }

  @Test
  void mapsMissingEventIdToBadRequest() {
    var response = new EventDetailHandler(eventId -> eventDetail(), OBJECT_MAPPER).handle(request(Map.of()));

    assertEquals(400, response.statusCode());
    assertEquals("{\"code\":\"invalid_event_id\",\"message\":\"Invalid event ID.\"}", response.body());
  }

  @Test
  void mapsBlankEventIdToBadRequest() {
    var response =
        new EventDetailHandler(eventId -> eventDetail(), OBJECT_MAPPER)
            .handle(request(Map.of("eventId", " ")));

    assertEquals(400, response.statusCode());
    assertEquals("{\"code\":\"invalid_event_id\",\"message\":\"Invalid event ID.\"}", response.body());
  }

  @Test
  void mapsEventNotFoundToNotFound() {
    var response =
        new EventDetailHandler(
                eventId -> {
                  throw new EventNotFoundException(eventId);
                },
                OBJECT_MAPPER)
            .handle(request(Map.of("eventId", "missing-event")));

    assertEquals(404, response.statusCode());
    assertEquals("{\"code\":\"event_not_found\",\"message\":\"Event not found.\"}", response.body());
  }

  @Test
  void mapsContentUnavailableToServiceUnavailable() {
    var response =
        new EventDetailHandler(
                eventId -> {
                  throw new ContentUnavailableException("missing");
                },
                OBJECT_MAPPER)
            .handle(request(Map.of("eventId", "battle-of-bosworth-field-1485")));

    assertEquals(503, response.statusCode());
    assertEquals(
        "{\"code\":\"content_unavailable\",\"message\":\"Content temporarily unavailable.\"}",
        response.body());
  }

  private static HttpRequest request(Map<String, String> pathParameters) {
    return new HttpRequest(HttpMethod.GET, "/v1/events/battle-of-bosworth-field-1485", Map.of(), pathParameters, Map.of(), "");
  }

  private static HistoricalEvent eventDetail() {
    var image =
        new EventImage(
            URI.create("https://example.com/image.jpg"),
            "Richard III at Bosworth.",
            "Wikimedia Commons",
            URI.create("https://commons.wikimedia.org/"),
            "Edmund Blair Leighton",
            "Edmund Blair Leighton",
            "Public Domain Mark 1.0",
            URI.create("https://creativecommons.org/publicdomain/mark/1.0/"));
    return new HistoricalEvent(
        "battle-of-bosworth-field-1485",
        "Richard III is defeated at the Battle of Bosworth Field",
        "1485",
        "August 22, 1485",
        "The battle ended the Wars of the Roses.",
        "A concise description of why it mattered.",
        List.of(new EventSource("Encyclopaedia Britannica", URI.create("https://www.britannica.com/"))),
        image,
        List.of(image),
        "Traditional date.");
  }
}
