package com.onthisday.notifications;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.onthisday.content.ContentDate;
import com.onthisday.content.ContentUnavailableException;
import com.onthisday.content.EventSummary;
import com.onthisday.content.FeaturedEvent;
import com.onthisday.content.TodayContent;
import com.onthisday.content.TodayContentRepository;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ScheduledNotificationServiceTest {

  private static final LocalDate OCT_1 = LocalDate.of(2026, 10, 1);

  private final FakeDevices devices = new FakeDevices();
  private final InMemoryNotificationDeliveryRepository deliveries =
      new InMemoryNotificationDeliveryRepository();
  private final FakeContent content = new FakeContent();
  private final RecordingSender sender = new RecordingSender();
  private final ScheduledNotificationService service =
      new ScheduledNotificationService(devices, deliveries, content, NotificationSchedule.DEFAULT);

  @ParameterizedTest(name = "{0} in {1} due={2}")
  @CsvSource({
    // Jamaica is UTC-05:00 all year.
    "2026-10-01T14:59:59Z, America/Jamaica, false",
    "2026-10-01T15:00:00Z, America/Jamaica, true",
    "2026-10-01T16:59:59Z, America/Jamaica, true",
    "2026-10-01T17:00:00Z, America/Jamaica, false",
    // Half-hour zone: 10:00 in Kolkata is 04:30 UTC, which a 15-minute schedule hits exactly.
    "2026-10-01T04:15:00Z, Asia/Kolkata, false",
    "2026-10-01T04:30:00Z, Asia/Kolkata, true",
    // Quarter-hour zone: 10:00 in Kathmandu (UTC+05:45) is 04:15 UTC.
    "2026-10-01T04:15:00Z, Asia/Kathmandu, true",
    // New York before and after daylight saving ends on 2026-11-01.
    "2026-10-31T14:00:00Z, America/New_York, true",
    "2026-11-01T14:00:00Z, America/New_York, false",
    "2026-11-01T15:00:00Z, America/New_York, true",
    // Daylight saving starts on 2026-03-08; 10:00 EDT is 14:00 UTC.
    "2026-03-08T14:00:00Z, America/New_York, true",
    "2026-03-08T13:45:00Z, America/New_York, false",
  })
  void sendsOnlyInsideTheLocalTenToNoonWindow(String instant, String timezone, boolean due) {
    devices.add("token", timezone);
    content.availableForAllDates = true;

    var summary = service.run(Instant.parse(instant), sender);

    assertEquals(due ? 1 : 0, summary.sent());
    assertEquals(due ? 0 : 1, summary.notDue());
  }

  @Test
  void usesEachDevicesOwnLocalDateForContent() {
    content.available(MonthDay.of(10, 1), MonthDay.of(10, 2));
    // At 20:00 UTC on Oct 1 it is already 10:00 on Oct 2 in Kiritimati (UTC+14).
    devices.add("kiritimati", "Pacific/Kiritimati");

    service.run(Instant.parse("2026-10-01T20:00:00Z"), sender);

    assertEquals(List.of("kiritimati:event-10-02:daily-2026-10-02"), sender.sent);
    assertEquals("sent", deliveries.row("kiritimati", LocalDate.of(2026, 10, 2)).status());
  }

  @Test
  void sendsOncePerDeviceAndLocalDateAcrossRepeatedRuns() {
    content.availableForAllDates = true;
    devices.add("token", "America/Jamaica");

    var first = service.run(Instant.parse("2026-10-01T15:00:00Z"), sender);
    var second = service.run(Instant.parse("2026-10-01T15:15:00Z"), sender);

    assertEquals(1, first.sent());
    assertEquals(0, second.sent());
    assertEquals(1, second.alreadyHandled());
    assertEquals(1, sender.sent.size());
  }

  @Test
  void lateRunRecoversBeforeNoon() {
    content.availableForAllDates = true;
    devices.add("token", "America/Jamaica");

    // The 10:00 to 11:30 runs never happened.
    var summary = service.run(Instant.parse("2026-10-01T16:45:00Z"), sender);

    assertEquals(1, summary.sent());
  }

  @Test
  void retryableFailureIsRetriedOnlyUntilNoonLocalTime() {
    content.availableForAllDates = true;
    devices.add("token", "America/Jamaica");
    sender.failTransiently = true;

    var first = service.run(Instant.parse("2026-10-01T16:30:00Z"), sender);
    var retried = service.run(Instant.parse("2026-10-01T16:45:00Z"), sender);
    var afterNoon = service.run(Instant.parse("2026-10-01T17:00:00Z"), sender);

    assertEquals(1, first.retryableFailures());
    assertEquals(1, retried.retryableFailures());
    assertEquals(0, afterNoon.due());
    assertEquals(2, sender.attempts);
    var row = deliveries.row("token", OCT_1);
    assertEquals("retryable_failure", row.status());
    assertEquals(2, row.attempts());
    assertEquals("UNAVAILABLE", row.errorCode());
  }

  @Test
  void retryableFailureSucceedsOnALaterRun() {
    content.availableForAllDates = true;
    devices.add("token", "America/Jamaica");
    sender.failTransiently = true;
    service.run(Instant.parse("2026-10-01T15:00:00Z"), sender);

    sender.failTransiently = false;
    var summary = service.run(Instant.parse("2026-10-01T15:15:00Z"), sender);

    assertEquals(1, summary.sent());
    assertEquals("sent", deliveries.row("token", OCT_1).status());
  }

  @Test
  void crashAfterSendIsResentWithTheSameCollapseKeyOnceTheClaimExpires() {
    content.availableForAllDates = true;
    devices.add("token", "America/Jamaica");
    deliveries.failMarkSent = true;

    // Firebase accepts the message, then the run dies before recording success.
    assertThrows(
        IllegalStateException.class,
        () -> service.run(Instant.parse("2026-10-01T15:00:00Z"), sender));
    deliveries.failMarkSent = false;

    // The next run starts while the 15-minute claim is still live: no second sender.
    var whileClaimed = service.run(Instant.parse("2026-10-01T15:14:00Z"), sender);
    // Once the lease has ended, a later run reclaims and sends again.
    var afterExpiry = service.run(Instant.parse("2026-10-01T15:15:01Z"), sender);

    assertEquals(1, whileClaimed.alreadyHandled());
    assertEquals(1, afterExpiry.sent());
    assertEquals(
        List.of(
            "token:event-10-01:daily-2026-10-01", "token:event-10-01:daily-2026-10-01"),
        sender.sent);
    assertEquals("sent", deliveries.row("token", OCT_1).status());
  }

  @Test
  void crashCloseToNoonIsNotResentAfterTheCutoff() {
    content.availableForAllDates = true;
    devices.add("token", "America/Jamaica");
    deliveries.failMarkSent = true;
    assertThrows(
        IllegalStateException.class,
        () -> service.run(Instant.parse("2026-10-01T16:50:00Z"), sender));
    deliveries.failMarkSent = false;

    var afterNoon = service.run(Instant.parse("2026-10-01T17:15:00Z"), sender);

    assertEquals(0, afterNoon.due());
    assertEquals(1, sender.sent.size());
  }

  @Test
  void permanentTokenFailureRemovesTheRegistration() {
    content.availableForAllDates = true;
    devices.add("dead-token", "America/Jamaica");
    sender.permanentTokens.add("dead-token");

    var summary = service.run(Instant.parse("2026-10-01T15:00:00Z"), sender);

    assertEquals(1, summary.permanentTokenFailures());
    assertEquals(List.of("dead-token"), devices.deleted);
  }

  @Test
  void configurationFailureStopsTheRunAndDefersTheRest() {
    content.availableForAllDates = true;
    devices.add("a", "America/Jamaica");
    devices.add("b", "America/Jamaica");
    devices.add("c", "America/Jamaica");
    sender.configurationFailure = true;

    var summary = service.run(Instant.parse("2026-10-01T15:00:00Z"), sender);

    assertTrue(summary.hasConfigurationFailure());
    assertEquals(1, sender.attempts);
    assertEquals(2, summary.deferred());
    assertEquals("retryable_failure", deliveries.row("a", OCT_1).status());
    assertNull(deliveries.row("b", OCT_1));
  }

  @Test
  void stopsClaimingWhenOutOfTimeAndLeavesTheRestForTheNextRun() {
    content.availableForAllDates = true;
    devices.add("a", "America/Jamaica");
    devices.add("b", "America/Jamaica");
    devices.add("c", "America/Jamaica");
    var calls = new int[] {0};

    var first =
        service.run(Instant.parse("2026-10-01T15:00:00Z"), sender, () -> ++calls[0] > 1);
    assertNull(deliveries.row("b", OCT_1));
    var next = service.run(Instant.parse("2026-10-01T15:15:00Z"), sender);

    assertEquals(1, first.sent());
    assertEquals(2, first.deferred());
    assertEquals(2, next.sent());
    assertEquals(1, next.alreadyHandled());
  }

  @Test
  void invalidStoredTimezoneSkipsOnlyThatDevice() {
    content.availableForAllDates = true;
    devices.add("bad-offset", "+05:00");
    devices.add("bad-name", "Mars/Olympus_Mons");
    devices.add("good", "America/Jamaica");

    var summary = service.run(Instant.parse("2026-10-01T15:00:00Z"), sender);

    assertEquals(2, summary.invalidTimezones());
    assertEquals(1, summary.sent());
  }

  @Test
  void missingContentSkipsOnlyDevicesOnThatDate() {
    content.available(MonthDay.of(10, 1));
    devices.add("jamaica", "America/Jamaica");
    // Kiritimati (UTC+14) reaches 10:00 on Oct 2, which has no content, at 20:00 UTC Oct 1.
    devices.add("kiritimati", "Pacific/Kiritimati");

    var jamaicaRun = service.run(Instant.parse("2026-10-01T15:00:00Z"), sender);
    var kiritimatiRun = service.run(Instant.parse("2026-10-01T20:00:00Z"), sender);

    assertEquals(1, jamaicaRun.sent());
    assertEquals(1, kiritimatiRun.noContent());
    assertEquals(0, kiritimatiRun.sent());
    assertNull(deliveries.row("kiritimati", LocalDate.of(2026, 10, 2)));
  }

  @Test
  void databaseFailureWhileLoadingContentFailsTheRun() {
    content.databaseDown = true;
    devices.add("token", "America/Jamaica");

    assertThrows(
        ContentUnavailableException.class,
        () -> service.run(Instant.parse("2026-10-01T15:00:00Z"), sender));
  }

  @Test
  void emptyRunStillProducesASummary() {
    var summary = service.run(Instant.parse("2026-10-01T15:00:00Z"), sender);

    assertEquals(0, summary.eligibleDevices());
    assertEquals(0, summary.sent());
    assertFalse(summary.dryRun());
  }

  @Test
  void dryRunNeitherClaimsNorSends() {
    content.availableForAllDates = true;
    devices.add("token", "America/Jamaica");

    var summary = service.dryRun(Instant.parse("2026-10-01T15:00:00Z"));

    assertTrue(summary.dryRun());
    assertEquals(1, summary.wouldSend());
    assertTrue(deliveries.rows.isEmpty());
    assertEquals(0, sender.attempts);
  }

  @Test
  void prunesRowsOlderThanTheRetentionPeriod() {
    deliveries.claim("old", LocalDate.of(2026, 8, 1), "e", Instant.EPOCH, Instant.EPOCH);
    deliveries.claim("recent", LocalDate.of(2026, 9, 20), "e", Instant.EPOCH, Instant.EPOCH);

    var summary = service.run(Instant.parse("2026-10-01T15:00:00Z"), sender);

    assertEquals(1, summary.pruned());
    assertEquals(1, deliveries.rows.size());
  }

  private static final class FakeDevices implements DeviceRegistrationRepository {
    private final List<DeviceRegistration> registrations = new ArrayList<>();
    private final List<String> deleted = new ArrayList<>();

    void add(String token, String timezone) {
      registrations.add(
          new DeviceRegistration(
              token, DevicePlatform.IOS, timezone, NotificationPermissionStatus.AUTHORIZED));
    }

    @Override
    public void upsert(DeviceRegistration registration) {}

    @Override
    public void deleteByToken(String token) {
      deleted.add(token);
      registrations.removeIf(registration -> registration.token().equals(token));
    }

    @Override
    public List<DeviceRegistration> findEligibleForNotifications() {
      return List.copyOf(registrations);
    }
  }

  private static final class FakeContent implements TodayContentRepository {
    private final Set<MonthDay> dates = new HashSet<>();
    private boolean availableForAllDates;
    private boolean databaseDown;

    void available(MonthDay... monthDays) {
      dates.addAll(List.of(monthDays));
    }

    @Override
    public TodayContent getTodayContent(MonthDay date) {
      if (databaseDown) {
        throw new ContentUnavailableException("down", new SQLException("connection refused"));
      }
      if (!availableForAllDates && !dates.contains(date)) {
        throw new ContentUnavailableException("Featured content unavailable");
      }
      var suffix = String.format("%02d-%02d", date.getMonthValue(), date.getDayOfMonth());
      return new TodayContent(
          new ContentDate(date.getMonthValue(), date.getDayOfMonth(), suffix),
          new FeaturedEvent(
              "event-" + suffix, "Title", "1900", "date", "Summary", "Hook", "Body", null, null),
          List.<EventSummary>of());
    }
  }

  private final class RecordingSender implements NotificationSender {
    private final List<String> sent = new ArrayList<>();
    private final Set<String> permanentTokens = new HashSet<>();
    private boolean failTransiently;
    private boolean configurationFailure;
    private int attempts;

    @Override
    public NotificationDeliveryResult send(String token, NotificationMessage message) {
      attempts++;
      if (configurationFailure) {
        return new NotificationDeliveryResult(
            NotificationDeliveryStatus.CONFIGURATION_FAILURE, "PERMISSION_DENIED");
      }
      if (permanentTokens.contains(token)) {
        deliveries.removeToken(token);
        return new NotificationDeliveryResult(
            NotificationDeliveryStatus.PERMANENT_TOKEN_FAILURE, "UNREGISTERED");
      }
      if (failTransiently) {
        return new NotificationDeliveryResult(
            NotificationDeliveryStatus.TRANSIENT_FAILURE, "UNAVAILABLE");
      }
      sent.add(token + ":" + message.eventId() + ":" + message.collapseKey());
      return NotificationDeliveryResult.success();
    }
  }
}
