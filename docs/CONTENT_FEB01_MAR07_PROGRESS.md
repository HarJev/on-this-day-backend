# Content Batch: February 1 to March 7

Started 2026-10-01. Handoff record for drafting the next content runway after the
December 26 to January 31 batch (PR #29): daily events plus connected quiz
questions and links for the 36 days from February 1 to March 7, including
February 29. A worker who picks this up resumes at the first stage not marked
COMPLETED.

## Scope and rules

- Drafts only. Nothing here is promoted into `content/`, imported, or deployed.
  Every ledger entry is `source_verified`, never `approved`; the owner (or a
  delegated reviewer) approves and promotes later.
- Owner editorial rules (2026-10-01): keep only events with an exact date or one
  most experts agree on; drop an event whose date or key fact is disputed rather
  than reworking it; a quiz answer that also appears in the linked event summary
  is fine.
- Sources: every cited URL was fetched and read on 2026-10-01 in the drafting
  session. Most events cite Britannica's dated On This Day page for their day.
  Because the PR #29 review found those pages misstating key facts, a day-page
  claim that looked doubtful was cross-checked against an article page, and the
  article is cited (or replaces the day page) where it adds or corrects a fact.
  No Wikipedia. No event images.
- Julian-calendar dates carry `dateNote`, as in the earlier batches.
- February 29 has a full day of five events. The backend serves it in leap years
  (see `docs/PRODUCT.md` on February 29 and `docs/API_CONTRACT.md`).

## Layout

| Batch | Days |
| --- | --- |
| `editorial/batches/2027-02-01-07-historical-events` | Feb 1 to 7 |
| `editorial/batches/2027-02-08-14-historical-events` | Feb 8 to 14 |
| `editorial/batches/2027-02-15-21-historical-events` | Feb 15 to 21 |
| `editorial/batches/2027-02-22-29-historical-events` | Feb 22 to 29 |
| `editorial/batches/2027-03-01-07-historical-events` | Mar 1 to 7 |
| `editorial/batches/2027-02-01-03-07-connected-quiz` | Quiz drafts (pack `180-connected-february-01-march-07.json`) and links to existing questions |

Generators: `editorial/tools/runway/b0201.py` (events) and
`editorial/tools/connected-quiz/build_feb_mar_cycle.py` (quiz). Both are
rerunnable and rewrite their batch directories. The quiz generator reads each
source's check note from the event ledgers, so run the event generator first.

## Status

| Stage | Status | Evidence / next action |
| --- | --- | --- |
| S1: branch from latest main | COMPLETED | `claude/content-feb01-mar07-3gezpy` from `1f7f3fc` (PR #29 merged). |
| S2: research | COMPLETED | Britannica On This Day pages for all 36 days read 2026-10-01, plus article pages (Britannica, NASA, Penn Engineering, University of Cambridge, UNESCO) for events and quiz facts. |
| S3: draft events | COMPLETED | `b0201.py`: 180 events, 36 days, five on every day. Every featured event has notification copy within 60/90 characters. |
| S4: draft quiz questions and links | COMPLETED | `build_feb_mar_cycle.py`: 126 new draft questions (109 multiple choice, 14 true/false, 3 ordering; 59 easy, 61 medium, 6 hard) and 14 relation proposals for unchanged published questions (5 image questions with inherited rights). Every day has at least three quiz hooks. Correct multiple-choice positions are balanced (27/27/28/27); true/false answers are 8 true, 6 false. |
| S5: validation | COMPLETED | Canonical validators on canonical + all drafts merged (`merge.py`, `ValidateMerged.java`, drafts marked published so publish-time checks run): events valid, quiz valid, only pre-existing warnings (missing images, editorial exceptions, small ancient-rome collection). `EditorialReviewCheckCommand` passes for all six batches. Structural script: dates match days, IDs unique against canonical and every draft batch, descriptions start with the date, no Wikipedia, featured copy within limits. `mvn -B test`: 266 tests, 0 failures (drafts are outside `content/`). |
| S6: pull request | COMPLETED | PR opened; awaiting a separate review thread (the author does not merge). Next after merge: owner review and promotion, then local DB import. |

## Counts

| Day range | Events | Quiz hooks (new + links) |
| --- | --- | --- |
| Feb 1 to 7 | 35 | 29 |
| Feb 8 to 14 | 35 | 28 |
| Feb 15 to 21 | 35 | 24 |
| Feb 22 to 29 | 40 | 30 |
| Mar 1 to 7 | 35 | 29 |

After promotion of this batch and PR #29's, the bank would reach 440 published
questions (229 + 85 + 126).

## Dropped or adjusted while drafting

Applied under the owner's rule (drop a disputed date or key fact, don't rework):

- Snow White and the Seven Dwarfs (Feb 4): Britannica's day page says 1937 theaters across the
  US; the general release was in February 1938 after a December 1937 premiere. Not drafted.
- White Rose executions (Feb 22): the day page says 1942; Britannica's White Rose article gives
  February 22, 1943. Drafted with the article as the only source.
- Mendeleev's periodic law (Mar 6, 1869): Russia's Julian calendar makes the presentation date
  ambiguous (sources give both March 6 and March 18). Not drafted.
- Voyager 1 at Io (Mar 5): the day page says nine active volcanoes were observed that day; they
  were found in later image analysis. The event is Voyager 1's closest approach to Jupiter,
  citing NASA.
- Peace Corps (Mar 1): the day page credits the Peace Corps Act; the March 1, 1961, step was
  Kennedy's executive order (the act followed on September 22). The event says so and cites
  Britannica's Peace Corps article.
- ENIAC (Feb 14): described as "announced" (Penn Engineering's wording), not as a public
  demonstration on that date.
- Not drafted for disputed or uncertain dates: Tutankhamun's burial chamber opening (Feb 16 vs 17,
  1923), the first Knesset (Feb 14 vs 16, 1949), the Communist Manifesto (published "February
  1848"), Langston Hughes's birth year, Chopin's birthday (Feb 22 or Mar 1), Rosa Luxemburg's
  birth, Mo Yan (the Mar 5 day page conflicts with his usual February birth date), the Supreme
  Court's first session (no quorum until Feb 2, 1790), the Massachusetts paper money of 1690/91,
  the destruction of Carthage (146 BCE), Coronado's departure (1540), the Mir launch (Feb 19 UTC,
  Feb 20 Moscow), the Sixteenth Amendment (ratified Feb 3, in effect Feb 25), the Ausgleich
  (Feb 8, 1867), Guadalcanal's evacuation, Dolly the sheep's announcement, the Iranian coup of
  1921, William Penn's charter (Julian), Pushkin's death (Julian, and the duel was days earlier),
  New Amsterdam's charter, and Ayn Rand's birth (Julian).
- Not drafted because no fetched source stated the exact date: Ulysses's publication (Feb 2,
  1922; James Joyce's birth is used instead), the Boeing 747's first flight, The Gambia's
  independence, the Dominican Republic's independence, the CITES signing, the Spanish coup
  attempt of 1981 and the Met Museum's opening.
- The Gregorian calendar bull (Feb 24, 1582) predates the reform it ordered, so it carries a
  Julian `dateNote`; it is distinct from the canonical `gregorian-calendar-takes-effect-1582`.

## Kept With A Note

- Julian-calendar dates carry `dateNote`: Mary, Queen of Scots; Martin Luther; Copernicus;
  Galileo; Handel (Halle used the Julian calendar until 1700); Michelangelo; Marcus Aurelius and
  Lucius Verus; the Gregorian bull.
- Gregorian dates with the Old Style day noted: George Washington's birth (February 11, 1731,
  Old Style) and the emancipation of Russia's serfs (February 19 in Russia's calendar).
- `battle-of-stalingrad-ends-1943` (Feb 2) is distinct from PR #29's
  `paulus-surrenders-at-stalingrad-1943` (Jan 31).
- `bell-telephone-patent-1876` (Mar 7) and `alexander-graham-bell-born-1847` (Mar 3) are both
  drafted; the published ordering question `order-modern-communication-milestones` is proposed
  for the patent.
- `ghana-becomes-independent-1957` is named so it does not share an ID string with the published
  question `ghana-independence-1957`, which the batch proposes to link to it.
- International Mother Language Day: UNESCO's page says the day was approved in 1999; the
  Cambridge page says 1998. The event text avoids the year.

## Not Done

- No event images. Featured days use the supported image-free layout until a Mac-side
  image pass (docs/EVENT_IMAGE_PIPELINE.md).
- Nothing promoted, imported or deployed. No Supabase or AWS.
- Daily Challenge assignments are not changed.
