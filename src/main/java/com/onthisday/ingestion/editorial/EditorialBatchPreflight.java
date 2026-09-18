package com.onthisday.ingestion.editorial;

import com.onthisday.ingestion.ContentValidationResult;
import com.onthisday.ingestion.CuratedContent;
import com.onthisday.ingestion.CuratedContentValidator;
import com.onthisday.ingestion.quiz.CuratedQuizContent;
import com.onthisday.ingestion.quiz.QuizContentValidator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Validates human approval and the selected canonical DTOs before any database connection is requested. */
public class EditorialBatchPreflight {

  private final EditorialReviewValidator reviewValidator;
  private final CuratedContentValidator historicalValidator;
  private final QuizContentValidator quizValidator;
  private final EditorialBatchSelector selector;

  public EditorialBatchPreflight(
      EditorialReviewValidator reviewValidator,
      CuratedContentValidator historicalValidator,
      QuizContentValidator quizValidator,
      EditorialBatchSelector selector) {
    this.reviewValidator = reviewValidator;
    this.historicalValidator = historicalValidator;
    this.quizValidator = quizValidator;
    this.selector = selector;
  }

  public EditorialPreflightResult validate(
      EditorialBatch batch, CuratedContent historicalContent, CuratedQuizContent quizContent) {
    var review = reviewValidator.validate(batch);
    var historical = selector.selectHistorical(historicalContent, batch.manifest());
    var quiz = selector.selectQuiz(quizContent, batch.manifest());
    var historicalValidation = historicalValidator.validate(historical.eventsFile(), historical.dailyEventsFile());
    var quizValidation = quizValidator.validate(quiz);
    var approvalErrors = approvalErrors(batch, historical, quiz);
    return new EditorialPreflightResult(review, historicalValidation, quizValidation, approvalErrors, historical, quiz);
  }

  private static List<String> approvalErrors(
      EditorialBatch batch, CuratedContent historical, CuratedQuizContent quiz) {
    var entries = new HashMap<String, EditorialReviewEntry>();
    for (var entry : safe(batch.reviewLedger().entries())) {
      if (entry != null && entry.canonicalId() != null) {
        entries.put(entry.canonicalId(), entry);
      }
    }
    var errors = new java.util.ArrayList<String>();
    for (var event : safe(historical.eventsFile().events())) {
      requireApproved(
          event.id(),
          safe(event.sources()).stream().map(source -> source.url()).toList(),
          event.images() != null && !event.images().isEmpty(), entries, errors);
    }
    for (var pack : quiz.questionPacks()) {
      for (var question : safe(pack.file().questions())) {
        requireApproved(
            question.id(),
            safe(question.sources()).stream().map(source -> source.url()).toList(),
            question.image() != null, entries, errors);
      }
    }
    return List.copyOf(errors);
  }

  private static void requireApproved(
      String canonicalId,
      List<String> sourceUrls,
      boolean hasImage,
      Map<String, EditorialReviewEntry> entries,
      List<String> errors) {
    var entry = entries.get(canonicalId);
    if (entry == null || !"approved".equals(entry.reviewStatus())) {
      errors.add(canonicalId + ": canonical content has no approved review entry");
      return;
    }
    var verifiedUrls = new HashSet<String>();
    for (var check : safe(entry.sourceChecks())) {
      if (check != null && "verified_supporting".equals(check.status())) {
        verifiedUrls.add(check.url());
      }
    }
    if (!verifiedUrls.containsAll(sourceUrls)) {
      errors.add(canonicalId + ": every canonical source URL must be verified_supporting in the ledger");
    }
    if (hasImage && !"verified".equals(entry.imageRightsStatus())) {
      errors.add(canonicalId + ": image content requires verified image rights");
    }
  }

  private static <T> List<T> safe(List<T> values) {
    return values == null ? List.of() : values;
  }
}
