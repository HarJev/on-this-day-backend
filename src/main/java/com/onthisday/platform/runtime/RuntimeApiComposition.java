package com.onthisday.platform.runtime;

import com.onthisday.content.JdbcHistoricalEventRepository;
import com.onthisday.content.JdbcTodayContentRepository;
import com.onthisday.platform.http.ApiRoutes;
import com.onthisday.platform.http.HttpRouter;
import java.time.Clock;
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
    return ApiRoutes.create(
        new JdbcTodayContentRepository(dataSource),
        new JdbcHistoricalEventRepository(dataSource),
        clock);
  }
}
