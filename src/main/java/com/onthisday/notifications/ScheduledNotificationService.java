package com.onthisday.notifications;

import com.onthisday.content.ContentUnavailableException;
import com.onthisday.content.TodayContentRepository;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.BooleanSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sends the daily featured-event notification to each opted-in device once per local date,
 * scheduled for {@link NotificationSchedule#sendAt()} local time. Runs every few minutes; each run
 * sends only to devices whose local time is inside the delivery window and whose local date is
 * not already sent or claimed by another run.
 *
 * <p>Delivery is at least once inside the window: if a run stops after Firebase accepted a
 * message but before it was recorded, the claim's lease expires and a later run before the
 * cutoff sends again. The repeat carries the same collapse key, so the device replaces the first
 * notification rather than showing two.
 */
public final class ScheduledNotificationService {

  private static final Logger LOG = LoggerFactory.getLogger(ScheduledNotificationService.class);

  private final DeviceRegistrationRepository devices;
  private final NotificationDeliveryRepository deliveries;
  private final TodayContentRepository content;
  private final NotificationSchedule schedule;

  public ScheduledNotificationService(
      DeviceRegistrationRepository devices,
      NotificationDeliveryRepository deliveries,
      TodayContentRepository content,
      NotificationSchedule schedule) {
    this.devices = Objects.requireNonNull(devices, "devices must not be null");
    this.deliveries = Objects.requireNonNull(deliveries, "deliveries must not be null");
    this.content = Objects.requireNonNull(content, "content must not be null");
    this.schedule = Objects.requireNonNull(schedule, "schedule must not be null");
  }

  /** Lists who is due and what they would receive, without claiming or contacting Firebase. */
  public ScheduledNotificationRunSummary dryRun(Instant now) {
    return execute(now, null, () -> false);
  }

  public ScheduledNotificationRunSummary run(Instant now, NotificationSender sender) {
    return run(now, sender, () -> false);
  }

  /**
   * Sends to due devices until {@code outOfTime} reports true. Devices not yet claimed by then
   * are left for the next run, so a large batch finishes over several runs instead of the
   * function timing out mid-send.
   */
  public ScheduledNotificationRunSummary run(
      Instant now, NotificationSender sender, BooleanSupplier outOfTime) {
    return execute(
        now,
        Objects.requireNonNull(sender, "sender must not be null"),
        Objects.requireNonNull(outOfTime, "outOfTime must not be null"));
  }

  private ScheduledNotificationRunSummary execute(
      Instant now, NotificationSender sender, BooleanSupplier outOfTime) {
    Objects.requireNonNull(now, "now must not be null");
    var startedAt = System.nanoTime();
    var dryRun = sender == null;
    var counts = new Counts();

    if (!dryRun) {
      counts.pruned =
          deliveries.deleteBefore(
              LocalDate.ofInstant(now, ZoneOffset.UTC).minusDays(schedule.retentionDays()));
    }

    var registrations = devices.findEligibleForNotifications();
    counts.eligible = registrations.size();
    var dueTokensByDate = new TreeMap<LocalDate, List<String>>();
    for (var registration : registrations) {
      var zone = IanaTimezones.parse(registration.timezone());
      if (zone.isEmpty()) {
        counts.invalidTimezones++;
        continue;
      }
      var local = now.atZone(zone.get());
      if (!schedule.isWithinWindow(local.toLocalTime())) {
        counts.notDue++;
        continue;
      }
      counts.due++;
      dueTokensByDate
          .computeIfAbsent(local.toLocalDate(), ignored -> new ArrayList<>())
          .add(registration.token());
    }

    var stopSending = false;
    for (Map.Entry<LocalDate, List<String>> entry : dueTokensByDate.entrySet()) {
      var localDate = entry.getKey();
      var tokens = entry.getValue();
      var message = messageFor(localDate);
      if (message.isEmpty()) {
        counts.noContent += tokens.size();
        LOG.warn(
            "scheduled_notification_no_content month={} day={} devices={}",
            localDate.getMonthValue(),
            localDate.getDayOfMonth(),
            tokens.size());
        continue;
      }
      for (var token : tokens) {
        if (dryRun) {
          counts.wouldSend++;
          continue;
        }
        if (stopSending || outOfTime.getAsBoolean()) {
          counts.deferred++;
          continue;
        }
        var leaseUntil = now.plus(schedule.claimLease());
        if (!deliveries.claim(token, localDate, message.get().eventId(), now, leaseUntil)) {
          counts.alreadyHandled++;
          continue;
        }
        var result = sendSafely(sender, token, message.get());
        switch (result.status()) {
          case SUCCESS -> {
            deliveries.markSent(token, localDate);
            counts.sent++;
          }
          case PERMANENT_TOKEN_FAILURE -> {
            // Deleting the registration also removes its delivery rows.
            devices.deleteByToken(token);
            counts.permanentTokenFailures++;
          }
          case TRANSIENT_FAILURE -> {
            deliveries.markRetryable(token, localDate, result.errorCode());
            counts.retryableFailures++;
          }
          case CONFIGURATION_FAILURE -> {
            // Credentials or project settings are wrong for every device; stop and let the
            // next run retry everyone once it is fixed.
            deliveries.markRetryable(token, localDate, result.errorCode());
            counts.configurationFailures++;
            stopSending = true;
          }
        }
      }
    }

    var summary = counts.toSummary(dryRun);
    LOG.info(
        "scheduled_notification_run_summary dryRun={} eligible={} invalidTimezones={} notDue={} due={} noContent={} alreadyHandled={} wouldSend={} sent={} permanentTokenFailures={} retryableFailures={} configurationFailures={} deferred={} pruned={} durationMs={}",
        summary.dryRun(),
        summary.eligibleDevices(),
        summary.invalidTimezones(),
        summary.notDue(),
        summary.due(),
        summary.noContent(),
        summary.alreadyHandled(),
        summary.wouldSend(),
        summary.sent(),
        summary.permanentTokenFailures(),
        summary.retryableFailures(),
        summary.configurationFailures(),
        summary.deferred(),
        summary.pruned(),
        (System.nanoTime() - startedAt) / 1_000_000);
    return summary;
  }

  private Optional<NotificationMessage> messageFor(LocalDate localDate) {
    try {
      var featured = content.getTodayContent(MonthDay.from(localDate)).featuredEvent();
      return Optional.of(
          new NotificationMessage(
              featured.notificationTitle(),
              featured.notificationBody(),
              featured.id(),
              "daily-" + localDate));
    } catch (ContentUnavailableException exception) {
      if (exception.getCause() instanceof SQLException) {
        throw exception;
      }
      return Optional.empty();
    }
  }

  private static NotificationDeliveryResult sendSafely(
      NotificationSender sender, String token, NotificationMessage message) {
    try {
      return Objects.requireNonNull(
          sender.send(token, message), "notification sender result must not be null");
    } catch (RuntimeException exception) {
      LOG.warn("scheduled_notification_send_failed_unexpected", exception);
      return new NotificationDeliveryResult(
          NotificationDeliveryStatus.TRANSIENT_FAILURE, "unexpected_sender_failure");
    }
  }

  private static final class Counts {
    int eligible;
    int invalidTimezones;
    int notDue;
    int due;
    int noContent;
    int alreadyHandled;
    int wouldSend;
    int sent;
    int permanentTokenFailures;
    int retryableFailures;
    int configurationFailures;
    int deferred;
    int pruned;

    ScheduledNotificationRunSummary toSummary(boolean dryRun) {
      return new ScheduledNotificationRunSummary(
          dryRun,
          eligible,
          invalidTimezones,
          notDue,
          due,
          noContent,
          alreadyHandled,
          wouldSend,
          sent,
          permanentTokenFailures,
          retryableFailures,
          configurationFailures,
          deferred,
          pruned);
    }
  }
}
