package com.onthisday.ingestion.editorial;

import com.onthisday.ingestion.ContentValidationError;
import com.onthisday.ingestion.ContentValidationResult;
import java.net.URI;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class EditorialReviewValidator {

  private static final Pattern SLUG = Pattern.compile("[a-z0-9]+(?:-[a-z0-9]+)*");
  private static final Set<String> KINDS = Set.of("event", "quiz");
  private static final Set<String> REVIEW_STATUSES = Set.of("candidate", "source_verified", "reviewed", "approved", "rejected");
  private static final Set<String> SOURCE_STATUSES = Set.of("unknown", "unreachable", "verified_supporting", "verified_bad");
  private static final Set<String> IMAGE_RIGHTS_STATUSES = Set.of("not_applicable", "unknown", "unreachable", "verified", "verified_bad");

  public ContentValidationResult validate(EditorialBatch batch) {
    var errors = new ArrayList<ContentValidationError>();
    var warnings = new ArrayList<com.onthisday.ingestion.ContentValidationWarning>();
    if (batch == null) {
      errors.add(error("$", "editorial batch is required"));
      return new ContentValidationResult(errors, warnings);
    }

    var manifest = batch.manifest();
    var candidatesFile = batch.candidates();
    var ledger = batch.reviewLedger();
    validateManifest(manifest, errors);
    validateCandidates(candidatesFile, errors);
    validateLedger(ledger, errors);

    if (manifest == null || candidatesFile == null || ledger == null) {
      return new ContentValidationResult(errors, warnings);
    }
    if (manifest.schemaVersion() != 1 || candidatesFile.schemaVersion() != 1 || ledger.schemaVersion() != 1) {
      return new ContentValidationResult(errors, warnings);
    }
    if (!equals(manifest.batchId(), candidatesFile.batchId()) || !equals(manifest.batchId(), ledger.batchId())) {
      errors.add(error("$.batchId", "manifest, candidates, and review ledger must share batchId"));
    }

    var candidatesById = new HashMap<String, EditorialCandidate>();
    for (var candidate : safe(candidatesFile.candidates())) {
      if (candidate != null && !blank(candidate.candidateId())) {
        candidatesById.put(candidate.candidateId(), candidate);
      }
    }
    var reviewedCanonicalIds = new HashSet<String>();
    for (int index = 0; index < safe(ledger.entries()).size(); index++) {
      var entry = safe(ledger.entries()).get(index);
      var path = "$.entries[" + index + "]";
      if (entry == null) {
        continue;
      }
      var candidate = candidatesById.get(entry.candidateId());
      if (candidate == null) {
        errors.add(error(path + ".candidateId", "review entry references an unknown candidate"));
        continue;
      }
      if (!equals(candidate.proposedCanonicalId(), entry.canonicalId())) {
        errors.add(error(path + ".canonicalId", "review entry must match the candidate canonical ID"));
      }
      if (!blank(entry.canonicalId()) && !reviewedCanonicalIds.add(entry.canonicalId())) {
        errors.add(error(path + ".canonicalId", "duplicate review entry canonical ID"));
      }
      validateApproval(entry, path, errors);
    }
    return new ContentValidationResult(errors, warnings);
  }

  private void validateManifest(EditorialBatchManifest manifest, List<ContentValidationError> errors) {
    if (manifest == null) {
      errors.add(error("$.manifest", "editorial batch manifest is required"));
      return;
    }
    if (manifest.schemaVersion() != 1) {
      errors.add(error("$.schemaVersion", "schemaVersion must be 1"));
    }
    slug(manifest.batchId(), "$.batchId", "batchId is required", errors);
    required(manifest.candidateFile(), "$.candidateFile", "candidateFile is required", errors);
    required(manifest.reviewLedgerFile(), "$.reviewLedgerFile", "reviewLedgerFile is required", errors);
    for (int index = 0; index < safe(manifest.eventIds()).size(); index++) {
      slug(safe(manifest.eventIds()).get(index), "$.eventIds[" + index + "]", "event ID is required", errors);
    }
    for (int index = 0; index < safe(manifest.monthDays()).size(); index++) {
      monthDay(safe(manifest.monthDays()).get(index), "$.monthDays[" + index + "]", errors);
    }
    for (int index = 0; index < safe(manifest.quizPackFilenames()).size(); index++) {
      required(safe(manifest.quizPackFilenames()).get(index), "$.quizPackFilenames[" + index + "]", "quiz pack filename is required", errors);
    }
  }

  private void validateCandidates(EditorialCandidatesFile candidatesFile, List<ContentValidationError> errors) {
    if (candidatesFile == null) {
      errors.add(error("$.candidates", "candidate file is required"));
      return;
    }
    if (candidatesFile.schemaVersion() != 1) {
      errors.add(error("$.candidates.schemaVersion", "schemaVersion must be 1"));
    }
    var ids = new HashSet<String>();
    for (int index = 0; index < safe(candidatesFile.candidates()).size(); index++) {
      var candidate = safe(candidatesFile.candidates()).get(index);
      var path = "$.candidates[" + index + "]";
      if (candidate == null) {
        errors.add(error(path, "candidate is required"));
        continue;
      }
      slug(candidate.candidateId(), path + ".candidateId", "candidateId is required", errors);
      if (!blank(candidate.candidateId()) && !ids.add(candidate.candidateId())) {
        errors.add(error(path + ".candidateId", "duplicate candidateId"));
      }
      if (!KINDS.contains(candidate.kind())) {
        errors.add(error(path + ".kind", "kind must be event or quiz"));
      }
      slug(candidate.proposedCanonicalId(), path + ".proposedCanonicalId", "proposedCanonicalId is required", errors);
      required(candidate.workingClaim(), path + ".workingClaim", "workingClaim is required", errors);
      for (int sourceIndex = 0; sourceIndex < safe(candidate.sourceUrls()).size(); sourceIndex++) {
        https(safe(candidate.sourceUrls()).get(sourceIndex), path + ".sourceUrls[" + sourceIndex + "]", errors);
      }
    }
  }

  private void validateLedger(EditorialReviewLedger ledger, List<ContentValidationError> errors) {
    if (ledger == null) {
      errors.add(error("$.reviewLedger", "review ledger is required"));
      return;
    }
    if (ledger.schemaVersion() != 1) {
      errors.add(error("$.reviewLedger.schemaVersion", "schemaVersion must be 1"));
    }
    for (int index = 0; index < safe(ledger.entries()).size(); index++) {
      var entry = safe(ledger.entries()).get(index);
      var path = "$.entries[" + index + "]";
      if (entry == null) {
        errors.add(error(path, "review entry is required"));
        continue;
      }
      slug(entry.candidateId(), path + ".candidateId", "candidateId is required", errors);
      slug(entry.canonicalId(), path + ".canonicalId", "canonicalId is required", errors);
      if (!REVIEW_STATUSES.contains(entry.reviewStatus())) {
        errors.add(error(path + ".reviewStatus", "reviewStatus is unsupported"));
      }
      if (!IMAGE_RIGHTS_STATUSES.contains(entry.imageRightsStatus())) {
        errors.add(error(path + ".imageRightsStatus", "imageRightsStatus is unsupported"));
      }
      for (int checkIndex = 0; checkIndex < safe(entry.sourceChecks()).size(); checkIndex++) {
        var check = safe(entry.sourceChecks()).get(checkIndex);
        var checkPath = path + ".sourceChecks[" + checkIndex + "]";
        if (check == null) {
          errors.add(error(checkPath, "source check is required"));
          continue;
        }
        https(check.url(), checkPath + ".url", errors);
        date(check.checkedOn(), checkPath + ".checkedOn", errors);
        if (!SOURCE_STATUSES.contains(check.status())) {
          errors.add(error(checkPath + ".status", "source status is unsupported"));
        }
      }
      for (int dayIndex = 0; dayIndex < safe(entry.calendarDays()).size(); dayIndex++) {
        monthDay(safe(entry.calendarDays()).get(dayIndex), path + ".calendarDays[" + dayIndex + "]", errors);
      }
    }
  }

  private void validateApproval(EditorialReviewEntry entry, String path, List<ContentValidationError> errors) {
    if (!"approved".equals(entry.reviewStatus())) {
      return;
    }
    required(entry.reviewer(), path + ".reviewer", "approved entry requires reviewer", errors);
    date(entry.reviewedOn(), path + ".reviewedOn", errors);
    if (safe(entry.sourceChecks()).isEmpty()
        || safe(entry.sourceChecks()).stream()
            .anyMatch(
                check ->
                    check == null
                        || !"verified_supporting".equals(check.status())
                        || blank(check.checkedOn()))) {
      errors.add(error(path + ".sourceChecks", "approved entry requires only verified_supporting source checks"));
    }
  }

  private static void required(String value, String path, String message, List<ContentValidationError> errors) {
    if (blank(value)) {
      errors.add(error(path, message));
    }
  }

  private static void slug(String value, String path, String message, List<ContentValidationError> errors) {
    required(value, path, message, errors);
    if (!blank(value) && !SLUG.matcher(value).matches()) {
      errors.add(error(path, "must use lowercase letters, numbers, and hyphens"));
    }
  }

  private static void https(String value, String path, List<ContentValidationError> errors) {
    try {
      var uri = URI.create(value);
      if (!uri.isAbsolute() || !"https".equals(uri.getScheme()) || blank(uri.getHost())) {
        errors.add(error(path, "must be an absolute HTTPS URL"));
      }
    } catch (RuntimeException exception) {
      errors.add(error(path, "must be an absolute HTTPS URL"));
    }
  }

  private static void monthDay(String value, String path, List<ContentValidationError> errors) {
    try {
      if (value == null || !value.matches("\\d{2}-\\d{2}")) {
        throw new IllegalArgumentException();
      }
      MonthDay.of(Integer.parseInt(value.substring(0, 2)), Integer.parseInt(value.substring(3, 5)));
    } catch (RuntimeException exception) {
      errors.add(error(path, "must be a valid MM-dd calendar date"));
    }
  }

  private static void date(String value, String path, List<ContentValidationError> errors) {
    try {
      LocalDate.parse(value);
    } catch (RuntimeException exception) {
      errors.add(error(path, "must be an ISO-8601 date"));
    }
  }

  private static boolean blank(String value) {
    return value == null || value.isBlank();
  }

  private static boolean equals(String left, String right) {
    return java.util.Objects.equals(left, right);
  }

  private static <T> List<T> safe(List<T> values) {
    return values == null ? List.of() : values;
  }

  private static ContentValidationError error(String path, String message) {
    return new ContentValidationError(path, message);
  }
}
