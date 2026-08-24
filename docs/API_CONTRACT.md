# On This Day API Contract v0.0.1

## Purpose

This document defines the backend contract consumed by the mobile app for the
first release. The API intentionally supports only the daily discovery loop.

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

```json
{
  "registered": true
}
```

Rules:

- Registration should upsert by token.
- No user account is required.
- `platform` should be one of `ios`, `android`, or another documented client
  platform if added later.
- Store timezone now even if v0.0.1 uses one global notification send time.

## Delete Device

```text
DELETE /v1/devices/{token}
```

Successful response:

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
