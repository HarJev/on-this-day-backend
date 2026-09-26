package com.onthisday.ingestion.quiz;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;

class QuizContentImporterTest {

  @Test
  void validationFailureHappensBeforeDatabaseAccess() {
    var reader = new QuizContentReader(new ObjectMapper());
    var importer = new QuizContentImporter(new FailingDataSource(), new QuizContentValidator());
    var content = reader.read(java.nio.file.Path.of("src/test/resources/ingestion/quiz/invalid"));

    assertThrows(QuizContentImportException.class, () -> importer.importContent(content));
  }

  private static final class FailingDataSource implements DataSource {

    @Override
    public Connection getConnection() {
      throw new AssertionError("database should not be accessed when validation fails");
    }

    @Override
    public Connection getConnection(String username, String password) {
      throw new AssertionError("database should not be accessed when validation fails");
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
      throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
      throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
      throw new SQLFeatureNotSupportedException();
    }

    @Override
    public int getLoginTimeout() throws SQLException {
      throw new SQLFeatureNotSupportedException();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
      throw new SQLFeatureNotSupportedException();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
      throw new SQLFeatureNotSupportedException();
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
      return false;
    }
  }
}
