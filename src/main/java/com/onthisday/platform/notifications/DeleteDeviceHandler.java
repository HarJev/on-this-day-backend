package com.onthisday.platform.notifications;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onthisday.notifications.DeviceRegistrationService;
import com.onthisday.notifications.InvalidDeviceRegistrationException;
import com.onthisday.platform.http.ErrorResponseWriter;
import com.onthisday.platform.http.HttpRequest;
import com.onthisday.platform.http.HttpResponse;
import com.onthisday.platform.http.HttpRoute;
import java.net.URI;

public final class DeleteDeviceHandler implements HttpRoute {

  private final DeviceRegistrationService service;
  private final ObjectMapper objectMapper;
  private final ErrorResponseWriter errorResponseWriter;

  public DeleteDeviceHandler(DeviceRegistrationService service, ObjectMapper objectMapper) {
    this.service = service;
    this.objectMapper = objectMapper;
    this.errorResponseWriter = new ErrorResponseWriter(objectMapper);
  }

  @Override
  public HttpResponse handle(HttpRequest request) {
    try {
      var token = request.pathParameter("token").map(DeleteDeviceHandler::decode).orElse("");
      service.delete(token);
      return HttpResponse.json(200, objectMapper.writeValueAsString(new DeleteDeviceResponse(true)));
    } catch (InvalidDeviceRegistrationException exception) {
      return errorResponseWriter.json(400, "invalid_device_registration", "Invalid device registration.");
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Failed to serialize device deletion response.", exception);
    }
  }

  private static String decode(String value) {
    try {
      var path = URI.create("http://localhost/" + value).getPath();
      return path.substring(1);
    } catch (IllegalArgumentException exception) {
      throw new InvalidDeviceRegistrationException("Invalid token.", exception);
    }
  }
}
