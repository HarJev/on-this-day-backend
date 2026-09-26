package com.onthisday.quiz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onthisday.ingestion.quiz.QuizContentImporter;
import com.onthisday.ingestion.quiz.QuizContentReader;
import com.onthisday.ingestion.quiz.QuizContentValidator;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
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
class QuizRepositoryIT {

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
  }

  @Test
  void mapsAllQuestionShapesWithoutDuplicateAggregatesAndPreservesRequestedOrder()
      throws SQLException {
    importFixture();
    inTransaction(
        connection -> {
          insertSource(connection, "capital-of-aztec-empire", 2, "Second source", "https://example.com/second");
          insertMembership(connection, "capital-of-aztec-empire", "spaceflight");
          execute(connection, "UPDATE quiz_image SET creator = NULL WHERE question_id = 'identify-battle-of-bosworth'");
        });

    var repository = new JdbcQuizQuestionRepository(dataSource);
    var questions =
        repository.findByIdsInOrder(
            List.of(
                "order-spaceflight-milestones",
                "identify-battle-of-bosworth",
                "movable-type-before-gutenberg",
                "capital-of-aztec-empire"));

    assertEquals(
        List.of(
            "order-spaceflight-milestones",
            "identify-battle-of-bosworth",
            "movable-type-before-gutenberg",
            "capital-of-aztec-empire"),
        questions.stream().map(QuizQuestion::id).toList());
    assertInstanceOf(ChronologicalOrderingQuestion.class, questions.get(0));
    assertInstanceOf(ImageIdentificationQuestion.class, questions.get(1));
    assertInstanceOf(TrueFalseQuestion.class, questions.get(2));
    assertInstanceOf(MultipleChoiceQuestion.class, questions.get(3));

    var chronological = (ChronologicalOrderingQuestion) questions.get(0);
    assertEquals(List.of("sputnik-1", "gagarin", "tereshkova", "apollo-11"), chronological.items().stream().map(ChronologicalOrderingItem::id).toList());

    var imageQuestion = (ImageIdentificationQuestion) questions.get(1);
    assertEquals("Wikimedia Commons", imageQuestion.image().source());
    assertNull(imageQuestion.image().creator());
    assertEquals(4, imageQuestion.options().size());

    var trueFalse = (TrueFalseQuestion) questions.get(2);
    assertEquals(List.of("true", "false"), trueFalse.options().stream().map(QuizOption::id).toList());

    var multipleChoice = (MultipleChoiceQuestion) questions.get(3);
    assertEquals(4, multipleChoice.options().size());
    assertEquals(2, multipleChoice.sources().size());
    assertEquals("Encyclopaedia Britannica - Tenochtitlan", multipleChoice.sources().get(0).displayName());
    assertEquals("Second source", multipleChoice.sources().get(1).displayName());
  }

  @Test
  void candidatesOnlyIncludePublishedQuestionsAndCanBeFilteredByCollection() throws SQLException {
    importFixture();
    execute(
        "UPDATE quiz_question SET publication_state = 'draft' WHERE question_id = 'movable-type-before-gutenberg'");
    execute(
        "UPDATE quiz_question SET publication_state = 'retired' WHERE question_id = 'identify-battle-of-bosworth'");

    var repository = new JdbcQuizQuestionRepository(dataSource);

    assertEquals(
        List.of("capital-of-aztec-empire", "order-spaceflight-milestones"),
        repository.findPublishedCandidates().stream().map(QuizQuestionCandidate::questionId).toList());
    assertEquals(
        List.of("capital-of-aztec-empire"),
        repository.findPublishedCandidatesByCollectionId("ancient-history").stream()
            .map(QuizQuestionCandidate::questionId)
            .toList());
    assertTrue(repository.findPublishedCandidatesByCollectionId("missing-collection").isEmpty());
  }

  @Test
  void explicitlyLoadsDraftAndRetiredQuestionsButFailsForMissingIds() throws SQLException {
    importFixture();
    execute(
        "UPDATE quiz_question SET publication_state = 'draft' WHERE question_id = 'movable-type-before-gutenberg'");
    execute(
        "UPDATE quiz_question SET publication_state = 'retired' WHERE question_id = 'identify-battle-of-bosworth'");

    var repository = new JdbcQuizQuestionRepository(dataSource);
    var questions =
        repository.findByIdsInOrder(
            List.of("identify-battle-of-bosworth", "movable-type-before-gutenberg"));

    assertEquals(QuestionPublicationState.RETIRED, questions.get(0).publicationState());
    assertEquals(QuestionPublicationState.DRAFT, questions.get(1).publicationState());
    var exception =
        assertThrows(
            QuizUnavailableException.class,
            () -> repository.findByIdsInOrder(List.of("capital-of-aztec-empire", "missing-question")));
    assertTrue(exception.getMessage().contains("missing-question"));
  }

  @Test
  void wrapsMalformedPersistedDataInOperationalException() throws SQLException {
    importFixture();
    execute(
        "UPDATE quiz_source SET url = 'https://bad host' WHERE question_id = 'capital-of-aztec-empire'");

    var exception =
        assertThrows(
            QuizUnavailableException.class,
            () -> new JdbcQuizQuestionRepository(dataSource).findByIdsInOrder(List.of("capital-of-aztec-empire")));

    assertTrue(exception.getMessage().contains("capital-of-aztec-empire"));
    assertTrue(exception.getCause() instanceof IllegalArgumentException);
  }

  @Test
  void collectionRepositoryReturnsEmptyCollectionsInStableOrder() throws SQLException {
    importFixture();
    inTransaction(
        connection -> {
          insertCollection(connection, "empty-collection", "Empty Collection", "historical_period");
          insertCollection(connection, "a-topic", "A Topic", "topic");
        });

    var repository = new JdbcQuizCollectionRepository(dataSource);

    assertEquals(
        List.of("ancient-history", "empty-collection", "a-topic", "spaceflight"),
        repository.findAll().stream().map(QuizCollection::id).toList());
    assertTrue(repository.findById("empty-collection").isPresent());
    assertFalse(repository.findById("missing-collection").isPresent());
  }

  @Test
  void catalogCountsPublishedQuestionsOnceAndIncludesEmptyCollections() throws SQLException {
    importFixture();
    inTransaction(
        connection -> {
          insertCollection(connection, "catalog-empty", "Catalog Empty", "historical_period");
          insertCollection(connection, "catalog-five", "Catalog Five", "topic");
          insertCollection(connection, "catalog-ten", "Catalog Ten", "topic");
          insertCollection(connection, "catalog-twenty", "Catalog Twenty", "topic");
          for (var index = 1; index <= 20; index++) {
            var questionId = "catalog-question-" + index;
            insertPublishedMultipleChoiceQuestion(connection, questionId);
            insertMembership(connection, questionId, "catalog-twenty");
            if (index <= 10) {
              insertMembership(connection, questionId, "catalog-ten");
            }
            if (index <= 5) {
              insertMembership(connection, questionId, "catalog-five");
            }
          }
          insertDraftMultipleChoiceQuestion(connection, "catalog-draft-question");
          insertMembership(connection, "catalog-draft-question", "catalog-twenty");
        });

    var catalog = new QuizCatalogService(new JdbcQuizCatalogRepository(dataSource)).getCatalog();

    assertEquals(24, catalog.mixed().publishedQuestionCount());
    assertEquals(List.of(5, 10, 20), catalog.mixed().supportedQuestionCounts());
    assertEquals(
        List.of("ancient-history", "catalog-empty", "catalog-five", "catalog-ten", "catalog-twenty", "spaceflight"),
        catalog.collections().stream().map(item -> item.collection().id()).toList());
    assertEquals(0, collection(catalog, "catalog-empty").availability().publishedQuestionCount());
    assertEquals(List.of(), collection(catalog, "catalog-empty").availability().supportedQuestionCounts());
    assertEquals(5, collection(catalog, "catalog-five").availability().publishedQuestionCount());
    assertEquals(List.of(5), collection(catalog, "catalog-five").availability().supportedQuestionCounts());
    assertEquals(10, collection(catalog, "catalog-ten").availability().publishedQuestionCount());
    assertEquals(List.of(5, 10), collection(catalog, "catalog-ten").availability().supportedQuestionCounts());
    assertEquals(20, collection(catalog, "catalog-twenty").availability().publishedQuestionCount());
    assertEquals(List.of(5, 10, 20), collection(catalog, "catalog-twenty").availability().supportedQuestionCounts());
  }

  private static QuizCatalogCollection collection(QuizCatalog catalog, String id) {
    return catalog.collections().stream().filter(item -> item.collection().id().equals(id)).findFirst().orElseThrow();
  }

  private static void importFixture() {
    var content = new QuizContentReader(new ObjectMapper()).read(java.nio.file.Path.of("src/test/resources/ingestion/quiz/valid"));
    new QuizContentImporter(dataSource, new QuizContentValidator()).importContent(content);
  }

  private static void inTransaction(SqlOperation operation) throws SQLException {
    try (var connection = dataSource.getConnection()) {
      connection.setAutoCommit(false);
      try {
        operation.run(connection);
        connection.commit();
      } catch (SQLException | RuntimeException exception) {
        connection.rollback();
        throw exception;
      }
    }
  }

  private static void execute(String sql) throws SQLException {
    inTransaction(connection -> execute(connection, sql));
  }

  private static void execute(Connection connection, String sql) throws SQLException {
    try (var statement = connection.createStatement()) {
      statement.execute(sql);
    }
  }

  private static void insertCollection(Connection connection, String id, String name, String group)
      throws SQLException {
    try (var statement = connection.prepareStatement("INSERT INTO quiz_collection (collection_id, name, collection_group) VALUES (?, ?, ?)")) {
      statement.setString(1, id);
      statement.setString(2, name);
      statement.setString(3, group);
      statement.executeUpdate();
    }
  }

  private static void insertMembership(Connection connection, String questionId, String collectionId)
      throws SQLException {
    try (var statement = connection.prepareStatement("INSERT INTO quiz_question_collection (question_id, collection_id) VALUES (?, ?)")) {
      statement.setString(1, questionId);
      statement.setString(2, collectionId);
      statement.executeUpdate();
    }
  }

  private static void insertSource(
      Connection connection, String questionId, int displayOrder, String displayName, String url)
      throws SQLException {
    try (var statement = connection.prepareStatement("INSERT INTO quiz_source (question_id, display_order, display_name, url) VALUES (?, ?, ?, ?)")) {
      statement.setString(1, questionId);
      statement.setInt(2, displayOrder);
      statement.setString(3, displayName);
      statement.setString(4, url);
      statement.executeUpdate();
    }
  }

  private static void insertPublishedMultipleChoiceQuestion(Connection connection, String questionId)
      throws SQLException {
    insertChoiceQuestion(connection, questionId, "published");
  }

  private static void insertDraftMultipleChoiceQuestion(Connection connection, String questionId)
      throws SQLException {
    insertChoiceQuestion(connection, questionId, "draft");
  }

  private static void insertChoiceQuestion(Connection connection, String questionId, String publicationState)
      throws SQLException {
    try (var question = connection.prepareStatement("INSERT INTO quiz_question (question_id, question_type, difficulty, publication_state, prompt, explanation) VALUES (?, 'multiple_choice', 'easy', ?, ?, ?)");
        var source = connection.prepareStatement("INSERT INTO quiz_source (question_id, display_order, display_name, url) VALUES (?, 1, 'Source', 'https://example.com/source')");
        var option = connection.prepareStatement("INSERT INTO quiz_option (question_id, option_id, display_order, option_text, is_correct) VALUES (?, ?, ?, ?, ?)")) {
      question.setString(1, questionId);
      question.setString(2, publicationState);
      question.setString(3, "Prompt " + questionId);
      question.setString(4, "Explanation " + questionId);
      question.executeUpdate();

      source.setString(1, questionId);
      source.executeUpdate();

      for (var index = 1; index <= 4; index++) {
        option.setString(1, questionId);
        option.setString(2, "option-" + index);
        option.setInt(3, index);
        option.setString(4, "Option " + index);
        option.setBoolean(5, index == 1);
        option.addBatch();
      }
      option.executeBatch();
    }
  }

  @FunctionalInterface
  private interface SqlOperation {
    void run(Connection connection) throws SQLException;
  }
}
