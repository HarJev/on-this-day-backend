package com.onthisday.platform.quiz;

import com.onthisday.quiz.DailyQuizService;
import com.onthisday.quiz.QuizCatalogService;
import com.onthisday.quiz.QuickPlayQuizService;
import java.util.Objects;

/** Quiz services needed by the HTTP route registration boundary. */
public record QuizApiServices(
    QuizCatalogService catalogService,
    QuickPlayQuizService quickPlayService,
    DailyQuizService dailyService) {

  public QuizApiServices {
    catalogService = Objects.requireNonNull(catalogService, "catalogService must not be null");
    quickPlayService = Objects.requireNonNull(quickPlayService, "quickPlayService must not be null");
    dailyService = Objects.requireNonNull(dailyService, "dailyService must not be null");
  }
}
