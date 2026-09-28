package com.onthisday.quiz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onthisday.ingestion.CuratedContentImporter;
import com.onthisday.ingestion.CuratedContentReader;
import com.onthisday.ingestion.CuratedContentValidator;
import com.onthisday.ingestion.quiz.CuratedQuizContent;
import com.onthisday.ingestion.quiz.CuratedQuizQuestionJson;
import com.onthisday.ingestion.quiz.LoadedQuizQuestionPack;
import com.onthisday.ingestion.quiz.QuizContentImportException;
import com.onthisday.ingestion.quiz.QuizContentImporter;
import com.onthisday.ingestion.quiz.QuizContentReader;
import com.onthisday.ingestion.quiz.QuizContentValidator;
import com.onthisday.ingestion.quiz.QuizQuestionPackFile;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class DateLinkedDailyIT {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final String FEATURED_EVENT = "cern-founded-1954";
  private static final String ADDITIONAL_EVENT = "discovery-return-to-flight-1988";
  private static final Clock SEPTEMBER_29 =
      Clock.fixed(Instant.parse("2026-09-29T12:00:00Z"), ZoneOffset.UTC);

  @Container
  static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16-alpine");

  private static DataSource dataSource;
  private static CuratedQuizContent quiz;

  @BeforeAll
  static void importCanonicalEvents() {
    var postgres = new PGSimpleDataSource();
    postgres.setUrl(POSTGRES.getJdbcUrl());
    postgres.setUser(POSTGRES.getUsername());
    postgres.setPassword(POSTGRES.getPassword());
    dataSource = postgres;
    Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").load().migrate();
    new CuratedContentImporter(dataSource, new CuratedContentValidator())
        .importContent(new CuratedContentReader(OBJECT_MAPPER).read(Path.of("content")));
    quiz = new QuizContentReader(OBJECT_MAPPER).read(Path.of("content/quizzes"));
  }

  @BeforeEach
  void clearAssignments() throws SQLException {
    try (var connection = dataSource.getConnection(); var statement = connection.createStatement()) {
      statement.execute("TRUNCATE quiz_daily_question, quiz_daily_challenge");
    }
  }

  @Test
  void linkedQuestionsLeadTheDailyAssignmentForTheirDate() throws SQLException {
    var published = publishedIds();
    var featuredQuestion = published.get(0);
    var additionalQuestion = published.get(1);
    importQuiz(
        Map.of(featuredQuestion, List.of(FEATURED_EVENT), additionalQuestion, List.of(ADDITIONAL_EVENT)));

    var linked = new JdbcQuizQuestionRepository(dataSource).findPublishedCandidatesLinkedTo(MonthDay.of(9, 29));
    var daily = service(SEPTEMBER_29).getQuiz(ZoneId.of("UTC"), 20);
    var ids = daily.questions().stream().map(question -> question.question().id()).toList();

    assertEquals(2, linked.size());
    assertEquals(featuredQuestion, ids.get(0));
    assertEquals(additionalQuestion, ids.get(5));
    assertEquals(2, count("SELECT count(*) FROM quiz_question_event"));
    assertEquals(
        DailyChallenge.DATE_LINKED_SELECTION_VERSION,
        new JdbcDailyChallengeRepository(dataSource).findByDate(LocalDate.of(2026, 9, 29)).orElseThrow().selectionVersion());
  }

  @Test
  void reimportReplacesLinksAndUnknownEventsRollBack() throws SQLException {
    var question = publishedIds().get(0);
    importQuiz(Map.of(question, List.of(FEATURED_EVENT)));
    importQuiz(Map.of());

    assertEquals(0, count("SELECT count(*) FROM quiz_question_event"));
    var exception =
        assertThrows(QuizContentImportException.class, () -> importQuiz(Map.of(question, List.of("no-such-event-1900"))));
    assertTrue(exception.getMessage().contains("no-such-event-1900"));
    assertEquals(0, count("SELECT count(*) FROM quiz_question_event"));
  }

  private static void importQuiz(Map<String, List<String>> relatedEvents) {
    UnaryOperator<CuratedQuizQuestionJson> link =
        question ->
            new CuratedQuizQuestionJson(
                question.id(),
                question.type(),
                question.difficulty(),
                question.publicationState(),
                question.prompt(),
                question.options(),
                question.correctOptionId(),
                question.items(),
                question.correctOrderItemIds(),
                question.image(),
                question.explanation(),
                question.sources(),
                question.collectionIds(),
                relatedEvents.getOrDefault(question.id(), null));
    var packs =
        quiz.questionPacks().stream()
            .map(
                pack ->
                    new LoadedQuizQuestionPack(
                        pack.relativeFilename(),
                        new QuizQuestionPackFile(
                            pack.file().schemaVersion(), pack.file().questions().stream().map(link).toList())))
            .toList();
    new QuizContentImporter(dataSource, new QuizContentValidator())
        .importContent(new CuratedQuizContent(quiz.collectionsFile(), packs));
  }

  private static List<String> publishedIds() {
    return quiz.questionPacks().stream()
        .flatMap(pack -> pack.file().questions().stream())
        .filter(question -> "published".equals(question.publicationState()))
        .map(CuratedQuizQuestionJson::id)
        .toList();
  }

  private static DailyQuizService service(Clock clock) {
    return new DailyQuizService(
        new JdbcQuizQuestionRepository(dataSource),
        new JdbcDailyChallengeRepository(dataSource),
        new QuizCandidateSelector(),
        new QuizQuestionPresenter(),
        clock);
  }

  private static long count(String sql) throws SQLException {
    try (var connection = dataSource.getConnection();
        var statement = connection.createStatement();
        var rows = statement.executeQuery(sql)) {
      rows.next();
      return rows.getLong(1);
    }
  }
}
