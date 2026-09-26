package com.onthisday.quiz;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class QuizCatalogServiceTest {

  @Test
  void derivesSupportedCountsTimersAndStableCollectionOrder() {
    var service =
        new QuizCatalogService(
            () ->
                new QuizCatalogCounts(
                    20,
                    List.of(
                        count("z-topic", "Z Topic", CollectionGroup.TOPIC, 19),
                        count("empty-period", "Empty Period", CollectionGroup.HISTORICAL_PERIOD, 0),
                        count("a-topic", "A Topic", CollectionGroup.TOPIC, 5),
                        count("nine-civilization", "Nine Civilization", CollectionGroup.CIVILIZATION, 9),
                        count("ten-conflict", "Ten Conflict", CollectionGroup.CONFLICT_OR_MOVEMENT, 10),
                        count("twenty-period", "Twenty Period", CollectionGroup.HISTORICAL_PERIOD, 20))));

    var catalog = service.getCatalog();

    assertEquals(List.of(5, 10, 20), catalog.questionCounts());
    assertEquals(List.of(5, 10, 20), catalog.mixed().supportedQuestionCounts());
    assertEquals(20, catalog.quickPlayTimerDefaults().secondsFor(QuestionType.MULTIPLE_CHOICE));
    assertEquals(20, catalog.quickPlayTimerDefaults().secondsFor(QuestionType.TRUE_FALSE));
    assertEquals(30, catalog.quickPlayTimerDefaults().secondsFor(QuestionType.IMAGE_IDENTIFICATION));
    assertEquals(45, catalog.quickPlayTimerDefaults().secondsFor(QuestionType.CHRONOLOGICAL_ORDERING));

    assertEquals(
        List.of(
            "nine-civilization",
            "ten-conflict",
            "empty-period",
            "twenty-period",
            "a-topic",
            "z-topic"),
        catalog.collections().stream().map(item -> item.collection().id()).toList());
    assertEquals(List.of(), catalog.collections().get(2).availability().supportedQuestionCounts());
    assertEquals(List.of(5), catalog.collections().get(4).availability().supportedQuestionCounts());
    assertEquals(List.of(5, 10), catalog.collections().get(5).availability().supportedQuestionCounts());
  }

  @Test
  void supportsCountsAtAllPublishedQuestionBoundaries() {
    assertEquals(List.of(), QuizRules.supportedQuestionCounts(0));
    assertEquals(List.of(), QuizRules.supportedQuestionCounts(4));
    assertEquals(List.of(5), QuizRules.supportedQuestionCounts(5));
    assertEquals(List.of(5), QuizRules.supportedQuestionCounts(9));
    assertEquals(List.of(5, 10), QuizRules.supportedQuestionCounts(10));
    assertEquals(List.of(5, 10), QuizRules.supportedQuestionCounts(19));
    assertEquals(List.of(5, 10, 20), QuizRules.supportedQuestionCounts(20));
  }

  private static QuizCollectionPublishedCount count(
      String id, String name, CollectionGroup group, int questionCount) {
    return new QuizCollectionPublishedCount(new QuizCollection(id, name, group), questionCount);
  }
}
