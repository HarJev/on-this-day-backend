package com.onthisday.notifications;

/**
 * Counts for one scheduled run. Every run produces one, including runs with nobody due, so a
 * successful empty run can be told apart from a run that never happened.
 */
public record ScheduledNotificationRunSummary(
    boolean dryRun,
    int eligibleDevices,
    int invalidTimezones,
    int notDue,
    int due,
    int noContent,
    int alreadyHandled,
    int wouldSend,
    int sent,
    int permanentTokenFailures,
    int retryableFailures,
    int configurationFailures,
    int deferred,
    int pruned) {

  public boolean hasConfigurationFailure() {
    return configurationFailures > 0;
  }
}
