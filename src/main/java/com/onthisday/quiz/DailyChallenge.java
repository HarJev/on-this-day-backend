package com.onthisday.quiz;

import static com.onthisday.quiz.QuizChecks.requireNonNull;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

public record DailyChallenge(
    LocalDate date, List<DailyChallengeQuestion> questions, int selectionVersion) {

  /** Balanced global selection; every assignment created before date-linked selection. */
  public static final int GLOBAL_SELECTION_VERSION = 1;

  /** Reserves reviewed questions linked to the date's curated events, then balances the rest. */
  public static final int DATE_LINKED_SELECTION_VERSION = 2;

  public DailyChallenge(LocalDate date, List<DailyChallengeQuestion> questions) {
    this(date, questions, GLOBAL_SELECTION_VERSION);
  }

  public DailyChallenge {
    date = requireNonNull(date, "date");
    if (selectionVersion != GLOBAL_SELECTION_VERSION && selectionVersion != DATE_LINKED_SELECTION_VERSION) {
      throw new InvalidQuizDefinitionException("selectionVersion is unsupported: " + selectionVersion);
    }
    if (questions == null) {
      throw new InvalidQuizDefinitionException("questions must not be null");
    }
    questions = List.copyOf(questions);
    if (questions.size() != 20) {
      throw new InvalidQuizDefinitionException("questions must contain exactly 20 entries");
    }

    var questionIds = new HashSet<String>();
    for (var index = 0; index < questions.size(); index++) {
      var question = questions.get(index);
      if (question.position() != index + 1) {
        throw new InvalidQuizDefinitionException(
            "questions must be ordered contiguously from position 1");
      }
      if (!questionIds.add(question.questionId())) {
        throw new InvalidQuizDefinitionException("question IDs must be distinct");
      }
    }
  }
}
