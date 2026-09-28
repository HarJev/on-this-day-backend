package com.onthisday.ingestion.editorial;

import com.onthisday.ingestion.CuratedDayJson;
import com.onthisday.ingestion.CuratedEventJson;
import com.onthisday.ingestion.quiz.CuratedQuizQuestionJson;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Stable per-record fingerprints over exactly the fields the importers persist.
 *
 * <p>Canonical JSON and database rows are reduced to the same ordered field list, so a record
 * whose fingerprints differ was edited after its last import.
 */
public final class ContentFingerprints {

  private static final String FIELD_SEPARATOR = "\u001f";
  private static final String NULL_MARKER = "\u0000";

  private ContentFingerprints() {}

  public static String event(CuratedEventJson event) {
    var fields = new Fields();
    fields.add(
        event.id(),
        event.title(),
        event.year(),
        event.historicalDate(),
        event.dateNote(),
        event.summary(),
        event.description(),
        event.notificationTitle(),
        event.notificationBody());
    fields.add("sources");
    for (var source : safe(event.sources())) {
      fields.add(source.name(), source.url());
    }
    fields.add("images");
    for (var image : safe(event.images())) {
      fields.add(
          Boolean.toString(Boolean.TRUE.equals(image.primary())),
          image.url(),
          image.altText(),
          image.source(),
          image.sourceUrl(),
          image.creator(),
          image.attribution(),
          image.license(),
          image.licenseUrl());
    }
    return fields.hash();
  }

  public static String day(CuratedDayJson day) {
    var fields = new Fields();
    fields.add(monthDay(day.month(), day.day()), day.featuredEventId());
    fields.addAll(safe(day.additionalEventIds()));
    return fields.hash();
  }

  public static String question(CuratedQuizQuestionJson question) {
    var fields = new Fields();
    fields.add(
        question.id(),
        question.type(),
        question.difficulty(),
        question.publicationState(),
        question.prompt(),
        question.explanation());
    fields.add("options");
    for (var option : safe(question.options())) {
      fields.add(option.id(), option.text(), Boolean.toString(option.id().equals(question.correctOptionId())));
    }
    fields.add("items");
    var correctOrder = safe(question.correctOrderItemIds());
    var items = new ArrayList<>(safe(question.items()));
    items.sort(Comparator.comparing(item -> item.id()));
    for (var item : items) {
      fields.add(item.id(), item.text(), Integer.toString(correctOrder.indexOf(item.id()) + 1));
    }
    fields.add("image");
    var image = question.image();
    if (image != null) {
      fields.add(
          image.url(),
          image.altText(),
          image.source(),
          image.sourceUrl(),
          image.attribution(),
          image.creator(),
          image.license(),
          image.licenseUrl());
    }
    fields.add("sources");
    for (var source : safe(question.sources())) {
      fields.add(source.displayName(), source.url());
    }
    fields.add("collections");
    fields.addAll(safe(question.collectionIds()).stream().sorted().toList());
    return fields.hash();
  }

  /** One fingerprint for a whole record set, independent of file or row order. */
  public static String aggregate(Map<String, String> fingerprintsById) {
    var fields = new Fields();
    for (var entry : new TreeMap<>(fingerprintsById).entrySet()) {
      fields.add(entry.getKey(), entry.getValue());
    }
    return fields.hash();
  }

  public static String monthDay(Integer month, Integer day) {
    return String.format("%02d-%02d", month, day);
  }

  private static <T> List<T> safe(List<T> values) {
    return values == null ? List.of() : values;
  }

  /** Ordered field accumulator shared by the canonical and database readers. */
  static final class Fields {
    private final List<String> values = new ArrayList<>();

    Fields add(String... fields) {
      for (var field : fields) {
        values.add(field == null ? NULL_MARKER : field);
      }
      return this;
    }

    Fields addAll(List<String> fields) {
      for (var field : fields) {
        add(field);
      }
      return this;
    }

    String hash() {
      try {
        var digest = MessageDigest.getInstance("SHA-256");
        return HexFormat.of()
            .formatHex(digest.digest(String.join(FIELD_SEPARATOR, values).getBytes(StandardCharsets.UTF_8)));
      } catch (NoSuchAlgorithmException exception) {
        throw new IllegalStateException("SHA-256 is unavailable.", exception);
      }
    }
  }
}
