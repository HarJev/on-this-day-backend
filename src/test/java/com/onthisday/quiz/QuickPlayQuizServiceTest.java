package com.onthisday.quiz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

class QuickPlayQuizServiceTest {

  @ParameterizedTest
  @ValueSource(ints = {5, 10, 20})
  void createsEverySupportedMixedCountWithOneRequestLocalGenerator(int questionCount) {
    var candidates = QuizTestFixtures.balancedCandidates(1);
    var questionRepository = new FakeQuestionRepository(candidates, candidates);
    var supplierCalls = new AtomicInteger();
    var service =
        service(
            questionRepository,
            new FakeCollectionRepository(Optional.empty()),
            () -> {
              supplierCalls.incrementAndGet();
              return new Random(31);
            });

    var quiz = service.createQuiz(questionCount, Optional.empty());

    assertTrue(quiz.collection().isEmpty());
    assertEquals(questionCount, quiz.questions().size());
    assertEquals(1, supplierCalls.get());
    assertEquals(1, questionRepository.mixedCandidateLoads);
    assertEquals(0, questionRepository.collectionCandidateLoads);
  }

  @Test
  void limitsSelectionToTheRequestedCollection() {
    var all = QuizTestFixtures.balancedCandidates(1);
    var collectionCandidates = all.subList(0, 5);
    var collection = new QuizCollection("ancient-history", "Ancient History", CollectionGroup.HISTORICAL_PERIOD);
    var repository = new FakeQuestionRepository(all, collectionCandidates);
    var service =
        service(
            repository,
            new FakeCollectionRepository(Optional.of(collection)),
            () -> new Random(3));

    var quiz = service.createQuiz(5, Optional.of("ancient-history"));

    assertEquals(collection, quiz.collection().orElseThrow());
    assertEquals(0, repository.mixedCandidateLoads);
    assertEquals(1, repository.collectionCandidateLoads);
    assertTrue(
        quiz.questions().stream()
            .allMatch(
                playable ->
                    collectionCandidates.stream()
                        .anyMatch(candidate -> candidate.questionId().equals(playable.question().id()))));
  }

  @Test
  void rejectsUnknownCollectionInsufficientContentAndInvalidCount() {
    var candidates = QuizTestFixtures.balancedCandidates(1);
    var repository = new FakeQuestionRepository(candidates, candidates.subList(0, 4));

    assertThrows(
        QuizCollectionNotFoundException.class,
        () ->
            service(repository, new FakeCollectionRepository(Optional.empty()), () -> new Random(1))
                .createQuiz(5, Optional.of("missing")));

    var collection = new QuizCollection("small", "Small", CollectionGroup.TOPIC);
    assertThrows(
        InsufficientQuizQuestionsException.class,
        () ->
            service(
                    repository,
                    new FakeCollectionRepository(Optional.of(collection)),
                    () -> new Random(1))
                .createQuiz(5, Optional.of("small")));
    assertThrows(
        InvalidQuizRequestException.class,
        () ->
            service(repository, new FakeCollectionRepository(Optional.empty()), () -> new Random(1))
                .createQuiz(7, Optional.empty()));
  }

  @Test
  void seededGenerationHasDirectlyRepeatableSelectionAndPresentation() {
    var candidates = QuizTestFixtures.balancedCandidates(1);
    var first =
        service(
                new FakeQuestionRepository(candidates, candidates),
                new FakeCollectionRepository(Optional.empty()),
                () -> new Random(99))
            .createQuiz(10, Optional.empty());
    var second =
        service(
                new FakeQuestionRepository(candidates, candidates),
                new FakeCollectionRepository(Optional.empty()),
                () -> new Random(99))
            .createQuiz(10, Optional.empty());

    assertEquals(first, second);
  }

  private static QuickPlayQuizService service(
      QuizQuestionRepository questionRepository,
      QuizCollectionRepository collectionRepository,
      java.util.function.Supplier<java.util.random.RandomGenerator> randomSupplier) {
    return new QuickPlayQuizService(
        questionRepository,
        collectionRepository,
        new QuizCandidateSelector(),
        new QuizQuestionPresenter(),
        randomSupplier);
  }

  private static final class FakeCollectionRepository implements QuizCollectionRepository {
    private final Optional<QuizCollection> collection;

    private FakeCollectionRepository(Optional<QuizCollection> collection) {
      this.collection = collection;
    }

    @Override
    public Optional<QuizCollection> findById(String collectionId) {
      return collection.filter(value -> value.id().equals(collectionId));
    }

    @Override
    public List<QuizCollection> findAll() {
      return collection.stream().toList();
    }
  }

  private static final class FakeQuestionRepository implements QuizQuestionRepository {
    private final List<QuizQuestionCandidate> mixed;
    private final List<QuizQuestionCandidate> collection;
    private int mixedCandidateLoads;
    private int collectionCandidateLoads;

    private FakeQuestionRepository(
        List<QuizQuestionCandidate> mixed, List<QuizQuestionCandidate> collection) {
      this.mixed = mixed;
      this.collection = collection;
    }

    @Override
    public List<QuizQuestionCandidate> findPublishedCandidates() {
      mixedCandidateLoads++;
      return mixed;
    }

    @Override
    public List<QuizQuestionCandidate> findPublishedCandidatesByCollectionId(String collectionId) {
      collectionCandidateLoads++;
      return collection;
    }

    @Override
    public List<QuizQuestion> findByIdsInOrder(List<String> questionIds) {
      return questionIds.stream()
          .map(
              id ->
                  mixed.stream()
                      .filter(candidate -> candidate.questionId().equals(id))
                      .findFirst()
                      .orElseGet(
                          () ->
                              collection.stream()
                                  .filter(candidate -> candidate.questionId().equals(id))
                                  .findFirst()
                                  .orElseThrow()))
          .map(QuizTestFixtures::question)
          .toList();
    }
  }
}
