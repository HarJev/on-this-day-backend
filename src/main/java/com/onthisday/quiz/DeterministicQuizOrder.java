package com.onthisday.quiz;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

final class DeterministicQuizOrder {

  static final String DAILY_SELECTION_NAMESPACE = "on-this-day:daily-selection:v1:";
  static final String DAILY_PRESENTATION_NAMESPACE = "on-this-day:daily-presentation:v1:";

  private DeterministicQuizOrder() {}

  static List<QuizQuestionCandidate> candidates(
      LocalDate date, List<QuizQuestionCandidate> candidates) {
    var seed = DAILY_SELECTION_NAMESPACE + date;
    return sorted(seed, candidates, QuizQuestionCandidate::questionId);
  }

  static List<String> presentationIds(
      LocalDate date, String questionId, List<String> ids) {
    return sorted(DAILY_PRESENTATION_NAMESPACE + date + ":" + questionId, ids, id -> id);
  }

  private static <T> List<T> sorted(
      String seed, List<T> values, java.util.function.Function<T, String> id) {
    var ranked = new ArrayList<Ranked<T>>(values.size());
    for (var value : values) {
      var stableId = id.apply(value);
      ranked.add(new Ranked<>(value, stableId, digest(seed, stableId)));
    }
    ranked.sort(
        Comparator.comparing(
                (Ranked<T> rankedValue) -> rankedValue.digest(),
                DeterministicQuizOrder::compareUnsigned)
            .thenComparing(Ranked::id));
    return ranked.stream().map(Ranked::value).toList();
  }

  private static byte[] digest(String seed, String id) {
    try {
      var digest = MessageDigest.getInstance("SHA-256");
      digest.update(seed.getBytes(StandardCharsets.UTF_8));
      digest.update(id.getBytes(StandardCharsets.UTF_8));
      return digest.digest();
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is required by the Java runtime", exception);
    }
  }

  private static int compareUnsigned(byte[] left, byte[] right) {
    for (var index = 0; index < left.length; index++) {
      var comparison = Integer.compare(Byte.toUnsignedInt(left[index]), Byte.toUnsignedInt(right[index]));
      if (comparison != 0) {
        return comparison;
      }
    }
    return Integer.compare(left.length, right.length);
  }

  private record Ranked<T>(T value, String id, byte[] digest) {}
}
