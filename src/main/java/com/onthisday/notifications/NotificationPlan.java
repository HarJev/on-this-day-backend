package com.onthisday.notifications;

import java.util.List;

public record NotificationPlan(List<NotificationDeliveryBatch> batches) {

  public NotificationPlan {
    batches = List.copyOf(batches);
  }

  public int recipientCount() {
    return batches.stream().mapToInt(batch -> batch.recipientTokens().size()).sum();
  }
}
