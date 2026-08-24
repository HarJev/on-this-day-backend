package com.onthisday.platform.content;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onthisday.content.ContentUnavailableException;
import com.onthisday.content.InvalidTimezoneException;
import com.onthisday.content.TodayContentService;
import com.onthisday.platform.http.ErrorResponseWriter;
import com.onthisday.platform.http.HttpRequest;
import com.onthisday.platform.http.HttpResponse;
import com.onthisday.platform.http.HttpRoute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TodayContentHandler implements HttpRoute {

  private static final Logger LOG = LoggerFactory.getLogger(TodayContentHandler.class);

  private final TodayContentService service;
  private final TodayContentResponseMapper responseMapper;
  private final ObjectMapper objectMapper;
  private final ErrorResponseWriter errorResponseWriter;

  public TodayContentHandler(TodayContentService service, ObjectMapper objectMapper) {
    this.service = service;
    this.responseMapper = new TodayContentResponseMapper();
    this.objectMapper = objectMapper;
    this.errorResponseWriter = new ErrorResponseWriter(objectMapper);
  }

  @Override
  public HttpResponse handle(HttpRequest request) {
    try {
      var timezone = request.queryParameter("timezone").orElse(null);
      var content = service.getTodayContent(timezone);
      return HttpResponse.json(200, objectMapper.writeValueAsString(responseMapper.toResponse(content)));
    } catch (InvalidTimezoneException exception) {
      return errorResponseWriter.json(400, "invalid_timezone", "Invalid timezone.");
    } catch (ContentUnavailableException exception) {
      LOG.warn("today_content_unavailable reason={}", exception.getMessage());
      return errorResponseWriter.json(503, "content_unavailable", "Content temporarily unavailable.");
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Failed to serialize today content response.", exception);
    }
  }
}
