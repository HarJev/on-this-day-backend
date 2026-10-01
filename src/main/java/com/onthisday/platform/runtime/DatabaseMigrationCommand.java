package com.onthisday.platform.runtime;

import org.flywaydb.core.Flyway;

/**
 * Runs the Flyway migrations in {@code db/migration} with credentials from {@link
 * CommandDatabaseConfig}, so production runs never put the password on the command line.
 *
 * <p>Usage: {@code DatabaseMigrationCommand [migrate|info|validate]} (default {@code migrate}).
 */
public final class DatabaseMigrationCommand {

  private DatabaseMigrationCommand() {}

  public static void main(String[] args) {
    var action = args.length == 0 ? "migrate" : args[0];
    if (args.length > 1 || !(action.equals("migrate") || action.equals("info") || action.equals("validate"))) {
      System.err.println("Usage: DatabaseMigrationCommand [migrate|info|validate]");
      System.exit(2);
    }

    var dataSource = new PostgresDataSourceFactory().create(CommandDatabaseConfig.fromEnvironment());
    var flyway = Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").load();
    switch (action) {
      case "info" -> {
        var info = flyway.info();
        var current = info.current();
        System.out.printf(
            "current=%s pending=%d%n",
            current == null ? "none" : current.getVersion(), info.pending().length);
      }
      case "validate" -> {
        flyway.validate();
        System.out.println("Migrations are valid.");
      }
      default -> {
        var result = flyway.migrate();
        System.out.printf(
            "migrationsExecuted=%d targetVersion=%s%n", result.migrationsExecuted, result.targetSchemaVersion);
      }
    }
  }
}
