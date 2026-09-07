# On This Day API Architecture

## Purpose

The backend exists to serve the v0.0.1 daily history experience:

1. return today's curated historical events,
2. return event details,
3. register notification-capable devices,
4. send one daily featured-event notification.

The backend should stay small until the product proves the daily habit. It is not
a general history platform, CMS, personalization engine, or search service for
v0.0.1.

## Technology Choices

- Runtime: Java 21
- Compute: AWS Lambda
- API: API Gateway HTTP API
- Infrastructure: Terraform
- Database: PostgreSQL
- Notifications: Firebase Cloud Messaging
- Content operations: curated files and import/validation tooling in this repo

## Component Boundaries

The API should be organized as a small modular monolith. Prefer clear package
boundaries over separate services.

Suggested backend modules:

- `content`: historical events, daily featured event selection, source and image
  metadata, content validation rules.
- `notifications`: device registrations, daily notification selection, Firebase
  Cloud Messaging integration, notification send logging.
- `platform`: Lambda adapters, HTTP request/response handling, configuration,
  database connections, logging, error mapping.
- `ingestion`: command-line or build-time tooling for loading curated content
  into PostgreSQL.

The domain/service code should not depend directly on API Gateway event shapes.
Lambda handlers should adapt HTTP requests into service calls and adapt service
results into HTTP responses.

## Lambda Functions

v0.0.1 should use a small set of Lambda entry points.

### Get Today Content

`GET /v1/days/today?timezone=Area/Location`

Returns the current day's content:

- resolved calendar date,
- exactly one featured event,
- zero or more curated additional events.

The client sends a timezone so the API can resolve the user's local calendar
date consistently. The mobile app still owns the user experience for date
rollover and refresh.

### Get Event

`GET /v1/events/{eventId}`

Returns the full event detail for a selectable event:

- title,
- historical date and year,
- short summary,
- concise description,
- source links,
- optional image metadata.

### Register Device

`POST /v1/devices`

Registers or updates an FCM token for daily notifications.

Expected data:

- FCM token,
- platform,
- timezone,
- notification permission status when available.

No user account is required. A device registration is not a user profile.

### Delete Device

`DELETE /v1/devices/{token}`

Removes or disables a device token when notification permission is revoked, the
token is replaced, or FCM reports that it is no longer valid.

### Send Daily Notifications

Triggered by EventBridge, not by a public HTTP route.

Responsibilities:

- identify active, notification-enabled devices,
- resolve the featured event for the notification date,
- send notification title/body through FCM,
- include the destination `eventId` in the notification payload,
- record send status.

## API Boundary

The public v0.0.1 API surface should remain intentionally narrow:

```text
GET    /v1/days/today
GET    /v1/events/{eventId}
POST   /v1/devices
DELETE /v1/devices/{token}
GET    /v1/health
```

Do not expose arbitrary date browsing, global search, categories, timelines, or
event mutation APIs in v0.0.1.

## Persistence Model

PostgreSQL is the canonical runtime store for curated content and device
registrations.

Core tables:

- `historical_event`: stable event identity and canonical event content.
- `daily_event`: curated month/day membership, role, and display order.
- `event_source`: one or more source/read-more links per event.
- `event_image`: optional image metadata, attribution, and licensing data.
- `device_registration`: FCM tokens and delivery metadata.

Optional later table:

- `notification_send_log`: notification delivery attempts and de-duplication
  when retry behavior, hourly schedules, or per-timezone delivery make duplicate
  protection necessary.

Every selectable event must have:

- stable event ID,
- title,
- historical date/year,
- short summary,
- concise description,
- at least one source.

Every featured event must additionally have:

- notification title,
- notification body.

Images are optional. Missing images must not block event inclusion.

## Database Access Strategy

Use straightforward SQL access for v0.0.1.

Recommended approach:

- Flyway for schema migrations.
- JDBC with small, explicit repository classes.
- Hand-written SQL for the small number of read/write paths.
- Separate database users for runtime access and migration/admin work where
  practical.

Avoid introducing Hibernate/JPA unless the codebase grows enough to justify it.
The data model is mostly curated content reads plus simple device registration
writes, so direct SQL keeps behavior easy to inspect.

When running Lambda against PostgreSQL:

- keep connection timeouts short,
- keep per-container pool size low if pooling is used,
- set conservative Lambda concurrency,
- add RDS Proxy only if connection churn becomes a real problem.

To minimize idle cost and networking complexity for v0.0.1, prefer a managed or
serverless PostgreSQL provider reachable over TLS from Lambda without placing
the functions in a VPC. If an AWS RDS instance is chosen instead, accept the
additional baseline cost and networking complexity as a conscious tradeoff.

## Notification Architecture

Firebase Cloud Messaging is the delivery provider.

Notification flow:

1. The mobile app asks the operating system for notification permission.
2. The app receives an FCM token.
3. The app registers the token with this API.
4. EventBridge triggers the notification Lambda on a schedule.
5. The Lambda selects active, notification-enabled devices.
6. The Lambda looks up the featured event for the notification date.
7. The Lambda sends curiosity-driven notification copy through FCM.
8. The notification payload includes the destination `eventId`.
9. The mobile app opens the matching event detail screen.

The required v0.0.1 delivery model is one consistent daily send at a configured
global time. This matches the product fallback that allows a general consistent
delivery time when local-time delivery would slow the first release. Store
device timezone from the start so per-device local-time delivery can be added
without changing the mobile registration contract.

Per-device local-time delivery is a later enhancement unless it becomes cheap
enough to implement without delaying v0.0.1. If the scheduler changes to hourly
or per-timezone sends, add persistent duplicate protection for the same device
and local date.

The current local foundation exposes notification delivery only through a
framework-free manual command. It selects enabled registrations with
`authorized` or `provisional` permission, resolves featured content by the
registered timezone, and sends through a `NotificationSender` interface backed
by the FCM HTTP v1 API. The command is dry-run by default and requires an
explicit `--send` plus external Google credentials before it contacts Firebase.
No service-account JSON or private key belongs in this repository.

FCM outcomes distinguish permanently invalid tokens from transient and
configuration failures. A permanent token failure may remove the registration;
transient failures never do. EventBridge scheduling and persistent duplicate
send protection remain required before this becomes an automated production
job.

## Content Ingestion and Curation

Featured-event selection is editorial for v0.0.1. The backend should serve
curated content; it should not rank events or generate event explanations at
runtime.

Recommended content workflow:

```text
Curated source files
-> validation tool
-> database import
-> API runtime reads from PostgreSQL
```

The curated source files should live in this repo so content changes can be
reviewed with the same discipline as code changes.

Validation should fail when:

- an event ID is missing or duplicated,
- required event fields are missing,
- an event has no source,
- a supported day has more than one featured event,
- a featured event lacks notification copy,
- image metadata is incomplete for an image that is present.

Content may be prepared using external tools, including AI-assisted drafting,
but generated text must be reviewed before it becomes canonical product
content. Runtime AI content generation is explicitly outside v0.0.1.

Do not build an admin CMS for v0.0.1.

## Configuration and Secrets

Terraform manages infrastructure and secret references, not plaintext secrets.

Use environment variables for non-secret configuration:

- deployment stage,
- log level,
- database secret name,
- Firebase credential secret name,
- notification target hour or schedule settings.

Use AWS Secrets Manager or SSM Parameter Store for sensitive values:

- PostgreSQL connection string or credentials,
- Firebase service-account credentials,
- third-party provider credentials if any are added later.

Never commit database credentials, Firebase service-account JSON, signing keys,
or production secrets.

## Local Development

Local backend development should not require full AWS emulation.

Recommended setup:

- run PostgreSQL locally, likely with Docker Compose,
- apply Flyway migrations,
- load sample curated content,
- run Java service and repository tests directly,
- test Lambda handlers through thin adapter tests,
- use deployed AWS resources only for end-to-end notification verification.

The most important local feedback loop is:

```text
test service logic
test repository SQL against local PostgreSQL
validate curated content
```

## Deployment

Terraform in this repo owns:

- API Gateway HTTP API,
- Lambda functions,
- IAM roles and permissions,
- EventBridge schedules,
- CloudWatch log groups and alarms,
- configuration and secret references.

Deployment flow:

1. Run tests.
2. Build Java Lambda artifacts.
3. Apply database migrations.
4. Import or validate curated content.
5. Run Terraform plan/apply.
6. Smoke test health and today's content endpoints.
7. Verify notification sending in a controlled environment before production
   release.

At minimum, support local development and production. A separate development AWS
environment is useful but not mandatory before v0.0.1.

## Observability

Use simple, useful observability before adding a larger telemetry stack.

Required:

- structured Lambda logs,
- request IDs in logs,
- API Gateway 4xx/5xx metrics,
- Lambda error and duration metrics,
- notification send counts and failure reasons,
- alarms for API 5xx spikes and failed notification jobs.

Optional later:

- distributed tracing,
- dashboards,
- product analytics,
- event streams.

## Explicitly Deferred

The backend should not implement these for v0.0.1:

- user accounts or authentication,
- personalization,
- arbitrary date browsing,
- search,
- category or timeline APIs,
- public event mutation APIs,
- admin CMS,
- automated event significance ranking,
- runtime AI summaries,
- queues or event streaming unless notification delivery requires them,
- GraphQL,
- Kubernetes or container orchestration,
- multi-region deployment,
- RDS Proxy unless connection pressure justifies it.

The v0.0.1 backend is complete when it reliably supports:

```text
today's curated content
event details
device registration
daily featured-event notifications
```

## Quiz v0.1.0 Architecture

Quiz is a new module within the existing modular monolith. It does not change
the v0.0.1 content or notification package responsibilities.

### Package boundaries

```text
src/main/java/com/onthisday/
  quiz/                  quiz domain, selection services, and repositories
  ingestion/quiz/        curated quiz JSON reading, validation, and import
  platform/quiz/         quiz HTTP handlers and API DTOs
```

The same dependency rules continue to apply:

- `com.onthisday.quiz` has no API Gateway dependencies;
- platform DTOs do not enter the quiz domain;
- JDBC implementations stay behind quiz repository interfaces;
- handlers perform request parsing, response mapping, and public error mapping;
- no framework or second deployable service is introduced.

### Quiz persistence responsibilities

The quiz schema should represent:

- questions with stable IDs, type, prompt, difficulty, explanation, publication
  state, and type-specific answer data;
- options for multiple-choice, true/false, and image-identification questions;
- ordering items for chronological-ordering questions;
- one or more credible sources per question;
- optional image metadata, required for published image-identification
  questions;
- flat collections with one constrained presentation grouping;
- many-to-many question membership in collections;
- one immutable 20-question Daily Challenge assignment per calendar date;
- the stable order of questions within each daily assignment.

Suggested table ownership remains within the quiz module. Exact names and
constraints are decided in Quiz Task Q2, but the database must enforce stable
identities, valid type-specific data, unique collection membership, and one
daily assignment per date.

### Daily Challenge generation

The Daily Challenge request flow is:

```text
IANA timezone
-> resolve local calendar date
-> read persisted assignment for date
-> if absent, select one ordered 20-question set from published questions
-> atomically persist or recover the concurrently persisted assignment
-> return the requested stable prefix
```

Generation has these invariants:

- the same calendar date maps to the same assignment worldwide;
- timezone affects date resolution only;
- an assignment contains 20 distinct published questions;
- generation is deterministic for a date and the eligible question-bank state;
- a database uniqueness constraint prevents multiple assignments for one date;
- concurrent creators either persist the same assignment or one creator wins
  and the others reread that persisted assignment;
- after persistence, imports and publication changes never rewrite the
  assignment;
- requests for 5 and 10 questions return positions 1-5 and 1-10 respectively.

Daily selection should balance difficulty and question type where the published
bank permits it. Exact selection rules must be deterministic and covered by
tests before the endpoint is implemented.

### Quick Play generation

Quick Play selects 5, 10, or 20 distinct published questions at request time.
An omitted `collectionId` selects from the Mixed bank; a supplied collection ID
limits candidates to that collection.

The catalog computes supported counts from currently published, eligible
questions. A request for more questions than a selection supports fails with
`400 insufficient_quiz_questions`; it must not silently return a smaller quiz.

Random selection should still make a reasonable effort to balance question type
and difficulty. It does not need to be reproducible or persisted.

### Question and answer delivery

The backend returns presentation data and correct answers together:

- choice-based questions return display-ordered options and
  `correctOptionId`;
- chronological questions return shuffled items and
  `correctOrderItemIds`;
- every question returns its explanation, sources, difficulty, and applicable
  timer metadata;
- image-identification questions return full image provenance metadata.

This is an intentional trusted-client design. Mobile grades locally and shows
immediate feedback. There is no answer-submission, grading, score-history, or
attempt endpoint.

### Timing ownership

The backend publishes timing metadata while the mobile client runs the timers:

- Daily Challenge uses a total duration of 120, 240, or 480 seconds for 5, 10,
  or 20 questions;
- Quick Play uses per-question defaults of 20 seconds for multiple choice and
  true/false, 30 seconds for image identification, and 45 seconds for
  chronological ordering;
- mobile may disable Quick Play timing;
- the backend does not receive timeout or completion events.

### Quiz ingestion

Curated quiz files should live under:

```text
content/quizzes/
```

Ingestion must validate all files before a transactional, idempotent import.
Validation should cover stable IDs, supported enums, type-specific answer
shapes, unique options/items, correct-answer references, required explanations
and sources, collection references, publication requirements, and complete
image provenance.

The first content milestone is 60 reviewed questions. Six subsequent batches of
30 expand the bank to 240. Runtime fetching, scraping, and AI generation are not
part of the serving path.

### Quiz testing boundaries

Unit tests should cover validators, response mapping, timer metadata, balanced
selection, deterministic generation, stable prefixes, and concurrent-generation
outcomes at the service/repository boundary.

Testcontainers integration tests should cover schema constraints, imports,
catalog queries, published-question filtering, assignment immutability, and the
database uniqueness behavior used by concurrent first requests.

Ordinary `mvn test` remains Docker-free. Quiz repository integration tests run
through the existing integration-test profile.

### Quiz-specific exclusions

Quiz v0.1.0 does not add:

- accounts or authentication;
- backend attempts, answer submission, grading, score history, or leaderboards;
- a CMS, mutation API, or user-created content;
- runtime AI;
- a service split or framework;
- quiz-specific deployment infrastructure.
