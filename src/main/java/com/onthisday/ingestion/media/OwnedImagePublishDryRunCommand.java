package com.onthisday.ingestion.media;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.nio.file.Path;

/** Command-line entry point for an offline pre-publication asset check. */
public final class OwnedImagePublishDryRunCommand {

  private OwnedImagePublishDryRunCommand() {}

  public static void main(String[] args) {
    if (args.length < 2 || args.length > 3) {
      System.err.println(
          "Usage: OwnedImagePublishDryRunCommand <manifestPath> <assetRoot> [ownedHttpsOrigin]");
      System.exit(2);
    }

    try {
      var objectMapper = new ObjectMapper();
      var manifest = new OwnedImageManifestReader(objectMapper).read(Path.of(args[0]));
      var origin = args.length == 3 ? URI.create(args[2]) : null;
      var plan = new OwnedImageAssetValidator().validateAndPlan(manifest, Path.of(args[1]), origin);
      objectMapper.writerWithDefaultPrettyPrinter().writeValue(System.out, plan);
      System.out.println();
    } catch (OwnedImageValidationException exception) {
      System.err.println(exception.getMessage());
      for (var error : exception.validationErrors()) {
        System.err.println(error.path() + ": " + error.message());
      }
      System.exit(1);
    } catch (IllegalArgumentException exception) {
      System.err.println("ownedHttpsOrigin must be a valid URI");
      System.exit(2);
    } catch (java.io.IOException exception) {
      System.err.println("Could not write owned image publish plan.");
      System.exit(1);
    }
  }
}
