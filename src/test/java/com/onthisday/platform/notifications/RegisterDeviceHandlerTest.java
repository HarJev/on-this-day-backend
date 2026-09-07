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

class RegisterDeviceHandlerTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Test
  void returnsRegisteredTrueForValidRequest() {
    var repository = new RecordingDeviceRegistrationRepository();
    var response = handler(repository).handle(request(validBody()));

    assertEquals(200, response.statusCode());
    assertEquals("application/json", response.headers().get("content-type"));
    assertEquals("{\"registered\":true}", response.body());
    assertEquals("fcm-token", repository.registration.token());
    assertEquals("ios", repository.registration.platform().value());
    assertEquals("America/Jamaica", repository.registration.timezone());
    assertEquals("authorized", repository.registration.notificationPermissionStatus().value());
  }

  @Test
  void mapsMalformedJsonToInvalidRequest() {
    var response = handler(new RecordingDeviceRegistrationRepository()).handle(request("{"));

    assertEquals(400, response.statusCode());
    assertEquals("{\"code\":\"invalid_request\",\"message\":\"Invalid request.\"}", response.body());
  }

  @Test
  void mapsJsonNullToInvalidDeviceRegistration() {
    var response = handler(new RecordingDeviceRegistrationRepository()).handle(request("null"));

    assertEquals(400, response.statusCode());
    assertEquals(
        "{\"code\":\"invalid_device_registration\",\"message\":\"Invalid device registration.\"}",
        response.body());
  }

  @Test
  void mapsBlankTokenToInvalidDeviceRegistration() {
    var response =
        handler(new RecordingDeviceRegistrationRepository())
            .handle(request(validBody().replace("\"fcm-token\"", "\" \"")));

    assertEquals(400, response.statusCode());
    assertEquals(
        "{\"code\":\"invalid_device_registration\",\"message\":\"Invalid device registration.\"}",
        response.body());
  }

  @Test
  void mapsInvalidPlatformToInvalidDeviceRegistration() {
    var response =
        handler(new RecordingDeviceRegistrationRepository())
            .handle(request(validBody().replace("\"ios\"", "\"web\"")));

    assertEquals(400, response.statusCode());
  }

  @Test
  void mapsInvalidTimezoneToInvalidDeviceRegistration() {
    var response =
        handler(new RecordingDeviceRegistrationRepository())
            .handle(request(validBody().replace("\"America/Jamaica\"", "\"Not/AZone\"")));

    assertEquals(400, response.statusCode());
  }

  @Test
  void mapsInvalidPermissionStatusToInvalidDeviceRegistration() {
    var response =
        handler(new RecordingDeviceRegistrationRepository())
            .handle(request(validBody().replace("\"authorized\"", "\"unknown\"")));

    assertEquals(400, response.statusCode());
  }

  private static RegisterDeviceHandler handler(DeviceRegistrationRepository repository) {
    return new RegisterDeviceHandler(new DeviceRegistrationService(repository), OBJECT_MAPPER);
  }

  private static HttpRequest request(String body) {
    return new HttpRequest(HttpMethod.POST, "/v1/devices", Map.of(), Map.of(), Map.of(), body);
  }

  private static String validBody() {
    return """
        {
          "token": "fcm-token",
          "platform": "ios",
          "timezone": "America/Jamaica",
          "notificationPermissionStatus": "authorized"
        }
        """;
  }

  private static final class RecordingDeviceRegistrationRepository implements DeviceRegistrationRepository {

    private DeviceRegistration registration;

    @Override
    public void upsert(DeviceRegistration registration) {
      this.registration = registration;
    }

    @Override
    public void deleteByToken(String token) {}

    @Override
    public java.util.List<DeviceRegistration> findEligibleForNotifications() {
      return java.util.List.of();
    }
  }
}
