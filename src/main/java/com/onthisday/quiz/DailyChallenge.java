package com.onthisday.quiz;

import static com.onthisday.quiz.QuizChecks.requireNonNull;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

public record DailyChallenge(LocalDate date, List<DailyChallengeQuestion> questions) {

  public DailyChallenge {
    date = requireNonNull(date, "date");
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
