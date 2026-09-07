# Curated Quiz Content

Quiz v0.1.0 curated content lives here, but the production question bank is
intentionally empty until Q7.

Q3 provides reader, validation, and import tooling only. The importer reads:

```text
content/quizzes/
  collections.json
  questions/
    *.json
```

The `questions/` directory may be missing or empty. When it is empty, validation
returns a warning that no published quiz questions exist.

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
