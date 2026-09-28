package com.onthisday.platform.content;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onthisday.content.ContentUnavailableException;
import com.onthisday.content.InvalidRecentDaysRequestException;
import com.onthisday.content.InvalidTimezoneException;
import com.onthisday.content.RecentDay;
import com.onthisday.content.RecentDaysService;
import com.onthisday.platform.http.ErrorResponseWriter;
import com.onthisday.platform.http.HttpRequest;
import com.onthisday.platform.http.HttpResponse;
import com.onthisday.platform.http.HttpRoute;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RecentDaysHandler implements HttpRoute {

  private static final Logger LOG = LoggerFactory.getLogger(RecentDaysHandler.class);

  private final RecentDaysService service;
  private final ObjectMapper objectMapper;
  private final ErrorResponseWriter errorResponseWriter;

  public RecentDaysHandler(RecentDaysService service, ObjectMapper objectMapper) {
    this.service = service;
    this.objectMapper = objectMapper;
    this.errorResponseWriter = new ErrorResponseWriter(objectMapper);
  }

  @Override
  public HttpResponse handle(HttpRequest request) {
    try {
      var timezone = request.queryParameter("timezone").orElse(null);
      var days = parseDays(request.queryParameter("days").orElse(null));
      var recentDays = service.getRecentDays(timezone, days);
      return HttpResponse.json(200, objectMapper.writeValueAsString(toResponse(recentDays)));
    } catch (InvalidTimezoneException exception) {
      return errorResponseWriter.json(400, "invalid_timezone", "Invalid timezone.");
    } catch (InvalidRecentDaysRequestException exception) {
      return errorResponseWriter.json(400, "invalid_days", "Invalid number of days.");
    } catch (ContentUnavailableException exception) {
      LOG.warn("recent_days_unavailable reason={}", exception.getMessage());
      return errorResponseWriter.json(503, "content_unavailable", "Content temporarily unavailable.");
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Failed to serialize recent days response.", exception);
    }
  }

  private static int parseDays(String value) {
    if (value == null) {
      return RecentDaysService.DEFAULT_DAYS;
    }
    try {
      return Integer.parseInt(value.strip());
    } catch (NumberFormatException exception) {
      throw new InvalidRecentDaysRequestException("days must be a whole number");
    }
  }

  private static RecentDaysResponse toResponse(List<RecentDay> recentDays) {
    return new RecentDaysResponse(
        recentDays.stream()
            .map(
                day -> {
                  var date = day.date();
                  var event = day.featuredEvent();
                  return new RecentDaysResponse.RecentDayResponse(
                      day.daysAgo(),
                      new ApiContentDateResponse(date.month(), date.day(), date.displayDate()),
                      new ApiEventSummaryResponse(
                          event.id(), event.title(), event.year(), event.historicalDate(), event.dateNote()));
                })
            .toList());
  }
}
