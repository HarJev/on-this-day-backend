package com.onthisday.platform.notifications.fcm;

import com.onthisday.notifications.NotificationDeliveryStatus;
import java.util.Set;

public final class FcmErrorClassifier {

  private static final Set<String> PERMANENT_TOKEN_ERRORS =
      Set.of("UNREGISTERED", "SENDER_ID_MISMATCH");
  private static final Set<String> TRANSIENT_ERRORS =
      Set.of("QUOTA_EXCEEDED", "UNAVAILABLE", "INTERNAL", "INTERNAL_SERVER_ERROR");

  private FcmErrorClassifier() {}

  public static NotificationDeliveryStatus classify(int statusCode, String errorCode) {
    if (PERMANENT_TOKEN_ERRORS.contains(errorCode)) {
      return NotificationDeliveryStatus.PERMANENT_TOKEN_FAILURE;
    }
    if (statusCode == 429 || statusCode >= 500 || TRANSIENT_ERRORS.contains(errorCode)) {
      return NotificationDeliveryStatus.TRANSIENT_FAILURE;
    }
    return NotificationDeliveryStatus.CONFIGURATION_FAILURE;
  }
}
