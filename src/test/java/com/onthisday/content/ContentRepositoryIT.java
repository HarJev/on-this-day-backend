package com.onthisday.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.MonthDay;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class ContentRepositoryIT {

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

  @Test
  void todayContentReturnsFeaturedImageAndOrderedAdditionalEvents() throws SQLException {
    inTransaction(
        connection -> {
          insertEvent(
              connection,
              "battle-of-bosworth-field-1485",
              "Richard III is defeated at the Battle of Bosworth Field",
              "1485",
              "August 22, 1485",
              "The battle ended the Wars of the Roses.",
              "A concise description.",
              "A king died in battle 541 years ago today",
              "Richard III's defeat at Bosworth changed England forever.",
              null);
          insertSource(connection, "battle-of-bosworth-field-1485", 1, "Encyclopaedia Britannica");
          insertImage(connection, "battle-of-bosworth-field-1485", 1, true);
          insertDailyEvent(connection, 8, 22, "battle-of-bosworth-field-1485", "featured", 1);

          insertEvent(
              connection,
              "cook-claims-eastern-australia-1770",
              "James Cook claims eastern Australia for Britain",
              "1770",
              "August 22, 1770",
              "Cook claims eastern Australia.",
              "A concise description.",
              null,
              null,
              null);
          insertSource(connection, "cook-claims-eastern-australia-1770", 1, "National Museum of Australia");
          insertDailyEvent(connection, 8, 22, "cook-claims-eastern-australia-1770", "additional", 3);

          insertEvent(
              connection,
              "loch-ness-columba-565",
              "Saint Columba reports seeing a monster in Loch Ness",
              "565",
              "August 22, 565",
              "A monster is reported in Loch Ness.",
              "A concise description.",
              null,
              null,
              "Traditional date used by later accounts.");
          insertSource(connection, "loch-ness-columba-565", 1, "Historic UK");
          insertDailyEvent(connection, 8, 22, "loch-ness-columba-565", "additional", 2);
        });

    var repository = new JdbcTodayContentRepository(dataSource);

    var content = repository.getTodayContent(MonthDay.of(8, 22));

    assertEquals(new ContentDate(8, 22, "Aug 22"), content.date());
    assertEquals("battle-of-bosworth-field-1485", content.featuredEvent().id());
    assertEquals("1485", content.featuredEvent().year());
    assertEquals("August 22, 1485", content.featuredEvent().historicalDate());
    assertEquals("A king died in battle 541 years ago today", content.featuredEvent().notificationTitle());
    assertEquals(URI.create("https://example.com/image.jpg"), content.featuredEvent().image().url());
    assertEquals(URI.create("https://commons.wikimedia.org/"), content.featuredEvent().image().sourceUrl());
    assertEquals("Public domain", content.featuredEvent().image().license());

    assertEquals(2, content.additionalEvents().size());
    assertEquals("loch-ness-columba-565", content.additionalEvents().get(0).id());
    assertEquals("cook-claims-eastern-australia-1770", content.additionalEvents().get(1).id());
    assertEquals("Traditional date used by later accounts.", content.additionalEvents().get(0).dateNote());
  }

  @Test
  void todayContentThrowsWhenFeaturedContentIsMissing() {
    var repository = new JdbcTodayContentRepository(dataSource);

    var exception =
        assertThrows(ContentUnavailableException.class, () -> repository.getTodayContent(MonthDay.of(12, 31)));

    assertEquals("Featured content unavailable for 12/31", exception.getMessage());
  }

  @Test
  void eventRepositoryReturnsDetailsSourcesImagesAndPrimaryImage() throws SQLException {
    inTransaction(
        connection -> {
          insertEvent(
              connection,
              "printing-press-1450",
              "Gutenberg develops movable type printing",
              "c. 1450",
              "About 1450",
              "Movable type changes communication.",
              "Gutenberg's press helped books spread more widely across Europe.",
              null,
              null,
              "The exact date is approximate.");
          insertSource(connection, "printing-press-1450", 2, "Library of Congress");
          insertSource(connection, "printing-press-1450", 1, "Encyclopaedia Britannica");
          insertImage(connection, "printing-press-1450", 2, true);
          insertImage(connection, "printing-press-1450", 1, false);
        });

    var repository = new JdbcHistoricalEventRepository(dataSource);

    var event = repository.getEvent("printing-press-1450");

    assertEquals("printing-press-1450", event.id());
    assertEquals("Gutenberg develops movable type printing", event.title());
    assertEquals("c. 1450", event.year());
    assertEquals("About 1450", event.historicalDate());
    assertEquals("The exact date is approximate.", event.dateNote());
    assertEquals("Gutenberg's press helped books spread more widely across Europe.", event.description());

    assertEquals(2, event.sources().size());
    assertEquals("Encyclopaedia Britannica", event.sources().get(0).name());
    assertEquals("Library of Congress", event.sources().get(1).name());

    assertEquals(2, event.images().size());
    assertEquals(URI.create("https://example.com/image.jpg"), event.primaryImage().url());
    assertEquals(URI.create("https://commons.wikimedia.org/"), event.primaryImage().sourceUrl());
    assertEquals("Wikimedia Commons", event.primaryImage().source());
  }

  @Test
  void eventRepositoryThrowsWhenEventIsMissing() {
    var repository = new JdbcHistoricalEventRepository(dataSource);

    var exception = assertThrows(EventNotFoundException.class, () -> repository.getEvent("missing-event"));

    assertEquals("Event not found: missing-event", exception.getMessage());
  }

  @Test
  void eventRepositoryValidatesEventId() {
    var repository = new JdbcHistoricalEventRepository(dataSource);

    assertThrows(IllegalArgumentException.class, () -> repository.getEvent(" "));
  }

  private static void insertEvent(
      Connection connection,
      String eventId,
      String title,
      String year,
      String historicalDate,
      String summary,
      String description,
      String notificationTitle,
      String notificationBody,
      String dateNote)
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
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """)) {
      statement.setString(1, eventId);
      statement.setString(2, title);
      statement.setString(3, year);
      statement.setString(4, historicalDate);
      statement.setString(5, dateNote);
      statement.setString(6, summary);
      statement.setString(7, description);
      statement.setString(8, notificationTitle);
      statement.setString(9, notificationBody);
      statement.executeUpdate();
    }
  }

  private static void insertSource(Connection connection, String eventId, int displayOrder, String displayName)
      throws SQLException {
    try (var statement =
        connection.prepareStatement(
            """
            INSERT INTO event_source (event_id, display_order, display_name, url)
            VALUES (?, ?, ?, ?)
            """)) {
      statement.setString(1, eventId);
      statement.setInt(2, displayOrder);
      statement.setString(3, displayName);
      statement.setString(4, "https://example.com/source-" + displayOrder);
      statement.executeUpdate();
    }
  }

  private static void insertImage(Connection connection, String eventId, int displayOrder, boolean primary)
      throws SQLException {
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
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """)) {
      statement.setString(1, eventId);
      statement.setInt(2, displayOrder);
      statement.setBoolean(3, primary);
      statement.setString(4, "https://example.com/image.jpg");
      statement.setString(5, "A historical image.");
      statement.setString(6, "Wikimedia Commons");
      statement.setString(7, "https://commons.wikimedia.org/");
      statement.setString(8, "Unknown artist");
      statement.setString(9, "Public domain image via Wikimedia Commons.");
      statement.setString(10, "Public domain");
      statement.setString(11, "https://creativecommons.org/publicdomain/mark/1.0/");
      statement.executeUpdate();
    }
  }

  private static void insertDailyEvent(
      Connection connection, int month, int day, String eventId, String role, int displayOrder) throws SQLException {
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
    try (var connection = dataSource.getConnection()) {
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
