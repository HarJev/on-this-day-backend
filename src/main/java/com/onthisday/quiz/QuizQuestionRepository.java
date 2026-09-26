package com.onthisday.quiz;

import java.util.List;

public interface QuizQuestionRepository {

  List<QuizQuestionCandidate> findPublishedCandidates();

  List<QuizQuestionCandidate> findPublishedCandidatesByCollectionId(String collectionId);

  List<QuizQuestion> findByIdsInOrder(List<String> questionIds);
}
