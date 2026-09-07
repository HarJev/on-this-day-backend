package com.onthisday.quiz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;

class JdbcQuizQuestionRepositoryTest {

  private final JdbcQuizQuestionRepository repository = new JdbcQuizQuestionRepository(new PGSimpleDataSource());

  @Test
  void returnsEmptyInputWithoutOpeningADatabaseConnection() {
    assertEquals(List.of(), repository.findByIdsInOrder(List.of()));
  }

  @Test
  void rejectsInvalidDuplicateAndOversizedInputsBeforeQuerying() {
    assertThrows(InvalidQuizDefinitionException.class, () -> repository.findByIdsInOrder(null));
    assertThrows(InvalidQuizDefinitionException.class, () -> repository.findByIdsInOrder(List.of(" ")));
    assertThrows(
        InvalidQuizDefinitionException.class,
        () -> repository.findByIdsInOrder(List.of("same-question", "same-question")));

    var tooManyIds = new ArrayList<String>();
    for (var index = 1; index <= 21; index++) {
      tooManyIds.add("question-" + index);
    }
    assertThrows(InvalidQuizDefinitionException.class, () -> repository.findByIdsInOrder(tooManyIds));
  }
}
