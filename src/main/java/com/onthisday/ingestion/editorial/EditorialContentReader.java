package com.onthisday.ingestion.editorial;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public class EditorialContentReader {

  private final ObjectMapper objectMapper;

  public EditorialContentReader(ObjectMapper objectMapper) {
    this.objectMapper =
        Objects.requireNonNull(objectMapper, "objectMapper must not be null")
            .copy()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
  }

  public EditorialBatch readBatch(Path manifestPath) {
    Objects.requireNonNull(manifestPath, "manifestPath must not be null");
    try {
      var normalizedManifestPath = manifestPath.normalize();
      var manifest = objectMapper.readValue(normalizedManifestPath.toFile(), EditorialBatchManifest.class);
      var parent = normalizedManifestPath.getParent();
      if (parent == null) {
        throw new EditorialContentException("Editorial batch manifest must have a parent directory.");
      }
      var candidates =
          objectMapper.readValue(parent.resolve(manifest.candidateFile()).normalize().toFile(), EditorialCandidatesFile.class);
      var ledger =
          objectMapper.readValue(parent.resolve(manifest.reviewLedgerFile()).normalize().toFile(), EditorialReviewLedger.class);
      return new EditorialBatch(manifest, candidates, ledger);
    } catch (IOException exception) {
      throw new EditorialContentException("Could not read editorial workflow JSON.", exception);
    }
  }
}
