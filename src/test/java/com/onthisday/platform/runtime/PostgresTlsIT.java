package com.onthisday.platform.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.images.builder.Transferable;

/**
 * Proves the production TLS rule against a real server: a throwaway CA and server certificate
 * for "localhost" are made with the JDK's keytool for each run, so no key is ever committed.
 */
class PostgresTlsIT {

  // Generated per run, like the certificates, so the test holds no fixed credentials.
  private static final String STOREPASS = UUID.randomUUID().toString();
  private static final String DB_SECRET = UUID.randomUUID().toString();

  @TempDir static Path dir;

  static PostgreSQLContainer<?> postgres;
  static Path caCert;
  static Path otherCaCert;

  @BeforeAll
  static void startTlsPostgres() throws Exception {
    keytool("-genkeypair", "-alias", "ca", "-dname", "CN=Test CA", "-ext", "bc:c", "-keyalg", "RSA", "-keysize", "2048", "-validity", "2");
    keytool("-genkeypair", "-alias", "other", "-dname", "CN=Other CA", "-ext", "bc:c", "-keyalg", "RSA", "-keysize", "2048", "-validity", "2");
    keytool("-genkeypair", "-alias", "server", "-dname", "CN=localhost", "-keyalg", "RSA", "-keysize", "2048", "-validity", "2");
    keytool("-certreq", "-alias", "server", "-file", dir.resolve("server.csr").toString());
    keytool("-gencert", "-alias", "ca", "-infile", dir.resolve("server.csr").toString(), "-outfile", dir.resolve("server.crt").toString(), "-ext", "SAN=dns:localhost", "-rfc", "-validity", "2");
    keytool("-exportcert", "-alias", "ca", "-rfc", "-file", dir.resolve("ca.crt").toString());
    keytool("-exportcert", "-alias", "other", "-rfc", "-file", dir.resolve("other.crt").toString());
    caCert = dir.resolve("ca.crt");
    otherCaCert = dir.resolve("other.crt");

    postgres =
        new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("on_this_day")
            .withUsername("on_this_day")
            .withPassword(DB_SECRET)
            .withCopyToContainer(Transferable.of(Files.readAllBytes(dir.resolve("server.crt"))), "/tmp/ssl/server.crt")
            .withCopyToContainer(Transferable.of(serverKeyPem()), "/tmp/ssl/server.key")
            .withCommand(
                "sh",
                "-c",
                "install -o postgres -m 600 /tmp/ssl/server.key /var/lib/postgresql/server.key"
                    + " && install -o postgres -m 644 /tmp/ssl/server.crt /var/lib/postgresql/server.crt"
                    + " && exec docker-entrypoint.sh postgres -c fsync=off -c ssl=on"
                    + " -c ssl_cert_file=/var/lib/postgresql/server.crt"
                    + " -c ssl_key_file=/var/lib/postgresql/server.key");
    postgres.start();
  }

  @AfterAll
  static void stopTlsPostgres() {
    if (postgres != null) {
      postgres.stop();
    }
  }

  @Test
  void productionSettingsConnectWithVerifyFull() throws SQLException {
    var config = productionConfig(postgres.getJdbcUrl(), caCert);

    assertEquals("verify-full", config.sslMode());
    assertEquals("on", queryOne(config, "SELECT CASE WHEN ssl THEN 'on' ELSE 'off' END FROM pg_stat_ssl WHERE pid = pg_backend_pid()"));
  }

  @Test
  void refusesAServerSignedByAnotherCa() {
    var config = productionConfig(postgres.getJdbcUrl(), otherCaCert);

    assertThrows(SQLException.class, () -> queryOne(config, "SELECT 1"));
  }

  @Test
  void refusesAHostNameTheCertificateDoesNotName() {
    var url = postgres.getJdbcUrl().replace("//localhost:", "//127.0.0.1:");
    var config = productionConfig(url, caCert);

    assertThrows(SQLException.class, () -> queryOne(config, "SELECT 1"));
  }

  @Test
  void anInsecureUrlCannotWeakenProductionTls() {
    var config = productionConfig(postgres.getJdbcUrl() + "?sslmode=disable", otherCaCert);

    assertThrows(SQLException.class, () -> queryOne(config, "SELECT 1"));
  }

  private static DatabaseConfig productionConfig(String url, Path rootCert) {
    return DatabaseConfig.resolve(
        Map.of(
            DatabaseConfig.DB_JDBC_URL, url,
            DatabaseConfig.DB_USER, "on_this_day",
            DatabaseConfig.DB_PASSWORD_SSM_PARAMETER, "/on-this-day/test/db-password",
            DatabaseConfig.DB_SSL_ROOT_CERT, rootCert.toString()),
        name -> DB_SECRET);
  }

  private static String queryOne(DatabaseConfig config, String sql) throws SQLException {
    try (var connection = new PostgresDataSourceFactory().create(config).getConnection();
        var statement = connection.createStatement();
        var result = statement.executeQuery(sql)) {
      result.next();
      return result.getString(1);
    }
  }

  private static byte[] serverKeyPem() throws Exception {
    var store = KeyStore.getInstance("PKCS12");
    try (var input = new FileInputStream(dir.resolve("keystore.p12").toFile())) {
      store.load(input, STOREPASS.toCharArray());
    }
    var key = (PrivateKey) store.getKey("server", STOREPASS.toCharArray());
    var body = Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.US_ASCII)).encodeToString(key.getEncoded());
    return ("-----BEGIN PRIVATE KEY-----\n" + body + "\n-----END PRIVATE KEY-----\n")
        .getBytes(StandardCharsets.US_ASCII);
  }

  private static void keytool(String... args) throws Exception {
    var command = new ArrayList<>(List.of(Path.of(System.getProperty("java.home"), "bin", "keytool").toString()));
    command.addAll(List.of(args));
    command.addAll(List.of("-keystore", dir.resolve("keystore.p12").toString(), "-storetype", "PKCS12", "-storepass", STOREPASS, "-noprompt"));
    var process = new ProcessBuilder(command).redirectErrorStream(true).start();
    var output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    if (process.waitFor() != 0) {
      throw new IllegalStateException("keytool failed: " + output);
    }
  }
}
