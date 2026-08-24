package com.onthisday.content;

import java.net.URI;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import javax.sql.DataSource;

public class JdbcTodayContentRepository implements TodayContentRepository {

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

  private final DataSource dataSource;

  public JdbcTodayContentRepository(DataSource dataSource) {
    this.dataSource = Objects.requireNonNull(dataSource, "dataSource must not be null");
  }

  @Override
  public TodayContent getTodayContent(MonthDay date) {
    Objects.requireNonNull(date, "date must not be null");

    try (var connection = dataSource.getConnection()) {
      var featuredEvent = findFeaturedEvent(connection.prepareStatement(FEATURED_SQL), date);
      var additionalEvents = findAdditionalEvents(connection.prepareStatement(ADDITIONAL_SQL), date);

      return new TodayContent(
          new ContentDate(date.getMonthValue(), date.getDayOfMonth(), displayDate(date)),
          featuredEvent,
          additionalEvents);
    } catch (SQLException exception) {
      throw new ContentUnavailableException("Could not load today content.", exception);
    }
  }

  private FeaturedEvent findFeaturedEvent(PreparedStatement statement, MonthDay date) throws SQLException {
    try (statement) {
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

  private List<EventSummary> findAdditionalEvents(PreparedStatement statement, MonthDay date) throws SQLException {
    try (statement) {
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
