package com.onthisday.platform.notifications.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onthisday.content.JdbcTodayContentRepository;
import com.onthisday.content.TodayContentService;
import com.onthisday.notifications.DailyNotificationService;
import com.onthisday.notifications.JdbcDeviceRegistrationRepository;
import com.onthisday.platform.notifications.fcm.FcmAccessTokenProvider;
import com.onthisday.platform.notifications.fcm.FcmMessageWriter;
import com.onthisday.platform.notifications.fcm.FcmNotificationSender;
import com.onthisday.platform.notifications.fcm.GoogleCredentialsAccessTokenProvider;
import com.onthisday.platform.runtime.ConfigurationException;
import com.onthisday.platform.runtime.DatabaseConfig;
import com.onthisday.platform.runtime.PostgresDataSourceFactory;
import java.io.IOException;
import java.time.Clock;
import java.time.ZoneOffset;

public final class ManualNotificationSenderCommand {

  private ManualNotificationSenderCommand() {}

  public static void main(String[] args) {
    var options = ManualNotificationSenderOptions.parse(args, System.getenv());
    var dataSource =
        new PostgresDataSourceFactory().create(DatabaseConfig.fromEnvironment());
    var deviceRepository = new JdbcDeviceRegistrationRepository(dataSource);
    var service =
        new DailyNotificationService(
            deviceRepository,
            new TodayContentService(
                new JdbcTodayContentRepository(dataSource), clockFor(options)));
    var plan = service.createPlan();
    var objectMapper = new ObjectMapper();
    var messageWriter = new FcmMessageWriter(objectMapper);

    System.out.printf(
        "mode=%s recipientCount=%d batchCount=%d%n",
        options.send() ? "send" : "dry-run", plan.recipientCount(), plan.batches().size());
    for (var batch : plan.batches()) {
      System.out.printf(
          "recipients=%d payload=%s%n",
          batch.recipientTokens().size(), messageWriter.writeRedacted(batch.message()));
    }

    if (!options.send()) {
      System.out.println("dry-run complete; Firebase was not contacted");
      return;
    }

    var sender =
        new FcmNotificationSender(
            options.projectId(), accessTokenProvider(options), objectMapper);
    var summary = service.send(plan, sender);
    System.out.printf(
        "send complete attempted=%d successes=%d permanentTokenFailures=%d transientFailures=%d configurationFailures=%d%n",
        summary.attemptedCount(),
        summary.successes(),
        summary.permanentTokenFailures(),
        summary.transientFailures(),
        summary.configurationFailures());
  }

  private static FcmAccessTokenProvider accessTokenProvider(
      ManualNotificationSenderOptions options) {
    try {
      if (options.credentialFile() != null) {
        return GoogleCredentialsAccessTokenProvider.fromFile(options.credentialFile());
      }
      return GoogleCredentialsAccessTokenProvider.applicationDefault();
    } catch (IOException exception) {
      throw new ConfigurationException(
          "Could not load Firebase credentials for --send. Configure Application Default Credentials or pass --credentials <path>.",
          exception);
    }
  }

  private static Clock clockFor(ManualNotificationSenderOptions options) {
    return options.clockInstant() == null
        ? Clock.systemUTC()
        : Clock.fixed(options.clockInstant(), ZoneOffset.UTC);
  }
}
