package com.onthisday.quiz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class DailyChallengeTest {

  @Test
  void acceptsTwentyDistinctOrderedQuestionReferencesAndCopiesThem() {
    var mutableQuestions = new ArrayList<>(questions(20));
    var challenge = new DailyChallenge(LocalDate.of(2026, 8, 24), mutableQuestions);

    mutableQuestions.clear();

    assertEquals(20, challenge.questions().size());
    assertEquals("question-1", challenge.questions().getFirst().questionId());
    assertThrows(UnsupportedOperationException.class, () -> challenge.questions().clear());
  }

  @Test
  void rejectsWrongQuestionCount() {
    assertThrows(
        InvalidQuizDefinitionException.class,
        () -> new DailyChallenge(LocalDate.of(2026, 8, 24), questions(19)));
  }

  @Test
  void rejectsDuplicateQuestionIds() {
    var questions = new ArrayList<>(questions(20));
    questions.set(19, new DailyChallengeQuestion(20, "question-1"));

    assertThrows(
        InvalidQuizDefinitionException.class,
        () -> new DailyChallenge(LocalDate.of(2026, 8, 24), questions));
  }

  @Test
  void rejectsQuestionsThatAreNotSuppliedInContiguousOrder() {
    var questions = new ArrayList<>(questions(20));
    questions.set(0, new DailyChallengeQuestion(2, "replacement-question"));

    assertThrows(
        InvalidQuizDefinitionException.class,
        () -> new DailyChallenge(LocalDate.of(2026, 8, 24), questions));
  }

  private static List<DailyChallengeQuestion> questions(int count) {
    var questions = new ArrayList<DailyChallengeQuestion>();
    for (var position = 1; position <= count; position++) {
      questions.add(new DailyChallengeQuestion(position, "question-" + position));
    }
    return questions;
  }
}
