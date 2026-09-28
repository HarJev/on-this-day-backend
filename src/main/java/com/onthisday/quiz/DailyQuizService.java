package com.onthisday.quiz;

import java.time.Clock;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
    var linked = questionRepository.findPublishedCandidatesLinkedTo(MonthDay.from(date));
    var selected = dateLinkedSelection(date, ordered, linked).orElseGet(() -> globalSelection(ordered));

    var references = new ArrayList<DailyChallengeQuestion>(20);
    for (var index = 0; index < selected.size(); index++) {
      references.add(new DailyChallengeQuestion(index + 1, selected.get(index).questionId()));
    }
    var proposed = new DailyChallenge(date, references, DailyChallenge.DATE_LINKED_SELECTION_VERSION);
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

  /** Version 1 behavior: balanced global selection with stable 5/10 prefixes. */
  private List<QuizQuestionCandidate> globalSelection(List<QuizQuestionCandidate> ordered) {
    var selected = new ArrayList<>(selector.select(ordered, 5));
    selected.addAll(
        selector.selectAdditional(remaining(ordered, selected), List.copyOf(selected), 10));
    selected.addAll(
        selector.selectAdditional(remaining(ordered, selected), List.copyOf(selected), 20));
    return selected;
  }

  /**
   * Reserves position 1 for a question linked to the date's featured event and position 6 for
   * one more question linked to any event curated for that date. Other date-linked questions
   * are left out so the whole assignment carries at most two. Empty when nothing is linked or
   * the remaining bank cannot fill 20, so the caller falls back to the global selection.
   */
  private Optional<List<QuizQuestionCandidate>> dateLinkedSelection(
      LocalDate date, List<QuizQuestionCandidate> ordered, List<DateLinkedQuizCandidate> linked) {
    if (linked.isEmpty()) {
      return Optional.empty();
    }
    var featured =
        DeterministicQuizOrder.candidates(
            date, linked.stream().filter(DateLinkedQuizCandidate::featured).map(DateLinkedQuizCandidate::candidate).toList());
    var others =
        DeterministicQuizOrder.candidates(
            date, linked.stream().filter(link -> !link.featured()).map(DateLinkedQuizCandidate::candidate).toList());
    var linkedIds = new HashSet<String>();
    linked.forEach(link -> linkedIds.add(link.candidate().questionId()));
    var pool = ordered.stream().filter(candidate -> !linkedIds.contains(candidate.questionId())).toList();

    var extras = new ArrayList<QuizQuestionCandidate>();
    extras.addAll(featured.stream().skip(1).toList());
    extras.addAll(others);
    try {
      var selected = new ArrayList<QuizQuestionCandidate>(20);
      featured.stream().findFirst().ifPresent(selected::add);
      selected.addAll(selector.selectAdditional(remaining(pool, selected), List.copyOf(selected), 5));
      extras.stream().findFirst().ifPresent(selected::add);
      selected.addAll(selector.selectAdditional(remaining(pool, selected), List.copyOf(selected), 10));
      selected.addAll(selector.selectAdditional(remaining(pool, selected), List.copyOf(selected), 20));
      return Optional.of(selected);
    } catch (InsufficientQuizQuestionsException exception) {
      return Optional.empty();
    }
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
