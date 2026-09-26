package com.onthisday.quiz;

import static com.onthisday.quiz.QuizChecks.copyNonEmpty;
import static com.onthisday.quiz.QuizChecks.requireNonBlank;
import static com.onthisday.quiz.QuizChecks.requireNonNull;
import static com.onthisday.quiz.QuizChecks.requireOrderingItems;
import static com.onthisday.quiz.QuizChecks.requireSlug;

import java.util.List;

public record ChronologicalOrderingQuestion(
    String id,
    QuizDifficulty difficulty,
    QuestionPublicationState publicationState,
    String prompt,
    String explanation,
    List<QuizSource> sources,
    List<ChronologicalOrderingItem> items)
    implements QuizQuestion {

  public ChronologicalOrderingQuestion {
    id = requireSlug(id, "id");
    difficulty = requireNonNull(difficulty, "difficulty");
    publicationState = requireNonNull(publicationState, "publicationState");
    prompt = requireNonBlank(prompt, "prompt");
    explanation = requireNonBlank(explanation, "explanation");
    sources = copyNonEmpty(sources, "sources");
    items = requireOrderingItems(items);
  }

  @Override
  public QuestionType type() {
    return QuestionType.CHRONOLOGICAL_ORDERING;
  }
}
