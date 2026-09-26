package com.onthisday.ingestion.quiz;

import java.util.List;

public record CuratedQuizContent(QuizCollectionsFile collectionsFile, List<LoadedQuizQuestionPack> questionPacks) {

  public CuratedQuizContent {
    questionPacks = questionPacks == null ? List.of() : List.copyOf(questionPacks);
  }
}
