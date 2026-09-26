package com.onthisday.ingestion.editorial;

import com.onthisday.ingestion.CuratedContent;
import com.onthisday.ingestion.CuratedDailyEventsFile;
import com.onthisday.ingestion.CuratedDayJson;
import com.onthisday.ingestion.CuratedEventJson;
import com.onthisday.ingestion.CuratedEventsFile;
import com.onthisday.ingestion.CuratedSourceJson;
import com.onthisday.ingestion.quiz.CuratedQuizCollectionJson;
import com.onthisday.ingestion.quiz.CuratedQuizContent;
import com.onthisday.ingestion.quiz.CuratedQuizOptionJson;
import com.onthisday.ingestion.quiz.CuratedQuizQuestionJson;
import com.onthisday.ingestion.quiz.CuratedQuizSourceJson;
import com.onthisday.ingestion.quiz.LoadedQuizQuestionPack;
import com.onthisday.ingestion.quiz.QuizCollectionsFile;
import com.onthisday.ingestion.quiz.QuizQuestionPackFile;
import java.util.ArrayList;
import java.util.List;

final class EditorialWorkflowFixtures {

  private EditorialWorkflowFixtures() {}

  static CuratedContent sevenDayHistoricalContent() {
    var events = new ArrayList<CuratedEventJson>();
    var days = new ArrayList<CuratedDayJson>();
    for (var day = 1; day <= 7; day++) {
      var eventIds = new ArrayList<String>();
      for (var position = 1; position <= 4; position++) {
        var id = "fixture-event-02-" + day + "-" + position;
        eventIds.add(id);
        events.add(
            new CuratedEventJson(
                id,
                "Fixture event " + day + "/" + position,
                "1900",
                "February " + day + ", 1900",
                null,
                "Fixture summary " + day + "/" + position + ".",
                "Fixture description " + day + "/" + position + ".",
                position == 1 ? "Fixture notification " + day : null,
                position == 1 ? "Fixture notification body " + day : null,
                List.of(new CuratedSourceJson("Fixture source", sourceUrl(id))),
                List.of()));
      }
      days.add(new CuratedDayJson(2, day, eventIds.getFirst(), eventIds.subList(1, 4), null));
    }
    return new CuratedContent(new CuratedEventsFile(events), new CuratedDailyEventsFile(days));
  }

  static CuratedQuizContent twentyQuestionQuizContent() {
    var questions = new ArrayList<CuratedQuizQuestionJson>();
    for (var number = 1; number <= 20; number++) {
      var id = "fixture-quiz-question-" + number;
      questions.add(
          new CuratedQuizQuestionJson(
              id,
              "multiple_choice",
              number % 3 == 0 ? "hard" : number % 2 == 0 ? "medium" : "easy",
              "published",
              "Fixture question " + number + "?",
              List.of(
                  new CuratedQuizOptionJson("a", "Answer A"),
                  new CuratedQuizOptionJson("b", "Answer B"),
                  new CuratedQuizOptionJson("c", "Answer C"),
                  new CuratedQuizOptionJson("d", "Answer D")),
              "a",
              null,
              null,
              null,
              "Fixture explanation " + number + ".",
              List.of(new CuratedQuizSourceJson("Fixture source", sourceUrl(id))),
              List.of("fixture-collection")));
    }
    return new CuratedQuizContent(
        new QuizCollectionsFile(
            1,
            List.of(
                new CuratedQuizCollectionJson(
                    "fixture-collection", "Fixture collection", "topic"))),
        List.of(
            new LoadedQuizQuestionPack(
                "questions/001-seven-day-fixture.json",
                new QuizQuestionPackFile(1, questions))));
  }

  static EditorialBatch approvedBatch() {
    var candidates = new ArrayList<EditorialCandidate>();
    var entries = new ArrayList<EditorialReviewEntry>();
    for (var event : sevenDayHistoricalContent().eventsFile().events()) {
      addApproved(candidates, entries, "event", event.id(), sourceUrl(event.id()), List.of("02-01"));
    }
    for (var question : twentyQuestionQuizContent().questionPacks().getFirst().file().questions()) {
      addApproved(candidates, entries, "quiz", question.id(), sourceUrl(question.id()), List.of("02-01", "02-02"));
    }
    return new EditorialBatch(
        new EditorialBatchManifest(
            1,
            "seven-day-fixture",
            "candidates.json",
            "review-ledger.json",
            List.of(),
            List.of("02-01", "02-02", "02-03", "02-04", "02-05", "02-06", "02-07"),
            List.of("questions/001-seven-day-fixture.json")),
        new EditorialCandidatesFile(1, "seven-day-fixture", candidates),
        new EditorialReviewLedger(1, "seven-day-fixture", entries));
  }

  private static void addApproved(
      List<EditorialCandidate> candidates,
      List<EditorialReviewEntry> entries,
      String kind,
      String id,
      String sourceUrl,
      List<String> calendarDays) {
    var candidateId = "candidate-" + id;
    candidates.add(
        new EditorialCandidate(candidateId, kind, id, "Fixture claim for " + id, List.of(sourceUrl)));
    entries.add(
        new EditorialReviewEntry(
            candidateId,
            id,
            "approved",
            "fixture-editor",
            "2026-09-17",
            List.of(
                new EditorialSourceCheck(
                    sourceUrl, "verified_supporting", "2026-09-17", "Fixture verification.")),
            "not_applicable",
            List.of("fixture-region"),
            List.of("fixture-era"),
            calendarDays,
            "Fixture approval."));
  }

  private static String sourceUrl(String id) {
    return "https://example.com/" + id;
  }
}
