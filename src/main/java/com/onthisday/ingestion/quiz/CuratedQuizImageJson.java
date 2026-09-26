package com.onthisday.ingestion.quiz;

public record CuratedQuizImageJson(
    String url,
    String altText,
    String source,
    String sourceUrl,
    String attribution,
    String creator,
    String license,
    String licenseUrl) {}
