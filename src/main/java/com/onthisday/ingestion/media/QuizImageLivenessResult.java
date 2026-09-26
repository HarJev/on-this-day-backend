package com.onthisday.ingestion.media;

import java.net.URI;

/** One bounded HTTP observation from the explicit quiz-image release audit. */
public record QuizImageLivenessResult(
    String questionId,
    URI url,
    boolean reachable,
    Integer statusCode,
    String contentType,
    Long byteSize,
    long durationMillis,
    String failure) {}
