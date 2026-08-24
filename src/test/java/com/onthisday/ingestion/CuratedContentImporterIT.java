package com.onthisday.ingestion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onthisday.content.JdbcHistoricalEventRepository;
import com.onthisday.content.JdbcTodayContentRepository;
import java.sql.SQLException;
import java.time.MonthDay;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class CuratedContentImporterIT {

  @Container
  static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("on_this_day")
          .withUsername("on_this_day")
          .withPassword("on_this_day");

  private static DataSource dataSource;

  @BeforeAll
  static void migrateDatabase() {
    var postgresDataSource = new PGSimpleDataSource();
    postgresDataSource.setUrl(POSTGRES.getJdbcUrl());
    postgresDataSource.setUser(POSTGRES.getUsername());
    postgresDataSource.setPassword(POSTGRES.getPassword());
    dataSource = postgresDataSource;

    Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").load().migrate();
  }

  @Test
  void importsAugustTwentyTwoContentAndCanRunTwiceWithoutDuplicatingRows() throws SQLException {
    var reader = new CuratedContentReader(new ObjectMapper());
    var importer = new CuratedContentImporter(dataSource, new CuratedContentValidator());
    var content = reader.readDefault();

    var firstResult = importer.importContent(content);
    var secondResult = importer.importContent(content);

    assertTrue(firstResult.valid());
    assertTrue(firstResult.warnings().isEmpty());
    assertTrue(secondResult.valid());
    assertTrue(secondResult.warnings().isEmpty());

    var todayRepository = new JdbcTodayContentRepository(dataSource);
    var todayContent = todayRepository.getTodayContent(MonthDay.of(8, 22));

    assertEquals("Aug 22", todayContent.date().displayDate());
    assertEquals("battle-of-bosworth-field-1485", todayContent.featuredEvent().id());
    assertEquals("A king died in battle 541 years ago today", todayContent.featuredEvent().notificationTitle());
    assertEquals(
        "https://commons.wikimedia.org/wiki/File:Richard_III_at_the_Battle_of_Bosworth.jpg",
        todayContent.featuredEvent().image().sourceUrl().toString());
    assertEquals(6, todayContent.additionalEvents().size());
    assertEquals("loch-ness-monster-columba-565", todayContent.additionalEvents().get(0).id());
    assertEquals("nolan-ryan-5000-strikeouts-1989", todayContent.additionalEvents().get(5).id());

    var eventRepository = new JdbcHistoricalEventRepository(dataSource);
    var bosworth = eventRepository.getEvent("battle-of-bosworth-field-1485");

    assertEquals("Richard III is defeated at the Battle of Bosworth Field", bosworth.title());
    assertEquals(2, bosworth.sources().size());
    assertEquals("Encyclopaedia Britannica", bosworth.sources().get(0).name());
    assertEquals(1, bosworth.images().size());
    assertEquals(bosworth.images().get(0), bosworth.primaryImage());
    assertEquals(
        "https://commons.wikimedia.org/wiki/File:Richard_III_at_the_Battle_of_Bosworth.jpg",
        bosworth.primaryImage().sourceUrl().toString());

    assertEquals(7, countRows("historical_event"));
    assertEquals(8, countRows("event_source"));
    assertEquals(1, countRows("event_image"));
    assertEquals(7, countRows("daily_event"));
  }

  private static int countRows(String tableName) throws SQLException {
    try (var connection = dataSource.getConnection();
        var statement = connection.createStatement();
        var resultSet = statement.executeQuery("SELECT count(*) FROM " + tableName)) {
      resultSet.next();
      return resultSet.getInt(1);
    }
  }
}
