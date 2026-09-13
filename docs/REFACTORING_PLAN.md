# Backend Refactoring Plan

## Purpose

This document records the planned backend refactoring sequence for On This Day.
The goal is to improve clarity, efficiency, and maintainability while preserving
the exact current API and product behavior.

The backend supports a small curated-history mobile experience:

```text
Today -> Featured event -> Learn -> Read more -> Come back tomorrow
```

That context matters. The system is not expected to process a large social feed,
arbitrary historical searches, or high-volume transactional workloads in the
first release. Refactoring should therefore favor simple, obvious Java and JDBC
code, predictable Lambda execution, bounded resource use, and measured changes
over broad abstractions or premature infrastructure.

## Non-negotiable constraints

- Preserve the current API routes, response shapes, status codes, and error
  behavior unless a separate compatibility decision is approved.
- Preserve curated editorial selection and stable event IDs.
- Preserve notification eligibility and FCM result classification.
- Keep the modular-monolith direction: content, notifications, quiz, platform,
  and ingestion remain understandable package boundaries.
- Do not introduce Hibernate/JPA or a new application framework for this scope.
- Do not add unbounded concurrency, caching, or connection pools without
  accounting for Lambda concurrency and PostgreSQL connection limits.
- Do not turn a small v0.0.1 system into a generalized platform.

## Execution principles

1. Establish a behavior and performance baseline.
2. Make resource ownership and failure behavior explicit.
3. Refactor the smallest reusable seams first.
4. Optimize the actual hot paths: daily content reads and notification sends.
5. Benchmark before selecting Lambda memory, pooling, proxy, or fan-out options.
6. Review each phase independently so changes remain easy to approve or revert.

## Current risks this plan addresses

- Physical PostgreSQL connections are opened per repository operation.
- Notification delivery materializes all eligible devices and groups them again.
- Notification sends are sequential and one HTTP request is made per token.
- Daily notification planning may load the same calendar date repeatedly for
  different timezones.
- JDBC transaction, rollback, timing, and exception-translation code is repeated.
- Normal database operations produce substantial INFO-level CloudWatch logging.
- Quiz composition has grown beyond the original v0.0.1 architecture description
  and needs an explicit scope decision.
- Static-analysis rules are not currently enforced by Maven or CI.

## Order of execution

### Phase 0 — Baseline and safety net

**Objective:** Make current behavior measurable before changing internals.

Tasks:

- Record the current Maven test and integration-test status.
- Capture representative API response fixtures for health, today, event detail,
  device registration, and quiz routes.
- Capture current error/status behavior for invalid requests, unavailable content,
  missing events, and database failures.
- Add or confirm focused tests around repository mapping, route generation,
  notification classification, and featured/additional event rendering.
- Add lightweight static analysis with rules appropriate for this Java 21 project.
  Start with useful findings, not a large noisy ruleset.
- Establish a simple measurement checklist for cold start, database acquisition,
  request latency, FCM latency, and memory use.

Exit criteria:

- Existing tests pass.
- Baseline response and error behavior is recorded.
- Static-analysis findings are classified into real issues, accepted findings,
  and false positives.
- No production behavior has changed.

**First implementation slice after review:** add the baseline tests and analysis
configuration only. This is intentionally low-risk and gives later refactors a
regression alarm.

Baseline captured on 2026-09-13:

- `mvn test` passes.
- Java `-Xlint:all` is enabled for compilation without making warnings fatal.
- SpotBugs runs during `verify` in report-only mode.
- SpotBugs reported 86 instances across 7 pattern types. Many are expected
  warnings for immutable records and dependency-injected collaborators. The
  findings requiring review are tracked rather than suppressed:
  - possible JDBC statement cleanup in `JdbcTodayContentRepository`;
  - possible null handling issues in `QuizContentReader`;
  - mutable-list exposure warnings in DTO/record models;
  - dynamically generated SQL warnings in the bounded quiz ID query path.
- SpotBugs is intentionally not fail-the-build yet. Findings will be classified
  and reduced during the relevant refactoring phases before enforcement.

Phase 1 completed on 2026-09-13:

- Quiz scope was clarified as an additive, already-implemented feature.
- API JSON mapper creation is centralized at the platform boundary.
- Runtime quiz wiring is isolated behind a named composition method.
- API routes and response behavior remain unchanged.

Phase 2 completed on 2026-09-13:

- Today-content JDBC statements are now created and closed within their owning
  repository methods.
- The quiz catalog read no longer opens an explicit repeatable-read transaction
  for two independent read queries.
- The aggregate quiz question read retains its explicit snapshot transaction
  because it combines multiple related queries.
- Connection-pool selection remains measurement-dependent and is intentionally
  deferred to deployment benchmarking.

Phase 5 completed on 2026-09-13:

- Routine JDBC and successful FCM timing logs now use DEBUG, reducing normal
  CloudWatch noise while preserving warnings, errors, and delivery summaries.
- No token values or user-sensitive notification data were added to logs.

Phase 3 started on 2026-09-13:

- Notification planning now groups tokens directly instead of retaining a second
  grouped copy of full `DeviceRegistration` objects. This reduces per-invocation
  object retention while preserving recipient ordering, timezone grouping, and
  delivery behavior.
- Full database paging is intentionally not introduced yet because the current
  repository contract returns a complete list. Adding paging requires a
  repository/API seam change so recipients are not silently omitted.

Phase 4 completed conservatively on 2026-09-13:

- Confirmed that `FcmNotificationSender` reuses one `HttpClient` and one access
  token provider per warm Lambda container.
- Made connection and request timeouts named constants so the external delivery
  limits are obvious and consistently applied.
- Did not add parallel sends or retries. The current notification volume does
  not justify introducing FCM rate-limit pressure or duplicate-delivery risk
  before batching and idempotency are designed together.

Phase 6 baseline completed on 2026-09-13:

- `mvn package -DskipTests` passed.
- `sam validate --template-file template.yaml --lint` confirmed that the SAM
  template is valid.
- ARM64 is already selected, which is a sensible default for this Java Lambda.
- The current 1024 MB memory and 30-second timeout were not changed because the
  repository has no production duration, cold-start, memory, concurrency, or
  database-capacity measurements. For this small curated-history workload,
  changing them by guess would be less responsible than measuring first.
- Reserved concurrency, connection pooling/RDS Proxy, and timeout changes remain
  deployment decisions requiring AWS metrics and representative load tests.

### Phase 1 — Clarify runtime composition and configuration

**Objective:** Make Lambda startup and dependency wiring obvious.

Tasks:

- Decide whether quiz endpoints are officially part of the current release
  scope. Update architecture documentation to match the decision.
- Keep the API handler thin and move construction into clearly named composition
  factories where that improves readability.
- Centralize the configured `ObjectMapper` and shared runtime settings.
- Document which initialization is safe during cold start and which operations
  must remain lazy.
- Review production secret loading and ensure database credentials do not rely on
  local-template defaults.

Exit criteria:

- A maintainer can trace Lambda handler -> router -> service -> repository without
  guessing which optional features are installed.
- Cold-start initialization does not open database connections unnecessarily.
- Route and response behavior remains unchanged.

### Phase 2 — Improve JDBC resource and transaction handling

**Objective:** Reduce connection churn and remove repeated low-level boilerplate
  without hiding SQL.

Tasks:

- Introduce a small internal JDBC execution helper for connection acquisition,
  transaction boundaries, rollback suppression, and exception translation.
- Keep SQL statements and row mapping in the repository classes.
- Remove explicit commits from read-only operations where snapshot consistency is
  not required.
- Document and test the one read path that intentionally needs repeatable-read
  consistency, if it is still necessary.
- Set read-only mode and conservative JDBC timeouts where appropriate.
- Select the connection strategy from measurements:
  - no pool for very low concurrency if connection volume is demonstrably safe;
  - a very small bounded pool per warm container; or
  - RDS Proxy/serverless-provider pooling when database connection pressure is
    the actual constraint.

Exit criteria:

- No connection or statement leak paths remain.
- Transaction ownership is obvious at each repository method.
- Connection count and latency are measured under expected Lambda concurrency.
- SQL remains straightforward and inspectable.

### Phase 3 — Bound notification memory and runtime *(deferred until after Phase 5)*

**Objective:** Make the daily notification job safe as device registrations grow.

Tasks:

- Replace the unbounded eligible-device list with bounded pages or an explicitly
  managed iterator.
- Avoid retaining both the complete registration list and a grouped copy.
- Cache featured content by resolved `MonthDay` within one invocation so shared
  dates are loaded once.
- Add an explicit maximum notification batch size.
- Ensure a single cleanup failure for an invalid token does not erase the result
  of the rest of the send operation.
- Preserve current permanent/transient/configuration result semantics.

Exit criteria:

- Memory use is bounded by page/batch size rather than total device count.
- A notification run has a predictable upper execution cost per batch.
- Existing notification tests and result counts remain unchanged.

### Phase 4 — Optimize FCM delivery deliberately *(deferred until after Phase 5)*

**Objective:** Reduce notification duration without creating uncontrolled pressure
  on FCM or the Lambda runtime.

Tasks:

- Measure the current sequential sender first.
- Evaluate FCM multicast/batch APIs against the requirement to classify invalid
  tokens independently.
- If concurrency is needed, use bounded concurrency with lifecycle ownership
  outside the invocation loop; never use an unbounded `parallelStream()`.
- Reuse the existing `HttpClient` and access-token provider across warm
  invocations.
- Add retry/backoff only for explicitly transient failures and keep the Lambda
  timeout bounded.
- Before enabling EventBridge automation, add durable idempotency/deduplication
  for a device and notification date.

Exit criteria:

- Notification duration and failure rates improve under a representative load.
- FCM rate limits are respected.
- Retries cannot create uncontrolled duplicate sends.

### Phase 5 — Consolidate maintainability patterns *(current phase)*

**Objective:** Make common Java practices consistent without over-abstracting.

Tasks:

- Consolidate genuinely identical enum parsing and validation helpers.
- Standardize repository exception translation and log fields.
- Reduce routine database logs to DEBUG or sampled metrics while retaining useful
  request-level and failure logs.
- Centralize request correlation IDs and operational timing metrics.
- Keep domain-specific checks in their owning package.
- Split only classes that remain difficult to scan after the earlier changes.

Exit criteria:

- Common patterns look the same across content, quiz, and notification code.
- Utility abstractions have a clear owner and purpose.
- CloudWatch logs are useful without excessive per-query noise.

### Phase 6 — Benchmark and tune Lambda deployment *(after Phases 3 and 4)*

**Objective:** Select deployment settings based on this app’s real workload.

Tasks:

- Benchmark ARM64 versus the current configuration.
- Test memory sizes using cold and warm invocations.
- Review timeout values against database and FCM worst-case paths.
- Configure reserved concurrency based on database capacity and notification
  delivery requirements.
- Confirm secret access, TLS, and database network behavior in the target AWS
  environment.
- Re-run static analysis, unit tests, integration tests, and response fixture
  comparisons.

Exit criteria:

- Memory, timeout, concurrency, and database choices are justified by data.
- No API or product behavior has changed.
- The final diff is split into reviewable commits or similarly reviewable units.

## Updated execution order

The requested order is now:

1. Phase 0 — baseline and safety net.
2. Phase 1 — runtime composition and configuration.
3. Phase 2 — JDBC resource and transaction handling.
4. Phase 5 — maintainability patterns.
5. Phase 3 — bounded notification memory and runtime.
6. Phase 4 — FCM delivery optimization.
7. Phase 6 — deployment benchmarking and tuning.

Phases 3 and 4 are intentionally delayed until the code is easier to measure
and maintain. This is appropriate for the current curated daily-history
workload, where notification scale is not yet the primary user-facing path.

## Suggested review checkpoints

Review and approve each of these independently:

1. Baseline tests and static-analysis configuration.
2. Runtime composition/configuration cleanup.
3. JDBC lifecycle and transaction refactor.
4. Bounded notification planning.
5. FCM delivery optimization and idempotency.
6. Logging, validation, and final deployment tuning.

## Intentionally deferred

These are not part of the immediate refactoring effort:

- Hibernate/JPA adoption.
- A generalized caching layer.
- Arbitrary date browsing, search, personalization, or accounts.
- Runtime AI content generation.
- Kubernetes or multi-service decomposition.
- Notification history or per-user scheduling unless the product explicitly
  expands beyond the current daily notification model.

## Definition of done

- The API contract and user-visible behavior are unchanged.
- Unit and integration tests pass.
- Static analysis has no unexplained high-severity findings.
- Database connections, statements, and result sets have clear ownership.
- Notification memory and runtime are bounded.
- Lambda settings are supported by measurements from the expected workload.
- Documentation reflects the actual architecture, including quiz scope.
- The final changes are small enough for a human reviewer to understand and
  maintain confidently.

## Refactoring pass status

**Complete — 2026-09-13.**

The planned read-only review and behavior-preserving refactoring pass is
complete. The remaining items are intentionally operational or scale-triggered
follow-ups, not unfinished cleanup:

- Add repository paging if notification registrations grow beyond the safe
  in-memory operating range.
- Measure Lambda memory, duration, concurrency, and PostgreSQL connection
  pressure in AWS before changing deployment settings.
- Revisit FCM batching, bounded parallelism, and durable idempotency when daily
  notification volume justifies the added complexity.
- Classify and reduce the remaining SpotBugs findings, many of which are
  expected warnings for records and injected collaborators.
