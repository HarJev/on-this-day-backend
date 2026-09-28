# L7 Third Global History Pack

30 quiz questions drafted and source-checked on 2026-09-28, promoted the same
day on the owner's instruction. The pack is
`content/quizzes/questions/120-l7-third-global-history.json`.

| Type | Count |
| --- | --- |
| Multiple choice | 18 |
| True/false | 6 |
| Chronological ordering | 6 |

Every source was opened in a real browser on 2026-09-28 and confirmed to state
the answer and explanation; the supporting wording is in each
`review-ledger.json` source-check note. Many cite Britannica's dated On This Day
pages. Before promotion every link was rechecked (paced HTTP check, then a
browser load for each blocked page); none were dead.

Checks: `EditorialReviewCheckCommand` passes; `mvn -B test` passes, including
`ProductionQuizContentTest`.

Import on a disposable database first, as described in
`docs/CONTENT_IMPORT_GUIDE.md`.
