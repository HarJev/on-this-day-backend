package com.onthisday.ingestion.editorial;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onthisday.ingestion.CuratedContent;
import com.onthisday.ingestion.CuratedContentImporter;
import com.onthisday.ingestion.CuratedContentReader;
import com.onthisday.ingestion.CuratedContentValidator;
import com.onthisday.ingestion.CuratedEventJson;
import com.onthisday.ingestion.CuratedEventsFile;
import com.onthisday.ingestion.quiz.QuizContentImporter;
import com.onthisday.ingestion.quiz.QuizContentReader;
import com.onthisday.ingestion.quiz.QuizContentValidator;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class ContentStatusIT {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Container
  static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16-alpine");

  private static DataSource dataSource;
  private static CuratedContent canonical;

  @BeforeAll
  static void importCanonicalContent() {
    var postgres = new PGSimpleDataSource();
    postgres.setUrl(POSTGRES.getJdbcUrl());
    postgres.setUser(POSTGRES.getUsername());
    postgres.setPassword(POSTGRES.getPassword());
    dataSource = postgres;
    Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").load().migrate();
    canonical = new CuratedContentReader(OBJECT_MAPPER).read(Path.of("content"));
    new CuratedContentImporter(dataSource, new CuratedContentValidator()).importContent(canonical);
    new QuizContentImporter(dataSource, new QuizContentValidator(true))
        .importContent(new QuizContentReader(OBJECT_MAPPER).read(Path.of("content/quizzes")));
  }

  @Test
  void freshImportOfCanonicalContentIsInSync() {
    var report = report(canonical);
    var database = section(report, "database");

    assertEquals(true, database.get("inSync"));
    assertEquals(section(report, "canonical").get("fingerprints"), database.get("fingerprints"));
  }

  @Test
  void editedAndRemovedCanonicalEventsShowAsDrift() {
    var events = new ArrayList<>(canonical.eventsFile().events());
    var removed = events.remove(events.size() - 1);
    var edited = events.get(0);
    events.set(
        0,
        new CuratedEventJson(
            edited.id(),
            edited.title() + " (revised)",
            edited.year(),
            edited.historicalDate(),
            edited.dateNote(),
            edited.summary(),
            edited.description(),
            edited.notificationTitle(),
            edited.notificationBody(),
            edited.sources(),
            edited.images()));

    var database =
        section(report(new CuratedContent(new CuratedEventsFile(events), canonical.dailyEventsFile())), "database");
    var drift = section(database, "eventDrift");

    assertEquals(false, database.get("inSync"));

    assertEquals(List.of(edited.id()), drift.get("stale"));
    assertEquals(List.of(), drift.get("notImported"));
    assertEquals(List.of(removed.id()), drift.get("onlyInDatabase"));
  }

  private static Map<String, Object> report(CuratedContent historical) {
    var report =
        new ContentStatusReporter()
            .report(
                historical,
                new QuizContentReader(OBJECT_MAPPER).read(Path.of("content/quizzes")),
                List.of(),
                Optional.of(new JdbcContentSnapshotReader(dataSource).read()));
    return report;
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> section(Map<String, Object> report, String name) {
    return (Map<String, Object>) report.get(name);
  }
}
