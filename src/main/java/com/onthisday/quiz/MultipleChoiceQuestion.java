package com.onthisday.quiz;

import static com.onthisday.quiz.QuizChecks.copyNonEmpty;
import static com.onthisday.quiz.QuizChecks.requireChoiceOptions;
import static com.onthisday.quiz.QuizChecks.requireNonBlank;
import static com.onthisday.quiz.QuizChecks.requireNonNull;
import static com.onthisday.quiz.QuizChecks.requireSlug;

import java.util.List;

public record MultipleChoiceQuestion(
    String id,
    QuizDifficulty difficulty,
    QuestionPublicationState publicationState,
    String prompt,
    String explanation,
    List<QuizSource> sources,
    List<QuizOption> options)
    implements QuizQuestion {

  public MultipleChoiceQuestion {
    id = requireSlug(id, "id");
    difficulty = requireNonNull(difficulty, "difficulty");
    publicationState = requireNonNull(publicationState, "publicationState");
    prompt = requireNonBlank(prompt, "prompt");
    explanation = requireNonBlank(explanation, "explanation");
    sources = copyNonEmpty(sources, "sources");
    options = requireChoiceOptions(options, 4, false);
  }

  @Override
  public QuestionType type() {
    return QuestionType.MULTIPLE_CHOICE;
  }
}
