package com.onthisday.notifications;

import java.util.List;
import java.util.Objects;

public record NotificationDeliveryBatch(NotificationMessage message, List<String> recipientTokens) {

  public NotificationDeliveryBatch {
    message = Objects.requireNonNull(message, "message must not be null");
    recipientTokens = List.copyOf(recipientTokens);
  }
}
