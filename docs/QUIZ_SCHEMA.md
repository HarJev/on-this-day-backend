# Quiz Schema

`V3__create_quiz_tables.sql` is the authoritative Quiz v0.1.0 schema. This
document explains its aggregate boundaries and lifecycle without duplicating
the migration.

## Question Relationships

`quiz_question` stores common question metadata: stable ID, type, difficulty,
publication state, prompt, and explanation. Each question has one or more
ordered `quiz_source` rows.

Question-specific children represent exactly four persisted shapes:

- `multiple_choice`: exactly four options and no image or ordering items;
- `true_false`: the ordered `True` and `False` options and no image or ordering
  items;
- `image_identification`: exactly four options and exactly one complete image;
- `chronological_ordering`: exactly four correctly ordered items and no options
  or image.

Deferred constraint triggers validate the complete question at transaction
commit. This lets an importer insert a parent and its children, or replace one
valid child set with another, without depending on statement order. Every
stored question is complete enough to validate, including drafts.

## Publication And Retirement

Publication state is `draft`, `published`, or `retired`. Quick Play and new
Daily assignments select only published questions.

Retiring a question does not remove it from an existing Daily assignment.
Assigned question IDs and types cannot change, but validated corrections to
prompt, explanation, difficulty, sources, image metadata, or answers remain
possible. A retired question may later be republished through validated import
work.

Deleting a question referenced by a Daily assignment is prohibited. Deleting
an unassigned question cascades to its options, ordering items, sources, image,
and collection memberships.

## Sources And Images

Every question must retain at least one source. The source constraint checks
both the old and new question when a source is moved and ignores child deletion
caused by deletion of the parent question.

Images are limited to image-identification questions. The single image record
requires an HTTPS image URL, alt text, source name and URL, attribution, license
name and URL, with an optional creator. Empty or partial provenance is invalid.

## Collections

Collections are flat and grouped as `topic`, `historical_period`,
`civilization`, or `conflict_or_movement`. The many-to-many
`quiz_question_collection` table lets one question appear in multiple broad or
focused collections. Questions need no collection membership to participate in
Mixed selection.

## Daily Challenge Lifecycle

One `quiz_daily_challenge` row is keyed by calendar date. Creation is one
transaction:

1. insert the challenge with no `completed_at` value;
2. insert 20 distinct published question references at positions 1 through 20;
3. set `completed_at`.

A deferred constraint rejects any transaction that would commit an incomplete
challenge. Once complete, the challenge row and all membership rows are
immutable. Retiring an assigned question remains allowed because retirement
does not change assignment membership or order.

The date primary key is the concurrency boundary. The future JDBC repository
will use `INSERT ... ON CONFLICT DO NOTHING`; a losing creator rereads the
winner's completed assignment. Unique position and question constraints ensure
that all 20 positions and references are distinct.

## Deferred Responsibilities

Q2 defines schema, immutable domain types, and repository contracts only.
Later tasks own:

- Q4: JDBC repositories and catalog counts;
- Q5: balanced selection, deterministic generation, and the concrete
  `insertIfAbsent` implementation;
- Q6: API DTOs, handlers, error mapping, runtime composition, and SAM routes.

The schema does not store attempts, answers, scores, users, leaderboards, timer
state, or mobile-local Daily history.

## Ingestion Lifecycle

Q3 adds curated JSON tooling under `content/quizzes/`. The canonical bank is
allowed to be empty until Q7; an empty bank is valid with a warning and imports
without writing questions.

The quiz importer validates all loaded collections and question packs before it
requests a database connection. It then uses one transaction, upserts only the
listed collections and question parents, and replaces child rows only for
imported questions: sources, options, ordering items, image metadata, and
collection memberships.

Import never deletes questions or collections just because they are absent from
the current files. Retirement must be explicit through `publicationState:
retired`. The importer never writes or modifies `quiz_daily_challenge` or
`quiz_daily_question`, so completed Daily assignments remain governed by the V3
immutability constraints.

Q4-Q6 still own runtime read repositories, selection/generation, catalog
responses, quiz API handlers, SAM route exposure, and timer metadata delivery.
