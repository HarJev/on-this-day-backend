# Backend Setup and Bootstrap Notes

## Current State

This repository has a Maven Java bootstrap, platform HTTP/Lambda edge,
PostgreSQL/Flyway schemas, JDBC repositories, curated JSON validation/import
tooling, content/device APIs, local AWS SAM support, and a dry-run-first manual
notification sender:

```text
GET /v1/health
GET /v1/days/today?timezone=Area/Location
GET /v1/events/{eventId}
POST /v1/devices
DELETE /v1/devices/{token}
GET /v1/quizzes/catalog
POST /v1/quizzes/quick-play
GET /v1/quizzes/daily?timezone=Area/Location&questionCount=5|10|20
```

It does not yet have scheduled notification delivery, Terraform/deployment
infrastructure, or deployed API Gateway wiring.

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
8. Device registration schema and `POST/DELETE /v1/devices`. Complete.
9. Manual Firebase notification sender behind an interface. Complete locally;
   production credentials and a real send remain operator-controlled.
10. EventBridge-triggered daily notification job. Deferred.

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
DB_CONNECT_TIMEOUT_SECONDS=5
DB_SOCKET_TIMEOUT_SECONDS=10
```

For local Docker Compose, the values above match the default database. Production
or hosted environments should provide their own values through the deployment
configuration. Secrets Manager/SSM integration is a later deployment phase.

`DB_CONNECT_TIMEOUT_SECONDS` defaults to `5`, and
`DB_SOCKET_TIMEOUT_SECONDS` defaults to `10`.

## Local SAM API

Use AWS SAM local API when Postman or the mobile app needs to call the backend
over HTTP before real deployment.

This is local SAM only. It does not add deployed API Gateway resources,
Terraform, or other AWS deployment infrastructure.

First prepare the local database from the host:

```sh
docker compose up -d postgres
mvn flyway:migrate
mvn exec:java \
  -Dexec.args="jdbc:postgresql://localhost:5432/on_this_day on_this_day on_this_day"
```

Flyway and the content import command run on the host, so they use:

```text
jdbc:postgresql://localhost:5432/on_this_day
```

The SAM Lambda runtime runs inside a Docker container. Inside that container,
`localhost` means the Lambda container itself, not the host machine. The
recommended SAM local path attaches the Lambda container to the Compose network
`on-this-day-backend_default`, so `env.sam.compose-network.json` uses:

```text
jdbc:postgresql://postgres:5432/on_this_day
```

Keep `env.local.json` as a fallback for `host.docker.internal`.

Package and start the local API on the Docker Compose network:

```sh
mvn package
sam build
sam local start-api \
  --env-vars ./env.sam.compose-network.json \
  --warm-containers EAGER \
  --docker-network on-this-day-backend_default
```

`--warm-containers EAGER` preloads and reuses the Lambda container, which makes
the Postman/mobile loop much closer to warm Lambda behavior. Plain
`sam local start-api` creates fresh containers by default and can be slow enough
to obscure backend issues during demos.

## Quiz Local Expectation

Quiz HTTP routes use the same local SAM/API Gateway path as the existing
endpoints. The canonical `content/quizzes/` directory intentionally contains no
published questions until Q7. After importing that empty catalog,
`GET /v1/quizzes/catalog` returns zero published counts, while Quick Play and
Daily Challenge correctly return `400 insufficient_quiz_questions`. Do not add
placeholder runtime quiz content merely to make these routes return a quiz.

For verbose SAM runtime diagnostics, start with:

```sh
sam local start-api \
  --env-vars ./env.sam.compose-network.json \
  --warm-containers EAGER \
  --docker-network on-this-day-backend_default \
  --debug
```

To smoke-test the Lambda handler directly without the local HTTP server, run:

```sh
sam local invoke OnThisDayApiFunction \
  --env-vars ./env.sam.compose-network.json \
  --docker-network on-this-day-backend_default \
  --event src/test/resources/sam/health-event.json

sam local invoke OnThisDayApiFunction \
  --env-vars ./env.sam.compose-network.json \
  --docker-network on-this-day-backend_default \
  --event src/test/resources/sam/today-event.json
```

The local API exposes:

```text
http://127.0.0.1:3000/v1/health
http://127.0.0.1:3000/v1/days/today?timezone=America/Jamaica
http://127.0.0.1:3000/v1/events/battle-of-bosworth-field-1485
http://127.0.0.1:3000/v1/devices
http://127.0.0.1:3000/v1/devices/<url-encoded-fcm-token>
```

Terminal smoke checks:

```sh
curl -i http://127.0.0.1:3000/v1/health
curl -i 'http://127.0.0.1:3000/v1/days/today?timezone=America/Jamaica'
curl -i http://127.0.0.1:3000/v1/events/battle-of-bosworth-field-1485
curl -i -X POST http://127.0.0.1:3000/v1/devices \
  -H 'content-type: application/json' \
  -d '{
    "token": "local-fcm-token",
    "platform": "ios",
    "timezone": "America/Jamaica",
    "notificationPermissionStatus": "authorized"
  }'
curl -i -X DELETE http://127.0.0.1:3000/v1/devices/local-fcm-token
```

Troubleshooting:

- Health can work even if the database is unreachable because it does not open a
  Postgres connection. It proves Lambda/SAM routing, not DB connectivity.
- Today and event detail endpoints require Postgres plus imported curated
  content.
- A timeout from a content endpoint usually means Postgres is not running or SAM
  was not started with `--docker-network on-this-day-backend_default`.
- Host-side Flyway/import commands use `localhost`; the SAM Lambda container
  uses `postgres` on the Compose network or `host.docker.internal` with the
  fallback env file.
- To test database reachability from a container, run:

```sh
docker run --rm postgres:16-alpine \
  pg_isready -h host.docker.internal -p 5432 -U on_this_day -d on_this_day
```

When testing from the Compose network, use:

```sh
docker run --rm --network on-this-day-backend_default postgres:16-alpine \
  pg_isready -h postgres -p 5432 -U on_this_day -d on_this_day
```

- Runtime database timeout env vars are `DB_CONNECT_TIMEOUT_SECONDS` and
  `DB_SOCKET_TIMEOUT_SECONDS`.

## Local API Data Loop

A complete local data loop is:

```sh
docker compose up -d postgres
mvn flyway:migrate
mvn exec:java \
  -Dexec.args="jdbc:postgresql://localhost:5432/on_this_day on_this_day on_this_day"
mvn test
```

For HTTP testing, continue with `mvn package`, `sam build`, and the
`sam local start-api` command with warm containers and the Compose Docker
network.

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

## Manual Notification Dry Run

The notification command is dry-run unless `--send` is present. Start and seed
PostgreSQL first, then register a fake authorized/provisional token through the
local API or SQL. Run against a curated August date with:

```sh
DB_JDBC_URL=jdbc:postgresql://localhost:5432/on_this_day \
DB_USER=on_this_day \
DB_PASSWORD=on_this_day \
mvn compile exec:java \
  -Dexec.mainClass=com.onthisday.platform.notifications.cli.ManualNotificationSenderCommand \
  -Dexec.args="--instant 2026-08-24T12:00:00Z"
```

Dry-run loads eligible registrations, resolves featured content using each
registration's timezone, and prints recipient counts plus FCM payloads with
tokens replaced by `<redacted>`. It does not load credentials or contact
Firebase.

An actual local/dev send is intentionally gated behind all of the following:

- explicit `--send`;
- `--project-id <id>` or `FIREBASE_PROJECT_ID`;
- Application Default Credentials, normally through
  `GOOGLE_APPLICATION_CREDENTIALS`, or `--credentials <absolute-path>`.

Example shape, to be run only with an authorized credential kept outside the
repository:

```sh
DB_JDBC_URL=jdbc:postgresql://localhost:5432/on_this_day \
DB_USER=on_this_day \
DB_PASSWORD=on_this_day \
FIREBASE_PROJECT_ID=on-this-day-98e6b \
GOOGLE_APPLICATION_CREDENTIALS=/absolute/private/path/service-account.json \
mvn compile exec:java \
  -Dexec.mainClass=com.onthisday.platform.notifications.cli.ManualNotificationSenderCommand \
  -Dexec.args="--send --instant 2026-08-24T12:00:00Z"
```

Missing send credentials fail with `ConfigurationException`. FCM permanent
token failures are separated from transient/configuration failures; only a
permanently invalid token is removed. No EventBridge schedule or AWS deployment
is created by this command.

## Quiz v0.1.0 Local Content Tooling

Quiz v0.1.0 currently has schema, domain, validation, and import tooling. The
canonical question bank remains empty until Q7, and no quiz HTTP routes are
locally callable yet.

The curated quiz-content root is:

```text
content/quizzes/
```

It contains `collections.json` and may contain a `questions/` directory with
regular `*.json` question packs. A missing or empty `questions/` directory is
valid while the canonical bank is empty; validation reports a warning that no
published quiz questions exist.

After starting Postgres and running Flyway migrations, import quiz content with:

```bash
mvn exec:java \
  -Dexec.mainClass=com.onthisday.ingestion.quiz.QuizContentImportCommand \
  -Dexec.args="jdbc:postgresql://localhost:5432/on_this_day on_this_day on_this_day"
```

To import from another directory:

```bash
mvn exec:java \
  -Dexec.mainClass=com.onthisday.ingestion.quiz.QuizContentImportCommand \
  -Dexec.args="jdbc:postgresql://localhost:5432/on_this_day on_this_day on_this_day /path/to/content/quizzes"
```

The historical-event importer remains:

```bash
mvn exec:java \
  -Dexec.args="jdbc:postgresql://localhost:5432/on_this_day on_this_day on_this_day"
```

Warnings are printed and do not fail the import. Validation errors, malformed
JSON, unknown JSON properties, missing migrations, or database constraint
failures fail the command. The database password is accepted only as a command
argument and is not printed by the command.

Planned quiz endpoints are documented in `docs/API_CONTRACT.md`; they must not
be treated as locally callable until their implementation tasks are complete.
