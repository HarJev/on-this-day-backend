package com.onthisday.quiz;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

public class QuizQuestionPresenter {

  public PlayableQuizQuestion present(QuizQuestion question, RandomGenerator random) {
    if (question == null || random == null) {
      throw new InvalidQuizDefinitionException("question and random must not be null");
    }
    var ids = canonicalIds(question);
    if (!(question instanceof TrueFalseQuestion)) {
      ids = shuffled(ids, random);
    }
    return new PlayableQuizQuestion(question, avoidCorrectChronologicalOrder(question, ids));
  }

  public PlayableQuizQuestion presentDaily(QuizQuestion question, LocalDate date) {
    if (question == null || date == null) {
      throw new InvalidQuizDefinitionException("question and date must not be null");
    }
    var ids = canonicalIds(question);
    if (!(question instanceof TrueFalseQuestion)) {
      ids = DeterministicQuizOrder.presentationIds(date, question.id(), ids);
    }
    return new PlayableQuizQuestion(question, avoidCorrectChronologicalOrder(question, ids));
  }

  private static List<String> canonicalIds(QuizQuestion question) {
    return switch (question) {
      case MultipleChoiceQuestion choice -> choice.options().stream().map(QuizOption::id).toList();
      case TrueFalseQuestion trueFalse -> trueFalse.options().stream().map(QuizOption::id).toList();
      case ImageIdentificationQuestion image -> image.options().stream().map(QuizOption::id).toList();
      case ChronologicalOrderingQuestion ordering ->
          ordering.items().stream()
              .sorted(java.util.Comparator.comparingInt(ChronologicalOrderingItem::correctPosition))
              .map(ChronologicalOrderingItem::id)
              .toList();
    };
  }

  private static List<String> shuffled(List<String> values, RandomGenerator random) {
    var shuffled = new ArrayList<>(values);
    for (var index = shuffled.size() - 1; index > 0; index--) {
      var swapIndex = random.nextInt(index + 1);
      var value = shuffled.get(index);
      shuffled.set(index, shuffled.get(swapIndex));
      shuffled.set(swapIndex, value);
    }
    return List.copyOf(shuffled);
  }

  private static List<String> avoidCorrectChronologicalOrder(
      QuizQuestion question, List<String> ids) {
    if (!(question instanceof ChronologicalOrderingQuestion) || !ids.equals(canonicalIds(question))) {
      return ids;
    }
    var rotated = new ArrayList<>(ids);
    var first = rotated.removeFirst();
    rotated.add(first);
    return List.copyOf(rotated);
  }
}
