package com.onthisday.quiz;

import java.time.MonthDay;
import java.util.List;

public interface QuizQuestionRepository {

  List<QuizQuestionCandidate> findPublishedCandidates();

  List<QuizQuestionCandidate> findPublishedCandidatesByCollectionId(String collectionId);

  List<QuizQuestion> findByIdsInOrder(List<String> questionIds);

  /**
   * Published questions linked to the events curated for a month/day, one entry per question.
   * A question linked to both the featured and an additional event is reported as featured.
   */
  default List<DateLinkedQuizCandidate> findPublishedCandidatesLinkedTo(MonthDay monthDay) {
    return List.of();
  }
}
