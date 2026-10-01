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
| R6: pull request | COMPLETED | PR #32 reviewed in a separate thread and merged to main as `6b832bf` with no changes requested. |
| R7: local DB import | COMPLETED | From main `6b832bf`, local Docker Postgres only (migrations already current): historical import, then quiz import, both successful. Before: 454 events, 109 days, 230 questions (229 published), 89 relations, 71 event images. After: 988 events, 218 days, 598 questions (597 published), 535 relations, 71 event images. Daily challenges unchanged: 11 challenges, 220 assigned questions. |
| R8: verification | COMPLETED | `ContentStatusCommand` against the DB: `inSync: true`, no stale, missing or extra events, days or questions. SAM local was not running, so no API check this time (PENDING). |
| I1: pick images | COMPLETED | Featured events first: 180 featured events from Oct 16 to Apr 12 lacked an image, plus 8 known gaps. Licences and descriptions read from Commons file metadata. Three batches: `2026-10-16-12-25` (64, including the gaps), `2026-12-26-02-29` (58), `2027-03-01-04-12` (37). |
| I2: prepare and review | COMPLETED | All three batches: `prepare` + `validate` pass and every rendition was checked by eye on contact sheets. Swaps and drops at review are in Image Notes. Final counts: 64, 56 and 35 images, plus a 3-image supplement (`2026-11-13-15-supplement`: Stevenson, Ruby Bridges, the League's first Assembly) found on a recount: 158. |
| I3: upload to S3 | COMPLETED | 2026-10-01 as `on-this-day-terraform` after the owner renewed the sign-in: dry runs listed only `event-images/<event-id>/<sha256>.jpg` keys, then `--execute` uploaded 155 objects (and the 3 supplement objects the same way) to `on-this-day-media-764574955085`. All 158 CloudFront URLs return 200 `image/jpeg` with bytes matching the manifest sha256. No other objects or resources touched. |
| I4: attach and PR | IN_PROGRESS | Batch 1 (`claude/event-images-oct16-dec25`, PR #33): 64 + 3 supplement images attached to `content/events.json` without reformatting it (the pipeline's `attach` proposal, then a text-preserving copy checked to parse identically), manifest copied to `editorial/event-images/manifests/`, `imageRightsStatus: verified` on the 67 ledger entries across 13 batches. Review checks for those batches pass; `mvn -B test` 266 tests, 0 failures. Batch 2: PR #34 (56). Batch 3: PR #35 (35). |
| I5: re-import | NOT_STARTED | After each merge: pull main, historical import, `ContentStatusCommand` against the DB. |

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
`docs/EVENT_IMAGE_PIPELINE.md`. Status is recorded in the table above.

Quiz images: all 36 image-identification questions already have images, and
the new packs are text questions, so no question needs an image.

### Image Notes

- Wikimedia returns HTTP 429 when `prepare` downloads a batch back to back, so
  downloads were paced and cached (a wrapper around the pipeline's own
  `download`, keeping its no-redirect, image-type and 20 MiB checks). Sources
  above 8 MiB or 6000 px use Commons' 1600 px rendition.
- Swapped at review: Tutankhamun (a blurry Carter and Callender photo for
  Burton's photo of Carter at the coffin), the heart transplant (a group photo
  where Barnard was not identifiable), and both Peanuts events (Schulz drawing
  Charlie Brown, a copyrighted character, for Commons' version with the
  drawing pixelated).
- Notre-Dame (Dec 8): the first image centred on the 2024 bronze high altar by
  Guillaume Bardet, a living artist; France has no freedom of panorama, so PR #33
  review replaced it with a CC0 photo of the rebuilt spire
  (`2026-12-08-notre-dame-replacement`, new S3 key; the old object stays
  unreferenced in the bucket).
- Rosa Parks (Dec 1, 1955) uses the AP photo of her being fingerprinted after
  her February 1956 arrest; the alt text says so.
- Dropped at review in batches 2 and 3: Times Square (a night shot of
  billboards, not the ball), Hank Aaron (a 1974 team photo whose public domain
  claim is doubtful) and the Iron Curtain speech (a Leiden ceremony where
  Churchill is hard to see). The German Empire image was swapped for Anton von
  Werner's well-known 1885 version.
- Kept with a note: the Luna 17 image is a drawing taken from a Soviet stamp;
  Tasman's New Zealand sighting uses Gilsemans's drawing of Murderers' Bay from
  five days later; Deep Blue uses a cabinet like the one Kasparov played.

### Featured Events Still Without An Image

No image with clear rights and a direct link to the event was found for 26
featured events: French women's first national vote, the UN coming into
existence, the last natural smallpox case, the EU's birth (Maastricht), Toy
Story, Doctor Who, the Anglo-Irish Treaty, UNICEF, the Paris climate agreement,
the Times Square ball drop, Waiting for Godot (the 1953 production photos'
public domain claim is doubtful), the Beatles' rooftop concert, TheFacebook,
element 112, Mandela's release, the Kyoto Protocol, the Miracle on Ice,
M*A*S*H, the Iron Curtain speech, Barbie, Please Please Me, the Treaty of Rome,
Nunavut, the Falklands invasion, Hank Aaron's 715th home run and the Good
Friday Agreement. Known gaps still open: the Outer Space Treaty,
Voskhod 1, the first Oktoberfest and Monty Python. These days use the
supported image-free layout.

## Not Done

- No Supabase, shared or production import. No deployment.
- Existing Daily assignments are not retrofitted.
- `editorial/tools/runway/b1226.py`, `b0201.py`, `b0308.py` and the three
  connected-quiz generators would recreate the removed draft files if rerun;
  they are now a historical record of the drafts.
