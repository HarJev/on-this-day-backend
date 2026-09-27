# Closed Beta Content Runway Plan

Drafted 2026-09-27. This plan covers the L6 milestone "90 consecutive days
ahead for closed beta" from `PRODUCTION_LAUNCH_WORKPLAN.md`.

## Window

The 90 upcoming dates from 2026-09-27 run from **September 27 to December 25**.
Canonical content already covers September 15 to October 1, so the gap is
**October 2 to December 25 (85 dates)**.

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
| `2026-10-02-08-historical-events` | Oct 2-8 | Drafted: 28 events (4 per date), source-verified |
| `2026-10-09-15-historical-events` | Oct 9-15 | Drafted: 30 events (4-5 per date), source-verified |
| `2026-10-16-22-historical-events` | Oct 16-22 | Drafted: 28 events (4 per date), source-verified |
| `2026-10-23-29-historical-events` | Oct 23-29 | Drafted: 28 events (4 per date), source-verified |
| `2026-10-30-11-05-historical-events` | Oct 30-Nov 5 | Drafted: 28 events (4 per date), source-verified |
| `2026-11-06-12-historical-events` | Nov 6-12 | Drafted: 28 events (4 per date), source-verified |
| `2026-11-13-19-historical-events` | Nov 13-19 | Drafted: 28 events (4 per date), source-verified |
| `2026-11-20-26-historical-events` | Nov 20-26 | Drafted: 28 events (4 per date), source-verified |
| `2026-11-27-12-03-historical-events` | Nov 27-Dec 3 | Not started |
| `2026-12-04-10-historical-events` | Dec 4-10 | Not started |
| `2026-12-11-17-historical-events` | Dec 11-17 | Not started |
| `2026-12-18-25-historical-events` | Dec 18-25 | Not started |

Each date targets four or five events: one featured event with notification
copy, plus three or four others. The mix aims for global variety across
regions and eras. No images are included; featured events use the supported
image-free layout.

## Review State

Every draft ledger entry is `source_verified`, not `approved`:

- Claude opened each cited URL and recorded what the page states in the
  source-check note.
- No reviewer or review date is set. Owner review is still required.
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

The canonical bank already holds 150 published questions. That meets the
closed-beta figure of roughly 150 reviewed questions, so no new quiz pack is
part of this runway. Autumn-anniversary questions can come in a later L7 pack
toward the 240 target.

## Promotion Steps (Per Batch)

1. Review each draft event against its ledger note and cited page.
2. Mark the approved ledger entries `approved`, adding `reviewer` and
   `reviewedOn`.
3. Append `draft-events.json` events to `content/events.json`, and
   `draft-daily-events.json` days to `content/daily-events.json`.
4. Run `EditorialReviewCheckCommand` on the batch, then the canonical import
   and coverage report described in `CONTENT_IMPORT_GUIDE.md`.
