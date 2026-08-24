package com.onthisday.platform.runtime;

import javax.sql.DataSource;
import org.postgresql.ds.PGSimpleDataSource;

public final class PostgresDataSourceFactory {

  public DataSource create(DatabaseConfig config) {
    var dataSource = new PGSimpleDataSource();
    dataSource.setUrl(config.jdbcUrl());
    dataSource.setUser(config.user());
    dataSource.setPassword(config.password());
    return dataSource;
  }
}
