package com.onthisday.platform.quiz;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onthisday.platform.http.ErrorResponseWriter;
import com.onthisday.platform.http.HttpRequest;
import com.onthisday.platform.http.HttpResponse;
import com.onthisday.platform.http.HttpRoute;
import com.onthisday.quiz.QuizCatalogService;
import com.onthisday.quiz.QuizUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class QuizCatalogHandler implements HttpRoute {

  private static final Logger LOG = LoggerFactory.getLogger(QuizCatalogHandler.class);

  private final QuizCatalogService service;
  private final QuizResponseMapper responseMapper;
  private final ObjectMapper objectMapper;
  private final ErrorResponseWriter errorResponseWriter;

  public QuizCatalogHandler(QuizCatalogService service, ObjectMapper objectMapper) {
    this.service = service;
    this.responseMapper = new QuizResponseMapper();
    this.objectMapper = objectMapper;
    this.errorResponseWriter = new ErrorResponseWriter(objectMapper);
  }

  @Override
  public HttpResponse handle(HttpRequest request) {
    try {
      return HttpResponse.json(200, objectMapper.writeValueAsString(responseMapper.toCatalogResponse(service.getCatalog())));
    } catch (QuizUnavailableException exception) {
      LOG.warn("quiz_catalog_unavailable reason={}", exception.getMessage());
      return errorResponseWriter.json(503, "quiz_unavailable", "Quiz content is temporarily unavailable.");
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Failed to serialize quiz catalog response.", exception);
    }
  }
}
