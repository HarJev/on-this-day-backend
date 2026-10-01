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
| I1: pick candidates | COMPLETED | 28 of 31 events picked (27 after I2 review dropped UPU). All 7 featured events covered. Licences read from Commons file metadata. |
| I2: prepare and review renditions | COMPLETED | 2026-10-01 on the owner's Mac: `prepare` + `validate` pass for both files (26 main + 1 supplementary JPEGs). Every rendition checked by eye. Dropped the UPU image (see Skipped Events); corrected alt text for Fiji, South African War, Yeager and Washington Monument to match what the photo shows. |
| I3: publish to S3 | COMPLETED | 2026-10-01 as `on-this-day-terraform`: dry run listed only 27 `event-images/<event-id>/<sha256>.jpg` keys, then `--execute` uploaded 27 objects (26 main + 1 supplementary) to `on-this-day-media-764574955085`. All 27 CloudFront URLs return 200 `image/jpeg`. No other objects or resources touched. |
| I4: attach to drafts | COMPLETED | 2026-10-01: `attach` wrote 26 images into `2026-10-09-15-historical-events/draft-events.json` and 1 into `2026-10-09-15-supplementary-events/draft-events.json`. Publish manifests copied to `editorial/event-images/manifests/`. `imageRightsStatus: verified` set for those 27 ledger entries only; `reviewStatus` stays `source_verified`. `ClosedBetaRunwayDraftTest`, `mvn -B test` (198 tests) and `EditorialReviewCheckCommand` on both batches pass. |
| I5: PR review | COMPLETED | 2026-10-01 review thread: licences and alt text checked against Commons for the CC and less obvious PD files; all CloudFront objects match their manifest sha256. Dropped the Washington Monument image (see Skipped Events), leaving 26 attached images (25 main + 1 supplementary). Its S3 object stays in the bucket, unreferenced. |

## Skipped Events

- `washington-monument-opens-to-public-1888`: dropped at I5 review. The 1885
  dedication photo is low resolution, the monument's top is cut off by the
  frame and the print is scratched, so it does not read at phone size. Its
  uploaded object
  (`event-images/washington-monument-opens-to-public-1888/674be651….jpg`) is left in S3
  and referenced nowhere; reuse or remove it in a later owner-approved step.

- `universal-postal-union-founded-1874`: dropped at I2 review. The Commons
  file is Georges Morin's prize-winning competition model (caption printed in
  the image), not the monument actually built in Bern.
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
