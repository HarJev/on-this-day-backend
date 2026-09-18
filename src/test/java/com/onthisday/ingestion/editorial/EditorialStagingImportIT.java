package com.onthisday.ingestion.editorial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.onthisday.ingestion.CuratedContentImporter;
import com.onthisday.ingestion.CuratedContentValidator;
import com.onthisday.ingestion.quiz.QuizContentImporter;
import com.onthisday.ingestion.quiz.QuizContentValidator;
import java.sql.SQLException;
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
class EditorialStagingImportIT {

  @Container
  static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("on_this_day")
          .withUsername("on_this_day")
          .withPassword("on_this_day");

  private static DataSource dataSource;

  @BeforeAll
  static void migrateDatabase() {
    var postgres = new PGSimpleDataSource();
    postgres.setUrl(POSTGRES.getJdbcUrl());
    postgres.setUser(POSTGRES.getUsername());
    postgres.setPassword(POSTGRES.getPassword());
    dataSource = postgres;
    Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").load().migrate();
  }

  @BeforeEach
  void resetTables() throws SQLException {
    try (var connection = dataSource.getConnection(); var statement = connection.createStatement()) {
      statement.execute("TRUNCATE quiz_daily_question, quiz_daily_challenge, quiz_question_collection, quiz_image, quiz_ordering_item, quiz_option, quiz_source, quiz_question, quiz_collection, daily_event, event_image, event_source, historical_event CASCADE");
    }
  }

  @Test
  void importsAnApprovedSevenDayBatchAndTwentyQuestionPackIdempotently() throws SQLException {
    var historical = EditorialWorkflowFixtures.sevenDayHistoricalContent();
    var quiz = EditorialWorkflowFixtures.twentyQuestionQuizContent();
    var preflight =
        new EditorialBatchPreflight(
            new EditorialReviewValidator(),
            new CuratedContentValidator(),
            new QuizContentValidator(),
            new EditorialBatchSelector());
    var selected = preflight.validate(EditorialWorkflowFixtures.approvedBatch(), historical, quiz);

    assertTrue(selected.valid());
    var historicalImporter = new CuratedContentImporter(dataSource, new CuratedContentValidator());
    var quizImporter = new QuizContentImporter(dataSource, new QuizContentValidator());
    historicalImporter.importContent(selected.historicalContent());
    quizImporter.importContent(selected.quizContent());
    historicalImporter.importContent(selected.historicalContent());
    quizImporter.importContent(selected.quizContent());

    assertEquals(28, countRows("historical_event"));
    assertEquals(7, countRows("daily_event") / 4);
    assertEquals(20, countRows("quiz_question"));
    assertEquals(20, countRows("quiz_source"));
    assertEquals(0, countRows("quiz_daily_challenge"));
  }

  private static int countRows(String table) throws SQLException {
    try (var connection = dataSource.getConnection();
        var statement = connection.createStatement();
        var result = statement.executeQuery("SELECT count(*) FROM " + table)) {
      result.next();
      return result.getInt(1);
    }
  }
}
