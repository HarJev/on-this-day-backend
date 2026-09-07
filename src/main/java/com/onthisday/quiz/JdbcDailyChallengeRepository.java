package com.onthisday.quiz;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcDailyChallengeRepository implements DailyChallengeRepository {

  private static final Logger LOG = LoggerFactory.getLogger(JdbcDailyChallengeRepository.class);
  private static final String FIND_BY_DATE_SQL =
      """
      SELECT challenge.challenge_date,
             daily.position,
             daily.question_id
      FROM quiz_daily_challenge challenge
      LEFT JOIN quiz_daily_question daily
        ON daily.challenge_date = challenge.challenge_date
      WHERE challenge.challenge_date = ?
        AND challenge.completed_at IS NOT NULL
      ORDER BY daily.position
      """;
  private static final String INSERT_CHALLENGE_SQL =
      """
      INSERT INTO quiz_daily_challenge (challenge_date)
      VALUES (?)
      ON CONFLICT (challenge_date) DO NOTHING
      """;
  private static final String INSERT_QUESTION_SQL =
      """
      INSERT INTO quiz_daily_question (challenge_date, position, question_id)
      VALUES (?, ?, ?)
      """;
  private static final String COMPLETE_CHALLENGE_SQL =
      """
      UPDATE quiz_daily_challenge
      SET completed_at = now()
      WHERE challenge_date = ?
      """;

  private final DataSource dataSource;

  public JdbcDailyChallengeRepository(DataSource dataSource) {
    this.dataSource = Objects.requireNonNull(dataSource, "dataSource must not be null");
  }

  @Override
  public Optional<DailyChallenge> findByDate(LocalDate date) {
    Objects.requireNonNull(date, "date must not be null");
    var startedAt = System.nanoTime();
    LOG.info("db_query_start operation=findDailyChallenge date={}", date);
    try (var connection = dataSource.getConnection();
        var statement = connection.prepareStatement(FIND_BY_DATE_SQL)) {
      statement.setObject(1, date);
      try (var resultSet = statement.executeQuery()) {
        var questions = new ArrayList<DailyChallengeQuestion>(20);
        var challengeFound = false;
        while (resultSet.next()) {
          challengeFound = true;
          var position = resultSet.getObject("position", Integer.class);
          if (position != null) {
            questions.add(
                new DailyChallengeQuestion(position, resultSet.getString("question_id")));
          }
        }
        if (!challengeFound) {
          LOG.info(
              "db_query_end operation=findDailyChallenge found=false durationMs={}",
              elapsedMillis(startedAt));
          return Optional.empty();
        }
        try {
          var challenge = new DailyChallenge(date, questions);
          LOG.info(
              "db_query_end operation=findDailyChallenge found=true durationMs={}",
              elapsedMillis(startedAt));
          return Optional.of(challenge);
        } catch (InvalidQuizDefinitionException exception) {
          throw new QuizUnavailableException(
              "Stored Daily Challenge is incomplete or malformed for date: " + date, exception);
        }
      }
    } catch (QuizUnavailableException exception) {
      throw exception;
    } catch (SQLException | RuntimeException exception) {
      LOG.error("db_query_failed operation=findDailyChallenge date={}", date, exception);
      throw new QuizUnavailableException("Could not load Daily Challenge for date: " + date, exception);
    }
  }

  @Override
  public boolean insertIfAbsent(DailyChallenge challenge) {
    Objects.requireNonNull(challenge, "challenge must not be null");
    var startedAt = System.nanoTime();
    LOG.info("db_query_start operation=insertDailyChallenge date={}", challenge.date());
    try (var connection = dataSource.getConnection()) {
      connection.setAutoCommit(false);
      try {
        if (!insertParent(connection, challenge.date())) {
          connection.commit();
          LOG.info(
              "db_query_end operation=insertDailyChallenge inserted=false durationMs={}",
              elapsedMillis(startedAt));
          return false;
        }
        insertQuestions(connection, challenge);
        completeChallenge(connection, challenge.date());
        connection.commit();
        LOG.info(
            "db_query_end operation=insertDailyChallenge inserted=true durationMs={}",
            elapsedMillis(startedAt));
        return true;
      } catch (SQLException | RuntimeException exception) {
        rollback(connection, exception);
        throw exception;
      }
    } catch (QuizUnavailableException exception) {
      throw exception;
    } catch (SQLException | RuntimeException exception) {
      LOG.error(
          "db_query_failed operation=insertDailyChallenge date={}", challenge.date(), exception);
      throw new QuizUnavailableException(
          "Could not create Daily Challenge for date: " + challenge.date(), exception);
    }
  }

  private static boolean insertParent(java.sql.Connection connection, LocalDate date)
      throws SQLException {
    try (var statement = connection.prepareStatement(INSERT_CHALLENGE_SQL)) {
      statement.setObject(1, date);
      return statement.executeUpdate() == 1;
    }
  }

  private static void insertQuestions(java.sql.Connection connection, DailyChallenge challenge)
      throws SQLException {
    try (var statement = connection.prepareStatement(INSERT_QUESTION_SQL)) {
      for (var question : challenge.questions()) {
        statement.setObject(1, challenge.date());
        statement.setInt(2, question.position());
        statement.setString(3, question.questionId());
        statement.addBatch();
      }
      statement.executeBatch();
    }
  }

  private static void completeChallenge(java.sql.Connection connection, LocalDate date)
      throws SQLException {
    try (var statement = connection.prepareStatement(COMPLETE_CHALLENGE_SQL)) {
      statement.setObject(1, date);
      if (statement.executeUpdate() != 1) {
        throw new SQLException("Daily Challenge disappeared before completion");
      }
    }
  }

  private static void rollback(java.sql.Connection connection, Throwable originalFailure) {
    try {
      connection.rollback();
    } catch (SQLException rollbackFailure) {
      originalFailure.addSuppressed(rollbackFailure);
    }
  }

  private static long elapsedMillis(long startedAt) {
    return (System.nanoTime() - startedAt) / 1_000_000;
  }
}
