package com.onthisday.platform.notifications.fcm;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.onthisday.notifications.NotificationDeliveryStatus;
import org.junit.jupiter.api.Test;

class FcmErrorClassifierTest {

  @Test
  void separatesPermanentTokenFailuresFromTransientFailures() {
    assertEquals(
        NotificationDeliveryStatus.PERMANENT_TOKEN_FAILURE,
        FcmErrorClassifier.classify(404, "UNREGISTERED"));
    assertEquals(
        NotificationDeliveryStatus.PERMANENT_TOKEN_FAILURE,
        FcmErrorClassifier.classify(403, "SENDER_ID_MISMATCH"));
    assertEquals(
        NotificationDeliveryStatus.TRANSIENT_FAILURE,
        FcmErrorClassifier.classify(503, "UNAVAILABLE"));
    assertEquals(
        NotificationDeliveryStatus.TRANSIENT_FAILURE,
        FcmErrorClassifier.classify(429, "RESOURCE_EXHAUSTED"));
    assertEquals(
        NotificationDeliveryStatus.CONFIGURATION_FAILURE,
        FcmErrorClassifier.classify(403, "PERMISSION_DENIED"));
  }
}
