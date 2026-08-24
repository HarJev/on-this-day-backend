package com.onthisday.content;

import static com.onthisday.content.ContentChecks.requireNonBlank;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.ZoneId;
import java.util.Objects;

public final class TodayContentService {

  private final TodayContentRepository repository;
  private final Clock clock;

  public TodayContentService(TodayContentRepository repository, Clock clock) {
    this.repository = Objects.requireNonNull(repository, "repository must not be null");
    this.clock = Objects.requireNonNull(clock, "clock must not be null");
  }

  public TodayContent getTodayContent(String timezone) {
    var zoneId = parseTimezone(timezone);
    var localDate = LocalDate.now(clock.withZone(zoneId));
    return repository.getTodayContent(MonthDay.from(localDate));
  }

  private static ZoneId parseTimezone(String timezone) {
    try {
      return ZoneId.of(requireNonBlank(timezone, "timezone"));
    } catch (IllegalArgumentException | DateTimeException exception) {
      throw new InvalidTimezoneException("Invalid timezone.", exception);
    }
  }
}
