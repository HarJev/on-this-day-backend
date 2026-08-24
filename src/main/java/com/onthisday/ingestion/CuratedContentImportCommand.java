package com.onthisday.ingestion;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Path;
import org.postgresql.ds.PGSimpleDataSource;

public final class CuratedContentImportCommand {

  private CuratedContentImportCommand() {}

  public static void main(String[] args) {
    if (args.length < 3 || args.length > 4) {
      System.err.println("Usage: CuratedContentImportCommand <jdbcUrl> <user> <password> [contentDir]");
      System.exit(2);
    }

    var dataSource = new PGSimpleDataSource();
    dataSource.setUrl(args[0]);
    dataSource.setUser(args[1]);
    dataSource.setPassword(args[2]);

    var reader = new CuratedContentReader(new ObjectMapper());
    var importer = new CuratedContentImporter(dataSource, new CuratedContentValidator());

    try {
      var content = args.length == 4 ? reader.read(Path.of(args[3])) : reader.readDefault();
      importer.importContent(content);
      System.out.println("Curated content import complete.");
    } catch (ContentImportException exception) {
      System.err.println(exception.getMessage());
      for (var error : exception.validationErrors()) {
        System.err.println(error.path() + ": " + error.message());
      }
      System.exit(1);
    }
  }
}
