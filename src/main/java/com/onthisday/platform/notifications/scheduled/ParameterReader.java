package com.onthisday.platform.notifications.scheduled;

/** Reads one decrypted configuration value by name. */
@FunctionalInterface
interface ParameterReader {

  String readDecrypted(String name);
}
