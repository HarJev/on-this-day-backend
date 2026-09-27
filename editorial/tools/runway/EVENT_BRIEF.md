# Drafting brief: On This Day historical-event batch

Repo: /home/claude/on-this-day-backend (Java backend; content is JSON). Today is 2026-09-27.
Do NOT run git commands, do NOT edit anything under content/ or src/, and do NOT call any
`mcp__hearthbot__` tools. Only write the five files in your batch directory (below).

## Read first (skim)
- docs/CONTENT_IMPORT_GUIDE.md, editorial/README.md, docs/SEPTEMBER_CONTENT_REVIEW.md
- Style reference: the events for 09-25 .. 10-01 in content/events.json and content/daily-events.json,
  and editorial/batches/2026-09-25-10-01-historical-events/*.json
- Existing event list (avoid duplicates): content/events.json

## What to produce
For EACH calendar date in your range: exactly 5 events (1 featured + 4 additional) that happened
on that month/day (any year). If you truly cannot find 5 strong, verifiable events for a date,
4 is the floor; never fewer.

Selection: globally varied (not US/UK-heavy; aim for at least 2 regions outside the US/UK per date),
mixed eras (ancient/medieval/early-modern/modern), non-trivial, interesting to a general audience.
Mix politics, science, exploration, culture, arts, sport, social change. Avoid: celebrity trivia,
questionable/legendary dates, events whose date is disputed or uses Julian/Gregorian ambiguity
without a dateNote, gratuitously violent framing, and events that duplicate the existing list.
Featured event: the most broadly resonant, curiosity-provoking, not grim if a strong alternative
exists (holidays like Christmas Day are fine to feature through a real historical event).

## Sources: verification is mandatory
Every event needs 1-2 direct HTTPS sources from authoritative sites (Britannica, national archives,
government history offices e.g. history.state.gov, NASA/ESA, UNESCO, national museums, libraries,
universities, reputable newspapers' archives, official organisations). Wikipedia is a research lead
only, never a cited source. For EVERY source URL you cite, fetch it with WebFetch and confirm the
page actually states the specific date (month/day + year) and the claim. If a URL fails or does not
support the date, find another authoritative source; if none can be verified, DROP the event and
pick another. Never invent or guess URLs. Record honestly what you checked.

## Writing
Original prose, no copied passages. Match existing tone:
- title: short sentence-case headline, e.g. "NASA begins operations".
- year: "1958" (use "79 CE"-style only if existing style does; BCE as e.g. "31 BCE").
- historicalDate: "October 1, 1958".
- dateNote: null, or a short note when calendar/timezone nuance matters.
- summary: one sentence (<= ~140 chars).
- description: 2-3 sentences, starts with "On <historicalDate>, ...", only claims your sources support.
- notificationTitle / notificationBody: ONLY on the featured event (curiosity-driven, concise,
  no clickbait, <= ~60 / ~90 chars); null on additional events.
- sources: [{"name": "Publisher - Page title", "url": "https://..."}]
- images: [] (no images in this batch).
IDs: lowercase slug ending in the year, e.g. "treaty-of-lisbon-signed-2007". Must be unique.

## Files to write in editorial/batches/<BATCH_ID>/
1. draft-events.json   -> {"events": [ ...event objects exactly in canonical schema... ]}
   Field order: id,title,year,historicalDate,dateNote,summary,description,notificationTitle,
   notificationBody,sources,images.
2. draft-daily-events.json -> {"days": [{"month": 10, "day": 2, "featuredEventId": "...",
   "additionalEventIds": ["...", ...]}, ...]} in date order.
3. batch.json -> {"schemaVersion":1,"batchId":"<BATCH_ID>","candidateFile":"candidates.json",
   "reviewLedgerFile":"review-ledger.json","eventIds":[],"monthDays":["10-02",...],"quizPackFilenames":[]}
4. candidates.json -> {"schemaVersion":1,"batchId":..., "candidates":[{"candidateId":"oct-02-<short>",
   "kind":"event","proposedCanonicalId":"<event id>","workingClaim":"<Month D, YYYY>: <claim>",
   "sourceUrls":[...]}]}  (one per event, candidateId format like "oct-02-gandhi-born").
5. review-ledger.json -> {"schemaVersion":1,"batchId":...,"entries":[{"candidateId":...,
   "canonicalId":...,"reviewStatus":"source_verified" (all sources verified) or "candidate",
   "sourceChecks":[{"url":...,"status":"verified_supporting"|"unknown"|"unreachable",
   "checkedOn":"2026-09-27","note":"<what the page states that supports the date/claim>"}],
   "imageRightsStatus":"not_applicable","regions":["..."],"eras":["..."],"calendarDays":["10-02"],
   "notes":"Drafted and source-checked by Claude; awaiting owner editorial review."}]}
   Do NOT set reviewStatus "approved", and do NOT add reviewer/reviewedOn.
   regions vocabulary like: americas, europe, africa, middle-east, asia, oceania, global.
   eras vocabulary like: ancient, medieval, early-modern, modern, contemporary (reuse existing ledger tags where they fit).

Use 2-space-indented JSON. Validate your files parse (python3 -m json.tool) and that every
day references only events in draft-events.json, has no duplicates, and the featured event has
notification copy.

## Final report (your last message, concise)
Per date: featured id + additional ids. Then a list of any events with unverified sources
(status not verified_supporting) and any dates below 5 events, with reason.
