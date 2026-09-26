package com.onthisday.platform.quiz;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onthisday.platform.http.ErrorResponseWriter;
import com.onthisday.platform.http.HttpRequest;
import com.onthisday.platform.http.HttpResponse;
import com.onthisday.platform.http.HttpRoute;
import com.onthisday.quiz.InsufficientQuizQuestionsException;
import com.onthisday.quiz.InvalidQuizRequestException;
import com.onthisday.quiz.QuizCollectionNotFoundException;
import com.onthisday.quiz.QuizUnavailableException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class QuickPlayQuizHandler implements HttpRoute {

  private static final Logger LOG = LoggerFactory.getLogger(QuickPlayQuizHandler.class);

  private final com.onthisday.quiz.QuickPlayQuizService service;
  private final QuizResponseMapper responseMapper;
  private final ObjectMapper responseObjectMapper;
  private final ObjectMapper requestObjectMapper;
  private final ErrorResponseWriter errorResponseWriter;

  public QuickPlayQuizHandler(
      com.onthisday.quiz.QuickPlayQuizService service, ObjectMapper objectMapper) {
    this.service = service;
    this.responseMapper = new QuizResponseMapper();
    this.responseObjectMapper = objectMapper;
    this.requestObjectMapper =
        objectMapper.copy().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
    this.errorResponseWriter = new ErrorResponseWriter(objectMapper);
  }

  @Override
  public HttpResponse handle(HttpRequest request) {
    final QuickPlayQuizRequest quizRequest;
    try {
      quizRequest = requestObjectMapper.readValue(request.body(), QuickPlayQuizRequest.class);
    } catch (JsonProcessingException exception) {
      return errorResponseWriter.json(400, "invalid_quiz_request", "Invalid quiz request.");
    }
    if (quizRequest == null || quizRequest.questionCount() == null) {
      return errorResponseWriter.json(400, "invalid_quiz_request", "Invalid quiz request.");
    }

    try {
      var quiz =
          service.createQuiz(
              quizRequest.questionCount(), Optional.ofNullable(quizRequest.collectionId()));
      return HttpResponse.json(200, responseObjectMapper.writeValueAsString(responseMapper.toQuickPlayResponse(quiz)));
    } catch (InvalidQuizRequestException exception) {
      return errorResponseWriter.json(400, "invalid_quiz_request", "Invalid quiz request.");
    } catch (InsufficientQuizQuestionsException exception) {
      return errorResponseWriter.json(
          400, "insufficient_quiz_questions", "Not enough quiz questions are available.");
    } catch (QuizCollectionNotFoundException exception) {
      return errorResponseWriter.json(404, "quiz_collection_not_found", "Quiz collection not found.");
    } catch (QuizUnavailableException exception) {
      LOG.warn("quick_play_quiz_unavailable reason={}", exception.getMessage());
      return errorResponseWriter.json(503, "quiz_unavailable", "Quiz content is temporarily unavailable.");
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Failed to serialize Quick Play quiz response.", exception);
    }
  }
}
