package com.onthisday.quiz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class JdbcDailyChallengeRepositoryIT {

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
    try (var connection = dataSource.getConnection(); var statement = connection.createStatement()) {
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
    insertQuestions(40);
  }

  @Test
  void insertsFindsAndDoesNotDuplicateACompletedAssignment() throws SQLException {
    var repository = new JdbcDailyChallengeRepository(dataSource);
    var date = LocalDate.of(2026, 9, 7);
    var challenge = challenge(date, questionIds(1, 20));

    assertTrue(repository.insertIfAbsent(challenge));
    assertEquals(challenge, repository.findByDate(date).orElseThrow());
    assertFalse(repository.insertIfAbsent(challenge(date, questionIds(21, 40))));
    assertEquals(challenge, repository.findByDate(date).orElseThrow());
    assertEquals(20, countRows("quiz_daily_question"));
  }

  @Test
  void rollsBackParentAndChildrenWhenCommitConstraintsFail() throws SQLException {
    var repository = new JdbcDailyChallengeRepository(dataSource);
    var ids = new ArrayList<>(questionIds(1, 19));
    ids.add("missing-question");
    var date = LocalDate.of(2026, 9, 8);

    var exception =
        assertThrows(
            QuizUnavailableException.class,
            () -> repository.insertIfAbsent(challenge(date, ids)));

    assertTrue(exception.getCause() instanceof SQLException);
    assertEquals(0, countWhere("quiz_daily_challenge", "challenge_date = '2026-09-08'"));
    assertEquals(0, countWhere("quiz_daily_question", "challenge_date = '2026-09-08'"));
  }

  @Test
  void rejectsMalformedCompletedStateAsUnavailable() throws SQLException {
    var date = LocalDate.of(2026, 9, 9);
    try (var connection = dataSource.getConnection(); var statement = connection.createStatement()) {
      statement.execute("ALTER TABLE quiz_daily_challenge DISABLE TRIGGER ALL");
      statement.execute("ALTER TABLE quiz_daily_question DISABLE TRIGGER ALL");
      statement.execute(
          "INSERT INTO quiz_daily_challenge (challenge_date, completed_at) VALUES ('2026-09-09', now())");
      statement.execute("ALTER TABLE quiz_daily_question ENABLE TRIGGER ALL");
      statement.execute("ALTER TABLE quiz_daily_challenge ENABLE TRIGGER ALL");
    }

    assertThrows(
        QuizUnavailableException.class,
        () -> new JdbcDailyChallengeRepository(dataSource).findByDate(date));
  }

  @Test
  void assignmentAndAggregateRemainLoadableAfterQuestionRetirement() throws SQLException {
    var date = LocalDate.of(2026, 9, 11);
    var challenge = challenge(date, questionIds(1, 20));
    var repository = new JdbcDailyChallengeRepository(dataSource);
    assertTrue(repository.insertIfAbsent(challenge));
    try (var connection = dataSource.getConnection(); var statement = connection.createStatement()) {
      statement.execute(
          "UPDATE quiz_question SET publication_state = 'retired' WHERE question_id = 'question-1'");
    }

    var persisted = repository.findByDate(date).orElseThrow();
    var loaded =
        new JdbcQuizQuestionRepository(dataSource)
            .findByIdsInOrder(List.of(persisted.questions().getFirst().questionId()));

    assertEquals(QuestionPublicationState.RETIRED, loaded.getFirst().publicationState());
  }

  @Test
  @Timeout(15)
  void concurrentCreatorsProduceOneWinnerWithoutMixedChildren() throws Exception {
    var date = LocalDate.of(2026, 9, 10);
    var first = challenge(date, questionIds(1, 20));
    var second = challenge(date, questionIds(21, 40));
    var ready = new CountDownLatch(2);
    var start = new CountDownLatch(1);

    try (var executor = Executors.newFixedThreadPool(2)) {
      var firstResult =
          executor.submit(() -> insertAfterBarrier(new JdbcDailyChallengeRepository(dataSource), first, ready, start));
      var secondResult =
          executor.submit(() -> insertAfterBarrier(new JdbcDailyChallengeRepository(dataSource), second, ready, start));
      ready.await();
      start.countDown();

      var firstWon = firstResult.get();
      var secondWon = secondResult.get();
      assertTrue(firstWon ^ secondWon);
      var persisted =
          new JdbcDailyChallengeRepository(dataSource).findByDate(date).orElseThrow();
      assertEquals(firstWon ? first : second, persisted);
      assertEquals(20, countWhere("quiz_daily_question", "challenge_date = '2026-09-10'"));
    }
  }

  private static boolean insertAfterBarrier(
      JdbcDailyChallengeRepository repository,
      DailyChallenge challenge,
      CountDownLatch ready,
      CountDownLatch start)
      throws InterruptedException {
    ready.countDown();
    start.await();
    return repository.insertIfAbsent(challenge);
  }

  private static DailyChallenge challenge(LocalDate date, List<String> ids) {
    var questions = new ArrayList<DailyChallengeQuestion>();
    for (var index = 0; index < ids.size(); index++) {
      questions.add(new DailyChallengeQuestion(index + 1, ids.get(index)));
    }
    return new DailyChallenge(date, questions);
  }

  private static List<String> questionIds(int first, int last) {
    var ids = new ArrayList<String>();
    for (var index = first; index <= last; index++) {
      ids.add("question-" + index);
    }
    return List.copyOf(ids);
  }

  private static void insertQuestions(int count) throws SQLException {
    try (var connection = dataSource.getConnection()) {
      connection.setAutoCommit(false);
      try (var question =
              connection.prepareStatement(
                  """
                  INSERT INTO quiz_question (
                    question_id, question_type, difficulty, publication_state, prompt, explanation
                  )
                  VALUES (?, 'multiple_choice', 'medium', 'published', ?, ?)
                  """);
          var source =
              connection.prepareStatement(
                  """
                  INSERT INTO quiz_source (question_id, display_order, display_name, url)
                  VALUES (?, 1, 'Source', 'https://example.com/source')
                  """);
          var option =
              connection.prepareStatement(
                  """
                  INSERT INTO quiz_option (
                    question_id, option_id, display_order, option_text, is_correct
                  )
                  VALUES (?, ?, ?, ?, ?)
                  """)) {
        for (var index = 1; index <= count; index++) {
          var id = "question-" + index;
          question.setString(1, id);
          question.setString(2, "Prompt " + id);
          question.setString(3, "Explanation " + id);
          question.executeUpdate();
          source.setString(1, id);
          source.executeUpdate();
          for (var optionIndex = 1; optionIndex <= 4; optionIndex++) {
            option.setString(1, id);
            option.setString(2, "option-" + optionIndex);
            option.setInt(3, optionIndex);
            option.setString(4, "Option " + optionIndex);
            option.setBoolean(5, optionIndex == 1);
            option.addBatch();
          }
          option.executeBatch();
        }
        connection.commit();
      } catch (SQLException | RuntimeException exception) {
        connection.rollback();
        throw exception;
      }
    }
  }

  private static int countRows(String table) throws SQLException {
    return countWhere(table, "true");
  }

  private static int countWhere(String table, String condition) throws SQLException {
    try (var connection = dataSource.getConnection();
        var statement = connection.createStatement();
        var resultSet =
            statement.executeQuery("SELECT count(*) FROM " + table + " WHERE " + condition)) {
      resultSet.next();
      return resultSet.getInt(1);
    }
  }
}
