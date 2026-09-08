package com.onthisday.platform.runtime;

import com.onthisday.content.JdbcHistoricalEventRepository;
import com.onthisday.content.JdbcTodayContentRepository;
import com.onthisday.notifications.JdbcDeviceRegistrationRepository;
import com.onthisday.platform.http.ApiRoutes;
import com.onthisday.platform.http.HttpRouter;
import com.onthisday.platform.quiz.QuizApiServices;
import com.onthisday.quiz.DailyQuizService;
import com.onthisday.quiz.JdbcDailyChallengeRepository;
import com.onthisday.quiz.JdbcQuizCatalogRepository;
import com.onthisday.quiz.JdbcQuizCollectionRepository;
import com.onthisday.quiz.JdbcQuizQuestionRepository;
import com.onthisday.quiz.QuizCandidateSelector;
import com.onthisday.quiz.QuizCatalogService;
import com.onthisday.quiz.QuizQuestionPresenter;
import com.onthisday.quiz.QuickPlayQuizService;
import java.time.Clock;
import java.util.random.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RuntimeApiComposition {

  private static final Logger LOG = LoggerFactory.getLogger(RuntimeApiComposition.class);

  private RuntimeApiComposition() {}

  public static HttpRouter createRouterFromEnvironment() {
    return createRouter(DatabaseConfig.fromEnvironment(), Clock.systemUTC());
  }

  public static HttpRouter createRouter(DatabaseConfig databaseConfig, Clock clock) {
    LOG.info(
        "runtime_composition_start dbJdbcUrlConfigured={} dbUser={} connectTimeoutSeconds={} socketTimeoutSeconds={}",
        !databaseConfig.jdbcUrl().isBlank(),
        databaseConfig.user(),
        databaseConfig.connectTimeoutSeconds(),
        databaseConfig.socketTimeoutSeconds());
    var dataSource = new PostgresDataSourceFactory().create(databaseConfig);
    var quizQuestionRepository = new JdbcQuizQuestionRepository(dataSource);
    var quizCollectionRepository = new JdbcQuizCollectionRepository(dataSource);
    var selector = new QuizCandidateSelector();
    var presenter = new QuizQuestionPresenter();
    var quizApiServices =
        new QuizApiServices(
            new QuizCatalogService(new JdbcQuizCatalogRepository(dataSource)),
            new QuickPlayQuizService(
                quizQuestionRepository,
                quizCollectionRepository,
                selector,
                presenter,
                RandomGenerator::getDefault),
            new DailyQuizService(
                quizQuestionRepository,
                new JdbcDailyChallengeRepository(dataSource),
                selector,
                presenter,
                clock));
    return ApiRoutes.create(
        new JdbcTodayContentRepository(dataSource),
        new JdbcHistoricalEventRepository(dataSource),
        new JdbcDeviceRegistrationRepository(dataSource),
        clock,
        quizApiServices);
  }
}
