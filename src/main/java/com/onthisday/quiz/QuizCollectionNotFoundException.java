package com.onthisday.quiz;

public class QuizCollectionNotFoundException extends RuntimeException {

  public QuizCollectionNotFoundException(String collectionId) {
    super("Quiz collection not found: " + collectionId);
  }
}
