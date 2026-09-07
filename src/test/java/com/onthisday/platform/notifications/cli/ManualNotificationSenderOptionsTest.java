package com.onthisday.platform.notifications.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.onthisday.platform.runtime.ConfigurationException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ManualNotificationSenderOptionsTest {

  @Test
  void defaultsToCredentialFreeDryRun() {
    var options = ManualNotificationSenderOptions.parse(new String[0], Map.of());

    assertFalse(options.send());
    assertEquals(null, options.projectId());
    assertEquals(null, options.credentialFile());
    assertEquals(null, options.clockInstant());
  }

  @Test
  void sendRequiresProjectAndAcceptsExplicitCredentialFile() {
    var options =
        ManualNotificationSenderOptions.parse(
            new String[] {
              "--send", "--project-id", "on-this-day-98e6b", "--credentials", "/tmp/key.json"
            },
            Map.of());

    assertTrue(options.send());
    assertEquals("on-this-day-98e6b", options.projectId());
    assertEquals(Path.of("/tmp/key.json"), options.credentialFile());
  }

  @Test
  void sendCanUseProjectFromEnvironmentButNeverDefaultsOne() {
    var options =
        ManualNotificationSenderOptions.parse(
            new String[] {"--send"}, Map.of("FIREBASE_PROJECT_ID", "on-this-day-98e6b"));

    assertEquals("on-this-day-98e6b", options.projectId());
    assertThrows(
        ConfigurationException.class,
        () -> ManualNotificationSenderOptions.parse(new String[] {"--send"}, Map.of()));
  }

  @Test
  void acceptsDeterministicInstantForLocalContent() {
    var options =
        ManualNotificationSenderOptions.parse(
            new String[] {"--instant", "2026-08-24T12:00:00Z"}, Map.of());

    assertEquals(Instant.parse("2026-08-24T12:00:00Z"), options.clockInstant());
    assertThrows(
        IllegalArgumentException.class,
        () ->
            ManualNotificationSenderOptions.parse(
                new String[] {"--instant", "2026-08-24"}, Map.of()));
  }
}
