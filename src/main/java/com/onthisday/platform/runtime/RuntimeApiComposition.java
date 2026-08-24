package com.onthisday.platform.runtime;

import com.onthisday.content.JdbcHistoricalEventRepository;
import com.onthisday.content.JdbcTodayContentRepository;
import com.onthisday.platform.http.ApiRoutes;
import com.onthisday.platform.http.HttpRouter;
import java.time.Clock;

public final class RuntimeApiComposition {

  private RuntimeApiComposition() {}

  public static HttpRouter createRouterFromEnvironment() {
    return createRouter(DatabaseConfig.fromEnvironment(), Clock.systemUTC());
  }

  public static HttpRouter createRouter(DatabaseConfig databaseConfig, Clock clock) {
    var dataSource = new PostgresDataSourceFactory().create(databaseConfig);
    return ApiRoutes.create(
        new JdbcTodayContentRepository(dataSource),
        new JdbcHistoricalEventRepository(dataSource),
        clock);
  }
}
