package com.onthisday.ingestion.quiz;

public record LoadedQuizQuestionPack(String relativeFilename, QuizQuestionPackFile file) {

  public LoadedQuizQuestionPack {
    if (relativeFilename == null || relativeFilename.isBlank()) {
      throw new IllegalArgumentException("relativeFilename must not be blank");
    }
  }
}
