package com.onthisday.platform.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;

class PostgresDataSourceFactoryTest {

  @Test
  void createsSimplePostgresDataSourceFromConfig() {
    var dataSource =
        new PostgresDataSourceFactory()
            .create(
                new DatabaseConfig(
                    "jdbc:postgresql://localhost:5432/on_this_day", "on_this_day", "secret"));

    var postgresDataSource = assertInstanceOf(PGSimpleDataSource.class, dataSource);
    assertEquals("on_this_day", postgresDataSource.getUser());
    assertEquals(5, postgresDataSource.getConnectTimeout());
    assertEquals(5, postgresDataSource.getLoginTimeout());
    assertEquals(10, postgresDataSource.getSocketTimeout());
  }

  @Test
  void appliesConfiguredTimeouts() {
    var dataSource =
        new PostgresDataSourceFactory()
            .create(
                new DatabaseConfig(
                    "jdbc:postgresql://localhost:5432/on_this_day",
                    "on_this_day",
                    "secret",
                    3,
                    7));

    var postgresDataSource = assertInstanceOf(PGSimpleDataSource.class, dataSource);
    assertEquals(3, postgresDataSource.getConnectTimeout());
    assertEquals(3, postgresDataSource.getLoginTimeout());
    assertEquals(7, postgresDataSource.getSocketTimeout());
  }
}
