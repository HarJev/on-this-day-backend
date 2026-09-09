package com.onthisday.quiz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onthisday.ingestion.quiz.QuizContentImporter;
import com.onthisday.ingestion.quiz.QuizContentReader;
import com.onthisday.ingestion.quiz.QuizContentValidator;
import com.onthisday.platform.quiz.QuizResponseMapper;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.random.RandomGeneratorFactory;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class ProductionQuizContentIT {

  @Container
  static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("on_this_day")
          .withUsername("on_this_day")
          .withPassword("on_this_day");

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2026-08-24T12:00:00Z"), ZoneOffset.UTC);
  private static DataSource dataSource;

  @BeforeAll
  static void migrateAndImportProductionContent() {
    var postgresDataSource = new PGSimpleDataSource();
    postgresDataSource.setUrl(POSTGRES.getJdbcUrl());
    postgresDataSource.setUser(POSTGRES.getUsername());
    postgresDataSource.setPassword(POSTGRES.getPassword());
    dataSource = postgresDataSource;
    Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").load().migrate();

    var content = new QuizContentReader(OBJECT_MAPPER).read(Path.of("content/quizzes"));
    new QuizContentImporter(dataSource, new QuizContentValidator(true)).importContent(content);
  }

  @Test
  void importedBankSupportsCatalogQuickPlayDailyAndApiSerialization() throws Exception {
    assertPersistedRowCounts();

    var questionRepository = new JdbcQuizQuestionRepository(dataSource);
    var collectionRepository = new JdbcQuizCollectionRepository(dataSource);
    var catalog =
        new QuizCatalogService(new JdbcQuizCatalogRepository(dataSource)).getCatalog();
    assertEquals(60, catalog.mixed().publishedQuestionCount());
    assertEquals(List.of(5, 10, 20), catalog.mixed().supportedQuestionCounts());
    assertEquals(9, catalog.collections().size());

    var quickPlayService =
        new QuickPlayQuizService(
            questionRepository,
            collectionRepository,
            new QuizCandidateSelector(),
            new QuizQuestionPresenter(),
            () -> RandomGeneratorFactory.of("L64X128MixRandom").create(20260824L));
    for (var count : QuizRules.questionCounts()) {
      assertEquals(count, quickPlayService.createQuiz(count, Optional.empty()).questions().size());
    }
    for (var collection : catalog.collections()) {
      for (var count : collection.availability().supportedQuestionCounts()) {
        assertEquals(
            count,
            quickPlayService
                .createQuiz(count, Optional.of(collection.collection().id()))
                .questions()
                .size());
      }
    }

    var dailyService =
        new DailyQuizService(
            questionRepository,
            new JdbcDailyChallengeRepository(dataSource),
            new QuizCandidateSelector(),
            new QuizQuestionPresenter(),
            CLOCK);
    var full = dailyService.getQuiz(ZoneId.of("America/Jamaica"), 20);
    var firstFive = dailyService.getQuiz(ZoneId.of("America/Jamaica"), 5);
    var firstTen = dailyService.getQuiz(ZoneId.of("America/Jamaica"), 10);
    assertEquals(ids(full).subList(0, 5), ids(firstFive));
    assertEquals(ids(full).subList(0, 10), ids(firstTen));
    assertEquals(4, full.questions().stream().map(question -> question.question().type()).distinct().count());

    var json = OBJECT_MAPPER.valueToTree(new QuizResponseMapper().toDailyResponse(full));
    assertEquals(20, json.path("questions").size());
    assertFalse(json.path("questions").get(0).has("timeLimitSeconds"));
    assertTrue(hasQuestionType(json, "multiple_choice"));
    assertTrue(hasQuestionType(json, "true_false"));
    assertTrue(hasQuestionType(json, "image_identification"));
    assertTrue(hasQuestionType(json, "chronological_ordering"));
    var imageQuestion =
        values(json.path("questions"))
            .filter(question -> "image_identification".equals(question.path("type").asText()))
            .findFirst()
            .orElseThrow();
    assertTrue(imageQuestion.path("image").path("url").asText().startsWith("https://upload.wikimedia.org/"));
    assertTrue(imageQuestion.path("image").path("sourceUrl").asText().startsWith("https://commons.wikimedia.org/"));
    assertFalse(imageQuestion.path("image").path("license").asText().isBlank());
  }

  private static void assertPersistedRowCounts() throws SQLException {
    assertEquals(9, count("quiz_collection"));
    assertEquals(60, count("quiz_question"));
    assertEquals(78, count("quiz_source"));
    assertEquals(198, count("quiz_option"));
    assertEquals(24, count("quiz_ordering_item"));
    assertEquals(9, count("quiz_image"));
    assertEquals(109, count("quiz_question_collection"));
    assertEquals(0, count("quiz_daily_challenge"));
  }

  private static int count(String table) throws SQLException {
    try (var connection = dataSource.getConnection();
        var statement = connection.createStatement();
        var resultSet = statement.executeQuery("SELECT count(*) FROM " + table)) {
      resultSet.next();
      return resultSet.getInt(1);
    }
  }

  private static List<String> ids(DailyQuiz quiz) {
    return quiz.questions().stream().map(question -> question.question().id()).toList();
  }

  private static boolean hasQuestionType(com.fasterxml.jackson.databind.JsonNode json, String type) {
    return values(json.path("questions"))
        .anyMatch(question -> type.equals(question.path("type").asText()));
  }

  private static Stream<com.fasterxml.jackson.databind.JsonNode> values(
      com.fasterxml.jackson.databind.JsonNode array) {
    return StreamSupport.stream(array.spliterator(), false);
  }
}
