package com.onthisday.platform.quiz;

import com.onthisday.quiz.ChronologicalOrderingItem;
import com.onthisday.quiz.ChronologicalOrderingQuestion;
import com.onthisday.quiz.ImageIdentificationQuestion;
import com.onthisday.quiz.MultipleChoiceQuestion;
import com.onthisday.quiz.PlayableQuizQuestion;
import com.onthisday.quiz.QuestionType;
import com.onthisday.quiz.DailyQuiz;
import com.onthisday.quiz.QuickPlayQuiz;
import com.onthisday.quiz.QuizCatalog;
import com.onthisday.quiz.QuizCatalogCollection;
import com.onthisday.quiz.QuizCollection;
import com.onthisday.quiz.QuizImage;
import com.onthisday.quiz.QuizOption;
import com.onthisday.quiz.QuizRules;
import com.onthisday.quiz.QuizSelectionAvailability;
import com.onthisday.quiz.QuizSource;
import com.onthisday.quiz.TrueFalseQuestion;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class QuizResponseMapper {

  private static final DateTimeFormatter DAILY_DATE_FORMATTER =
      DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH);

  public ApiQuizCatalogResponse toCatalogResponse(QuizCatalog catalog) {
    var timerDefaults = catalog.quickPlayTimerDefaults();
    return new ApiQuizCatalogResponse(
        catalog.questionCounts(),
        new ApiQuickPlayTimerDefaultsResponse(
            timerDefaults.secondsFor(QuestionType.MULTIPLE_CHOICE),
            timerDefaults.secondsFor(QuestionType.TRUE_FALSE),
            timerDefaults.secondsFor(QuestionType.IMAGE_IDENTIFICATION),
            timerDefaults.secondsFor(QuestionType.CHRONOLOGICAL_ORDERING)),
        toAvailability(catalog.mixed()),
        catalog.collections().stream().map(this::toCatalogCollection).toList());
  }

  public ApiQuickPlayQuizResponse toQuickPlayResponse(QuickPlayQuiz quiz) {
    return new ApiQuickPlayQuizResponse(
        "quick_play",
        quiz.questions().size(),
        toSelection(quiz.collection()),
        new ApiQuickPlayTimerResponse("per_question", true),
        quiz.questions().stream()
            .map(question -> toQuestionResponse(question, true))
            .toList());
  }

  public ApiDailyQuizResponse toDailyResponse(DailyQuiz quiz) {
    var date = quiz.date();
    return new ApiDailyQuizResponse(
        "daily",
        "daily-" + date,
        new ApiDailyQuizDateResponse(date.toString(), DAILY_DATE_FORMATTER.format(date)),
        quiz.questions().size(),
        20,
        new ApiDailyTimerResponse("total", QuizRules.dailyTotalTimerSeconds(quiz.questions().size())),
        quiz.questions().stream()
            .map(question -> toQuestionResponse(question, false))
            .toList());
  }

  private ApiQuizCatalogCollectionResponse toCatalogCollection(QuizCatalogCollection item) {
    var collection = item.collection();
    var availability = item.availability();
    return new ApiQuizCatalogCollectionResponse(
        collection.id(),
        collection.name(),
        collection.group().value(),
        availability.publishedQuestionCount(),
        availability.supportedQuestionCounts());
  }

  private static ApiQuizSelectionAvailabilityResponse toAvailability(
      QuizSelectionAvailability availability) {
    return new ApiQuizSelectionAvailabilityResponse(
        availability.publishedQuestionCount(), availability.supportedQuestionCounts());
  }

  private static ApiQuizSelectionResponse toSelection(Optional<QuizCollection> collection) {
    return collection
        .map(value -> new ApiQuizSelectionResponse(value.id(), value.name()))
        .orElseGet(() -> new ApiQuizSelectionResponse(null, "Mixed"));
  }

  private ApiQuizQuestionResponse toQuestionResponse(
      PlayableQuizQuestion playableQuestion, boolean includeTimeLimit) {
    var question = playableQuestion.question();
    var timeLimitSeconds =
        includeTimeLimit ? QuizRules.quickPlayTimerDefaultsSeconds().get(question.type()) : null;
    return switch (question) {
      case MultipleChoiceQuestion choice ->
          new ApiChoiceQuizQuestionResponse(
              choice.id(),
              choice.type().value(),
              choice.difficulty().value(),
              choice.prompt(),
              timeLimitSeconds,
              presentedOptions(choice.options(), playableQuestion.presentationOrderIds()),
              correctOptionId(choice.options()),
              choice.explanation(),
              sources(choice.sources()));
      case TrueFalseQuestion trueFalse ->
          new ApiChoiceQuizQuestionResponse(
              trueFalse.id(),
              trueFalse.type().value(),
              trueFalse.difficulty().value(),
              trueFalse.prompt(),
              timeLimitSeconds,
              presentedOptions(trueFalse.options(), playableQuestion.presentationOrderIds()),
              correctOptionId(trueFalse.options()),
              trueFalse.explanation(),
              sources(trueFalse.sources()));
      case ImageIdentificationQuestion image ->
          new ApiImageIdentificationQuizQuestionResponse(
              image.id(),
              image.type().value(),
              image.difficulty().value(),
              image.prompt(),
              timeLimitSeconds,
              image(image.image()),
              presentedOptions(image.options(), playableQuestion.presentationOrderIds()),
              correctOptionId(image.options()),
              image.explanation(),
              sources(image.sources()));
      case ChronologicalOrderingQuestion ordering ->
          new ApiChronologicalOrderingQuizQuestionResponse(
              ordering.id(),
              ordering.type().value(),
              ordering.difficulty().value(),
              ordering.prompt(),
              timeLimitSeconds,
              presentedItems(ordering.items(), playableQuestion.presentationOrderIds()),
              ordering.items().stream()
                  .sorted(java.util.Comparator.comparingInt(ChronologicalOrderingItem::correctPosition))
                  .map(ChronologicalOrderingItem::id)
                  .toList(),
              ordering.explanation(),
              sources(ordering.sources()));
    };
  }

  private static List<ApiQuizOptionResponse> presentedOptions(
      List<QuizOption> options, List<String> presentationOrderIds) {
    var optionsById = byId(options, QuizOption::id);
    return presentationOrderIds.stream()
        .map(optionsById::get)
        .map(option -> new ApiQuizOptionResponse(option.id(), option.text()))
        .toList();
  }

  private static List<ApiChronologicalOrderingItemResponse> presentedItems(
      List<ChronologicalOrderingItem> items, List<String> presentationOrderIds) {
    var itemsById = byId(items, ChronologicalOrderingItem::id);
    return presentationOrderIds.stream()
        .map(itemsById::get)
        .map(item -> new ApiChronologicalOrderingItemResponse(item.id(), item.text()))
        .toList();
  }

  private static <T> Map<String, T> byId(List<T> values, Function<T, String> id) {
    return values.stream().collect(Collectors.toUnmodifiableMap(id, Function.identity()));
  }

  private static String correctOptionId(List<QuizOption> options) {
    return options.stream().filter(QuizOption::correct).findFirst().orElseThrow().id();
  }

  private static List<ApiQuizSourceResponse> sources(List<QuizSource> sources) {
    return sources.stream()
        .map(source -> new ApiQuizSourceResponse(source.displayName(), source.url()))
        .toList();
  }

  private static ApiQuizImageResponse image(QuizImage image) {
    return new ApiQuizImageResponse(
        image.url(),
        image.altText(),
        image.source(),
        image.sourceUrl(),
        image.attribution(),
        image.creator(),
        image.license(),
        image.licenseUrl());
  }
}
