# Featured-Story Quiz Links: October 1 to December 25 and Later Gaps

Updated: 2026-10-01. Requested by the owner on 2026-10-01 ("more linked questions
for the events in the near future, today onwards"). Follows
`CONNECTED_QUIZ_CONTENT_PLAN.md` and the formats of PRs #29 to #32.

## Status

| Step | Status | Evidence |
| --- | --- | --- |
| F1: measure coverage | COMPLETED | 18 of the next 90 Daily Challenges (from Oct 1) had a published question linked to the day's featured event. Oct 16 to Dec 25 had no linked questions at all. |
| F2: research and source checks | COMPLETED | Every new question's source was opened in the authoring session on 2026-10-01; what each page states is recorded in its ledger source check. |
| F3: drafts and links | COMPLETED | `editorial/tools/connected-quiz/build_featured_links_cycle.py` writes `editorial/batches/2026-10-01-12-25-connected-quiz/`: 98 new questions (proposed pack `200-connected-featured-stories.json`) and 70 new relations on 57 unchanged published questions. |
| F4: checks | COMPLETED | Canonical validators on content plus the drafts (`merge.py`, `ValidateMerged.java`): events valid, quiz valid, only the pre-existing small ancient-rome warning. `EditorialReviewCheckCommand` passes. `promote_featured_links.py --check`: all 57 link snapshots match and apply cleanly. `mvn -B test`: 266 tests, 0 failures. |
| F5: review and publish | COMPLETED | Reviewed 2026-10-01 by a separate review thread. Two fixes made by the author (Wright brothers distractors; armistice signing time removed). Promoted with `promote_featured_links.py --reviewer "Claude (owner review delegated by Jevaun Harris)"`: pack 200 publishes 98 questions and 57 existing questions gain 70 links. `mvn -B test`: 271 tests, 0 failures. |
| F6: local import | NOT_STARTED | After merge, the Mac session pulls main and runs the quiz import. Nothing was imported or deployed from this thread. |

## Coverage

Counted from `content/daily-events.json` with only published questions, as the
Daily Challenge generator does (`DailyQuizService`: slot 1 takes a question linked
to the featured event, slot 6 one linked to another event of the day).

| Measure | Before | After promotion |
| --- | --- | --- |
| Next 90 days (Oct 1 to Dec 29) with a featured-story question | 18 | 90 |
| All 218 published days with a featured-story question | 135 | 216 |
| Next 90 days with a second, other-event linked question | 19 | 49 |

The two remaining days are September 18 and 28, which next come round in 2027.

## What Was Added

- One to three new questions for every featured event from Oct 16 to Dec 25 that
  had none, plus Oct 1, Jan 11, 16 and 19, and Aug 22, 25 to 29.
- Reuse first: pack 120 already held unlinked questions written for many of these
  days (Persons Case, Tasman, Madero, Naismith, Barnard, James Webb and others).
  They are linked as they stand, with no wording change.
- Additional-event links for Daily slot 6 where an existing question genuinely
  fits (Alaska transfer, Cortes in Tenochtitlan, Ford's assembly line, Pearl
  Harbor, Munch, Austen, Champollion and others).

## Notes For The Reviewer

- `order-world-war-milestones` would have fitted the Armistice but is retired, so
  it was not linked; two new Armistice questions cover the day instead.
- Some ordering questions now link several days: `order-film-milestones` (Nov 18,
  22, 26), `order-political-turning-points` (Oct 30, Nov 7, 9, 17),
  `order-peace-treaties` (Oct 24, Nov 21, 27), `order-space-exploration-firsts`
  (Nov 12, Dec 7) and `order-science-discoveries` (Nov 8, Dec 2). Every featured
  day that uses one also has a new question, so the same ordering question is not
  guaranteed to open several Dailies in one week; a repeat is still possible.
- Calder Hall is called the first commercial nuclear power plant, as World Nuclear
  News puts it; the question avoids the earlier Soviet grid-connected Obninsk claim.
- Britannica's Ataturk page does not state the October 29 date; the reused question
  only asks who became the republic's first president in 1923.
- Sources that refused scripted fetches (403) were not used: the Tenement Museum
  subway page, IWM's armistice and Paris pages, ESA's Philae page and the Museums
  of History NSW page. Replacements were opened and cited instead.
- Reused questions' own sources were not re-fetched unless a check note says so.

## Resume Notes

If work stops mid-review: rerun the build script from the repository root (it is
deterministic and rewrites the batch), then `promote_featured_links.py --check`.
Do not run the promotion twice; it refuses if pack 200 already exists.
