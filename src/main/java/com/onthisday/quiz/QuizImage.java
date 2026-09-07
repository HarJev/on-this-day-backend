package com.onthisday.quiz;

import static com.onthisday.quiz.QuizChecks.requireHttps;
import static com.onthisday.quiz.QuizChecks.requireNonBlank;
import static com.onthisday.quiz.QuizChecks.requireNullableNonBlank;

import java.net.URI;

public record QuizImage(
    URI url,
    String altText,
    String source,
    URI sourceUrl,
    String attribution,
    String creator,
    String license,
    URI licenseUrl) {

  public QuizImage {
    url = requireHttps(url, "url");
    altText = requireNonBlank(altText, "altText");
    source = requireNonBlank(source, "source");
    sourceUrl = requireHttps(sourceUrl, "sourceUrl");
    attribution = requireNonBlank(attribution, "attribution");
    creator = requireNullableNonBlank(creator, "creator");
    license = requireNonBlank(license, "license");
    licenseUrl = requireHttps(licenseUrl, "licenseUrl");
  }
}
