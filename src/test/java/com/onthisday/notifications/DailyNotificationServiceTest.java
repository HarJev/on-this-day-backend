package com.onthisday.notifications;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.onthisday.content.ContentDate;
import com.onthisday.content.EventSummary;
import com.onthisday.content.FeaturedEvent;
import com.onthisday.content.TodayContent;
import com.onthisday.content.TodayContentService;
import java.time.Clock;
import java.time.Instant;
import java.time.MonthDay;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class DailyNotificationServiceTest {

  @Test
  void createsOnePayloadBatchForEachRegisteredTimezone() {
    var devices = new RecordingDeviceRegistrationRepository();
    devices.eligible =
        List.of(
            registration("jamaica-token", "America/Jamaica"),
            registration("utc-token-1", "UTC"),
            registration("utc-token-2", "UTC"));
    var content = new RecordingTodayContentRepository();
    var service = service(devices, content);

    var plan = service.createPlan();

    assertEquals(3, plan.recipientCount());
    assertEquals(2, plan.batches().size());
    assertEquals(
        new NotificationMessage("August 23 notification", "August 23 body", "event-23"),
        plan.batches().get(0).message());
    assertEquals(List.of("jamaica-token"), plan.batches().get(0).recipientTokens());
    assertEquals(
        new NotificationMessage("August 24 notification", "August 24 body", "event-24"),
        plan.batches().get(1).message());
    assertEquals(List.of("utc-token-1", "utc-token-2"), plan.batches().get(1).recipientTokens());
    assertEquals(List.of(MonthDay.of(8, 23), MonthDay.of(8, 24)), content.requestedDates);
  }

  @Test
  void deletesOnlyTokensClassifiedAsPermanentFailures() {
    var devices = new RecordingDeviceRegistrationRepository();
    var service = service(devices, new RecordingTodayContentRepository());
    var message = new NotificationMessage("Title", "Body", "event-id");
    var plan =
        new NotificationPlan(
            List.of(
                new NotificationDeliveryBatch(
                    message, List.of("success", "permanent", "transient", "configuration"))));

    var summary =
        service.send(
            plan,
            (token, ignored) ->
                switch (token) {
                  case "success" -> NotificationDeliveryResult.success();
                  case "permanent" ->
                      new NotificationDeliveryResult(
                          NotificationDeliveryStatus.PERMANENT_TOKEN_FAILURE, "UNREGISTERED");
                  case "transient" ->
                      new NotificationDeliveryResult(
                          NotificationDeliveryStatus.TRANSIENT_FAILURE, "UNAVAILABLE");
                  default ->
                      new NotificationDeliveryResult(
                          NotificationDeliveryStatus.CONFIGURATION_FAILURE, "PERMISSION_DENIED");
                });

    assertEquals(1, summary.successes());
    assertEquals(1, summary.permanentTokenFailures());
    assertEquals(1, summary.transientFailures());
    assertEquals(1, summary.configurationFailures());
    assertEquals(List.of("permanent"), devices.deletedTokens);
  }

  private static DailyNotificationService service(
      DeviceRegistrationRepository devices, RecordingTodayContentRepository content) {
    var clock = Clock.fixed(Instant.parse("2026-08-24T01:00:00Z"), ZoneOffset.UTC);
    return new DailyNotificationService(devices, new TodayContentService(content, clock));
  }

  private static DeviceRegistration registration(String token, String timezone) {
    return new DeviceRegistration(
        token, DevicePlatform.IOS, timezone, NotificationPermissionStatus.AUTHORIZED);
  }

  private static final class RecordingDeviceRegistrationRepository
      implements DeviceRegistrationRepository {

    private List<DeviceRegistration> eligible = List.of();
    private final List<String> deletedTokens = new ArrayList<>();

    @Override
    public void upsert(DeviceRegistration registration) {}

    @Override
    public void deleteByToken(String token) {
      deletedTokens.add(token);
    }

    @Override
    public List<DeviceRegistration> findEligibleForNotifications() {
      return eligible;
    }
  }

  private static final class RecordingTodayContentRepository
      implements com.onthisday.content.TodayContentRepository {

    private final List<MonthDay> requestedDates = new ArrayList<>();

    @Override
    public TodayContent getTodayContent(MonthDay date) {
      requestedDates.add(date);
      var day = date.getDayOfMonth();
      return new TodayContent(
          new ContentDate(date.getMonthValue(), day, "Aug " + day),
          new FeaturedEvent(
              "event-" + day,
              "Event " + day,
              "2026",
              "August " + day + ", 2026",
              "Summary",
              "August " + day + " notification",
              "August " + day + " body",
              null,
              null),
          List.<EventSummary>of());
    }
  }
}
