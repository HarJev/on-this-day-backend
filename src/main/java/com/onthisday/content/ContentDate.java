package com.onthisday.content;

import static com.onthisday.content.ContentChecks.requireNonBlank;

public record ContentDate(int month, int day, String displayDate) {

  public ContentDate {
    if (month < 1 || month > 12) {
      throw new IllegalArgumentException("month must be between 1 and 12");
    }
    if (day < 1 || day > 31) {
      throw new IllegalArgumentException("day must be between 1 and 31");
    }
    displayDate = requireNonBlank(displayDate, "displayDate");
  }
}
