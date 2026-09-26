package com.onthisday.platform.quiz;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onthisday.platform.http.ErrorResponseWriter;
import com.onthisday.platform.http.HttpRequest;
import com.onthisday.platform.http.HttpResponse;
import com.onthisday.platform.http.HttpRoute;
import com.onthisday.quiz.InsufficientQuizQuestionsException;
import com.onthisday.quiz.InvalidQuizRequestException;
import com.onthisday.quiz.QuizUnavailableException;
import java.time.DateTimeException;
import java.time.ZoneId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DailyQuizHandler implements HttpRoute {

  private static final Logger LOG = LoggerFactory.getLogger(DailyQuizHandler.class);

  private final com.onthisday.quiz.DailyQuizService service;
  private final QuizResponseMapper responseMapper;
  private final ObjectMapper objectMapper;
  private final ErrorResponseWriter errorResponseWriter;

  public DailyQuizHandler(com.onthisday.quiz.DailyQuizService service, ObjectMapper objectMapper) {
    this.service = service;
    this.responseMapper = new QuizResponseMapper();
    this.objectMapper = objectMapper;
    this.errorResponseWriter = new ErrorResponseWriter(objectMapper);
  }

  @Override
  public HttpResponse handle(HttpRequest request) {
    final ZoneId timezone;
    try {
      timezone = parseTimezone(request.queryParameter("timezone").orElse(null));
    } catch (InvalidTimezoneException exception) {
      return errorResponseWriter.json(400, "invalid_timezone", "Invalid timezone.");
    }

    final int questionCount;
    try {
      questionCount = parseQuestionCount(request.queryParameter("questionCount").orElse(null));
    } catch (InvalidQuizRequestException exception) {
      return errorResponseWriter.json(400, "invalid_quiz_request", "Invalid quiz request.");
    }

    try {
      var quiz = service.getQuiz(timezone, questionCount);
      return HttpResponse.json(200, objectMapper.writeValueAsString(responseMapper.toDailyResponse(quiz)));
    } catch (InsufficientQuizQuestionsException exception) {
      return errorResponseWriter.json(
          400, "insufficient_quiz_questions", "Not enough quiz questions are available.");
    } catch (InvalidQuizRequestException exception) {
      return errorResponseWriter.json(400, "invalid_quiz_request", "Invalid quiz request.");
    } catch (QuizUnavailableException exception) {
      LOG.warn("daily_quiz_unavailable reason={}", exception.getMessage());
      return errorResponseWriter.json(503, "quiz_unavailable", "Quiz content is temporarily unavailable.");
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Failed to serialize Daily Challenge response.", exception);
    }
  }

  private static ZoneId parseTimezone(String value) {
    if (value == null || value.isBlank()) {
      throw new InvalidTimezoneException();
    }
    try {
      return ZoneId.of(value);
    } catch (DateTimeException exception) {
      throw new InvalidTimezoneException();
    }
  }

  private static int parseQuestionCount(String value) {
    if (value == null || value.isBlank()) {
      throw new InvalidQuizRequestException("questionCount is required");
    }
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException exception) {
      throw new InvalidQuizRequestException("questionCount must be numeric");
    }
  }

  private static final class InvalidTimezoneException extends RuntimeException {}
}
