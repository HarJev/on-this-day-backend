package com.onthisday.ingestion;

public record CuratedImageJson(
    String url,
    String altText,
    String source,
    String sourceUrl,
    String creator,
    String attribution,
    String license,
    String licenseUrl,
    Boolean primary) {}
