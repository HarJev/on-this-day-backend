package com.onthisday.quiz;

import java.util.List;
import java.util.Optional;

public interface QuizCollectionRepository {

  Optional<QuizCollection> findById(String collectionId);

  List<QuizCollection> findAll();
}
