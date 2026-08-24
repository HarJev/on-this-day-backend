# AGENTS.md

## Purpose

This repository contains the backend for On This Day.

The backend exists to support the v0.0.1 daily history loop:

```text
Today -> Featured event -> Learn -> Read more -> Come back tomorrow
```

Every change should support that loop unless the user explicitly expands the
product scope.

## Required Reading

Before making architectural, API, persistence, notification, infrastructure, or
content-ingestion changes, read the relevant docs:

- `docs/PRODUCT.md` - canonical v0.0.1 product definition.
- `docs/PRODUCT_DECISIONS.md` - product decisions and scope boundaries.
- `docs/ARCHITECTURE.md` - backend architecture, AWS, database, notification,
  and content-ingestion guidance.
- `docs/API_CONTRACT.md` - v0.0.1 API request/response contract.
- `docs/SETUP.md` - intended local/bootstrap setup.

If docs and implementation disagree, call it out before changing behavior.

## Product Scope

Keep v0.0.1 intentionally small:

- return today's resolved date and curated events;
- return event details by stable event ID;
- register/delete device notification tokens;
- send one daily featured-event notification;
- serve curated content, not generated runtime content.

Do not add v0.0.1 non-goals unless the user explicitly changes scope:

- accounts, login, profiles, or personalization;
- arbitrary date browsing, search, categories, timelines, or public mutation
  APIs;
- admin CMS;
- runtime AI explanations or chat;
- automated visible event scoring;
- GraphQL, event streams, Kubernetes, or multi-service decomposition.

## Architecture Direction

Use Java 21, AWS Lambda, API Gateway HTTP API, Terraform, PostgreSQL, Firebase
Cloud Messaging, and curated content import/validation tooling unless the user
changes the technical direction.

Prefer a small modular monolith with clear package boundaries:

```text
content
notifications
platform
ingestion
```

Keep Lambda/API Gateway event shapes at the platform edge. Domain and service
code should receive ordinary Java request objects and return ordinary response
objects.

Use straightforward SQL first. Prefer Flyway plus JDBC/small repository classes
over Hibernate/JPA for v0.0.1.

## API Scope

The public v0.0.1 API is:

```text
GET    /v1/health
GET    /v1/days/today?timezone=Area/Location
GET    /v1/events/{eventId}
POST   /v1/devices
DELETE /v1/devices/{token}
```

EventBridge-triggered notification sending is not a public HTTP route.

## Content Rules

Curated source files should be reviewable in this repository.

Validation should fail when:

- an event ID is missing or duplicated;
- required event fields are missing;
- an event has no source;
- a supported day has more than one featured event;
- a featured event lacks notification title/body;
- image metadata is incomplete for an image that is present.

Images are optional. Missing imagery must not block an event.

## Collaboration

The user is a senior Java/Spring backend engineer. Explain AWS Lambda, Terraform,
and Flutter-facing contract choices in plain terms and connect them to familiar
backend concepts when useful.

Do not make code changes without giving the user a chance to review the plan
first. Documentation-only updates may be made directly when requested, but keep
the diff focused.

Before finishing implementation work, run relevant tests/build checks, review
the diff, and summarize what changed and what remains.
