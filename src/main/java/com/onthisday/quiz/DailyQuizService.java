package com.onthisday.quiz;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class DailyQuizService {

  private final QuizQuestionRepository questionRepository;
  private final DailyChallengeRepository challengeRepository;
  private final QuizCandidateSelector selector;
  private final QuizQuestionPresenter presenter;
  private final Clock clock;

  public DailyQuizService(
      QuizQuestionRepository questionRepository,
      DailyChallengeRepository challengeRepository,
      QuizCandidateSelector selector,
      QuizQuestionPresenter presenter,
      Clock clock) {
    this.questionRepository =
        Objects.requireNonNull(questionRepository, "questionRepository must not be null");
    this.challengeRepository =
        Objects.requireNonNull(challengeRepository, "challengeRepository must not be null");
    this.selector = Objects.requireNonNull(selector, "selector must not be null");
    this.presenter = Objects.requireNonNull(presenter, "presenter must not be null");
    this.clock = Objects.requireNonNull(clock, "clock must not be null");
  }

  public DailyQuiz getQuiz(ZoneId timezone, int questionCount) {
    if (timezone == null) {
      throw new InvalidQuizRequestException("timezone must not be null");
    }
    QuizRules.requireSupportedQuestionCount(questionCount);
    var date = LocalDate.now(clock.withZone(timezone));
    var challenge =
        challengeRepository.findByDate(date).orElseGet(() -> createChallenge(date));
    var questionIds =
        challenge.questions().stream()
            .limit(questionCount)
            .map(DailyChallengeQuestion::questionId)
            .toList();
    var questions = questionRepository.findByIdsInOrder(questionIds);
    var playable = questions.stream().map(question -> presenter.presentDaily(question, date)).toList();
    return new DailyQuiz(date, playable);
  }

  private DailyChallenge createChallenge(LocalDate date) {
    var candidates = questionRepository.findPublishedCandidates();
    if (candidates.size() < 20) {
      throw new InsufficientQuizQuestionsException(20, candidates.size());
    }

    var ordered = DeterministicQuizOrder.candidates(date, candidates);
    var selected = new ArrayList<>(selector.select(ordered, 5));
    selected.addAll(
        selector.selectAdditional(remaining(ordered, selected), List.copyOf(selected), 10));
    selected.addAll(
        selector.selectAdditional(remaining(ordered, selected), List.copyOf(selected), 20));

    var references = new ArrayList<DailyChallengeQuestion>(20);
    for (var index = 0; index < selected.size(); index++) {
      references.add(new DailyChallengeQuestion(index + 1, selected.get(index).questionId()));
    }
    var proposed = new DailyChallenge(date, references);
    if (challengeRepository.insertIfAbsent(proposed)) {
      return proposed;
    }
    return challengeRepository
        .findByDate(date)
        .orElseThrow(
            () ->
                new QuizUnavailableException(
                    "A concurrent Daily Challenge creator won, but its assignment could not be read."));
  }

  private static List<QuizQuestionCandidate> remaining(
      List<QuizQuestionCandidate> candidates, List<QuizQuestionCandidate> selected) {
    var selectedIds = new HashSet<String>();
    selected.forEach(candidate -> selectedIds.add(candidate.questionId()));
    return candidates.stream()
        .filter(candidate -> !selectedIds.contains(candidate.questionId()))
        .toList();
  }
}
