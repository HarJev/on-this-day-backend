package com.onthisday.platform.quiz;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.onthisday.content.HistoricalEventRepository;
import com.onthisday.content.TodayContentRepository;
import com.onthisday.notifications.DeviceRegistration;
import com.onthisday.notifications.DeviceRegistrationRepository;
import com.onthisday.platform.lambda.ApiGatewayHttpHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onthisday.platform.http.HttpMethod;
import com.onthisday.platform.http.HttpRequest;
import com.onthisday.quiz.CollectionGroup;
import com.onthisday.quiz.DailyChallenge;
import com.onthisday.quiz.DailyChallengeRepository;
import com.onthisday.quiz.DailyQuizService;
import com.onthisday.quiz.MultipleChoiceQuestion;
import com.onthisday.quiz.QuestionPublicationState;
import com.onthisday.quiz.QuizCandidateSelector;
import com.onthisday.quiz.QuizCatalogCounts;
import com.onthisday.quiz.QuizCatalogService;
import com.onthisday.quiz.QuizCollection;
import com.onthisday.quiz.QuizCollectionRepository;
import com.onthisday.quiz.QuizDifficulty;
import com.onthisday.quiz.QuizOption;
import com.onthisday.quiz.QuizQuestion;
import com.onthisday.quiz.QuizQuestionCandidate;
import com.onthisday.quiz.QuizQuestionPresenter;
import com.onthisday.quiz.QuizQuestionRepository;
import com.onthisday.quiz.QuizSource;
import com.onthisday.quiz.QuizUnavailableException;
import com.onthisday.quiz.QuickPlayQuizService;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.random.RandomGeneratorFactory;
import org.junit.jupiter.api.Test;

class QuizHandlersTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2026-08-24T12:00:00Z"), ZoneOffset.UTC);

  @Test
  void returnsCatalogWithExplicitSnakeCaseGroupAndTimerKeys() {
    var handler =
        new QuizCatalogHandler(
            new QuizCatalogService(
                () ->
                    new QuizCatalogCounts(
                        5,
                        List.of(
                            new com.onthisday.quiz.QuizCollectionPublishedCount(
                                new QuizCollection("punic-wars", "Punic Wars", CollectionGroup.CONFLICT_OR_MOVEMENT),
                                5)))),
            OBJECT_MAPPER);

    var response = handler.handle(request(HttpMethod.GET, "/v1/quizzes/catalog", Map.of(), ""));

    assertEquals(200, response.statusCode());
    assertEquals(
        "{\"questionCounts\":[5,10,20],\"quickPlayTimerDefaultsSeconds\":{\"multipleChoice\":20,\"trueFalse\":20,\"imageIdentification\":30,\"chronologicalOrdering\":45},\"mixed\":{\"publishedQuestionCount\":5,\"supportedQuestionCounts\":[5]},\"collections\":[{\"id\":\"punic-wars\",\"name\":\"Punic Wars\",\"group\":\"conflict_or_movement\",\"publishedQuestionCount\":5,\"supportedQuestionCounts\":[5]}]}",
        response.body());
  }

  @Test
  void mapsCatalogUnavailableToServiceUnavailable() {
    var handler =
        new QuizCatalogHandler(
            new QuizCatalogService(
                () -> {
                  throw new QuizUnavailableException("database unavailable");
                }),
            OBJECT_MAPPER);

    var response = handler.handle(request(HttpMethod.GET, "/v1/quizzes/catalog", Map.of(), ""));

    assertError(response, 503, "quiz_unavailable");
  }

  @Test
  void createsMixedAndCollectionQuickPlayQuizzes() throws Exception {
    var handler = new QuickPlayQuizHandler(quickPlayService(5, true), OBJECT_MAPPER);

    var mixed =
        handler.handle(
            request(HttpMethod.POST, "/v1/quizzes/quick-play", Map.of(), "{\"questionCount\":5}"));
    var collection =
        handler.handle(
            request(
                HttpMethod.POST,
                "/v1/quizzes/quick-play",
                Map.of(),
                "{\"questionCount\":5,\"collectionId\":\"punic-wars\"}"));

    assertEquals(200, mixed.statusCode());
    assertEquals("Mixed", OBJECT_MAPPER.readTree(mixed.body()).path("selection").path("displayName").asText());
    assertEquals(20, OBJECT_MAPPER.readTree(mixed.body()).path("questions").get(0).path("timeLimitSeconds").asInt());
    assertEquals("punic-wars", OBJECT_MAPPER.readTree(collection.body()).path("selection").path("collectionId").asText());
  }

  @Test
  void rejectsMalformedMissingUnknownAndInvalidQuickPlayRequests() {
    var handler = new QuickPlayQuizHandler(quickPlayService(5, true), OBJECT_MAPPER);

    assertError(handler.handle(request(HttpMethod.POST, "/v1/quizzes/quick-play", Map.of(), "{")), 400, "invalid_quiz_request");
    assertError(handler.handle(request(HttpMethod.POST, "/v1/quizzes/quick-play", Map.of(), "{}")), 400, "invalid_quiz_request");
    assertError(handler.handle(request(HttpMethod.POST, "/v1/quizzes/quick-play", Map.of(), "{\"questionCount\":5,\"extra\":true}")), 400, "invalid_quiz_request");
    assertError(handler.handle(request(HttpMethod.POST, "/v1/quizzes/quick-play", Map.of(), "{\"questionCount\":6}")), 400, "invalid_quiz_request");
    assertError(handler.handle(request(HttpMethod.POST, "/v1/quizzes/quick-play", Map.of(), "{\"questionCount\":5,\"collectionId\":\" \"}")), 400, "invalid_quiz_request");
  }

  @Test
  void mapsQuickPlayCollectionMissingInsufficientAndUnavailable() {
    var missingCollection = new QuickPlayQuizHandler(quickPlayService(5, false), OBJECT_MAPPER);
    var insufficient = new QuickPlayQuizHandler(quickPlayService(4, true), OBJECT_MAPPER);
    var unavailable =
        new QuickPlayQuizHandler(
            new QuickPlayQuizService(
                new ThrowingQuestionRepository(),
                collectionRepository(true),
                new QuizCandidateSelector(),
                new QuizQuestionPresenter(),
                RandomGeneratorFactory.of("L64X128MixRandom")::create),
            OBJECT_MAPPER);

    assertError(
        missingCollection.handle(request(HttpMethod.POST, "/v1/quizzes/quick-play", Map.of(), "{\"questionCount\":5,\"collectionId\":\"punic-wars\"}")),
        404,
        "quiz_collection_not_found");
    assertError(
        insufficient.handle(request(HttpMethod.POST, "/v1/quizzes/quick-play", Map.of(), "{\"questionCount\":5}")),
        400,
        "insufficient_quiz_questions");
    assertError(
        unavailable.handle(request(HttpMethod.POST, "/v1/quizzes/quick-play", Map.of(), "{\"questionCount\":5}")),
        503,
        "quiz_unavailable");
  }

  @Test
  void validatesDailyTimezoneAndQuestionCountBeforeServiceInvocation() {
    var handler = new DailyQuizHandler(dailyQuizService(20), OBJECT_MAPPER);

    assertError(handler.handle(request(HttpMethod.GET, "/v1/quizzes/daily", Map.of(), "")), 400, "invalid_timezone");
    assertError(handler.handle(request(HttpMethod.GET, "/v1/quizzes/daily", Map.of("timezone", " "), "")), 400, "invalid_timezone");
    assertError(handler.handle(request(HttpMethod.GET, "/v1/quizzes/daily", Map.of("timezone", "Not/AZone"), "")), 400, "invalid_timezone");
    assertError(handler.handle(request(HttpMethod.GET, "/v1/quizzes/daily", Map.of("timezone", "America/Jamaica"), "")), 400, "invalid_quiz_request");
    assertError(handler.handle(request(HttpMethod.GET, "/v1/quizzes/daily", Map.of("timezone", "America/Jamaica", "questionCount", "six"), "")), 400, "invalid_quiz_request");
  }

  @Test
  void returnsDailyTimerMetadataForAllSupportedCountsWithoutPerQuestionTimer() throws Exception {
    var handler = new DailyQuizHandler(dailyQuizService(20), OBJECT_MAPPER);

    for (var expected : Map.of(5, 120, 10, 240, 20, 480).entrySet()) {
      var response =
          handler.handle(
              request(
                  HttpMethod.GET,
                  "/v1/quizzes/daily",
                  Map.of("timezone", "America/Jamaica", "questionCount", expected.getKey().toString()),
                  ""));
      var root = OBJECT_MAPPER.readTree(response.body());
      assertEquals(200, response.statusCode());
      assertEquals(expected.getValue().intValue(), root.path("timer").path("durationSeconds").asInt());
      assertEquals("Aug 24", root.path("date").path("displayDate").asText());
      assertEquals(20, root.path("assignmentQuestionCount").asInt());
      assertEquals(expected.getKey().intValue(), root.path("questions").size());
      assertEquals(false, root.path("questions").get(0).has("timeLimitSeconds"));
    }
  }

  @Test
  void mapsDailyInsufficientAndUnavailable() {
    var insufficient = new DailyQuizHandler(dailyQuizService(19), OBJECT_MAPPER);
    var unavailable =
        new DailyQuizHandler(
            new DailyQuizService(
                new ThrowingQuestionRepository(),
                new MemoryDailyChallengeRepository(),
                new QuizCandidateSelector(),
                new QuizQuestionPresenter(),
                CLOCK),
            OBJECT_MAPPER);
    var request =
        request(
            HttpMethod.GET,
            "/v1/quizzes/daily",
            Map.of("timezone", "America/Jamaica", "questionCount", "5"),
            "");

    assertError(insufficient.handle(request), 400, "insufficient_quiz_questions");
    assertError(unavailable.handle(request), 503, "quiz_unavailable");
  }

  @Test
  void routesQuickPlayThroughTheInjectableApiGatewayHandler() throws Exception {
    var handler =
        new ApiGatewayHttpHandler(
            unavailableTodayRepository(),
            unavailableEventRepository(),
            noOpDeviceRepository(),
            CLOCK,
            new QuizApiServices(
                new QuizCatalogService(() -> new QuizCatalogCounts(5, List.of())),
                quickPlayService(5, true),
                dailyQuizService(20)));
    var event = apiGatewayEvent("POST", "/v1/quizzes/quick-play");
    event.setBody("{\"questionCount\":5}");

    var response = handler.handleRequest(event, null);

    assertEquals(200, response.getStatusCode());
    assertEquals("quick_play", OBJECT_MAPPER.readTree(response.getBody()).path("mode").asText());
  }

  private static QuickPlayQuizService quickPlayService(int questionCount, boolean collectionExists) {
    var questions = questions(questionCount);
    return new QuickPlayQuizService(
        new InMemoryQuestionRepository(questions),
        collectionRepository(collectionExists),
        new QuizCandidateSelector(),
        new QuizQuestionPresenter(),
        RandomGeneratorFactory.of("L64X128MixRandom")::create);
  }

  private static DailyQuizService dailyQuizService(int questionCount) {
    return new DailyQuizService(
        new InMemoryQuestionRepository(questions(questionCount)),
        new MemoryDailyChallengeRepository(),
        new QuizCandidateSelector(),
        new QuizQuestionPresenter(),
        CLOCK);
  }

  private static QuizCollectionRepository collectionRepository(boolean collectionExists) {
    return new QuizCollectionRepository() {
      @Override
      public Optional<QuizCollection> findById(String collectionId) {
        return collectionExists
            ? Optional.of(new QuizCollection(collectionId, "Punic Wars", CollectionGroup.CONFLICT_OR_MOVEMENT))
            : Optional.empty();
      }

      @Override
      public List<QuizCollection> findAll() {
        return List.of();
      }
    };
  }

  private static List<QuizQuestion> questions(int count) {
    var values = new ArrayList<QuizQuestion>();
    for (var index = 1; index <= count; index++) {
      values.add(
          new MultipleChoiceQuestion(
              "question-" + index,
              QuizDifficulty.EASY,
              QuestionPublicationState.PUBLISHED,
              "Prompt " + index,
              "Explanation " + index,
              List.of(new QuizSource("Source", URI.create("https://example.com/source"))),
              List.of(
                  new QuizOption("option-1", "Option 1", 1, true),
                  new QuizOption("option-2", "Option 2", 2, false),
                  new QuizOption("option-3", "Option 3", 3, false),
                  new QuizOption("option-4", "Option 4", 4, false))));
    }
    return List.copyOf(values);
  }

  private static HttpRequest request(
      HttpMethod method, String path, Map<String, String> queryParameters, String body) {
    return new HttpRequest(method, path, queryParameters, Map.of(), Map.of(), body);
  }

  private static APIGatewayV2HTTPEvent apiGatewayEvent(String method, String path) {
    var event = new APIGatewayV2HTTPEvent();
    var requestContext = new APIGatewayV2HTTPEvent.RequestContext();
    var http = new APIGatewayV2HTTPEvent.RequestContext.Http();
    http.setMethod(method);
    http.setPath(path);
    requestContext.setHttp(http);
    event.setRequestContext(requestContext);
    return event;
  }

  private static TodayContentRepository unavailableTodayRepository() {
    return date -> {
      throw new AssertionError("today route should not be called");
    };
  }

  private static HistoricalEventRepository unavailableEventRepository() {
    return eventId -> {
      throw new AssertionError("event route should not be called");
    };
  }

  private static DeviceRegistrationRepository noOpDeviceRepository() {
    return new DeviceRegistrationRepository() {
      @Override
      public void upsert(DeviceRegistration registration) {}

      @Override
      public void deleteByToken(String token) {}

      @Override
      public List<DeviceRegistration> findEligibleForNotifications() {
        return List.of();
      }
    };
  }

  private static void assertError(com.onthisday.platform.http.HttpResponse response, int status, String code) {
    assertEquals(status, response.statusCode());
    try {
      assertEquals(code, OBJECT_MAPPER.readTree(response.body()).path("code").asText());
    } catch (com.fasterxml.jackson.core.JsonProcessingException exception) {
      throw new AssertionError("Error response was not JSON.", exception);
    }
  }

  private static final class InMemoryQuestionRepository implements QuizQuestionRepository {
    private final List<QuizQuestion> questions;

    private InMemoryQuestionRepository(List<QuizQuestion> questions) {
      this.questions = questions;
    }

    @Override
    public List<QuizQuestionCandidate> findPublishedCandidates() {
      return candidates();
    }

    @Override
    public List<QuizQuestionCandidate> findPublishedCandidatesByCollectionId(String collectionId) {
      return candidates();
    }

    @Override
    public List<QuizQuestion> findByIdsInOrder(List<String> questionIds) {
      return questionIds.stream()
          .map(id -> questions.stream().filter(question -> question.id().equals(id)).findFirst().orElseThrow())
          .toList();
    }

    private List<QuizQuestionCandidate> candidates() {
      return questions.stream()
          .map(question -> new QuizQuestionCandidate(question.id(), question.type(), question.difficulty()))
          .toList();
    }
  }

  private static final class ThrowingQuestionRepository implements QuizQuestionRepository {
    @Override
    public List<QuizQuestionCandidate> findPublishedCandidates() {
      throw new QuizUnavailableException("database unavailable");
    }

    @Override
    public List<QuizQuestionCandidate> findPublishedCandidatesByCollectionId(String collectionId) {
      throw new QuizUnavailableException("database unavailable");
    }

    @Override
    public List<QuizQuestion> findByIdsInOrder(List<String> questionIds) {
      throw new QuizUnavailableException("database unavailable");
    }
  }

  private static final class MemoryDailyChallengeRepository implements DailyChallengeRepository {
    private DailyChallenge challenge;

    @Override
    public Optional<DailyChallenge> findByDate(LocalDate date) {
      return Optional.ofNullable(challenge);
    }

    @Override
    public boolean insertIfAbsent(DailyChallenge challenge) {
      if (this.challenge != null) {
        return false;
      }
      this.challenge = challenge;
      return true;
    }
  }
}
