package com.onthisday.notifications;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class NotificationDeliveryRepositoryIT {

  @Container
  static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16-alpine").withDatabaseName("on_this_day");

  private static final LocalDate OCT_1 = LocalDate.of(2026, 10, 1);
  private static final Instant TEN_AM = Instant.parse("2026-10-01T15:00:00Z");
  private static final Instant LEASE_END = TEN_AM.plusSeconds(15 * 60);

  private static DataSource dataSource;
  private JdbcNotificationDeliveryRepository deliveries;
  private JdbcDeviceRegistrationRepository devices;

  @BeforeAll
  static void migrateDatabase() {
    var postgresDataSource = new PGSimpleDataSource();
    postgresDataSource.setUrl(POSTGRES.getJdbcUrl());
    postgresDataSource.setUser(POSTGRES.getUsername());
    postgresDataSource.setPassword(POSTGRES.getPassword());
    dataSource = postgresDataSource;
    Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").load().migrate();
  }

  @BeforeEach
  void resetTables() throws SQLException {
    try (var connection = dataSource.getConnection();
        var statement = connection.createStatement()) {
      statement.execute("DELETE FROM notification_delivery");
      statement.execute("DELETE FROM device_registration");
    }
    deliveries = new JdbcNotificationDeliveryRepository(dataSource);
    devices = new JdbcDeviceRegistrationRepository(dataSource);
    register("token");
  }

  @Test
  void firstClaimWinsAndASentDateIsNeverReclaimed() throws SQLException {
    assertTrue(deliveries.claim("token", OCT_1, "event", TEN_AM, LEASE_END));
    assertFalse(deliveries.claim("token", OCT_1, "event", TEN_AM, LEASE_END));

    deliveries.markSent("token", OCT_1);

    assertFalse(deliveries.claim("token", OCT_1, "event", LEASE_END.plusSeconds(3600), LEASE_END));
    assertEquals("sent", status("token", OCT_1));
    assertEquals(1, attempts("token", OCT_1));
  }

  @Test
  void retryableFailureCanBeClaimedAgain() throws SQLException {
    deliveries.claim("token", OCT_1, "event", TEN_AM, LEASE_END);
    deliveries.markRetryable("token", OCT_1, "UNAVAILABLE");

    assertTrue(deliveries.claim("token", OCT_1, "event", TEN_AM.plusSeconds(900), LEASE_END));
    assertEquals(2, attempts("token", OCT_1));
  }

  @Test
  void liveClaimBlocksAndExpiredClaimCanBeReclaimed() throws SQLException {
    deliveries.claim("token", OCT_1, "event", TEN_AM, LEASE_END);

    assertFalse(deliveries.claim("token", OCT_1, "event", LEASE_END.minusSeconds(1), LEASE_END));
    assertFalse(deliveries.claim("token", OCT_1, "event", LEASE_END, LEASE_END.plusSeconds(900)));
    assertTrue(
        deliveries.claim("token", OCT_1, "event", LEASE_END.plusSeconds(1), LEASE_END.plusSeconds(901)));
    assertEquals("claimed", status("token", OCT_1));
    assertEquals(2, attempts("token", OCT_1));
  }

  @Test
  void concurrentClaimsForTheSameDeviceAndDateHaveOneWinner() throws Exception {
    for (var round = 0; round < 20; round++) {
      var date = OCT_1.plusDays(round);
      var executor = Executors.newFixedThreadPool(8);
      try {
        var start = new CountDownLatch(1);
        var futures = new ArrayList<Future<Boolean>>();
        for (var i = 0; i < 8; i++) {
          Callable<Boolean> task =
              () -> {
                start.await();
                return deliveries.claim("token", date, "event", TEN_AM, LEASE_END);
              };
          futures.add(executor.submit(task));
        }
        start.countDown();
        var winners = 0;
        for (var future : futures) {
          winners += future.get() ? 1 : 0;
        }
        assertEquals(1, winners, "round " + round);
      } finally {
        executor.shutdownNow();
      }
    }
  }

  @Test
  void concurrentReclaimOfAnExpiredClaimHasOneWinner() throws Exception {
    deliveries.claim("token", OCT_1, "event", TEN_AM, LEASE_END);
    var later = LEASE_END.plusSeconds(60);
    var executor = Executors.newFixedThreadPool(8);
    try {
      var start = new CountDownLatch(1);
      var futures = new ArrayList<Future<Boolean>>();
      for (var i = 0; i < 8; i++) {
        futures.add(
            executor.submit(
                () -> {
                  start.await();
                  return deliveries.claim("token", OCT_1, "event", later, later.plusSeconds(900));
                }));
      }
      start.countDown();
      var winners = 0;
      for (var future : futures) {
        winners += future.get() ? 1 : 0;
      }
      assertEquals(1, winners);
      assertEquals(2, attempts("token", OCT_1));
    } finally {
      executor.shutdownNow();
    }
  }

  @Test
  void deletingTheDeviceRemovesItsDeliveries() throws SQLException {
    deliveries.claim("token", OCT_1, "event", TEN_AM, LEASE_END);

    devices.deleteByToken("token");

    assertEquals(0, count());
  }

  @Test
  void prunesOnlyDatesBeforeTheCutoff() throws SQLException {
    deliveries.claim("token", OCT_1.minusDays(31), "event", TEN_AM, LEASE_END);
    deliveries.claim("token", OCT_1, "event", TEN_AM, LEASE_END);

    assertEquals(1, deliveries.deleteBefore(OCT_1.minusDays(30)));
    assertEquals(1, count());
  }

  private void register(String token) {
    devices.upsert(
        new DeviceRegistration(
            token, DevicePlatform.IOS, "America/Jamaica", NotificationPermissionStatus.AUTHORIZED));
  }

  private String status(String token, LocalDate date) throws SQLException {
    return queryString("status", token, date);
  }

  private int attempts(String token, LocalDate date) throws SQLException {
    return Integer.parseInt(queryString("attempt_count", token, date));
  }

  private String queryString(String column, String token, LocalDate date) throws SQLException {
    try (var connection = dataSource.getConnection();
        var statement =
            connection.prepareStatement(
                "SELECT "
                    + column
                    + "::text FROM notification_delivery WHERE token = ? AND local_date = ?")) {
      statement.setString(1, token);
      statement.setObject(2, date);
      try (var resultSet = statement.executeQuery()) {
        resultSet.next();
        return resultSet.getString(1);
      }
    }
  }

  private int count() throws SQLException {
    try (var connection = dataSource.getConnection();
        var statement = connection.createStatement();
        var resultSet = statement.executeQuery("SELECT COUNT(*) FROM notification_delivery")) {
      resultSet.next();
      return resultSet.getInt(1);
    }
  }
}
