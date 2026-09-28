package com.onthisday.content;

import java.time.MonthDay;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface TodayContentRepository {

  TodayContent getTodayContent(MonthDay date);

  /**
   * Returns the featured event summary for each requested date that has featured content. Dates
   * without featured content are absent from the result.
   */
  default Map<MonthDay, EventSummary> findFeaturedEventSummaries(List<MonthDay> dates) {
    var summaries = new HashMap<MonthDay, EventSummary>();
    for (var date : dates) {
      try {
        var featured = getTodayContent(date).featuredEvent();
        summaries.put(
            date,
            new EventSummary(
                featured.id(),
                featured.title(),
                featured.year(),
                featured.historicalDate(),
                featured.dateNote()));
      } catch (ContentUnavailableException exception) {
        // No featured content for this date.
      }
    }
    return Map.copyOf(summaries);
  }
}
