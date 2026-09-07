package com.onthisday.platform.notifications.fcm;

import com.google.auth.oauth2.GoogleCredentials;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class GoogleCredentialsAccessTokenProvider implements FcmAccessTokenProvider {

  private static final String FIREBASE_MESSAGING_SCOPE =
      "https://www.googleapis.com/auth/firebase.messaging";

  private final GoogleCredentials credentials;

  private GoogleCredentialsAccessTokenProvider(GoogleCredentials credentials) {
    this.credentials = credentials.createScoped(List.of(FIREBASE_MESSAGING_SCOPE));
  }

  public static GoogleCredentialsAccessTokenProvider applicationDefault() throws IOException {
    return new GoogleCredentialsAccessTokenProvider(GoogleCredentials.getApplicationDefault());
  }

  public static GoogleCredentialsAccessTokenProvider fromFile(Path credentialFile)
      throws IOException {
    try (var input = Files.newInputStream(credentialFile)) {
      return new GoogleCredentialsAccessTokenProvider(GoogleCredentials.fromStream(input));
    }
  }

  @Override
  public synchronized String getAccessToken() throws IOException {
    credentials.refreshIfExpired();
    var accessToken = credentials.getAccessToken();
    if (accessToken == null || accessToken.getTokenValue() == null) {
      throw new IOException("Google credentials did not provide an access token.");
    }
    return accessToken.getTokenValue();
  }
}
