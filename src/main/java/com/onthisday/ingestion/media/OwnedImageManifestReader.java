package com.onthisday.ingestion.media;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

/** Reads a manifest strictly so editorial spelling mistakes cannot be ignored. */
public final class OwnedImageManifestReader {

  private final ObjectMapper objectMapper;

  public OwnedImageManifestReader(ObjectMapper objectMapper) {
    this.objectMapper =
        Objects.requireNonNull(objectMapper, "objectMapper must not be null")
            .copy()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
  }

  public OwnedImageManifest read(Path manifestPath) {
    Objects.requireNonNull(manifestPath, "manifestPath must not be null");
    try {
      return objectMapper.readValue(manifestPath.toFile(), OwnedImageManifest.class);
    } catch (IOException exception) {
      throw new OwnedImageValidationException("Could not read owned image manifest JSON.", exception);
    }
  }
}
