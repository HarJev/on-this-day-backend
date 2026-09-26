package com.onthisday.ingestion.media;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class QuizImageLivenessAuditTest {

  private HttpServer server;

  @AfterEach
  void stopServer() {
    if (server != null) {
      server.stop(0);
    }
  }

  @Test
  void acceptsA200ImageResponseAndUsesTheIdentifiableUserAgent() throws IOException {
    var receivedUserAgent = new AtomicReference<String>();
    startServer();
    server.createContext(
        "/image",
        exchange -> {
          receivedUserAgent.set(exchange.getRequestHeaders().getFirst("User-Agent"));
          respond(exchange, 200, "image/jpeg", jpeg(3));
        });

    var result = audit("image", serverUri("/image")).results().getFirst();

    assertTrue(result.reachable());
    assertEquals(200, result.statusCode());
    assertEquals("image/jpeg", result.contentType());
    assertEquals(3L + JPEG_HEADER.length, result.byteSize());
    assertNull(result.failure());
    assertEquals(QuizImageLivenessAuditConfiguration.DEFAULT_USER_AGENT, receivedUserAgent.get());
  }

  @Test
  void rejectsRedirectResponsesWithoutFollowingThem() throws IOException {
    startServer();
    server.createContext("/redirect", exchange -> redirect(exchange, "/image"));
    server.createContext("/image", exchange -> respond(exchange, 200, "image/jpeg", jpeg(1)));

    var result = audit("redirect", serverUri("/redirect")).results().getFirst();

    assertFalse(result.reachable());
    assertEquals(302, result.statusCode());
    assertEquals("redirect response", result.failure());
  }

  @Test
  void rejectsNonImageContentTypes() throws IOException {
    startServer();
    server.createContext("/html", exchange -> respond(exchange, 200, "text/html", "no image".getBytes()));

    var result = audit("html", serverUri("/html")).results().getFirst();

    assertFalse(result.reachable());
    assertEquals(200, result.statusCode());
    assertEquals("text/html", result.contentType());
    assertEquals("non-image content type", result.failure());
  }

  @Test
  void rejectsOversizedStreamingBodiesWithoutRetainingThem() throws IOException {
    startServer();
    server.createContext("/large", exchange -> respond(exchange, 200, "image/png", png(1025)));

    var result = audit("large", serverUri("/large"), 1024, Duration.ofSeconds(1)).results().getFirst();

    assertFalse(result.reachable());
    assertEquals("asset exceeds encoded-byte limit", result.failure());
    assertTrue(result.byteSize() > 1024);
  }

  @Test
  void rejectsSlowResponsesWithinTheConfiguredDeadline() throws IOException {
    startServer();
    server.createContext(
        "/slow",
        exchange -> {
          try {
            Thread.sleep(300);
            respond(exchange, 200, "image/jpeg", jpeg(1));
          } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
          }
        });

    var result = audit("slow", serverUri("/slow"), 1024, Duration.ofMillis(50)).results().getFirst();

    assertFalse(result.reachable());
    assertEquals("request timed out", result.failure());
  }

  @Test
  void rejectsABodyThatStallsAfterHeadersWithinTheDeadline() throws IOException {
    startServer();
    server.createContext(
        "/stall",
        exchange -> {
          exchange.getResponseHeaders().set("Content-Type", "image/jpeg");
          exchange.sendResponseHeaders(200, 1024);
          exchange.getResponseBody().write(jpeg(10));
          exchange.getResponseBody().flush();
          try {
            Thread.sleep(10_000);
          } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
          }
          exchange.close();
        });

    var result =
        assertTimeoutPreemptively(
            Duration.ofSeconds(3),
            () -> audit("stall", serverUri("/stall"), 4096, Duration.ofMillis(200)).results().getFirst());

    assertFalse(result.reachable());
    assertEquals("request timed out", result.failure());
  }

  @Test
  void rejectsImageTypesTheMobileGateDoesNotAccept() throws IOException {
    startServer();
    server.createContext(
        "/svg", exchange -> respond(exchange, 200, "image/svg+xml", "<svg/>".getBytes()));

    var result = audit("svg", serverUri("/svg")).results().getFirst();

    assertFalse(result.reachable());
    assertEquals("image/svg+xml", result.contentType());
    assertEquals("non-image content type", result.failure());
  }

  @Test
  void rejectsAMissingContentType() throws IOException {
    startServer();
    server.createContext(
        "/untyped",
        exchange -> {
          exchange.sendResponseHeaders(200, 4);
          exchange.getResponseBody().write(jpeg(0), 0, 4);
          exchange.close();
        });

    var result = audit("untyped", serverUri("/untyped")).results().getFirst();

    assertFalse(result.reachable());
    assertEquals("non-image content type", result.failure());
  }

  @Test
  void acceptsMediaTypeParametersAndCase() throws IOException {
    startServer();
    server.createContext(
        "/param", exchange -> respond(exchange, 200, "IMAGE/PNG; charset=binary", png(2)));

    var result = audit("param", serverUri("/param")).results().getFirst();

    assertTrue(result.reachable());
  }

  @Test
  void rejectsABodyThatDoesNotMatchTheDeclaredImageType() throws IOException {
    startServer();
    server.createContext(
        "/fake", exchange -> respond(exchange, 200, "image/jpeg", "<html>blocked</html>".getBytes()));

    var result = audit("fake", serverUri("/fake")).results().getFirst();

    assertFalse(result.reachable());
    assertEquals("content does not match declared image type", result.failure());
  }

  @Test
  void countsChunkedBodiesWithoutAContentLength() throws IOException {
    startServer();
    server.createContext("/chunked", exchange -> respondChunked(exchange, "image/jpeg", jpeg(500)));
    server.createContext("/chunked-large", exchange -> respondChunked(exchange, "image/jpeg", jpeg(5000)));

    var accepted = audit("chunked", serverUri("/chunked")).results().getFirst();
    var rejected = audit("chunked-large", serverUri("/chunked-large")).results().getFirst();

    assertTrue(accepted.reachable());
    assertEquals(500L + JPEG_HEADER.length, accepted.byteSize());
    assertFalse(rejected.reachable());
    assertEquals("asset exceeds encoded-byte limit", rejected.failure());
  }

  @Test
  void rejectsNon200Responses() throws IOException {
    startServer();
    server.createContext("/missing", exchange -> respond(exchange, 404, "text/plain", "gone".getBytes()));

    var result = audit("missing", serverUri("/missing")).results().getFirst();

    assertFalse(result.reachable());
    assertEquals(404, result.statusCode());
    assertEquals("non-200 response", result.failure());
  }

  @Test
  void reportsUnresolvableHostsAsUnreachable() {
    var result =
        audit("dns", URI.create("https://image-host.invalid/image.jpg"), 1024, Duration.ofSeconds(2))
            .results()
            .getFirst();

    assertFalse(result.reachable());
    assertEquals("image host unreachable", result.failure());
  }

  @Test
  void reportsEveryImageInInputOrderWithItsQuestionId() throws IOException {
    startServer();
    server.createContext("/ok", exchange -> respond(exchange, 200, "image/jpeg", jpeg(1)));
    server.createContext("/redirect", exchange -> redirect(exchange, "/ok"));
    var configuration =
        new QuizImageLivenessAuditConfiguration(
            Duration.ofSeconds(1), Duration.ofSeconds(1), 2, 1024,
            QuizImageLivenessAuditConfiguration.DEFAULT_USER_AGENT);

    var report =
        new QuizImageLivenessAudit(configuration)
            .auditImages(
                List.of(
                    new QuizImageLivenessAudit.PublishedQuizImage("first", serverUri("/ok")),
                    new QuizImageLivenessAudit.PublishedQuizImage("second", serverUri("/redirect")),
                    new QuizImageLivenessAudit.PublishedQuizImage("third", serverUri("/ok"))));

    assertEquals(
        List.of("first", "second", "third"),
        report.results().stream().map(QuizImageLivenessResult::questionId).toList());
    assertEquals(2, report.passedCount());
    assertFalse(report.successful());
  }

  @Test
  void reportsAnUnreachableHostWithoutExposingTransportDetails() throws IOException {
    int unusedPort;
    try (var socket = new ServerSocket(0)) {
      unusedPort = socket.getLocalPort();
    }

    var result =
        audit("unreachable", URI.create("http://127.0.0.1:" + unusedPort + "/image"), 1024, Duration.ofMillis(250))
            .results()
            .getFirst();

    assertFalse(result.reachable());
    assertEquals("image host unreachable", result.failure());
    assertNull(result.statusCode());
  }

  private QuizImageLivenessReport audit(String questionId, URI url) {
    return audit(questionId, url, 1024, Duration.ofSeconds(1));
  }

  private QuizImageLivenessReport audit(
      String questionId, URI url, long maximumBytes, Duration requestTimeout) {
    var configuration =
        new QuizImageLivenessAuditConfiguration(
            Duration.ofSeconds(1), requestTimeout, 2, maximumBytes,
            QuizImageLivenessAuditConfiguration.DEFAULT_USER_AGENT);
    return new QuizImageLivenessAudit(configuration)
        .auditImages(List.of(new QuizImageLivenessAudit.PublishedQuizImage(questionId, url)));
  }

  private void startServer() throws IOException {
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.setExecutor(Executors.newCachedThreadPool());
    server.start();
  }

  private URI serverUri(String path) {
    return URI.create("http://127.0.0.1:" + server.getAddress().getPort() + path);
  }

  private static void respond(HttpExchange exchange, int status, String contentType, byte[] body)
      throws IOException {
    exchange.getResponseHeaders().set("Content-Type", contentType);
    exchange.sendResponseHeaders(status, body.length);
    exchange.getResponseBody().write(body);
    exchange.close();
  }

  private static final byte[] JPEG_HEADER = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0};
  private static final byte[] PNG_HEADER = {
    (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
  };

  private static byte[] jpeg(int extraBytes) {
    return withHeader(JPEG_HEADER, extraBytes);
  }

  private static byte[] png(int totalBytes) {
    return withHeader(PNG_HEADER, Math.max(0, totalBytes - PNG_HEADER.length));
  }

  private static byte[] withHeader(byte[] header, int extraBytes) {
    var bytes = new byte[header.length + extraBytes];
    System.arraycopy(header, 0, bytes, 0, header.length);
    return bytes;
  }

  private static void respondChunked(HttpExchange exchange, String contentType, byte[] body)
      throws IOException {
    exchange.getResponseHeaders().set("Content-Type", contentType);
    exchange.sendResponseHeaders(200, 0);
    try (var output = exchange.getResponseBody()) {
      for (var offset = 0; offset < body.length; offset += 256) {
        output.write(body, offset, Math.min(256, body.length - offset));
        output.flush();
      }
    }
  }

  private static void redirect(HttpExchange exchange, String location) throws IOException {
    exchange.getResponseHeaders().set("Location", location);
    exchange.sendResponseHeaders(302, -1);
    exchange.close();
  }
}
