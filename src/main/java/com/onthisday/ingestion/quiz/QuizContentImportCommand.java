package com.onthisday.ingestion.quiz;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onthisday.platform.runtime.CommandDatabaseConfig;
import com.onthisday.platform.runtime.PostgresDataSourceFactory;
import java.nio.file.Path;
import javax.sql.DataSource;
import org.postgresql.ds.PGSimpleDataSource;

public final class QuizContentImportCommand {

  private static final String USAGE =
      "Usage: QuizContentImportCommand [contentDir]  (database from DB_JDBC_URL, DB_USER and"
          + " DB_PASSWORD_SSM_PARAMETER, DB_PASSWORD or a prompt)\n"
          + "       QuizContentImportCommand <jdbcUrl> <user> <password> [contentDir]  (local databases only)";

  private QuizContentImportCommand() {}

  public static void main(String[] args) {
    var legacy = args.length == 3 || args.length == 4;
    if (!legacy && args.length > 1) {
      System.err.println(USAGE);
      System.exit(2);
    }

    DataSource dataSource;
    String contentDir;
    if (legacy) {
      System.err.println(
          "WARNING: a password on the command line is for local databases only. For production, set"
              + " DB_JDBC_URL, DB_USER and DB_PASSWORD_SSM_PARAMETER and pass only [contentDir].");
      var local = new PGSimpleDataSource();
      local.setUrl(args[0]);
      local.setUser(args[1]);
      local.setPassword(args[2]);
      dataSource = local;
      contentDir = args.length == 4 ? args[3] : null;
    } else {
      dataSource = new PostgresDataSourceFactory().create(CommandDatabaseConfig.fromEnvironment());
      contentDir = args.length == 1 ? args[0] : null;
    }

    var reader = new QuizContentReader(new ObjectMapper());
    var importer = new QuizContentImporter(dataSource, new QuizContentValidator());

    try {
      var content = contentDir != null ? reader.read(Path.of(contentDir)) : reader.readDefault();
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
