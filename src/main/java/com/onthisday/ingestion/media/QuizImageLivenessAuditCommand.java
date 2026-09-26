package com.onthisday.ingestion.media;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onthisday.ingestion.quiz.QuizContentReader;
import com.onthisday.ingestion.quiz.QuizContentValidator;
import java.nio.file.Path;

/** Command-line entry point for the explicit pre-release remote image liveness audit. */
public final class QuizImageLivenessAuditCommand {

  private QuizImageLivenessAuditCommand() {}

  public static void main(String[] args) {
    if (args.length > 1) {
      System.err.println("Usage: QuizImageLivenessAuditCommand [quizContentDirectory]");
      System.exit(2);
    }

    var contentDirectory = args.length == 1 ? Path.of(args[0]) : Path.of("content/quizzes");
    try {
      var objectMapper = new ObjectMapper();
      var content = new QuizContentReader(objectMapper).read(contentDirectory);
      var validation = new QuizContentValidator(true).validate(content);
      if (!validation.valid()) {
        System.err.println("Canonical quiz content is invalid; image liveness audit was not run.");
        validation.errors().forEach(error -> System.err.println(error.path() + ": " + error.message()));
        System.exit(2);
      }

      var report = new QuizImageLivenessAudit().audit(content);
      objectMapper.writerWithDefaultPrettyPrinter().writeValue(System.out, report);
      System.out.println();
      if (!report.successful()) {
        System.exit(1);
      }
    } catch (RuntimeException exception) {
      System.err.println("Could not read canonical quiz content for image liveness audit.");
      System.exit(2);
    } catch (java.io.IOException exception) {
      System.err.println("Could not write quiz image liveness report.");
      System.exit(1);
    }
  }
}
