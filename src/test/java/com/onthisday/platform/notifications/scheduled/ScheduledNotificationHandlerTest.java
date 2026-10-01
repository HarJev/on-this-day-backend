package com.onthisday.platform.notifications.scheduled;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.onthisday.content.ContentDate;
import com.onthisday.content.EventSummary;
import com.onthisday.content.FeaturedEvent;
import com.onthisday.content.TodayContent;
import com.onthisday.content.TodayContentRepository;
import com.onthisday.notifications.DevicePlatform;
import com.onthisday.notifications.DeviceRegistration;
import com.onthisday.notifications.DeviceRegistrationRepository;
import com.onthisday.notifications.NotificationDeliveryRepository;
import com.onthisday.notifications.NotificationDeliveryResult;
import com.onthisday.notifications.NotificationDeliveryStatus;
import com.onthisday.notifications.NotificationPermissionStatus;
import com.onthisday.notifications.NotificationSchedule;
import com.onthisday.notifications.NotificationSender;
import com.onthisday.notifications.ScheduledNotificationService;
import com.onthisday.platform.runtime.DatabaseConfig;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class ScheduledNotificationHandlerTest {

  private static final Clock TEN_AM_JAMAICA =
      Clock.fixed(Instant.parse("2026-10-01T15:00:00Z"), ZoneOffset.UTC);

  // Stands in for the plain local database setting; not a credential.
  private static final String LOCAL_VALUE = "x";

  private final AtomicInteger senderLoads = new AtomicInteger();
  private final AtomicInteger sends = new AtomicInteger();

  @Test
  void sendsAndReturnsTheRunSummary() {
    var response = handler(false, NotificationDeliveryStatus.SUCCESS).handleRequest(Map.of(), null);

    assertEquals(1, response.get("sent"));
    assertEquals(false, response.get("dryRun"));
  }

  @Test
  void dryRunInputDoesNotLoadCredentialsOrSend() {
    var response =
        handler(false, NotificationDeliveryStatus.SUCCESS)
            .handleRequest(Map.of("dryRun", true), null);

    assertEquals(1, response.get("wouldSend"));
    assertEquals(0, senderLoads.get());
    assertEquals(0, sends.get());
  }

  @Test
  void dryRunEnvironmentOverridesTheInput() {
    var response =
        handler(true, NotificationDeliveryStatus.SUCCESS).handleRequest(Map.of("dryRun", false), null);

    assertEquals(true, response.get("dryRun"));
    assertEquals(0, sends.get());
  }

  @Test
  void instantInputRunsAsThatTime() {
    var response =
        handler(false, NotificationDeliveryStatus.SUCCESS)
            .handleRequest(Map.of("instant", "2026-10-01T13:00:00Z"), null);

    assertEquals(1, response.get("notDue"));
  }

  @Test
  void loadsCredentialsOnceAcrossWarmInvocations() {
    var handler = handler(false, NotificationDeliveryStatus.SUCCESS);

    handler.handleRequest(Map.of(), null);
    handler.handleRequest(Map.of("instant", "2026-10-02T15:00:00Z"), null);

    assertEquals(1, senderLoads.get());
  }

  @Test
  void configurationFailureFailsTheInvocationSoTheAlarmFires() {
    var handler = handler(false, NotificationDeliveryStatus.CONFIGURATION_FAILURE);

    assertThrows(IllegalStateException.class, () -> handler.handleRequest(Map.of(), null));
  }

  @Test
  void readsTheDatabasePasswordFromSsmWhenNamed() {
    var environment =
        Map.of(
            "DB_JDBC_URL", "jdbc:postgresql://db.example/on_this_day",
            "DB_USER", "runtime",
            "DB_PASSWORD_SSM_PARAMETER", "/on-this-day/prod/db-password");

    var config =
        ScheduledNotificationHandler.databaseConfig(
            environment, name -> name.equals("/on-this-day/prod/db-password") ? "y" : null);

    assertEquals("y", config.password());
  }

  @Test
  void usesThePlainLocalPasswordOtherwise() {
    var environment = new HashMap<String, String>();
    environment.put("DB_JDBC_URL", "jdbc:postgresql://localhost/on_this_day");
    environment.put("DB_USER", "local");
    environment.put(DatabaseConfig.DB_PASSWORD, LOCAL_VALUE);

    var config =
        ScheduledNotificationHandler.databaseConfig(
            environment,
            name -> {
              throw new AssertionError("SSM must not be read");
            });

    assertEquals(LOCAL_VALUE, config.password());
  }

  private ScheduledNotificationHandler handler(
      boolean forceDryRun, NotificationDeliveryStatus status) {
    var service =
        new ScheduledNotificationService(
            new SingleDevice(), new AlwaysClaims(), new AnyDateContent(), NotificationSchedule.DEFAULT);
    NotificationSender sender =
        (token, message) -> {
          sends.incrementAndGet();
          return new NotificationDeliveryResult(status, null);
        };
    return new ScheduledNotificationHandler(
        service,
        () -> {
          senderLoads.incrementAndGet();
          return sender;
        },
        TEN_AM_JAMAICA,
        forceDryRun);
  }

  private static final class SingleDevice implements DeviceRegistrationRepository {
    @Override
    public void upsert(DeviceRegistration registration) {}

    @Override
    public void deleteByToken(String token) {}

    @Override
    public List<DeviceRegistration> findEligibleForNotifications() {
      return List.of(
          new DeviceRegistration(
              "token", DevicePlatform.IOS, "America/Jamaica", NotificationPermissionStatus.AUTHORIZED));
    }
  }

  private static final class AlwaysClaims implements NotificationDeliveryRepository {
    private final Map<String, Boolean> claimed = new HashMap<>();

    @Override
    public boolean claim(
        String token, LocalDate localDate, String eventId, Instant now, Instant leaseUntil) {
      return claimed.putIfAbsent(token + localDate, true) == null;
    }

    @Override
    public void markSent(String token, LocalDate localDate) {}

    @Override
    public void markRetryable(String token, LocalDate localDate, String errorCode) {}

    @Override
    public int deleteBefore(LocalDate cutoff) {
      return 0;
    }
  }

  private static final class AnyDateContent implements TodayContentRepository {
    @Override
    public TodayContent getTodayContent(MonthDay date) {
      return new TodayContent(
          new ContentDate(date.getMonthValue(), date.getDayOfMonth(), "date"),
          new FeaturedEvent("event", "Title", "1900", "date", "Summary", "Hook", "Body", null, null),
          List.<EventSummary>of());
    }
  }
}
