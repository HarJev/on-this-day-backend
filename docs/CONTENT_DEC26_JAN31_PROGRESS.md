# Content Batch: December 26 to January 31

Started 2026-10-01. Handoff record for drafting the next content runway: daily
events plus connected quiz questions and links for the 37 days after the
canonical October 9 to December 25 publish. A worker who picks this up resumes
at the first stage not marked COMPLETED.

## Scope and rules

- Drafts only. Nothing here is promoted into `content/`, imported, or deployed.
  Every ledger entry is `source_verified`, never `approved`; the owner (or a
  delegated reviewer) approves and promotes later.
- Owner editorial rules (2026-10-01): keep only events with an exact date or one
  most experts agree on; drop an event whose date or key fact is disputed rather
  than reworking it; a quiz answer that also appears in the linked event summary
  is fine.
- Sources: every cited URL was fetched and read on the check date. Most events
  cite Britannica's dated On This Day page for their day, following the Nov 13 to
  Dec 25 runway batches. No Wikipedia. No event images (Wikimedia rate-limits
  cloud containers; images are a separate Mac-side pass).
- Julian-calendar dates carry `dateNote`, as in the earlier batches.

## Layout

| Batch | Days |
| --- | --- |
| `editorial/batches/2026-12-26-01-01-historical-events` | Dec 26 to Jan 1 |
| `editorial/batches/2027-01-02-08-historical-events` | Jan 2 to 8 |
| `editorial/batches/2027-01-09-15-historical-events` | Jan 9 to 15 |
| `editorial/batches/2027-01-16-22-historical-events` | Jan 16 to 22 |
| `editorial/batches/2027-01-23-31-historical-events` | Jan 23 to 31 |
| `editorial/batches/2026-12-26-01-31-connected-quiz` | Quiz drafts (pack `170-connected-december-26-january-31.json`) and links to existing questions |

Generators: `editorial/tools/runway/b1226.py` (events) and
`editorial/tools/connected-quiz/build_dec_jan_cycle.py` (quiz). Both are
rerunnable and rewrite their batch directories.

## Status

| Stage | Status | Evidence / next action |
| --- | --- | --- |
| S1: branch from latest main | COMPLETED | `claude/content-dec26-jan31-shc6xd` from `190915d` (PR #28 merged). |
| S2: research | COMPLETED | Britannica On This Day pages for all 37 days read 2026-10-01, plus article pages for events not on them. |
| S3: draft events | IN PROGRESS | `b1226.py`. |
| S4: draft quiz questions and links | NOT STARTED | |
| S5: validation | NOT STARTED | Merge drafts with canonical content and run the canonical validators, `EditorialReviewCheckCommand` per batch, `mvn -B test`. |
| S6: pull request | NOT STARTED | Reviewed and merged by a separate review thread, not the author. |
