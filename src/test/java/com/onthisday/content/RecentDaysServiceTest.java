package com.onthisday.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Clock;
import java.time.Instant;
import java.time.MonthDay;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RecentDaysServiceTest {

  @Test
  void returnsPreviousSixLocalDatesNewestFirstForDefaultWindow() {
    var repository = new FakeRepository(Set.of());
    var service = service(repository, "2026-08-23T03:30:00Z");

    var recentDays = service.getRecentDays("America/Jamaica", RecentDaysService.DEFAULT_DAYS);

    assertEquals(
        List.of(
            MonthDay.of(8, 21),
            MonthDay.of(8, 20),
            MonthDay.of(8, 19),
            MonthDay.of(8, 18),
            MonthDay.of(8, 17),
            MonthDay.of(8, 16)),
        repository.requestedDates);
    assertEquals(6, recentDays.size());
    assertEquals(1, recentDays.get(0).daysAgo());
    assertEquals(new ContentDate(8, 21, "Aug 21"), recentDays.get(0).date());
    assertEquals("event-8-21", recentDays.get(0).featuredEvent().id());
    assertEquals(6, recentDays.get(5).daysAgo());
    assertEquals(new ContentDate(8, 16, "Aug 16"), recentDays.get(5).date());
  }

  @Test
  void skipsDatesWithoutFeaturedContentAndKeepsDaysAgo() {
    var repository = new FakeRepository(Set.of(MonthDay.of(8, 20), MonthDay.of(8, 18)));
    var service = service(repository, "2026-08-22T12:00:00Z");

    var recentDays = service.getRecentDays("UTC", 4);

    assertEquals(2, recentDays.size());
    assertEquals(1, recentDays.get(0).daysAgo());
    assertEquals(new ContentDate(8, 21, "Aug 21"), recentDays.get(0).date());
    assertEquals(3, recentDays.get(1).daysAgo());
    assertEquals(new ContentDate(8, 19, "Aug 19"), recentDays.get(1).date());
  }

  @Test
  void crossesYearAndLeapDayBoundaries() {
    var newYear = new FakeRepository(Set.of());
    service(newYear, "2027-01-02T12:00:00Z").getRecentDays("UTC", 3);
    assertEquals(List.of(MonthDay.of(1, 1), MonthDay.of(12, 31)), newYear.requestedDates);

    var leapYear = new FakeRepository(Set.of());
    service(leapYear, "2028-03-01T12:00:00Z").getRecentDays("UTC", 2);
    assertEquals(List.of(MonthDay.of(2, 29)), leapYear.requestedDates);
  }

  @Test
  void rejectsInvalidTimezonesAndOutOfRangeDays() {
    var service = service(new FakeRepository(Set.of()), "2026-08-22T12:00:00Z");

    assertThrows(InvalidTimezoneException.class, () -> service.getRecentDays(null, 7));
    assertThrows(InvalidTimezoneException.class, () -> service.getRecentDays(" ", 7));
    assertThrows(InvalidTimezoneException.class, () -> service.getRecentDays("Not/AZone", 7));
    assertThrows(InvalidRecentDaysRequestException.class, () -> service.getRecentDays("UTC", 1));
    assertThrows(
        InvalidRecentDaysRequestException.class,
        () -> service.getRecentDays("UTC", RecentDaysService.MAX_DAYS + 1));
  }

  @Test
  void defaultRepositoryLookupSkipsDatesWithoutFeaturedContent() {
    TodayContentRepository repository =
        date -> {
          if (date.equals(MonthDay.of(8, 20))) {
            throw new ContentUnavailableException("missing");
          }
          return new TodayContent(
              new ContentDate(date.getMonthValue(), date.getDayOfMonth(), "Aug"),
              new FeaturedEvent(
                  "event-" + date.getDayOfMonth(),
                  "Title",
                  "1485",
                  "August 1485",
                  "Summary.",
                  "Notification title",
                  "Notification body",
                  null,
                  null),
              List.of());
        };

    var summaries =
        repository.findFeaturedEventSummaries(List.of(MonthDay.of(8, 21), MonthDay.of(8, 20)));

    assertEquals(Set.of(MonthDay.of(8, 21)), summaries.keySet());
    assertEquals("event-21", summaries.get(MonthDay.of(8, 21)).id());
  }

  private static RecentDaysService service(TodayContentRepository repository, String instant) {
    return new RecentDaysService(repository, Clock.fixed(Instant.parse(instant), ZoneOffset.UTC));
  }

  private static final class FakeRepository implements TodayContentRepository {

    private final Set<MonthDay> emptyDates;
    private List<MonthDay> requestedDates;

    private FakeRepository(Set<MonthDay> emptyDates) {
      this.emptyDates = emptyDates;
    }

    @Override
    public TodayContent getTodayContent(MonthDay date) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Map<MonthDay, EventSummary> findFeaturedEventSummaries(List<MonthDay> dates) {
      requestedDates = List.copyOf(dates);
      var summaries = new java.util.HashMap<MonthDay, EventSummary>();
      for (var date : dates) {
        if (!emptyDates.contains(date)) {
          summaries.put(
              date,
              new EventSummary(
                  "event-" + date.getMonthValue() + "-" + date.getDayOfMonth(),
                  "Title",
                  "1485",
                  "August 1485",
                  null));
        }
      }
      return summaries;
    }
  }
}
