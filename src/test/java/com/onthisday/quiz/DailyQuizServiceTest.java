package com.onthisday.quiz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DailyQuizServiceTest {

  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2026-09-08T00:30:00Z"), ZoneOffset.UTC);

  @Test
  void createsAllTwentyOnceAndLoadsOnlyRequestedStablePrefixes() {
    var candidates = QuizTestFixtures.balancedCandidates(1);
    var challengeRepository = new FakeDailyChallengeRepository();
    var questionRepository = new FakeQuestionRepository(candidates);
    var service = service(questionRepository, challengeRepository);

    var five = service.getQuiz(ZoneId.of("America/Jamaica"), 5);
    var ten = service.getQuiz(ZoneId.of("America/Jamaica"), 10);
    var twenty = service.getQuiz(ZoneId.of("America/Jamaica"), 20);

    assertEquals(LocalDate.of(2026, 9, 7), five.date());
    assertEquals(20, challengeRepository.challenge.questions().size());
    assertEquals(1, challengeRepository.insertCalls);
    assertEquals(List.of(5, 10, 20), questionRepository.aggregateLoadSizes);
    assertBalancedPrefix(challengeRepository.challenge, candidates, 5);
    assertBalancedPrefix(challengeRepository.challenge, candidates, 10);
    assertBalancedPrefix(challengeRepository.challenge, candidates, 20);
    assertEquals(
        twenty.questions().subList(0, 5).stream().map(item -> item.question().id()).toList(),
        five.questions().stream().map(item -> item.question().id()).toList());
    assertEquals(
        twenty.questions().subList(0, 10).stream().map(item -> item.question().id()).toList(),
        ten.questions().stream().map(item -> item.question().id()).toList());
  }

  @Test
  void selectionAndPresentationAreDeterministicForTheDate() {
    var candidates = QuizTestFixtures.balancedCandidates(1);

    var first = service(new FakeQuestionRepository(candidates), new FakeDailyChallengeRepository())
        .getQuiz(ZoneId.of("America/Jamaica"), 20);
    var second = service(new FakeQuestionRepository(candidates), new FakeDailyChallengeRepository())
        .getQuiz(ZoneId.of("America/Jamaica"), 20);

    assertEquals(first, second);
  }

  @Test
  void timezoneDeterminesTheWorldwideChallengeDate() {
    var candidates = QuizTestFixtures.balancedCandidates(1);
    var jamaica = service(new FakeQuestionRepository(candidates), new FakeDailyChallengeRepository())
        .getQuiz(ZoneId.of("America/Jamaica"), 5);
    var tokyo = service(new FakeQuestionRepository(candidates), new FakeDailyChallengeRepository())
        .getQuiz(ZoneId.of("Asia/Tokyo"), 5);

    assertEquals(LocalDate.of(2026, 9, 7), jamaica.date());
    assertEquals(LocalDate.of(2026, 9, 8), tokyo.date());
  }

  @Test
  void existingAssignmentBypassesCandidatesAndInsertionAndLoadsRetiredQuestions() {
    var candidates = QuizTestFixtures.balancedCandidates(1);
    var challenge = challenge(LocalDate.of(2026, 9, 7), candidates);
    var challengeRepository = new FakeDailyChallengeRepository(challenge, true);
    var questionRepository = new FakeQuestionRepository(candidates, true);

    var quiz = service(questionRepository, challengeRepository)
        .getQuiz(ZoneId.of("America/Jamaica"), 5);

    assertEquals(0, questionRepository.candidateLoads);
    assertEquals(0, challengeRepository.insertCalls);
    assertEquals(QuestionPublicationState.RETIRED, quiz.questions().getFirst().question().publicationState());
  }

  @Test
  void losingConcurrentCreatorReadsTheWinnerOnce() {
    var candidates = QuizTestFixtures.balancedCandidates(1);
    var winner = challenge(LocalDate.of(2026, 9, 7), candidates.reversed());
    var repository = new LosingDailyChallengeRepository(winner);

    var quiz = service(new FakeQuestionRepository(candidates), repository)
        .getQuiz(ZoneId.of("America/Jamaica"), 5);

    assertEquals(2, repository.findCalls);
    assertEquals(1, repository.insertCalls);
    assertEquals(
        winner.questions().getFirst().questionId(), quiz.questions().getFirst().question().id());
  }

  @Test
  void losingCreatorFailsClearlyWhenWinnerCannotBeRead() {
    var candidates = QuizTestFixtures.balancedCandidates(1);
    var repository = new LosingDailyChallengeRepository(null);

    assertThrows(
        QuizUnavailableException.class,
        () ->
            service(new FakeQuestionRepository(candidates), repository)
                .getQuiz(ZoneId.of("America/Jamaica"), 5));
    assertEquals(2, repository.findCalls);
  }

  @Test
  void refusesToGenerateAnIncompleteAssignment() {
    var candidates = QuizTestFixtures.balancedCandidates(1).subList(0, 19);

    assertThrows(
        InsufficientQuizQuestionsException.class,
        () ->
            service(new FakeQuestionRepository(candidates), new FakeDailyChallengeRepository())
                .getQuiz(ZoneId.of("America/Jamaica"), 5));
  }

  private static DailyQuizService service(
      QuizQuestionRepository questions, DailyChallengeRepository challenges) {
    return new DailyQuizService(
        questions,
        challenges,
        new QuizCandidateSelector(),
        new QuizQuestionPresenter(),
        CLOCK);
  }

  private static DailyChallenge challenge(
      LocalDate date, List<QuizQuestionCandidate> candidates) {
    var questions = new ArrayList<DailyChallengeQuestion>();
    for (var index = 0; index < 20; index++) {
      questions.add(new DailyChallengeQuestion(index + 1, candidates.get(index).questionId()));
    }
    return new DailyChallenge(date, questions);
  }

  private static void assertBalancedPrefix(
      DailyChallenge challenge, List<QuizQuestionCandidate> candidates, int count) {
    var byId =
        candidates.stream()
            .collect(java.util.stream.Collectors.toMap(QuizQuestionCandidate::questionId, value -> value));
    var prefix =
        challenge.questions().stream()
            .limit(count)
            .map(question -> byId.get(question.questionId()))
            .toList();
    var types = new EnumMap<QuestionType, Integer>(QuestionType.class);
    var difficulties = new EnumMap<QuizDifficulty, Integer>(QuizDifficulty.class);
    for (var type : QuestionType.values()) {
      types.put(type, 0);
    }
    for (var difficulty : QuizDifficulty.values()) {
      difficulties.put(difficulty, 0);
    }
    prefix.forEach(
        candidate -> {
          types.merge(candidate.type(), 1, Integer::sum);
          difficulties.merge(candidate.difficulty(), 1, Integer::sum);
        });
    assertEquals(QuizRules.questionTypeTargets(count), types);
    assertEquals(QuizRules.difficultyTargets(count), difficulties);
  }

  private static class FakeDailyChallengeRepository implements DailyChallengeRepository {
    private DailyChallenge challenge;
    private final boolean initiallyPresent;
    private int insertCalls;

    private FakeDailyChallengeRepository() {
      this(null, false);
    }

    private FakeDailyChallengeRepository(DailyChallenge challenge, boolean initiallyPresent) {
      this.challenge = challenge;
      this.initiallyPresent = initiallyPresent;
    }

    @Override
    public Optional<DailyChallenge> findByDate(LocalDate date) {
      return initiallyPresent || insertCalls > 0 ? Optional.ofNullable(challenge) : Optional.empty();
    }

    @Override
    public boolean insertIfAbsent(DailyChallenge challenge) {
      insertCalls++;
      this.challenge = challenge;
      return true;
    }
  }

  private static final class LosingDailyChallengeRepository
      implements DailyChallengeRepository {
    private final DailyChallenge winner;
    private int findCalls;
    private int insertCalls;

    private LosingDailyChallengeRepository(DailyChallenge winner) {
      this.winner = winner;
    }

    @Override
    public Optional<DailyChallenge> findByDate(LocalDate date) {
      findCalls++;
      return findCalls == 1 ? Optional.empty() : Optional.ofNullable(winner);
    }

    @Override
    public boolean insertIfAbsent(DailyChallenge challenge) {
      insertCalls++;
      return false;
    }
  }

  private static final class FakeQuestionRepository implements QuizQuestionRepository {
    private final List<QuizQuestionCandidate> candidates;
    private final boolean retireFirst;
    private int candidateLoads;
    private final List<Integer> aggregateLoadSizes = new ArrayList<>();

    private FakeQuestionRepository(List<QuizQuestionCandidate> candidates) {
      this(candidates, false);
    }

    private FakeQuestionRepository(List<QuizQuestionCandidate> candidates, boolean retireFirst) {
      this.candidates = candidates;
      this.retireFirst = retireFirst;
    }

    @Override
    public List<QuizQuestionCandidate> findPublishedCandidates() {
      candidateLoads++;
      return candidates;
    }

    @Override
    public List<QuizQuestionCandidate> findPublishedCandidatesByCollectionId(String collectionId) {
      throw new AssertionError("Daily Challenge does not use collections");
    }

    @Override
    public List<QuizQuestion> findByIdsInOrder(List<String> questionIds) {
      aggregateLoadSizes.add(questionIds.size());
      var questions = new ArrayList<QuizQuestion>();
      for (var id : questionIds) {
        var candidate =
            candidates.stream()
                .filter(value -> value.questionId().equals(id))
                .findFirst()
                .orElseThrow();
        var question = QuizTestFixtures.question(candidate);
        if (retireFirst && questions.isEmpty()) {
          question = retired(question);
        }
        questions.add(question);
      }
      return List.copyOf(questions);
    }

    private static QuizQuestion retired(QuizQuestion question) {
      var choice = (MultipleChoiceQuestion) question;
      return new MultipleChoiceQuestion(
          choice.id(),
          choice.difficulty(),
          QuestionPublicationState.RETIRED,
          choice.prompt(),
          choice.explanation(),
          choice.sources(),
          choice.options());
    }
  }
}
