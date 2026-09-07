package com.onthisday.ingestion.quiz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class QuizContentReaderTest {

  private final QuizContentReader reader = new QuizContentReader(new ObjectMapper());

  @Test
  void readsQuestionPacksInDeterministicFilenameOrder() {
    var content = reader.read(Path.of("src/test/resources/ingestion/quiz/reader"));

    assertEquals(2, content.questionPacks().size());
    assertEquals("questions/001-first.json", content.questionPacks().get(0).relativeFilename());
    assertEquals("questions/002-second.json", content.questionPacks().get(1).relativeFilename());
  }

  @Test
  void missingQuestionsDirectoryMeansNoQuestionPacks(@TempDir Path tempDir) throws IOException {
    Files.writeString(tempDir.resolve("collections.json"), "{\"schemaVersion\":1,\"collections\":[]}");

    var content = reader.read(tempDir);

    assertEquals(0, content.questionPacks().size());
  }

  @Test
  void unknownPropertiesFailClearly(@TempDir Path tempDir) throws IOException {
    Files.writeString(
        tempDir.resolve("collections.json"),
        "{\"schemaVersion\":1,\"collections\":[],\"unexpected\":true}");

    assertThrows(QuizContentImportException.class, () -> reader.read(tempDir));
  }

  @Test
  void malformedJsonFailsClearly(@TempDir Path tempDir) throws IOException {
    Files.writeString(tempDir.resolve("collections.json"), "{");

    assertThrows(QuizContentImportException.class, () -> reader.read(tempDir));
  }
}
