package com.onthisday.ingestion.editorial;

import com.onthisday.ingestion.CuratedContent;
import com.onthisday.ingestion.CuratedDailyEventsFile;
import com.onthisday.ingestion.CuratedEventsFile;
import com.onthisday.ingestion.quiz.CuratedQuizContent;
import com.onthisday.ingestion.quiz.LoadedQuizQuestionPack;
import com.onthisday.ingestion.quiz.QuizCollectionsFile;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Builds importer-safe canonical subsets selected by an approved editorial batch. */
public class EditorialBatchSelector {

  public CuratedContent selectHistorical(CuratedContent canonical, EditorialBatchManifest manifest) {
    Objects.requireNonNull(canonical, "canonical must not be null");
    Objects.requireNonNull(manifest, "manifest must not be null");
    var selectedDays =
        safe(canonical.dailyEventsFile().days()).stream()
            .filter(day -> safe(manifest.monthDays()).contains(key(day.month(), day.day())))
            .toList();
    if (selectedDays.size() != new HashSet<>(safe(manifest.monthDays())).size()) {
      throw new EditorialContentException("Editorial batch references a canonical day that does not exist.");
    }
    var eventIds = new HashSet<>(safe(manifest.eventIds()));
    for (var day : selectedDays) {
      eventIds.add(day.featuredEventId());
      eventIds.addAll(safe(day.additionalEventIds()));
    }
    var events = safe(canonical.eventsFile().events()).stream().filter(event -> eventIds.contains(event.id())).toList();
    if (events.size() != eventIds.size()) {
      throw new EditorialContentException("Editorial batch references a canonical event that does not exist.");
    }
    return new CuratedContent(new CuratedEventsFile(events), new CuratedDailyEventsFile(selectedDays));
  }

  public CuratedQuizContent selectQuiz(CuratedQuizContent canonical, EditorialBatchManifest manifest) {
    Objects.requireNonNull(canonical, "canonical must not be null");
    Objects.requireNonNull(manifest, "manifest must not be null");
    var filenames = new HashSet<>(safe(manifest.quizPackFilenames()));
    var packs =
        canonical.questionPacks().stream()
            .filter(pack -> filenames.contains(pack.relativeFilename()))
            .toList();
    if (packs.size() != filenames.size()) {
      throw new EditorialContentException("Editorial batch references a canonical quiz pack that does not exist.");
    }
    var collectionIds = new HashSet<String>();
    for (var pack : packs) {
      for (var question : safe(pack.file().questions())) {
        collectionIds.addAll(safe(question.collectionIds()));
      }
    }
    var collections =
        safe(canonical.collectionsFile().collections()).stream()
            .filter(collection -> collectionIds.contains(collection.id()))
            .toList();
    if (collections.size() != collectionIds.size()) {
      throw new EditorialContentException("Editorial batch references a canonical quiz collection that does not exist.");
    }
    return new CuratedQuizContent(new QuizCollectionsFile(1, collections), packs);
  }

  private static String key(int month, int day) {
    return String.format("%02d-%02d", month, day);
  }

  private static <T> List<T> safe(List<T> values) {
    return values == null ? List.of() : values;
  }
}
