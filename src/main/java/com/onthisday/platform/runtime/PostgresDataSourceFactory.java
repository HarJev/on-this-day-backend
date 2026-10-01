package com.onthisday.platform.runtime;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;
import org.postgresql.ds.PGSimpleDataSource;

public final class PostgresDataSourceFactory {

  private static final String CLASSPATH_PREFIX = "classpath:";
  private static final String DEFAULT_SSL_FACTORY = "org.postgresql.ssl.LibPQFactory";
  // The driver reads root certificates from files, so a bundled one is copied out once.
  private static final Map<String, Path> EXTRACTED_CERTS = new ConcurrentHashMap<>();

  public DataSource create(DatabaseConfig config) {
    var dataSource = new PGSimpleDataSource();
    dataSource.setUrl(config.jdbcUrl());
    dataSource.setUser(config.user());
    dataSource.setPassword(config.password());
    dataSource.setConnectTimeout(config.connectTimeoutSeconds());
    dataSource.setLoginTimeout(config.connectTimeoutSeconds());
    dataSource.setSocketTimeout(config.socketTimeoutSeconds());
    // Set after the URL so explicit TLS settings win over anything in its query string.
    if (config.sslMode() != null) {
      dataSource.setSslMode(config.sslMode());
    }
    if (config.sslRootCert() != null) {
      dataSource.setSslRootCert(rootCertPath(config.sslRootCert()).toString());
    }
    if (config.verifiesServer()) {
      // The default factory is the one that checks the certificate chain and host name.
      dataSource.setSslfactory(DEFAULT_SSL_FACTORY);
    }
    return dataSource;
  }

  static Path rootCertPath(String location) {
    if (!location.startsWith(CLASSPATH_PREFIX)) {
      var path = Path.of(location);
      if (!Files.isRegularFile(path)) {
        throw new ConfigurationException("Database root certificate not found: " + location);
      }
      return path;
    }
    return EXTRACTED_CERTS.computeIfAbsent(
        location.substring(CLASSPATH_PREFIX.length()), PostgresDataSourceFactory::extract);
  }

  private static Path extract(String resource) {
    try (var input = PostgresDataSourceFactory.class.getClassLoader().getResourceAsStream(resource)) {
      if (input == null) {
        throw new ConfigurationException(
            "Database root certificate is not bundled: "
                + resource
                + ". Download it from the Supabase project's Database Settings into src/main/resources/"
                + resource
                + ".");
      }
      var file = Files.createTempFile("db-root-", ".crt");
      file.toFile().deleteOnExit();
      Files.copy(input, file, StandardCopyOption.REPLACE_EXISTING);
      return file;
    } catch (IOException exception) {
      throw new UncheckedIOException("Could not extract the database root certificate.", exception);
    }
  }
}
