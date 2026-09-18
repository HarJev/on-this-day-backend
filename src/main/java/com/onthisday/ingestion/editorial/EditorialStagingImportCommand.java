package com.onthisday.ingestion.editorial;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onthisday.ingestion.CuratedContentImporter;
import com.onthisday.ingestion.CuratedContentReader;
import com.onthisday.ingestion.CuratedContentValidator;
import com.onthisday.ingestion.quiz.QuizContentImporter;
import com.onthisday.ingestion.quiz.QuizContentReader;
import com.onthisday.ingestion.quiz.QuizContentValidator;
import java.nio.file.Path;
import org.postgresql.ds.PGSimpleDataSource;

/** Explicitly imports one approved batch with staging database configuration supplied by the environment. */
public final class EditorialStagingImportCommand {

  private EditorialStagingImportCommand() {}

  public static void main(String[] args) {
    if (args.length < 1 || args.length > 3) {
      System.err.println(
          "Usage: EditorialStagingImportCommand <batchManifest> [historicalContentDir] [quizContentDir]");
      System.exit(2);
    }
    try {
      var objectMapper = new ObjectMapper();
      var batch = new EditorialContentReader(objectMapper).readBatch(Path.of(args[0]));
      var historicalDir = args.length >= 2 ? Path.of(args[1]) : Path.of("content");
      var quizDir = args.length == 3 ? Path.of(args[2]) : Path.of("content", "quizzes");
      var preflight =
          new EditorialBatchPreflight(
              new EditorialReviewValidator(),
              new CuratedContentValidator(),
              new QuizContentValidator(),
              new EditorialBatchSelector());
      var result =
          preflight.validate(
              batch,
              new CuratedContentReader(objectMapper).read(historicalDir),
              new QuizContentReader(objectMapper).read(quizDir));
      if (!result.valid()) {
        printFailure(result);
        System.exit(1);
      }
      var dataSource = new PGSimpleDataSource();
      dataSource.setUrl(requiredEnvironment("EDITORIAL_STAGING_DB_JDBC_URL"));
      dataSource.setUser(requiredEnvironment("EDITORIAL_STAGING_DB_USER"));
      dataSource.setPassword(requiredEnvironment("EDITORIAL_STAGING_DB_PASSWORD"));
      new CuratedContentImporter(dataSource, new CuratedContentValidator()).importContent(result.historicalContent());
      new QuizContentImporter(dataSource, new QuizContentValidator()).importContent(result.quizContent());
      System.out.println("Editorial staging import complete for batch " + batch.manifest().batchId() + ".");
    } catch (EditorialContentException exception) {
      System.err.println(exception.getMessage());
      System.exit(1);
    }
  }

  private static String requiredEnvironment(String name) {
    var value = System.getenv(name);
    if (value == null || value.isBlank()) {
      throw new EditorialContentException("Missing required environment variable: " + name);
    }
    return value;
  }

  private static void printFailure(EditorialPreflightResult result) {
    result.reviewValidation().errors().forEach(error -> System.err.println(error.path() + ": " + error.message()));
    result.historicalValidation().errors().forEach(error -> System.err.println(error.path() + ": " + error.message()));
    result.quizValidation().errors().forEach(error -> System.err.println(error.path() + ": " + error.message()));
    result.approvalErrors().forEach(System.err::println);
  }
}
