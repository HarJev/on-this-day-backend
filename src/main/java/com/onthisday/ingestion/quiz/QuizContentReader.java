package com.onthisday.ingestion.quiz;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class QuizContentReader {

  private static final Path DEFAULT_CONTENT_DIR = Path.of("content", "quizzes");

  private final ObjectMapper objectMapper;

  public QuizContentReader(ObjectMapper objectMapper) {
    this.objectMapper =
        Objects.requireNonNull(objectMapper, "objectMapper must not be null")
            .copy()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
  }

  public CuratedQuizContent readDefault() {
    return read(DEFAULT_CONTENT_DIR);
  }

  public CuratedQuizContent read(Path contentDir) {
    Objects.requireNonNull(contentDir, "contentDir must not be null");

    var normalizedContentDir = contentDir.normalize();
    var collectionsPath = normalizedContentDir.resolve("collections.json").normalize();
    var questionsDir = normalizedContentDir.resolve("questions").normalize();

    try {
      var collectionsFile = objectMapper.readValue(collectionsPath.toFile(), QuizCollectionsFile.class);
      return new CuratedQuizContent(collectionsFile, readQuestionPacks(questionsDir));
    } catch (IOException exception) {
      throw new QuizContentImportException("Could not read curated quiz content JSON.", exception);
    }
  }

  private List<LoadedQuizQuestionPack> readQuestionPacks(Path questionsDir) throws IOException {
    if (!Files.isDirectory(questionsDir)) {
      return List.of();
    }

    try (var files = Files.list(questionsDir)) {
      return files
          .filter(Files::isRegularFile)
          .filter(path -> path.getFileName().toString().endsWith(".json"))
          .sorted(Comparator.comparing(path -> path.getFileName().toString()))
          .map(this::readQuestionPack)
          .toList();
    }
  }

  private LoadedQuizQuestionPack readQuestionPack(Path path) {
    try {
      var relativeFilename = "questions/" + path.getFileName();
      return new LoadedQuizQuestionPack(
          relativeFilename, objectMapper.readValue(path.toFile(), QuizQuestionPackFile.class));
    } catch (IOException exception) {
      throw new QuizContentImportException("Could not read curated quiz question pack JSON.", exception);
    }
  }
}
