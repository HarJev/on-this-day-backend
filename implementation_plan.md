Using backend folder: `on-this-day-backend`

Path: `/Users/jevaunharris/Workspace/on-this-day-backend`

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
