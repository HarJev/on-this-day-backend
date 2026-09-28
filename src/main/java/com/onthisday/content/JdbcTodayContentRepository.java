package com.onthisday.content;

import java.net.URI;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTodayContentRepository implements TodayContentRepository {

  private static final Logger LOG = LoggerFactory.getLogger(JdbcTodayContentRepository.class);

  private static final DateTimeFormatter DISPLAY_DATE_FORMATTER =
      DateTimeFormatter.ofPattern("MMM d", Locale.US);

  private static final String FEATURED_SQL =
      """
      SELECT
        he.event_id,
        he.title,
        he.year_label,
        he.historical_date,
        he.summary,
        he.notification_title,
        he.notification_body,
        he.date_note,
        ei.url AS image_url,
        ei.alt_text,
        ei.source_name,
        ei.source_url,
        ei.attribution,
        ei.creator,
        ei.license_name,
        ei.license_url
      FROM daily_event de
      JOIN historical_event he ON he.event_id = de.event_id
      LEFT JOIN event_image ei
        ON ei.event_id = he.event_id
       AND ei.is_primary = TRUE
      WHERE de.month = ?
        AND de.day = ?
        AND de.role = 'featured'
      """;

  private static final String ADDITIONAL_SQL =
      """
      SELECT
        he.event_id,
        he.title,
        he.year_label,
        he.historical_date,
        he.date_note
      FROM daily_event de
      JOIN historical_event he ON he.event_id = de.event_id
      WHERE de.month = ?
        AND de.day = ?
        AND de.role = 'additional'
      ORDER BY de.display_order
      """;

  private static final String FEATURED_SUMMARIES_SQL_PREFIX =
      """
      SELECT
        de.month,
        de.day,
        he.event_id,
        he.title,
        he.year_label,
        he.historical_date,
        he.date_note
      FROM daily_event de
      JOIN historical_event he ON he.event_id = de.event_id
      WHERE de.role = 'featured'
        AND (de.month, de.day) IN (""";

  private final DataSource dataSource;

  public JdbcTodayContentRepository(DataSource dataSource) {
    this.dataSource = Objects.requireNonNull(dataSource, "dataSource must not be null");
  }

  @Override
  public TodayContent getTodayContent(MonthDay date) {
    Objects.requireNonNull(date, "date must not be null");

    var startedAt = System.nanoTime();
    LOG.debug(
        "db_query_start operation=getTodayContent month={} day={}",
        date.getMonthValue(),
        date.getDayOfMonth());
    LOG.debug(
        "db_connection_start operation=getTodayContent month={} day={}",
        date.getMonthValue(),
        date.getDayOfMonth());
    var connectionStartedAt = System.nanoTime();
    try (var connection = dataSource.getConnection()) {
      var connectionDurationMs = (System.nanoTime() - connectionStartedAt) / 1_000_000;
      LOG.debug(
          "db_connection_acquired operation=getTodayContent month={} day={} durationMs={}",
          date.getMonthValue(),
          date.getDayOfMonth(),
          connectionDurationMs);
      var featuredEvent = findFeaturedEvent(connection, date);
      var additionalEvents = findAdditionalEvents(connection, date);

      var content =
          new TodayContent(
              new ContentDate(date.getMonthValue(), date.getDayOfMonth(), displayDate(date)),
              featuredEvent,
              additionalEvents);
      var durationMs = (System.nanoTime() - startedAt) / 1_000_000;
      LOG.debug(
          "db_query_end operation=getTodayContent month={} day={} additionalCount={} durationMs={}",
          date.getMonthValue(),
          date.getDayOfMonth(),
          additionalEvents.size(),
          durationMs);
      return content;
    } catch (SQLException exception) {
      LOG.error(
          "db_query_failed operation=getTodayContent month={} day={}",
          date.getMonthValue(),
          date.getDayOfMonth(),
          exception);
      throw new ContentUnavailableException("Could not load today content.", exception);
    }
  }

  @Override
  public Map<MonthDay, EventSummary> findFeaturedEventSummaries(List<MonthDay> dates) {
    Objects.requireNonNull(dates, "dates must not be null");
    if (dates.isEmpty()) {
      return Map.of();
    }

    var sql =
        FEATURED_SUMMARIES_SQL_PREFIX
            + dates.stream().map(date -> "(?, ?)").collect(Collectors.joining(", "))
            + ")";
    var startedAt = System.nanoTime();
    LOG.debug("db_query_start operation=findFeaturedEventSummaries dateCount={}", dates.size());
    try (var connection = dataSource.getConnection();
        var statement = connection.prepareStatement(sql)) {
      var parameterIndex = 1;
      for (var date : dates) {
        statement.setInt(parameterIndex++, date.getMonthValue());
        statement.setInt(parameterIndex++, date.getDayOfMonth());
      }

      try (var resultSet = statement.executeQuery()) {
        var summaries = new HashMap<MonthDay, EventSummary>();
        while (resultSet.next()) {
          summaries.put(
              MonthDay.of(resultSet.getInt("month"), resultSet.getInt("day")),
              new EventSummary(
                  resultSet.getString("event_id"),
                  resultSet.getString("title"),
                  resultSet.getString("year_label"),
                  resultSet.getString("historical_date"),
                  resultSet.getString("date_note")));
        }
        var durationMs = (System.nanoTime() - startedAt) / 1_000_000;
        LOG.debug(
            "db_query_end operation=findFeaturedEventSummaries dateCount={} resultCount={} durationMs={}",
            dates.size(),
            summaries.size(),
            durationMs);
        return Map.copyOf(summaries);
      }
    } catch (SQLException exception) {
      LOG.error("db_query_failed operation=findFeaturedEventSummaries dateCount={}", dates.size(), exception);
      throw new ContentUnavailableException("Could not load recent days.", exception);
    }
  }

  private FeaturedEvent findFeaturedEvent(Connection connection, MonthDay date) throws SQLException {
    try (var statement = connection.prepareStatement(FEATURED_SQL)) {
      statement.setInt(1, date.getMonthValue());
      statement.setInt(2, date.getDayOfMonth());

      try (var resultSet = statement.executeQuery()) {
        if (!resultSet.next()) {
          throw new ContentUnavailableException(
              "Featured content unavailable for " + date.getMonthValue() + "/" + date.getDayOfMonth());
        }

        var featuredEvent =
            new FeaturedEvent(
                resultSet.getString("event_id"),
                resultSet.getString("title"),
                resultSet.getString("year_label"),
                resultSet.getString("historical_date"),
                resultSet.getString("summary"),
                resultSet.getString("notification_title"),
                resultSet.getString("notification_body"),
                mapOptionalImage(resultSet),
                resultSet.getString("date_note"));

        if (resultSet.next()) {
          throw new ContentUnavailableException(
              "Multiple featured events found for " + date.getMonthValue() + "/" + date.getDayOfMonth());
        }

        return featuredEvent;
      }
    }
  }

  private List<EventSummary> findAdditionalEvents(Connection connection, MonthDay date)
      throws SQLException {
    try (var statement = connection.prepareStatement(ADDITIONAL_SQL)) {
      statement.setInt(1, date.getMonthValue());
      statement.setInt(2, date.getDayOfMonth());

      try (var resultSet = statement.executeQuery()) {
        var additionalEvents = new ArrayList<EventSummary>();
        while (resultSet.next()) {
          additionalEvents.add(
              new EventSummary(
                  resultSet.getString("event_id"),
                  resultSet.getString("title"),
                  resultSet.getString("year_label"),
                  resultSet.getString("historical_date"),
                  resultSet.getString("date_note")));
        }
        return additionalEvents;
      }
    }
  }

  private static EventImage mapOptionalImage(ResultSet resultSet) throws SQLException {
    var imageUrl = resultSet.getString("image_url");
    if (imageUrl == null) {
      return null;
    }

    return new EventImage(
        URI.create(imageUrl),
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

  private static String displayDate(MonthDay date) {
    return date.atYear(2000).format(DISPLAY_DATE_FORMATTER);
  }
}
