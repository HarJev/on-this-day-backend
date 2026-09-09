# Curated Quiz Content

Quiz v0.1.0 curated content lives here. Q7 supplies the initial reviewed bank:
60 published questions in six independently reviewable packs.

Q3 provides reader, validation, and import tooling only. The importer reads:

```text
content/quizzes/
  collections.json
  questions/
    *.json
```

The reader still permits a missing or empty `questions/` directory for tooling
and tests; validation then warns that no published quiz questions exist.

The final distribution, source review, sensitive-content decisions, and image
licenses are recorded in `docs/QUIZ_CONTENT_REVIEW.md`.

## Collections

```json
{
  "schemaVersion": 1,
  "collections": [
    {
      "id": "ancient-history",
      "name": "Ancient History",
      "group": "historical_period"
    }
  ]
}
```

Supported groups are `topic`, `historical_period`, `civilization`, and
`conflict_or_movement`.

## Question Packs

Each regular `*.json` file under `questions/` contains:

```json
{
  "schemaVersion": 1,
  "questions": [
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
      "collectionIds": ["ancient-history"]
    }
  ]
}
```

True/false questions use the same option model with exactly `true` / `True` and
`false` / `False`. Image-identification questions require one complete `image`
object with URL, alt text, source, source URL, attribution, optional creator,
license, and license URL. Chronological-ordering questions use exactly four
`items` plus `correctOrderItemIds`.
