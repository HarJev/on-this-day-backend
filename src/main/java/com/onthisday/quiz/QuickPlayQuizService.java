package com.onthisday.quiz;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;

public class QuickPlayQuizService {

  private final QuizQuestionRepository questionRepository;
  private final QuizCollectionRepository collectionRepository;
  private final QuizCandidateSelector selector;
  private final QuizQuestionPresenter presenter;
  private final Supplier<RandomGenerator> randomGeneratorSupplier;

  public QuickPlayQuizService(
      QuizQuestionRepository questionRepository,
      QuizCollectionRepository collectionRepository,
      QuizCandidateSelector selector,
      QuizQuestionPresenter presenter,
      Supplier<RandomGenerator> randomGeneratorSupplier) {
    this.questionRepository =
        Objects.requireNonNull(questionRepository, "questionRepository must not be null");
    this.collectionRepository =
        Objects.requireNonNull(collectionRepository, "collectionRepository must not be null");
    this.selector = Objects.requireNonNull(selector, "selector must not be null");
    this.presenter = Objects.requireNonNull(presenter, "presenter must not be null");
    this.randomGeneratorSupplier =
        Objects.requireNonNull(
            randomGeneratorSupplier, "randomGeneratorSupplier must not be null");
  }

  public QuickPlayQuiz createQuiz(int questionCount, Optional<String> collectionId) {
    QuizRules.requireSupportedQuestionCount(questionCount);
    if (collectionId == null) {
      throw new InvalidQuizRequestException("collectionId selection must not be null");
    }
    collectionId.ifPresent(QuickPlayQuizService::validateCollectionId);

    Optional<QuizCollection> collection =
        collectionId.map(
            id ->
                collectionRepository
                    .findById(id)
                    .orElseThrow(() -> new QuizCollectionNotFoundException(id)));
    var candidates =
        collection
            .map(value -> questionRepository.findPublishedCandidatesByCollectionId(value.id()))
            .orElseGet(questionRepository::findPublishedCandidates);
    if (candidates.size() < questionCount) {
      throw new InsufficientQuizQuestionsException(questionCount, candidates.size());
    }

    var random =
        Objects.requireNonNull(
            randomGeneratorSupplier.get(), "randomGeneratorSupplier returned null");
    var orderedCandidates = shuffled(candidates, random);
    var selected = selector.select(orderedCandidates, questionCount);
    var questions =
        questionRepository.findByIdsInOrder(
            selected.stream().map(QuizQuestionCandidate::questionId).toList());
    if (questions.stream()
        .anyMatch(question -> question.publicationState() != QuestionPublicationState.PUBLISHED)) {
      throw new QuizUnavailableException(
          "Selected Quick Play questions changed publication state during generation.");
    }
    var playable = questions.stream().map(question -> presenter.present(question, random)).toList();
    return new QuickPlayQuiz(collection, playable);
  }

  private static void validateCollectionId(String collectionId) {
    try {
      QuizChecks.requireSlug(collectionId, "collectionId");
    } catch (InvalidQuizDefinitionException exception) {
      throw new InvalidQuizRequestException("collectionId must be a lowercase slug");
    }
  }

  private static <T> List<T> shuffled(List<T> values, RandomGenerator random) {
    var shuffled = new ArrayList<>(values);
    for (var index = shuffled.size() - 1; index > 0; index--) {
      var swapIndex = random.nextInt(index + 1);
      var value = shuffled.get(index);
      shuffled.set(index, shuffled.get(swapIndex));
      shuffled.set(swapIndex, value);
    }
    return List.copyOf(shuffled);
  }
}
