package com.onthisday.platform.notifications;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onthisday.notifications.DeviceRegistration;
import com.onthisday.notifications.DeviceRegistrationRepository;
import com.onthisday.notifications.DeviceRegistrationService;
import com.onthisday.platform.http.HttpMethod;
import com.onthisday.platform.http.HttpRequest;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DeleteDeviceHandlerTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Test
  void returnsDeletedTrueForValidToken() {
    var repository = new RecordingDeviceRegistrationRepository();

    var response = handler(repository).handle(request("fcm-token"));

    assertEquals(200, response.statusCode());
    assertEquals("application/json", response.headers().get("content-type"));
    assertEquals("{\"deleted\":true}", response.body());
    assertEquals("fcm-token", repository.deletedToken);
  }

  @Test
  void decodesUrlEncodedToken() {
    var repository = new RecordingDeviceRegistrationRepository();

    var response = handler(repository).handle(request("abc%2F123%3Aios%2Btoken"));

    assertEquals(200, response.statusCode());
    assertEquals("abc/123:ios+token", repository.deletedToken);
  }

  @Test
  void preservesLiteralPlusInTokenPath() {
    var repository = new RecordingDeviceRegistrationRepository();

    var response = handler(repository).handle(request("abc+123"));

    assertEquals(200, response.statusCode());
    assertEquals("abc+123", repository.deletedToken);
  }

  @Test
  void mapsBlankTokenToInvalidDeviceRegistration() {
    var response = handler(new RecordingDeviceRegistrationRepository()).handle(request(" "));

    assertEquals(400, response.statusCode());
    assertEquals(
        "{\"code\":\"invalid_device_registration\",\"message\":\"Invalid device registration.\"}",
        response.body());
  }

  @Test
  void deletingUnknownTokenIsStillSuccessful() {
    var response = handler(new RecordingDeviceRegistrationRepository()).handle(request("missing-token"));

    assertEquals(200, response.statusCode());
    assertEquals("{\"deleted\":true}", response.body());
  }

  private static DeleteDeviceHandler handler(DeviceRegistrationRepository repository) {
    return new DeleteDeviceHandler(new DeviceRegistrationService(repository), OBJECT_MAPPER);
  }

  private static HttpRequest request(String token) {
    return new HttpRequest(
        HttpMethod.DELETE, "/v1/devices/" + token, Map.of(), Map.of("token", token), Map.of(), "");
  }

  private static final class RecordingDeviceRegistrationRepository implements DeviceRegistrationRepository {

    private String deletedToken;

    @Override
    public void upsert(DeviceRegistration registration) {}

    @Override
    public void deleteByToken(String token) {
      deletedToken = token;
    }

    @Override
    public java.util.List<DeviceRegistration> findEligibleForNotifications() {
      return java.util.List.of();
    }
  }
}
