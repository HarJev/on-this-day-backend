package com.onthisday.quiz;

import java.util.List;
import java.util.Map;

public interface QuizEventLinkRepository {
  Map<String, List<RelatedQuizEvent>> findByQuestionIds(List<String> ids);
  boolean hasPublishedQuestionForEvent(String eventId);

  static QuizEventLinkRepository empty() {
    return new QuizEventLinkRepository() {
      @Override
      public Map<String, List<RelatedQuizEvent>> findByQuestionIds(List<String> ids) {
        return Map.of();
      }
      @Override
      public boolean hasPublishedQuestionForEvent(String eventId) {
        return false;
      }
    };
  }
}
