package com.onthisday.platform.notifications.scheduled;

import com.onthisday.platform.notifications.fcm.FcmAccessTokenProvider;
import com.onthisday.platform.notifications.fcm.GoogleCredentialsAccessTokenProvider;
import com.onthisday.platform.runtime.ConfigurationException;
import com.onthisday.platform.runtime.ParameterReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

/**
 * Loads the Firebase service-account key at runtime. Deployed, it comes from an SSM SecureString
 * named by {@code FIREBASE_CREDENTIALS_SSM_PARAMETER}; locally, from a gitignored file named by
 * {@code FIREBASE_CREDENTIALS_FILE}. The key is never read from an environment variable value.
 */
final class FirebaseCredentialsLoader {

  static final String SSM_PARAMETER = "FIREBASE_CREDENTIALS_SSM_PARAMETER";
  static final String FILE = "FIREBASE_CREDENTIALS_FILE";

  private final ParameterReader parameterReader;

  FirebaseCredentialsLoader(ParameterReader parameterReader) {
    this.parameterReader = Objects.requireNonNull(parameterReader, "parameterReader must not be null");
  }

  FcmAccessTokenProvider load(Map<String, String> environment) {
    var parameterName = environment.get(SSM_PARAMETER);
    var file = environment.get(FILE);
    try {
      if (parameterName != null && !parameterName.isBlank()) {
        var json = parameterReader.readDecrypted(parameterName);
        return GoogleCredentialsAccessTokenProvider.fromJson(json.getBytes(StandardCharsets.UTF_8));
      }
      if (file != null && !file.isBlank()) {
        return GoogleCredentialsAccessTokenProvider.fromFile(Path.of(file));
      }
    } catch (IOException | RuntimeException exception) {
      // The message names only where the key was expected, never its contents.
      throw new ConfigurationException(
          "Could not load Firebase credentials from "
              + (parameterName != null && !parameterName.isBlank() ? SSM_PARAMETER : FILE)
              + ".",
          exception);
    }
    throw new ConfigurationException(
        "Set " + SSM_PARAMETER + " or " + FILE + " to send notifications.");
  }
}
