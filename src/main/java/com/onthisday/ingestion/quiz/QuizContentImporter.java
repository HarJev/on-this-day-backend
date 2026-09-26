package com.onthisday.ingestion.quiz;

import com.onthisday.ingestion.ContentValidationResult;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import javax.sql.DataSource;

public class QuizContentImporter {

  private static final String UPSERT_COLLECTION_SQL =
      """
      INSERT INTO quiz_collection (collection_id, name, collection_group)
      VALUES (?, ?, ?)
      ON CONFLICT (collection_id) DO UPDATE SET
        name = EXCLUDED.name,
        collection_group = EXCLUDED.collection_group,
        updated_at = now()
      """;

  private static final String UPSERT_QUESTION_SQL =
      """
      INSERT INTO quiz_question (
        question_id,
        question_type,
        difficulty,
        publication_state,
        prompt,
        explanation
      )
      VALUES (?, ?, ?, ?, ?, ?)
      ON CONFLICT (question_id) DO UPDATE SET
        question_type = EXCLUDED.question_type,
        difficulty = EXCLUDED.difficulty,
        publication_state = EXCLUDED.publication_state,
        prompt = EXCLUDED.prompt,
        explanation = EXCLUDED.explanation,
        updated_at = now()
      """;

  private static final String DELETE_SOURCES_SQL = "DELETE FROM quiz_source WHERE question_id = ?";
  private static final String DELETE_OPTIONS_SQL = "DELETE FROM quiz_option WHERE question_id = ?";
  private static final String DELETE_ORDERING_ITEMS_SQL = "DELETE FROM quiz_ordering_item WHERE question_id = ?";
  private static final String DELETE_IMAGE_SQL = "DELETE FROM quiz_image WHERE question_id = ?";
  private static final String DELETE_COLLECTION_MEMBERSHIP_SQL =
      "DELETE FROM quiz_question_collection WHERE question_id = ?";

  private static final String INSERT_SOURCE_SQL =
      """
      INSERT INTO quiz_source (question_id, display_order, display_name, url)
      VALUES (?, ?, ?, ?)
      """;

  private static final String INSERT_OPTION_SQL =
      """
      INSERT INTO quiz_option (question_id, option_id, display_order, option_text, is_correct)
      VALUES (?, ?, ?, ?, ?)
      """;

  private static final String INSERT_ORDERING_ITEM_SQL =
      """
      INSERT INTO quiz_ordering_item (question_id, item_id, item_text, correct_position)
      VALUES (?, ?, ?, ?)
      """;

  private static final String UPSERT_IMAGE_SQL =
      """
      INSERT INTO quiz_image (
        question_id,
        url,
        alt_text,
        source_name,
        source_url,
        attribution,
        creator,
        license_name,
        license_url
      )
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
      ON CONFLICT (question_id) DO UPDATE SET
        url = EXCLUDED.url,
        alt_text = EXCLUDED.alt_text,
        source_name = EXCLUDED.source_name,
        source_url = EXCLUDED.source_url,
        attribution = EXCLUDED.attribution,
        creator = EXCLUDED.creator,
        license_name = EXCLUDED.license_name,
        license_url = EXCLUDED.license_url,
        updated_at = now()
      """;

  private static final String INSERT_COLLECTION_MEMBERSHIP_SQL =
      """
      INSERT INTO quiz_question_collection (question_id, collection_id)
      VALUES (?, ?)
      """;

  private final DataSource dataSource;
  private final QuizContentValidator validator;

  public QuizContentImporter(DataSource dataSource, QuizContentValidator validator) {
    this.dataSource = Objects.requireNonNull(dataSource, "dataSource must not be null");
    this.validator = Objects.requireNonNull(validator, "validator must not be null");
  }

  public ContentValidationResult importContent(CuratedQuizContent content) {
    Objects.requireNonNull(content, "content must not be null");

    var validationResult = validator.validate(content);
    if (!validationResult.valid()) {
      throw new QuizContentImportException(validationResult.errors());
    }

    try (var connection = dataSource.getConnection()) {
      connection.setAutoCommit(false);
      try {
        importContent(connection, content);
        connection.commit();
      } catch (SQLException | RuntimeException exception) {
        connection.rollback();
        throw exception;
      }
    } catch (SQLException exception) {
      throw new QuizContentImportException("Curated quiz content import failed.", exception);
    }

    return validationResult;
  }

  private void importContent(Connection connection, CuratedQuizContent content) throws SQLException {
    for (var collection : collections(content.collectionsFile())) {
      upsertCollection(connection, collection);
    }
    for (var pack : content.questionPacks()) {
      for (var question : questions(pack.file())) {
        upsertQuestion(connection, question);
      }
    }
    for (var pack : content.questionPacks()) {
      for (var question : questions(pack.file())) {
        replaceChildren(connection, question);
      }
    }
  }

  private void upsertCollection(Connection connection, CuratedQuizCollectionJson collection)
      throws SQLException {
    try (var statement = connection.prepareStatement(UPSERT_COLLECTION_SQL)) {
      statement.setString(1, collection.id());
      statement.setString(2, collection.name());
      statement.setString(3, collection.group());
      statement.executeUpdate();
    }
  }

  private void upsertQuestion(Connection connection, CuratedQuizQuestionJson question) throws SQLException {
    try (var statement = connection.prepareStatement(UPSERT_QUESTION_SQL)) {
      statement.setString(1, question.id());
      statement.setString(2, question.type());
      statement.setString(3, question.difficulty());
      statement.setString(4, question.publicationState());
      statement.setString(5, question.prompt());
      statement.setString(6, question.explanation());
      statement.executeUpdate();
    }
  }

  private void replaceChildren(Connection connection, CuratedQuizQuestionJson question) throws SQLException {
    deleteByQuestionId(connection, DELETE_SOURCES_SQL, question.id());
    deleteByQuestionId(connection, DELETE_OPTIONS_SQL, question.id());
    deleteByQuestionId(connection, DELETE_ORDERING_ITEMS_SQL, question.id());
    deleteByQuestionId(connection, DELETE_IMAGE_SQL, question.id());
    deleteByQuestionId(connection, DELETE_COLLECTION_MEMBERSHIP_SQL, question.id());

    insertSources(connection, question);
    if ("chronological_ordering".equals(question.type())) {
      insertOrderingItems(connection, question);
    } else {
      insertOptions(connection, question);
      if ("image_identification".equals(question.type())) {
        upsertImage(connection, question);
      }
    }
    insertCollectionMembership(connection, question);
  }

  private void insertSources(Connection connection, CuratedQuizQuestionJson question) throws SQLException {
    try (var statement = connection.prepareStatement(INSERT_SOURCE_SQL)) {
      var displayOrder = 1;
      for (var source : question.sources()) {
        statement.setString(1, question.id());
        statement.setInt(2, displayOrder);
        statement.setString(3, source.displayName());
        statement.setString(4, source.url());
        statement.addBatch();
        displayOrder += 1;
      }
      statement.executeBatch();
    }
  }

  private void insertOptions(Connection connection, CuratedQuizQuestionJson question) throws SQLException {
    try (var statement = connection.prepareStatement(INSERT_OPTION_SQL)) {
      var displayOrder = 1;
      for (var option : question.options()) {
        statement.setString(1, question.id());
        statement.setString(2, option.id());
        statement.setInt(3, displayOrder);
        statement.setString(4, option.text());
        statement.setBoolean(5, option.id().equals(question.correctOptionId()));
        statement.addBatch();
        displayOrder += 1;
      }
      statement.executeBatch();
    }
  }

  private void insertOrderingItems(Connection connection, CuratedQuizQuestionJson question)
      throws SQLException {
    try (var statement = connection.prepareStatement(INSERT_ORDERING_ITEM_SQL)) {
      for (var item : question.items()) {
        statement.setString(1, question.id());
        statement.setString(2, item.id());
        statement.setString(3, item.text());
        statement.setInt(4, question.correctOrderItemIds().indexOf(item.id()) + 1);
        statement.addBatch();
      }
      statement.executeBatch();
    }
  }

  private void upsertImage(Connection connection, CuratedQuizQuestionJson question) throws SQLException {
    var image = question.image();
    try (var statement = connection.prepareStatement(UPSERT_IMAGE_SQL)) {
      statement.setString(1, question.id());
      statement.setString(2, image.url());
      statement.setString(3, image.altText());
      statement.setString(4, image.source());
      statement.setString(5, image.sourceUrl());
      statement.setString(6, image.attribution());
      statement.setString(7, image.creator());
      statement.setString(8, image.license());
      statement.setString(9, image.licenseUrl());
      statement.executeUpdate();
    }
  }

  private void insertCollectionMembership(Connection connection, CuratedQuizQuestionJson question)
      throws SQLException {
    try (var statement = connection.prepareStatement(INSERT_COLLECTION_MEMBERSHIP_SQL)) {
      for (var collectionId : collectionIds(question)) {
        statement.setString(1, question.id());
        statement.setString(2, collectionId);
        statement.addBatch();
      }
      statement.executeBatch();
    }
  }

  private void deleteByQuestionId(Connection connection, String sql, String questionId) throws SQLException {
    try (var statement = connection.prepareStatement(sql)) {
      statement.setString(1, questionId);
      statement.executeUpdate();
    }
  }

  private static List<CuratedQuizCollectionJson> collections(QuizCollectionsFile file) {
    return file.collections() == null ? List.of() : file.collections();
  }

  private static List<CuratedQuizQuestionJson> questions(QuizQuestionPackFile file) {
    return file.questions() == null ? List.of() : file.questions();
  }

  private static List<String> collectionIds(CuratedQuizQuestionJson question) {
    return question.collectionIds() == null ? List.of() : question.collectionIds();
  }
}
