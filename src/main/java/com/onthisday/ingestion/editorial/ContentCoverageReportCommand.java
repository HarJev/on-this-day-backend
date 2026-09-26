package com.onthisday.ingestion.editorial;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onthisday.ingestion.CuratedContentReader;
import com.onthisday.ingestion.quiz.QuizContentReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Writes a deterministic JSON audit report. It performs no HTTP calls and changes no content or database rows. */
public final class ContentCoverageReportCommand {

  private ContentCoverageReportCommand() {}

  public static void main(String[] args) {
    if (args.length < 3) {
      System.err.println("Usage: ContentCoverageReportCommand <historicalContentDir> <quizContentDir> <outputFile> [batchManifest...]");
      System.exit(2);
    }
    try {
      var objectMapper = new ObjectMapper();
      var ledgers = new ArrayList<EditorialReviewLedger>();
      var reader = new EditorialContentReader(objectMapper);
      for (int index = 3; index < args.length; index++) {
        ledgers.add(reader.readBatch(Path.of(args[index])).reviewLedger());
      }
      var report =
          new ContentCoverageReporter()
              .report(
                  new CuratedContentReader(objectMapper).read(Path.of(args[0])),
                  new QuizContentReader(objectMapper).read(Path.of(args[1])),
                  ledgers);
      var output = Path.of(args[2]);
      var parent = output.getParent();
      if (parent != null) {
        Files.createDirectories(parent);
      }
      objectMapper.writerWithDefaultPrettyPrinter().writeValue(output.toFile(), report);
      System.out.println("Coverage report written to " + output + ".");
    } catch (Exception exception) {
      System.err.println("Could not produce coverage report: " + exception.getMessage());
      System.exit(1);
    }
  }
}
