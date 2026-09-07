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