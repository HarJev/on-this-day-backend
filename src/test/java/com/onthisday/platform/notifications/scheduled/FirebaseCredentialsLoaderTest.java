package com.onthisday.platform.notifications.scheduled;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.onthisday.platform.runtime.ConfigurationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class FirebaseCredentialsLoaderTest {

  private final List<String> requested = new ArrayList<>();

  @Test
  void requiresAParameterOrFile() {
    var loader = new FirebaseCredentialsLoader(name -> fail());

    var exception = assertThrows(ConfigurationException.class, () -> loader.load(Map.of()));

    assertEquals(
        "Set FIREBASE_CREDENTIALS_SSM_PARAMETER or FIREBASE_CREDENTIALS_FILE to send notifications.",
        exception.getMessage());
  }

  @Test
  void readsTheNamedParameterAndNeverEchoesItsValue() {
    var secretLookingValue = "{\"type\":\"service_account\",\"private_key\":\"not-a-real-key\"}";
    var loader =
        new FirebaseCredentialsLoader(
            name -> {
              requested.add(name);
              return secretLookingValue;
            });

    var exception =
        assertThrows(
            ConfigurationException.class,
            () ->
                loader.load(
                    Map.of(
                        FirebaseCredentialsLoader.SSM_PARAMETER,
                        "/on-this-day/prod/firebase-service-account")));

    assertEquals(List.of("/on-this-day/prod/firebase-service-account"), requested);
    assertEquals(
        "Could not load Firebase credentials from FIREBASE_CREDENTIALS_SSM_PARAMETER.",
        exception.getMessage());
    assertFalse(exception.getMessage().contains("not-a-real-key"));
  }

  @Test
  void reportsAMissingLocalFile() {
    var loader = new FirebaseCredentialsLoader(name -> fail());

    var exception =
        assertThrows(
            ConfigurationException.class,
            () -> loader.load(Map.of(FirebaseCredentialsLoader.FILE, "/nonexistent/key.json")));

    assertEquals(
        "Could not load Firebase credentials from FIREBASE_CREDENTIALS_FILE.",
        exception.getMessage());
  }

  private static String fail() {
    throw new AssertionError("SSM must not be read");
  }
}
