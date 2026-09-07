# On This Day API Contract

## Purpose

This document defines the backend contract consumed by the mobile app. Existing
daily-history and device endpoints are the v0.0.1 contract. Quiz endpoints are
an additive v0.1.0 contract and do not change v0.0.1 behavior.

## General Rules

- Base path: `/v1`
- Response body format: JSON
- Dates returned to the app should be display-ready where noted.
- Event IDs are stable strings and must work across home, detail, and
  notifications.
- The app sends an IANA timezone such as `America/Jamaica`.
- The backend resolves the content date and returns the display date; the app
  should not infer it from local time.
- Source URLs and image URLs must be absolute HTTPS URLs unless a local
  development exception is explicitly documented.

## Error Shape

Use one simple error body for public API failures:

```json
{
  "code": "event_not_found",
  "message": "Event not found."
}
```

Recommended status mapping:

| Status | Use |
| --- | --- |
| `400` | invalid request input, including invalid timezone |
| `404` | event ID not found |
| `409` | request conflicts with current registration state, if needed |
| `500` | unexpected server failure |
| `503` | content temporarily unavailable |

Keep user-facing wording generic. Log operational detail server-side.

## Health

```text
GET /v1/health
```

Successful response:

```json
{
  "status": "ok"
}
```

## Get Today Content

```text
GET /v1/days/today?timezone=Area/Location
```

Example:

```text
GET /v1/days/today?timezone=America/Jamaica
```

Successful response:

```json
{
  "date": {
    "month": 8,
    "day": 22,
    "displayDate": "Aug 22"
  },
  "featuredEvent": {
    "id": "battle-of-bosworth-field-1485",
    "title": "Richard III is defeated at the Battle of Bosworth Field",
    "year": "1485",
    "historicalDate": "August 22, 1485",
    "summary": "The battle ended the Wars of the Roses and brought Henry Tudor to the English throne.",
    "notificationTitle": "A king died in battle 541 years ago today",
    "notificationBody": "Richard III's defeat at Bosworth changed England forever.",
    "image": null,
    "dateNote": null
  },
  "additionalEvents": [
    {
      "id": "cook-claims-eastern-australia-1770",
      "title": "James Cook claims eastern Australia for Britain",
      "year": "1770",
      "historicalDate": "August 22, 1770",
      "dateNote": null
    }
  ]
}
```

Rules:

- Return exactly one `featuredEvent`.
- Return zero or more `additionalEvents`, targeting 6-10 when worthwhile
  content exists.
- `featuredEvent` must also be valid for the resolved month/day.
- Empty or missing featured content is a backend/content issue, not normal
  browsing behavior.

## Get Event

```text
GET /v1/events/{eventId}
```

Successful response:

```json
{
  "id": "battle-of-bosworth-field-1485",
  "title": "Richard III is defeated at the Battle of Bosworth Field",
  "year": "1485",
  "historicalDate": "August 22, 1485",
  "summary": "The battle ended the Wars of the Roses and brought Henry Tudor to the English throne.",
  "description": "On August 22, 1485, Richard III was killed at the Battle of Bosworth Field, the decisive clash that ended the Wars of the Roses. His defeat allowed Henry Tudor to become Henry VII, beginning the Tudor dynasty and reshaping English politics for generations.",
  "sources": [
    {
      "name": "Encyclopaedia Britannica",
      "url": "https://www.britannica.com/event/Battle-of-Bosworth-Field"
    }
  ],
  "primaryImage": null,
  "images": [],
  "dateNote": null
}
```

Rules:

- Every returned event must have at least one source.
- `description` should be concise and include what happened and why it matters.
- Approximate or disputed dates should be explained in `description`; `dateNote`
  may carry short metadata when useful.
- `primaryImage` and `images` allow the backend to retain more than one image
  for an event. This is data-model support only; v0.0.1 mobile UI does not
  require or expose an image gallery.

## Image Object

When present, image objects use:

```json
{
  "url": "https://example.com/image.jpg",
  "altText": "Readable description of the image.",
  "source": "Wikimedia Commons",
  "sourceUrl": "https://commons.wikimedia.org/wiki/File:Example.jpg",
  "attribution": "Creator or attribution statement",
  "creator": "Creator name",
  "license": "Public Domain Mark 1.0",
  "licenseUrl": "https://creativecommons.org/publicdomain/mark/1.0/"
}
```

Rules:

- `url` and `altText` are required for any image.
- Source, attribution, creator, license, and license URL should be populated
  when applicable and known.
- Do not include placeholder images.

## Register Device

```text
POST /v1/devices
```

Request:

```json
{
  "token": "fcm-token",
  "platform": "ios",
  "timezone": "America/Jamaica",
  "notificationPermissionStatus": "authorized"
}
```

Successful response:

Status: `200`

```json
{
  "registered": true
}
```

Rules:

- Registration should upsert by token.
- No user account is required.
- `platform` must be one of `ios` or `android`.
- `notificationPermissionStatus` must be one of `authorized`, `provisional`,
  `denied`, or `not_determined`.
- `timezone` must be a valid IANA timezone.
- Store timezone now even if v0.0.1 uses one global notification send time.

## Delete Device

```text
DELETE /v1/devices/{token}
```

Successful response:

Status: `200`

```json
{
  "deleted": true
}
```

Rules:

- Deleting a missing token may be treated as success to keep client cleanup
  idempotent.
- The FCM token is currently part of the path because this contract matches the
  mobile v0.0.1 shape. Replacing it with a separate `deviceId` can be revisited
  later if token exposure in URLs becomes a practical concern.

## Notification Payload

Daily notification sends through Firebase Cloud Messaging should include:

```json
{
  "eventId": "battle-of-bosworth-field-1485"
}
```

The notification title/body come from the featured event's notification fields.

## Quiz v0.1.0 General Rules

Quiz v0.1.0 adds these endpoints:

```text
GET  /v1/quizzes/catalog
POST /v1/quizzes/quick-play
GET  /v1/quizzes/daily?timezone=Area/Location&questionCount=5|10|20
```

Quiz responses intentionally include correct answers, explanations, and
sources. The mobile app grades locally. There is no answer-submission endpoint.

Supported question type values are:

- `multiple_choice`
- `true_false`
- `image_identification`
- `chronological_ordering`

Supported difficulty values are `easy`, `medium`, and `hard`. Difficulty does
not affect scoring.

Supported collection group values are:

- `topic`
- `historical_period`
- `civilization`
- `conflict_or_movement`

Quiz request errors use the existing error shape. Quiz-specific mappings are:

| Status | Code | Use |
| --- | --- | --- |
| `400` | `invalid_quiz_request` | malformed JSON, unsupported question count, or invalid input |
| `400` | `invalid_timezone` | missing, blank, or invalid IANA timezone |
| `400` | `insufficient_quiz_questions` | requested count is unavailable for Mixed or the selected collection |
| `404` | `quiz_collection_not_found` | supplied collection ID does not exist |
| `503` | `quiz_unavailable` | published quiz content or a daily assignment cannot be served |

## Get Quiz Catalog

```text
GET /v1/quizzes/catalog
```

The catalog describes the Mixed selection, stored collections, collection
groupings, published-question counts, supported question counts, and timer
defaults.

Successful response:

```json
{
  "questionCounts": [5, 10, 20],
  "quickPlayTimerDefaultsSeconds": {
    "multipleChoice": 20,
    "trueFalse": 20,
    "imageIdentification": 30,
    "chronologicalOrdering": 45
  },
  "mixed": {
    "publishedQuestionCount": 240,
    "supportedQuestionCounts": [5, 10, 20]
  },
  "collections": [
    {
      "id": "wars-and-conflicts",
      "name": "Wars & Conflicts",
      "group": "topic",
      "publishedQuestionCount": 84,
      "supportedQuestionCounts": [5, 10, 20]
    },
    {
      "id": "punic-wars",
      "name": "Punic Wars",
      "group": "conflict_or_movement",
      "publishedQuestionCount": 12,
      "supportedQuestionCounts": [5, 10]
    }
  ]
}
```

Supported counts are derived from currently published, eligible questions. The
catalog does not promise that unpublished or invalid content can be selected.

## Create Quick Play Quiz

```text
POST /v1/quizzes/quick-play
```

Request for a collection:

```json
{
  "questionCount": 10,
  "collectionId": "punic-wars"
}
```

`collectionId` is optional. When omitted, the backend selects from Mixed:

```json
{
  "questionCount": 5
}
```

Successful response shape:

```json
{
  "mode": "quick_play",
  "questionCount": 5,
  "selection": {
    "collectionId": null,
    "displayName": "Mixed"
  },
  "timer": {
    "mode": "per_question",
    "enabledByDefault": true
  },
  "questions": [
    {
      "id": "capital-of-the-aztec-empire",
      "type": "multiple_choice",
      "difficulty": "easy",
      "prompt": "Which city was the capital of the Aztec Empire?",
      "timeLimitSeconds": 20,
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
      ]
    }
  ]
}
```

The example contains one question for readability. The actual `questions`
array contains exactly the requested count. For multiple-choice and
image-identification questions, option order is generated presentation order;
true/false remains in canonical `True`, `False` order. The mobile client may
disable Quick Play timing without sending that preference to the backend.

## Get Daily Challenge

```text
GET /v1/quizzes/daily?timezone=Area/Location&questionCount=5|10|20
```

Example:

```text
GET /v1/quizzes/daily?timezone=America/Jamaica&questionCount=10
```

Successful response:

```json
{
  "mode": "daily",
  "challengeId": "daily-2026-08-24",
  "date": {
    "isoDate": "2026-08-24",
    "displayDate": "Aug 24"
  },
  "questionCount": 10,
  "assignmentQuestionCount": 20,
  "timer": {
    "mode": "total",
    "durationSeconds": 240
  },
  "questions": []
}
```

The example omits question objects for brevity; they use the shapes below. A
calendar date has one immutable, ordered 20-question assignment worldwide.
Timezone resolves the local date only. Five- and ten-question requests return
stable prefixes of that assignment. Question order and each question's
presentation order are stable for the Daily date.

Daily timer durations are:

| Question count | `durationSeconds` |
| --- | ---: |
| 5 | 120 |
| 10 | 240 |
| 20 | 480 |

The backend does not track whether this is the device's first attempt. Official
attempt and replay state remains local to mobile.

## Quiz Question Objects

All question objects contain `id`, `type`, `difficulty`, `prompt`,
`explanation`, and one or more `sources`. Quick Play questions also contain the
type-specific `timeLimitSeconds`. Daily questions do not need a per-question
limit because the response has one total timer.

The `options` or `items` arrays are in playable presentation order. Correct
answers remain explicit in `correctOptionId` or `correctOrderItemIds`; the
canonical aggregate is not rewritten when presentation order is generated.

### Multiple choice

```json
{
  "id": "capital-of-the-aztec-empire",
  "type": "multiple_choice",
  "difficulty": "easy",
  "prompt": "Which city was the capital of the Aztec Empire?",
  "timeLimitSeconds": 20,
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
  ]
}
```

### True/false

True/false uses the same option model as multiple choice.

```json
{
  "id": "printing-press-before-gutenberg",
  "type": "true_false",
  "difficulty": "medium",
  "prompt": "Movable-type printing existed in East Asia before Gutenberg's press.",
  "timeLimitSeconds": 20,
  "options": [
    {"id": "true", "text": "True"},
    {"id": "false", "text": "False"}
  ],
  "correctOptionId": "true",
  "explanation": "Movable type was developed in China centuries before Gutenberg's work in Europe.",
  "sources": [
    {
      "displayName": "Encyclopaedia Britannica - Printing Press",
      "url": "https://www.britannica.com/technology/printing-press"
    }
  ]
}
```

Source URLs in illustrative contract examples must be verified when the
question enters curated content. Contract examples are not themselves imported
content.

### Image identification

```json
{
  "id": "identify-battle-of-bosworth",
  "type": "image_identification",
  "difficulty": "medium",
  "prompt": "Which battle is depicted in this painting?",
  "timeLimitSeconds": 30,
  "image": {
    "url": "https://upload.wikimedia.org/wikipedia/commons/2/22/Richard_III_at_the_Battle_of_Bosworth.jpg",
    "altText": "Richard III on horseback at the Battle of Bosworth Field",
    "source": "Wikimedia Commons",
    "sourceUrl": "https://commons.wikimedia.org/wiki/File:Richard_III_at_the_Battle_of_Bosworth.jpg",
    "attribution": "Edmund Blair Leighton, Richard III at the Battle of Bosworth Field",
    "creator": "Edmund Blair Leighton",
    "license": "Public Domain Mark 1.0",
    "licenseUrl": "https://creativecommons.org/publicdomain/mark/1.0/"
  },
  "options": [
    {"id": "bosworth", "text": "Battle of Bosworth Field"},
    {"id": "hastings", "text": "Battle of Hastings"},
    {"id": "bannockburn", "text": "Battle of Bannockburn"},
    {"id": "agincourt", "text": "Battle of Agincourt"}
  ],
  "correctOptionId": "bosworth",
  "explanation": "Edmund Blair Leighton's painting depicts Richard III at the Battle of Bosworth Field.",
  "sources": [
    {
      "displayName": "Encyclopaedia Britannica - Battle of Bosworth Field",
      "url": "https://www.britannica.com/event/Battle-of-Bosworth-Field"
    }
  ]
}
```

The image metadata above reuses the currently curated Bosworth image. All
imported image metadata must use real, verified HTTPS URLs, attribution,
creator, and license information.

### Chronological ordering

```json
{
  "id": "order-spaceflight-milestones",
  "type": "chronological_ordering",
  "difficulty": "hard",
  "prompt": "Put these spaceflight milestones in chronological order, earliest first.",
  "timeLimitSeconds": 45,
  "items": [
    {"id": "apollo-11", "text": "Apollo 11 lands on the Moon"},
    {"id": "sputnik-1", "text": "Sputnik 1 reaches orbit"},
    {"id": "gagarin", "text": "Yuri Gagarin orbits Earth"},
    {"id": "valentina-tereshkova", "text": "Valentina Tereshkova orbits Earth"}
  ],
  "correctOrderItemIds": [
    "sputnik-1",
    "gagarin",
    "valentina-tereshkova",
    "apollo-11"
  ],
  "explanation": "The milestones occurred in 1957, 1961, 1963, and 1969 respectively.",
  "sources": [
    {
      "displayName": "NASA History",
      "url": "https://www.nasa.gov/history/"
    }
  ]
}
```

The `items` array is shuffled presentation order. The
`correctOrderItemIds` array is the answer from earliest to latest.
