package com.onthisday.platform.quiz;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

public record ApiChronologicalOrderingQuizQuestionResponse(
    String id,
    String type,
    String difficulty,
    String prompt,
    @JsonInclude(JsonInclude.Include.NON_NULL) Integer timeLimitSeconds,
    List<ApiChronologicalOrderingItemResponse> items,
    List<String> correctOrderItemIds,
    String explanation,
    List<ApiQuizSourceResponse> sources)
    implements ApiQuizQuestionResponse {}
