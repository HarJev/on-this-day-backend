package com.onthisday.ingestion.media;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** JSON-friendly release report for every published quiz-image rendition. */
public record QuizImageLivenessReport(List<QuizImageLivenessResult> results) {

  public QuizImageLivenessReport {
    results = results == null ? List.of() : List.copyOf(results);
  }

  @JsonProperty("total")
  public int totalCount() {
    return results.size();
  }

  @JsonProperty("passed")
  public int passedCount() {
    return (int) results.stream().filter(QuizImageLivenessResult::reachable).count();
  }

  @JsonProperty("failed")
  public int failedCount() {
    return results.size() - passedCount();
  }

  @JsonProperty("successful")
  public boolean successful() {
    return failedCount() == 0;
  }
}
