package com.onthisday.ingestion.media;

/** A validated immutable object proposal for one asset. */
public record OwnedImagePublishPlanEntry(
    String id,
    String questionId,
    String objectKey,
    String contentType,
    long byteSize,
    int width,
    int height,
    String sha256,
    String cacheControl,
    String proposedUrl) {}
