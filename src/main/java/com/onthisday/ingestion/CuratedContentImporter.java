package com.onthisday.ingestion;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.sql.DataSource;

public class CuratedContentImporter {

  private static final String UPSERT_EVENT_SQL =
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
      ON CONFLICT (event_id) DO UPDATE SET
        title = EXCLUDED.title,
        year_label = EXCLUDED.year_label,
        historical_date = EXCLUDED.historical_date,
        date_note = EXCLUDED.date_note,
        summary = EXCLUDED.summary,
        description = EXCLUDED.description,
        notification_title = EXCLUDED.notification_title,
        notification_body = EXCLUDED.notification_body,
        updated_at = now()
      """;

  private static final String DELETE_DAILY_EVENTS_SQL =
      """
      DELETE FROM daily_event
      WHERE month = ?
        AND day = ?
      """;

  private static final String DELETE_SOURCES_SQL = "DELETE FROM event_source WHERE event_id = ?";
  private static final String DELETE_IMAGES_SQL = "DELETE FROM event_image WHERE event_id = ?";

  private static final String INSERT_SOURCE_SQL =
      """
      INSERT INTO event_source (event_id, display_order, display_name, url)
      VALUES (?, ?, ?, ?)
      """;

  private static final String INSERT_IMAGE_SQL =
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
      """;

  private static final String INSERT_DAILY_EVENT_SQL =
      """
      INSERT INTO daily_event (month, day, event_id, role, display_order)
      VALUES (?, ?, ?, ?, ?)
      """;

  private final DataSource dataSource;
  private final CuratedContentValidator validator;

  public CuratedContentImporter(DataSource dataSource, CuratedContentValidator validator) {
    this.dataSource = Objects.requireNonNull(dataSource, "dataSource must not be null");
    this.validator = Objects.requireNonNull(validator, "validator must not be null");
  }

  public ContentValidationResult importContent(CuratedContent content) {
    Objects.requireNonNull(content, "content must not be null");

    var validationResult = validator.validate(content.eventsFile(), content.dailyEventsFile());
    if (!validationResult.valid()) {
      throw new ContentImportException(validationResult.errors());
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
      throw new ContentImportException("Curated content import failed.", exception);
    }

    return validationResult;
  }

  private void importContent(Connection connection, CuratedContent content) throws SQLException {
    var eventsById = new HashMap<String, CuratedEventJson>();
    for (var event : content.eventsFile().events()) {
      eventsById.put(event.id(), event);
      upsertEvent(connection, event);
    }

    for (var event : content.eventsFile().events()) {
      replaceSources(connection, event);
      replaceImages(connection, event);
    }

    for (var day : content.dailyEventsFile().days()) {
      replaceDailyEvents(connection, day, eventsById);
    }
  }

  private void upsertEvent(Connection connection, CuratedEventJson event) throws SQLException {
    try (var statement = connection.prepareStatement(UPSERT_EVENT_SQL)) {
      statement.setString(1, event.id());
      statement.setString(2, event.title());
      statement.setString(3, event.year());
      statement.setString(4, event.historicalDate());
      statement.setString(5, event.dateNote());
      statement.setString(6, event.summary());
      statement.setString(7, event.description());
      statement.setString(8, event.notificationTitle());
      statement.setString(9, event.notificationBody());
      statement.executeUpdate();
    }
  }

  private void replaceSources(Connection connection, CuratedEventJson event) throws SQLException {
    deleteByEventId(connection, DELETE_SOURCES_SQL, event.id());

    try (var statement = connection.prepareStatement(INSERT_SOURCE_SQL)) {
      var displayOrder = 1;
      for (var source : event.sources()) {
        statement.setString(1, event.id());
        statement.setInt(2, displayOrder);
        statement.setString(3, source.name());
        statement.setString(4, source.url());
        statement.addBatch();
        displayOrder += 1;
      }
      statement.executeBatch();
    }
  }

  private void replaceImages(Connection connection, CuratedEventJson event) throws SQLException {
    deleteByEventId(connection, DELETE_IMAGES_SQL, event.id());

    if (event.images() == null || event.images().isEmpty()) {
      return;
    }

    try (var statement = connection.prepareStatement(INSERT_IMAGE_SQL)) {
      var displayOrder = 1;
      for (var image : event.images()) {
        statement.setString(1, event.id());
        statement.setInt(2, displayOrder);
        statement.setBoolean(3, Boolean.TRUE.equals(image.primary()));
        statement.setString(4, image.url());
        statement.setString(5, image.altText());
        statement.setString(6, image.source());
        statement.setString(7, image.sourceUrl());
        statement.setString(8, image.creator());
        statement.setString(9, image.attribution());
        statement.setString(10, image.license());
        statement.setString(11, image.licenseUrl());
        statement.addBatch();
        displayOrder += 1;
      }
      statement.executeBatch();
    }
  }

  private void replaceDailyEvents(
      Connection connection,
      CuratedDayJson day,
      Map<String, CuratedEventJson> eventsById)
      throws SQLException {
    try (var statement = connection.prepareStatement(DELETE_DAILY_EVENTS_SQL)) {
      statement.setInt(1, day.month());
      statement.setInt(2, day.day());
      statement.executeUpdate();
    }

    insertDailyEvent(connection, day.month(), day.day(), day.featuredEventId(), "featured", 1);

    var displayOrder = 2;
    for (var eventId : additionalEventIds(day)) {
      if (!eventsById.containsKey(eventId)) {
        throw new ContentImportException("Daily event references unknown event after validation: " + eventId);
      }
      insertDailyEvent(connection, day.month(), day.day(), eventId, "additional", displayOrder);
      displayOrder += 1;
    }
  }

  private void insertDailyEvent(
      Connection connection,
      int month,
      int day,
      String eventId,
      String role,
      int displayOrder)
      throws SQLException {
    try (var statement = connection.prepareStatement(INSERT_DAILY_EVENT_SQL)) {
      statement.setInt(1, month);
      statement.setInt(2, day);
      statement.setString(3, eventId);
      statement.setString(4, role);
      statement.setInt(5, displayOrder);
      statement.executeUpdate();
    }
  }

  private void deleteByEventId(Connection connection, String sql, String eventId) throws SQLException {
    try (var statement = connection.prepareStatement(sql)) {
      statement.setString(1, eventId);
      statement.executeUpdate();
    }
  }

  private List<String> additionalEventIds(CuratedDayJson day) {
    return day.additionalEventIds() == null ? List.of() : day.additionalEventIds();
  }
}
