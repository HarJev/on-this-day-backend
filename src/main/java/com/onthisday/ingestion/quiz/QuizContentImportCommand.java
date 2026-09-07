package com.onthisday.ingestion.quiz;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Path;
import org.postgresql.ds.PGSimpleDataSource;

public final class QuizContentImportCommand {

  private QuizContentImportCommand() {}

  public static void main(String[] args) {
    if (args.length < 3 || args.length > 4) {
      System.err.println("Usage: QuizContentImportCommand <jdbcUrl> <user> <password> [contentDir]");
      System.exit(2);
    }

    var dataSource = new PGSimpleDataSource();
    dataSource.setUrl(args[0]);
    dataSource.setUser(args[1]);
    dataSource.setPassword(args[2]);

    var reader = new QuizContentReader(new ObjectMapper());
    var importer = new QuizContentImporter(dataSource, new QuizContentValidator());

    try {
      var content = args.length == 4 ? reader.read(Path.of(args[3])) : reader.readDefault();
      var result = importer.importContent(content);
      System.out.println("Curated quiz content import complete.");
      for (var warning : result.warnings()) {
        System.out.println("WARNING " + warning.path() + ": " + warning.message());
      }
    } catch (QuizContentImportException exception) {
      System.err.println(exception.getMessage());
      for (var error : exception.validationErrors()) {
        System.err.println(error.path() + ": " + error.message());
      }
      System.exit(1);
    }
  }
}
