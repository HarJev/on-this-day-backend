package com.onthisday.ingestion.editorial;

import com.onthisday.ingestion.ContentValidationResult;
import com.onthisday.ingestion.CuratedContent;
import com.onthisday.ingestion.quiz.CuratedQuizContent;
import java.util.List;

public record EditorialPreflightResult(
    ContentValidationResult reviewValidation,
    ContentValidationResult historicalValidation,
    ContentValidationResult quizValidation,
    List<String> approvalErrors,
    CuratedContent historicalContent,
    CuratedQuizContent quizContent) {

  public boolean valid() {
    return reviewValidation.valid()
        && historicalValidation.valid()
        && quizValidation.valid()
        && approvalErrors.isEmpty();
  }
}
