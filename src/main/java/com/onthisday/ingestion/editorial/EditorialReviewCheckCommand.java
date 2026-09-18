package com.onthisday.ingestion.editorial;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Path;

/** Offline check for candidate and human-review evidence. It never reads canonical content or opens a database. */
public final class EditorialReviewCheckCommand {

  private EditorialReviewCheckCommand() {}

  public static void main(String[] args) {
    if (args.length != 1) {
      System.err.println("Usage: EditorialReviewCheckCommand <batchManifest>");
      System.exit(2);
    }
    try {
      var batch = new EditorialContentReader(new ObjectMapper()).readBatch(Path.of(args[0]));
      var result = new EditorialReviewValidator().validate(batch);
      if (!result.valid()) {
        result.errors().forEach(error -> System.err.println(error.path() + ": " + error.message()));
        System.exit(1);
      }
      System.out.println("Editorial review check complete for batch " + batch.manifest().batchId() + ".");
    } catch (EditorialContentException exception) {
      System.err.println(exception.getMessage());
      System.exit(1);
    }
  }
}
