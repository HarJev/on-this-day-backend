# Connected Daily Quiz Content Plan

Updated: 2026-10-01. Owner-approved direction; content remains subject to review.

This extends **L6 (event runway), L7 (quiz expansion), and A2 (Connect Today
And Daily)** in `../on-this-day-mobile/docs/PRODUCTION_LAUNCH_WORKPLAN.md`.
Read `CLOSED_BETA_RUNWAY_PLAN.md`, `EDITORIAL_CONTENT_WORKFLOW.md`, and
`CONTENT_IMPORT_GUIDE.md`. Do not treat this plan as editorial approval.

## Product Rule

Make Daily feel like a discovery following today's stories, not homework.
Research forthcoming event batches first, then look for enjoyable quiz hooks.
Quick Play remains a broad global-history bank. A linked question is still
useful independently: do not require the player to have read the article.

- Prefer recognizable people, places, inventions, cultural works, meaningful
  turning points, and clear cause/effect or context.
- Ask about something interesting connected to the event, not an arbitrary
  date, a quotation lifted from its description, or an obscure proper name.
- A contextual question may go beyond the article when independently sourced
  and genuinely related. Record why the relationship reinforces the story.
- Skip tenuous or obscure hooks. Never manufacture a question for every event
  or inflate the bank with paraphrases to reach a quota.
- Keep parallel, plausible distractors in the same conceptual category. Avoid
  correct-answer leakage, absurd alternatives, and length/wording giveaways.
- Keep most new hooks accessible easy/medium; use hard questions selectively.
  These are editorial judgments, not a new scoring or selection algorithm.
- Image identification should be fair at actual phone size and recognizable
  without labels. Reuse approved owned renditions; new assets require rights,
  provenance, mobile fairness, and delivery review. Do not force an image type.

## Fetching Order

1. Audit canonical events, approved relations, published questions, and
   pending editorial drafts. Reuse suitable existing questions by proposing
   reviewed `relatedEventIds` instead of immediately creating more.
2. Prioritize the earliest incomplete upcoming seven-day event window.
   Current runway drafts begin October 2; recheck on every run because other
   workers are preparing events. Do not edit their drafts without coordination.
3. For each featured story, seek at least one good reusable or new question
   when the material supports it. Seek another suitable hook among additional
   events. One or two viable linked candidates per day is a research target,
   **not** a publication minimum; no-link dates are legitimate.
4. Assemble reviewable packs of roughly 20-30 questions across one or more
   windows. Publish fewer when the quality bar demands it. Maintain global,
   era, collection, and type variety; the old 240 target is a checkpoint,
   not a ceiling and not a reason to pad.
5. Open supporting sources directly. Verify the question, correct answer,
   distractor exclusivity, explanation, and relation independently. An
   event's source need not support every broader quiz claim.
6. Keep question drafts, candidate links, and ledgers under `editorial/`.
   Existing canonical questions needing new links also require a review
   record. Mark `source_verified`, not `approved`, until owner sign-off.
7. After approval, promote/import historical events before questions referencing
   them. The imports are separate transactions: audit both outcomes. Use the
   import guide and approved-subset preflight; never invent publication state
   or silently resolve a missing event foreign key.

## Selection And Coverage

The current A2 generator reserves a featured-linked slot in Daily-5 when
possible and at most one additional same-date slot in Daily-10/20. The rest
stays globally balanced. More authored links improve supply and variety;
they do not make every Daily question about today's events. Do not increase
the linked quota, regenerate persisted assignments, or change scoring here.

For each batch report date, featured/additional canonical IDs, existing reusable
questions, new candidate IDs, relation rationale, source/review state, and
reason for intentionally skipping a day. Keep editorial coverage separate
from the actual persisted Daily selection: late imports do not retrofit it.

Review for near-duplicates across packs and repeated questions across upcoming
days. Preserve one canonical question ID when it genuinely relates to multiple
events, rather than cloning it. Report adjacent-day repetition for a future
L7 selection decision; do not change the algorithm inside a content task.

## Ordered Worker Tasks

| Item | Status | Acceptance |
| --- | --- | --- |
| C1: upcoming-date link inventory | COMPLETED | October 2-8 inventory complete; see CONNECTED_QUIZ_CONTENT_PROGRESS.md. Event drafts remain unapproved. |
| C2: linked question slate | COMPLETED | Owner approved the fourteen October 2-8 hooks on 2026-09-29; drafting only, not publication approval. |
| C3: sourced editorial pack | COMPLETED | October 2-8: eight validated drafts, six source-checked reuse links; owner content review pending. See CONNECTED_QUIZ_CONTENT_PROGRESS.md. |
| C4: owner review and promotion | COMPLETED | October 2-8: owner approved 2026-09-29; 28 events, Pack 140 (8 questions) and six reuse links promoted and imported locally. See CONNECTED_QUIZ_CONTENT_PROGRESS.md. |
| October 9-15 C1-C3 (+ Oct 1-8 supplement) | COMPLETED | 19 + 7 new drafts, 10 + 2 reuse links, one supplementary event; owner review (C4) NOT_STARTED. See CONNECTED_QUIZ_CONTENT_PROGRESS.md. |

Set an item IN_PROGRESS when work starts and COMPLETED only with evidence.
Repeat C1-C4 for each window; do not mark the whole runway complete after one
pack. Separate agents may research nonoverlapping batches, but coordinate
canonical promotion/imports to avoid overlapping edits.

## Worker Prompt

Read AGENTS.md, CLAUDE.md, docs/CONNECTED_QUIZ_CONTENT_PLAN.md and the import
guide. Start C1 for the earliest upcoming incomplete event window. Audit
existing published questions and propose reviewed reuse before new questions.
Then present a C2 slate inspired by featured/additional events: recognizable,
fun, independently playable, with plausible parallel distractors. Skip obscure
or forced hooks and report why. Preserve concurrent event-worker changes.
Do not approve, promote, import, alter selection, or create cloud resources.
Update task status before work and after evidence; stop for slate review.
