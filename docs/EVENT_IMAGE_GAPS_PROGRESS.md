# Event Images: Bosworth Self-Host And Remaining Gaps

Started 2026-10-01. Handoff record; resume at the first stage not COMPLETED.

Rules: `AWS_PROFILE=on-this-day` (owner runs `aws login --profile on-this-day`
if expired), existing bucket `on-this-day-media-764574955085`, origin
`https://d2v6di8uk52rif.cloudfront.net`. Never delete S3 objects, no deploys,
no Supabase. Web fetches in the main session only.

## Status

| Stage | Status | Evidence / next action |
| --- | --- | --- |
| G1: worktree | COMPLETED | `claude/event-images-bosworth-gaps` from `66e3abb`. |
| G2: audit | COMPLETED | Only `battle-of-bosworth-field-1485` (Aug 22) uses a non-CloudFront URL (Leighton painting, PD). 26 Oct-Apr featured events and 5 gaps (Outer Space Treaty, Voskhod 1, first Oktoberfest, Monty Python, Washington Monument) lack images. 18 Aug 24 to Sep 26 featured events also lack images (out of scope for now). |
| G3: Bosworth candidate | NOT_STARTED | Candidate batch `2026-10-01-bosworth-gaps.json` with the same Commons file; prepare, validate, publish, attach (replace the external URL). |
| G4: gap candidates | NOT_STARTED | Search Commons for the 31 events, soonest date first (Oct 21 onward). Skip any without clear rights. Prior helpers: `prepare_paced.py`, `attach_text.py`, `ledger_rights.py` in the bebca073 session scratchpad. |
| G5: upload, attach, ledger rights, checks | NOT_STARTED | |
| G6: PR, cross-session handoff for review | NOT_STARTED | |
| G7: after merge, local Docker re-import and counts | NOT_STARTED | |
