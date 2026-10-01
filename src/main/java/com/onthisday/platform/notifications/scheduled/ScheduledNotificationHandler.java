package com.onthisday.platform.notifications.scheduled;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onthisday.content.JdbcTodayContentRepository;
import com.onthisday.notifications.JdbcDeviceRegistrationRepository;
import com.onthisday.notifications.JdbcNotificationDeliveryRepository;
import com.onthisday.notifications.NotificationSchedule;
import com.onthisday.notifications.NotificationSender;
import com.onthisday.notifications.ScheduledNotificationRunSummary;
import com.onthisday.notifications.ScheduledNotificationService;
import com.onthisday.platform.notifications.fcm.FcmNotificationSender;
import com.onthisday.platform.runtime.ConfigurationException;
import com.onthisday.platform.runtime.DatabaseConfig;
import com.onthisday.platform.runtime.PostgresDataSourceFactory;
import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Entry point for the scheduled daily notification function, invoked every 15 minutes by
 * EventBridge Scheduler. Separate from the HTTP API function so it has its own role, timeout and
 * concurrency.
 *
 * <p>The input event is ignored except for two optional keys used in manual runs: {@code dryRun}
 * (true lists who is due without claiming or contacting Firebase) and {@code instant} (an
 * ISO-8601 time to run as, for local testing). The {@code NOTIFICATIONS_DRY_RUN} environment
 * variable set to {@code true} forces every run to be a dry run.
 */
public final class ScheduledNotificationHandler
    implements RequestHandler<Map<String, Object>, Map<String, Object>> {

  static final String DRY_RUN_ENV = "NOTIFICATIONS_DRY_RUN";
  static final String FIREBASE_PROJECT_ID = "FIREBASE_PROJECT_ID";
  static final String DB_PASSWORD_SSM_PARAMETER = "DB_PASSWORD_SSM_PARAMETER";
  // One FCM call has a 20-second request timeout, so stop claiming well before that.
  static final int STOP_CLAIMING_MARGIN_MILLIS = 45_000;

  private final ScheduledNotificationService service;
  private final Supplier<NotificationSender> senderFactory;
  private final Clock clock;
  private final boolean forceDryRun;
  private NotificationSender sender;

  public ScheduledNotificationHandler() {
    this(System.getenv());
  }

  ScheduledNotificationHandler(Map<String, String> environment) {
    this(
        createService(environment),
        () -> createSender(environment),
        Clock.systemUTC(),
        "true".equalsIgnoreCase(environment.get(DRY_RUN_ENV)));
  }

  ScheduledNotificationHandler(
      ScheduledNotificationService service,
      Supplier<NotificationSender> senderFactory,
      Clock clock,
      boolean forceDryRun) {
    this.service = Objects.requireNonNull(service, "service must not be null");
    this.senderFactory = Objects.requireNonNull(senderFactory, "senderFactory must not be null");
    this.clock = Objects.requireNonNull(clock, "clock must not be null");
    this.forceDryRun = forceDryRun;
  }

  @Override
  public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
    var event = input == null ? Map.<String, Object>of() : input;
    var now = instantFrom(event.get("instant"));
    var dryRun = forceDryRun || Boolean.TRUE.equals(event.get("dryRun"));

    var summary =
        dryRun ? service.dryRun(now) : service.run(now, sender(), outOfTime(context));
    if (summary.hasConfigurationFailure()) {
      // Failing the invocation raises the function's error alarm.
      throw new IllegalStateException(
          "Firebase rejected the send configuration; remaining devices were deferred.");
    }
    return toResponse(summary);
  }

  /** Stops claiming new devices with a safety margin before the function's timeout. */
  private static BooleanSupplier outOfTime(Context context) {
    if (context == null) {
      return () -> false;
    }
    return () -> context.getRemainingTimeInMillis() < STOP_CLAIMING_MARGIN_MILLIS;
  }

  private synchronized NotificationSender sender() {
    // Loaded on the first real send and reused while the function stays warm.
    if (sender == null) {
      sender = senderFactory.get();
    }
    return sender;
  }

  private Instant instantFrom(Object value) {
    if (value == null) {
      return clock.instant();
    }
    try {
      return Instant.parse(value.toString());
    } catch (DateTimeParseException exception) {
      throw new IllegalArgumentException("instant must be an ISO-8601 instant.", exception);
    }
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> toResponse(ScheduledNotificationRunSummary summary) {
    return new ObjectMapper().convertValue(summary, Map.class);
  }

  private static ScheduledNotificationService createService(Map<String, String> environment) {
    var dataSource =
        new PostgresDataSourceFactory()
            .create(databaseConfig(environment, new SsmParameterReader()));
    return new ScheduledNotificationService(
        new JdbcDeviceRegistrationRepository(dataSource),
        new JdbcNotificationDeliveryRepository(dataSource),
        new JdbcTodayContentRepository(dataSource),
        NotificationSchedule.DEFAULT);
  }

  /**
   * Deployed, the database password is an SSM SecureString named by {@code
   * DB_PASSWORD_SSM_PARAMETER}, so it never sits in the function's configuration or Terraform
   * state. Locally, {@code DB_PASSWORD} is used as before.
   */
  static DatabaseConfig databaseConfig(Map<String, String> environment, ParameterReader reader) {
    var parameterName = environment.get(DB_PASSWORD_SSM_PARAMETER);
    if (parameterName == null || parameterName.isBlank()) {
      return DatabaseConfig.from(environment);
    }
    var resolved = new HashMap<>(environment);
    resolved.put(DatabaseConfig.DB_PASSWORD, reader.readDecrypted(parameterName));
    return DatabaseConfig.from(resolved);
  }

  private static NotificationSender createSender(Map<String, String> environment) {
    var projectId = environment.get(FIREBASE_PROJECT_ID);
    if (projectId == null || projectId.isBlank()) {
      throw new ConfigurationException("Missing required environment variable: " + FIREBASE_PROJECT_ID);
    }
    var tokenProvider = new FirebaseCredentialsLoader(new SsmParameterReader()).load(environment);
    return new FcmNotificationSender(projectId, tokenProvider, new ObjectMapper());
  }
}
