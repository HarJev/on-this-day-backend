package com.onthisday.quiz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

class QuizCandidateSelectorTest {

  private final QuizCandidateSelector selector = new QuizCandidateSelector();

  @ParameterizedTest
  @ValueSource(ints = {5, 10, 20})
  void meetsExactTypeAndDifficultyTargetsWhenTheMatrixPermits(int questionCount) {
    var selected = selector.select(QuizTestFixtures.balancedCandidates(1), questionCount);

    assertEquals(questionCount, selected.size());
    assertEquals(questionCount, new HashSet<>(selected).size());
    assertEquals(QuizRules.questionTypeTargets(questionCount), countsByType(selected));
    assertEquals(QuizRules.difficultyTargets(questionCount), countsByDifficulty(selected));
  }

  @Test
  void usesDeterministicBestEffortForSparseBuckets() {
    var candidates = new ArrayList<QuizQuestionCandidate>();
    for (var index = 1; index <= 10; index++) {
      candidates.add(
          new QuizQuestionCandidate(
              "medium-choice-" + index,
              QuestionType.MULTIPLE_CHOICE,
              QuizDifficulty.MEDIUM));
    }
    candidates.add(
        new QuizQuestionCandidate(
            "hard-ordering",
            QuestionType.CHRONOLOGICAL_ORDERING,
            QuizDifficulty.HARD));

    var first = selector.select(candidates, 5);
    var second = selector.select(candidates, 5);

    assertEquals(first, second);
    assertEquals(5, first.size());
    assertEquals("hard-ordering", first.getLast().questionId());
    assertEquals(5, new HashSet<>(first).size());
  }

  @Test
  void balancesAnAdditionalStageAgainstItsEstablishedPrefix() {
    var candidates = QuizTestFixtures.balancedCandidates(1);
    var firstFive = selector.select(candidates, 5);
    var remaining = candidates.stream().filter(candidate -> !firstFive.contains(candidate)).toList();

    var additional = selector.selectAdditional(remaining, firstFive, 10);
    var ten = new ArrayList<>(firstFive);
    ten.addAll(additional);

    assertEquals(5, additional.size());
    assertEquals(QuizRules.questionTypeTargets(10), countsByType(ten));
    assertEquals(QuizRules.difficultyTargets(10), countsByDifficulty(ten));
  }

  @Test
  void rejectsUnsupportedInsufficientDuplicateAndOverlappingInputs() {
    var candidates = QuizTestFixtures.balancedCandidates(1);

    assertThrows(InvalidQuizRequestException.class, () -> selector.select(candidates, 6));
    assertThrows(
        InsufficientQuizQuestionsException.class,
        () -> selector.select(candidates.subList(0, 4), 5));
    assertThrows(
        InvalidQuizDefinitionException.class,
        () -> selector.select(List.of(candidates.getFirst(), candidates.getFirst()), 5));
    assertThrows(
        InvalidQuizDefinitionException.class,
        () -> selector.selectAdditional(candidates, List.of(candidates.getFirst()), 5));
  }

  private static EnumMap<QuestionType, Integer> countsByType(
      List<QuizQuestionCandidate> candidates) {
    var counts = new EnumMap<QuestionType, Integer>(QuestionType.class);
    for (var type : QuestionType.values()) {
      counts.put(type, 0);
    }
    candidates.forEach(candidate -> counts.merge(candidate.type(), 1, Integer::sum));
    return counts;
  }

  private static EnumMap<QuizDifficulty, Integer> countsByDifficulty(
      List<QuizQuestionCandidate> candidates) {
    var counts = new EnumMap<QuizDifficulty, Integer>(QuizDifficulty.class);
    for (var difficulty : QuizDifficulty.values()) {
      counts.put(difficulty, 0);
    }
    candidates.forEach(candidate -> counts.merge(candidate.difficulty(), 1, Integer::sum));
    return counts;
  }
}
