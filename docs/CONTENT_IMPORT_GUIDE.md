# Content Import Guide

This guide is the operator path for reviewed historical-event days and quiz
packs. It is intentionally separate from research: candidates and review
ledgers are stored under `editorial/`; importer inputs are only canonical JSON
under `content/`.

## 1. Review Before Editing Canonical Content

Use stable lowercase slug IDs. Historical events live in:

```text
content/events.json
content/daily-events.json
```

`daily-events.json` refers to event IDs from `events.json`. Each day has one
featured ID and zero or more additional IDs. The L4 editorial policy is at
least four total events (one featured plus three additional). Five or more is
preferred when well sourced. A sparse day needs a nonblank
`editorialException` explaining why; it is valid but produces the warning
`editorial exception declared`. A blank exception is an error.

Quiz content lives in:

```text
content/quizzes/collections.json
content/quizzes/questions/<reviewable-pack>.json
```

Question `collectionIds` refer to IDs in `collections.json`. Question, option,
and ordering-item IDs are stable lowercase slugs. Do not reuse an ID for a new
historical claim or answer.

Every event and question needs at least one direct source. Verify that each
source supports the claim and explanation, not merely the broad subject.
Images are optional, but every included image needs its direct rendition URL,
neutral alt text, source name and URL, attribution, license name and URL, and
creator when known. Record source/link and image-rights review in the sidecar
ledger described in [Editorial Content Workflow](EDITORIAL_CONTENT_WORKFLOW.md).

L3's owned-image **dry run** validates a reviewed local rendition and its
metadata. It does not upload an asset, provision storage, or change a canonical
production URL.

## 2. Make A Reviewed Batch

Create a directory such as `editorial/batches/2026-09-example/` containing
`batch.json`, `candidates.json`, and `review-ledger.json`. The full schemas and
approval statuses are in [`editorial/README.md`](../editorial/README.md).

The batch manifest names the touched month/day values, standalone event IDs,
and quiz pack filenames. It is the release selection: omitted canonical content
is not deleted or changed by import. The review check is local and never makes
network requests:

```sh
JAVA_HOME=$(/usr/libexec/java_home -v 21) \
mvn -B exec:java \
  -Dexec.mainClass=com.onthisday.ingestion.editorial.EditorialReviewCheckCommand \
  -Dexec.args="editorial/batches/2026-09-example/batch.json"
```

An approved entry needs a named reviewer, ISO review date, dated
`verified_supporting` checks for every selected canonical source, and
`imageRightsStatus: "verified"` when the selected item has an image. Keep
`unknown`, `unreachable`, and `verified_bad` URLs in the ledger; do not silently
replace them with another URL.

## 3. Start A Disposable Local Or Staging Database

For a disposable local database, start the Compose service and wait until its
health check is healthy:

```sh
docker compose up -d postgres
docker compose ps postgres
```

Apply every Flyway migration with Java 21:

```sh
JAVA_HOME=$(/usr/libexec/java_home -v 21) mvn -B flyway:migrate
```

The host uses `jdbc:postgresql://localhost:5432/on_this_day`. A SAM Lambda on
the Compose network uses `jdbc:postgresql://postgres:5432/on_this_day` instead.

For a staging database, obtain an explicit environment approval first. Supply
credentials through the approved secret/environment mechanism, never shell
arguments or committed files. Production import is a separate, approval-gated
operation; this guide does not authorize it.

## 4. Run Canonical Importers

The historical and quiz importers are independent transactions. A failure in
the second importer does not roll back a successful first import. Run the historical
importer first: quiz questions can link to events through `relatedEventIds`, and
the quiz import stops, listing each missing ID, if a linked event is not in the
database yet. Run and check both commands:

```sh
JAVA_HOME=$(/usr/libexec/java_home -v 21) \
mvn -B exec:java \
  -Dexec.args="jdbc:postgresql://localhost:5432/on_this_day on_this_day on_this_day"

JAVA_HOME=$(/usr/libexec/java_home -v 21) \
mvn -B exec:java \
  -Dexec.mainClass=com.onthisday.ingestion.quiz.QuizContentImportCommand \
  -Dexec.args="jdbc:postgresql://localhost:5432/on_this_day on_this_day on_this_day"
```

These local credentials are Docker Compose defaults only. Do not adapt these
arguments for staging or production; use the environment form below instead. Both importers validate before opening a
connection, upsert only supplied canonical records, replace children only for
supplied records, and are idempotent.

### Production Or Staging Databases

For any database other than local Docker, pass no password on the command
line. Set the connection in the environment and name an SSM SecureString for
the password; the commands then require `sslmode=verify-full` against the
bundled Supabase CA. With neither `DB_PASSWORD_SSM_PARAMETER` nor
`DB_PASSWORD` set, the commands prompt for the password without echoing it.

```sh
# Short-lived AWS credentials from your signed-in profile, for the SSM read.
eval "$(aws configure export-credentials --profile on-this-day --format env)"
export DB_JDBC_URL='jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:5432/postgres'
export DB_USER='postgres.<project-ref>'
export DB_PASSWORD_SSM_PARAMETER=/on-this-day/prod/db-admin-password

mvn -B -q compile exec:java \
  -Dexec.mainClass=com.onthisday.platform.runtime.DatabaseMigrationCommand \
  -Dexec.args=migrate
mvn -B -q exec:java -Dexec.mainClass=com.onthisday.ingestion.CuratedContentImportCommand
mvn -B -q exec:java -Dexec.mainClass=com.onthisday.ingestion.quiz.QuizContentImportCommand
```

`DatabaseMigrationCommand` also takes `info` or `validate`. Use the pooler's
session port (5432) for migrations and imports. Production import still needs
the owner's explicit go-ahead.

For a reviewed staging subset, preflight and import only the batch selection.
The L5 command requires staging configuration in its environment:

```sh
EDITORIAL_STAGING_DB_JDBC_URL=jdbc:postgresql://localhost:5432/on_this_day \
EDITORIAL_STAGING_DB_USER=on_this_day \
EDITORIAL_STAGING_DB_PASSWORD=on_this_day \
JAVA_HOME=$(/usr/libexec/java_home -v 21) \
mvn -B exec:java \
  -Dexec.mainClass=com.onthisday.ingestion.editorial.EditorialStagingImportCommand \
  -Dexec.args="editorial/batches/2026-09-example/batch.json content content/quizzes"
```

The command first validates the review ledger, selected event/day content, and
selected quiz packs. It then invokes the two existing importers. Treat a
partially successful run as a release incident to reconcile, not as one atomic
cross-content transaction.

## 5. Check Coverage, Database Rows, And API Output

Generate a report after every batch. Manifests are optional: without one, the
report marks review status as `unknown` rather than inventing it.

```sh
JAVA_HOME=$(/usr/libexec/java_home -v 21) \
mvn -B exec:java \
  -Dexec.mainClass=com.onthisday.ingestion.editorial.ContentCoverageReportCommand \
  -Dexec.args="content content/quizzes build/content-coverage.json editorial/batches/2026-09-example/batch.json"
```

Then check editorial state and database drift. The status report counts
ledger entries by review status, lists drafts still outside `content/`, lists
canonical records without an approved ledger entry, records canonical content
fingerprints, and summarizes authored correct-answer positions and featured
days without an image. With the database variables set, it also compares every
event, day, and question against the imported rows and lists `stale`
(edited since import), `notImported`, and `onlyInDatabase` records. It is
read-only and never approves, promotes, or imports anything.

```sh
DB_JDBC_URL=jdbc:postgresql://localhost:5432/on_this_day \
DB_USER=on_this_day \
DB_PASSWORD=on_this_day \
JAVA_HOME=$(/usr/libexec/java_home -v 21) \
mvn -B exec:java \
  -Dexec.mainClass=com.onthisday.ingestion.editorial.ContentStatusCommand \
  -Dexec.args="content content/quizzes editorial/batches build/content-status.json"
```

Leave out the three `DB_` variables to skip the database comparison. After an
import, `database.inSync` should be `true`.

Check the selected database rows:

```sh
docker compose exec -T postgres \
  psql -U on_this_day -d on_this_day \
  -c "SELECT month, day, role, event_id FROM daily_event WHERE month = 8 AND day = 22 ORDER BY display_order;"

docker compose exec -T postgres \
  psql -U on_this_day -d on_this_day \
  -c "SELECT question_id, publication_state FROM quiz_question ORDER BY question_id LIMIT 20;"
```

For HTTP checks, start local SAM using the documented Compose-network command
in [`SETUP.md`](SETUP.md#local-sam-api), then query the actual selected day or
quiz endpoint. For example:

```sh
SAM_PORT=3000
curl -i "http://127.0.0.1:${SAM_PORT}/v1/days/today?timezone=America/Jamaica"
curl -i "http://127.0.0.1:${SAM_PORT}/v1/quizzes/catalog"
```

Use a day available in the loaded data when checking `today`; a
`content_unavailable` response for an uncurated calendar date is not an import
failure.

## Example: One Day And One Quiz Pack

1. Add four or more well-sourced event records to `events.json`; add their IDs
   to one day in `daily-events.json`, including notification copy on the
   featured event.
2. Add reviewed questions to
   `content/quizzes/questions/100-september-example.json`; ensure every
   collection ID exists in `collections.json`.
3. Add matching candidate and reviewed ledger entries. Give the batch manifest
   the day (`"09-01"`) and pack filename
   (`"questions/100-september-example.json"`).
4. Run the review check and migration, then choose one local verification path:
   either run the two full canonical importers, or run the reviewed-subset
   staging import for this batch. They are alternatives, not consecutive steps.
   Follow the chosen path with the coverage report, database checks, and API
   checks above.
5. Resolve every error and intentional warning before seeking production-import
   approval. Do not execute production imports from a personal shell history.

## Common Validation Failures

| Message | Meaning and correction |
| --- | --- |
| `editorialException must not be blank when present` | Remove the field or provide a concrete reason for a day below the floor. |
| `featured event must have notificationTitle and notificationBody` | Add both concise featured-notification fields. |
| `source url must be an absolute HTTPS URL` | Use the directly reviewed HTTPS source URL. |
| `image ... is required` | Complete every required provenance field, or remove the optional image. |
| `references an unknown event` / `unknown collection` | Correct the stable cross-file ID reference. |
| `canonical content has no approved review entry` | Add a matching approved ledger entry; candidate status is insufficient. |
| `every canonical source URL must be verified_supporting` | Directly review every canonical URL and record it unchanged. |
| `image content requires verified image rights` | Verify and record rights before selecting image-bearing content. |
| `duplicate ... id` | Retain stable IDs and rename only genuinely new records. |

To reset only the disposable local database after testing, use `docker compose
down -v`, then repeat the migration and import steps. Never use that command on
a shared staging or production database.

## Approval And Import Status

Draft and `source_verified` records are not importer inputs. Canonical files are
the owner-approved publication set; a successful canonical import is the only
path into runtime tables.

The post-audit plan adds a read-only status command that will compare editorial,
canonical, and database counts/fingerprints. Until that command is implemented,
record the approved manifest, run the appropriate canonical importer, inspect
database counts, and repeat the import for idempotency as described above. Do not
claim that a database is current solely because its migrations are current.
