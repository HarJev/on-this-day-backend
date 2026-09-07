package com.onthisday.platform.notifications.cli;

import com.onthisday.platform.runtime.ConfigurationException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Map;

record ManualNotificationSenderOptions(
    boolean send, String projectId, Path credentialFile, Instant clockInstant) {

  static final String FIREBASE_PROJECT_ID = "FIREBASE_PROJECT_ID";

  static ManualNotificationSenderOptions parse(String[] args, Map<String, String> environment) {
    var send = false;
    String projectId = null;
    Path credentialFile = null;
    Instant clockInstant = null;

    for (var index = 0; index < args.length; index++) {
      switch (args[index]) {
        case "--send" -> send = true;
        case "--project-id" -> projectId = requireValue(args, ++index, "--project-id");
        case "--credentials" ->
            credentialFile = Path.of(requireValue(args, ++index, "--credentials"));
        case "--instant" ->
            clockInstant = parseInstant(requireValue(args, ++index, "--instant"));
        default -> throw new IllegalArgumentException("Unknown argument: " + args[index]);
      }
    }

    if (projectId == null || projectId.isBlank()) {
      projectId = environment.get(FIREBASE_PROJECT_ID);
    }
    if (send && (projectId == null || projectId.isBlank())) {
      throw new ConfigurationException(
          "--send requires --project-id or the FIREBASE_PROJECT_ID environment variable.");
    }

    return new ManualNotificationSenderOptions(send, projectId, credentialFile, clockInstant);
  }

  private static String requireValue(String[] args, int index, String option) {
    if (index >= args.length || args[index].isBlank()) {
      throw new IllegalArgumentException(option + " requires a value.");
    }
    return args[index];
  }

  private static Instant parseInstant(String value) {
    try {
      return Instant.parse(value);
    } catch (DateTimeParseException exception) {
      throw new IllegalArgumentException("--instant must be an ISO-8601 instant.", exception);
    }
  }
}
