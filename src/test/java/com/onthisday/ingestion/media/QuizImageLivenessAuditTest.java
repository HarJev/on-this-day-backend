package com.onthisday.ingestion.media;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
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
          respond(exchange, 200, "image/jpeg", new byte[] {1, 2, 3});
        });

    var result = audit("image", serverUri("/image")).results().getFirst();

    assertTrue(result.reachable());
    assertEquals(200, result.statusCode());
    assertEquals("image/jpeg", result.contentType());
    assertEquals(3L, result.byteSize());
    assertNull(result.failure());
    assertEquals(QuizImageLivenessAuditConfiguration.DEFAULT_USER_AGENT, receivedUserAgent.get());
  }

  @Test
  void rejectsRedirectResponsesWithoutFollowingThem() throws IOException {
    startServer();
    server.createContext("/redirect", exchange -> redirect(exchange, "/image"));
    server.createContext("/image", exchange -> respond(exchange, 200, "image/jpeg", new byte[] {1}));

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
    server.createContext("/large", exchange -> respond(exchange, 200, "image/png", new byte[1025]));

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
            respond(exchange, 200, "image/jpeg", new byte[] {1});
          } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
          }
        });

    var result = audit("slow", serverUri("/slow"), 1024, Duration.ofMillis(50)).results().getFirst();

    assertFalse(result.reachable());
    assertEquals("request timed out", result.failure());
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

  private static void redirect(HttpExchange exchange, String location) throws IOException {
    exchange.getResponseHeaders().set("Location", location);
    exchange.sendResponseHeaders(302, -1);
    exchange.close();
  }
}
