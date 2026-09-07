package com.onthisday.quiz;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcQuizCatalogRepository implements QuizCatalogRepository {

  private static final Logger LOG = LoggerFactory.getLogger(JdbcQuizCatalogRepository.class);

  private static final String MIXED_COUNT_SQL =
      """
      SELECT count(*)
      FROM quiz_question
      WHERE publication_state = 'published'
      """;

  private static final String COLLECTION_COUNTS_SQL =
      """
      SELECT
        collection.collection_id,
        collection.name,
        collection.collection_group,
        count(DISTINCT question.question_id) AS published_question_count
      FROM quiz_collection collection
      LEFT JOIN quiz_question_collection membership
        ON membership.collection_id = collection.collection_id
      LEFT JOIN quiz_question question
        ON question.question_id = membership.question_id
       AND question.publication_state = 'published'
      GROUP BY collection.collection_id, collection.name, collection.collection_group
      ORDER BY collection.collection_group, collection.name, collection.collection_id
      """;

  private final DataSource dataSource;

  public JdbcQuizCatalogRepository(DataSource dataSource) {
    this.dataSource = Objects.requireNonNull(dataSource, "dataSource must not be null");
  }

  @Override
  public QuizCatalogCounts loadPublishedCounts() {
    var startedAt = System.nanoTime();
    LOG.info("db_query_start operation=loadQuizCatalogCounts");
    try (var connection = dataSource.getConnection()) {
      configureSnapshotRead(connection);
      try {
        var mixedCount = findMixedCount(connection);
        var collectionCounts = findCollectionCounts(connection);
        connection.commit();
        LOG.info(
            "db_query_end operation=loadQuizCatalogCounts mixedPublishedQuestionCount={} collectionCount={} durationMs={}",
            mixedCount,
            collectionCounts.size(),
            elapsedMillis(startedAt));
        return new QuizCatalogCounts(mixedCount, collectionCounts);
      } catch (SQLException | RuntimeException exception) {
        rollback(connection, exception);
        throw exception;
      }
    } catch (QuizUnavailableException exception) {
      throw exception;
    } catch (SQLException | RuntimeException exception) {
      LOG.error("db_query_failed operation=loadQuizCatalogCounts", exception);
      throw new QuizUnavailableException("Could not load quiz catalog counts.", exception);
    }
  }

  private static void configureSnapshotRead(Connection connection) throws SQLException {
    connection.setReadOnly(true);
    connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
    connection.setAutoCommit(false);
  }

  private static int findMixedCount(Connection connection) throws SQLException {
    try (var statement = connection.prepareStatement(MIXED_COUNT_SQL);
        var resultSet = statement.executeQuery()) {
      resultSet.next();
      return resultSet.getInt(1);
    }
  }

  private static List<QuizCollectionPublishedCount> findCollectionCounts(Connection connection)
      throws SQLException {
    try (var statement = connection.prepareStatement(COLLECTION_COUNTS_SQL);
        var resultSet = statement.executeQuery()) {
      var counts = new ArrayList<QuizCollectionPublishedCount>();
      while (resultSet.next()) {
        var collection =
            new QuizCollection(
                resultSet.getString("collection_id"),
                resultSet.getString("name"),
                CollectionGroup.fromValue(resultSet.getString("collection_group")));
        counts.add(new QuizCollectionPublishedCount(collection, resultSet.getInt("published_question_count")));
      }
      return List.copyOf(counts);
    }
  }

  private static void rollback(Connection connection, Throwable originalFailure) {
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
