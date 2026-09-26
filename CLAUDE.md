# Claude Context - On This Day Backend

## Required Reading

Read `AGENTS.md` first. Then read the documents relevant to the task:

- `docs/PRODUCT.md`
- `docs/PRODUCT_DECISIONS.md`
- `docs/ARCHITECTURE.md`
- `docs/API_CONTRACT.md`
- `docs/SETUP.md`
- `docs/CONTENT_IMPORT_GUIDE.md` for content work
- `docs/EDITORIAL_CONTENT_WORKFLOW.md` for candidates/review/promotion
- `docs/QUIZ_AUTHORING_GUIDE.md` for quiz content
- `docs/PRODUCTION_DEPLOYMENT_PLAN.md` for deployment work
- `implementation_plan.md`
- `../PROPOSED_CHANGES.md` for current priorities
- `../CLAUDE.md` for shared workflow rules

Do not duplicate or replace those documents here. If they disagree, report the
conflict before changing behavior.

## Current Reality

This is a Java 21 modular monolith deployed conceptually as Lambda behind API
Gateway, using PostgreSQL, Flyway, and plain JDBC. Quiz APIs and editorial
tooling are implemented, not future placeholders.

Current HTTP surface includes:

- health;
- Today content by timezone;
- Event Detail;
- device registration/deletion;
- Quiz catalog;
- Quick Play;
- Daily Challenge.

Some older `AGENTS.md` sections list only the original history endpoints.
Current code and API documentation supersede those stale endpoint lists.

## Boundaries

Keep these responsibilities separate:

- domain/services contain ordinary Java logic;
- JDBC repositories own SQL and transactional persistence;
- `platform` owns Lambda/API Gateway adapters and JSON DTOs;
- `ingestion` owns curated-content reading, validation, review, and imports;
- runtime composition wires dependencies without introducing Spring/JPA.

Do not add Spring Boot, Hibernate/JPA, connection pooling, service splitting, or
new infrastructure frameworks without an explicit architectural decision.

## Content And Editorial Safety

Canonical importable content lives under `content/`. Editorial candidates,
review ledgers, and batch manifests live under `editorial/`.

- Never import drafts or unapproved candidates.
- Never treat generated prose as reviewed content.
- Preserve stable event/question IDs and completed Daily assignments.
- Validate before opening a database transaction.
- Use transactional, idempotent importers.
- Use a disposable PostgreSQL database for verification unless the owner
  explicitly approves another target.
- Do not silently replace unreachable sources or invent provenance.
- Image records require accurate direct rendition URL, source page, creator,
  attribution, licence, licence URL, and neutral alt text.
- Keep network liveness checks explicit and outside ordinary deterministic
  import and unit-test paths.
- Distractors are editorial content: plausible, parallel, unambiguous, and not
  giveaways. Correctness is explicit, never inferred from option position.

Generated `build/` review artifacts and unrelated untracked editorial files
may be present. Preserve them unless the task explicitly owns them.

## Local Commands

Use Java 21. A typical shell setup is:

```sh
export JAVA_HOME="$HOME/Library/Java/JavaVirtualMachines/corretto-21.0.4/Contents/Home"
```

Verification should scale with the change:

- focused tests first;
- `mvn -B test` for the unit regression suite;
- `mvn -B verify -Pintegration` when persistence behavior changes and Docker is
  available;
- `sam build` when Lambda/runtime/template wiring changes;
- `git diff --check` before completion.

Do not claim integration success when Docker, SAM, networking, or credentials
prevented the check.

## Deployment And Secrets

No production infrastructure is currently assumed deployed merely because
Terraform or SAM files exist.

- Do not run Terraform apply, SAM deploy, AWS resource creation, Firebase Admin
  setup, production imports, or paid-plan activation without explicit approval.
- Never print or commit secret values.
- Prefer runtime secret references and least-privilege access.
- Treat S3/CloudFront image delivery as a separate owner-approved deployment
  phase.
- Keep local SAM configuration distinct from production deployment design.

## Git

Use a dedicated `codex/` branch or the worktree named by the task. Preserve
unrelated changes. Commit and push verified approved work, but do not merge or
open a PR unless requested.
