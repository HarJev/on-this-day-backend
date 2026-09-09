# Quiz v0.1.0 Initial Content Review

This record covers the initial 60-question bank added in Q7. The canonical
content is `content/quizzes/collections.json` plus the six files under
`content/quizzes/questions/`. Review was completed on 2026-09-08.

The working candidate slate contained 72 questions. Only the final 60 are
committed. Candidates with weaker sourcing, ambiguous answers, or unsuitable
image rights were omitted rather than retained as draft product content.

## Final Distribution

| Dimension | Final count |
| --- | ---: |
| Multiple choice | 36 |
| True/false | 9 |
| Image identification | 9 |
| Chronological ordering | 6 |
| Easy | 15 |
| Medium | 33 |
| Hard | 12 |
| Published | 60 |

Every question belongs to at least one collection. Collection membership is
many-to-many and totals more than 60.

| Collection | Group | Published questions | Supported counts |
| --- | --- | ---: | --- |
| Ancient History | historical_period | 15 | 5, 10 |
| Ancient Rome | civilization | 6 | 5 |
| Wars & Conflicts | topic | 20 | 5, 10, 20 |
| Leaders & Power | topic | 16 | 5, 10 |
| Revolutions | conflict_or_movement | 10 | 5, 10 |
| World Wars | conflict_or_movement | 10 | 5, 10 |
| Science & Innovation | topic | 12 | 5, 10 |
| Exploration & Exchange | topic | 10 | 5, 10 |
| Society, Culture & Ideas | topic | 10 | 5, 10 |

## Breadth Audit

Geography and period are editorial audit lenses, not persisted categories or
selection quotas. Each question was assigned one primary lens for this review,
even when its subject crosses regions or periods.

| Primary geographic lens | Questions |
| --- | ---: |
| Africa | 8 |
| Asia | 11 |
| Europe | 16 |
| Middle East and North Africa | 4 |
| Americas and Caribbean | 15 |
| Oceania and Pacific | 2 |
| Transregional or global | 4 |

| Primary period | Questions |
| --- | ---: |
| Ancient through 500 CE | 15 |
| 500-1500 | 9 |
| 1500-1800 | 9 |
| 1800-1914 | 10 |
| 1914-1945 | 12 |
| 1945-present | 5 |

The bank is intentionally broad rather than uniform. Later Q8 batches should
increase Middle Eastern, Pacific, post-1945, Indigenous, and social-history
coverage without displacing better-supported questions merely to satisfy a
quota.

## Source Review

The bank contains 78 source references. Each cited page was selected because it
supports the answer and the factual explanation, rather than only naming the
general subject. Chronological questions cite separate references when one page
does not support all four dates.

Sources favor public institutions and established reference works, including
UNESCO, national archives and legislatures, the U.S. National Park Service,
Imperial War Museums, the British Museum, the Metropolitan Museum of Art,
Bletchley Park, the Nelson Mandela Foundation, the International Olympic
Committee, and Encyclopaedia Britannica. No Wikipedia article is used as a
factual source.

Editorial checks covered:

- one unambiguous correct option and three plausible but incorrect distractors;
- canonical True/False option ordering;
- four independently dated items for each chronological question;
- qualifications such as traditional or approximate dates where needed;
- explanations that do not claim more than their cited source supports;
- stable names and terminology suitable for a general audience.

## Image Review

Wikimedia Commons is used for image provenance only. Each direct media URL was
requested independently and returned JPEG image content. Each Commons
description page was separately retained as `sourceUrl`. Alt text describes
visible form without naming the correct answer.

| Question | Creator | License |
| --- | --- | --- |
| Identify the Colosseum | David Iliff | CC BY-SA 2.5 |
| Identify a Benin Bronze | Mike Peel | CC BY-SA 4.0 |
| Identify Nelson Mandela | 14GTR; sculpture by Ian Walters | CC BY-SA 4.0 |
| Identify the Terracotta Army | Gary Todd | CC0 1.0 |
| Identify Mahatma Gandhi | Elliott & Fry | Public Domain Mark 1.0 |
| Identify Harriet Tubman | Horatio Seymour Squyer | Public Domain Mark 1.0 |
| Identify Machu Picchu | Diego Delso | CC BY-SA 4.0 |
| Identify Winston Churchill | Yousuf Karsh | Public Domain Mark 1.0 |
| Identify Alan Turing | Elliott & Fry | Public Domain Mark 1.0 |

The canonical JSON records the direct URL, Commons description page,
attribution, creator, license name, and license URL for every image.

## Sensitive Content Decisions

- The Treaty of Waitangi explanation notes that its English and Maori texts
  differ materially rather than presenting an uncontested agreement.
- The Columbian Exchange explanation acknowledges forced human movement,
  disease, and catastrophic consequences instead of framing exchange as purely
  beneficial.
- Haitian independence is described through the overthrow of colonial rule and
  slavery.
- Great Zimbabwe is attributed to a Shona-speaking society, rejecting obsolete
  colonial misattribution.
- Benin court art is identified accurately and linked to the British Museum's
  contested-objects documentation.
- Harriet Tubman is described as having escaped slavery and guided others to
  freedom; dehumanizing historical labels are not used as neutral narration.
- The Manhattan Project question tests its documented objective without
  celebrating the use of atomic weapons.
- The Battle of Adwa explanation centers Ethiopian sovereignty.

No question in this batch depends on a disputed answer. Where dates are
traditional or approximate, that uncertainty is stated in the prompt or
explanation. Questions that could not meet that standard were replaced from the
working candidate slate.
