package com.onthisday.quiz;

import static com.onthisday.quiz.QuizChecks.requireSlug;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcQuizCollectionRepository implements QuizCollectionRepository {

  private static final Logger LOG = LoggerFactory.getLogger(JdbcQuizCollectionRepository.class);

  private static final String FIND_BY_ID_SQL =
      """
      SELECT collection_id, name, collection_group
      FROM quiz_collection
      WHERE collection_id = ?
      """;

  private static final String FIND_ALL_SQL =
      """
      SELECT collection_id, name, collection_group
      FROM quiz_collection
      ORDER BY collection_group, name, collection_id
      """;

  private final DataSource dataSource;

  public JdbcQuizCollectionRepository(DataSource dataSource) {
    this.dataSource = Objects.requireNonNull(dataSource, "dataSource must not be null");
  }

  @Override
  public Optional<QuizCollection> findById(String collectionId) {
    requireSlug(collectionId, "collectionId");
    var startedAt = System.nanoTime();
    LOG.info("db_query_start operation=findQuizCollectionById collectionId={}", collectionId);
    try (var connection = dataSource.getConnection();
        var statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
      statement.setString(1, collectionId);
      try (var resultSet = statement.executeQuery()) {
        var result = resultSet.next() ? Optional.of(mapCollection(resultSet)) : Optional.<QuizCollection>empty();
        LOG.info(
            "db_query_end operation=findQuizCollectionById collectionId={} found={} durationMs={}",
            collectionId,
            result.isPresent(),
            elapsedMillis(startedAt));
        return result;
      }
    } catch (SQLException | RuntimeException exception) {
      LOG.error("db_query_failed operation=findQuizCollectionById collectionId={}", collectionId, exception);
      throw new QuizUnavailableException("Could not load quiz collection: " + collectionId, exception);
    }
  }

  @Override
  public List<QuizCollection> findAll() {
    var startedAt = System.nanoTime();
    LOG.info("db_query_start operation=findAllQuizCollections");
    try (var connection = dataSource.getConnection();
        var statement = connection.prepareStatement(FIND_ALL_SQL);
        var resultSet = statement.executeQuery()) {
      var collections = new ArrayList<QuizCollection>();
      while (resultSet.next()) {
        collections.add(mapCollection(resultSet));
      }
      LOG.info(
          "db_query_end operation=findAllQuizCollections collectionCount={} durationMs={}",
          collections.size(),
          elapsedMillis(startedAt));
      return List.copyOf(collections);
    } catch (SQLException | RuntimeException exception) {
      LOG.error("db_query_failed operation=findAllQuizCollections", exception);
      throw new QuizUnavailableException("Could not load quiz collections.", exception);
    }
  }

  private static QuizCollection mapCollection(ResultSet resultSet) throws SQLException {
    return new QuizCollection(
        resultSet.getString("collection_id"),
        resultSet.getString("name"),
        CollectionGroup.fromValue(resultSet.getString("collection_group")));
  }

  private static long elapsedMillis(long startedAt) {
    return (System.nanoTime() - startedAt) / 1_000_000;
  }
}
