package com.onthisday.ingestion.media;

import java.util.List;

/** Immutable, reviewable description of locally staged image renditions. */
public record OwnedImageManifest(int schemaVersion, List<OwnedImageManifestEntry> assets) {

  public OwnedImageManifest {
    assets = assets == null ? null : List.copyOf(assets);
  }
}
