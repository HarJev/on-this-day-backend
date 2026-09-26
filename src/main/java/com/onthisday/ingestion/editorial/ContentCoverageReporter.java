package com.onthisday.ingestion.editorial;

import com.onthisday.ingestion.CuratedContent;
import com.onthisday.ingestion.quiz.CuratedQuizContent;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/** Produces an audit report; unknown review data remains visible rather than being inferred. */
public class ContentCoverageReporter {

  public Map<String, Object> report(
      CuratedContent historicalContent, CuratedQuizContent quizContent, List<EditorialReviewLedger> ledgers) {
    var entries = ledgerEntries(ledgers);
    var report = new LinkedHashMap<String, Object>();
    report.put("reportVersion", 1);
    report.put("historical", historical(historicalContent, entries));
    report.put("quiz", quiz(quizContent, entries));
    return report;
  }

  private Map<String, Object> historical(CuratedContent content, Map<String, EditorialReviewEntry> entries) {
    var days = content.dailyEventsFile().days() == null ? List.<com.onthisday.ingestion.CuratedDayJson>of() : content.dailyEventsFile().days();
    var dayByKey = days.stream().collect(Collectors.toMap(day -> dayKey(day.month(), day.day()), day -> day, (left, right) -> left));
    var missingDays = new ArrayList<String>();
    var belowFloor = new ArrayList<Map<String, Object>>();
    var fiveOrMore = new ArrayList<String>();
    for (var day : allLeapYearDays()) {
      var entry = dayByKey.get(day);
      if (entry == null) {
        missingDays.add(day);
        continue;
      }
      var total = 1 + (entry.additionalEventIds() == null ? 0 : entry.additionalEventIds().size());
      if (total < 4) {
        belowFloor.add(belowFloorDay(day, total, entry.editorialException()));
      }
      if (total >= 5) {
        fiveOrMore.add(day);
      }
    }
    var events = content.eventsFile().events() == null ? List.<com.onthisday.ingestion.CuratedEventJson>of() : content.eventsFile().events();
    var sourceStatuses = new ArrayList<Map<String, Object>>();
    var notificationCopy = new ArrayList<Map<String, Object>>();
    var imageRights = new ArrayList<Map<String, Object>>();
    var duplicateCandidates = new TreeMap<String, List<String>>();
    for (var event : events) {
      var ledger = entries.get(event.id());
      for (var source : safe(event.sources())) {
        sourceStatuses.add(sourceStatus("event", event.id(), source.url(), ledger));
      }
      for (var image : event.images() == null ? List.<com.onthisday.ingestion.CuratedImageJson>of() : event.images()) {
        imageRights.add(Map.of("kind", "event", "id", event.id(), "url", image.url(), "status", imageStatus(ledger)));
      }
      duplicateCandidates.computeIfAbsent(normalized(event.title()) + "|" + normalized(event.historicalDate()), ignored -> new ArrayList<>()).add(event.id());
    }
    for (var day : days) {
      var featured = events.stream().filter(event -> event.id().equals(day.featuredEventId())).findFirst().orElse(null);
      notificationCopy.add(Map.of("monthDay", dayKey(day.month(), day.day()), "eventId", day.featuredEventId(), "complete", featured != null && !blank(featured.notificationTitle()) && !blank(featured.notificationBody())));
    }
    return Map.of(
        "missingMonthDays", missingDays,
        "belowFourTotalEvents", belowFloor,
        "fiveOrMoreTotalEvents", fiveOrMore,
        "sourceLinks", sourceStatuses,
        "potentialDuplicateEvents", duplicateCandidates.entrySet().stream().filter(entry -> entry.getValue().size() > 1).map(entry -> Map.of("normalizedKey", entry.getKey(), "eventIds", entry.getValue())).toList(),
        "featuredNotificationCopy", notificationCopy,
        "imageRights", imageRights);
  }

  private Map<String, Object> quiz(CuratedQuizContent content, Map<String, EditorialReviewEntry> entries) {
    var questions = content.questionPacks().stream().flatMap(pack -> safe(pack.file().questions()).stream()).filter(question -> "published".equals(question.publicationState())).toList();
    var sourceStatuses = new ArrayList<Map<String, Object>>();
    var imageRights = new ArrayList<Map<String, Object>>();
    for (var question : questions) {
      var ledger = entries.get(question.id());
      for (var source : safe(question.sources())) {
        sourceStatuses.add(sourceStatus("quiz", question.id(), source.url(), ledger));
      }
      if (question.image() != null) {
        imageRights.add(Map.of("kind", "quiz", "id", question.id(), "url", question.image().url(), "status", imageStatus(ledger)));
      }
    }
    var regions = countTags(questions.stream().map(question -> entries.get(question.id())).toList(), true);
    var eras = countTags(questions.stream().map(question -> entries.get(question.id())).toList(), false);
    return Map.of(
        "publishedByType", count(questions.stream().map(question -> question.type()).toList()),
        "publishedByDifficulty", count(questions.stream().map(question -> question.difficulty()).toList()),
        "publishedByCollection", count(questions.stream().flatMap(question -> safe(question.collectionIds()).stream()).toList()),
        "publishedByRegion", regions,
        "publishedByEra", eras,
        "sourceLinks", sourceStatuses,
        "imageRights", imageRights,
        "adjacentDayQuestionOverlap", adjacentOverlap(entries));
  }

  private static List<Map<String, Object>> adjacentOverlap(Map<String, EditorialReviewEntry> entries) {
    var byDay = new TreeMap<String, List<String>>();
    for (var entry : entries.values()) {
      for (var day : safe(entry.calendarDays())) {
        byDay.computeIfAbsent(day, ignored -> new ArrayList<>()).add(entry.canonicalId());
      }
    }
    var overlap = new ArrayList<Map<String, Object>>();
    for (var day : byDay.keySet()) {
      var next = nextDay(day);
      var shared = new ArrayList<>(byDay.get(day));
      shared.retainAll(byDay.getOrDefault(next, List.of()));
      if (!shared.isEmpty()) {
        shared.sort(Comparator.naturalOrder());
        overlap.add(Map.of("firstDay", day, "secondDay", next, "questionIds", shared));
      }
    }
    return overlap;
  }

  private static Map<String, Integer> countTags(List<EditorialReviewEntry> entries, boolean regions) {
    var tags = entries.stream().filter(java.util.Objects::nonNull).flatMap(entry -> (regions ? safe(entry.regions()) : safe(entry.eras())).stream()).toList();
    var counts = count(tags);
    if (counts.isEmpty()) {
      return Map.of("unknown", 0);
    }
    return counts;
  }

  private static Map<String, Integer> count(List<String> values) {
    var counts = new TreeMap<String, Integer>();
    for (var value : values) {
      counts.merge(value, 1, Integer::sum);
    }
    return counts;
  }

  private static Map<String, Object> sourceStatus(String kind, String id, String url, EditorialReviewEntry entry) {
    var status = "unknown";
    if (entry != null) {
      for (var check : safe(entry.sourceChecks())) {
        if (url.equals(check.url())) {
          status = check.status();
          break;
        }
      }
    }
    return Map.of("kind", kind, "id", id, "url", url, "status", status);
  }

  private static Map<String, Object> belowFloorDay(
      String monthDay, int totalEvents, String editorialException) {
    var result = new LinkedHashMap<String, Object>();
    result.put("monthDay", monthDay);
    result.put("totalEvents", totalEvents);
    result.put("editorialException", editorialException);
    return result;
  }

  private static String imageStatus(EditorialReviewEntry entry) {
    return entry == null || entry.imageRightsStatus() == null ? "unknown" : entry.imageRightsStatus();
  }

  private static Map<String, EditorialReviewEntry> ledgerEntries(List<EditorialReviewLedger> ledgers) {
    var entries = new HashMap<String, EditorialReviewEntry>();
    for (var ledger : safe(ledgers)) {
      for (var entry : ledger == null ? List.<EditorialReviewEntry>of() : safe(ledger.entries())) {
        if (entry != null && entry.canonicalId() != null) {
          entries.put(entry.canonicalId(), entry);
        }
      }
    }
    return entries;
  }

  private static List<String> allLeapYearDays() {
    var days = new ArrayList<String>();
    var current = MonthDay.of(1, 1);
    for (var index = 0; index < 366; index++) {
      days.add(String.format("%02d-%02d", current.getMonthValue(), current.getDayOfMonth()));
      current = current.equals(MonthDay.of(12, 31)) ? MonthDay.of(1, 1) : MonthDay.from(current.atYear(2000).plusDays(1));
    }
    return days;
  }

  private static String nextDay(String value) {
    var day = MonthDay.of(Integer.parseInt(value.substring(0, 2)), Integer.parseInt(value.substring(3, 5)));
    var next = day.equals(MonthDay.of(12, 31)) ? MonthDay.of(1, 1) : MonthDay.from(day.atYear(2000).plusDays(1));
    return String.format("%02d-%02d", next.getMonthValue(), next.getDayOfMonth());
  }

  private static String dayKey(int month, int day) {
    return String.format("%02d-%02d", month, day);
  }

  private static String normalized(String value) {
    return value == null ? "" : value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ").trim();
  }

  private static boolean blank(String value) {
    return value == null || value.isBlank();
  }

  private static <T> List<T> safe(List<T> values) {
    return values == null ? List.of() : values;
  }
}
