# Backend Setup and Bootstrap Notes

## Current State

This repository has a Maven Java bootstrap, platform HTTP/Lambda edge,
PostgreSQL/Flyway content schema, JDBC content repositories, curated JSON
validation/import tooling, and the initial content APIs:

```text
GET /v1/health
GET /v1/days/today?timezone=Area/Location
GET /v1/events/{eventId}
```

It does not yet have device APIs, notification delivery, Terraform/deployment
infrastructure, or API Gateway deployment wiring.

## Intended Stack

- Java 21
- Maven
- AWS Lambda
- API Gateway HTTP API
- PostgreSQL
- Flyway
- JDBC
- Firebase Cloud Messaging
- Docker Compose for local PostgreSQL

## Local Bootstrap Target

The first useful backend bootstrap should create a local feedback loop:

```text
run tests
start local PostgreSQL
apply migrations
load sample curated content
exercise Lambda handler behavior through tests or deployed AWS runtime
```

Full AWS emulation is not required for local development. Deployment
infrastructure is a later phase after the local backend/API loop works.

## Proposed Project Shape

Use Maven for the Java project:

```text
pom.xml
src/main/java/com/onthisday/
  content/
  notifications/
  platform/
  ingestion/
src/test/java/com/onthisday/
src/main/resources/db/migration/
content/
docker-compose.yml
```

Package intent:

- `content`: event/day models, content services, SQL repositories, validation.
- `notifications`: device registration, FCM integration, notification jobs.
- `platform`: Lambda handlers, HTTP mapping, config, logging, error responses.
- `ingestion`: content import and validation command-line tooling.

## Bootstrap Milestones

1. Java project and test framework.
2. Health handler and basic Lambda/API adapter tests.
3. Local PostgreSQL with Flyway migrations.
4. Content schema and repository tests.
5. Seed content import for at least August 22. Complete for initial content.
6. `GET /v1/days/today` and `GET /v1/events/{eventId}`. Complete.
7. Runtime composition for Postgres-backed API handlers. Complete locally.
8. Device registration schema and `POST/DELETE /v1/devices`.
9. Firebase notification service behind an interface/fake.
10. EventBridge-triggered daily notification job.

## Settled Bootstrap Decisions

- Deployment stages follow `docs/ARCHITECTURE.md`: support local development and
  production at minimum. A separate development AWS environment is useful but
  not mandatory before v0.0.1.
- Use Maven, not Gradle.
- Use plain AWS Lambda handlers with API Gateway HTTP API event adapters at the
  platform edge.
- Start with one Java project and one deployable artifact.
- Use PostgreSQL with Flyway migrations and JDBC repositories, not JPA.
- Use Docker Compose PostgreSQL for local development.
- Use Testcontainers for repository integration tests.
- Use JSON curated content files.
- Put Firebase Cloud Messaging behind an interface and use a fake implementation
  until notification delivery is wired.
- Add deployment infrastructure later, after the local backend/API loop works.

## Recommended Defaults

Given the small v0.0.1 scope and the user's Java/Spring background:

- Use Maven.
- Use plain AWS Lambda handlers with small API Gateway adapters first.
- Use one Java project and package route/job handlers from the same artifact
  initially.
- Use Flyway and JDBC, not JPA.
- Use Docker Compose PostgreSQL locally.
- Use Testcontainers for repository integration tests.
- Use JSON for curated content files.

## Local Database

Start local PostgreSQL:

```sh
docker compose up -d postgres
```

The local defaults are:

```text
Database: on_this_day
User: on_this_day
Password: on_this_day
Port: 5432
JDBC URL: jdbc:postgresql://localhost:5432/on_this_day
```

Stop local PostgreSQL:

```sh
docker compose down
```

Remove the local database volume when a full reset is needed:

```sh
docker compose down -v
```

## Migrations

Flyway migrations live in:

```text
src/main/resources/db/migration/
```

The first real migration should create the content schema. There is intentionally
no no-op baseline migration.

After migrations exist and local PostgreSQL is running, apply them with:

```sh
mvn flyway:migrate
```

The Maven defaults target the Docker Compose database. Override
`flyway.url`, `flyway.user`, or `flyway.password` with `-D...` properties when
needed.

## Curated Content Import

Curated content files live in:

```text
content/events.json
content/daily-events.json
```

After local PostgreSQL is running and migrations have been applied, import the
curated content with:

```sh
mvn exec:java \
  -Dexec.args="jdbc:postgresql://localhost:5432/on_this_day on_this_day on_this_day"
```

To import from another content directory containing `events.json` and
`daily-events.json`, pass the directory as the fourth argument:

```sh
mvn exec:java \
  -Dexec.args="jdbc:postgresql://localhost:5432/on_this_day on_this_day on_this_day /path/to/content"
```

The importer validates content before writing, treats warnings as non-fatal,
and imports transactionally and idempotently.

## Runtime Configuration

The no-argument Lambda handler constructor is the real runtime entry path. It
wires the API through PostgreSQL-backed repositories using these environment
variables:

```text
DB_JDBC_URL=jdbc:postgresql://localhost:5432/on_this_day
DB_USER=on_this_day
DB_PASSWORD=on_this_day
```

For local Docker Compose, the values above match the default database. Production
or hosted environments should provide their own values through the deployment
configuration. Secrets Manager/SSM integration is a later deployment phase.

## Local API Data Loop

A complete local data loop is:

```sh
docker compose up -d postgres
mvn flyway:migrate
mvn exec:java \
  -Dexec.args="jdbc:postgresql://localhost:5432/on_this_day on_this_day on_this_day"
mvn test
```

This repository does not currently include a local HTTP server. Handler and
routing behavior is exercised through unit and integration tests until deployment
infrastructure is added.

## Tests

Run unit tests:

```sh
mvn test
```

`mvn test` must not require Docker or a live PostgreSQL database.

Run integration tests:

```sh
mvn verify -Pintegration
```

Integration tests use the Maven Failsafe plugin and should be named `*IT.java`.
Repository integration tests should use Testcontainers PostgreSQL so they do not
depend on the Docker Compose database being started manually.

## Verification Expectations

Once code exists, normal verification should include:

```text
unit tests
repository tests against PostgreSQL
content validation
Java formatting/checks
```

Do not require live AWS or Firebase for ordinary local test runs.
