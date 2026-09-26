package com.onthisday.ingestion.media;

import java.time.Duration;

/** Bounded transport settings for the explicit pre-release quiz-image audit. */
public record QuizImageLivenessAuditConfiguration(
    Duration connectTimeout,
    Duration requestTimeout,
    int maxConcurrency,
    long maxEncodedBytes,
    String userAgent) {

  public static final long DEFAULT_MAX_ENCODED_BYTES = 8L * 1024 * 1024;
  public static final String DEFAULT_USER_AGENT =
      "OnThisDayImageAudit/1.0 (+https://github.com/HarJev/on-this-day-backend)";

  public QuizImageLivenessAuditConfiguration {
    if (connectTimeout == null || connectTimeout.isZero() || connectTimeout.isNegative()) {
      throw new IllegalArgumentException("connectTimeout must be positive");
    }
    if (requestTimeout == null || requestTimeout.isZero() || requestTimeout.isNegative()) {
      throw new IllegalArgumentException("requestTimeout must be positive");
    }
    if (maxConcurrency <= 0) {
      throw new IllegalArgumentException("maxConcurrency must be positive");
    }
    if (maxEncodedBytes <= 0) {
      throw new IllegalArgumentException("maxEncodedBytes must be positive");
    }
    if (userAgent == null || userAgent.isBlank()) {
      throw new IllegalArgumentException("userAgent must not be blank");
    }
  }

  public static QuizImageLivenessAuditConfiguration defaults() {
    return new QuizImageLivenessAuditConfiguration(
        Duration.ofSeconds(5),
        Duration.ofSeconds(15),
        4,
        DEFAULT_MAX_ENCODED_BYTES,
        DEFAULT_USER_AGENT);
  }
}
