package com.onthisday.platform.quiz;

import java.util.List;

public record ApiQuizCatalogCollectionResponse(
    String id,
    String name,
    String group,
    int publishedQuestionCount,
    List<Integer> supportedQuestionCounts) {}
