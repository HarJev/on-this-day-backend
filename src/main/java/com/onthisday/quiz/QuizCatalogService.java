package com.onthisday.quiz;

import java.util.Comparator;
import java.util.Objects;

public class QuizCatalogService {

  private static final Comparator<QuizCollectionPublishedCount> COLLECTION_ORDER =
      Comparator.comparing((QuizCollectionPublishedCount count) -> count.collection().group().value())
          .thenComparing(count -> count.collection().name())
          .thenComparing(count -> count.collection().id());

  private final QuizCatalogRepository catalogRepository;

  public QuizCatalogService(QuizCatalogRepository catalogRepository) {
    this.catalogRepository = Objects.requireNonNull(catalogRepository, "catalogRepository must not be null");
  }

  public QuizCatalog getCatalog() {
    var counts = catalogRepository.loadPublishedCounts();
    var mixed = availability(counts.mixedPublishedQuestionCount());
    var collections =
        counts.collections().stream()
            .sorted(COLLECTION_ORDER)
            .map(
                count ->
                    new QuizCatalogCollection(
                        count.collection(), availability(count.publishedQuestionCount())))
            .toList();
    return new QuizCatalog(
        QuizRules.questionCounts(),
        new QuickPlayTimerDefaults(QuizRules.quickPlayTimerDefaultsSeconds()),
        mixed,
        collections);
  }

  private static QuizSelectionAvailability availability(int publishedQuestionCount) {
    return new QuizSelectionAvailability(
        publishedQuestionCount, QuizRules.supportedQuestionCounts(publishedQuestionCount));
  }
}
