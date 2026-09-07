package com.onthisday.notifications;

public record NotificationSendSummary(
    int successes,
    int permanentTokenFailures,
    int transientFailures,
    int configurationFailures) {

  public int attemptedCount() {
    return successes + permanentTokenFailures + transientFailures + configurationFailures;
  }
}
