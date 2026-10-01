# Content Batch: March 8 to April 12

Started 2026-10-01. Handoff record for drafting the next content runway after the
February 1 to March 7 batch (PR #30): daily events plus connected quiz questions
and links for the 36 days from March 8 to April 12. A worker who picks this up
resumes at the first stage not marked COMPLETED.

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
  Because the PR #29 and #30 reviews found those pages misstating key facts, featured
  events, durations, numbers and quiz answers were checked against an article page
  (Britannica, NASA or WHO), and the article is cited where it adds or corrects a
  fact. No Wikipedia. No event images.
- Julian-calendar dates carry `dateNote`, as in the earlier batches.

## Layout

| Batch | Days |
| --- | --- |
| `editorial/batches/2027-03-08-14-historical-events` | Mar 8 to 14 |
| `editorial/batches/2027-03-15-21-historical-events` | Mar 15 to 21 |
| `editorial/batches/2027-03-22-28-historical-events` | Mar 22 to 28 |
| `editorial/batches/2027-03-29-04-04-historical-events` | Mar 29 to Apr 4 |
| `editorial/batches/2027-04-05-12-historical-events` | Apr 5 to 12 |
| `editorial/batches/2027-03-08-04-12-connected-quiz` | Quiz drafts (pack `190-connected-march-08-april-12.json`) and links to existing questions |

Generators: `editorial/tools/runway/b0308.py` (events) and
`editorial/tools/connected-quiz/build_mar_apr_cycle.py` (quiz). Both are
rerunnable and rewrite their batch directories. The quiz generator reads each
source's check note from the event ledgers, so run the event generator first.

## Status

| Stage | Status | Evidence / next action |
| --- | --- | --- |
| S1: branch from latest main | COMPLETED | `claude/content-mar08-apr12-agebrg` from `90fbc3d` (PR #30 merged). |
| S2: research | COMPLETED | Britannica On This Day pages for all 36 days read 2026-10-01, plus article pages (Britannica, NASA, WHO) for featured events, numbers and quiz facts. |
| S3: draft events | COMPLETED | `b0308.py`: 180 events, 36 days, five on every day. Every featured event has notification copy within 60/90 characters. |
| S4: draft quiz questions and links | COMPLETED | `build_mar_apr_cycle.py`: 162 new draft questions (151 multiple choice, 8 true/false, 3 ordering; 62 easy, 98 medium, 2 hard) and 11 relation proposals for unchanged published questions (5 image questions with inherited rights). Every day has at least three quiz hooks. Correct multiple-choice positions are balanced (35/38/40/38); true/false answers are 4 true, 4 false. |
| S5: validation | COMPLETED | Canonical validators on canonical + all drafts merged (`merge.py`, `ValidateMerged.java`, drafts marked published so publish-time checks run): events valid, quiz valid, only pre-existing warnings (missing images, editorial exceptions, small ancient-rome collection). `EditorialReviewCheckCommand` passes for all six batches. Structural script: dates match days, descriptions start with the date, event and question IDs unique against canonical and every draft batch, no Wikipedia, featured copy within limits. `mvn -B test`: 266 tests, 0 failures (drafts are outside `content/`). |
| S6: pull request | COMPLETED | PR opened; awaiting a separate review thread (the author does not merge). Next after merge: owner review and promotion, then local DB import. |

## Counts

| Day range | Events | Quiz hooks (new + links) |
| --- | --- | --- |
| Mar 8 to 14 | 35 | 34 |
| Mar 15 to 21 | 35 | 30 |
| Mar 22 to 28 | 35 | 34 |
| Mar 29 to Apr 4 | 35 | 34 |
| Apr 5 to 12 | 40 | 41 |

After promotion of this batch and PRs #29 and #30, the bank would reach 601
published questions (229 + 85 + 125 + 162).

## Dropped or adjusted while drafting

Applied under the owner's rule (drop a disputed date or key fact, don't rework):

- Robert Koch (Mar 24, 1882): the day page says he "introduced the basis of germ theory". The
  announcement was his discovery of the tuberculosis bacterium (WHO's World TB Day page); the
  event says that and cites WHO and Britannica's Koch article.
- Lee at Appomattox (Apr 9, 1865): the day page says Lee "signed a treaty of surrender". The
  event says he surrendered his army to Grant and that this ended the war in Virginia, citing
  Britannica's Appomattox article; fighting elsewhere continued.
- Pope Francis (Mar 13): the day page says "Francis I"; the event uses "Francis".
- Bell (Mar 10, 1876): cites Britannica's Bell article for the date and the words to Watson.
  It is distinct from the Feb batch's `bell-telephone-patent-1876` (Mar 7).
- The Feb Revolution article gives "Feb. 24-28, old style" for March 8-12, which is off by one
  day, so the `dateNote` only says Russia's Julian calendar was 13 days behind.
- Eiffel Tower (Mar 31): officially inaugurated that day; the event adds that it opened to the
  public on May 15 (Britannica article).
- Not drafted for a disputed or wrong date or key fact: Dachau opening (the Mar 10 day page;
  the camp opened later in March 1933), the Astrodome's first game (the Apr 9 page says 1966;
  the opening exhibition game was in 1965), the Mir crew arrival (Mar 13 page; Soyuz T-15
  launched that day but docked on Mar 15), Bangladesh (the Mar 26 page describes the
  government-in-exile, formed in April 1971), the Tennessee evolution ban (Mar 13 and Mar 21
  pages give different dates), the British North America Act (Mar 29 page says the colonies
  "were united"; Confederation took effect July 1), the last US troops leaving Vietnam (the
  Mar 29 page says "evacuated Saigon", which conflates 1973 and 1975), The Godfather premiere
  (Mar 14 or 15), AZT approval (Mar 19 or 20), the Piccard-Jones balloon flight (finished
  Mar 20, landed Mar 21), the Iraq War's start (Mar 19 US time, Mar 20 in Baghdad), the Foreign
  Legion's founding (Mar 9 or 10, 1831), Tambora's eruption (peaked Apr 10 to 11), Napoleon's
  abdication (Apr 4, 6 or 11, 1814), the Biological Weapons Convention (Apr 10 opened it for
  signature, not "outlawed"), Kurt Cobain's death (date estimated), Booker T. Washington's
  birth (he did not know the date), Peary at the North Pole (claim disputed), Raphael's birth
  (Mar 28 or Apr 6), the Sicilian Vespers, Ponce de Leon in Florida, Pocahontas's marriage,
  Magellan in the Philippines, Samoset and the Pilgrims, St. Patrick's death ("according to
  legend"), the Parthenon's consecration and Cleopatra's reinstatement.
- Not drafted for scope or tone: Malaysia Airlines 370, My Lai, Dunblane and the Tenerife
  runway collision, where a less grim event was available for the day; Artemis II (Apr 1 and
  6, 2026) was left out because it is too recent to check.
- The drop reasons above that are not quoted from a fetched page (Dachau, the Astrodome, Mir,
  Bangladesh, the Vietnam withdrawal) record why the day-page line looked wrong; nothing was
  drafted from them.

## Kept With A Note

- Julian-calendar dates carry `dateNote`: Julius Caesar (44 BCE), Johann Sebastian Bach
  (1685), Elizabeth I (1603).
- Gregorian dates with Russia's Julian calendar noted: the February Revolution, Alexander II's
  assassination and Nicholas II's abdication.
- Gregorian dates with the Old Style or Julian date noted: James Madison's birth (March 5,
  1751, Old Style) and the 1896 Athens Olympics (March 25 in Greece's Julian calendar).
- `gagarin-first-human-in-space-1961` (Apr 12) and `yuri-gagarin-born-1934` (Mar 9) are both
  drafted; one quiz question relates to both.
- `north-atlantic-treaty-signed-1949` (Apr 4) is distinct from the canonical
  `nato-treaty-enters-force-1949`.
- `alaska-purchase-treaty-signed-1867` (Mar 30) is distinct from the canonical
  `alaska-transferred-to-us-1867`.
- `stamp-act-passed-1765` (Mar 22) and `stamp-act-repealed-1766` (Mar 18) are both drafted.
- `beatles-home-city` is proposed for Please Please Me here; PR #30 also proposes it for the
  Beatles' arrival in New York. Both relations can coexist.
- The Pony Express article gives "April 1860"; the day page gives April 3.

## Not Done

- No event images. Featured days use the supported image-free layout until a Mac-side
  image pass (docs/EVENT_IMAGE_PIPELINE.md).
- Nothing promoted, imported or deployed. No Supabase or AWS.
- Daily Challenge assignments are not changed.
