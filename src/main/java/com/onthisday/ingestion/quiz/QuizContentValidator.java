package com.onthisday.ingestion.quiz;

import com.onthisday.ingestion.ContentValidationError;
import com.onthisday.ingestion.ContentValidationResult;
import com.onthisday.ingestion.ContentValidationWarning;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class QuizContentValidator {

  private static final int SUPPORTED_SCHEMA_VERSION = 1;
  private static final Pattern SLUG_PATTERN = Pattern.compile("[a-z0-9]+(?:-[a-z0-9]+)*");
  private static final Set<String> QUESTION_TYPES =
      Set.of("multiple_choice", "true_false", "image_identification", "chronological_ordering");
  private static final Set<String> DIFFICULTIES = Set.of("easy", "medium", "hard");
  private static final Set<String> PUBLICATION_STATES = Set.of("draft", "published", "retired");
  private static final Set<String> COLLECTION_GROUPS =
      Set.of("topic", "historical_period", "civilization", "conflict_or_movement");

  private final boolean validateDistribution;

  public QuizContentValidator() {
    this(false);
  }

  public QuizContentValidator(boolean validateDistribution) {
    this.validateDistribution = validateDistribution;
  }

  public ContentValidationResult validate(CuratedQuizContent content) {
    var errors = new ArrayList<ContentValidationError>();
    var warnings = new ArrayList<ContentValidationWarning>();

    if (content == null) {
      errors.add(error("$", "quiz content must be present"));
      return new ContentValidationResult(errors, warnings);
    }

    var collectionIds = validateCollections(content.collectionsFile(), errors);
    var publishedCollectionCounts = new HashMap<String, Integer>();
    var aggregate = validateQuestionPacks(content.questionPacks(), collectionIds, publishedCollectionCounts, errors, warnings);
    addCatalogWarnings(aggregate.publishedCount(), collectionIds, publishedCollectionCounts, warnings);
    if (validateDistribution) {
      addDistributionWarnings(aggregate, warnings);
    }

    return new ContentValidationResult(errors, warnings);
  }

  private Set<String> validateCollections(
      QuizCollectionsFile collectionsFile, List<ContentValidationError> errors) {
    var collectionIds = new HashSet<String>();
    var duplicateCollectionIds = new HashSet<String>();

    if (collectionsFile == null) {
      errors.add(error("collections.json:$", "collections file must be present"));
      return collectionIds;
    }

    validateSchemaVersion(collectionsFile.schemaVersion(), "collections.json:$.schemaVersion", errors);
    var collections = collectionsFile.collections();
    if (collections == null) {
      errors.add(error("collections.json:$.collections", "collections must be present"));
      collections = List.of();
    }

    for (int index = 0; index < collections.size(); index++) {
      var collection = collections.get(index);
      var path = "collections.json:$.collections[" + index + "]";
      if (collection == null) {
        errors.add(error(path, "collection must be present"));
        continue;
      }

      validateSlug(collection.id(), path + ".id", "collection id is required", errors);
      if (!blank(collection.id()) && !collectionIds.add(collection.id())) {
        duplicateCollectionIds.add(collection.id());
      }
      requireNonBlank(collection.name(), path + ".name", "collection name is required", errors);
      validateSupported(collection.group(), COLLECTION_GROUPS, path + ".group", "collection group is unsupported", errors);
    }

    for (var duplicateId : duplicateCollectionIds) {
      errors.add(error("collections.json:$.collections", "duplicate collection id: " + duplicateId));
    }
    return collectionIds;
  }

  private QuestionAggregate validateQuestionPacks(
      List<LoadedQuizQuestionPack> packs,
      Set<String> collectionIds,
      Map<String, Integer> publishedCollectionCounts,
      List<ContentValidationError> errors,
      List<ContentValidationWarning> warnings) {
    var questionIds = new HashSet<String>();
    var duplicateQuestionIds = new HashSet<String>();
    var aggregate = new QuestionAggregate();

    if (packs == null) {
      return aggregate;
    }

    for (var pack : packs) {
      if (pack == null || pack.file() == null) {
        errors.add(error("questions", "question pack must be present"));
        continue;
      }
      validateSchemaVersion(pack.file().schemaVersion(), pack.relativeFilename() + ":$.schemaVersion", errors);
      var questions = pack.file().questions();
      if (questions == null) {
        errors.add(error(pack.relativeFilename() + ":$.questions", "questions must be present"));
        questions = List.of();
      }

      for (int index = 0; index < questions.size(); index++) {
        var question = questions.get(index);
        var path = pack.relativeFilename() + ":$.questions[" + index + "]";
        if (question == null) {
          errors.add(error(path, "question must be present"));
          continue;
        }

        validateQuestion(question, path, collectionIds, publishedCollectionCounts, aggregate, errors, warnings);
        if (!blank(question.id()) && !questionIds.add(question.id())) {
          duplicateQuestionIds.add(question.id());
        }
      }
    }

    for (var duplicateId : duplicateQuestionIds) {
      errors.add(error("questions", "duplicate question id: " + duplicateId));
    }

    return aggregate;
  }

  private void validateQuestion(
      CuratedQuizQuestionJson question,
      String path,
      Set<String> collectionIds,
      Map<String, Integer> publishedCollectionCounts,
      QuestionAggregate aggregate,
      List<ContentValidationError> errors,
      List<ContentValidationWarning> warnings) {
    validateSlug(question.id(), path + ".id", "question id is required", errors);
    validateSupported(question.type(), QUESTION_TYPES, path + ".type", "question type is unsupported", errors);
    validateSupported(question.difficulty(), DIFFICULTIES, path + ".difficulty", "difficulty is unsupported", errors);
    validateSupported(
        question.publicationState(),
        PUBLICATION_STATES,
        path + ".publicationState",
        "publicationState is unsupported",
        errors);
    requireNonBlank(question.prompt(), path + ".prompt", "prompt is required", errors);
    requireNonBlank(question.explanation(), path + ".explanation", "explanation is required", errors);
    validateSources(question.sources(), path + ".sources", errors);
    validateCollectionReferences(question, path, collectionIds, publishedCollectionCounts, warnings, errors);

    if ("published".equals(question.publicationState())) {
      aggregate.record(question.type(), question.difficulty());
    }

    switch (question.type() == null ? "" : question.type()) {
      case "multiple_choice" -> validateChoiceQuestion(question, path, 4, false, errors);
      case "true_false" -> validateChoiceQuestion(question, path, 2, true, errors);
      case "image_identification" -> {
        validateChoiceQuestion(question, path, 4, false, errors);
        validateImage(question.image(), path + ".image", errors);
      }
      case "chronological_ordering" -> validateChronologicalQuestion(question, path, errors);
      default -> validateMutualExclusionForUnknownType(question, path, errors);
    }
  }

  private void validateChoiceQuestion(
      CuratedQuizQuestionJson question,
      String path,
      int expectedOptionCount,
      boolean trueFalse,
      List<ContentValidationError> errors) {
    if (question.items() != null) {
      errors.add(error(path + ".items", "choice question must not include items"));
    }
    if (question.correctOrderItemIds() != null) {
      errors.add(error(path + ".correctOrderItemIds", "choice question must not include correctOrderItemIds"));
    }
    if (!"image_identification".equals(question.type()) && question.image() != null) {
      errors.add(error(path + ".image", "only image_identification questions may include image"));
      validateImage(question.image(), path + ".image", errors);
    }

    var options = question.options();
    if (options == null) {
      errors.add(error(path + ".options", "options must be present"));
      options = List.of();
    }
    if (options.size() != expectedOptionCount) {
      errors.add(error(path + ".options", "options must contain exactly " + expectedOptionCount + " entries"));
    }

    var optionIds = new HashSet<String>();
    for (int index = 0; index < options.size(); index++) {
      var option = options.get(index);
      var optionPath = path + ".options[" + index + "]";
      if (option == null) {
        errors.add(error(optionPath, "option must be present"));
        continue;
      }
      validateSlug(option.id(), optionPath + ".id", "option id is required", errors);
      if (!blank(option.id()) && !optionIds.add(option.id())) {
        errors.add(error(optionPath + ".id", "duplicate option id: " + option.id()));
      }
      requireNonBlank(option.text(), optionPath + ".text", "option text is required", errors);
    }

    requireNonBlank(question.correctOptionId(), path + ".correctOptionId", "correctOptionId is required", errors);
    if (!blank(question.correctOptionId()) && !optionIds.contains(question.correctOptionId())) {
      errors.add(error(path + ".correctOptionId", "correctOptionId must reference an option"));
    }

    if (trueFalse) {
      validateTrueFalseOptions(options, path, errors);
    }
  }

  private void validateTrueFalseOptions(
      List<CuratedQuizOptionJson> options, String path, List<ContentValidationError> errors) {
    if (options.size() != 2 || options.get(0) == null || options.get(1) == null) {
      return;
    }
    if (!"true".equals(options.get(0).id()) || !"True".equals(options.get(0).text())) {
      errors.add(error(path + ".options[0]", "true_false option 1 must be id true with text True"));
    }
    if (!"false".equals(options.get(1).id()) || !"False".equals(options.get(1).text())) {
      errors.add(error(path + ".options[1]", "true_false option 2 must be id false with text False"));
    }
  }

  private void validateChronologicalQuestion(
      CuratedQuizQuestionJson question, String path, List<ContentValidationError> errors) {
    if (question.options() != null) {
      errors.add(error(path + ".options", "chronological_ordering question must not include options"));
    }
    if (question.correctOptionId() != null) {
      errors.add(error(path + ".correctOptionId", "chronological_ordering question must not include correctOptionId"));
    }
    if (question.image() != null) {
      errors.add(error(path + ".image", "chronological_ordering question must not include image"));
      validateImage(question.image(), path + ".image", errors);
    }

    var items = question.items();
    if (items == null) {
      errors.add(error(path + ".items", "items must be present"));
      items = List.of();
    }
    if (items.size() != 4) {
      errors.add(error(path + ".items", "items must contain exactly 4 entries"));
    }

    var itemIds = new HashSet<String>();
    for (int index = 0; index < items.size(); index++) {
      var item = items.get(index);
      var itemPath = path + ".items[" + index + "]";
      if (item == null) {
        errors.add(error(itemPath, "item must be present"));
        continue;
      }
      validateSlug(item.id(), itemPath + ".id", "item id is required", errors);
      if (!blank(item.id()) && !itemIds.add(item.id())) {
        errors.add(error(itemPath + ".id", "duplicate item id: " + item.id()));
      }
      requireNonBlank(item.text(), itemPath + ".text", "item text is required", errors);
    }

    var correctOrderItemIds = question.correctOrderItemIds();
    if (correctOrderItemIds == null) {
      errors.add(error(path + ".correctOrderItemIds", "correctOrderItemIds must be present"));
      return;
    }
    if (correctOrderItemIds.size() != 4) {
      errors.add(error(path + ".correctOrderItemIds", "correctOrderItemIds must contain exactly 4 entries"));
    }
    var orderedIds = new HashSet<String>();
    for (int index = 0; index < correctOrderItemIds.size(); index++) {
      var itemId = correctOrderItemIds.get(index);
      var itemPath = path + ".correctOrderItemIds[" + index + "]";
      validateSlug(itemId, itemPath, "correctOrderItemId is required", errors);
      if (!blank(itemId) && !orderedIds.add(itemId)) {
        errors.add(error(itemPath, "duplicate correctOrderItemId: " + itemId));
      }
      if (!blank(itemId) && !itemIds.contains(itemId)) {
        errors.add(error(itemPath, "correctOrderItemId must reference an item"));
      }
    }
    if (correctOrderItemIds.size() == 4 && orderedIds.size() != itemIds.size()) {
      errors.add(error(path + ".correctOrderItemIds", "correctOrderItemIds must reference each item exactly once"));
    }
  }

  private void validateMutualExclusionForUnknownType(
      CuratedQuizQuestionJson question, String path, List<ContentValidationError> errors) {
    if (question.options() != null && question.items() != null) {
      errors.add(error(path, "question must not include both options and items"));
    }
    if (question.correctOptionId() != null && question.correctOrderItemIds() != null) {
      errors.add(error(path, "question must not include both correctOptionId and correctOrderItemIds"));
    }
  }

  private void validateSources(
      List<CuratedQuizSourceJson> sources, String path, List<ContentValidationError> errors) {
    if (sources == null || sources.isEmpty()) {
      errors.add(error(path, "question must have at least one source"));
      return;
    }

    var sourceUrls = new HashSet<String>();
    for (int index = 0; index < sources.size(); index++) {
      var source = sources.get(index);
      var sourcePath = path + "[" + index + "]";
      if (source == null) {
        errors.add(error(sourcePath, "source must be present"));
        continue;
      }
      requireNonBlank(source.displayName(), sourcePath + ".displayName", "source displayName is required", errors);
      requireHttpsUrl(source.url(), sourcePath + ".url", "source url must be an absolute HTTPS URL", errors);
      if (!blank(source.url()) && !sourceUrls.add(source.url())) {
        errors.add(error(sourcePath + ".url", "duplicate source url: " + source.url()));
      }
    }
  }

  private void validateImage(
      CuratedQuizImageJson image, String path, List<ContentValidationError> errors) {
    if (image == null) {
      errors.add(error(path, "image_identification question must include image"));
      return;
    }

    requireHttpsUrl(image.url(), path + ".url", "image url must be an absolute HTTPS URL", errors);
    requireNonBlank(image.altText(), path + ".altText", "image altText is required", errors);
    requireNonBlank(image.source(), path + ".source", "image source is required", errors);
    requireHttpsUrl(image.sourceUrl(), path + ".sourceUrl", "image sourceUrl must be an absolute HTTPS URL", errors);
    requireNonBlank(image.attribution(), path + ".attribution", "image attribution is required", errors);
    if (image.creator() != null) {
      requireNonBlank(image.creator(), path + ".creator", "image creator must not be blank when present", errors);
    }
    requireNonBlank(image.license(), path + ".license", "image license is required", errors);
    requireHttpsUrl(image.licenseUrl(), path + ".licenseUrl", "image licenseUrl must be an absolute HTTPS URL", errors);
  }

  private void validateCollectionReferences(
      CuratedQuizQuestionJson question,
      String path,
      Set<String> collectionIds,
      Map<String, Integer> publishedCollectionCounts,
      List<ContentValidationWarning> warnings,
      List<ContentValidationError> errors) {
    var referencedCollectionIds = question.collectionIds();
    if (referencedCollectionIds == null) {
      referencedCollectionIds = List.of();
    }

    if (referencedCollectionIds.isEmpty()) {
      warnings.add(warning(path + ".collectionIds", "question has no collection membership"));
    }

    var seen = new HashSet<String>();
    for (int index = 0; index < referencedCollectionIds.size(); index++) {
      var collectionId = referencedCollectionIds.get(index);
      var collectionPath = path + ".collectionIds[" + index + "]";
      validateSlug(collectionId, collectionPath, "collection id is required", errors);
      if (blank(collectionId)) {
        continue;
      }
      if (!seen.add(collectionId)) {
        errors.add(error(collectionPath, "duplicate collection reference: " + collectionId));
      }
      if (!collectionIds.contains(collectionId)) {
        errors.add(error(collectionPath, "collection id references an unknown collection"));
      }
      if ("published".equals(question.publicationState())) {
        publishedCollectionCounts.merge(collectionId, 1, Integer::sum);
      }
    }
  }

  private void addCatalogWarnings(
      int publishedQuestionCount,
      Set<String> collectionIds,
      Map<String, Integer> publishedCollectionCounts,
      List<ContentValidationWarning> warnings) {
    if (publishedQuestionCount == 0) {
      warnings.add(warning("questions", "no published quiz questions exist"));
    }

    for (var collectionId : collectionIds) {
      var publishedCount = publishedCollectionCounts.getOrDefault(collectionId, 0);
      if (publishedCount < 5) {
        warnings.add(
            warning(
                "collections.json:$.collections",
                "collection " + collectionId + " has fewer than 5 published questions"));
      } else if (publishedCount < 10) {
        warnings.add(
            warning(
                "collections.json:$.collections",
                "collection " + collectionId + " supports 5 questions only"));
      } else if (publishedCount < 20) {
        warnings.add(
            warning(
                "collections.json:$.collections",
                "collection " + collectionId + " supports 5 and 10 questions only"));
      }
    }
  }

  private void addDistributionWarnings(QuestionAggregate aggregate, List<ContentValidationWarning> warnings) {
    if (aggregate.publishedCount() < 20) {
      return;
    }
    for (var type : QUESTION_TYPES) {
      if (aggregate.typeCounts().getOrDefault(type, 0) == 0) {
        warnings.add(warning("questions", "published questions omit type " + type));
      }
    }
    for (var difficulty : DIFFICULTIES) {
      if (aggregate.difficultyCounts().getOrDefault(difficulty, 0) == 0) {
        warnings.add(warning("questions", "published questions omit difficulty " + difficulty));
      }
    }
  }

  private void validateSchemaVersion(
      Integer schemaVersion, String path, List<ContentValidationError> errors) {
    if (schemaVersion == null) {
      errors.add(error(path, "schemaVersion is required"));
    } else if (schemaVersion != SUPPORTED_SCHEMA_VERSION) {
      errors.add(error(path, "schemaVersion must be 1"));
    }
  }

  private void validateSupported(
      String value,
      Set<String> supportedValues,
      String path,
      String message,
      List<ContentValidationError> errors) {
    requireNonBlank(value, path, message, errors);
    if (!blank(value) && !supportedValues.contains(value)) {
      errors.add(error(path, message));
    }
  }

  private void validateSlug(
      String value, String path, String requiredMessage, List<ContentValidationError> errors) {
    requireNonBlank(value, path, requiredMessage, errors);
    if (!blank(value) && !SLUG_PATTERN.matcher(value).matches()) {
      errors.add(error(path, "id must use lowercase letters, numbers, and hyphens"));
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

  private static final class QuestionAggregate {
    private int publishedCount;
    private final Map<String, Integer> typeCounts = new HashMap<>();
    private final Map<String, Integer> difficultyCounts = new HashMap<>();

    void record(String type, String difficulty) {
      publishedCount += 1;
      typeCounts.merge(type, 1, Integer::sum);
      difficultyCounts.merge(difficulty, 1, Integer::sum);
    }

    int publishedCount() {
      return publishedCount;
    }

    Map<String, Integer> typeCounts() {
      return typeCounts;
    }

    Map<String, Integer> difficultyCounts() {
      return difficultyCounts;
    }
  }
}
