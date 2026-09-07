package com.onthisday.ingestion.quiz;

import java.util.List;

public record CuratedQuizQuestionJson(
    String id,
    String type,
    String difficulty,
    String publicationState,
    String prompt,
    List<CuratedQuizOptionJson> options,
    String correctOptionId,
    List<CuratedQuizOrderingItemJson> items,
    List<String> correctOrderItemIds,
    CuratedQuizImageJson image,
    String explanation,
    List<CuratedQuizSourceJson> sources,
    List<String> collectionIds) {}
