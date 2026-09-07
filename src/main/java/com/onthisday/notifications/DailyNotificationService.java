package com.onthisday.notifications;

import com.onthisday.content.TodayContentService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DailyNotificationService {

  private static final Logger LOG = LoggerFactory.getLogger(DailyNotificationService.class);

  private final DeviceRegistrationRepository deviceRegistrationRepository;
  private final TodayContentService todayContentService;

  public DailyNotificationService(
      DeviceRegistrationRepository deviceRegistrationRepository,
      TodayContentService todayContentService) {
    this.deviceRegistrationRepository =
        Objects.requireNonNull(
            deviceRegistrationRepository, "deviceRegistrationRepository must not be null");
    this.todayContentService =
        Objects.requireNonNull(todayContentService, "todayContentService must not be null");
  }

  public NotificationPlan createPlan() {
    var registrations = deviceRegistrationRepository.findEligibleForNotifications();
    var registrationsByTimezone = new LinkedHashMap<String, List<DeviceRegistration>>();
    for (var registration : registrations) {
      registrationsByTimezone
          .computeIfAbsent(registration.timezone(), ignored -> new ArrayList<>())
          .add(registration);
    }

    var batches = new ArrayList<NotificationDeliveryBatch>();
    for (var entry : registrationsByTimezone.entrySet()) {
      var content = todayContentService.getTodayContent(entry.getKey());
      var featured = content.featuredEvent();
      var message =
          new NotificationMessage(
              featured.notificationTitle(), featured.notificationBody(), featured.id());
      var tokens = entry.getValue().stream().map(DeviceRegistration::token).toList();
      batches.add(new NotificationDeliveryBatch(message, tokens));
    }

    var plan = new NotificationPlan(batches);
    LOG.info(
        "daily_notification_plan_created recipientCount={} batchCount={}",
        plan.recipientCount(),
        plan.batches().size());
    return plan;
  }

  public NotificationSendSummary send(NotificationPlan plan, NotificationSender sender) {
    Objects.requireNonNull(plan, "plan must not be null");
    Objects.requireNonNull(sender, "sender must not be null");

    var counts = new LinkedHashMap<NotificationDeliveryStatus, Integer>();
    for (var status : NotificationDeliveryStatus.values()) {
      counts.put(status, 0);
    }

    for (var batch : plan.batches()) {
      for (var token : batch.recipientTokens()) {
        var result = sendSafely(sender, token, batch.message());
        counts.compute(result.status(), (ignored, count) -> count + 1);
        if (result.status() == NotificationDeliveryStatus.PERMANENT_TOKEN_FAILURE) {
          deviceRegistrationRepository.deleteByToken(token);
        }
      }
    }

    var summary =
        new NotificationSendSummary(
            counts.get(NotificationDeliveryStatus.SUCCESS),
            counts.get(NotificationDeliveryStatus.PERMANENT_TOKEN_FAILURE),
            counts.get(NotificationDeliveryStatus.TRANSIENT_FAILURE),
            counts.get(NotificationDeliveryStatus.CONFIGURATION_FAILURE));
    LOG.info(
        "daily_notification_send_complete attemptedCount={} successes={} permanentTokenFailures={} transientFailures={} configurationFailures={}",
        summary.attemptedCount(),
        summary.successes(),
        summary.permanentTokenFailures(),
        summary.transientFailures(),
        summary.configurationFailures());
    return summary;
  }

  private static NotificationDeliveryResult sendSafely(
      NotificationSender sender, String token, NotificationMessage message) {
    try {
      return Objects.requireNonNull(
          sender.send(token, message), "notification sender result must not be null");
    } catch (RuntimeException exception) {
      LOG.warn("daily_notification_send_failed_unexpected", exception);
      return new NotificationDeliveryResult(
          NotificationDeliveryStatus.TRANSIENT_FAILURE, "unexpected_sender_failure");
    }
  }
}
