package com.onthisday.ingestion.editorial;

import com.onthisday.ingestion.CuratedContent;
import com.onthisday.ingestion.CuratedDayJson;
import com.onthisday.ingestion.CuratedEventJson;
import com.onthisday.ingestion.editorial.JdbcContentSnapshotReader.ContentSnapshot;
import com.onthisday.ingestion.quiz.CuratedQuizContent;
import com.onthisday.ingestion.quiz.CuratedQuizQuestionJson;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Read-only editorial state: drafts, review status, canonical fingerprints, and (optionally) drift
 * between canonical JSON and an imported database. It never approves, promotes, or imports.
 */
public class ContentStatusReporter {

  public Map<String, Object> report(
      CuratedContent canonical,
      CuratedQuizContent quiz,
      List<BatchState> batches,
      Optional<ContentSnapshot> database) {
    var ledgerEntries = new HashMap<String, EditorialReviewEntry>();
    for (var batch : batches) {
      for (var entry : safe(batch.ledger() == null ? null : batch.ledger().entries())) {
        if (entry != null && entry.canonicalId() != null) {
          ledgerEntries.put(entry.canonicalId(), entry);
        }
      }
    }
    var events = safe(canonical.eventsFile().events());
    var days = safe(canonical.dailyEventsFile().days());
    var questions = quiz.questionPacks().stream().flatMap(pack -> safe(pack.file().questions()).stream()).toList();

    var canonicalFingerprints = new CanonicalFingerprints(events, days, questions);
    var report = new LinkedHashMap<String, Object>();
    report.put("reportVersion", 1);
    report.put("editorial", editorial(batches, canonicalFingerprints, ledgerEntries));
    report.put("canonical", canonical(canonicalFingerprints, questions));
    report.put("database", database.map(snapshot -> database(canonicalFingerprints, snapshot)).orElse(Map.of("checked", false)));
    report.put("quizAnswerPositions", answerPositions(questions));
    report.put("featuredImages", featuredImages(events, days, ledgerEntries));
    report.put("relatedEvents", relatedEvents(events, days, questions));
    return report;
  }

  private static Map<String, Object> editorial(
      List<BatchState> batches,
      CanonicalFingerprints canonical,
      Map<String, EditorialReviewEntry> ledgerEntries) {
    var statusTotals = new TreeMap<String, Integer>();
    var batchRows = new ArrayList<Map<String, Object>>();
    var draftEventCount = 0;
    var draftsAlreadyCanonical = new TreeSet<String>();
    for (var batch : batches) {
      var statusCounts = new TreeMap<String, Integer>();
      for (var entry : safe(batch.ledger() == null ? null : batch.ledger().entries())) {
        var status = entry == null || entry.reviewStatus() == null ? "unknown" : entry.reviewStatus();
        statusCounts.merge(status, 1, Integer::sum);
        statusTotals.merge(status, 1, Integer::sum);
      }
      for (var event : batch.draftEvents()) {
        if (canonical.events().containsKey(event.id())) {
          draftsAlreadyCanonical.add(event.id());
        }
      }
      draftEventCount += batch.draftEvents().size();
      var row = new LinkedHashMap<String, Object>();
      row.put("batchId", batch.batchId());
      row.put("reviewStatusCounts", statusCounts);
      row.put("draftEvents", batch.draftEvents().size());
      row.put("draftDays", batch.draftDays().stream().map(day -> ContentFingerprints.monthDay(day.month(), day.day())).toList());
      batchRows.add(row);
    }
    var result = new LinkedHashMap<String, Object>();
    result.put("reviewStatusCounts", statusTotals);
    result.put("draftEvents", draftEventCount);
    result.put("draftEventsAlreadyCanonical", List.copyOf(draftsAlreadyCanonical));
    result.put("canonicalEventsWithoutApprovedReview", withoutApproval(canonical.events().keySet(), ledgerEntries));
    result.put("canonicalQuestionsWithoutApprovedReview", withoutApproval(canonical.questions().keySet(), ledgerEntries));
    result.put("batches", batchRows);
    return result;
  }

  private static List<String> withoutApproval(
      Iterable<String> ids, Map<String, EditorialReviewEntry> ledgerEntries) {
    var missing = new ArrayList<String>();
    for (var id : ids) {
      var entry = ledgerEntries.get(id);
      if (entry == null || !"approved".equals(entry.reviewStatus())) {
        missing.add(id);
      }
    }
    return missing;
  }

  private static Map<String, Object> canonical(
      CanonicalFingerprints canonical, List<CuratedQuizQuestionJson> questions) {
    var byState = new TreeMap<String, Integer>();
    for (var question : questions) {
      byState.merge(String.valueOf(question.publicationState()), 1, Integer::sum);
    }
    var result = new LinkedHashMap<String, Object>();
    result.put("events", canonical.events().size());
    result.put("days", canonical.days().size());
    result.put("questionsByPublicationState", byState);
    result.put("fingerprints", fingerprints(canonical.events(), canonical.days(), canonical.questions()));
    return result;
  }

  private static Map<String, Object> database(CanonicalFingerprints canonical, ContentSnapshot snapshot) {
    var eventDrift = drift(canonical.events(), snapshot.events());
    var dayDrift = drift(canonical.days(), snapshot.days());
    var questionDrift = drift(canonical.questions(), snapshot.questions());
    var result = new LinkedHashMap<String, Object>();
    result.put("checked", true);
    result.put(
        "inSync",
        eventDrift.inSync() && dayDrift.inSync() && questionDrift.inSync());
    result.put("events", snapshot.events().size());
    result.put("days", snapshot.days().size());
    result.put("questions", snapshot.questions().size());
    result.put("fingerprints", fingerprints(snapshot.events(), snapshot.days(), snapshot.questions()));
    result.put("eventDrift", eventDrift.asMap());
    result.put("dayDrift", dayDrift.asMap());
    result.put("questionDrift", questionDrift.asMap());
    return result;
  }

  private static Map<String, Object> fingerprints(
      Map<String, String> events, Map<String, String> days, Map<String, String> questions) {
    var result = new LinkedHashMap<String, Object>();
    result.put("events", ContentFingerprints.aggregate(events));
    result.put("days", ContentFingerprints.aggregate(days));
    result.put("questions", ContentFingerprints.aggregate(questions));
    return result;
  }

  private static Drift drift(Map<String, String> canonical, Map<String, String> database) {
    var stale = new ArrayList<String>();
    var notImported = new ArrayList<String>();
    for (var entry : canonical.entrySet()) {
      var imported = database.get(entry.getKey());
      if (imported == null) {
        notImported.add(entry.getKey());
      } else if (!imported.equals(entry.getValue())) {
        stale.add(entry.getKey());
      }
    }
    var onlyInDatabase = database.keySet().stream().filter(id -> !canonical.containsKey(id)).toList();
    return new Drift(stale, notImported, onlyInDatabase);
  }

  /**
   * Authored correct-option positions. Play order is shuffled at serve time, so this is an
   * authoring-quality signal, not a user-visible leak.
   */
  private static Map<String, Object> answerPositions(List<CuratedQuizQuestionJson> questions) {
    var result = new TreeMap<String, Object>();
    for (var question : questions) {
      if (!"published".equals(question.publicationState()) || question.options() == null || question.correctOptionId() == null) {
        continue;
      }
      @SuppressWarnings("unchecked")
      var counts = (Map<String, Integer>) result.computeIfAbsent(question.type(), ignored -> new TreeMap<String, Integer>());
      var key =
          "true_false".equals(question.type())
              ? question.correctOptionId()
              : Integer.toString(indexOfOption(question) + 1);
      counts.merge(key, 1, Integer::sum);
    }
    return result;
  }

  private static int indexOfOption(CuratedQuizQuestionJson question) {
    var options = question.options();
    for (var index = 0; index < options.size(); index++) {
      if (question.correctOptionId().equals(options.get(index).id())) {
        return index;
      }
    }
    return -1;
  }

  private static Map<String, Object> featuredImages(
      List<CuratedEventJson> events, List<CuratedDayJson> days, Map<String, EditorialReviewEntry> ledgerEntries) {
    var eventsById = new HashMap<String, CuratedEventJson>();
    events.forEach(event -> eventsById.put(event.id(), event));
    var withImage = 0;
    var withoutImage = new ArrayList<Map<String, Object>>();
    for (var day : days) {
      var featured = eventsById.get(day.featuredEventId());
      if (featured != null && !safe(featured.images()).isEmpty()) {
        withImage += 1;
        continue;
      }
      var entry = ledgerEntries.get(day.featuredEventId());
      var row = new LinkedHashMap<String, Object>();
      row.put("monthDay", ContentFingerprints.monthDay(day.month(), day.day()));
      row.put("eventId", day.featuredEventId());
      row.put("imageRightsStatus", entry == null || entry.imageRightsStatus() == null ? "unknown" : entry.imageRightsStatus());
      withoutImage.add(row);
    }
    var result = new LinkedHashMap<String, Object>();
    result.put("daysWithFeaturedImage", withImage);
    result.put("daysWithoutFeaturedImage", withoutImage);
    return result;
  }

  /** Coverage of reviewed event-question links that date-linked Daily selection draws on. */
  private static Map<String, Object> relatedEvents(
      List<CuratedEventJson> events, List<CuratedDayJson> days, List<CuratedQuizQuestionJson> questions) {
    var eventIds = new HashMap<String, Boolean>();
    events.forEach(event -> eventIds.put(event.id(), true));
    var publishedLinksByEvent = new HashMap<String, Integer>();
    var linkedQuestions = 0;
    var unknownEventIds = new TreeSet<String>();
    for (var question : questions) {
      var related = safe(question.relatedEventIds());
      if (!related.isEmpty()) {
        linkedQuestions += 1;
      }
      for (var eventId : related) {
        if (!eventIds.containsKey(eventId)) {
          unknownEventIds.add(eventId);
        }
        if ("published".equals(question.publicationState())) {
          publishedLinksByEvent.merge(eventId, 1, Integer::sum);
        }
      }
    }
    var featuredWithLink = new ArrayList<String>();
    var featuredWithoutLink = new ArrayList<String>();
    for (var day : days) {
      var monthDay = ContentFingerprints.monthDay(day.month(), day.day());
      (publishedLinksByEvent.containsKey(day.featuredEventId()) ? featuredWithLink : featuredWithoutLink).add(monthDay);
    }
    var result = new LinkedHashMap<String, Object>();
    result.put("questionsWithRelatedEvents", linkedQuestions);
    result.put("featuredDaysWithLinkedQuestion", featuredWithLink);
    result.put("featuredDaysWithoutLinkedQuestion", featuredWithoutLink);
    result.put("unknownRelatedEventIds", List.copyOf(unknownEventIds));
    return result;
  }

  private static <T> List<T> safe(List<T> values) {
    return values == null ? List.of() : values;
  }

  /** One editorial batch: its ledger plus any drafts that sit outside {@code content/}. */
  public record BatchState(
      String batchId,
      EditorialReviewLedger ledger,
      List<CuratedEventJson> draftEvents,
      List<CuratedDayJson> draftDays) {
    public BatchState {
      draftEvents = draftEvents == null ? List.of() : List.copyOf(draftEvents);
      draftDays = draftDays == null ? List.of() : List.copyOf(draftDays);
    }
  }

  private record CanonicalFingerprints(
      Map<String, String> events, Map<String, String> days, Map<String, String> questions) {
    CanonicalFingerprints(
        List<CuratedEventJson> events, List<CuratedDayJson> days, List<CuratedQuizQuestionJson> questions) {
      this(eventFingerprints(events), dayFingerprints(days), questionFingerprints(questions));
    }

    private static Map<String, String> eventFingerprints(List<CuratedEventJson> events) {
      var result = new TreeMap<String, String>();
      events.forEach(event -> result.put(event.id(), ContentFingerprints.event(event)));
      return result;
    }

    private static Map<String, String> dayFingerprints(List<CuratedDayJson> days) {
      var result = new TreeMap<String, String>();
      days.forEach(day -> result.put(ContentFingerprints.monthDay(day.month(), day.day()), ContentFingerprints.day(day)));
      return result;
    }

    private static Map<String, String> questionFingerprints(List<CuratedQuizQuestionJson> questions) {
      var result = new TreeMap<String, String>();
      questions.forEach(question -> result.put(question.id(), ContentFingerprints.question(question)));
      return result;
    }
  }

  private record Drift(List<String> stale, List<String> notImported, List<String> onlyInDatabase) {
    boolean inSync() {
      return stale.isEmpty() && notImported.isEmpty() && onlyInDatabase.isEmpty();
    }

    Map<String, Object> asMap() {
      var result = new LinkedHashMap<String, Object>();
      result.put("stale", stale);
      result.put("notImported", notImported);
      result.put("onlyInDatabase", onlyInDatabase);
      return result;
    }
  }
}
