package com.onthisday.platform.notifications.scheduled;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

/**
 * Runs the scheduled notification function once from a terminal, the same way the Lambda runs.
 * Dry run unless {@code --send} is given. Reads the {@code DB_*}, {@code FIREBASE_PROJECT_ID} and
 * {@code FIREBASE_CREDENTIALS_FILE} environment variables.
 *
 * <pre>
 * mvn -q compile exec:java \
 *   -Dexec.mainClass=com.onthisday.platform.notifications.scheduled.ScheduledNotificationRunCommand \
 *   -Dexec.args="--instant 2026-10-01T15:00:00Z"
 * </pre>
 */
public final class ScheduledNotificationRunCommand {

  private ScheduledNotificationRunCommand() {}

  public static void main(String[] args) throws Exception {
    var event = new HashMap<String, Object>();
    var send = false;
    for (var index = 0; index < args.length; index++) {
      switch (args[index]) {
        case "--send" -> send = true;
        case "--instant" -> {
          if (index + 1 >= args.length) {
            throw new IllegalArgumentException("--instant requires a value.");
          }
          event.put("instant", args[++index]);
        }
        default -> throw new IllegalArgumentException("Unknown argument: " + args[index]);
      }
    }
    event.put("dryRun", !send);

    var environment = new HashMap<>(System.getenv());
    if (send) {
      environment.remove(ScheduledNotificationHandler.DRY_RUN_ENV);
    }
    var summary = new ScheduledNotificationHandler(Map.copyOf(environment)).handleRequest(event, null);
    System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(summary));
  }
}
