package com.onthisday.quiz;

import static com.onthisday.quiz.QuizChecks.requireSlug;

import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcQuizQuestionRepository implements QuizQuestionRepository {

  private static final Logger LOG = LoggerFactory.getLogger(JdbcQuizQuestionRepository.class);
  private static final int MAX_QUESTION_IDS_PER_AGGREGATE_READ = 20;

  private static final String PUBLISHED_CANDIDATES_SQL =
      """
      SELECT question_id, question_type, difficulty
      FROM quiz_question
      WHERE publication_state = 'published'
      ORDER BY question_id
      """;

  private static final String PUBLISHED_COLLECTION_CANDIDATES_SQL =
      """
      SELECT question.question_id, question.question_type, question.difficulty
      FROM quiz_question question
      JOIN quiz_question_collection membership
        ON membership.question_id = question.question_id
      WHERE membership.collection_id = ?
        AND question.publication_state = 'published'
      ORDER BY question.question_id
      """;

  private final DataSource dataSource;

  public JdbcQuizQuestionRepository(DataSource dataSource) {
    this.dataSource = Objects.requireNonNull(dataSource, "dataSource must not be null");
  }

  @Override
  public List<QuizQuestionCandidate> findPublishedCandidates() {
    return findCandidates(PUBLISHED_CANDIDATES_SQL, null, "findPublishedQuizCandidates");
  }

  @Override
  public List<QuizQuestionCandidate> findPublishedCandidatesByCollectionId(String collectionId) {
    requireSlug(collectionId, "collectionId");
    return findCandidates(
        PUBLISHED_COLLECTION_CANDIDATES_SQL,
        collectionId,
        "findPublishedQuizCandidatesByCollection");
  }

  @Override
  public List<QuizQuestion> findByIdsInOrder(List<String> questionIds) {
    var validatedIds = validateQuestionIds(questionIds);
    if (validatedIds.isEmpty()) {
      return List.of();
    }

    var startedAt = System.nanoTime();
    LOG.info("db_query_start operation=findQuizQuestionsByIds questionCount={}", validatedIds.size());
    try (var connection = dataSource.getConnection()) {
      configureSnapshotRead(connection);
      try {
        var baseRows = findBaseRows(connection, validatedIds);
        requireAllRequestedIds(baseRows, validatedIds);
        var sources = findSources(connection, validatedIds);
        var options = findOptions(connection, validatedIds);
        var orderingItems = findOrderingItems(connection, validatedIds);
        var images = findImages(connection, validatedIds);
        var questions = mapQuestions(validatedIds, baseRows, sources, options, orderingItems, images);
        connection.commit();
        LOG.info(
            "db_query_end operation=findQuizQuestionsByIds questionCount={} durationMs={}",
            questions.size(),
            elapsedMillis(startedAt));
        return questions;
      } catch (SQLException | RuntimeException exception) {
        rollback(connection, exception);
        throw exception;
      }
    } catch (QuizUnavailableException exception) {
      throw exception;
    } catch (SQLException | RuntimeException exception) {
      LOG.error("db_query_failed operation=findQuizQuestionsByIds questionCount={}", validatedIds.size(), exception);
      throw new QuizUnavailableException("Could not load quiz questions.", exception);
    }
  }

  private List<QuizQuestionCandidate> findCandidates(
      String sql, String collectionId, String operation) {
    var startedAt = System.nanoTime();
    LOG.info("db_query_start operation={}{}", operation, collectionId == null ? "" : " collectionId=" + collectionId);
    try (var connection = dataSource.getConnection();
        var statement = connection.prepareStatement(sql)) {
      if (collectionId != null) {
        statement.setString(1, collectionId);
      }
      try (var resultSet = statement.executeQuery()) {
        var candidates = new ArrayList<QuizQuestionCandidate>();
        while (resultSet.next()) {
          candidates.add(
              new QuizQuestionCandidate(
                  resultSet.getString("question_id"),
                  QuestionType.fromValue(resultSet.getString("question_type")),
                  QuizDifficulty.fromValue(resultSet.getString("difficulty"))));
        }
        LOG.info(
            "db_query_end operation={} candidateCount={} durationMs={}",
            operation,
            candidates.size(),
            elapsedMillis(startedAt));
        return List.copyOf(candidates);
      }
    } catch (SQLException | RuntimeException exception) {
      LOG.error("db_query_failed operation={}", operation, exception);
      throw new QuizUnavailableException("Could not load published quiz candidates.", exception);
    }
  }

  private static List<String> validateQuestionIds(List<String> questionIds) {
    if (questionIds == null) {
      throw new InvalidQuizDefinitionException("questionIds must not be null");
    }
    if (questionIds.size() > MAX_QUESTION_IDS_PER_AGGREGATE_READ) {
      throw new InvalidQuizDefinitionException(
          "questionIds must not contain more than " + MAX_QUESTION_IDS_PER_AGGREGATE_READ + " IDs");
    }

    var ids = List.copyOf(questionIds);
    Set<String> uniqueIds = new HashSet<>();
    for (var questionId : ids) {
      requireSlug(questionId, "questionId");
      if (!uniqueIds.add(questionId)) {
        throw new InvalidQuizDefinitionException("questionIds must not contain duplicates");
      }
    }
    return ids;
  }

  private static void configureSnapshotRead(Connection connection) throws SQLException {
    connection.setReadOnly(true);
    connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
    connection.setAutoCommit(false);
  }

  private static Map<String, QuestionRow> findBaseRows(Connection connection, List<String> questionIds)
      throws SQLException {
    var sql =
        """
        SELECT question_id, question_type, difficulty, publication_state, prompt, explanation
        FROM quiz_question
        WHERE question_id IN (%s)
        """.formatted(placeholders(questionIds.size()));
    try (var statement = connection.prepareStatement(sql)) {
      bindQuestionIds(statement, questionIds);
      try (var resultSet = statement.executeQuery()) {
        var rows = new HashMap<String, QuestionRow>();
        while (resultSet.next()) {
          var id = resultSet.getString("question_id");
          rows.put(
              id,
              new QuestionRow(
                  id,
                  QuestionType.fromValue(resultSet.getString("question_type")),
                  QuizDifficulty.fromValue(resultSet.getString("difficulty")),
                  QuestionPublicationState.fromValue(resultSet.getString("publication_state")),
                  resultSet.getString("prompt"),
                  resultSet.getString("explanation")));
        }
        return rows;
      }
    }
  }

  private static Map<String, List<QuizSource>> findSources(Connection connection, List<String> questionIds)
      throws SQLException {
    var sql =
        """
        SELECT question_id, display_name, url
        FROM quiz_source
        WHERE question_id IN (%s)
        ORDER BY question_id, display_order
        """.formatted(placeholders(questionIds.size()));
    try (var statement = connection.prepareStatement(sql)) {
      bindQuestionIds(statement, questionIds);
      try (var resultSet = statement.executeQuery()) {
        var sources = new HashMap<String, List<QuizSource>>();
        while (resultSet.next()) {
          var questionId = resultSet.getString("question_id");
          try {
            sources
                .computeIfAbsent(questionId, ignored -> new ArrayList<>())
                .add(new QuizSource(resultSet.getString("display_name"), URI.create(resultSet.getString("url"))));
          } catch (RuntimeException exception) {
            throw new QuizUnavailableException("Could not map source for quiz question: " + questionId, exception);
          }
        }
        return sources;
      }
    }
  }

  private static Map<String, List<QuizOption>> findOptions(Connection connection, List<String> questionIds)
      throws SQLException {
    var sql =
        """
        SELECT question_id, option_id, option_text, display_order, is_correct
        FROM quiz_option
        WHERE question_id IN (%s)
        ORDER BY question_id, display_order
        """.formatted(placeholders(questionIds.size()));
    try (var statement = connection.prepareStatement(sql)) {
      bindQuestionIds(statement, questionIds);
      try (var resultSet = statement.executeQuery()) {
        var options = new HashMap<String, List<QuizOption>>();
        while (resultSet.next()) {
          var questionId = resultSet.getString("question_id");
          try {
            options
                .computeIfAbsent(questionId, ignored -> new ArrayList<>())
                .add(
                    new QuizOption(
                        resultSet.getString("option_id"),
                        resultSet.getString("option_text"),
                        resultSet.getInt("display_order"),
                        resultSet.getBoolean("is_correct")));
          } catch (RuntimeException exception) {
            throw new QuizUnavailableException("Could not map options for quiz question: " + questionId, exception);
          }
        }
        return options;
      }
    }
  }

  private static Map<String, List<ChronologicalOrderingItem>> findOrderingItems(
      Connection connection, List<String> questionIds) throws SQLException {
    var sql =
        """
        SELECT question_id, item_id, item_text, correct_position
        FROM quiz_ordering_item
        WHERE question_id IN (%s)
        ORDER BY question_id, correct_position
        """.formatted(placeholders(questionIds.size()));
    try (var statement = connection.prepareStatement(sql)) {
      bindQuestionIds(statement, questionIds);
      try (var resultSet = statement.executeQuery()) {
        var items = new HashMap<String, List<ChronologicalOrderingItem>>();
        while (resultSet.next()) {
          var questionId = resultSet.getString("question_id");
          try {
            items
                .computeIfAbsent(questionId, ignored -> new ArrayList<>())
                .add(
                    new ChronologicalOrderingItem(
                        resultSet.getString("item_id"),
                        resultSet.getString("item_text"),
                        resultSet.getInt("correct_position")));
          } catch (RuntimeException exception) {
            throw new QuizUnavailableException(
                "Could not map ordering items for quiz question: " + questionId, exception);
          }
        }
        return items;
      }
    }
  }

  private static Map<String, QuizImage> findImages(Connection connection, List<String> questionIds)
      throws SQLException {
    var sql =
        """
        SELECT question_id, url, alt_text, source_name, source_url, attribution, creator, license_name, license_url
        FROM quiz_image
        WHERE question_id IN (%s)
        """.formatted(placeholders(questionIds.size()));
    try (var statement = connection.prepareStatement(sql)) {
      bindQuestionIds(statement, questionIds);
      try (var resultSet = statement.executeQuery()) {
        var images = new HashMap<String, QuizImage>();
        while (resultSet.next()) {
          var questionId = resultSet.getString("question_id");
          try {
            images.put(
                questionId,
                new QuizImage(
                    URI.create(resultSet.getString("url")),
                    resultSet.getString("alt_text"),
                    resultSet.getString("source_name"),
                    URI.create(resultSet.getString("source_url")),
                    resultSet.getString("attribution"),
                    resultSet.getString("creator"),
                    resultSet.getString("license_name"),
                    URI.create(resultSet.getString("license_url"))));
          } catch (RuntimeException exception) {
            throw new QuizUnavailableException("Could not map image for quiz question: " + questionId, exception);
          }
        }
        return images;
      }
    }
  }

  private static List<QuizQuestion> mapQuestions(
      List<String> questionIds,
      Map<String, QuestionRow> baseRows,
      Map<String, List<QuizSource>> sources,
      Map<String, List<QuizOption>> options,
      Map<String, List<ChronologicalOrderingItem>> orderingItems,
      Map<String, QuizImage> images) {
    var questions = new ArrayList<QuizQuestion>(questionIds.size());
    for (var questionId : questionIds) {
      var row = baseRows.get(questionId);
      try {
        questions.add(
            switch (row.type()) {
              case MULTIPLE_CHOICE ->
                  new MultipleChoiceQuestion(
                      row.id(), row.difficulty(), row.publicationState(), row.prompt(), row.explanation(),
                      sources.getOrDefault(row.id(), List.of()), options.getOrDefault(row.id(), List.of()));
              case TRUE_FALSE ->
                  new TrueFalseQuestion(
                      row.id(), row.difficulty(), row.publicationState(), row.prompt(), row.explanation(),
                      sources.getOrDefault(row.id(), List.of()), options.getOrDefault(row.id(), List.of()));
              case IMAGE_IDENTIFICATION ->
                  new ImageIdentificationQuestion(
                      row.id(), row.difficulty(), row.publicationState(), row.prompt(), row.explanation(),
                      sources.getOrDefault(row.id(), List.of()), images.get(row.id()),
                      options.getOrDefault(row.id(), List.of()));
              case CHRONOLOGICAL_ORDERING ->
                  new ChronologicalOrderingQuestion(
                      row.id(), row.difficulty(), row.publicationState(), row.prompt(), row.explanation(),
                      sources.getOrDefault(row.id(), List.of()), orderingItems.getOrDefault(row.id(), List.of()));
            });
      } catch (RuntimeException exception) {
        throw new QuizUnavailableException("Could not map quiz question: " + questionId, exception);
      }
    }
    return List.copyOf(questions);
  }

  private static void requireAllRequestedIds(Map<String, QuestionRow> baseRows, List<String> questionIds) {
    var missingIds = questionIds.stream().filter(id -> !baseRows.containsKey(id)).toList();
    if (!missingIds.isEmpty()) {
      throw new QuizUnavailableException("Requested quiz questions were not found: " + String.join(", ", missingIds));
    }
  }

  private static String placeholders(int count) {
    return String.join(", ", java.util.Collections.nCopies(count, "?"));
  }

  private static void bindQuestionIds(PreparedStatement statement, List<String> questionIds) throws SQLException {
    for (var index = 0; index < questionIds.size(); index++) {
      statement.setString(index + 1, questionIds.get(index));
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

  private record QuestionRow(
      String id,
      QuestionType type,
      QuizDifficulty difficulty,
      QuestionPublicationState publicationState,
      String prompt,
      String explanation) {}
}
