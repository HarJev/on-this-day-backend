package com.onthisday.content;

import java.time.Clock;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class RecentDaysService {

  public static final int DEFAULT_DAYS = 7;
  public static final int MIN_DAYS = 2;
  public static final int MAX_DAYS = 14;

  private static final DateTimeFormatter DISPLAY_DATE_FORMATTER =
      DateTimeFormatter.ofPattern("MMM d", Locale.US);

  private final TodayContentRepository repository;
  private final Clock clock;

  public RecentDaysService(TodayContentRepository repository, Clock clock) {
    this.repository = Objects.requireNonNull(repository, "repository must not be null");
    this.clock = Objects.requireNonNull(clock, "clock must not be null");
  }

  /**
   * Returns the featured event for each local date before today inside a window of {@code days}
   * dates that ends today, newest first. Dates without featured content are skipped.
   */
  public List<RecentDay> getRecentDays(String timezone, int days) {
    var zoneId = TodayContentService.parseTimezone(timezone);
    if (days < MIN_DAYS || days > MAX_DAYS) {
      throw new InvalidRecentDaysRequestException(
          "days must be between " + MIN_DAYS + " and " + MAX_DAYS);
    }

    var today = LocalDate.now(clock.withZone(zoneId));
    var pastDates = new ArrayList<MonthDay>();
    for (int daysAgo = 1; daysAgo < days; daysAgo++) {
      pastDates.add(MonthDay.from(today.minusDays(daysAgo)));
    }

    var featuredEvents = repository.findFeaturedEventSummaries(pastDates);
    var recentDays = new ArrayList<RecentDay>();
    for (int index = 0; index < pastDates.size(); index++) {
      var date = pastDates.get(index);
      var featuredEvent = featuredEvents.get(date);
      if (featuredEvent != null) {
        recentDays.add(new RecentDay(index + 1, contentDate(date), featuredEvent));
      }
    }
    return List.copyOf(recentDays);
  }

  private static ContentDate contentDate(MonthDay date) {
    return new ContentDate(
        date.getMonthValue(),
        date.getDayOfMonth(),
        date.atYear(2000).format(DISPLAY_DATE_FORMATTER));
  }
}
