# Connected Quiz Content: Active Handoff

Updated: 2026-09-28. Scope: C1/C2 for October 2-8 only.

Read AGENTS.md, CLAUDE.md, CONNECTED_QUIZ_CONTENT_PLAN.md and the editorial
workflow before continuing. This document is not content approval.

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
| C2: October 2-8 slate | IN_PROGRESS | Fourteen proposed hooks below; awaiting owner selection/revisions before C3. |
| C3: sourced pack and link ledgers | NOT_STARTED | Independently verify answers, explanations, distractors and each relationship; draft outside canonical discovery. |
| C4: approval, promotion and import | NOT_STARTED | Explicit owner approval; events first, then questions/links; disposable verification before any approved shared-DB import. |

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

Stop for owner slate review. After approval, perform C3 on an isolated branch
with new editorial files owned by this task. Do not edit the event worker's
October drafts, existing canonical question packs or shared tracker. Propose
reuse links through review records, not immediate canonical edits.

No code, canonical content, ledger approvals, database imports, Daily assignment
regeneration, deployments or cloud resources changed in C1/C2.
