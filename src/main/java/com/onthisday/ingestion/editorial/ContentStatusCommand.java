package com.onthisday.ingestion.editorial;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onthisday.ingestion.CuratedContentReader;
import com.onthisday.ingestion.CuratedDailyEventsFile;
import com.onthisday.ingestion.CuratedEventsFile;
import com.onthisday.ingestion.editorial.ContentStatusReporter.BatchState;
import com.onthisday.ingestion.quiz.QuizContentReader;
import com.onthisday.platform.runtime.CommandDatabaseConfig;
import com.onthisday.platform.runtime.PostgresDataSourceFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Writes a deterministic JSON status report. When {@code DB_JDBC_URL} and {@code DB_USER} are set
 * it also compares canonical content with that database, read-only, taking the password as {@link
 * CommandDatabaseConfig} does. Apart from that optional SSM read it performs no HTTP calls, and it
 * never approves, promotes, or imports content.
 */
public final class ContentStatusCommand {

  private ContentStatusCommand() {}

  public static void main(String[] args) {
    if (args.length != 4) {
      System.err.println(
          "Usage: ContentStatusCommand <historicalContentDir> <quizContentDir> <editorialBatchesDir> <outputFile>");
      System.exit(2);
    }
    try {
      var objectMapper = new ObjectMapper();
      var database = databaseSnapshot(System.getenv());
      var report =
          new ContentStatusReporter()
              .report(
                  new CuratedContentReader(objectMapper).read(Path.of(args[0])),
                  new QuizContentReader(objectMapper).read(Path.of(args[1])),
                  batches(objectMapper, Path.of(args[2])),
                  database);
      var output = Path.of(args[3]);
      var parent = output.getParent();
      if (parent != null) {
        Files.createDirectories(parent);
      }
      objectMapper.writerWithDefaultPrettyPrinter().writeValue(output.toFile(), report);
      System.out.println(
          "Content status report written to "
              + output
              + (database.isPresent() ? " (database compared)." : " (database not checked)."));
    } catch (Exception exception) {
      System.err.println("Could not produce content status report: " + exception.getMessage());
      System.exit(1);
    }
  }

  static List<BatchState> batches(ObjectMapper objectMapper, Path batchesDir) throws IOException {
    var reader = new EditorialContentReader(objectMapper);
    var batches = new ArrayList<BatchState>();
    try (Stream<Path> children = Files.list(batchesDir)) {
      for (var directory : children.filter(Files::isDirectory).sorted().toList()) {
        var manifest = directory.resolve("batch.json");
        if (!Files.isRegularFile(manifest)) {
          continue;
        }
        var batch = reader.readBatch(manifest);
        var draftEvents = directory.resolve("draft-events.json");
        var draftDays = directory.resolve("draft-daily-events.json");
        batches.add(
            new BatchState(
                batch.manifest().batchId(),
                batch.reviewLedger(),
                Files.isRegularFile(draftEvents)
                    ? objectMapper.readValue(draftEvents.toFile(), CuratedEventsFile.class).events()
                    : List.of(),
                Files.isRegularFile(draftDays)
                    ? objectMapper.readValue(draftDays.toFile(), CuratedDailyEventsFile.class).days()
                    : List.of()));
      }
    }
    return batches;
  }

  private static Optional<JdbcContentSnapshotReader.ContentSnapshot> databaseSnapshot(Map<String, String> env) {
    var url = env.get("DB_JDBC_URL");
    if (url == null || url.isBlank()) {
      return Optional.empty();
    }
    var dataSource = new PostgresDataSourceFactory().create(CommandDatabaseConfig.fromEnvironment(env));
    return Optional.of(new JdbcContentSnapshotReader(dataSource).read());
  }
}
