# Owner Review: October 9 to December 25 Content

Started 2026-10-01. This is the handoff record for the delegated owner review
and publication of every `source_verified` draft that was not yet canonical.
A worker who picks this up resumes at the first stage not marked COMPLETED.

## Authority

The owner delegated the review in the project thread: "run a owner's review for
me of all the unpublished stuff in the database that should go to the database
and run them against the database if all good." Rules applied:

- Keep only events with an exact or widely accepted date. Drop an event whose
  date or key fact is disputed instead of reworking it.
- A quiz answer that appears in its linked event summary is acceptable.
- "The database" is the local Docker Postgres. Supabase and every hosted or
  shared database stay untouched. Nothing is deployed.

Ledgers record the reviewer as `Claude (owner review delegated by Jevaun
Harris)` with `reviewedOn` 2026-10-01, so the delegation stays visible.

## Status

| Stage | Status | Evidence / next action |
| --- | --- | --- |
| R1: worktree from latest main | COMPLETED | Branch `claude/owner-review-publish-oct-dec` from `6ce4dab` (PR #23 merged). |
| R2: review events | COMPLETED | 317 drafts (316 runway + 1 supplementary). Automated date, source, duplicate and featured-copy checks, then every title, summary and description read. 308 approved, 9 rejected (below). |
| R3: review quiz content | COMPLETED | 19 Oct 9-15 questions, 7 Oct 1-8 supplement questions and 12 reuse links all approved as drafted. |
| R4: promote to canonical | COMPLETED | `editorial/tools/runway/promote_oct09_dec25.py`: 308 events and 78 days appended; Packs 150 and 160 published; 12 `relatedEventIds` added after re-checking snapshot hashes. Draft files removed. |
| R5: checks | COMPLETED | `EditorialReviewCheckCommand` passes for all 16 Oct-Dec batches; `mvn -B test` 198 tests, 0 failures; `ContentStatusCommand` (no DB): 0 drafts outside `content/`, no new event without an approved entry. |
| R6: pull request | IN_PROGRESS | Open the PR and send the number to the coordinator. A separate review thread reviews and merges it. Do not merge it from this thread. |
| R7: local DB import | NOT_STARTED | After the merge is confirmed: from main, run the historical importer, then the quiz importer, against local Docker Postgres only. |
| R8: verification | NOT_STARTED | Counts, `ContentStatusCommand` with DB (`inSync: true`), and SAM local checks of today, event detail, catalog and Quick Play. |

## Result

Canonical content after promotion: 454 events, 109 days (every date from
September 15 to December 25 plus the earlier August and September days),
230 questions (229 published, one
retired), 89 question-event relations.

## Rejected Events

Each stays in its batch ledger as `rejected` with this reason.

| Date | Event | Reason |
| --- | --- | --- |
| Nov 10 | `stanley-meets-livingstone-1871` | Its own source says the date is unclear: Stanley gave November 10, Livingstone's journal suggests October 24-28. |
| Nov 17 | `diocletian-acclaimed-emperor-284` | Most accounts give November 20, 284. |
| Nov 24 | `turkey-legal-equality-reform-2001` | The new Turkish Civil Code was adopted on November 22, 2001. |
| Nov 28 | `lady-astor-takes-seat-1919` | Astor was elected on November 28 but took her seat on December 1, 1919. |
| Dec 5 | `mary-celeste-found-abandoned-1872` (featured) | Discovery date disputed between December 4 and 5. Nelson Mandela's death is the new featured event, with new notification copy. |
| Dec 16 | `avatar-released-2009` | No single release date: London premiere December 10, staggered international release, US December 18. |
| Dec 17 | `us-cuba-relations-restored-2014` | December 17, 2014 was the announcement; relations were restored on July 20, 2015. |
| Dec 21 | `curies-discover-radium-1898` | The discovery was announced on December 26, 1898. |
| Dec 24 | `king-john-born-1167` | Birth year disputed between 1166 and 1167. |

These nine days now have three events and an `editorialException` naming the
dropped event. They were not padded with new, unreviewed events.

## Kept With A Note

- Julian-calendar dates (Agincourt, Lutzen, the Gunpowder Plot, the Mayflower
  Compact, ancient and medieval births) keep their existing `dateNote`. These
  are the dates history uses, not disputes.
- `luther-ninety-five-theses-1517` already says "according to tradition".
- `smallpox-declared-eradicated-1979`: the Global Commission certified
  eradication on December 9, 1979; the World Health Assembly endorsed it in
  May 1980. The Britannica wording is kept.
- `benazir-bhutto-elected-1988`: November 16 was the general election; she was
  sworn in on December 2. Britannica's wording is kept.
- Featured days without an image: only Oct 9-15 events have owned images
  (PR #23). Later featured events use the supported image-free layout.

## Not Done

- No Supabase, shared or production import. No deployment. The unused
  Washington Monument image stays in S3 untouched.
- Existing Daily assignments are not retrofitted.
- `editorial/tools/connected-quiz/build_oct_cycle.py` would recreate the
  removed draft files if rerun; it is now a historical record of the drafts.
