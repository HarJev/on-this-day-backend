# Connected Quiz Content: Active Handoff

Updated: 2026-10-01. Active scope: C1-C3 for October 9-15, plus a
supplementary pass for October 1-8. The October 2-8 record below is complete.

Read AGENTS.md, CLAUDE.md, CONNECTED_QUIZ_CONTENT_PLAN.md and the editorial
workflow before continuing. This document is not content approval.

## Cycle: October 9-15 (with October 1-8 supplement)

- Branch: `claude/connected-quiz-oct-09-15` from main `123eeae`, in a
  separate worktree. Researched by Claude.
- Batch: `editorial/batches/2026-10-09-15-connected-quiz/` (drafts only).
- Event dependency: `editorial/batches/2026-10-09-15-historical-events/`
  (30 `source_verified` drafts, read-only in this cycle).
- Hard limits: no approval, canonical edits, imports, Daily assignment
  changes, deployments, uploads or cloud resources.

| Stage | Status | Evidence / next action |
| --- | --- | --- |
| C1: Oct 9-15 event and bank audit | IN_PROGRESS | Started 2026-10-01. |
| C1b: missing strong events | NOT_STARTED | Research only where a genuinely strong event is missing. |
| C2/C3a: reuse links | NOT_STARTED | Verify each relation against a directly opened source. |
| C3b: new question drafts | NOT_STARTED | Draft, source-check and validate. |
| C3c: Oct 1-8 supplement | NOT_STARTED | Extra hooks for published days; drafts only. |
| C3d: validation and owner checklist | NOT_STARTED | Strict validators, OWNER_REVIEW.md, draft PR. |

If this cycle is interrupted, the next worker resumes at the first stage not
marked COMPLETED and rechecks main for concurrent content changes first.

## Checkpoint

- Backend metadata checkpoint `f0ace9f` is merged in main at `dc329ad`.
- Mobile navigation checkpoint `f6f5d7d` is merged in main at `6cd4be8`.
- Native connected-flow verification is still pending. See mobile
  `docs/QUIZ_EVENT_INTEGRATION_PROGRESS.md`; do not erase that gate.
- Canonical inventory: 118 events, 24 dates, 196 questions (195 published,
  one retired). Published types: 108 multiple choice, 31 true/false,
  36 image identification, 20 chronological ordering.
- 34 published questions have 37 declared event relations.
- No October 2-8 dates exist in canonical content. The separate event-worker
  batch contains seven days and 28 events; all 28 ledger entries are
  `source_verified`, not approved. Preserve those files unchanged.
- This is a filesystem inventory, not a database synchronization claim.

## Status

| Item | Status | Evidence / next action |
| --- | --- | --- |
| C1: October 2-8 inventory | COMPLETED | Canonical bank, all seven draft days, 28 event records and their ledger inspected; reuse candidates below. |
| C2: October 2-8 slate | COMPLETED | Owner approved all fourteen hooks on 2026-09-29 for research/drafting, not publication. |
| C3: sourced pack and link ledgers | COMPLETED | Eight draft questions and six link proposals researched; strict quiz/editorial checks pass. Final owner content review pending. |
| C4: approval, promotion and import | COMPLETED | Owner approved events, questions and links on 2026-09-29; promoted and imported into the local Docker DB only. See C4 evidence below. |

## Proposed Slate

These are editorial proposals, not approved relations or validated question
JSON. Existing IDs are exact bank IDs; new IDs are provisional. A broader
context hook is intentional: the answer must not require reading the article.

| Date | Event ID | Proposed hook | Action / rationale |
| --- | --- | --- | --- |
| Oct 2 | `gandhi-born-1869` (featured) | `identify-mahatma-gandhi` | Reuse approved owned portrait and unchanged answer model; recognizes the person rather than testing his birthday. |
| Oct 2 | `beagle-returns-to-england-1836` | `identify-charles-darwin` | Reuse owned portrait; directly identifies the scientist on the voyage. |
| Oct 3 | `german-reunification-1990` (featured) | `german-reunification-two-states` | New MC: which two German states reunited? Use formal state-name pairs, not an unrelated-country giveaway. |
| Oct 3 | `washington-thanksgiving-proclamation-1789` | `identify-george-washington` | Reuse owned portrait; the proclamation's author is a natural link. |
| Oct 4 | `sputnik-1-launched-1957` (featured) | `order-space-exploration-firsts` | Reuse existing four-item sequence including Sputnik; do not clone an easier date-order variant. |
| Oct 4 | `lesotho-independence-1966` | `lesotho-surrounding-country` | New MC: which country surrounds Lesotho? Southern African country options; accessible geographic context, not the independence date. |
| Oct 5 | `beatles-love-me-do-1962` (featured) | `beatles-home-city` | New MC: which English city did the Beatles emerge from? Liverpool / Manchester / Birmingham / London. Research the origin, not the recording studio's location. |
| Oct 5 | `monty-python-first-broadcast-1969` | `monty-python-television-format` | New MC: what kind of show was Flying Circus? Sketch comedy / situation comedy / panel comedy / stand-up showcase. Related comedy formats, not random genres. |
| Oct 6 | `first-exoplanet-announced-1995` (featured) | `exoplanet-meaning` | New MC: what defines an exoplanet? Contrast location with composition, moons and distance within our Solar System; avoid implying all orbit a star. |
| Oct 6 | `the-jazz-singer-premieres-1927` | `order-film-milestones` | Reuse the existing sequence containing this film; check precise synchronized-dialogue wording during C3. |
| Oct 7 | `klm-founded-1919` (featured) | `klm-home-country` | New MC: in which country was KLM founded? Netherlands / Belgium / Denmark / Switzerland; do not expand the Dutch acronym in the question. |
| Oct 7 | `project-mercury-approved-1958` | `project-mercury-primary-goal` | New MC: what was Mercury designed to achieve? Human Earth orbit / lunar landing / Mars flyby / permanent space station. Four human-spaceflight goals; no acronym trivia. |
| Oct 8 | `don-larsen-perfect-game-1956` (featured) | `baseball-perfect-game-meaning` | New MC: what distinguishes a perfect game? Four baseball outcome descriptions; explain the term for non-US players without asking for statistics or an opponent. |
| Oct 8 | `great-chicago-fire-1871` | `great-chicago-fire-duration` | Reuse existing true/false misconception; no new casualty question. |

Six reuse proposals and eight new MC hooks. Reuse contributes three image,
two ordering and one true/false question. No new image downloads or rights work
is required for this slate. Proposed new questions should be easy/medium, not
promoted to hard to meet a ratio. Existing difficulty remains unchanged unless
separately reviewed. Fourteen is enough for this window; no padding to 20-30.

Draft distractors above are starting points, not reviewed answers. C3 must
check that no competing option is also defensible and keep lengths parallel.
The featured story may reveal a linked answer when read beforehand; that is
learning, not permission to leak the answer within quiz prompts or alt text.

## Skips And Reserves

- Do not force Saladin's surrender date, Francis's death, Rembrandt's death,
  assassination details, naval casualties, Balkan belligerent dates or treaty
  clause recall. Better hooks may be researched later without a quota.
- `ferdinand-bulgarian-independence` is an exact reusable match, but hard and
  name-heavy. Reserve rather than use as the accessible Oct 5 addition.
- `order-cold-war-milestones` offers broader context, but the sequence does not
  include reunification or the GDR's founding. Do not manufacture a direct
  relation merely because they concern Germany.
- This week skews European/US/space because of the event slate. Lesotho and
  Gandhi add African/Asian coverage; audit the next window across the combined
  pack rather than shoehorning unrelated regions into these relations.
- None of these fourteen question IDs repeats within the proposed week.
  Check adjacent-window reuse before promotion; Daily global fallback can
  still select repeats. This is not a no-repeat runtime guarantee.

## Research Leads Opened

These are discovery leads only, not a new `source_verified` ledger. Event
source-check records were read, not independently re-approved in this task.

- Beatles release/origin: https://www.thebeatles.com/love-me-do and
  https://www.thebeatles.com/please-please-me (origin lead found in search;
  open directly during C3).
- Reunification: https://www.bundesregierung.de/breg-de/schwerpunkte/deutsche-einheit/die-einheit-ist-wirklichkeit-432814
  (official search lead; direct source verification pending).
- Monty Python: https://www.montypython.com/python_The_Pythons/14
  (official search lead; direct source verification pending).
- Exoplanet: https://science.nasa.gov/exoplanets/what-is-an-exoplanet/
  (page opened; extract supporting definition during C3).
- KLM: https://www.klm.com/information/corporate/history
  (opened; founding context supports the proposed country hook).
- Mercury: https://www.nasa.gov/project-mercury/
  (opened; specific goal and competing options still need claim checks).
- Lesotho: https://history.state.gov/countries/lesotho
  (opened; add a directly supporting geographic source for the surrounding-country hook).
- Baseball: https://www.mlb.com/glossary/standard-stats/perfect-game
  (opened but extracted body did not expose a usable definition; do not mark
  supporting until a direct definition can be read).

## Next Worker

C2 slate approval and C3 drafting are complete. Stop for final owner content
review at `editorial/batches/2026-10-02-08-connected-quiz/OWNER_REVIEW.md`.
C4 remains NOT_STARTED. Preserve the event worker's October drafts; obtain
separate event approval before canonical promotion or imports. The prospective
Pack 140 selector does not itself apply six updates to existing packs.

No code, canonical content, ledger approvals, database imports, Daily assignment
regeneration, deployments or cloud resources changed in C1/C2.

## C3 Evidence - 2026-09-29

- Branch: `codex/connected-quiz-draft` from merged main.
- Batch: `editorial/batches/2026-10-02-08-connected-quiz/`.
- `draft-questions.json`: eight MC drafts, five easy/three medium, authored
  correct positions 2/2/2/2. No new images.
- `proposed-links.json`: six existing-question links, current snapshot hashes,
  inherited image-rights evidence. Original questions are not cloned.
- Candidates/ledger: 14 `source_verified`, zero approved, no reviewer/date.
  This is final-content review pending, not permission to publish.
- Existing strict Jackson parser and QuizContentValidator: PASS, eight drafts;
  ten expected draft-only catalog warnings. Log:
  `build/connected-quiz-c3/draft-validation.log`.
- Existing strict editorial reader/validator: PASS, fourteen entries.
- Canonical strict quiz reader/validator after authorized film correction:
  PASS, 196 records. Log: `build/connected-quiz-c3/canonical-validation.log`.
- Local audit: all fourteen targets exist in the read-only event draft; six
  canonical snapshots match; no new ID collisions or repeated hook IDs.
  This does not establish live foreign keys or runtime Daily selection.
- User authorized immediate factual corrections. `4b50434` qualifies the
  Jazz Singer explanation as a landmark in the transition to sound film and
  its October premiere. Items, correct order, other facts and existing links
  are unchanged. Original review evidence updated; no DB import performed.
- Full tests/integration tests were not run for this content-only task.
  Whitespace checks passed. No mobile edits or assets downloaded/uploaded.
- Bank remains 195 published/one retired, with 34 linked questions and 37
  relations. Conditional approval/import projection: 203 published and 48
  linked/51 relations; never report this projection as existing DB coverage.
- Historical dependencies remain unapproved: seven days/28 events. No
  preflight, shared/disposable DB imports, migrations, cloud changes or Daily
  assignment regeneration occurred.

C4 next: owner may approve/revise any of the eight questions or six links.
Recheck concurrent content/pack numbers; preserve stable IDs and existing
Daily assignments; plan dependency-ordered promotion/import separately.

## C4 Evidence - 2026-09-29

- Branch: `codex/connected-quiz-oct-02-08-promotion` from main `08cdecc`.
- Owner approval (2026-09-29, project thread): review, update if needed,
  approve and import the October 2-8 drafts with all links. Ledgers record
  reviewer `Jevaun Harris`, `reviewedOn` 2026-09-29: 28 event entries and 14
  quiz entries (8 new questions, 6 relationships) now `approved`.
- Pre-approval review change: `the-jazz-singer-premieres-1927` title, summary
  and description now describe the October 6, 1927 New York premiere as a
  landmark in the transition to sound film, not the first synchronized-dialogue
  feature, matching the AFI caution already applied to `order-film-milestones`.
  Source URLs unchanged. Other 27 events promoted as drafted.
- Canonical: 28 events and seven days (10-02 to 10-08) appended; Pack 140
  `140-connected-october-02-08.json` holds the eight questions as `published`.
  Six `relatedEventIds` added in the original packs (040, 100, 110, 120) after
  re-verifying each snapshot hash; no other field changed. Draft files removed;
  `proposed-links.json` kept as the link-review record.
- `ClosedBetaRunwayDraftTest` skips runway batches without `draft-events.json`
  (promoted batches), as their events are now canonical.
- Checks: `EditorialReviewCheckCommand` passes for both batches; `mvn -B test`
  198 tests, 0 failures.
- Local Docker Postgres only (migrations V1-V4 current): historical import, then
  quiz import, both successful. Database now 146 events, 31 days, 204 questions
  (203 published), 51 question-event relations. All 14 October 2-8 links present
  and resolve to published questions. `ContentStatusCommand`: `inSync: true`.
  The 10 Daily challenges and 200 assigned questions are unchanged.
- SAM local: catalog reports 203 published; new events resolve via
  `/v1/events/{id}`; Quick Play returns linked questions with `relatedEvents`.
- Event images (owner approved publishing 2026-09-29): 17 new renditions in
  `editorial/event-images/candidates/2026-10-04-08.json`, published to the
  owned media origin (17/17 CloudFront 200, SHA-256 match); 7 October 2-3
  renditions from batch 2026-09-27-10-03 reused. 24 of 28 events and 6 of 7
  featured days now have a primary image; ledger `imageRightsStatus: verified`.
  No free image: Peanuts, Monty Python (copyright), Don Larsen (featured
  10-08; no usable public-domain rendition), Yom Kippur War (unclear rights).
  Local re-import: 24 `event_image` rows; status `inSync: true`.
- Not done: Supabase/shared or production import, deployment, Daily
  regeneration. Existing Daily assignments are not retrofitted.

