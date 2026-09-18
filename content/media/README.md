# Reviewed Owned-Image Staging

This directory defines the offline handoff for owned quiz-image delivery. It
does not contain image bytes and it is not an upload destination.

Before publishing an image, an editor obtains the already reviewed rendition
into an untracked local asset directory, creates a strict JSON manifest, and
runs the dry-run command. The manifest must retain the question ID, source
rendition URL, source page, source name, alt text, attribution, optional
creator, license, license URL, content type, dimensions, encoded byte count,
SHA-256 digest, and immutable object key.

The initial nine review records are in `docs/QUIZ_CONTENT_REVIEW.md`; their
canonical provenance remains in `content/quizzes/questions/*.json`. A
production manifest is intentionally absent until the exact locally reviewed
rendition bytes and their checksums are ready. Do not invent checksums or copy
unreviewed assets into this repository.

The dry-run command reads only local bytes and makes no AWS call:

```sh
mvn exec:java \
  -Dexec.mainClass=com.onthisday.ingestion.media.OwnedImagePublishDryRunCommand \
  -Dexec.args="<manifest.json> <local-reviewed-asset-root> [https://future-owned-origin]"
```

It accepts JPEG and PNG renditions up to 8 MiB and a 1024-pixel longest edge,
verifies actual type/dimensions/bytes/SHA-256, then prints the proposed immutable
object keys and long-lived cache headers. The optional origin is only used to
display a proposed URL; it does not modify quiz JSON or upload anything.

After separately approved infrastructure and a successful publication check,
update only `image.url` to the new HTTPS object URL. Keep all source and license
fields unchanged, import idempotently, and use a new checksum-derived key for
every correction. Never overwrite an existing immutable object.
