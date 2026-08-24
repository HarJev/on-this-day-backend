package com.onthisday.ingestion;

import java.net.URI;
import java.time.DateTimeException;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class CuratedContentValidator {

  private static final Pattern EVENT_ID_PATTERN = Pattern.compile("[a-z0-9]+(?:-[a-z0-9]+)*");

  public ContentValidationResult validate(CuratedEventsFile eventsFile, CuratedDailyEventsFile dailyEventsFile) {
    var errors = new ArrayList<ContentValidationError>();
    var warnings = new ArrayList<ContentValidationWarning>();

    var events = eventsFile == null || eventsFile.events() == null ? null : eventsFile.events();
    var days = dailyEventsFile == null || dailyEventsFile.days() == null ? null : dailyEventsFile.days();

    if (events == null) {
      errors.add(error("$.events", "events must be present"));
      events = List.of();
    }
    if (days == null) {
      errors.add(error("$.days", "days must be present"));
      days = List.of();
    }

    var eventsById = validateEvents(events, errors);
    validateDays(days, eventsById, errors, warnings);

    return new ContentValidationResult(errors, warnings);
  }

  private Map<String, CuratedEventJson> validateEvents(
      List<CuratedEventJson> events, List<ContentValidationError> errors) {
    var eventsById = new HashMap<String, CuratedEventJson>();
    var duplicateIds = new HashSet<String>();

    for (int index = 0; index < events.size(); index++) {
      var event = events.get(index);
      var path = "$.events[" + index + "]";

      if (event == null) {
        errors.add(error(path, "event must be present"));
        continue;
      }

      requireNonBlank(event.id(), path + ".id", "id is required", errors);
      if (!blank(event.id()) && !EVENT_ID_PATTERN.matcher(event.id()).matches()) {
        errors.add(error(path + ".id", "id must use lowercase letters, numbers, and hyphens"));
      }
      if (!blank(event.id())) {
        if (eventsById.putIfAbsent(event.id(), event) != null) {
          duplicateIds.add(event.id());
        }
      }

      requireNonBlank(event.title(), path + ".title", "title is required", errors);
      requireNonBlank(event.year(), path + ".year", "year is required", errors);
      requireNonBlank(event.historicalDate(), path + ".historicalDate", "historicalDate is required", errors);
      requireNonBlank(event.summary(), path + ".summary", "summary is required", errors);
      requireNonBlank(event.description(), path + ".description", "description is required", errors);
      if (event.dateNote() != null) {
        requireNonBlank(event.dateNote(), path + ".dateNote", "dateNote must not be blank when present", errors);
      }
      if (event.notificationTitle() != null) {
        requireNonBlank(
            event.notificationTitle(),
            path + ".notificationTitle",
            "notificationTitle must not be blank when present",
            errors);
      }
      if (event.notificationBody() != null) {
        requireNonBlank(
            event.notificationBody(),
            path + ".notificationBody",
            "notificationBody must not be blank when present",
            errors);
      }

      validateSources(event.sources(), path + ".sources", errors);
      validateImages(event.images(), path + ".images", errors);
    }

    for (var duplicateId : duplicateIds) {
      errors.add(error("$.events", "duplicate event id: " + duplicateId));
    }

    return eventsById;
  }

  private void validateSources(
      List<CuratedSourceJson> sources, String path, List<ContentValidationError> errors) {
    if (sources == null || sources.isEmpty()) {
      errors.add(error(path, "event must have at least one source"));
      return;
    }

    for (int index = 0; index < sources.size(); index++) {
      var source = sources.get(index);
      var sourcePath = path + "[" + index + "]";
      if (source == null) {
        errors.add(error(sourcePath, "source must be present"));
        continue;
      }

      requireNonBlank(source.name(), sourcePath + ".name", "source name is required", errors);
      requireHttpsUrl(source.url(), sourcePath + ".url", "source url must be an absolute HTTPS URL", errors);
    }
  }

  private void validateImages(List<CuratedImageJson> images, String path, List<ContentValidationError> errors) {
    if (images == null || images.isEmpty()) {
      return;
    }

    var primaryCount = 0;
    for (int index = 0; index < images.size(); index++) {
      var image = images.get(index);
      var imagePath = path + "[" + index + "]";
      if (image == null) {
        errors.add(error(imagePath, "image must be present"));
        continue;
      }

      if (Boolean.TRUE.equals(image.primary())) {
        primaryCount += 1;
      }

      requireHttpsUrl(image.url(), imagePath + ".url", "image url must be an absolute HTTPS URL", errors);
      requireNonBlank(image.altText(), imagePath + ".altText", "image altText is required", errors);
      requireNonBlank(image.source(), imagePath + ".source", "image source is required", errors);
      requireHttpsUrl(image.sourceUrl(), imagePath + ".sourceUrl", "image sourceUrl must be an absolute HTTPS URL", errors);
      requireNonBlank(image.attribution(), imagePath + ".attribution", "image attribution is required", errors);
      requireNonBlank(image.license(), imagePath + ".license", "image license is required", errors);
      requireHttpsUrl(
          image.licenseUrl(), imagePath + ".licenseUrl", "image licenseUrl must be an absolute HTTPS URL", errors);
      if (image.creator() != null) {
        requireNonBlank(image.creator(), imagePath + ".creator", "image creator must not be blank when present", errors);
      }
    }

    if (primaryCount > 1) {
      errors.add(error(path, "event must not have more than one primary image"));
    }
  }

  private void validateDays(
      List<CuratedDayJson> days,
      Map<String, CuratedEventJson> eventsById,
      List<ContentValidationError> errors,
      List<ContentValidationWarning> warnings) {
    var seenDays = new HashSet<String>();

    for (int index = 0; index < days.size(); index++) {
      var day = days.get(index);
      var path = "$.days[" + index + "]";
      if (day == null) {
        errors.add(error(path, "day must be present"));
        continue;
      }

      validateMonthDay(day.month(), day.day(), path, errors);
      var dayKey = day.month() + "/" + day.day();
      if (day.month() != null && day.day() != null && !seenDays.add(dayKey)) {
        errors.add(error(path, "duplicate day entry: " + dayKey));
      }

      requireNonBlank(day.featuredEventId(), path + ".featuredEventId", "featuredEventId is required", errors);
      var featuredEvent = eventsById.get(day.featuredEventId());
      if (!blank(day.featuredEventId()) && featuredEvent == null) {
        errors.add(error(path + ".featuredEventId", "featuredEventId references an unknown event"));
      }

      var additionalEventIds = day.additionalEventIds();
      if (additionalEventIds == null) {
        additionalEventIds = List.of();
      }
      if (additionalEventIds.size() < 6) {
        warnings.add(warning(path + ".additionalEventIds", "day has fewer than 6 additional events"));
      }

      if (featuredEvent != null) {
        if (blank(featuredEvent.notificationTitle()) || blank(featuredEvent.notificationBody())) {
          errors.add(error(path + ".featuredEventId", "featured event must have notificationTitle and notificationBody"));
        }
        if (featuredEvent.images() == null || featuredEvent.images().stream().noneMatch(image -> Boolean.TRUE.equals(image.primary()))) {
          warnings.add(warning(path + ".featuredEventId", "featured event has no primary image"));
        }
      }

      validateAdditionalReferences(additionalEventIds, day.featuredEventId(), eventsById, path, errors);
    }
  }

  private void validateAdditionalReferences(
      List<String> additionalEventIds,
      String featuredEventId,
      Map<String, CuratedEventJson> eventsById,
      String path,
      List<ContentValidationError> errors) {
    var seenAdditionalIds = new HashSet<String>();

    for (int index = 0; index < additionalEventIds.size(); index++) {
      var eventId = additionalEventIds.get(index);
      var eventPath = path + ".additionalEventIds[" + index + "]";
      requireNonBlank(eventId, eventPath, "additional event id is required", errors);

      if (blank(eventId)) {
        continue;
      }
      if (!seenAdditionalIds.add(eventId)) {
        errors.add(error(eventPath, "duplicate additional event id: " + eventId));
      }
      if (eventId.equals(featuredEventId)) {
        errors.add(error(eventPath, "featured event must not also be listed as additional"));
      }
      if (!eventsById.containsKey(eventId)) {
        errors.add(error(eventPath, "additional event id references an unknown event"));
      }
    }
  }

  private void validateMonthDay(Integer month, Integer day, String path, List<ContentValidationError> errors) {
    if (month == null) {
      errors.add(error(path + ".month", "month is required"));
    }
    if (day == null) {
      errors.add(error(path + ".day", "day is required"));
    }
    if (month == null || day == null) {
      return;
    }
    try {
      MonthDay.of(month, day);
    } catch (DateTimeException exception) {
      errors.add(error(path, "month/day must be a valid calendar date"));
    }
  }

  private void requireNonBlank(
      String value, String path, String message, List<ContentValidationError> errors) {
    if (blank(value)) {
      errors.add(error(path, message));
    }
  }

  private void requireHttpsUrl(
      String value, String path, String message, List<ContentValidationError> errors) {
    if (blank(value)) {
      errors.add(error(path, message));
      return;
    }

    try {
      var uri = URI.create(value);
      if (!uri.isAbsolute() || !"https".equalsIgnoreCase(uri.getScheme()) || blank(uri.getHost())) {
        errors.add(error(path, message));
      }
    } catch (IllegalArgumentException exception) {
      errors.add(error(path, message));
    }
  }

  private static boolean blank(String value) {
    return value == null || value.isBlank();
  }

  private static ContentValidationError error(String path, String message) {
    return new ContentValidationError(path, message);
  }

  private static ContentValidationWarning warning(String path, String message) {
    return new ContentValidationWarning(path, message);
  }
}
