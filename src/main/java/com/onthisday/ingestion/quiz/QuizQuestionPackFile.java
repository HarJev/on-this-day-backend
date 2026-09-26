package com.onthisday.ingestion.quiz;

import java.util.List;

public record QuizQuestionPackFile(Integer schemaVersion, List<CuratedQuizQuestionJson> questions) {}
