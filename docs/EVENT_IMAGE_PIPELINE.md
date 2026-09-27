# Event Image Pipeline

This is the reviewable, non-runtime process for adding licensed images to historical events. The Lambda never downloads, resizes, or uploads images.

## Coverage policy

Give every featured event one reviewed primary image first. Then add images to additional events where a strong, directly licensed rendition exists. Do not use generic AI illustrations or weakly related stock imagery for a historical claim.

The initial target is 100% featured-event coverage for the closed-beta runway, then at least 60% coverage across all events. A source, license, crop, and recognizability review is required for every image.

## Candidate JSON

Copy `editorial/event-images/candidates/event-image-candidate.example.json` outside the canonical `content/` tree. Replace every placeholder with a direct HTTPS rendition URL and complete provenance. One candidate manifest may contain one primary image per event.

Use sources with clear reuse terms, such as NASA, Library of Congress, National Archives, Smithsonian, National Park Service, or museum open-access records. An owner must approve rights and the visual choice before publication.

## Prepare and review

Install the isolated editorial dependency:

```sh
python3 -m venv .venv-event-images
.venv-event-images/bin/pip install -r editorial/event-images/requirements.txt

# Use .venv-event-images/bin/python for the commands below.
```

Prepare a weekly batch. This rejects redirects, non-image responses, sources above 20 MiB, decode bombs, and outputs above 1.5 MiB. It creates JPEG renditions at 960 pixels or less, aiming for 750 KiB.

```sh
python3 editorial/event-images/event_image_pipeline.py prepare \
  --candidates editorial/event-images/candidates/2026-10-02-08.json \
  --asset-root build/event-images/2026-10-02-08 \
  --output-manifest build/event-images/2026-10-02-08/publish-manifest.json \
  --review-html build/event-images/2026-10-02-08/review.html
```

Open the generated review page. Check phone-scale recognizability, neutral alt text, dignified crop, and complete provenance. Staged assets below `build/` are not committed.

Validate exact bytes again before publishing:

```sh
python3 editorial/event-images/event_image_pipeline.py validate \
  --manifest build/event-images/2026-10-02-08/publish-manifest.json \
  --asset-root build/event-images/2026-10-02-08
```

## Publish after AWS approval

The private S3/CloudFront Terraform module must be applied and allow `event-images/*` before first publish. Publishing defaults to dry run. Only `--execute` invokes the AWS CLI.

```sh
python3 editorial/event-images/event_image_pipeline.py publish \
  --manifest build/event-images/2026-10-02-08/publish-manifest.json \
  --asset-root build/event-images/2026-10-02-08 \
  --bucket YOUR_PRIVATE_MEDIA_BUCKET \
  --origin https://YOUR_DISTRIBUTION.cloudfront.net

# Repeat with --execute only after reviewing the dry run.
```

Objects use immutable `event-images/<event-id>/<sha256>.jpg` keys and one-year immutable cache headers. Corrections create new keys rather than overwrite an existing rendition.

## Attach and import

After CloudFront liveness is verified, create a proposed events file:

```sh
python3 editorial/event-images/event_image_pipeline.py attach \
  --manifest build/event-images/2026-10-02-08/publish-manifest.json \
  --asset-root build/event-images/2026-10-02-08 \
  --events content/events.json \
  --output build/event-images/2026-10-02-08/events.with-images.json \
  --origin https://YOUR_DISTRIBUTION.cloudfront.net
```

Review the output diff, copy approved entries to `content/events.json`, run the existing canonical event importer from `CONTENT_IMPORT_GUIDE.md`, then check the owned URLs. The tool refuses to overwrite the input event file unless `--write-canonical` is also supplied. The mobile cache already handles ordinary HTTPS image URLs, so no mobile code change is required.

## Automation boundary

The GitHub Action performs syntax checks only. It never fetches source images or uploads assets: external sources rate-limit automation and human rights review is required. Publishing uses a separately approved least-privilege AWS role, not the Lambda.