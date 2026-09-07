package com.onthisday.platform.notifications;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onthisday.notifications.DeviceRegistrationService;
import com.onthisday.notifications.InvalidDeviceRegistrationException;
import com.onthisday.platform.http.ErrorResponseWriter;
import com.onthisday.platform.http.HttpRequest;
import com.onthisday.platform.http.HttpResponse;
import com.onthisday.platform.http.HttpRoute;

public final class RegisterDeviceHandler implements HttpRoute {

  private final DeviceRegistrationService service;
  private final ObjectMapper objectMapper;
  private final ErrorResponseWriter errorResponseWriter;

  public RegisterDeviceHandler(DeviceRegistrationService service, ObjectMapper objectMapper) {
    this.service = service;
    this.objectMapper = objectMapper;
    this.errorResponseWriter = new ErrorResponseWriter(objectMapper);
  }

  @Override
  public HttpResponse handle(HttpRequest request) {
    final DeviceRegistrationRequest registrationRequest;
    try {
      registrationRequest = objectMapper.readValue(request.body(), DeviceRegistrationRequest.class);
    } catch (JsonProcessingException exception) {
      return errorResponseWriter.json(400, "invalid_request", "Invalid request.");
    }
    if (registrationRequest == null) {
      return errorResponseWriter.json(400, "invalid_device_registration", "Invalid device registration.");
    }

    try {
      service.register(registrationRequest.toRegistration());
      return HttpResponse.json(200, objectMapper.writeValueAsString(new RegisterDeviceResponse(true)));
    } catch (InvalidDeviceRegistrationException exception) {
      return errorResponseWriter.json(400, "invalid_device_registration", "Invalid device registration.");
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Failed to serialize device registration response.", exception);
    }
  }
}
