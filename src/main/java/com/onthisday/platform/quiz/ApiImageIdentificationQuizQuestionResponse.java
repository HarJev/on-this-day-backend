package com.onthisday.platform.quiz;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

public record ApiImageIdentificationQuizQuestionResponse(
    String id,
    String type,
    String difficulty,
    String prompt,
    @JsonInclude(JsonInclude.Include.NON_NULL) Integer timeLimitSeconds,
    ApiQuizImageResponse image,
    List<ApiQuizOptionResponse> options,
    String correctOptionId,
    String explanation,
    List<ApiQuizSourceResponse> sources)
    implements ApiQuizQuestionResponse {}
