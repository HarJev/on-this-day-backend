package com.onthisday.notifications;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DeviceRegistrationServiceTest {

  private final List<DeviceRegistration> stored = new ArrayList<>();
  private final DeviceRegistrationService service =
      new DeviceRegistrationService(
          new DeviceRegistrationRepository() {
            @Override
            public void upsert(DeviceRegistration registration) {
              stored.add(registration);
            }

            @Override
            public void deleteByToken(String token) {}

            @Override
            public List<DeviceRegistration> findEligibleForNotifications() {
              return List.of();
            }
          });

  @ParameterizedTest
  @ValueSource(strings = {"America/Jamaica", "Asia/Kolkata", "Asia/Kathmandu", "UTC", "Etc/UTC"})
  void acceptsIanaTimezoneNames(String timezone) {
    service.register(registration(timezone));

    assertEquals(timezone, stored.get(0).timezone());
  }

  @ParameterizedTest
  @ValueSource(strings = {"+05:00", "Z", "GMT+5", "UTC+01:00", "EST5EDT ", "Mars/Olympus_Mons", "america/jamaica"})
  void rejectsOffsetsAndUnknownNames(String timezone) {
    assertThrows(
        InvalidDeviceRegistrationException.class, () -> service.register(registration(timezone)));
    assertEquals(List.of(), stored);
  }

  @Test
  void rejectsOversizedTokensOnRegisterAndDelete() {
    var tooLong = "t".repeat(DeviceRegistration.MAX_TOKEN_LENGTH + 1);

    assertThrows(
        InvalidDeviceRegistrationException.class,
        () ->
            service.register(
                new DeviceRegistration(
                    tooLong, DevicePlatform.IOS, "UTC", NotificationPermissionStatus.AUTHORIZED)));
    assertThrows(InvalidDeviceRegistrationException.class, () -> service.delete(tooLong));
    assertEquals(List.of(), stored);

    service.register(
        new DeviceRegistration(
            "t".repeat(DeviceRegistration.MAX_TOKEN_LENGTH),
            DevicePlatform.IOS,
            "UTC",
            NotificationPermissionStatus.AUTHORIZED));
    assertEquals(1, stored.size());
  }

  private static DeviceRegistration registration(String timezone) {
    return new DeviceRegistration(
        "token", DevicePlatform.IOS, timezone, NotificationPermissionStatus.AUTHORIZED);
  }
}
