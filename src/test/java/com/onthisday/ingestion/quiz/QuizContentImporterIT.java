package com.onthisday.ingestion.quiz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
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
class QuizContentImporterIT {

  @Container
  static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("on_this_day")
          .withUsername("on_this_day")
          .withPassword("on_this_day");

  private static DataSource dataSource;

  @BeforeAll
  static void migrateDatabase() {
    var postgresDataSource = new PGSimpleDataSource();
    postgresDataSource.setUrl(POSTGRES.getJdbcUrl());
    postgresDataSource.setUser(POSTGRES.getUsername());
    postgresDataSource.setPassword(POSTGRES.getPassword());
    dataSource = postgresDataSource;

    Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").load().migrate();
  }

  @BeforeEach
  void resetQuizTables() throws SQLException {
    try (var connection = dataSource.getConnection();
        var statement = connection.createStatement()) {
      statement.execute(
          """
          TRUNCATE quiz_daily_question,
            quiz_daily_challenge,
            quiz_question_collection,
            quiz_image,
            quiz_ordering_item,
            quiz_option,
            quiz_source,
            quiz_question,
            quiz_collection
          CASCADE
          """);
    }
  }

  @Test
  void importsAllQuestionShapesAndCanRunTwiceWithoutDuplicates() throws SQLException {
    var importer = importer();
    var content = validContent();

    var firstResult = importer.importContent(content);
    var secondResult = importer.importContent(content);

    assertTrue(firstResult.valid());
    assertTrue(secondResult.valid());
    assertEquals(2, countRows("quiz_collection"));
    assertEquals(4, countRows("quiz_question"));
    assertEquals(10, countRows("quiz_option"));
    assertEquals(4, countRows("quiz_ordering_item"));
    assertEquals(4, countRows("quiz_source"));
    assertEquals(1, countRows("quiz_image"));
    assertEquals(3, countRows("quiz_question_collection"));
    assertEquals(0, countRows("quiz_daily_challenge"));
    assertEquals(0, countRows("quiz_daily_question"));

    assertEquals(
        "tenochtitlan",
        queryString(
            """
            SELECT option_id
            FROM quiz_option
            WHERE question_id = 'capital-of-aztec-empire'
              AND is_correct
            """));
    assertEquals(
        "sputnik-1",
        queryString(
            """
            SELECT item_id
            FROM quiz_ordering_item
            WHERE question_id = 'order-spaceflight-milestones'
              AND correct_position = 1
            """));
  }

  @Test
  void importUpdatesExistingQuestionReplacesChildrenRetiresAndLeavesOmittedQuestionsUntouched()
      throws SQLException {
    importer().importContent(validContent());

    var updateContent =
        new CuratedQuizContent(
            new QuizCollectionsFile(
                1,
                java.util.List.of(
                    new CuratedQuizCollectionJson("ancient-history", "Ancient History Updated", "historical_period"))),
            java.util.List.of(
                new LoadedQuizQuestionPack(
                    "questions/update.json",
                    new QuizQuestionPackFile(
                        1,
                        java.util.List.of(
                            multipleChoice(
                                "capital-of-aztec-empire",
                                "Which city anchored the Aztec Empire?",
                                "A corrected explanation.",
                                "retired",
                                java.util.List.of("ancient-history")))))));

    importer().importContent(updateContent);

    assertEquals("retired", queryString("SELECT publication_state FROM quiz_question WHERE question_id = 'capital-of-aztec-empire'"));
    assertEquals("Which city anchored the Aztec Empire?", queryString("SELECT prompt FROM quiz_question WHERE question_id = 'capital-of-aztec-empire'"));
    assertEquals("A corrected explanation.", queryString("SELECT explanation FROM quiz_question WHERE question_id = 'capital-of-aztec-empire'"));
    assertEquals("Tenochtitlan updated", queryString("SELECT option_text FROM quiz_option WHERE question_id = 'capital-of-aztec-empire' AND option_id = 'tenochtitlan'"));
    assertEquals(1, countWhere("quiz_source", "question_id = 'capital-of-aztec-empire'"));
    assertEquals(4, countRows("quiz_question"));
    assertEquals("published", queryString("SELECT publication_state FROM quiz_question WHERE question_id = 'identify-battle-of-bosworth'"));
  }

  @Test
  void failedImportRollsBackCompletely() throws SQLException {
    importer().importContent(validContent());
    createCompletedDailyAssignment(LocalDate.of(2026, 8, 25));

    var failingContent =
        new CuratedQuizContent(
            new QuizCollectionsFile(1, java.util.List.of()),
            java.util.List.of(
                new LoadedQuizQuestionPack(
                    "questions/failing.json",
                    new QuizQuestionPackFile(
                        1,
                        java.util.List.of(
                            multipleChoice(
                                "new-question-before-failure",
                                "This should roll back",
                                "This should roll back",
                                "published",
                                java.util.List.of()),
                            chronological(
                                "capital-of-aztec-empire",
                                "Rollback prompt",
                                "Rollback explanation"))))));

    var exception = assertThrows(QuizContentImportException.class, () -> importer().importContent(failingContent));

    assertTrue(exception.getMessage().contains("failed"));
    assertEquals("Which city was the capital of the Aztec Empire?", queryString("SELECT prompt FROM quiz_question WHERE question_id = 'capital-of-aztec-empire'"));
    assertEquals(0, countWhere("quiz_question", "question_id = 'new-question-before-failure'"));
  }

  @Test
  void completedDailyAssignmentsRemainUnchangedAndAssignedTypeChangeFails() throws SQLException {
    importer().importContent(validContent());
    createCompletedDailyAssignment(LocalDate.of(2026, 8, 24));

    var originalDailyCount = countRows("quiz_daily_question");
    var typeChange =
        new CuratedQuizContent(
            new QuizCollectionsFile(1, java.util.List.of()),
            java.util.List.of(
                new LoadedQuizQuestionPack(
                    "questions/type-change.json",
                    new QuizQuestionPackFile(
                        1,
                        java.util.List.of(
                            chronological(
                                "capital-of-aztec-empire",
                                "Try to change assigned question type",
                                "This should fail."))))));

    assertThrows(QuizContentImportException.class, () -> importer().importContent(typeChange));

    assertEquals(originalDailyCount, countRows("quiz_daily_question"));
    assertEquals("multiple_choice", queryString("SELECT question_type FROM quiz_question WHERE question_id = 'capital-of-aztec-empire'"));
  }

  private static QuizContentImporter importer() {
    return new QuizContentImporter(dataSource, new QuizContentValidator());
  }

  private static CuratedQuizContent validContent() {
    return new QuizContentReader(new ObjectMapper()).read(java.nio.file.Path.of("src/test/resources/ingestion/quiz/valid"));
  }

  private static CuratedQuizQuestionJson multipleChoice(
      String id,
      String prompt,
      String explanation,
      String publicationState,
      java.util.List<String> collectionIds) {
    return new CuratedQuizQuestionJson(
        id,
        "multiple_choice",
        "easy",
        publicationState,
        prompt,
        java.util.List.of(
            new CuratedQuizOptionJson("tenochtitlan", "Tenochtitlan updated"),
            new CuratedQuizOptionJson("cusco", "Cusco"),
            new CuratedQuizOptionJson("teotihuacan", "Teotihuacan"),
            new CuratedQuizOptionJson("tikal", "Tikal")),
        "tenochtitlan",
        null,
        null,
        null,
        explanation,
        java.util.List.of(new CuratedQuizSourceJson("Updated source", "https://example.com/updated")),
        collectionIds);
  }

  private static CuratedQuizQuestionJson chronological(String id, String prompt, String explanation) {
    return new CuratedQuizQuestionJson(
        id,
        "chronological_ordering",
        "hard",
        "published",
        prompt,
        null,
        null,
        java.util.List.of(
            new CuratedQuizOrderingItemJson("one", "One"),
            new CuratedQuizOrderingItemJson("two", "Two"),
            new CuratedQuizOrderingItemJson("three", "Three"),
            new CuratedQuizOrderingItemJson("four", "Four")),
        java.util.List.of("one", "two", "three", "four"),
        null,
        explanation,
        java.util.List.of(new CuratedQuizSourceJson("Source", "https://example.com/source")),
        java.util.List.of());
  }

  private static void createCompletedDailyAssignment(LocalDate date) throws SQLException {
    try (var connection = dataSource.getConnection()) {
      connection.setAutoCommit(false);
      try {
        insertGeneratedQuestions(connection);
        try (var statement = connection.prepareStatement("INSERT INTO quiz_daily_challenge (challenge_date) VALUES (?)")) {
          statement.setObject(1, date);
          statement.executeUpdate();
        }
        try (var statement =
            connection.prepareStatement(
                "INSERT INTO quiz_daily_question (challenge_date, position, question_id) VALUES (?, ?, ?)")) {
          statement.setObject(1, date);
          statement.setInt(2, 1);
          statement.setString(3, "capital-of-aztec-empire");
          statement.addBatch();
          for (int index = 2; index <= 20; index++) {
            statement.setObject(1, date);
            statement.setInt(2, index);
            statement.setString(3, "daily-assignment-question-" + index);
            statement.addBatch();
          }
          statement.executeBatch();
        }
        try (var statement = connection.prepareStatement("UPDATE quiz_daily_challenge SET completed_at = now() WHERE challenge_date = ?")) {
          statement.setObject(1, date);
          statement.executeUpdate();
        }
        connection.commit();
      } catch (SQLException | RuntimeException exception) {
        connection.rollback();
        throw exception;
      }
    }
  }

  private static void insertGeneratedQuestions(Connection connection) throws SQLException {
    for (int index = 2; index <= 20; index++) {
      var id = "daily-assignment-question-" + index;
      insertQuestion(connection, id);
    }
  }

  private static void insertQuestion(Connection connection, String id) throws SQLException {
    try (var question =
            connection.prepareStatement(
                """
                INSERT INTO quiz_question (
                  question_id,
                  question_type,
                  difficulty,
                  publication_state,
                  prompt,
                  explanation
                )
                VALUES (?, 'multiple_choice', 'easy', 'published', ?, ?)
                """);
        var source =
            connection.prepareStatement(
                "INSERT INTO quiz_source (question_id, display_order, display_name, url) VALUES (?, 1, 'Source', 'https://example.com/source')");
        var option =
            connection.prepareStatement(
                "INSERT INTO quiz_option (question_id, option_id, display_order, option_text, is_correct) VALUES (?, ?, ?, ?, ?)")) {
      question.setString(1, id);
      question.setString(2, "Prompt " + id);
      question.setString(3, "Explanation " + id);
      question.executeUpdate();

      source.setString(1, id);
      source.executeUpdate();

      for (int index = 1; index <= 4; index++) {
        option.setString(1, id);
        option.setString(2, "option-" + index);
        option.setInt(3, index);
        option.setString(4, "Option " + index);
        option.setBoolean(5, index == 1);
        option.addBatch();
      }
      option.executeBatch();
    }
  }

  private static int countRows(String tableName) throws SQLException {
    return countWhere(tableName, "true");
  }

  private static int countWhere(String tableName, String whereClause) throws SQLException {
    try (var connection = dataSource.getConnection();
        var statement = connection.createStatement();
        var resultSet = statement.executeQuery("SELECT count(*) FROM " + tableName + " WHERE " + whereClause)) {
      resultSet.next();
      return resultSet.getInt(1);
    }
  }

  private static String queryString(String sql) throws SQLException {
    try (var connection = dataSource.getConnection();
        var statement = connection.createStatement();
        var resultSet = statement.executeQuery(sql)) {
      resultSet.next();
      return resultSet.getString(1);
    }
  }
}
