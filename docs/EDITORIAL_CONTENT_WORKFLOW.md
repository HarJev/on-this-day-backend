# Editorial Content Workflow

L5 adds a reproducible review and staging path without changing the runtime
content contract. Candidate material and review ledgers live under `editorial/`,
outside importer inputs. Canonical event and quiz JSON remains under `content/`.

```text
candidate -> direct-source check -> human review ledger -> canonical JSON
-> strict validation -> staging import -> coverage report
```

Between the source check and the review ledger, each event gets the story-quality
review in [Event Story-Quality Guide](EVENT_STORY_QUALITY_GUIDE.md). Copy that
changes after an approval returns to `source_verified` until it is re-reviewed.

The workflow never publishes a candidate automatically. A staging batch selects
specific canonical days, event IDs, and quiz pack filenames. Preflight confirms
that every selected item has an approved review record, every canonical source
URL is recorded as `verified_supporting`, and every selected image has verified
rights. It makes no network requests and never substitutes a different URL.

`unknown` and `unreachable` link statuses remain visible in the coverage report.
`verified_bad` is distinct from a link that could not be reached. These states
require editorial resolution before a selected item can pass staging preflight.

The machine-readable coverage report includes all 366 leap-year calendar days,
including February 29; the L4 four-event floor and declared editorial exceptions;
featured notification-copy completeness; potential event duplicates; source and
image-rights review states; published quiz type/difficulty/collection counts;
and explicit adjacent-day quiz overlap from ledger calendar-day tags. Region and
era figures are audit tags, not selection quotas; untagged canonical content is
reported as `unknown` rather than inferred.

Historical and quiz importers remain separate transactions. A successful
historical import is not rolled back if a later quiz import fails, so inspect
both command results and the coverage report before treating a batch as ready.
The workflow does not modify Daily assignments, and missing canonical items are
never deleted by import.

L3's owned-image dry run validates local assets and metadata only. It does not
upload an asset or change a production image URL. Any actual owned-origin upload
is separately approval-gated.

## Post-Audit Review Metadata

The workflow will add explicit related event IDs for approved quiz questions,
pack-level correct-option position reporting, and a featured-image review result
for each curated day. Related IDs are factual editorial assertions: validate the
event exists and that the question meaningfully reinforces it.

Draft, `source_verified`, approved canonical, and imported database states must
remain distinguishable. `ContentStatusCommand` compares ledgers, canonical
files, and an optional target database by counts and deterministic per-record
fingerprints, and reports correct-option positions and featured days without an
image. It reports drift but never approves, promotes, imports, or deletes
content. See `CONTENT_IMPORT_GUIDE.md` section 5 for the command.
