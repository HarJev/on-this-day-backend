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
| S2: research | COMPLETED | Britannica On This Day pages for all 37 days read 2026-10-01, plus article pages (Britannica, NASA) for events and quiz facts. |
| S3: draft events | COMPLETED | `b1226.py`: 178 events, 37 days. 30 days have 5 events; Jan 2, 9, 13, 19, 23, 24 and 29 have 4. Every featured event has notification copy. |
| S4: draft quiz questions and links | COMPLETED | `build_dec_jan_cycle.py`: 85 new draft questions (74 multiple choice, 8 true/false, 3 ordering; 36 easy, 44 medium, 5 hard) and 19 relation proposals for unchanged published questions (7 image questions with inherited rights). Every day has at least one quiz hook. |
| S5: validation | COMPLETED | Canonical validators on canonical + drafts merged (`merge.py`, `ValidateMerged.java`, drafts marked published so publish-time checks run): events valid, quiz valid, only pre-existing warnings. `EditorialReviewCheckCommand` passes for all six batches. Structural script: dates match days, IDs unique against canonical, descriptions start with the date, no Wikipedia, featured copy within 60/90 characters. `mvn -B test`: 266 tests, 0 failures (drafts are outside `content/`, so unit counts are unchanged). |
| S6: pull request | COMPLETED | PR #29 opened; awaiting a separate review thread (the author does not merge). Next after merge: owner review and promotion, then local DB import. |

## Counts

| Day range | Events | New questions + links |
| --- | --- | --- |
| Dec 26 to Jan 1 | 35 | 30 hooks |
| Jan 2 to 8 | 33 | 21 hooks |
| Jan 9 to 15 | 33 | 15 hooks |
| Jan 16 to 22 | 34 | 12 hooks |
| Jan 23 to 31 | 43 | 26 hooks |

After promotion the bank would reach 314 published questions (229 + 85).

## Dropped or adjusted while drafting

Applied under the owner's rule (drop a disputed date or key fact, don't rework):

- Aswan High Dam construction begins (Jan 9, 1960): Britannica's own dam article says
  construction started in 1959. Dropped; Joan of Arc's trial is the Jan 9 featured event.
- Scott reaches the South Pole (Jan 18, 1912): commonly dated January 17. Not drafted.
- Swiss Guard arrives at the Vatican (Jan 21, 1506): usually dated January 22. Not drafted.
- Indira Gandhi becomes prime minister (Jan 19, 1966): elected leader on the 19th, sworn in
  on the 24th. Not drafted.
- First US IVF baby (Dec 28): Britannica's page gives 1982; the birth was in 1981. Not drafted.
- Dostoyevsky's death, Rasputin's murder, the Gulf War air campaign, Spirit and Opportunity
  landings, Explorer 1, the Pompidou Centre opening and the Eighteenth Amendment: calendar,
  time-zone or announcement-versus-event ambiguity. Not drafted.
- Galileo's discovery of Jupiter's moons (Jan 7, 1610): Britannica's article gives only
  January 1610. Not drafted.
- Quiz facts the fetched page did not state were rewritten or dropped (for example a
  Galapagos question became one about the voyage's length).
- Earhart's Hawaii flight keeps a `dateNote` (left January 11, landed January 12).
- Review fixes (2026-10-01, review thread on PR #29):
  - Texas (Dec 29): Congress passed the annexation resolution on February 28, 1845; December 29
    is Texas's legal entry into the Union as a state (TSHA Handbook of Texas). The event is now
    `texas-statehood-1845`, "Texas joins the Union as a state", citing TSHA; the
    `texas-before-annexation` explanation says the same.
  - Westminster Abbey (Dec 28): Edward the Confessor was too ill to attend the consecration.
    The event no longer says he opened it and cites the Abbey's own page instead of the
    Britannica day page; `westminster-abbey-founder-king` now asks which king built the church.

## Kept With A Note

- Julian-calendar dates carry `dateNote` (Hagia Sophia, Kepler's birth, Westminster Abbey,
  the East India Company charter, Granada, Luther's excommunication, Anne of Cleves,
  Richard II, Joan of Arc's trial, Charles V, the Shaanxi earthquake, Sao Paulo, Dante,
  Charlemagne, Charles I, Guy Fawkes). Russian Old Style dates note the Julian day (Bloody
  Sunday, Chekhov). Franklin's birth notes the Old Style January 6.
- Caesar's crossing of the Rubicon uses the traditional date in the pre-Julian calendar.
- `league-of-nations-established-1920` (Jan 10) is distinct from the canonical
  `league-of-nations-first-assembly-1920` (Nov 15).
- Markievicz: the 1918 election was held December 14; results were declared December 28.

## Not Done

- No event images. Featured days use the supported image-free layout until a Mac-side
  image pass (docs/EVENT_IMAGE_PIPELINE.md).
- Nothing promoted, imported or deployed. No Supabase or AWS.
- Daily Challenge assignments are not changed.
