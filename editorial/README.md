# Editorial Workflow Inputs

This directory is deliberately outside `content/`. It holds research candidates,
source checks, human review records, and batch manifests. Nothing here is read by
the runtime API or either canonical importer.

## Lifecycle

1. Record a candidate with its working claim and direct-source URLs.
2. Verify each source directly and record the result in a review ledger.
3. Have a named editor approve the ledger entry.
4. Add the separately reviewed material to canonical JSON under `content/`.
5. Create a batch manifest selecting only those canonical days, events, and quiz
   packs.
6. Run `EditorialReviewCheckCommand`, then the canonical validators, staging
   import, and coverage report.

Candidate status is not publication. Only a manifest whose selected canonical
IDs have `approved` review-ledger entries can be staged by the L5 command.

## Files

Place a batch in a dedicated directory, for example:

```text
editorial/batches/2026-09-example/
  batch.json
  candidates.json
  review-ledger.json
```

`batch.json` selects canonical content and points to the two sidecar files:

```json
{
  "schemaVersion": 1,
  "batchId": "2026-09-example",
  "candidateFile": "candidates.json",
  "reviewLedgerFile": "review-ledger.json",
  "eventIds": ["example-event-1900"],
  "monthDays": ["09-01"],
  "quizPackFilenames": ["questions/100-september-example.json"]
}
```

`candidates.json` keeps research separate from publication inputs:

```json
{
  "schemaVersion": 1,
  "batchId": "2026-09-example",
  "candidates": [
    {
      "candidateId": "example-event-candidate",
      "kind": "event",
      "proposedCanonicalId": "example-event-1900",
      "workingClaim": "A concise, provisional research claim.",
      "sourceUrls": ["https://example.org/primary-source"]
    }
  ]
}
```

`review-ledger.json` is the required human audit record. Source statuses are
`unknown`, `unreachable`, `verified_supporting`, and `verified_bad`.
`unreachable` means the reviewer could not establish the result; it is not
silently treated as bad or replaced. Image rights statuses are `not_applicable`,
`unknown`, `unreachable`, `verified`, and `verified_bad`.

```json
{
  "schemaVersion": 1,
  "batchId": "2026-09-example",
  "entries": [
    {
      "candidateId": "example-event-candidate",
      "canonicalId": "example-event-1900",
      "reviewStatus": "approved",
      "reviewer": "editor-name",
      "reviewedOn": "2026-09-17",
      "sourceChecks": [
        {
          "url": "https://example.org/primary-source",
          "status": "verified_supporting",
          "checkedOn": "2026-09-17",
          "note": "Direct source supports the canonical claim."
        }
      ],
      "imageRightsStatus": "not_applicable",
      "regions": ["example-region"],
      "eras": ["modern"],
      "calendarDays": ["09-01"],
      "notes": "Approved after direct-source review."
    }
  ]
}
```

For approved entries, every source must be `verified_supporting`, and `reviewer`,
`reviewedOn`, and every source-check date are required. Canonical image content
also requires `imageRightsStatus: "verified"`. The ledger records verification;
it does not substitute for source or rights metadata in canonical JSON.
