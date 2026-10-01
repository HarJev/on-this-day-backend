# Event Image Batches: Active Handoff

Updated: 2026-10-01. Follow `EVENT_IMAGE_PIPELINE.md`. This document is not
content approval: the October 9-15 events are still `source_verified` drafts.

## Cycle: October 9-15

- Branch: `claude/project-thread-ay0nu2` from main `47ed8ce`.
- Candidates: `editorial/event-images/candidates/2026-10-09-15.json` (27
  events in `2026-10-09-15-historical-events`) and
  `2026-10-09-15-supplementary.json` (the King Nobel event in
  `2026-10-09-15-supplementary-events`). The split exists because `attach`
  needs every manifest event in the target events file.
- Owner authorization (2026-10-01, project chat): run the S3 uploads for these
  images. Upload and CloudFront checks run on the owner's Mac as
  `on-this-day-terraform` (`aws login --profile on-this-day`), never in the
  cloud container. Database imports are a separate later step.
- Hard limits: no canonical `content/` edits, no imports, no Daily changes,
  no new cloud resources.

| Stage | Status | Evidence / next action |
| --- | --- | --- |
| I1: pick candidates | COMPLETED | 28 of 31 events. All 7 featured events covered. Licences read from Commons file metadata. |
| I2: prepare and review renditions | NOT_STARTED | Run `prepare` for both candidate files on the Mac, open `review.html`, check crops at phone size. |
| I3: publish to S3 | NOT_STARTED | Dry run, then `--execute` with bucket `on-this-day-media-764574955085`, origin `https://d2v6di8uk52rif.cloudfront.net`. Check each URL returns 200 `image/jpeg`. |
| I4: attach to drafts | NOT_STARTED | `attach` into each batch's `draft-events.json` (not `content/events.json`); commit the publish manifests and set `imageRightsStatus: verified` for attached events. |
| I5: PR review | NOT_STARTED | Separate review thread reviews and merges. |

## Skipped Events

- `outer-space-treaty-enters-force-1967`: the only licensed signing photo has
  unclear provenance (ITU Flickr, no event detail).
- `voskhod-1-launches-1964`: the crew portrait's CC BY-SA claim comes from a
  photo-montage site, not the original photographer.
- `first-oktoberfest-1810`: no licensed image of the 1810 races found; modern
  festival photos would misrepresent the event.

## Reviewer Notes

- `templars-arrested-in-france-1307` uses a manuscript miniature of the 1314
  burning of the Templar leaders, the arrest's outcome. Alt text says so.
- `equatorial-guinea-independence-1968` uses the national flag (PNG rendition
  of the Commons SVG). It is the weakest match; drop it if a ceremony photo
  with clear rights turns up.
- `shenzhou-5-yang-liwei-2003` is CC BY 3.0 from China News Service, with
  the broadcast watermark removed on Commons.
- `che-guevara-executed-1967` uses Korda's 1960 portrait, public domain on
  Commons, a dignified choice for a death event.
- Wikimedia rate-limits the cloud container (HTTP 429, `retry-after: 600`) and
  its 1280px thumbnails, so candidates use original file URLs.
