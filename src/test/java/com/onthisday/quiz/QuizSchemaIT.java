package com.onthisday.quiz;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class QuizSchemaIT {

  @Container
  static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("on_this_day")
          .withUsername("on_this_day")
          .withPassword("on_this_day");

  @BeforeAll
  static void migrateDatabase() {
    Flyway.configure()
        .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
        .locations("classpath:db/migration")
        .load()
        .migrate();
  }

  @Test
  void acceptsAllFourCompleteQuestionShapesAndCollectionMembership() {
    assertDoesNotThrow(
        () ->
            inTransaction(
                connection -> {
                  insertChoiceQuestion(connection, "valid-multiple-choice", "multiple_choice", "draft");
                  insertTrueFalseQuestion(connection, "valid-true-false", "published");
                  insertImageQuestion(connection, "valid-image-identification", "published");
                  insertOrderingQuestion(connection, "valid-chronological-ordering", "retired");
                  insertCollection(connection, "ancient-history", "Ancient History", "historical_period");
                  insertCollectionMembership(
                      connection, "valid-multiple-choice", "ancient-history");
                }));
  }

  @Test
  void rejectsInvalidQuestionAndCollectionValues() {
    assertThrows(
        SQLException.class,
        () ->
            inTransaction(
                connection -> {
                  insertQuestion(connection, "Not A Slug", "multiple_choice", "easy", "draft");
                }));
    assertThrows(
        SQLException.class,
        () ->
            inTransaction(
                connection -> {
                  insertQuestion(connection, "unsupported-type", "essay", "easy", "draft");
                }));
    assertThrows(
        SQLException.class,
        () ->
            inTransaction(
                connection ->
                    insertCollection(connection, "bad-group", "Bad group", "hierarchy")));
  }

  @Test
  void requiresSourceForEveryPublicationState() {
    assertThrows(
        SQLException.class,
        () ->
            inTransaction(
                connection -> {
                  insertQuestion(connection, "draft-without-source", "multiple_choice", "easy", "draft");
                  insertFourOptions(connection, "draft-without-source");
                }));
  }

  @Test
  void rejectsSourceMoveThatLeavesOldQuestionWithoutSource() {
    assertThrows(
        SQLException.class,
        () ->
            inTransaction(
                connection -> {
                  insertChoiceQuestion(connection, "source-old-owner", "multiple_choice", "draft");
                  insertChoiceQuestion(connection, "source-new-owner", "multiple_choice", "draft");
                  try (var statement =
                      connection.prepareStatement(
                          "UPDATE quiz_source SET question_id = ?, display_order = 2 "
                              + "WHERE question_id = ?")) {
                    statement.setString(1, "source-new-owner");
                    statement.setString(2, "source-old-owner");
                    statement.executeUpdate();
                  }
                }));
  }

  @Test
  void allowsTransactionalSourceReplacementAndQuestionIdUpdate() {
    assertDoesNotThrow(
        () ->
            inTransaction(
                connection -> {
                  insertChoiceQuestion(connection, "replace-source", "multiple_choice", "draft");
                  execute(connection, "DELETE FROM quiz_source WHERE question_id = 'replace-source'");
                  insertSource(connection, "replace-source", 1);
                  execute(
                      connection,
                      "UPDATE quiz_question SET question_id = 'renamed-source' "
                          + "WHERE question_id = 'replace-source'");
                }));
  }

  @Test
  void allowsDeletingUnassignedQuestionWithCascadedChildren() {
    assertDoesNotThrow(
        () ->
            inTransaction(
                connection -> {
                  insertImageQuestion(connection, "delete-unassigned-question", "draft");
                  execute(
                      connection,
                      "DELETE FROM quiz_question WHERE question_id = 'delete-unassigned-question'");
                }));
  }

  @Test
  void rejectsInvalidChoiceAndOrderingShapes() {
    assertThrows(
        SQLException.class,
        () ->
            inTransaction(
                connection -> {
                  insertQuestion(connection, "three-option-question", "multiple_choice", "easy", "draft");
                  insertSource(connection, "three-option-question", 1);
                  insertOptions(connection, "three-option-question", 3, 1);
                }));
    assertThrows(
        SQLException.class,
        () ->
            inTransaction(
                connection -> {
                  insertQuestion(connection, "two-correct-options", "multiple_choice", "easy", "draft");
                  insertSource(connection, "two-correct-options", 1);
                  insertOptions(connection, "two-correct-options", 4, 2);
                }));
    assertThrows(
        SQLException.class,
        () ->
            inTransaction(
                connection -> {
                  insertQuestion(
                      connection,
                      "three-ordering-items",
                      "chronological_ordering",
                      "hard",
                      "draft");
                  insertSource(connection, "three-ordering-items", 1);
                  insertOrderingItems(connection, "three-ordering-items", 3);
                }));
  }

  @Test
  void rejectsMalformedTrueFalseOptions() {
    assertThrows(
        SQLException.class,
        () ->
            inTransaction(
                connection -> {
                  insertQuestion(connection, "yes-no-question", "true_false", "easy", "draft");
                  insertSource(connection, "yes-no-question", 1);
                  insertOption(connection, "yes-no-question", "yes", "Yes", 1, true);
                  insertOption(connection, "yes-no-question", "no", "No", 2, false);
                }));
  }

  @Test
  void enforcesImageShapeAndCompleteProvenance() {
    assertThrows(
        SQLException.class,
        () ->
            inTransaction(
                connection -> {
                  insertQuestion(
                      connection,
                      "image-question-without-image",
                      "image_identification",
                      "medium",
                      "draft");
                  insertSource(connection, "image-question-without-image", 1);
                  insertFourOptions(connection, "image-question-without-image");
                }));
    assertThrows(
        SQLException.class,
        () ->
            inTransaction(
                connection -> {
                  insertChoiceQuestion(connection, "non-image-with-image", "multiple_choice", "draft");
                  insertImage(connection, "non-image-with-image");
                }));
    assertThrows(
        SQLException.class,
        () ->
            inTransaction(
                connection -> {
                  insertQuestion(
                      connection,
                      "incomplete-image-question",
                      "image_identification",
                      "medium",
                      "draft");
                  insertSource(connection, "incomplete-image-question", 1);
                  insertFourOptions(connection, "incomplete-image-question");
                  try (var statement =
                      connection.prepareStatement(
                          """
                          INSERT INTO quiz_image (
                            question_id, url, alt_text, source_name, source_url,
                            attribution, creator, license_name, license_url
                          )
                          VALUES (?, 'https://example.com/image.jpg', 'Alt text', 'Archive',
                            'https://example.com/source', ' ', NULL, 'Public domain',
                            'https://example.com/license')
                          """)) {
                    statement.setString(1, "incomplete-image-question");
                    statement.executeUpdate();
                  }
                }));
  }

  @Test
  void permitsTransactionalTypeChangeOnlyWhenFinalShapeIsValid() {
    assertDoesNotThrow(
        () ->
            inTransaction(
                connection -> {
                  insertChoiceQuestion(connection, "change-valid-type", "multiple_choice", "draft");
                  execute(connection, "DELETE FROM quiz_option WHERE question_id = 'change-valid-type'");
                  insertOrderingItems(connection, "change-valid-type", 4);
                  execute(
                      connection,
                      "UPDATE quiz_question SET question_type = 'chronological_ordering' "
                          + "WHERE question_id = 'change-valid-type'");
                }));

    assertThrows(
        SQLException.class,
        () ->
            inTransaction(
                connection -> {
                  insertChoiceQuestion(connection, "change-invalid-type", "multiple_choice", "draft");
                  execute(
                      connection,
                      "UPDATE quiz_question SET question_type = 'chronological_ordering' "
                          + "WHERE question_id = 'change-invalid-type'");
                }));
  }

  @Test
  void requiresExactlyTwentyDistinctPublishedDailyQuestions() throws SQLException {
    var validDate = LocalDate.of(2026, 9, 1);
    insertPublishedQuestions("daily-valid", 20);
    assertDoesNotThrow(
        () ->
            inTransaction(
                connection -> createCompleteChallenge(connection, validDate, "daily-valid", 20)));

    insertPublishedQuestions("daily-nineteen", 19);
    assertThrows(
        SQLException.class,
        () ->
            inTransaction(
                connection ->
                    createCompleteChallenge(
                        connection, LocalDate.of(2026, 9, 2), "daily-nineteen", 19)));

    insertPublishedQuestions("daily-with-draft", 19);
    inTransaction(
        connection -> insertChoiceQuestion(connection, "daily-with-draft-20", "multiple_choice", "draft"));
    assertThrows(
        SQLException.class,
        () ->
            inTransaction(
                connection ->
                    createCompleteChallenge(
                        connection, LocalDate.of(2026, 9, 3), "daily-with-draft", 20)));
  }

  @Test
  void rejectsDuplicateQuestionDuplicatePositionAndOutOfRangePosition() throws SQLException {
    var challengeDate = LocalDate.of(2026, 9, 7);
    insertPublishedQuestions("daily-key-constraint", 2);

    assertThrows(
        SQLException.class,
        () ->
            inTransaction(
                connection -> {
                  insertChallenge(connection, challengeDate);
                  insertDailyQuestion(connection, challengeDate, 1, "daily-key-constraint-1");
                  insertDailyQuestion(connection, challengeDate, 2, "daily-key-constraint-1");
                }));
    assertThrows(
        SQLException.class,
        () ->
            inTransaction(
                connection -> {
                  insertChallenge(connection, challengeDate);
                  insertDailyQuestion(connection, challengeDate, 1, "daily-key-constraint-1");
                  insertDailyQuestion(connection, challengeDate, 1, "daily-key-constraint-2");
                }));
    assertThrows(
        SQLException.class,
        () ->
            inTransaction(
                connection -> {
                  insertChallenge(connection, challengeDate);
                  insertDailyQuestion(connection, challengeDate, 21, "daily-key-constraint-1");
                }));
  }

  @Test
  void completedDailyChallengeAndMembershipAreImmutable() throws SQLException {
    var challengeDate = LocalDate.of(2026, 9, 4);
    insertPublishedQuestions("immutable-daily", 21);
    inTransaction(
        connection -> createCompleteChallenge(connection, challengeDate, "immutable-daily", 20));

    assertThrows(
        SQLException.class,
        () ->
            inTransaction(
                connection ->
                    execute(
                        connection,
                        "UPDATE quiz_daily_question SET question_id = 'immutable-daily-21' "
                            + "WHERE challenge_date = DATE '2026-09-04' AND position = 1")));
    assertThrows(
        SQLException.class,
        () ->
            inTransaction(
                connection ->
                    execute(
                        connection,
                        "DELETE FROM quiz_daily_question "
                            + "WHERE challenge_date = DATE '2026-09-04' AND position = 20")));
    assertThrows(
        SQLException.class,
        () ->
            inTransaction(
                connection ->
                    execute(
                        connection,
                        "INSERT INTO quiz_daily_question (challenge_date, position, question_id) "
                            + "VALUES (DATE '2026-09-04', 20, 'immutable-daily-21')")));
    assertThrows(
        SQLException.class,
        () ->
            inTransaction(
                connection ->
                    execute(
                        connection,
                        "UPDATE quiz_daily_challenge SET completed_at = NULL "
                            + "WHERE challenge_date = DATE '2026-09-04'")));
    assertThrows(
        SQLException.class,
        () ->
            inTransaction(
                connection ->
                    execute(
                        connection,
                        "DELETE FROM quiz_daily_challenge "
                            + "WHERE challenge_date = DATE '2026-09-04'")));
  }

  @Test
  void assignedQuestionCanBeCorrectedRetiredAndRepublishedButNotReshaped() throws SQLException {
    var challengeDate = LocalDate.of(2026, 9, 5);
    insertPublishedQuestions("assigned-question", 20);
    inTransaction(
        connection -> createCompleteChallenge(connection, challengeDate, "assigned-question", 20));

    assertDoesNotThrow(
        () ->
            inTransaction(
                connection ->
                    execute(
                        connection,
                        "UPDATE quiz_question SET publication_state = 'retired' "
                            + "WHERE question_id = 'assigned-question-1'")));
    assertDoesNotThrow(
        () ->
            inTransaction(
                connection -> {
                  execute(
                      connection,
                      "UPDATE quiz_question SET prompt = 'A corrected prompt', "
                          + "explanation = 'A corrected explanation' "
                          + "WHERE question_id = 'assigned-question-1'");
                  execute(
                      connection,
                      "UPDATE quiz_source SET display_name = 'Corrected source' "
                          + "WHERE question_id = 'assigned-question-1'");
                  execute(
                      connection,
                      "UPDATE quiz_option SET option_text = 'Corrected option' "
                          + "WHERE question_id = 'assigned-question-1' AND option_id = 'option-2'");
                  execute(
                      connection,
                      "UPDATE quiz_question SET publication_state = 'published' "
                          + "WHERE question_id = 'assigned-question-1'");
                }));
    assertThrows(
        SQLException.class,
        () ->
            inTransaction(
                connection ->
                    execute(
                        connection,
                        "UPDATE quiz_question SET question_type = 'true_false' "
                            + "WHERE question_id = 'assigned-question-1'")));
    assertThrows(
        SQLException.class,
        () ->
            inTransaction(
                connection ->
                    execute(
                        connection,
                        "UPDATE quiz_question SET question_id = 'renamed-assigned-question' "
                            + "WHERE question_id = 'assigned-question-1'")));
    assertThrows(
        SQLException.class,
        () ->
            inTransaction(
                connection ->
                    execute(
                        connection,
                        "DELETE FROM quiz_question WHERE question_id = 'assigned-question-1'")));
  }

  @Test
  @Timeout(20)
  void concurrentChallengeDateInsertionCreatesOneCompleteAssignment() throws Exception {
    var challengeDate = LocalDate.of(2026, 9, 6);
    insertPublishedQuestions("concurrent-daily", 20);
    var start = new CountDownLatch(1);

    try (var executor = Executors.newFixedThreadPool(2)) {
      var first =
          executor.submit(
              () -> insertChallengeIfAbsent(challengeDate, "concurrent-daily", start));
      var second =
          executor.submit(
              () -> insertChallengeIfAbsent(challengeDate, "concurrent-daily", start));
      start.countDown();

      var results = List.of(first.get(), second.get());
      assertEquals(1, results.stream().filter(Boolean::booleanValue).count());
      assertTrue(results.contains(true));
      assertTrue(results.contains(false));
    }

    try (var connection = openConnection()) {
      assertEquals(
          1,
          queryCount(
              connection,
              "SELECT count(*) FROM quiz_daily_challenge "
                  + "WHERE challenge_date = DATE '2026-09-06' AND completed_at IS NOT NULL"));
      assertEquals(
          20,
          queryCount(
              connection,
              "SELECT count(*) FROM quiz_daily_question "
                  + "WHERE challenge_date = DATE '2026-09-06'"));
    }
  }

  private static boolean insertChallengeIfAbsent(
      LocalDate challengeDate, String questionPrefix, CountDownLatch start) throws Exception {
    try (var connection = openConnection()) {
      connection.setAutoCommit(false);
      try {
        start.await();
        int inserted;
        try (var statement =
            connection.prepareStatement(
                """
                INSERT INTO quiz_daily_challenge (challenge_date)
                VALUES (?)
                ON CONFLICT (challenge_date) DO NOTHING
                """)) {
          statement.setObject(1, challengeDate);
          inserted = statement.executeUpdate();
        }

        if (inserted == 0) {
          connection.commit();
          return false;
        }

        insertDailyQuestions(connection, challengeDate, questionPrefix, 20);
        completeChallenge(connection, challengeDate);
        connection.commit();
        return true;
      } catch (Exception exception) {
        connection.rollback();
        throw exception;
      }
    }
  }

  private static void createCompleteChallenge(
      Connection connection, LocalDate challengeDate, String questionPrefix, int questionCount)
      throws SQLException {
    insertChallenge(connection, challengeDate);
    insertDailyQuestions(connection, challengeDate, questionPrefix, questionCount);
    completeChallenge(connection, challengeDate);
  }

  private static void insertChallenge(Connection connection, LocalDate challengeDate)
      throws SQLException {
    try (var statement =
        connection.prepareStatement(
            "INSERT INTO quiz_daily_challenge (challenge_date) VALUES (?)")) {
      statement.setObject(1, challengeDate);
      statement.executeUpdate();
    }
  }

  private static void insertDailyQuestions(
      Connection connection, LocalDate challengeDate, String questionPrefix, int questionCount)
      throws SQLException {
    try (var statement =
        connection.prepareStatement(
            """
            INSERT INTO quiz_daily_question (challenge_date, position, question_id)
            VALUES (?, ?, ?)
            """)) {
      for (var position = 1; position <= questionCount; position++) {
        statement.setObject(1, challengeDate);
        statement.setInt(2, position);
        statement.setString(3, questionPrefix + "-" + position);
        statement.addBatch();
      }
      statement.executeBatch();
    }
  }

  private static void insertDailyQuestion(
      Connection connection, LocalDate challengeDate, int position, String questionId)
      throws SQLException {
    try (var statement =
        connection.prepareStatement(
            """
            INSERT INTO quiz_daily_question (challenge_date, position, question_id)
            VALUES (?, ?, ?)
            """)) {
      statement.setObject(1, challengeDate);
      statement.setInt(2, position);
      statement.setString(3, questionId);
      statement.executeUpdate();
    }
  }

  private static void completeChallenge(Connection connection, LocalDate challengeDate)
      throws SQLException {
    try (var statement =
        connection.prepareStatement(
            "UPDATE quiz_daily_challenge SET completed_at = now() WHERE challenge_date = ?")) {
      statement.setObject(1, challengeDate);
      statement.executeUpdate();
    }
  }

  private static void insertPublishedQuestions(String prefix, int count) throws SQLException {
    inTransaction(
        connection -> {
          for (var index = 1; index <= count; index++) {
            insertChoiceQuestion(
                connection, prefix + "-" + index, "multiple_choice", "published");
          }
        });
  }

  private static void insertChoiceQuestion(
      Connection connection, String questionId, String questionType, String publicationState)
      throws SQLException {
    insertQuestion(connection, questionId, questionType, "easy", publicationState);
    insertSource(connection, questionId, 1);
    insertFourOptions(connection, questionId);
  }

  private static void insertTrueFalseQuestion(
      Connection connection, String questionId, String publicationState) throws SQLException {
    insertQuestion(connection, questionId, "true_false", "medium", publicationState);
    insertSource(connection, questionId, 1);
    insertOption(connection, questionId, "true", "True", 1, true);
    insertOption(connection, questionId, "false", "False", 2, false);
  }

  private static void insertImageQuestion(
      Connection connection, String questionId, String publicationState) throws SQLException {
    insertQuestion(connection, questionId, "image_identification", "medium", publicationState);
    insertSource(connection, questionId, 1);
    insertFourOptions(connection, questionId);
    insertImage(connection, questionId);
  }

  private static void insertOrderingQuestion(
      Connection connection, String questionId, String publicationState) throws SQLException {
    insertQuestion(connection, questionId, "chronological_ordering", "hard", publicationState);
    insertSource(connection, questionId, 1);
    insertOrderingItems(connection, questionId, 4);
  }

  private static void insertQuestion(
      Connection connection,
      String questionId,
      String questionType,
      String difficulty,
      String publicationState)
      throws SQLException {
    try (var statement =
        connection.prepareStatement(
            """
            INSERT INTO quiz_question (
              question_id, question_type, difficulty, publication_state, prompt, explanation
            )
            VALUES (?, ?, ?, ?, 'A test prompt', 'A test explanation')
            """)) {
      statement.setString(1, questionId);
      statement.setString(2, questionType);
      statement.setString(3, difficulty);
      statement.setString(4, publicationState);
      statement.executeUpdate();
    }
  }

  private static void insertSource(Connection connection, String questionId, int displayOrder)
      throws SQLException {
    try (var statement =
        connection.prepareStatement(
            """
            INSERT INTO quiz_source (question_id, display_order, display_name, url)
            VALUES (?, ?, 'Encyclopaedia Britannica', 'https://www.britannica.com/')
            """)) {
      statement.setString(1, questionId);
      statement.setInt(2, displayOrder);
      statement.executeUpdate();
    }
  }

  private static void insertFourOptions(Connection connection, String questionId)
      throws SQLException {
    insertOptions(connection, questionId, 4, 1);
  }

  private static void insertOptions(
      Connection connection, String questionId, int count, int correctCount) throws SQLException {
    for (var position = 1; position <= count; position++) {
      insertOption(
          connection,
          questionId,
          "option-" + position,
          "Option " + position,
          position,
          position <= correctCount);
    }
  }

  private static void insertOption(
      Connection connection,
      String questionId,
      String optionId,
      String optionText,
      int displayOrder,
      boolean correct)
      throws SQLException {
    try (var statement =
        connection.prepareStatement(
            """
            INSERT INTO quiz_option (
              question_id, option_id, display_order, option_text, is_correct
            )
            VALUES (?, ?, ?, ?, ?)
            """)) {
      statement.setString(1, questionId);
      statement.setString(2, optionId);
      statement.setInt(3, displayOrder);
      statement.setString(4, optionText);
      statement.setBoolean(5, correct);
      statement.executeUpdate();
    }
  }

  private static void insertOrderingItems(Connection connection, String questionId, int count)
      throws SQLException {
    try (var statement =
        connection.prepareStatement(
            """
            INSERT INTO quiz_ordering_item (
              question_id, item_id, item_text, correct_position
            )
            VALUES (?, ?, ?, ?)
            """)) {
      for (var position = 1; position <= count; position++) {
        statement.setString(1, questionId);
        statement.setString(2, "item-" + position);
        statement.setString(3, "Item " + position);
        statement.setInt(4, position);
        statement.addBatch();
      }
      statement.executeBatch();
    }
  }

  private static void insertImage(Connection connection, String questionId) throws SQLException {
    try (var statement =
        connection.prepareStatement(
            """
            INSERT INTO quiz_image (
              question_id, url, alt_text, source_name, source_url,
              attribution, creator, license_name, license_url
            )
            VALUES (?, 'https://example.com/image.jpg', 'A historical image', 'Example Archive',
              'https://example.com/source', 'Example Archive', NULL, 'Public domain',
              'https://example.com/license')
            """)) {
      statement.setString(1, questionId);
      statement.executeUpdate();
    }
  }

  private static void insertCollection(
      Connection connection, String collectionId, String name, String collectionGroup)
      throws SQLException {
    try (var statement =
        connection.prepareStatement(
            """
            INSERT INTO quiz_collection (collection_id, name, collection_group)
            VALUES (?, ?, ?)
            """)) {
      statement.setString(1, collectionId);
      statement.setString(2, name);
      statement.setString(3, collectionGroup);
      statement.executeUpdate();
    }
  }

  private static void insertCollectionMembership(
      Connection connection, String questionId, String collectionId) throws SQLException {
    try (var statement =
        connection.prepareStatement(
            """
            INSERT INTO quiz_question_collection (question_id, collection_id)
            VALUES (?, ?)
            """)) {
      statement.setString(1, questionId);
      statement.setString(2, collectionId);
      statement.executeUpdate();
    }
  }

  private static void execute(Connection connection, String sql) throws SQLException {
    try (var statement = connection.createStatement()) {
      statement.executeUpdate(sql);
    }
  }

  private static int queryCount(Connection connection, String sql) throws SQLException {
    try (var statement = connection.createStatement(); var resultSet = statement.executeQuery(sql)) {
      resultSet.next();
      return resultSet.getInt(1);
    }
  }

  private static Connection openConnection() throws SQLException {
    return DriverManager.getConnection(
        POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
  }

  private static void inTransaction(SqlWork work) throws SQLException {
    try (var connection = openConnection()) {
      connection.setAutoCommit(false);
      try {
        work.run(connection);
        connection.commit();
      } catch (SQLException | RuntimeException exception) {
        connection.rollback();
        throw exception;
      }
    }
  }

  @FunctionalInterface
  private interface SqlWork {
    void run(Connection connection) throws SQLException;
  }
}
