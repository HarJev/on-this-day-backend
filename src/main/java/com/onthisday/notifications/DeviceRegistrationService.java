package com.onthisday.notifications;

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
    if (token.length() > DeviceRegistration.MAX_TOKEN_LENGTH) {
      throw new InvalidDeviceRegistrationException("Token is too long.");
    }
    repository.deleteByToken(token);
  }

  private void validateTimezone(String timezone) {
    if (!IanaTimezones.isValid(timezone)) {
      throw new InvalidDeviceRegistrationException("Invalid timezone.");
    }
  }
}
