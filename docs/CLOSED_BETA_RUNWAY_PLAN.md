# Closed Beta Content Runway Plan

Drafted 2026-09-27. This plan covers the L6 milestone "90 consecutive days
ahead for closed beta" from `PRODUCTION_LAUNCH_WORKPLAN.md`.

## Window

The 90 upcoming dates from 2026-09-27 run from **September 27 to December 25**.
Canonical content covers September 15 to April 12. October 2-8 was promoted
on 2026-09-29, October 9 to December 25 on 2026-10-01 after the delegated
owner review (`OWNER_REVIEW_OCT09_DEC25_PROGRESS.md`), and December 26 to
April 12 on 2026-10-01 after a second delegated review
(`OWNER_REVIEW_DEC26_APR12_PROGRESS.md`).

September 16 and 17 are outside this window but still sit below the
four-event floor. They should get a follow-up batch.

## Batches

The work follows the documented seven-day batch pattern. Each batch directory
under `editorial/batches/` holds the usual `batch.json`, `candidates.json`, and
`review-ledger.json`, plus two draft files in canonical schema:

- `draft-events.json`: event records, exactly as they would appear in
  `content/events.json`.
- `draft-daily-events.json`: day records, exactly as they would appear in
  `content/daily-events.json`.

These drafts sit **outside `content/`**, so neither importer can read them.

| Batch | Dates | Status |
| --- | --- | --- |
| `2026-10-02-08-historical-events` | Oct 2-8 | Approved and promoted 2026-09-29: 28 events (4 per date) |
| `2026-10-09-15-historical-events` | Oct 9-15 | Promoted 2026-10-01 (delegated owner review) |
| `2026-10-16-22-historical-events` | Oct 16-22 | Promoted 2026-10-01 (delegated owner review) |
| `2026-10-23-29-historical-events` | Oct 23-29 | Promoted 2026-10-01 (delegated owner review) |
| `2026-10-30-11-05-historical-events` | Oct 30-Nov 5 | Promoted 2026-10-01 (delegated owner review) |
| `2026-11-06-12-historical-events` | Nov 6-12 | Promoted 2026-10-01 (delegated owner review) |
| `2026-11-13-19-historical-events` | Nov 13-19 | Promoted 2026-10-01 (delegated owner review) |
| `2026-11-20-26-historical-events` | Nov 20-26 | Promoted 2026-10-01 (delegated owner review) |
| `2026-11-27-12-03-historical-events` | Nov 27-Dec 3 | Promoted 2026-10-01 (delegated owner review) |
| `2026-12-04-10-historical-events` | Dec 4-10 | Promoted 2026-10-01 (delegated owner review) |
| `2026-12-11-17-historical-events` | Dec 11-17 | Promoted 2026-10-01 (delegated owner review) |
| `2026-12-18-25-historical-events` | Dec 18-25 | Promoted 2026-10-01 (delegated owner review) |

As of the Dec 18-25 batch, every date from Oct 2 to Dec 25 has four or five
draft events (344 in total). Merged with canonical content, all 90 dates from
Sep 27 to Dec 25 meet the four-event floor.

Each date targets four or five events: one featured event with notification
copy, plus three or four others. The mix aims for global variety across
regions and eras. No images are included; featured events use the supported
image-free layout.

## Review State

All batches are promoted. 308 of the 317 October 9 to December 25 drafts were
approved on 2026-10-01; nine were rejected for disputed dates or key facts and
nine days carry an `editorialException` with three events. Details:
`OWNER_REVIEW_OCT09_DEC25_PROGRESS.md`. Drafting record (before review):

- Claude opened each cited URL and recorded what the page states in the
  source-check note.
- Sources are authoritative publishers such as Britannica, NASA, national
  archives, and government history offices. Wikipedia is never cited.
- Descriptions are limited to what the cited pages state.

Pages that could not be read were left out and not replaced by guessed URLs.
The web-fetch service rate-limited this pass, so throughput set the pace.
From the Oct 12-15 pass onward, Britannica and the Library of Congress returned
HTTP 403 to scripted fetches, so cited pages were read in a real browser and the
exact supporting sentence was recorded in each source-check note.
From the Nov 13-19 batch onward, many events cite Britannica's dated "On This
Day" pages (britannica.com/on-this-day/<Month>-<day>), which state the year and
claim for each listed event.

## Quiz Bank

The canonical bank held 150 published questions when this plan was drafted and
holds 203 as of 2026-09-29 (packs 010-140, including 15 image questions). That meets the
closed-beta volume checkpoint, but count alone does not provide connected Daily supply.
For the remaining runway, follow `CONNECTED_QUIZ_CONTENT_PLAN.md`: first reuse
suitable questions through reviewed links, then research accessible new hooks
from upcoming featured/additional stories. Do not force every event into a quiz.
The 240 target is a checkpoint, not a ceiling. Packs 150 and 160 bring the bank to 229 published,
and packs 170, 180 and 190 (December 26 to April 12) bring it to 597 published.

## Promotion Steps (Per Batch)

1. Review each draft event against its ledger note and cited page.
2. Mark the approved ledger entries `approved`, adding `reviewer` and
   `reviewedOn`.
3. Append `draft-events.json` events to `content/events.json`, and
   `draft-daily-events.json` days to `content/daily-events.json`.
4. Run `mvn -B -Dtest=ClosedBetaRunwayDraftTest test` and
   `EditorialReviewCheckCommand` on the batch. Then run the canonical import
   and coverage report described in `CONTENT_IMPORT_GUIDE.md`.