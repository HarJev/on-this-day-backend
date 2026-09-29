package com.onthisday.quiz;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.sql.DataSource;

public final class JdbcQuizEventLinkRepository implements QuizEventLinkRepository {
  private final DataSource dataSource;
  public JdbcQuizEventLinkRepository(DataSource dataSource) {
    this.dataSource = Objects.requireNonNull(dataSource);
  }

  @Override
  public Map<String, List<RelatedQuizEvent>> findByQuestionIds(List<String> ids) {
    ids = List.copyOf(ids);
    if (ids.isEmpty()) {
      return Map.of();
    }
    if (ids.size() > 20 || ids.stream().distinct().count() != ids.size()) {
      throw new InvalidQuizDefinitionException("Expected at most 20 distinct question IDs");
    }
    ids.forEach(id -> QuizChecks.requireSlug(id, "question id"));
    var sql = "SELECT link.question_id, event.event_id, event.title, event.year_label "
        + "FROM quiz_question_event link JOIN historical_event event ON event.event_id = link.event_id "
        + "WHERE link.question_id IN (" + String.join(",", Collections.nCopies(ids.size(), "?"))
        + ") ORDER BY link.question_id, event.event_id";
    try (var connection = dataSource.getConnection(); var statement = connection.prepareStatement(sql)) {
      for (int i = 0; i < ids.size(); i++) {
        statement.setString(i + 1, ids.get(i));
      }
      var links = new HashMap<String, List<RelatedQuizEvent>>();
      try (var rows = statement.executeQuery()) {
        while (rows.next()) {
          links.computeIfAbsent(rows.getString("question_id"), ignored -> new ArrayList<>())
              .add(new RelatedQuizEvent(rows.getString("event_id"), rows.getString("title"), rows.getString("year_label")));
        }
      }
      links.replaceAll((id, events) -> List.copyOf(events));
      return Map.copyOf(links);
    } catch (java.sql.SQLException | InvalidQuizDefinitionException exception) {
      throw new QuizUnavailableException("Related history is unavailable", exception);
    }
  }

  @Override
  public boolean hasPublishedQuestionForEvent(String eventId) {
    QuizChecks.requireSlug(eventId, "event id");
    var sql = "SELECT EXISTS (SELECT 1 FROM quiz_question_event link JOIN quiz_question question "
        + "ON question.question_id = link.question_id WHERE link.event_id = ? "
        + "AND question.publication_state = 'published')";
    try (var connection = dataSource.getConnection(); var statement = connection.prepareStatement(sql)) {
      statement.setString(1, eventId);
      try (var rows = statement.executeQuery()) {
        rows.next();
        return rows.getBoolean(1);
      }
    } catch (java.sql.SQLException exception) {
      throw new QuizUnavailableException("Related quiz availability is unavailable", exception);
    }
  }
}
