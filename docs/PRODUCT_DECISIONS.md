# On This Day — Product Decision Log

## PD-001 — The product is centered on one featured historical event per day

**Decision**  
For each calendar date, the product will select exactly one historical event as the day's featured event.

**Rationale**  
The product should give the user an obvious place to begin rather than asking them to choose among many historical events. The core experience is intended to answer: “What important thing happened on this day in history?”

**Implications**  
- The home screen must visually distinguish one featured event.
- Other events from the same date are secondary.
- The featured event becomes the basis for the daily notification.  

## PD-002 — v0.0.1 is designed to validate repeat daily usage

**Decision**  
The primary product question for v0.0.1 is whether users will return regularly to discover the day's historical event.

**Rationale**  
The first release should validate the core daily-history habit before investing in broader functionality.

**Implications**  
Features that do not materially support the daily discovery experience are excluded from v0.0.1. 

## PD-003 — The experience should prioritize concise historical understanding

**Decision**  
Historical content will be designed for short sessions rather than long-form study.

**Rationale**  
A user should be able to quickly understand what happened and why it mattered without the app attempting to replace Wikipedia, books, documentaries, or other long-form resources.

**Implications**  
- Event descriptions should generally be one concise paragraph.
- The app provides context and significance but directs users elsewhere for deeper reading.  

## PD-004 — External sources are part of the core event experience

**Decision**  
Every event must provide at least one external source or read-more link.

**Rationale**  
Users should be able to verify the information and continue learning without the application reproducing full source material.

**Implications**  
- Sources are required canonical content for every event.
- Event detail screens must expose source links.
- Source attribution must make the destination understandable.  

## PD-005 — Daily notifications will use curiosity-driven copy

**Decision**  
The daily notification may use wording different from the formal event title and should create curiosity around the featured event.

**Rationale**  
The notification is intended to encourage the user to open the app rather than simply announce the event, while avoiding misleading or clickbait wording.

**Implications**  
- Featured events require notification-specific title and body content.
- Notification copy is a separate content concern from the normal event title and summary.  

## PD-006 — The daily notification deep-links directly to the featured event

**Decision**  
Tapping the daily notification will open the event detail view for the event referenced by that notification.

**Rationale**  
The notification should provide the shortest possible path from curiosity to the historical content that generated it.

**Implications**  
- Notifications must include or reference the destination event ID.
- Deep linking must work whether the application is closed or already running.  

## PD-007 — Normal app launches always begin with today

**Decision**  
Opening the application normally will display the current day's home screen rather than a previously viewed event.

**Rationale**  
The product is fundamentally organized around today's date and today's historical discovery.

**Implications**  
The application must detect calendar-date rollover and replace the previous day's content when appropriate.  

## PD-008 — The home screen includes a curated set of additional events

**Decision**  
In addition to the featured event, the home screen will target approximately 6–10 additional notable events from the same calendar date.

**Rationale**  
Users should have more history to explore when interested, without turning the experience into an overwhelming or exhaustive list.

**Implications**  
- Fewer than 6–10 events are acceptable when stronger events are unavailable.
- Weak events should not be added simply to meet a numerical target.
- The product does not attempt to display every event associated with a date.  

## PD-009 — Featured-event selection is editorial for v0.0.1

**Decision**  
The featured event for each date will be manually/editorially selected rather than chosen by an automated ranking system.

**Rationale**  
The first release does not require the complexity of automated significance scoring.

The principal editorial factors are:
- long-term historical impact
- number of people affected
- recognizability
- overall historical significance

**Implications**  
- Featured selections can be prepared as part of the product's content set.
- Automated ranking or scoring is not required for v0.0.1.
- The same event may remain featured on that date every year.  


## PD-010 — Historical events have stable internal identities

**Decision**  
Every historical event will have a stable event identifier.

**Rationale**  
The same event must be consistently referenced across the home screen, event detail view, and notifications.

**Implications**  
Event ID is required canonical content for every event.  

## PD-011 — Historical imagery is optional

**Decision**  
Events may include historical imagery, but images are not required for an event to exist, appear in the application, or be selected as the featured event.

**Rationale**  
Useful historical events should not be excluded or delay the release simply because suitable licensed imagery is unavailable.

**Implications**  
- The interface must remain visually complete without an image.
- Featured events should use good imagery when it is readily available.
- The content model must support image source, attribution, creator, and licensing metadata where applicable.
- Complete image coverage is not required for v0.0.1.  

## PD-012 — Disputed historical dates use the most widely recognized date

**Decision**  
When an event's exact date is approximate or disputed, the product will associate it with the most widely recognized date.

**Rationale**  
A consistent calendar date is needed for the daily experience while still representing historical uncertainty accurately.

**Implications**  
- The event description must state when the date is approximate or disputed.
- Other commonly cited dates may be mentioned where useful.  


## PD-013 — v0.0.1 does not require user accounts or personalization

**Decision**  
The first release will not include accounts, login, profiles, personalized recommendations, history-interest preferences, or cross-device synchronization.

**Rationale**  
These capabilities are not required to validate the core daily-history experience.

**Implications**  
A first-time user must be able to access today's historical content without registering or logging in.  

## PD-014 — v0.0.1 is centered exclusively on today rather than historical browsing

**Decision**  
The first release will not include global search, arbitrary-date browsing, timelines, or browsing by year, century, category, country, or person.

**Rationale**  
The core experience is specifically focused on discovering history associated with the current calendar date.

**Implications**  
The application does not need navigation structures for broader historical exploration in v0.0.1.  

## PD-015 — Gamification and social features are outside v0.0.1

**Decision**  
The first release will not include gamification systems or social/community functionality.

**Rationale**  
Neither category is required to validate the fundamental daily discovery behavior.

**Implications**  
v0.0.1 excludes features such as:
- streaks, achievements, badges, points, levels, and leaderboards
- comments, followers, likes, social profiles, feeds, and community submissions  


## PD-016 — Runtime AI content generation is not part of v0.0.1

**Decision**  
The product will not generate event explanations or personalized summaries using AI at runtime, and it will not include an AI chat experience.

**Rationale**  
These capabilities are outside the functionality necessary to validate the first release.

**Implications**  
Content may still be prepared using external tooling, but that is considered a content-operations or implementation decision rather than a user-facing v0.0.1 feature.  


## PD-017 — v0.0.1 requires only two primary application screens

**Decision**  
The initial product consists of two primary screens:

1. Home
2. Event Detail

**Rationale**  
These two screens are sufficient to support the complete core experience.

**Implications**  
- Home contains today's date, the featured event, optional imagery, and additional events.
- Event Detail contains the event's date, description/context, optional imagery, and sources.
- No standalone onboarding flow is required unless minimal explanation becomes necessary for notification handling.  


## PD-018 — The v0.0.1 core experience consists of three user flows

**Decision**  
The complete core experience for the first release is represented by:

1. Normal discovery:  
   Open app → Today's page → Featured event → Event detail → Optional external source

2. Additional-event discovery:  
   Open app → Today's page → Additional event → Event detail → Optional external source

3. Daily notification:  
   Receive notification → Tap notification → Event detail → Optional external source

**Rationale**  
These flows cover the intended ways a user discovers, understands, and returns to daily historical content.

**Implications**  
Functionality outside these flows needs a specific justification before being included in v0.0.1.  


## PD-019 — The governing scope rule is “Today → Featured event → Learn → Read more → Come back tomorrow”

**Decision**  
v0.0.1 will use the daily discovery loop as its scope boundary.

**Rationale**  
If removing a feature would still allow the user to discover today's featured event, understand it, explore a few related events, and return through the next day's notification, that feature probably does not belong in the first release.

**Implications**  
This rule should be used when deciding whether proposed functionality belongs in v0.0.1.  


## PD-020 — Quiz is an explicit v0.1.0 product expansion

**Decision**
Daily Challenge and Quick Play are introduced as Quiz v0.1.0. The existing
v0.0.1 daily-history loop and APIs remain unchanged.

**Rationale**
PD-015 correctly excludes gamification from v0.0.1. Versioning the quiz work
makes the expanded scope intentional rather than silently changing the first
release.

**Implications**
- Quiz documentation and implementation must identify itself as v0.1.0.
- Existing event discovery and notification behavior remains compatible.
- Quiz work must not add unrelated competitive or social features.


## PD-021 — Daily Challenge uses one immutable assignment per calendar date

**Decision**
The backend generates and persists one ordered 20-question assignment for each
calendar date. The 5- and 10-question challenges are stable prefixes of that
assignment.

**Rationale**
Every user receiving the same date should receive the same challenge, and a
question-bank import must not rewrite a challenge that users may already have
started.

**Implications**
- Generation uses published questions available at first creation.
- A persisted assignment is immutable.
- Concurrent first requests must converge on one assignment.
- Timezone resolves the user's local date; it does not create a
  timezone-specific question set.


## PD-022 — Daily Challenge attempt status remains local to mobile

**Decision**
The first Daily Challenge attempt is official in local mobile state. Replays are
practice attempts.

**Rationale**
The product can support a meaningful daily result without accounts or backend
attempt tracking.

**Implications**
- The backend does not receive answers or scores.
- The backend cannot distinguish an official attempt from a replay.
- Cross-device result synchronization is out of scope.


## PD-023 — Quick Play supports Mixed or one optional collection

**Decision**
Quick Play accepts an optional `collectionId`. Omitting it selects Mixed content.

**Rationale**
This provides a fast default while allowing focused play without requiring a
category hierarchy.

**Implications**
- Quick Play selection is random among eligible published questions.
- The request supports 5, 10, or 20 questions.
- An unsupported count returns `400 insufficient_quiz_questions`.


## PD-024 — Quiz timing is mode-specific and returned as metadata

**Decision**
Daily Challenge has one total timer: 2 minutes for 5 questions, 4 minutes for
10, and 8 minutes for 20. Quick Play defaults to 20 seconds for multiple choice,
20 seconds for true/false, 30 seconds for image identification, and 45 seconds
for chronological ordering.

**Rationale**
Chronological ordering and image identification require different amounts of
interaction and recognition time.

**Implications**
- Daily timeout ends the challenge and unanswered questions are incorrect.
- Quick Play timeout marks the current question incorrect and continues.
- The API returns timer metadata.
- The mobile client may disable Quick Play timing.


## PD-025 — Correct answers are included for on-device evaluation

**Decision**
Quiz responses include correct answers, explanations, and sources. The mobile
client evaluates answers and presents immediate feedback and end-of-quiz review.

**Rationale**
Quiz v0.1.0 has no competitive integrity requirement and does not need a grading
round trip.

**Implications**
- There is no answer-submission or grading endpoint.
- True/false uses the normal option model.
- Chronological ordering returns shuffled items and the correct ordered item
  IDs.


## PD-026 — Collections are flat, many-to-many, and grouped for presentation

**Decision**
Collections do not form a hierarchy. Each collection has one catalog grouping:
`topic`, `historical_period`, `civilization`, or `conflict_or_movement`.

**Rationale**
Flat membership supports both broad and focused choices without creating a
brittle taxonomy.

**Implications**
- Questions may belong to multiple collections.
- Catalog responses group collections for display.
- Each collection advertises supported question counts based on its published
  question count.


## PD-027 — Difficulty balances selection but does not affect scoring

**Decision**
Every question is marked Easy, Medium, or Hard. Difficulty is used in balanced
selection and remains visually secondary.

**Rationale**
Difficulty helps create varied sessions without turning scoring into an opaque
or overly game-like system.

**Implications**
- Difficulty metadata is included in quiz responses.
- Correct answers are not worth different amounts based on difficulty.
- The reviewed bank targets approximately 25% Easy, 55% Medium, and 20% Hard.


## PD-028 — Quiz content is curated, sourced, and expanded in reviewed batches

**Decision**
The v0.1.0 target is 240 reviewed questions: 144 multiple choice, 36 true/false,
36 image identification, and 24 chronological ordering. Delivery begins with
60 questions and continues in six batches of 30.

**Rationale**
Incremental review protects accuracy and editorial quality while building a
large enough bank for varied play.

**Implications**
- Every question needs an explanation and at least one credible source.
- Image metadata must be complete and factual when an image is used.
- Content should broaden globally without rigid quotas.
- Sensitive subjects require educational and respectful treatment.
