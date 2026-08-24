package com.onthisday.platform.content;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onthisday.content.ContentUnavailableException;
import com.onthisday.content.EventNotFoundException;
import com.onthisday.content.HistoricalEventRepository;
import com.onthisday.platform.http.ErrorResponseWriter;
import com.onthisday.platform.http.HttpRequest;
import com.onthisday.platform.http.HttpResponse;
import com.onthisday.platform.http.HttpRoute;

public final class EventDetailHandler implements HttpRoute {

  private final HistoricalEventRepository repository;
  private final EventDetailResponseMapper responseMapper;
  private final ObjectMapper objectMapper;
  private final ErrorResponseWriter errorResponseWriter;

  public EventDetailHandler(HistoricalEventRepository repository, ObjectMapper objectMapper) {
    this.repository = repository;
    this.responseMapper = new EventDetailResponseMapper();
    this.objectMapper = objectMapper;
    this.errorResponseWriter = new ErrorResponseWriter(objectMapper);
  }

  @Override
  public HttpResponse handle(HttpRequest request) {
    var eventId = request.pathParameter("eventId").orElse("");
    if (eventId.isBlank()) {
      return errorResponseWriter.json(400, "invalid_event_id", "Invalid event ID.");
    }

    try {
      var event = repository.getEvent(eventId);
      return HttpResponse.json(200, objectMapper.writeValueAsString(responseMapper.toResponse(event)));
    } catch (EventNotFoundException exception) {
      return errorResponseWriter.json(404, "event_not_found", "Event not found.");
    } catch (ContentUnavailableException exception) {
      return errorResponseWriter.json(503, "content_unavailable", "Content temporarily unavailable.");
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Failed to serialize event detail response.", exception);
    }
  }
}
