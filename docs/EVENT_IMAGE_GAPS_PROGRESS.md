# Event Images: Bosworth Self-Host And Remaining Gaps

Started 2026-10-01. Handoff record; resume at the first stage not COMPLETED.

Rules: `AWS_PROFILE=on-this-day` (owner runs `aws login --profile on-this-day`
if expired), existing bucket `on-this-day-media-764574955085`, origin
`https://d2v6di8uk52rif.cloudfront.net`. Never delete S3 objects, no deploys,
no Supabase. Web fetches in the main session only.

## Status

| Stage | Status | Evidence / next action |
| --- | --- | --- |
| G1: worktree | COMPLETED | `claude/event-images-bosworth-gaps` from `9260e65` (PR #37 merged). |
| G2: audit | COMPLETED | Only `battle-of-bosworth-field-1485` (Aug 22) used a non-CloudFront URL. 26 featured events from Oct 21 to Apr 10 and 5 gaps lacked images. |
| G3: pick and prepare | COMPLETED | Candidates `2026-10-01-gaps` (13): Bosworth plus 12 events. `prepare` and `validate` pass; every rendition checked by eye on a contact sheet. |
| G4: upload | COMPLETED | 2026-10-01 as `on-this-day-terraform`: dry run listed only the 13 new `event-images/<event-id>/<sha256>.jpg` keys, then `--execute`. All 13 CloudFront URLs return 200 `image/jpeg` with bytes matching the manifest sha256. Nothing deleted. |
| G5: attach and checks | COMPLETED | Bosworth swapped to the CloudFront rendition (`--replace-primary`, same Commons file); 12 images attached to `content/events.json` without reformatting it (parse identical to the pipeline proposal). `imageRightsStatus: verified` on 12 ledger entries in 9 batches. `EditorialReviewCheckCommand` passes for all 9; `mvn -B test` 271 tests, 0 failures. |
| G6: PR and review handoff | IN_PROGRESS | PR opened; channel session notified. Do not merge from this thread. |
| G7: after merge, local re-import | NOT_STARTED | Pull main (also picks up PR #37: 98 questions, 70 links), historical import, quiz import, `ContentStatusCommand`; report counts. |

## Images Added (12 + Bosworth)

French women's first vote (1945 elector card), last natural smallpox case
(smallpox virus micrograph, CDC), Maastricht Treaty (memorial stone), Doctor
Who (TARDIS prop), Paris climate agreement (April 2016 signing ceremony),
Kyoto Protocol (2005 ratification map), Hank Aaron's 715th (the wall marking
where it landed), Outer Space Treaty (signing, ITU Flickr, CC BY 2.0), Voskhod 1
(the capsule at the Science Museum), first Oktoberfest (Kobell's 1811 painting
of the first horse race), Washington Monument (1890s photo), Argentine seizure of
the Falklands (operation map). Alt text says what each image actually shows.

## Skipped (no properly licensed, relevant image found)

UN Charter in force (only the Russian-language Charter scan), Anglo-Irish
Treaty (only a lone portrait of a secretary), UNICEF (the Pate photo's 1946 date
and its CC licence from an archive photo are doubtful), Monty Python (only a
cropped Bronzino foot), Iron Curtain speech (the Churchill photo's public domain
claim is doubtful), Toy Story, TheFacebook, element 112, Mandela's release,
Treaty of Rome, Nunavut, Good Friday Agreement, the Times Square ball drop (1907),
Waiting for Godot, Beatles rooftop, Miracle on Ice, M*A*S*H, Barbie, Please
Please Me. The 18 featured events from Aug 24 to Sep 26 without an image are
later in the cycle and were not part of this pass.
