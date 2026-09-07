package com.onthisday.quiz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class QuizCollectionTest {

  @Test
  void acceptsFlatGroupedCollectionAndCandidate() {
    var collection =
        new QuizCollection("punic-wars", "Punic Wars", CollectionGroup.CONFLICT_OR_MOVEMENT);
    var candidate =
        new QuizQuestionCandidate(
            "battle-of-cannae", QuestionType.MULTIPLE_CHOICE, QuizDifficulty.MEDIUM);

    assertEquals("conflict_or_movement", collection.group().value());
    assertEquals("multiple_choice", candidate.type().value());
    assertEquals("medium", candidate.difficulty().value());
  }

  @Test
  void rejectsInvalidCollectionOrCandidate() {
    assertThrows(
        InvalidQuizDefinitionException.class,
        () -> new QuizCollection("Punic Wars", "Punic Wars", CollectionGroup.TOPIC));
    assertThrows(
        InvalidQuizDefinitionException.class,
        () -> new QuizQuestionCandidate(" ", QuestionType.TRUE_FALSE, QuizDifficulty.EASY));
  }

  @Test
  void parsesPersistedEnumValues() {
    assertEquals(QuestionType.IMAGE_IDENTIFICATION, QuestionType.fromValue("image_identification"));
    assertEquals(QuizDifficulty.HARD, QuizDifficulty.fromValue("hard"));
    assertEquals(QuestionPublicationState.RETIRED, QuestionPublicationState.fromValue("retired"));
    assertEquals(CollectionGroup.HISTORICAL_PERIOD, CollectionGroup.fromValue("historical_period"));
    assertThrows(InvalidQuizDefinitionException.class, () -> QuestionType.fromValue("essay"));
  }
}
