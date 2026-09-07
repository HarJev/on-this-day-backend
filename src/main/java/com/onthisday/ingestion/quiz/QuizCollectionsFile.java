package com.onthisday.ingestion.quiz;

import java.util.List;

public record QuizCollectionsFile(Integer schemaVersion, List<CuratedQuizCollectionJson> collections) {}
