package com.onthisday.platform.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;

class PostgresDataSourceFactoryTest {

  @Test
  void createsSimplePostgresDataSourceFromConfig() {
    var dataSource =
        new PostgresDataSourceFactory()
            .create(
                new DatabaseConfig(
                    "jdbc:postgresql://localhost:5432/on_this_day", "on_this_day", "secret"));

    var postgresDataSource = assertInstanceOf(PGSimpleDataSource.class, dataSource);
    assertEquals("on_this_day", postgresDataSource.getUser());
    assertEquals(5, postgresDataSource.getConnectTimeout());
    assertEquals(5, postgresDataSource.getLoginTimeout());
    assertEquals(10, postgresDataSource.getSocketTimeout());
  }

  @Test
  void appliesConfiguredTimeouts() {
    var dataSource =
        new PostgresDataSourceFactory()
            .create(
                new DatabaseConfig(
                    "jdbc:postgresql://localhost:5432/on_this_day",
                    "on_this_day",
                    "secret",
                    3,
                    7));

    var postgresDataSource = assertInstanceOf(PGSimpleDataSource.class, dataSource);
    assertEquals(3, postgresDataSource.getConnectTimeout());
    assertEquals(3, postgresDataSource.getLoginTimeout());
    assertEquals(7, postgresDataSource.getSocketTimeout());
  }

  @Test
  void verifiedTlsSettingsOverrideTheUrl() throws Exception {
    var cert = Files.createTempFile("root", ".crt");
    try {
      var dataSource =
          new PostgresDataSourceFactory()
              .create(
                  new DatabaseConfig(
                      "jdbc:postgresql://db.example:6543/postgres?sslmode=disable&sslfactory=org.postgresql.ssl.NonValidatingFactory",
                      "runtime",
                      "secret",
                      5,
                      10,
                      "verify-full",
                      cert.toString()));

      var postgresDataSource = assertInstanceOf(PGSimpleDataSource.class, dataSource);
      assertEquals("verify-full", postgresDataSource.getSslMode());
      assertEquals(cert.toString(), postgresDataSource.getSslRootCert());
      assertEquals("org.postgresql.ssl.LibPQFactory", postgresDataSource.getSslfactory());
    } finally {
      Files.deleteIfExists(cert);
    }
  }

  @Test
  void leavesTlsAloneWhenNotConfigured() {
    var postgresDataSource =
        assertInstanceOf(
            PGSimpleDataSource.class,
            new PostgresDataSourceFactory()
                .create(new DatabaseConfig("jdbc:postgresql://localhost/on_this_day", "local", "secret")));

    assertNull(postgresDataSource.getSslRootCert());
  }

  @Test
  void failsClosedWhenTheRootCertificateIsMissing() {
    assertThrows(
        ConfigurationException.class,
        () -> PostgresDataSourceFactory.rootCertPath("classpath:certs/not-bundled.crt"));
    assertThrows(
        ConfigurationException.class,
        () -> PostgresDataSourceFactory.rootCertPath("/nonexistent/root.crt"));
  }

  @Test
  void extractsABundledCertificateToAFile() throws Exception {
    var path = PostgresDataSourceFactory.rootCertPath("classpath:logback.xml");

    assertTrue(Files.isRegularFile(path));
    assertTrue(Files.readString(path).contains("<configuration>"));
  }
}
