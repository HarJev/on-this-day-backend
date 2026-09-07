# On This Day Backend

Backend for the On This Day v0.0.1 mobile app.

The backend serves the first-release daily history loop:

```text
Today -> Featured event -> Learn -> Read more -> Come back tomorrow
```

It is intentionally not a general history platform, CMS, search service, or
personalization engine. The first release is designed to answer one question:
can users build a small daily habit around one curated historical event?

## Current Status

Implemented:

- Maven Java 21 project.
- Plain AWS Lambda/API Gateway HTTP API edge.
- PostgreSQL schema managed by Flyway.
- JDBC content repositories.
- Curated JSON validation and import tooling.
- Initial August 22 curated content.
- Runtime composition for Postgres-backed API handlers.
- Device registration API for captured FCM tokens.
- Dry-run-first manual Firebase notification sender.
- API handler support for:

```text
GET /v1/health
GET /v1/days/today?timezone=Area/Location
GET /v1/events/{eventId}
POST /v1/devices
DELETE /v1/devices/{token}
```

Not implemented yet:

- Deployed API Gateway.
- Scheduled Firebase notification delivery.
- Terraform/deployment infrastructure.
- Complete 366-day content set.
- Quiz v0.1.0 schema, ingestion, services, APIs, and curated question bank. Its
  reviewed contract and implementation sequence are documented only.

## Architecture Overview

This is a small modular monolith. Package boundaries are preferred over
splitting into services too early.

```text
src/main/java/com/onthisday/
  content/       domain records, date resolution, JDBC read repositories
  ingestion/     curated JSON DTOs, validation, import command
  notifications/ device registration and notification delivery domain
  platform/      HTTP/Lambda/runtime adapters and FCM HTTP v1 adapter
```

The main rule: domain and service code should not know about API Gateway event
objects. AWS-specific request/response shapes stay at the `platform.lambda`
edge.

Runtime flow:

```text
API Gateway HTTP API event
-> ApiGatewayHttpHandler
-> ApiGatewayHttpRequestAdapter
-> HttpRouter
-> endpoint handler
-> content service/repository
-> PostgreSQL
-> response DTO
-> ApiGatewayHttpResponseAdapter
```

Content flow:

```text
content/events.json
content/daily-events.json
-> CuratedContentValidator
-> CuratedContentImporter
-> PostgreSQL
-> JDBC repositories
-> API responses
```

## Technology Choices

- Java 21
- Maven
- AWS Lambda handler interfaces
- API Gateway HTTP API v2 event model
- PostgreSQL
- Flyway
- JDBC
- Docker Compose for local PostgreSQL
- Testcontainers for integration tests
- Jackson for JSON
- Google Application Default Credentials for explicit Firebase sends
- JUnit 5

Deliberately not used:

- Spring
- Gradle
- JPA/Hibernate
- Lombok
- MapStruct
- DI frameworks
- Terraform, for now

## Repository Layout

```text
content/
  events.json
  daily-events.json

docs/
  API_CONTRACT.md
  ARCHITECTURE.md
  PRODUCT.md
  PRODUCT_DECISIONS.md
  SETUP.md

src/main/resources/db/migration/
  V1__create_content_tables.sql

docker-compose.yml
pom.xml
```

## Local Setup

Requirements:

- Java 21
- Maven
- Docker Desktop or another Docker runtime
- AWS SAM CLI for local HTTP API testing

Start local PostgreSQL:

```sh
docker compose up -d postgres
```

Apply migrations:

```sh
mvn flyway:migrate
```

Import curated content:

```sh
mvn exec:java \
  -Dexec.args="jdbc:postgresql://localhost:5432/on_this_day on_this_day on_this_day"
```

Run unit tests:

```sh
mvn test
```

Run unit and integration tests:

```sh
mvn verify -Pintegration
```

Reset local database:

```sh
docker compose down -v
docker compose up -d postgres
mvn flyway:migrate
mvn exec:java \
  -Dexec.args="jdbc:postgresql://localhost:5432/on_this_day on_this_day on_this_day"
```

Local database defaults:

```text
Database: on_this_day
User: on_this_day
Password: on_this_day
JDBC URL: jdbc:postgresql://localhost:5432/on_this_day
```

## Runtime Configuration

The no-argument Lambda handler constructor is the real runtime entry path. It
expects:

```text
DB_JDBC_URL
DB_USER
DB_PASSWORD
DB_CONNECT_TIMEOUT_SECONDS
DB_SOCKET_TIMEOUT_SECONDS
```

For local Docker Compose:

```text
DB_JDBC_URL=jdbc:postgresql://localhost:5432/on_this_day
DB_USER=on_this_day
DB_PASSWORD=on_this_day
DB_CONNECT_TIMEOUT_SECONDS=5
DB_SOCKET_TIMEOUT_SECONDS=10
```

`DB_CONNECT_TIMEOUT_SECONDS` defaults to `5`, and
`DB_SOCKET_TIMEOUT_SECONDS` defaults to `10`.

Secrets Manager or SSM Parameter Store integration is deferred until deployment
work begins.

## Local API Running

Local HTTP API testing uses AWS SAM local API with the same no-argument Lambda
handler used by the eventual Lambda runtime. This is local SAM only; it is not
real AWS deployment infrastructure.

Prepare the local database from the host:

```sh
docker compose up -d postgres
mvn flyway:migrate
mvn exec:java \
  -Dexec.args="jdbc:postgresql://localhost:5432/on_this_day on_this_day on_this_day"
```

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

Flyway and the import command run on the host, so they use
`jdbc:postgresql://localhost:5432/on_this_day`. The recommended SAM local path
attaches the Lambda container to the Compose network
`on-this-day-backend_default`, so `env.sam.compose-network.json` uses
`jdbc:postgresql://postgres:5432/on_this_day`. Keep `env.local.json` as a
fallback for `host.docker.internal`.

Troubleshooting:

- `GET /v1/health` can work even when Postgres is unavailable because it does
  not use the database. It proves Lambda/SAM routing, not DB connectivity.
- `GET /v1/days/today` and `GET /v1/events/{eventId}` require Postgres and
  imported curated content.
- If a content endpoint times out under SAM local, check that Docker Compose
  Postgres is running and that SAM was started with
  `--docker-network on-this-day-backend_default`.
- Use `localhost` for host-side Flyway/import commands, `postgres` for the SAM
  Lambda container on the Compose network, and `host.docker.internal` only for
  the fallback env file.
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

Postman or the mobile app can call:

```text
http://127.0.0.1:3000/v1/health
http://127.0.0.1:3000/v1/days/today?timezone=America/Jamaica
http://127.0.0.1:3000/v1/events/battle-of-bosworth-field-1485
```

Terminal smoke checks:

```sh
curl -i http://127.0.0.1:3000/v1/health
curl -i 'http://127.0.0.1:3000/v1/days/today?timezone=America/Jamaica'
curl -i http://127.0.0.1:3000/v1/events/battle-of-bosworth-field-1485
```

## API Summary

Health:

```text
GET /v1/health
```

Today:

```text
GET /v1/days/today?timezone=America/Jamaica
```

Event detail:

```text
GET /v1/events/battle-of-bosworth-field-1485
```

See [docs/API_CONTRACT.md](docs/API_CONTRACT.md) for response shapes and error
codes.

## Quiz v0.1.0 Planning

Quiz v0.1.0 is an approved additive expansion that has not been implemented.
The existing v0.0.1 endpoints above remain the complete runnable API.

The planned Quiz API is:

```text
GET  /v1/quizzes/catalog
POST /v1/quizzes/quick-play
GET  /v1/quizzes/daily?timezone=Area/Location&questionCount=5|10|20
```

The product and API contracts define Daily Challenge, Quick Play, four question
types, grouped flat collections, timer metadata, and local mobile grading. The
backend implementation sequence is Q1-Q9 in
[implementation_plan.md](implementation_plan.md).

Do not expect these routes to work until Q2-Q6 are implemented. No quiz setup,
migration, import, or run command exists yet.

## Content Editing

Canonical curated content lives in:

```text
content/events.json
content/daily-events.json
```

Validation rules enforce:

- stable slug-style event IDs;
- required event title/year/date/summary/description;
- at least one source per event;
- HTTPS source/image URLs;
- image provenance metadata when images are present;
- exactly one featured event per supported day;
- notification title/body for featured events.

The current content set covers August 22 and August 24 through August 29.

## Manual Notification Sender

The manual sender reads enabled device registrations whose permission is
`authorized` or `provisional`, resolves the featured event for each registered
timezone, and builds an FCM HTTP v1 message containing:

```json
{
  "message": {
    "token": "<redacted>",
    "notification": {"title": "...", "body": "..."},
    "data": {"eventId": "stable-event-id"}
  }
}
```

The command defaults to dry-run. It reports recipient and batch counts and
prints the exact send payload with the device token replaced by `<redacted>`.
Dry-run never loads Firebase credentials or contacts Firebase:

```sh
DB_JDBC_URL=jdbc:postgresql://localhost:5432/on_this_day \
DB_USER=on_this_day \
DB_PASSWORD=on_this_day \
mvn compile exec:java \
  -Dexec.mainClass=com.onthisday.platform.notifications.cli.ManualNotificationSenderCommand \
  -Dexec.args="--instant 2026-08-24T12:00:00Z"
```

The optional `--instant` override is for deterministic local checks against the
currently curated dates. Omit it to resolve the actual current date in each
registration's IANA timezone.

Sending requires the explicit `--send` flag, a Firebase project ID, and either
Application Default Credentials or an explicit credential file outside this
repository. This command contacts Firebase, so do not run it as part of normal
tests:

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

Alternatively, pass `--credentials /absolute/private/path/service-account.json`.
Never place a service-account JSON file or private key in this repository.
Permanent token failures reported by FCM remove that registration; transient
and configuration failures do not. EventBridge scheduling remains deferred.

## Testing Notes

`mvn test` must stay Docker-free.

`mvn verify -Pintegration` uses Testcontainers and requires Docker. It verifies
Flyway migrations, schema constraints, JDBC repositories, and curated content
import behavior against PostgreSQL.

## Development Guardrails

Preserve the implemented v0.0.1 daily-history behavior while adding only the
explicitly documented Quiz v0.1.0 expansion:

- no accounts or authentication;
- no arbitrary date browsing;
- no search;
- no CMS/admin UI;
- no runtime AI generation;
- no GraphQL;
- no Kubernetes or service split;
- no notification personalization.

Quiz v0.1.0 additionally excludes backend answer submission, attempt/score
history, leaderboards, runtime AI question generation, and mutation/admin APIs.

The next backend phases are EventBridge-triggered scheduling and deployment
infrastructure. Neither is part of the local manual sender.
