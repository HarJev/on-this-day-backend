# L7 Third Global History Pack (Draft)

30 new quiz questions drafted 2026-09-28 and awaiting owner review. The pack
lives outside `content/`, so no importer reads it yet.

| Type | Count |
| --- | --- |
| Multiple choice | 18 |
| True/false | 6 |
| Chronological ordering | 6 |

No image questions: those need a separate image-rights review.

Every source was opened in a real browser on 2026-09-28 and confirmed to state
the answer and explanation; the exact supporting wording is in each
`review-ledger.json` source-check note. Many cite Britannica's dated On This Day
pages. Ledger entries are `source_verified`, with no reviewer.

Checks run: merged with canonical content, `ValidateMerged` reports quiz valid;
`EditorialReviewCheckCommand` passes; with the pack temporarily copied into
`content/quizzes/questions/`, `ProductionQuizContentTest` and
`QuizContentValidatorTest` pass.

## Promote after review

1. Review each question against its ledger note and source.
2. Set approved entries to `approved` with `reviewer` and `reviewedOn`; drop or
   fix any you reject.
3. Move `draft-questions/120-l7-third-global-history.json` to
   `content/quizzes/questions/`, and list it in `batch.json`
   `quizPackFilenames` as `questions/120-l7-third-global-history.json`.
4. Run `EditorialReviewCheckCommand` on this batch, then the quiz import on a
   disposable database as described in `docs/CONTENT_IMPORT_GUIDE.md`.
