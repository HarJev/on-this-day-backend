package com.onthisday.notifications;

import java.time.DateTimeException;
import java.time.ZoneId;

public final class DeviceRegistrationService {

  private final DeviceRegistrationRepository repository;

  public DeviceRegistrationService(DeviceRegistrationRepository repository) {
    this.repository = repository;
  }

  public void register(DeviceRegistration registration) {
    validateTimezone(registration.timezone());
    repository.upsert(registration);
  }

  public void delete(String token) {
    if (token == null || token.isBlank()) {
      throw new InvalidDeviceRegistrationException("Token is required.");
    }
    repository.deleteByToken(token);
  }

  private void validateTimezone(String timezone) {
    try {
      ZoneId.of(timezone);
    } catch (DateTimeException exception) {
      throw new InvalidDeviceRegistrationException("Invalid timezone.", exception);
    }
  }
}
