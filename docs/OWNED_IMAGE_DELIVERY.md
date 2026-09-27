# Owned Image Delivery

L3 prepares, but does not deploy, the public-release image path. The reviewed
quiz bank currently contains nine Wikimedia Commons image-identification
records. Their original URLs and complete provenance remain canonical content;
the owned object is only a licensed rendition used for delivery. Event imagery
uses the same private delivery path under `event-images/*`; its candidate,
preparation, publication, and attachment workflow is documented in
[Event Image Pipeline](EVENT_IMAGE_PIPELINE.md).

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

## Future Publication

The unapplied `infra/media` module describes a private S3 origin and a
pay-as-you-go CloudFront distribution with OAC. Its bucket allows only the
specific distribution to read `quiz-images/*` and `event-images/*`; no public bucket policy, public
write access, image proxy, or automatic flat-rate subscription is present.

Every corrected image receives a new checksum-derived key and
`public, max-age=31536000, immutable`. Existing question IDs, Daily assignments,
source pages, attribution, creators, licenses, and license URLs do not change.
Do not update `image.url` until a separate owner approval permits applying the
reviewed plan, publishing assets, and verifying the owned HTTPS origin.

The module includes an explicit deny of
`pricingplanmanager:ApprovePaidSubscription`. Attach it to every deployment or
agent role through `deployment_role_names`. Attach the separate write-only
publisher policy only to reviewed roles through `media_publisher_role_names`;
only an owner may make a paid-plan decision outside that automation path.