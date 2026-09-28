package com.onthisday.ingestion.editorial;

import com.onthisday.ingestion.editorial.ContentFingerprints.Fields;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import javax.sql.DataSource;

/** Reads imported content back into the same fingerprints as {@link ContentFingerprints}. Read-only. */
public class JdbcContentSnapshotReader {

  private static final String EVENTS_SQL =
      """
      SELECT event_id, title, year_label, historical_date, date_note, summary, description,
             notification_title, notification_body
      FROM historical_event
      """;
  private static final String EVENT_SOURCES_SQL =
      "SELECT event_id, display_name, url FROM event_source ORDER BY event_id, display_order";
  private static final String EVENT_IMAGES_SQL =
      """
      SELECT event_id, is_primary, url, alt_text, source_name, source_url, creator, attribution,
             license_name, license_url
      FROM event_image
      ORDER BY event_id, display_order
      """;
  private static final String DAILY_EVENTS_SQL =
      "SELECT month, day, event_id, role FROM daily_event ORDER BY month, day, display_order";
  private static final String QUESTIONS_SQL =
      """
      SELECT question_id, question_type, difficulty, publication_state, prompt, explanation
      FROM quiz_question
      """;
  private static final String OPTIONS_SQL =
      """
      SELECT question_id, option_id, option_text, is_correct
      FROM quiz_option
      ORDER BY question_id, display_order
      """;
  private static final String ORDERING_ITEMS_SQL =
      """
      SELECT question_id, item_id, item_text, correct_position
      FROM quiz_ordering_item
      ORDER BY question_id, item_id
      """;
  private static final String IMAGES_SQL =
      """
      SELECT question_id, url, alt_text, source_name, source_url, attribution, creator,
             license_name, license_url
      FROM quiz_image
      """;
  private static final String QUIZ_SOURCES_SQL =
      "SELECT question_id, display_name, url FROM quiz_source ORDER BY question_id, display_order";
  private static final String COLLECTIONS_SQL =
      """
      SELECT question_id, collection_id
      FROM quiz_question_collection
      ORDER BY question_id, collection_id
      """;

  private final DataSource dataSource;

  public JdbcContentSnapshotReader(DataSource dataSource) {
    this.dataSource = Objects.requireNonNull(dataSource, "dataSource must not be null");
  }

  public ContentSnapshot read() {
    try (var connection = dataSource.getConnection()) {
      connection.setReadOnly(true);
      connection.setAutoCommit(false);
      try {
        var snapshot = new ContentSnapshot(events(connection), days(connection), questions(connection));
        connection.commit();
        return snapshot;
      } catch (SQLException | RuntimeException exception) {
        connection.rollback();
        throw exception;
      }
    } catch (SQLException exception) {
      throw new EditorialContentException("Could not read imported content from the database.", exception);
    }
  }

  private static Map<String, String> events(Connection connection) throws SQLException {
    var base = new TreeMap<String, Fields>();
    query(
        connection,
        EVENTS_SQL,
        row ->
            base.put(
                row.getString(1),
                new Fields()
                    .add(
                        row.getString(1),
                        row.getString(2),
                        row.getString(3),
                        row.getString(4),
                        row.getString(5),
                        row.getString(6),
                        row.getString(7),
                        row.getString(8),
                        row.getString(9))));
    var sources = children(connection, EVENT_SOURCES_SQL, row -> List.of(row.getString(2), row.getString(3)));
    var images =
        children(
            connection,
            EVENT_IMAGES_SQL,
            row ->
                List.of(
                    Boolean.toString(row.getBoolean(2)),
                    nullable(row.getString(3)),
                    nullable(row.getString(4)),
                    nullable(row.getString(5)),
                    nullable(row.getString(6)),
                    nullable(row.getString(7)),
                    nullable(row.getString(8)),
                    nullable(row.getString(9)),
                    nullable(row.getString(10))));
    var fingerprints = new TreeMap<String, String>();
    for (var entry : base.entrySet()) {
      var fields = entry.getValue();
      fields.add("sources");
      sources.getOrDefault(entry.getKey(), List.of()).forEach(fields::addAll);
      fields.add("images");
      images.getOrDefault(entry.getKey(), List.of()).forEach(fields::addAll);
      fingerprints.put(entry.getKey(), fields.hash());
    }
    return fingerprints;
  }

  private static Map<String, String> days(Connection connection) throws SQLException {
    var days = new TreeMap<String, List<String>>();
    query(
        connection,
        DAILY_EVENTS_SQL,
        row -> {
          var key = ContentFingerprints.monthDay(row.getInt(1), row.getInt(2));
          var eventIds = days.computeIfAbsent(key, ignored -> new ArrayList<>());
          if ("featured".equals(row.getString(4))) {
            eventIds.add(0, row.getString(3));
          } else {
            eventIds.add(row.getString(3));
          }
        });
    var fingerprints = new TreeMap<String, String>();
    for (var entry : days.entrySet()) {
      fingerprints.put(entry.getKey(), new Fields().add(entry.getKey()).addAll(entry.getValue()).hash());
    }
    return fingerprints;
  }

  private static Map<String, String> questions(Connection connection) throws SQLException {
    var base = new TreeMap<String, Fields>();
    query(
        connection,
        QUESTIONS_SQL,
        row ->
            base.put(
                row.getString(1),
                new Fields()
                    .add(
                        row.getString(1),
                        row.getString(2),
                        row.getString(3),
                        row.getString(4),
                        row.getString(5),
                        row.getString(6))));
    var options =
        children(
            connection,
            OPTIONS_SQL,
            row -> List.of(row.getString(2), row.getString(3), Boolean.toString(row.getBoolean(4))));
    var items =
        children(
            connection,
            ORDERING_ITEMS_SQL,
            row -> List.of(row.getString(2), row.getString(3), Integer.toString(row.getInt(4))));
    var images =
        children(
            connection,
            IMAGES_SQL,
            row ->
                List.of(
                    nullable(row.getString(2)),
                    nullable(row.getString(3)),
                    nullable(row.getString(4)),
                    nullable(row.getString(5)),
                    nullable(row.getString(6)),
                    nullable(row.getString(7)),
                    nullable(row.getString(8)),
                    nullable(row.getString(9))));
    var sources = children(connection, QUIZ_SOURCES_SQL, row -> List.of(row.getString(2), row.getString(3)));
    var collections = children(connection, COLLECTIONS_SQL, row -> List.of(row.getString(2)));
    var fingerprints = new TreeMap<String, String>();
    for (var entry : base.entrySet()) {
      var id = entry.getKey();
      var fields = entry.getValue();
      fields.add("options");
      options.getOrDefault(id, List.of()).forEach(fields::addAll);
      fields.add("items");
      items.getOrDefault(id, List.of()).forEach(fields::addAll);
      fields.add("image");
      images.getOrDefault(id, List.of()).forEach(fields::addAll);
      fields.add("sources");
      sources.getOrDefault(id, List.of()).forEach(fields::addAll);
      fields.add("collections");
      collections.getOrDefault(id, List.of()).forEach(fields::addAll);
      fingerprints.put(id, fields.hash());
    }
    return fingerprints;
  }

  /** Groups ordered child rows by the parent ID in column 1. */
  private static Map<String, List<List<String>>> children(
      Connection connection, String sql, RowMapper<List<String>> mapper) throws SQLException {
    var grouped = new LinkedHashMap<String, List<List<String>>>();
    query(
        connection,
        sql,
        row -> grouped.computeIfAbsent(row.getString(1), ignored -> new ArrayList<>()).add(mapper.map(row)));
    return grouped;
  }

  private static void query(Connection connection, String sql, RowConsumer consumer) throws SQLException {
    try (var statement = connection.prepareStatement(sql);
        var rows = statement.executeQuery()) {
      while (rows.next()) {
        consumer.accept(rows);
      }
    }
  }

  /** {@link List#of} rejects nulls; the marker matches the one {@link Fields} writes for null. */
  private static String nullable(String value) {
    return value == null ? "\u0000" : value;
  }

  @FunctionalInterface
  private interface RowConsumer {
    void accept(ResultSet row) throws SQLException;
  }

  @FunctionalInterface
  private interface RowMapper<T> {
    T map(ResultSet row) throws SQLException;
  }

  /** Per-record fingerprints keyed by event ID, {@code MM-DD}, and question ID. */
  public record ContentSnapshot(
      Map<String, String> events, Map<String, String> days, Map<String, String> questions) {
    public ContentSnapshot {
      events = Collections.unmodifiableMap(new TreeMap<>(events));
      days = Collections.unmodifiableMap(new TreeMap<>(days));
      questions = Collections.unmodifiableMap(new TreeMap<>(questions));
    }
  }
}
