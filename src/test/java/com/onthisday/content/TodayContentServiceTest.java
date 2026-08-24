package com.onthisday.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Clock;
import java.time.Instant;
import java.time.MonthDay;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

class TodayContentServiceTest {

  @Test
  void resolvesMonthDayUsingRequestedTimezone() {
    var repository = new RecordingTodayContentRepository();
    var service =
        new TodayContentService(
            repository, Clock.fixed(Instant.parse("2026-08-23T03:30:00Z"), ZoneOffset.UTC));

    service.getTodayContent("America/Jamaica");

    assertEquals(MonthDay.of(8, 22), repository.requestedDate);
  }

  @Test
  void resolvesNextDayWhenTimezoneHasAlreadyRolledOver() {
    var repository = new RecordingTodayContentRepository();
    var service =
        new TodayContentService(
            repository, Clock.fixed(Instant.parse("2026-08-22T23:30:00Z"), ZoneOffset.UTC));

    service.getTodayContent("Europe/London");

    assertEquals(MonthDay.of(8, 23), repository.requestedDate);
  }

  @Test
  void rejectsMissingBlankAndInvalidTimezones() {
    var service =
        new TodayContentService(
            new RecordingTodayContentRepository(),
            Clock.fixed(Instant.parse("2026-08-22T12:00:00Z"), ZoneOffset.UTC));

    assertThrows(InvalidTimezoneException.class, () -> service.getTodayContent(null));
    assertThrows(InvalidTimezoneException.class, () -> service.getTodayContent(" "));
    assertThrows(InvalidTimezoneException.class, () -> service.getTodayContent("Not/AZone"));
  }

  @Test
  void propagatesContentUnavailable() {
    var service =
        new TodayContentService(
            date -> {
              throw new ContentUnavailableException("missing");
            },
            Clock.fixed(Instant.parse("2026-08-22T12:00:00Z"), ZoneOffset.UTC));

    assertThrows(ContentUnavailableException.class, () -> service.getTodayContent("America/Jamaica"));
  }

  private static final class RecordingTodayContentRepository implements TodayContentRepository {

    private MonthDay requestedDate;

    @Override
    public TodayContent getTodayContent(MonthDay date) {
      requestedDate = date;
      return new TodayContent(
          new ContentDate(date.getMonthValue(), date.getDayOfMonth(), "Aug 22"),
          new FeaturedEvent(
              "battle-of-bosworth-field-1485",
              "Richard III is defeated at the Battle of Bosworth Field",
              "1485",
              "August 22, 1485",
              "The battle ended the Wars of the Roses.",
              "A king died in battle 541 years ago today",
              "Richard III's defeat at Bosworth changed England forever.",
              null,
              null),
          List.of());
    }
  }
}
