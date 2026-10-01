package com.onthisday.notifications;

import java.time.ZoneId;
import java.util.Optional;

/**
 * Accepts only region-based IANA identifiers such as {@code America/Jamaica} or {@code UTC}.
 * {@link ZoneId#of(String)} also accepts fixed offsets like {@code +05:00} or {@code GMT+5},
 * which do not follow daylight saving, so registrations must not store them.
 */
public final class IanaTimezones {

  private IanaTimezones() {}

  public static boolean isValid(String timezone) {
    return timezone != null && ZoneId.getAvailableZoneIds().contains(timezone);
  }

  public static Optional<ZoneId> parse(String timezone) {
    return isValid(timezone) ? Optional.of(ZoneId.of(timezone)) : Optional.empty();
  }
}
