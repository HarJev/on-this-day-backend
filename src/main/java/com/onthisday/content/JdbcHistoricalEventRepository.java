package com.onthisday.content;

import static com.onthisday.content.ContentChecks.requireNonBlank;

import java.net.URI;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcHistoricalEventRepository implements HistoricalEventRepository {

  private static final Logger LOG = LoggerFactory.getLogger(JdbcHistoricalEventRepository.class);

  private static final String EVENT_SQL =
      """
      SELECT
        event_id,
        title,
        year_label,
        historical_date,
        summary,
        description,
        date_note
      FROM historical_event
      WHERE event_id = ?
      """;

  private static final String SOURCES_SQL =
      """
      SELECT display_name, url
      FROM event_source
      WHERE event_id = ?
      ORDER BY display_order
      """;

  private static final String IMAGES_SQL =
      """
      SELECT
        url,
        alt_text,
        source_name,
        source_url,
        attribution,
        creator,
        license_name,
        license_url,
        is_primary
      FROM event_image
      WHERE event_id = ?
      ORDER BY display_order
      """;

  private final DataSource dataSource;

  public JdbcHistoricalEventRepository(DataSource dataSource) {
    this.dataSource = Objects.requireNonNull(dataSource, "dataSource must not be null");
  }

  @Override
  public HistoricalEvent getEvent(String eventId) {
    requireNonBlank(eventId, "eventId");

    var startedAt = System.nanoTime();
    LOG.info("db_query_start operation=getEvent eventId={}", eventId);
    LOG.info("db_connection_start operation=getEvent eventId={}", eventId);
    var connectionStartedAt = System.nanoTime();
    try (var connection = dataSource.getConnection()) {
      var connectionDurationMs = (System.nanoTime() - connectionStartedAt) / 1_000_000;
      LOG.info(
          "db_connection_acquired operation=getEvent eventId={} durationMs={}",
          eventId,
          connectionDurationMs);
      var event = findEvent(connection, eventId);
      if (event == null) {
        throw new EventNotFoundException(eventId);
      }

      var sources = findSources(connection, eventId);
      if (sources.isEmpty()) {
        throw new ContentUnavailableException("Event has no sources: " + eventId);
      }

      var images = findImages(connection, eventId);
      EventImage primaryImage = null;
      var eventImages = new ArrayList<EventImage>();
      for (var image : images) {
        eventImages.add(image.image());
        if (image.primary()) {
          primaryImage = image.image();
        }
      }

      var historicalEvent =
          new HistoricalEvent(
              event.id(),
              event.title(),
              event.year(),
              event.historicalDate(),
              event.summary(),
              event.description(),
              sources,
              primaryImage,
              eventImages,
              event.dateNote());
      var durationMs = (System.nanoTime() - startedAt) / 1_000_000;
      LOG.info(
          "db_query_end operation=getEvent eventId={} sourceCount={} imageCount={} durationMs={}",
          eventId,
          sources.size(),
          eventImages.size(),
          durationMs);
      return historicalEvent;
    } catch (SQLException exception) {
      LOG.error("db_query_failed operation=getEvent eventId={}", eventId, exception);
      throw new ContentUnavailableException("Could not load event: " + eventId, exception);
    }
  }

  private EventRecord findEvent(Connection connection, String eventId) throws SQLException {
    try (var statement = connection.prepareStatement(EVENT_SQL)) {
      statement.setString(1, eventId);

      try (var resultSet = statement.executeQuery()) {
        if (!resultSet.next()) {
          return null;
        }

        return new EventRecord(
            resultSet.getString("event_id"),
            resultSet.getString("title"),
            resultSet.getString("year_label"),
            resultSet.getString("historical_date"),
            resultSet.getString("summary"),
            resultSet.getString("description"),
            resultSet.getString("date_note"));
      }
    }
  }

  private List<EventSource> findSources(Connection connection, String eventId) throws SQLException {
    try (var statement = connection.prepareStatement(SOURCES_SQL)) {
      statement.setString(1, eventId);

      try (var resultSet = statement.executeQuery()) {
        var sources = new ArrayList<EventSource>();
        while (resultSet.next()) {
          sources.add(new EventSource(resultSet.getString("display_name"), URI.create(resultSet.getString("url"))));
        }
        return sources;
      }
    }
  }

  private List<ImageRecord> findImages(Connection connection, String eventId) throws SQLException {
    try (var statement = connection.prepareStatement(IMAGES_SQL)) {
      statement.setString(1, eventId);

      try (var resultSet = statement.executeQuery()) {
        var images = new ArrayList<ImageRecord>();
        while (resultSet.next()) {
          images.add(new ImageRecord(mapImage(resultSet), resultSet.getBoolean("is_primary")));
        }
        return images;
      }
    }
  }

  private static EventImage mapImage(ResultSet resultSet) throws SQLException {
    return new EventImage(
        URI.create(resultSet.getString("url")),
        resultSet.getString("alt_text"),
        resultSet.getString("source_name"),
        toUri(resultSet.getString("source_url")),
        resultSet.getString("attribution"),
        resultSet.getString("creator"),
        resultSet.getString("license_name"),
        toUri(resultSet.getString("license_url")));
  }

  private static URI toUri(String value) {
    return value == null ? null : URI.create(value);
  }

  private record EventRecord(
      String id,
      String title,
      String year,
      String historicalDate,
      String summary,
      String description,
      String dateNote) {}

  private record ImageRecord(EventImage image, boolean primary) {}
}
