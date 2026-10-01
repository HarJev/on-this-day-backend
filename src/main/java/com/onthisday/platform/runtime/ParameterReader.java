package com.onthisday.platform.runtime;

/** Reads one decrypted configuration value by name. */
@FunctionalInterface
public interface ParameterReader {

  String readDecrypted(String name);
}
