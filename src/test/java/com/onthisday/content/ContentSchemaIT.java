package com.onthisday.content;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class ContentSchemaIT {

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
  void allowsFeaturedEventWithRequiredSourceAndOptionalImage() {
    assertDoesNotThrow(
        () ->
            inTransaction(
                connection -> {
                  insertEvent(connection, "battle-of-bosworth", true);
                  insertSource(connection, "battle-of-bosworth", 1);
                  insertFeaturedDailyEvent(connection, 8, 22, "battle-of-bosworth");
                }));
  }

  @Test
  void rejectsMultipleFeaturedEventsForSameMonthDay() {
    assertThrows(
        SQLException.class,
        () ->
            inTransaction(
                connection -> {
                  insertEvent(connection, "featured-one", true);
                  insertSource(connection, "featured-one", 1);
                  insertFeaturedDailyEvent(connection, 1, 1, "featured-one");

                  insertEvent(connection, "featured-two", true);
                  insertSource(connection, "featured-two", 1);
                  insertFeaturedDailyEvent(connection, 1, 1, "featured-two");
                }));
  }

  @Test
  void rejectsEventWithoutSourceAtCommit() {
    assertThrows(
        SQLException.class,
        () ->
            inTransaction(
                connection -> {
                  insertEvent(connection, "source-missing", false);
                }));
  }

  @Test
  void rejectsSourceMoveThatLeavesOldEventWithoutSource() {
    assertThrows(
        SQLException.class,
        () ->
            inTransaction(
                connection -> {
                  insertEvent(connection, "old-source-owner", false);
                  insertSource(connection, "old-source-owner", 1);
                  insertEvent(connection, "new-source-owner", false);
                  insertSource(connection, "new-source-owner", 1);

                  try (var statement =
                      connection.prepareStatement("UPDATE event_source SET event_id = ? WHERE event_id = ?")) {
                    statement.setString(1, "new-source-owner");
                    statement.setString(2, "old-source-owner");
                    statement.executeUpdate();
                  }
                }));
  }

  @Test
  void allowsDeletingEventWithCascadedSources() {
    assertDoesNotThrow(
        () ->
            inTransaction(
                connection -> {
                  insertEvent(connection, "delete-with-source", false);
                  insertSource(connection, "delete-with-source", 1);

                  try (var statement =
                      connection.prepareStatement("DELETE FROM historical_event WHERE event_id = ?")) {
                    statement.setString(1, "delete-with-source");
                    statement.executeUpdate();
                  }
                }));
  }

  @Test
  void rejectsFeaturedEventWithoutNotificationCopy() {
    assertThrows(
        SQLException.class,
        () ->
            inTransaction(
                connection -> {
                  insertEvent(connection, "featured-without-notification", false);
                  insertSource(connection, "featured-without-notification", 1);
                  insertFeaturedDailyEvent(connection, 2, 1, "featured-without-notification");
                }));
  }

  @Test
  void rejectsRemovingNotificationCopyFromFeaturedEvent() {
    assertThrows(
        SQLException.class,
        () ->
            inTransaction(
                connection -> {
                  insertEvent(connection, "featured-copy-removed", true);
                  insertSource(connection, "featured-copy-removed", 1);
                  insertFeaturedDailyEvent(connection, 2, 2, "featured-copy-removed");

                  try (var statement =
                      connection.prepareStatement(
                          """
                          UPDATE historical_event
                          SET notification_title = NULL
                          WHERE event_id = ?
                          """)) {
                    statement.setString(1, "featured-copy-removed");
                    statement.executeUpdate();
                  }
                }));
  }

  @Test
  void allowsAdditionalEventsWithOrderedNoImageContent() {
    assertDoesNotThrow(
        () ->
            inTransaction(
                connection -> {
                  insertEvent(connection, "additional-no-image", false);
                  insertSource(connection, "additional-no-image", 1);
                  insertAdditionalDailyEvent(connection, 3, 14, "additional-no-image", 2);
                }));
  }

  @Test
  void rejectsDuplicateDisplayOrderForDailyEvents() {
    assertThrows(
        SQLException.class,
        () ->
            inTransaction(
                connection -> {
                  insertEvent(connection, "additional-one", false);
                  insertSource(connection, "additional-one", 1);
                  insertAdditionalDailyEvent(connection, 4, 5, "additional-one", 2);

                  insertEvent(connection, "additional-two", false);
                  insertSource(connection, "additional-two", 1);
                  insertAdditionalDailyEvent(connection, 4, 5, "additional-two", 2);
                }));
  }

  @Test
  void rejectsInvalidMonthDay() {
    assertThrows(
        SQLException.class,
        () ->
            inTransaction(
                connection -> {
                  insertEvent(connection, "invalid-date-event", false);
                  insertSource(connection, "invalid-date-event", 1);
                  insertAdditionalDailyEvent(connection, 2, 30, "invalid-date-event", 2);
                }));
  }

  @Test
  void rejectsImageWithoutAltTextOrHttpsUrl() {
    assertThrows(
        SQLException.class,
        () ->
            inTransaction(
                connection -> {
                  insertEvent(connection, "bad-image-event", false);
                  insertSource(connection, "bad-image-event", 1);

                  try (var statement =
                      connection.prepareStatement(
                          """
                          INSERT INTO event_image (event_id, display_order, is_primary, url, alt_text)
                          VALUES (?, 1, true, 'http://example.com/image.jpg', ' ')
                          """)) {
                    statement.setString(1, "bad-image-event");
                    statement.executeUpdate();
                  }
                }));
  }

  @Test
  void allowsImageMetadataWhenPresent() {
    assertDoesNotThrow(
        () ->
            inTransaction(
                connection -> {
                  insertEvent(connection, "image-metadata-event", false);
                  insertSource(connection, "image-metadata-event", 1);

                  try (var statement =
                      connection.prepareStatement(
                          """
                          INSERT INTO event_image (
                            event_id,
                            display_order,
                            is_primary,
                            url,
                            alt_text,
                            source_name,
                            source_url,
                            creator,
                            attribution,
                            license_name,
                            license_url
                          )
                          VALUES (?, 1, true, ?, ?, ?, ?, ?, ?, ?, ?)
                          """)) {
                    statement.setString(1, "image-metadata-event");
                    statement.setString(2, "https://example.com/image.jpg");
                    statement.setString(3, "A historical illustration.");
                    statement.setString(4, "Wikimedia Commons");
                    statement.setString(5, "https://commons.wikimedia.org/");
                    statement.setString(6, "Unknown artist");
                    statement.setString(7, "Public domain image via Wikimedia Commons.");
                    statement.setString(8, "Public domain");
                    statement.setString(9, "https://creativecommons.org/publicdomain/mark/1.0/");
                    statement.executeUpdate();
                  }
                }));
  }

  private static void insertEvent(Connection connection, String eventId, boolean withNotificationCopy)
      throws SQLException {
    try (var statement =
        connection.prepareStatement(
            """
            INSERT INTO historical_event (
              event_id,
              title,
              year_label,
              historical_date,
              date_note,
              summary,
              description,
              notification_title,
              notification_body
            )
            VALUES (?, ?, '1485', 'August 22, 1485', 'Date follows the commonly recognized historical record.',
              'A concise summary.', 'A concise description.', ?, ?)
            """)) {
      statement.setString(1, eventId);
      statement.setString(2, "Test event " + eventId);
      statement.setString(3, withNotificationCopy ? "A notification title" : null);
      statement.setString(4, withNotificationCopy ? "A notification body" : null);
      statement.executeUpdate();
    }
  }

  private static void insertSource(Connection connection, String eventId, int displayOrder)
      throws SQLException {
    try (var statement =
        connection.prepareStatement(
            """
            INSERT INTO event_source (event_id, display_order, display_name, url)
            VALUES (?, ?, 'Encyclopaedia Britannica', 'https://www.britannica.com/')
            """)) {
      statement.setString(1, eventId);
      statement.setInt(2, displayOrder);
      statement.executeUpdate();
    }
  }

  private static void insertFeaturedDailyEvent(
      Connection connection, int month, int day, String eventId) throws SQLException {
    insertDailyEvent(connection, month, day, eventId, "featured", 1);
  }

  private static void insertAdditionalDailyEvent(
      Connection connection, int month, int day, String eventId, int displayOrder) throws SQLException {
    insertDailyEvent(connection, month, day, eventId, "additional", displayOrder);
  }

  private static void insertDailyEvent(
      Connection connection, int month, int day, String eventId, String role, int displayOrder)
      throws SQLException {
    try (var statement =
        connection.prepareStatement(
            """
            INSERT INTO daily_event (month, day, event_id, role, display_order)
            VALUES (?, ?, ?, ?, ?)
            """)) {
      statement.setInt(1, month);
      statement.setInt(2, day);
      statement.setString(3, eventId);
      statement.setString(4, role);
      statement.setInt(5, displayOrder);
      statement.executeUpdate();
    }
  }

  private static void inTransaction(SqlWork work) throws SQLException {
    try (var connection =
        DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())) {
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
