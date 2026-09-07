package com.onthisday.notifications;

import java.util.List;

public interface DeviceRegistrationRepository {

  void upsert(DeviceRegistration registration);

  void deleteByToken(String token);

  List<DeviceRegistration> findEligibleForNotifications();
}
