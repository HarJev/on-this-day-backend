# Owner Review: December 26 to April 12 Content, Then Event Images

Started 2026-10-01. This is the handoff record for the delegated owner review
and publication of the December 26 to April 12 drafts (PRs #29, #30 and #31),
followed by an image pass over published events. A worker who picks this up
resumes at the first stage not marked COMPLETED.

## Authority

The owner delegated this review in the project thread, following the October 9
to December 25 review (`OWNER_REVIEW_OCT09_DEC25_PROGRESS.md`, PR #24) as the
template. Rules applied:

- Keep only events with an exact or widely accepted date. Drop an event whose
  date or key fact is disputed instead of reworking it.
- A quiz answer that appears in its linked event summary is acceptable.
- "The database" is the local Docker Postgres. Supabase and every hosted or
  shared database stay untouched. Nothing is deployed.
- Images go to the existing event-image S3 bucket (`AWS_PROFILE=on-this-day`,
  CloudFront Free plan). No other AWS resource is created or changed.

Ledgers record the reviewer as `Claude (owner review delegated by Jevaun
Harris)` with `reviewedOn` 2026-10-01.

## Status

| Stage | Status | Evidence / next action |
| --- | --- | --- |
| R1: worktree from latest main | COMPLETED | Branch `claude/owner-review-publish-dec26-apr12` from `a8839b8` (PR #31 merged). |
| R2: review events | COMPLETED | 538 drafts over 109 days. Every title, description, date note and source note read; doubtful dates checked against a second source. 534 approved, 4 rejected (below). |
| R3: review quiz content | COMPLETED | 372 draft questions and 44 links to existing questions read. 368 questions and all 44 links approved; 4 questions rejected because their only event was dropped. |
| R4: promote to canonical | COMPLETED | `editorial/tools/runway/promote_dec26_apr12.py`: 534 events and 109 days appended; packs 170, 180 and 190 published; 44 `relatedEventIds` added after re-checking every snapshot hash. Draft files removed. |
| R5: checks | COMPLETED | `EditorialReviewCheckCommand` passes for all 18 batches; `mvn -B test` 266 tests, 0 failures; `ContentStatusCommand` (no DB): 0 drafts outside `content/`, every new event and question has an approved entry, 0 unknown related event IDs; `git diff --check` clean. |
| R6: pull request | IN_PROGRESS | PR #32 opened; a separate review thread reviews and merges it. The author does not merge. |
| R7: local DB import | NOT_STARTED | After merge: pull main, historical import then quiz import into local Docker Postgres only. |
| R8: verification | NOT_STARTED | `ContentStatusCommand` against the DB (`inSync: true`) and record counts. |
| I1: image candidates | NOT_STARTED | See "Image Pass" below. |

## Result (after R4)

Canonical content: 988 events, 218 days (every date from September 15 to April
12, February 29 included, plus the earlier August and September days), 598
questions (597 published, one retired).

## Rejected Events

Each stays in its batch ledger as `rejected` with this reason.

| Date | Event | Reason |
| --- | --- | --- |
| Dec 28 | `constance-markievicz-elected-1918` | The general election was held on December 14, 1918; December 28 is when results were declared. |
| Jan 5 | `ford-five-dollar-day-1914` | Ford announced the $5 day on January 5, 1914; the new pay took effect on January 12. |
| Feb 13 | `georges-simenon-born-1903` | His birth certificate gives February 12; Simenon said he was born just after midnight on the 13th. |
| Feb 27 | `hey-detects-solar-radio-waves-1942` (featured) | The radar interference was reported on February 27 and 28, 1942. The Reichstag fire is the new featured event, with new notification copy. |

Each of these days keeps four events, so none needs an `editorialException`.
The questions `markievicz-took-seat`, `ford-five-dollar-day`,
`simenon-detective` and `hey-radio-source` were rejected with their events.

## Corrected Details

Two secondary details were narrowed; the date and key fact stand.

- `eris-discovered-2005`: "images taken two years earlier" is now "images taken
  in 2003" (the images date from October 2003, about 15 months before the
  discovery).
- `haiti-earthquake-2010`: the death toll ("more than 300,000", the Haitian
  government's figure) is disputed, so the description now says the quake was
  one of the deadliest on record instead of giving a number.

## Kept With A Note

- George Harrison (Feb 25): he later said he was born late on February 24, but
  his birth certificate and standard references give February 25.
- Julian and Old Style dates keep the `dateNote` the drafting batches added.
- Caesar's crossing of the Rubicon (Jan 10, 49 BCE) is the traditional date,
  noted as such, like Luther's theses in the earlier review.
- Earhart's Hawaii flight (left January 11, landed January 12) and the Castle
  Hill Rising (began the evening of March 4) say what happened on the date.
- Washington's election (Feb 4, 1789) is the day the electors voted.
- Gagarin's "one orbit in 1 hour 29 minutes" is the orbit time; the whole
  flight lasted 108 minutes. The question asks about the orbit.
- The Bangla language protest toll (seven killed) follows the cited Cambridge
  page.
- The Hoover Dam "completed" wording follows the cited page (PR #30 review
  marked the alternative optional).

## Image Pass

Scope: published events without an image from October 16 onward, plus the known
gaps (UPU, Outer Space Treaty, Voskhod 1, first Oktoberfest, Peanuts, Monty
Python, Yom Kippur War, Don Larsen), and quiz questions that need an image.
Tooling: `editorial/event-images/event_image_pipeline.py` and
`docs/EVENT_IMAGE_PIPELINE.md`. Status is recorded here as batches progress.

## Not Done

- No Supabase, shared or production import. No deployment.
- Existing Daily assignments are not retrofitted.
- `editorial/tools/runway/b1226.py`, `b0201.py`, `b0308.py` and the three
  connected-quiz generators would recreate the removed draft files if rerun;
  they are now a historical record of the drafts.
