package com.onthisday.ingestion.media;

import java.util.List;

/** Dry-run output. It contains no AWS credentials and does not upload anything. */
public record OwnedImagePublishPlan(List<OwnedImagePublishPlanEntry> entries) {

  public OwnedImagePublishPlan {
    entries = List.copyOf(entries);
  }
}
