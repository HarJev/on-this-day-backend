package com.onthisday.quiz;

import java.util.List;

public sealed interface QuizQuestion
    permits MultipleChoiceQuestion,
        TrueFalseQuestion,
        ImageIdentificationQuestion,
        ChronologicalOrderingQuestion {

  String id();

  QuestionType type();

  QuizDifficulty difficulty();

  QuestionPublicationState publicationState();

  String prompt();

  String explanation();

  List<QuizSource> sources();
}
