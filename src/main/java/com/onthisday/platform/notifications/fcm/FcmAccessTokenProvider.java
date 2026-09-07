package com.onthisday.platform.notifications.fcm;

import java.io.IOException;

@FunctionalInterface
public interface FcmAccessTokenProvider {

  String getAccessToken() throws IOException;
}
