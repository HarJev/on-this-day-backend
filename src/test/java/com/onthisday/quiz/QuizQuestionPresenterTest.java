package com.onthisday.quiz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class QuizQuestionPresenterTest {

  private final QuizQuestionPresenter presenter = new QuizQuestionPresenter();

  @Test
  void validatesPresentationOrderAsAnExactPermutation() {
    var question =
        QuizTestFixtures.question(
            new QuizQuestionCandidate(
                "choice-question", QuestionType.MULTIPLE_CHOICE, QuizDifficulty.EASY));

    assertThrows(
        InvalidQuizDefinitionException.class,
        () -> new PlayableQuizQuestion(question, List.of("option-1", "option-2")));
    assertThrows(
        InvalidQuizDefinitionException.class,
        () ->
            new PlayableQuizQuestion(
                question, List.of("option-1", "option-2", "option-3", "unknown")));
    assertThrows(
        InvalidQuizDefinitionException.class,
        () ->
            new PlayableQuizQuestion(
                question, List.of("option-1", "option-2", "option-2", "option-4")));
  }

  @Test
  void trueFalseAlwaysUsesCanonicalPresentationOrder() {
    var question =
        QuizTestFixtures.question(
            new QuizQuestionCandidate(
                "boolean-question", QuestionType.TRUE_FALSE, QuizDifficulty.EASY));

    var playable = presenter.present(question, new Random(7));

    assertEquals(List.of("true", "false"), playable.presentationOrderIds());
    assertThrows(
        InvalidQuizDefinitionException.class,
        () -> new PlayableQuizQuestion(question, List.of("false", "true")));
  }

  @Test
  void quickPlayUsesInjectedRandomnessWithoutMutatingCanonicalOptions() {
    var question =
        QuizTestFixtures.question(
            new QuizQuestionCandidate(
                "choice-question", QuestionType.MULTIPLE_CHOICE, QuizDifficulty.EASY));

    var playable = presenter.present(question, new Random(7));

    assertEquals(
        List.of("option-1", "option-2", "option-3", "option-4"),
        ((MultipleChoiceQuestion) question).options().stream().map(QuizOption::id).toList());
    assertEquals(List.of("option-1", "option-2", "option-4", "option-3"), playable.presentationOrderIds());
  }

  @Test
  void chronologicalPresentationCanNeverEqualTheCorrectOrder() {
    var question =
        QuizTestFixtures.question(
            new QuizQuestionCandidate(
                "ordering-question", QuestionType.CHRONOLOGICAL_ORDERING, QuizDifficulty.MEDIUM));
    var alwaysKeepsOrder = new Random(0) {
      @Override
      public int nextInt(int bound) {
        return bound - 1;
      }
    };

    var playable = presenter.present(question, alwaysKeepsOrder);

    assertNotEquals(
        List.of("item-1", "item-2", "item-3", "item-4"),
        playable.presentationOrderIds());
    assertEquals(
        List.of("item-2", "item-3", "item-4", "item-1"),
        playable.presentationOrderIds());
  }

  @Test
  void dailyPresentationIsStableForDateAndQuestion() {
    var question =
        QuizTestFixtures.question(
            new QuizQuestionCandidate(
                "daily-choice", QuestionType.MULTIPLE_CHOICE, QuizDifficulty.MEDIUM));
    var date = LocalDate.of(2026, 9, 7);

    assertEquals(
        presenter.presentDaily(question, date), presenter.presentDaily(question, date));
    assertNotEquals(
        presenter.presentDaily(question, date).presentationOrderIds(),
        presenter.presentDaily(question, date.plusDays(1)).presentationOrderIds());
  }
}
