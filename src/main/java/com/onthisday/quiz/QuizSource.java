package com.onthisday.quiz;

import static com.onthisday.quiz.QuizChecks.requireHttps;
import static com.onthisday.quiz.QuizChecks.requireNonBlank;

import java.net.URI;

public record QuizSource(String displayName, URI url) {

  public QuizSource {
    displayName = requireNonBlank(displayName, "displayName");
    url = requireHttps(url, "url");
  }
}
