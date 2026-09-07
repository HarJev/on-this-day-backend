package com.onthisday.quiz;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyChallengeRepository {

  Optional<DailyChallenge> findByDate(LocalDate date);

  /**
   * Atomically attempts to persist a complete challenge for its date.
   * Implementations return false when that date already exists; PostgreSQL
   * implementations use INSERT ... ON CONFLICT DO NOTHING rather than treating
   * a uniqueness exception as normal control flow.
   */
  boolean insertIfAbsent(DailyChallenge challenge);
}
