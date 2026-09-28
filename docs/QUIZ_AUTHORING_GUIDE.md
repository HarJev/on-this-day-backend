# Quiz Authoring Guide

This is the manual and agent-facing guide for adding reviewed quiz content.
Canonical quiz packs live under `content/quizzes/questions/`; research notes,
candidates, and human review records belong under `editorial/` and are not
runtime inputs.

## Current Marker - 2026-09-26

- L6 historical-event content is reviewed and committed through October 1.
- The canonical working tree contains 120 published questions: the original
  bank, three still-uncommitted Q8 packs, and the committed 24-question Pack 100.
- The local project database was last verified with those 120 questions after
  Pack 100 import. Recheck its fingerprint before relying on that statement.
- Pack 110 contains 30 `source_verified` editorial drafts. It is not canonical,
  approved, imported, or part of the published count.
- Preserve and review every dirty pack before starting another numbered pack.
- Public v1 targets 240 reviewed questions: 144 multiple choice, 36 true/false,
  36 image identification, and 24 chronological ordering.
- Recompute type, difficulty, region, era, collection, and correct-position
  distribution from canonical content before allocating the next pack. Do not
  copy a stale total from this guide into a release claim.

Historical events are useful question leads, but they are not automatically
approved quiz facts. Every prompt, answer, distractor, explanation, date, and
image-rights claim must survive direct-source and editorial review.

## File Envelope

Create one reviewable pack rather than editing an unrelated pack. Use the next
available numeric prefix only after checking the working tree.

```json
{
  "schemaVersion": 1,
  "questions": []
}
```

Question, option, item, and collection IDs use lowercase slug form, such as
`identify-frederick-douglass`. Supported values are:

- `type`: `multiple_choice`, `true_false`, `image_identification`, or
  `chronological_ordering`
- `difficulty`: `easy`, `medium`, or `hard`
- `publicationState`: `draft`, `published`, or `retired`
- `collectionIds`: IDs already present in
  `content/quizzes/collections.json`
- `relatedEventIds` (optional): stable event IDs from `content/events.json` that
  the question genuinely reinforces. The quiz import fails, listing each ID, if
  a related event has not been imported yet, so import events first.

Every published question needs a nonblank explanation, at least one directly
supporting HTTPS source, and at least one collection membership. Unknown JSON
fields are rejected by the strict reader.

## Multiple Choice

Multiple choice uses exactly four options and exactly one referenced correct
option.

```json
{
  "id": "capital-of-aztec-empire",
  "type": "multiple_choice",
  "difficulty": "easy",
  "publicationState": "published",
  "prompt": "Which city was the capital of the Aztec Empire?",
  "options": [
    {"id": "tenochtitlan", "text": "Tenochtitlan"},
    {"id": "cusco", "text": "Cusco"},
    {"id": "teotihuacan", "text": "Teotihuacan"},
    {"id": "tikal", "text": "Tikal"}
  ],
  "correctOptionId": "tenochtitlan",
  "explanation": "Tenochtitlan was the political and ceremonial center of the Aztec Empire.",
  "sources": [
    {
      "displayName": "Encyclopaedia Britannica - Tenochtitlan",
      "url": "https://www.britannica.com/place/Tenochtitlan"
    }
  ],
  "collectionIds": ["ancient-history", "society-culture-and-ideas"]
}
```

Distractors must be plausible enough to make the question interesting but
clearly wrong under the wording used. Avoid trick wording, overlapping answers,
and options that differ only because one is much more specific.

The prompt must not reveal the answer through a repeated country, movement,
person, or title. Prefer distractors from the same semantic class, era, region,
or role at comparable specificity. Avoid one option that is uniquely long,
technical, famous, or grammatically compatible. The correct answer is identified
by `correctOptionId`; vary its authored position across the pack even though the
playable API shuffles options. True/false is the deliberate exception.

The validator rejects a prompt or image alt text that repeats the complete correct
choice after case and punctuation normalization. This is a narrow mechanical guard;
human review still catches subtler answer hints.

## True Or False

True/false always has the two canonical options below, in this order. Do not
replace them with Yes/No or custom labels.

```json
{
  "id": "roman-republic-before-empire",
  "type": "true_false",
  "difficulty": "easy",
  "publicationState": "published",
  "prompt": "The Roman Republic existed before the Roman Empire.",
  "options": [
    {"id": "true", "text": "True"},
    {"id": "false", "text": "False"}
  ],
  "correctOptionId": "true",
  "explanation": "Rome's republican period began centuries before Augustus established imperial rule.",
  "sources": [
    {
      "displayName": "Encyclopaedia Britannica - Ancient Rome",
      "url": "https://www.britannica.com/place/ancient-Rome"
    }
  ],
  "collectionIds": ["ancient-history"]
}
```

Prefer statements that test a meaningful misconception. Avoid changing one
minor word solely to manufacture a false statement.

## Image Identification

Image identification uses exactly four options plus one complete image object.
The neutral alt text must describe what is visibly present without naming or
revealing the answer.

```json
{
  "id": "identify-historical-person-example",
  "type": "image_identification",
  "difficulty": "medium",
  "publicationState": "published",
  "prompt": "Which historical figure is shown in this portrait?",
  "image": {
    "url": "https://upload.wikimedia.org/path/to/reviewed-rendition.jpg",
    "altText": "Black-and-white head-and-shoulders portrait of a seated nineteenth-century figure",
    "source": "Wikimedia Commons",
    "sourceUrl": "https://commons.wikimedia.org/wiki/File:Reviewed_file.jpg",
    "attribution": "Exact attribution from the reviewed source page",
    "creator": "Known creator, or omit this field when genuinely unknown",
    "license": "Public Domain Mark 1.0",
    "licenseUrl": "https://creativecommons.org/publicdomain/mark/1.0/"
  },
  "options": [
    {"id": "correct-person", "text": "Correct Person"},
    {"id": "plausible-person-two", "text": "Plausible Person Two"},
    {"id": "plausible-person-three", "text": "Plausible Person Three"},
    {"id": "plausible-person-four", "text": "Plausible Person Four"}
  ],
  "correctOptionId": "correct-person",
  "explanation": "Identify the person and explain why the figure matters in one or two sourced sentences.",
  "sources": [
    {
      "displayName": "Institutional biography supporting the identity and explanation",
      "url": "https://example.org/direct-supporting-biography"
    }
  ],
  "collectionIds": ["leaders-and-power"]
}
```

Do not identify a person from an uncertain, disputed, fictionalized, or merely
traditional likeness without saying so. Favor clearly identified photographs,
portraits held by reputable collections, sculptures only when the prompt names
the medium, and globally varied figures. Match distractors by era, region, or
role so the answer is not obvious from visual demographics alone.

Before publication, retain a direct rendition URL that returns an image without
a redirect, plus its source page; verify the creator and license; and review the
exact bytes. The offline owned-image gate accepts JPEG or PNG renditions up to
8 MiB and 1024 pixels on the longest edge. Before release, run the separate
`QuizImageLivenessAuditCommand` against canonical content; it checks remote
HTTP delivery without adding network work to imports or ordinary tests. Follow
`content/media/README.md` and `docs/OWNED_IMAGE_DELIVERY.md`; do not invent
checksums or claim an owned URL before infrastructure is approved.

## Chronological Ordering

Ordering uses exactly four items. `items` is the initial presentation list;
`correctOrderItemIds` contains the exact earliest-to-latest answer.

```json
{
  "id": "order-spaceflight-milestones",
  "type": "chronological_ordering",
  "difficulty": "hard",
  "publicationState": "published",
  "prompt": "Put these spaceflight milestones in chronological order, earliest first.",
  "items": [
    {"id": "milestone-a", "text": "First milestone"},
    {"id": "milestone-b", "text": "Second milestone"},
    {"id": "milestone-c", "text": "Third milestone"},
    {"id": "milestone-d", "text": "Fourth milestone"}
  ],
  "correctOrderItemIds": [
    "milestone-a",
    "milestone-b",
    "milestone-c",
    "milestone-d"
  ],
  "explanation": "State the four verified dates in order and briefly connect the sequence.",
  "sources": [
    {
      "displayName": "Direct source supporting the dated sequence",
      "url": "https://example.org/direct-supporting-timeline"
    }
  ],
  "collectionIds": ["science-and-innovation"]
}
```

Verify all four dates independently when one source does not support the whole
sequence. Do not rely on approximate dates that could change the order. Avoid
submitting the `items` array already in correct order unless a later presentation
shuffle is deliberately verified.

## Manual Authoring Workflow

1. Check `git status` and list existing pack filenames. Do not overwrite dirty
   or uncommitted packs.
2. Create one bounded pack under `content/quizzes/questions/`, normally 20-30
   questions for L7.
3. Use historical events only as inspiration. Open a direct institutional or
   authoritative source for every retained fact and answer.
4. Draft the prompt, answer, distractors, explanation, sources, difficulty, and
   collection membership. Keep the language understandable without making the
   question childish.
5. For images, inspect the exact source page and rendition, verify rights and
   mobile bounds, and record all provenance fields. Image rights review is a
   separate requirement from factual sourcing.
6. Add candidate and review-ledger entries under `editorial/`. Keep entries
   `source_verified` until a human editor approves them.
7. Run the quiz importer/validator against a disposable database, inspect all
   four question shapes through Quick Play, and rerun the import to prove
   idempotency.
8. Commit only the reviewed pack, its review records, and any deliberately
   updated distribution report. Production import and deployment require
   separate approval.

The command for a canonical local quiz import is documented in
`docs/CONTENT_IMPORT_GUIDE.md`. Do not hand-edit quiz tables with SQL.

## Editorial Checklist

- The source directly supports the correct answer and explanation.
- The prompt has one unambiguous interpretation.
- Every distractor is plausible but demonstrably incorrect.
- Difficulty reflects the reasoning or knowledge required, not obscure wording.
- The explanation teaches something beyond repeating the answer.
- Names, dates, translations, colonial framing, and sensitive violence receive
  an explicit editorial pass.
- Region, era, question type, difficulty, and collection coverage improve the
  bank rather than duplicating a recent question.
- Record stable related event IDs when a question genuinely reinforces a
  curated event. Do not add a loose relation merely because subjects overlap.
- The prompt does not repeat or strongly telegraph the answer.
- Distractors are parallel in category and specificity and remain plausible to
  a reader who recognizes the general subject.
- Correct-option positions vary across multiple-choice and image questions;
  position never identifies correctness.
