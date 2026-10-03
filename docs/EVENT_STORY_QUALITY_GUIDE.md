# Event Story-Quality Guide

A correct date and a one-line fact are the floor for an event, not the goal.
Every featured and additional event should leave a reader with something they
did not know and a reason to care. This guide is the story-quality review that
sits between the direct-source check and owner approval in
[Editorial Content Workflow](EDITORIAL_CONTENT_WORKFLOW.md). It applies to new
batches and to audits of existing events.

## Two Surfaces, Two Jobs

- **`summary` is the Today teaser.** One short, concrete sentence that makes
  someone want to open the event. Do not restate the whole story.
- **`description` is the Event Detail read.** A reader should finish it having
  learned something. Use two short sections, separated by a blank line: first,
  explain what happened in plain language; second, explain what led to it or
  why it mattered, including one concrete consequence. A reader with no prior
  background should understand who or what this is, why it happened, and why it
  matters. Name unfamiliar people and say what they did. Keep each section to a
  few concise sentences; do not turn an event into a long article.
- `notificationTitle` and `notificationBody` are a separate contract. Do not
  change them while rewriting copy unless the owner asks.

## The Story Check

Before an event goes to review, its `description` should answer, with a
directly sourced claim for each:

1. **What happened**, in the first section, in plain language and with the exact date.
2. **Why it happened or mattered**, in the second section: the situation or
   turning point that led here, what changed because of it, and one concrete
   consequence or human detail such as a number, place, or person.
3. **A takeaway** the reader can repeat to someone else.
4. Check that a reader with no background can identify unfamiliar people and
   understand why the event happened and why it matters.

Keep both sections short: usually a few sentences each, and never a five-
paragraph article. Do not provide a full war, revolution, or political-history
survey; include only the one or two facts needed to make this event intelligible.
Never pad an obscure event to a target length. If sources only support a short
read, say so in the ledger rather than inventing context.

## Rules For The Copy

- Every added claim must be supported by a source listed on the event. Fetch the
  page and check the sentence; a title or search snippet is not enough. Add new
  source entries for every page that supports an added claim.
- Keep uncertainty and disagreement. If sources differ or a view is contested,
  say what is established and attribute the rest.
- Describe the person or event plainly. Avoid hero language, superlatives the
  source does not make ("greatest", "changed the world"), and filler such as
  "legacy continues to inspire". A specific fact earns its place; an adjective
  does not.
- When a source gives a quotation, keep it short and attributed.
- Include only events with an exact date, or one that most authorities on the
  topic accept. Do not rely on a debated or commemorative-only date.
- Preserve event IDs, daily assignments, featured roles, and notification copy.

## Review Workflow

1. Draft the copy and the new sources.
2. Verify each added claim against its source and record the check in the
   review ledger as a dated `verified_supporting` entry with a note that quotes
   the supporting passage.
3. Set the ledger entry to `source_verified` when copy changes after an
   approval. Only the named reviewer moves it back to `approved`.
4. Run `EditorialReviewCheckCommand` and the validators.
5. Report the batch (what changed, what remains thin, what sources were used)
   for editorial review before promotion. A source check or a passing
   validator is not approval, and approval is not a production import.

## Image Review As Part Of The Story

Each featured event should have one reviewed image (see
[Event Image Pipeline](EVENT_IMAGE_PIPELINE.md)). In addition to rights and
attribution:

- **Hero crop.** The Today hero is shown 16:9.5 (cover) and the Event Detail
  image is shown uncropped. A portrait or square image of a person is cropped
  from the top and bottom on Today, so a face can be cut off. Prefer a landscape
  image where the subject's face sits near the vertical middle or upper third
  (check a 1920 by 1240 style rendition cropped to 16:9.5). Do not change every
  image to top alignment.
- **Relevance.** The image should show the event, its people, or a
  contemporary record, not a generic or later stand-in. Say what it shows in
  neutral alt text.
- **Rights.** Record the item-level source page, creator when known, licence,
  and the basis for any public-domain claim (publication date, jurisdiction).
  Flag an unknown author. An unverified claim stays `unknown`.
- **Fallback.** If no properly licensed image exists, leave the event without
  one and say so in the ledger.

## Audit Report

For an audit of existing events, produce a short report per date batch listing,
for each event: whether the Today teaser works, whether the Event Detail meets
the story check, what sources would support more, and any image problem. Rewrite
the events that need it in the same batch and leave the rest listed as pending.
