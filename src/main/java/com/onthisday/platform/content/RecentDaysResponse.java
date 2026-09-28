package com.onthisday.platform.content;

import java.util.List;

public record RecentDaysResponse(List<RecentDayResponse> days) {

  public RecentDaysResponse {
    days = List.copyOf(days);
  }

  public record RecentDayResponse(
      int daysAgo, ApiContentDateResponse date, ApiEventSummaryResponse featuredEvent) {}
}
