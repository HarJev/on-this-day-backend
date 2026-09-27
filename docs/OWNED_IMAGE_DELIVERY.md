# Owned Image Delivery

Quiz images are served from an owned origin. All 21 reviewed
image-identification records (Wikimedia Commons, Library of Congress, and The
Met) point `image.url` at an immutable rendition on
`https://d2v6di8uk52rif.cloudfront.net/quiz-images/`. Source pages,
attribution, creators, licences, and licence URLs remain canonical in the quiz
JSON; each original rendition URL is kept as `sourceRenditionUrl` in
`content/media/quiz-images.manifest.json`. The owned object is only a licensed
rendition used for delivery.

## Offline Review Gate

Use `OwnedImagePublishDryRunCommand` against a local manifest and local staged
bytes. The command requires a SHA-256-derived immutable key and verifies:

- JPEG or PNG content type;
- actual dimensions no greater than 1024 pixels on either edge;
- encoded size no greater than 8 MiB;
- exact declared byte count and SHA-256;
- source, source-page, alt-text, attribution, and license metadata; and
- a safe local path below the supplied asset root.

The command only prints an upload plan. It has no AWS SDK, credentials, upload
path, or network fetch. Invalid bytes or incomplete rights metadata stop the
plan before any publisher could act.

## Live Origin

Terraform (`infra/media`, moving to `infra/prod/media.tf`) manages the private
bucket `on-this-day-media-764574955085` in us-east-1 and CloudFront
distribution `E37FOLDK13W1ZN`, which the owner subscribed to the CloudFront
**Free** flat-rate plan (with its AWS WAF web ACL). The bucket allows only that
distribution to read `quiz-images/*`; there is no public bucket policy, public
write access, or image proxy. Never move the distribution to a paid plan
without an owner decision.

The 21 renditions were published on 2026-09-27. Twelve were downscaled to a
1024-pixel longest edge; nine are the original source bytes. None were
cropped or edited.

Every corrected image receives a new checksum-derived key and
`public, max-age=31536000, immutable`. Existing question IDs, Daily assignments,
source pages, attribution, creators, licenses, and license URLs do not change.
To add or correct an image: stage the rendition, add a manifest entry, run the
dry-run command, have the owner publish the object, verify the HTTPS URL
serves the exact checksum, then update only `image.url`. Never overwrite an
existing object.

The module includes an explicit deny of
`pricingplanmanager:ApprovePaidSubscription`. Attach it to every deployment or
agent role through `deployment_role_names`. Attach the separate write-only
publisher policy only to reviewed roles through `media_publisher_role_names`;
only an owner may make a paid-plan decision outside that automation path.
