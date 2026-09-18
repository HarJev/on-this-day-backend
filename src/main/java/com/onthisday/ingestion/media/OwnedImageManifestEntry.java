package com.onthisday.ingestion.media;

/** One source rendition and its proposed immutable object location. */
public record OwnedImageManifestEntry(
    String id,
    String questionId,
    String relativePath,
    String sourceRenditionUrl,
    String source,
    String sourceUrl,
    String altText,
    String attribution,
    String creator,
    String license,
    String licenseUrl,
    String contentType,
    int width,
    int height,
    long byteSize,
    String sha256,
    String objectKey) {}
