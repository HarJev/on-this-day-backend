Using backend folder: `on-this-day-backend`

Path: `/Users/jevaunharris/Workspace/on-this-day/on-this-day-backend`

I read all requested files and updated docs/SETUP.md so the defaults are clearly Maven-based, not open-ended Gradle/tooling choices. Since the repo is currently untracked docs-only, `git diff` is empty, but `git status` shows `AGENTS.md` and `docs/` as untracked.

**Ambiguities / Notes**

- `docs/API_CONTRACT.md` lists `GET /v1/days/today` in one API-surface block, but the detailed endpoint and your scope require `GET /v1/days/today?timezone=Area/Location`. I’d implement timezone as required.
- FCM is required for v0.0.1, but should stay behind an interface/fake until the notification task.
- Terraform local state is now a settled default.

**Backend Implementation Plan**

**Task 1: Maven Java 21 Bootstrap**

- Goal: Create one Java/Maven project artifact.
- Files/components: `pom.xml`, `src/main/java/com/onthisday/...`, `src/test/java/...`.
- Acceptance: Java 21 configured; JUnit tests run with `mvn test`; no Spring/Gradle.
- Dependencies: none.
- Out of scope: API endpoints, database, Terraform.

**Task 2: Platform HTTP/Lambda Edge**

- Goal: Add plain Lambda handler structure and API Gateway HTTP API adapter types.
- Files/components: `platform/handler`, request/response adapters, JSON mapper.
- Acceptance: handlers receive API Gateway events but service code receives ordinary request objects; adapter tests cover path/query/body mapping.
- Dependencies: Task 1.
- Out of scope: real content/device behavior.

**Task 3: Health Endpoint**

- Goal: Implement `GET /v1/health`.
- Files/components: health handler/service, routing dispatch.
- Acceptance: returns `200 {"status":"ok"}`; unsupported methods/routes return contract-shaped errors.
- Dependencies: Task 2.
- Out of scope: DB health unless explicitly added later.

**Task 4: Local Postgres And Flyway**

- Goal: Establish local persistence loop.
- Files/components: `docker-compose.yml`, Flyway config, `src/main/resources/db/migration/`.
- Acceptance: Docker Compose starts Postgres; migrations apply; Maven can run migration/test flow locally.
- Dependencies: Task 1.
- Out of scope: production DB provisioning.

**Task 5: Content Schema**

- Goal: Model curated historical content in PostgreSQL.
- Files/components: Flyway migrations for `historical_event`, `daily_event`, `event_source`, `event_image`.
- Acceptance: schema supports stable IDs, one featured event per day, additional ordering, required sources, optional image metadata.
- Dependencies: Task 4.
- Out of scope: device registrations, notifications.

**Task 6: Content Domain And JDBC Repositories**

- Goal: Implement content read repositories with JDBC.
- Files/components: `content/domain`, `content/repository`.
- Acceptance: Testcontainers integration tests cover lookup by month/day, featured/additional events, event by ID, not found, image/source mapping.
- Dependencies: Tasks 4-5.
- Out of scope: HTTP handlers, ingestion.

**Task 7: JSON Curated Content And Validation**

- Goal: Add reviewable curated JSON files and validation/import tooling.
- Files/components: `content/*.json`, `ingestion/`, validation tests.
- Acceptance: validation fails for duplicate/missing IDs, missing sources, multiple featured events per day, missing notification copy, incomplete image metadata.
- Dependencies: Tasks 5-6.
- Out of scope: CMS/admin UI, runtime AI.

**Task 8: Seed/Import August 22 Content**

- Goal: Load initial curated content for local API behavior.
- Files/components: JSON content file, importer command/test fixture.
- Acceptance: local DB can contain one featured August 22 event and curated additional events; every event has at least one source.
- Dependencies: Task 7.
- Out of scope: complete 366-day content set.

**Task 9: Today Content API**

- Goal: Implement `GET /v1/days/today?timezone=Area/Location`.
- Files/components: content service, today handler, timezone validation.
- Acceptance: validates IANA timezone; resolves local month/day; returns backend-resolved `date.displayDate`, exactly one featured event, zero or more additional events; content unavailable maps to `503`.
- Dependencies: Tasks 2, 6, 8.
- Out of scope: arbitrary date browsing.

**Task 10: Event Detail API**

- Goal: Implement `GET /v1/events/{eventId}`.
- Files/components: event service/handler.
- Acceptance: returns full event detail with sources and image metadata; unknown ID returns `404 event_not_found`.
- Dependencies: Tasks 2, 6.
- Out of scope: search, related events, mutation APIs.

**Task 11: Device Registration Schema And Repository**

- Goal: Persist notification-capable devices.
- Files/components: Flyway migration for `device_registration`, JDBC repository.
- Acceptance: upsert by token; delete missing token is idempotent success; repository tests use Testcontainers.
- Dependencies: Task 4.
- Out of scope: Firebase sending.

**Task 12: Device APIs**

- Goal: Implement `POST /v1/devices` and `DELETE /v1/devices/{token}`.
- Files/components: notification/device service, handlers.
- Acceptance: validates token/platform/timezone/permission status; POST returns `{"registered":true}`; DELETE returns `{"deleted":true}`.
- Dependencies: Tasks 2, 11.
- Out of scope: accounts/auth, user profiles.

**Task 13: Notification Sender Interface And Fake**

- Goal: Prepare FCM behind an interface.
- Files/components: `notifications/NotificationSender`, fake sender, notification message model.
- Acceptance: service can compose title/body/payload without real Firebase; tests assert `eventId` payload.
- Dependencies: Tasks 6, 11.
- Out of scope: real FCM credentials/client.

**Task 14: Daily Notification Job**

- Goal: Implement EventBridge-triggered Lambda job.
- Files/components: scheduled Lambda handler, notification service, send logging if needed minimally.
- Acceptance: selects active devices, resolves featured event for configured notification date/global send, sends notification title/body with `eventId` payload through fake sender in tests.
- Dependencies: Tasks 6, 11, 13.
- Out of scope: per-device local-time scheduling, notification history UI, personalization.

**Task 15: Terraform Skeleton**

- Goal: Define deployable AWS shape with local Terraform state.
- Files/components: `infra/`, API Gateway HTTP API, Lambda functions, IAM, EventBridge, logs, secret references.
- Acceptance: `terraform fmt` and `terraform validate` pass locally; no plaintext secrets.
- Dependencies: Tasks 1-3, then expand after APIs/jobs exist.
- Out of scope: remote state, Kubernetes, multi-service split.

**Task 16: End-To-End Local Verification Docs**

- Goal: Document and verify local backend loop.
- Files/components: `docs/SETUP.md`, README if needed.
- Acceptance: commands cover `mvn test`, Docker Compose Postgres, Flyway migration, content import, handler/API tests.
- Dependencies: prior implementation tasks.
- Out of scope: live AWS/Firebase required for ordinary local runs.

---

# Quiz v0.1.0 Backend Implementation Sequence

This sequence is an additive expansion. It preserves all v0.0.1 event,
notification, and device APIs and keeps the backend as one Maven modular
monolith.

## Q1: Product, Architecture, API, and Implementation Contracts

- **Goal:** Establish the reviewed Quiz v0.1.0 product and technical contract
  before implementation.
- **Files/components:** `docs/PRODUCT.md`, `docs/PRODUCT_DECISIONS.md`,
  `docs/ARCHITECTURE.md`, `docs/API_CONTRACT.md`, `docs/SETUP.md`, `README.md`,
  and this plan.
- **Acceptance criteria:** Both modes, four question types, timers, collection
  grouping, difficulty, local grading, immutable Daily assignments, API shapes,
  content targets, and non-goals are explicit; existing v0.0.1 contracts remain
  unchanged.
- **Dependencies:** None.
- **Out of scope:** Java, SQL, migrations, dependencies, curated quiz content,
  and mobile implementation.

## Q2: Quiz Schema and Domain

- **Goal:** Model quiz questions, answers, collections, sources, images, and
  immutable Daily assignments.
- **Files/components:** a new Flyway migration; `com.onthisday.quiz` domain
  records, enums, repository contracts, and domain exceptions.
- **Acceptance criteria:** The schema supports all four question types, stable
  IDs, Easy/Medium/Hard, publication state, required sources, complete optional
  image provenance, flat grouped collections, many-to-many membership, and one
  ordered 20-question assignment per date. Constraints reject invalid
  type-specific records and duplicate membership/order. Assignment uniqueness
  provides the concurrency boundary for first generation.
- **Dependencies:** Q1.
- **Out of scope:** ingestion, selection algorithms, HTTP handlers, attempts,
  scores, and content population.

## Q3: Curated Quiz Ingestion and Validation

- **Goal:** Establish reviewable JSON formats and safe import tooling under
  `content/quizzes/`.
- **Files/components:** quiz JSON DTOs, reader, validator, validation errors and
  warnings, importer, CLI wiring, unit tests, and Testcontainers integration
  tests.
- **Acceptance criteria:** Validation covers stable IDs, enums, required text,
  type-specific options/items and answers, unique IDs, required explanations and
  sources, collection references, publication rules, and complete image
  provenance. Import validates first and is transactional and idempotent.
- **Dependencies:** Q2.
- **Out of scope:** production question content, runtime fetching, scraping,
  runtime AI, and HTTP APIs.

## Q4: Quiz Repositories and Catalog

- **Goal:** Read published quiz content efficiently and expose playable catalog
  metadata.
- **Files/components:** JDBC quiz question, collection, and catalog repository
  implementations; catalog service; repository integration tests.
- **Acceptance criteria:** Repositories map each question type without duplicate
  aggregate rows, filter unpublished or ineligible questions, load sources and
  images, and compute published counts plus supported 5/10/20 counts for Mixed
  and each collection. Collections retain their presentation grouping.
- **Dependencies:** Q2-Q3.
- **Out of scope:** quiz generation, HTTP handlers, answer submission, and
  caching infrastructure.

## Q5: Quick Play and Daily Generation

- **Goal:** Generate balanced Quick Play quizzes and stable Daily Challenge
  assignments.
- **Files/components:** selection services, deterministic Daily generator,
  random Quick Play generator, assignment repository/JDBC implementation,
  `Clock`-based date resolution, and focused unit/integration tests.
- **Acceptance criteria:** Quick Play returns exactly 5, 10, or 20 distinct
  published questions from Mixed or one collection and rejects unsupported
  counts. Daily generation creates one immutable 20-question assignment per
  calendar date, survives concurrent first requests, and returns stable 5/10
  prefixes. Selection balances question types and difficulty where the bank
  permits.
- **Dependencies:** Q4.
- **Out of scope:** HTTP transport, backend grading, attempt history,
  leaderboards, and scheduled generation.

## Q6: Quiz HTTP APIs

- **Goal:** Expose the catalog, Quick Play, and Daily Challenge through the
  existing Lambda/API Gateway edge.
- **Files/components:** `com.onthisday.platform.quiz` request/response DTOs,
  handlers, response mappers, route registration, runtime composition, SAM route
  declarations, and handler/route tests.
- **Acceptance criteria:** Implement `GET /v1/quizzes/catalog`,
  `POST /v1/quizzes/quick-play`, and `GET /v1/quizzes/daily`; validate IANA
  timezone and 5/10/20 counts; return correct answers, explanations, sources,
  difficulty, image provenance, and exact timer metadata; map documented quiz
  errors consistently. Existing routes remain unchanged.
- **Dependencies:** Q4-Q5 and the existing platform/runtime edge.
- **Out of scope:** answer submission, score storage, authentication, deployment,
  and mobile code.

## Q7: Initial 60 Reviewed Questions

- **Goal:** Make Quiz v0.1.0 usable with an editorially reviewed starter bank.
- **Files/components:** curated files under `content/quizzes/`, source and image
  review records, validator tests, importer integration tests, and local setup
  documentation.
- **Acceptance criteria:** Exactly 60 publishable questions span all four types,
  approximate target difficulty proportions, varied world-history subjects, and
  useful collections. Every question has an explanation and credible source;
  every image has verified provenance and licensing metadata. Catalog, Quick
  Play, and Daily generation work against the imported set.
- **Dependencies:** Q3-Q6.
- **Out of scope:** the remaining 180 questions, forced geographic quotas, CMS,
  and automated publication.

## Q8: Expand to 240 Reviewed Questions

- **Goal:** Reach the complete v0.1.0 content-bank target through six reviewable
  batches of 30.
- **Files/components:** six curated-content batches, review/validation fixtures,
  and aggregate content-distribution tests or reports.
- **Acceptance criteria:** The published bank totals 144 multiple-choice, 36
  true/false, 36 image-identification, and 24 chronological-ordering questions,
  with an approximate 25/55/20 Easy/Medium/Hard split. Content broadens globally,
  sensitive material is educational and respectful, sources are credible, and
  all images have verified provenance. Existing persisted Daily assignments are
  unchanged after each import.
- **Dependencies:** Q7, completed sequentially in six 30-question batches.
- **Out of scope:** complete historical coverage, quotas, runtime AI, scraping,
  and new APIs.

## Q9: Mobile Quiz Implementation Plan

- **Goal:** Produce a separately reviewable mobile plan against the stable Quiz
  v0.1.0 backend contract.
- **Files/components:** mobile planning documentation only, covering domain/DTO
  mapping, catalog, setup, play, feedback, results/review, timers, Daily local
  attempt state, and tests.
- **Acceptance criteria:** The plan uses existing mobile architecture, grades
  locally, stores official Daily history and best results locally, supports
  disabling Quick Play timing, and introduces no account or backend-attempt
  dependency.
- **Dependencies:** Q1 API contract; schedule detailed implementation after Q6
  stabilizes response DTOs.
- **Out of scope:** mobile code in the backend repository, accounts, cross-device
  sync, leaderboards, and notification changes.

---

# Post-Audit Backend Implementation Sequence

These tasks implement the approved integrated daily-learning direction. They do
not rewrite completed Q1-Q9 checkpoints or existing persisted Daily assignments.
Implement and review them separately.

## Status (checked against `main` on 2026-09-28)

Tasks 1-16 and Q1-Q9 are complete. Deployment work (Lambda, API Gateway and
database provisioning) is deferred until launch; local Docker PostgreSQL and
SAM local are the working environment until then.

| Task | Status | Evidence |
| --- | --- | --- |
| PA1 editorial status baseline | **Complete** | `ContentCoverageReportCommand` plus `ContentStatusCommand` (review counts, drafts, fingerprints, database drift, answer positions, featured images). Related-event coverage lands with PA2 |
| PA2 event-question relations | Not started | |
| PA3 date-linked Daily selection | Not started | Depends on PA2 |
| PA4 recent content API | **Complete** | `GET /v1/days/recent`, PR #12. Returns featured events for up to 14 past days and skips uncovered dates |
| PA5 scheduled notification delivery | Parked by owner | Needs a paid Apple Developer account for iOS push |
| PA6 event image coverage | In progress | Pipeline in `docs/EVENT_IMAGE_PIPELINE.md` (PR #5); Sep 27-Oct 1 images live (PR #8) |
| PA7 backend observability | Not started | |

## PA1: Editorial Quality And Content Status Baseline

- **Goal:** Make draft, approved canonical, and imported database state
  inspectable before adding new runtime relationships.
- **Files/components:** editorial validators/reporting, quiz authoring checks,
  featured-image review metadata, read-only status/fingerprint command, docs.
- **Acceptance criteria:** report draft/source-verified/approved/imported counts,
  canonical/database fingerprints, stale imports, answer-position distribution,
  related-event metadata, featured-image review outcomes, and unresolved
  distractor review. The command performs no approval, promotion, or import.
- **Out of scope:** automatic distractor rewriting, runtime draft tables, and
  production import.

## PA2: Event-Question Relationship Schema And Ingestion

- **Goal:** Persist explicit reviewed relationships between quiz questions and
  historical events.
- **Files/components:** Flyway migration, domain/repository contracts, quiz JSON
  field/DTO validation, importer, editorial preflight, coverage reporting.
- **Acceptance criteria:** many-to-many relations use stable foreign keys;
  unknown/duplicate IDs fail validation; omitted questions/events are preserved;
  imports remain transactional/idempotent; existing assignments remain
  unchanged.
- **Dependencies:** PA1 and existing quiz/event ingestion.
- **Out of scope:** relation inference, runtime AI, user tagging, and selector
  changes.

## PA3: Date-Linked Daily Selection

- **Goal:** Make Daily reinforce the current day's curated events without losing
  deterministic global balance.
- **Files/components:** Daily candidate queries, versioned generator, services,
  response mapping where link metadata is needed, unit/Testcontainers tests.
- **Acceptance criteria:** newly assigned Daily-5 includes one eligible featured
  relation when available; Daily-10/20 may include at most one additional
  same-date relation; missing supply falls back safely; questions stay distinct;
  5/10/20 prefixes, concurrent first creation, and existing assignment
  immutability remain intact.
- **Dependencies:** PA2.
- **Out of scope:** rewriting prior assignments, backend scoring, and generated
  questions.

## PA4: Seven-Day Recent Content API

- **Goal:** Return today and the previous six local calendar dates without
  introducing arbitrary archive browsing.
- **Files/components:** recent-content domain response, repository/service,
  handler/DTO/route, SAM event, API contract and tests.
- **Acceptance criteria:** validates IANA timezone; handles year/leap boundaries;
  returns available days in date order and exposes uncovered dates safely;
  one missing day does not fail the whole window.
- **Dependencies:** existing event repositories/platform edge.
- **Out of scope:** search, arbitrary ranges, complete archive UI, and past
  official quiz attempts.

## PA5: Scheduled Notification Delivery And Failure Isolation

- **Goal:** Complete the deferred production notification loop.
- **Files/components:** scheduled Lambda entry, EventBridge/SAM wiring,
  idempotency record if required, per-timezone planning, sender metrics/tests.
- **Acceptance criteria:** groups eligible devices by local date/timezone; sends
  reviewed featured copy and event ID; skips unavailable groups while continuing
  others; distinguishes permanent/transient/configuration failures; retry does
  not duplicate an already completed local-date send.
- **Dependencies:** existing device repository and FCM sender foundation.
- **Supersedes:** original Task 14's global-send simplification.
- **Out of scope:** personalized categories, multiple daily pushes, and iOS APNs
  credential ownership.

## PA6: Event Image Coverage

- **Goal:** Increase visual completeness without weakening rights review.
- **Files/components:** seven-day editorial batches, image manifests/renditions,
  coverage report, owned-origin publishing plan when approved.
- **Acceptance criteria:** every featured event records an image search and
  rights outcome; published media passes provenance, checksum, size/dimension,
  and mobile first-fetch gates; text-only decisions remain explicit; served URL
  changes preserve original source/license metadata.
- **Dependencies:** existing editorial workflow and owned-image dry run.
- **Out of scope:** automated scraping, unreviewed hotlink substitution, and AWS
  provisioning without approval.

## PA7: Backend Observability Contract

- **Goal:** Produce the minimum safe operational/product signals required for a
  closed beta.
- **Files/components:** structured logs/metrics, failure categories, privacy and
  retention documentation, deployment alarm plan.
- **Acceptance criteria:** measure API errors/latency, content unavailable,
  notification batches/results, and categorized image-origin failures without
  logging tokens, answer content, query-string secrets, or unnecessary personal
  identifiers.
- **Dependencies:** PA3-PA5 where applicable and approved deployment design.
- **Out of scope:** advertising profiles, user accounts, or broad event-level
  behavioral tracking.
