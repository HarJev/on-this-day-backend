# October 9-15 Connected Quiz: Owner Review

Updated: 2026-10-01. **Draft only: no approval, publication or import.**

Researched and source-checked by Claude. Every ledger entry is
`source_verified`; none has a reviewer or approval date. Nothing under
`content/` changed. The October 1-8 supplement has its own review file:
`editorial/batches/2026-10-01-08-connected-quiz-supplement/OWNER_REVIEW.md`.

## Approval Checklist

Reply with approve, revise or reject for each group. Approval of a later group
does not imply approval of an earlier one.

1. **Oct 9-15 event batch** (`2026-10-09-15-historical-events`, 30 drafts by the
   event worker). Audited, not edited: every day has 4-5 events, each with a
   featured notification and authoritative sources (no Wikipedia). Featured
   choices look right; see "Event Audit" for the one judgement call.
2. **One supplementary event** (`2026-10-09-15-supplementary-events`):
   Martin Luther King Jr. named Nobel Peace Prize winner (10-14), added as an
   additional event on its day (`proposed-day-additions.json`).
3. **Ten reuse links** on published questions (table below). Questions,
   answers and images stay unchanged; only `relatedEventIds` would gain an ID.
4. **Nineteen new questions** for prospective Pack 150 (below): 16 multiple
   choice and 3 true/false; 8 easy and 11 medium.

Promotion order after approval: events (including the supplementary event
and its day addition, if approved), then Pack 150, then the link edits to existing packs
after re-checking each snapshot hash in `proposed-links.json`.

## Per-Day Coverage

| Date | Featured (event batch) | Hooks | New questions | Reuse links |
| --- | --- | --- | --- | --- |
| 10-09 | `universal-postal-union-founded-1874` | 5 | UPU city, Washington Monument, Uganda, Kepler supernova, Che Guevara | none |
| 10-10 | `wuchang-uprising-1911` | 4 | Outer Space Treaty | Xinhai/Qing, Sun Yat-sen portrait (featured), Pacific independence ordering (Fiji) |
| 10-11 | `apollo-7-launches-1968` | 3 | Apollo 7 orbit (featured), Vatican II Latin, Mary Rose | none |
| 10-12 | `equatorial-guinea-independence-1968` | 6 | Equatorial Guinea (featured), Oktoberfest | African independence ordering (featured), Americas ordering, Columbian exchange, Voskhod 1 |
| 10-13 | `chilean-miners-rescued-2010` | 3 | Templars, White House | Chile miners (featured) |
| 10-14 | `battle-of-hastings-1066` | 5 | Bayeux Tapestry (featured), Yeager X-1, Cuban Missile Crisis | Napoleon portrait (Jena), King portrait (new Nobel event) |
| 10-15 | `shenzhou-5-yang-liwei-2003` | 3 | Shenzhou 5 (featured), Huygens/Titan, Gregorian leap year | none |

Every featured story has at least one linked hook. Twenty-nine hooks in total; this
is supply for the existing selector (one featured-linked slot in Daily-5, at
most one more same-date slot in Daily-10/20), not a promise that every Daily
will show them.

## Owner Decisions Applied (2026-10-01)

- Events must have an exact or widely accepted date. The Hangeul proclamation
  (only a lunar month is recorded) and the Greenwich meridian event (owner
  preferred to leave it out) were dropped with their question and link. The
  base event batch already meets the four-event floor on both days, so no
  replacement events were needed.
- An event summary that gives away a linked answer is fine; the Chile link
  stays, and this applies to future cycles.
- To keep supply up, the two reserve hooks were drafted: the Washington
  Monument obelisk and Kampala's lake.

## Notes

- **Space weighting.** Five of 29 hooks are spaceflight (Apollo 7, Voskhod,
  Outer Space Treaty, Shenzhou, Huygens), following the event slate.
- **Regions.** Europe and the Americas dominate; Asia, Africa and Oceania
  get eight hooks between them. No unrelated regions were shoehorned in.

## Event Audit

- All 30 drafts have 1-2 sources from Britannica, NASA, national archives,
  official bodies or museums. Ledger notes record the supporting sentence for
  each. I re-read the sources used for links and questions on 2026-10-01; I did
  not re-approve the rest of the event batch.
- 10-12 features Equatorial Guinea rather than Columbus's landfall. That is a
  defensible calm choice (Columbus remains an additional event with a Julian
  `dateNote`); swap it only if you want the more recognizable story.
- Strong event missing from the slate, drafted as a supplementary event:
  King's Nobel Peace Prize (10-14, civil rights). Considered and not added:
  Hangeul (lunar-month date only), the Greenwich meridian (owner preference)
  and John Lennon's birth (10-09; repeats the Oct 5 Beatles hook).

## New Questions

### 1. upu-founding-city-bern

The Universal Postal Union was founded in 1874 by a treaty signed in which Swiss city?

A. Geneva

B. Bern **(correct)**

C. Zurich

D. Basel

**Explanation:** Delegates meeting in Bern signed the Treaty of Bern on October 9, 1874, creating the union for international mail. The anniversary is now marked as World Post Day.

**Type:** multiple choice. **Difficulty:** medium. **Date:** 10-09.
**Related event:** `universal-postal-union-founded-1874`.

Sources: [Universal Postal Union - History](https://www.upu.int/en/universal-postal-union/about-upu/history); [Swiss National Museum - The story of the Universal Postal Union](https://blog.nationalmuseum.ch/en/2024/10/the-story-of-the-universal-postal-union/).

### 2. washington-monument-shape

The Washington Monument in Washington, D.C., takes the form of what kind of structure?

A. A pyramid

B. A domed rotunda

C. An obelisk **(correct)**

D. A triumphal arch

**Explanation:** The monument to George Washington is a marble-faced granite obelisk. Built between 1848 and 1884, it opened to the public on October 9, 1888.

**Type:** multiple choice. **Difficulty:** easy. **Date:** 10-09.
**Related event:** `washington-monument-opens-to-public-1888`.

Sources: [Encyclopaedia Britannica - On This Day, October 9](https://www.britannica.com/on-this-day/October-9).

### 3. uganda-capital-lake

Uganda's capital, Kampala, lies near the shore of which lake, Africa's largest freshwater lake?

A. Lake Victoria **(correct)**

B. Lake Tanganyika

C. Lake Malawi

D. Lake Chad

**Explanation:** Kampala sits near Lake Victoria, which Uganda shares with Kenya and Tanzania. Uganda became independent from Britain on October 9, 1962.

**Type:** multiple choice. **Difficulty:** easy. **Date:** 10-09.
**Related event:** `uganda-independence-1962`.

Sources: [Encyclopaedia Britannica - Uganda](https://www.britannica.com/place/Uganda).

### 4. kepler-supernova-last-naked-eye

Kepler's Supernova, first seen in 1604, is the most recent supernova in our own galaxy to be seen with the naked eye.

A. True **(correct)**

B. False

**Explanation:** No later supernova in the Milky Way has been seen with the naked eye. The bright Supernova 1987A was in the Large Magellanic Cloud, a neighboring galaxy.

**Type:** true false. **Difficulty:** medium. **Date:** 10-09.
**Related event:** `keplers-supernova-first-sighted-1604`.

Sources: [NASA - 420 years ago, Johannes Kepler observes a supernova](https://www.nasa.gov/history/420-years-ago-astronomer-johannes-kepler-observes-a-supernova/).

### 5. che-guevara-birth-country

In which country was the revolutionary Che Guevara born?

A. Cuba

B. Bolivia

C. Mexico

D. Argentina **(correct)**

**Explanation:** Ernesto 'Che' Guevara was born in Rosario, Argentina, in 1928. He became a leader of the Cuban Revolution and was killed in Bolivia in 1967.

**Type:** multiple choice. **Difficulty:** medium. **Date:** 10-09.
**Related event:** `che-guevara-executed-1967`.

Sources: [Encyclopaedia Britannica - Che Guevara](https://www.britannica.com/biography/Che-Guevara).

### 6. outer-space-treaty-prohibition

Under the 1967 Outer Space Treaty, which of these may a country NOT do?

A. Send astronauts to the Moon

B. Claim the Moon as its own territory **(correct)**

C. Carry out scientific research on the Moon

D. Launch satellites into Earth orbit

**Explanation:** The treaty says outer space, including the Moon, is not subject to national appropriation by claim of sovereignty. Exploration and scientific investigation remain free for all states.

**Type:** multiple choice. **Difficulty:** medium. **Date:** 10-10.
**Related event:** `outer-space-treaty-enters-force-1967`.

Sources: [U.S. Department of State (archive) - Outer Space Treaty](https://2009-2017.state.gov/t/isn/5181.htm); [Encyclopaedia Britannica - Outer Space Treaty](https://www.britannica.com/event/Outer-Space-Treaty).

### 7. apollo-7-flew-around-moon

Apollo 7, the first crewed Apollo mission, flew around the Moon.

A. True

B. False **(correct)**

**Explanation:** Apollo 7 stayed in Earth orbit for about 11 days in October 1968. Apollo 8 carried the first humans to orbit the Moon that December.

**Type:** true false. **Difficulty:** easy. **Date:** 10-11.
**Related event:** `apollo-7-launches-1968`.

Sources: [NASA - Apollo 7](https://www.nasa.gov/mission/apollo-7/); [NASA - Apollo 8](https://www.nasa.gov/mission/apollo-8/).

### 8. vatican-ii-mass-language

The Second Vatican Council allowed Mass to be celebrated in local languages instead of only in which language?

A. Latin **(correct)**

B. Greek

C. Hebrew

D. Italian

**Explanation:** The council's Constitution on the Sacred Liturgy authorized vernacular languages instead of Latin in the Mass, one of its most visible changes to Catholic worship.

**Type:** multiple choice. **Difficulty:** easy. **Date:** 10-11.
**Related event:** `second-vatican-council-opens-1962`.

Sources: [Encyclopaedia Britannica - Second Vatican Council](https://www.britannica.com/event/Second-Vatican-Council).

### 9. mary-rose-monarch

The Mary Rose, raised from the seabed in 1982, was the warship of which English monarch?

A. Elizabeth I

B. Richard III

C. Henry VIII **(correct)**

D. Charles I

**Explanation:** The Mary Rose served Henry VIII for 34 years before sinking in 1545. Its hull was raised on October 11, 1982.

**Type:** multiple choice. **Difficulty:** easy. **Date:** 10-11.
**Related event:** `mary-rose-raised-1982`.

Sources: [Mary Rose Trust - Raising the Mary Rose](https://maryrose.org/discover/history/recovering-the-mary-rose/).

### 10. equatorial-guinea-colonial-power

Equatorial Guinea became independent in 1968 from which European country?

A. Portugal

B. France

C. Belgium

D. Spain **(correct)**

**Explanation:** Equatorial Guinea gained independence from Spain on October 12, 1968.

**Type:** multiple choice. **Difficulty:** easy. **Date:** 10-12.
**Related event:** `equatorial-guinea-independence-1968`.

Sources: [U.S. Office of the Historian - Equatorial Guinea](https://history.state.gov/countries/equatorial-guinea).

### 11. oktoberfest-original-celebration

Munich's first Oktoberfest, in 1810, celebrated what?

A. A military victory

B. A royal wedding **(correct)**

C. The end of the harvest

D. A king's coronation

**Explanation:** The festival began on October 12, 1810, to celebrate the marriage of the Bavarian crown prince, later King Louis I, to Princess Therese. The Theresienwiese grounds are named after her.

**Type:** multiple choice. **Difficulty:** medium. **Date:** 10-12.
**Related event:** `first-oktoberfest-1810`.

Sources: [Encyclopaedia Britannica - Oktoberfest](https://www.britannica.com/topic/Oktoberfest).

### 12. templars-original-purpose

The Knights Templar were originally founded to do what?

A. Protect Christian pilgrims to the Holy Land **(correct)**

B. Guard the pope in Rome

C. Defend the king of France

D. Escort merchants on the Silk Roads

**Explanation:** The order began around 1119-20 to protect Christian pilgrims travelling to Jerusalem. It later took on wider military and banking roles before Philip IV of France ordered its members arrested in 1307.

**Type:** multiple choice. **Difficulty:** medium. **Date:** 10-13.
**Related event:** `templars-arrested-in-france-1307`.

Sources: [Encyclopaedia Britannica - Templar](https://www.britannica.com/topic/Templars).

### 13. white-house-first-president

Which U.S. president was the first to live in the White House?

A. George Washington

B. John Adams **(correct)**

C. Thomas Jefferson

D. James Madison

**Explanation:** Construction began with a cornerstone on October 13, 1792. John Adams moved in on November 1, 1800, near the end of his term, so Washington never lived there.

**Type:** multiple choice. **Difficulty:** medium. **Date:** 10-13.
**Related event:** `white-house-cornerstone-laid-1792`.

Sources: [White House Historical Association - Building the White House](https://www.whitehousehistory.org/building-the-white-house); [Encyclopaedia Britannica - On This Day, October 13](https://www.britannica.com/on-this-day/October-13).

### 14. bayeux-tapestry-hastings

Which famous work of medieval textile art shows the Norman Conquest and the Battle of Hastings?

A. The Bayeux Tapestry **(correct)**

B. The Apocalypse Tapestry

C. The Lady and the Unicorn tapestries

D. The Devonshire Hunting Tapestries

**Explanation:** The Bayeux Tapestry is an embroidered linen band with more than 70 scenes of the Norman Conquest of 1066, including the battle at Hastings.

**Type:** multiple choice. **Difficulty:** easy. **Date:** 10-14.
**Related event:** `battle-of-hastings-1066`.

Sources: [Encyclopaedia Britannica - Bayeux Tapestry](https://www.britannica.com/topic/Bayeux-Tapestry); [Encyclopaedia Britannica - Battle of Hastings](https://www.britannica.com/event/Battle-of-Hastings).

### 15. yeager-x1-runway-takeoff

In 1947, Chuck Yeager's Bell X-1 took off from a runway under its own power before breaking the sound barrier.

A. True

B. False **(correct)**

**Explanation:** The X-1 was carried aloft attached to a B-29 mother ship and released at about 25,000 feet. It then climbed on rocket power to about 40,000 feet, where Yeager flew faster than sound.

**Type:** true false. **Difficulty:** medium. **Date:** 10-14.
**Related event:** `yeager-breaks-sound-barrier-1947`.

Sources: [Encyclopaedia Britannica - Chuck Yeager](https://www.britannica.com/biography/Chuck-Yeager).

### 16. cuban-missile-crisis-president

Which U.S. president told the nation in October 1962 that Soviet missile sites had been found in Cuba?

A. Dwight D. Eisenhower

B. John F. Kennedy **(correct)**

C. Lyndon B. Johnson

D. Richard Nixon

**Explanation:** U-2 photographs taken on October 14, 1962, revealed Soviet missile installations. President Kennedy announced them in a televised address on October 22, beginning the public crisis.

**Type:** multiple choice. **Difficulty:** easy. **Date:** 10-14.
**Related event:** `u2-photographs-missiles-in-cuba-1962`.

Sources: [National Archives - Aerial photograph of missiles in Cuba (1962)](https://www.archives.gov/milestone-documents/aerial-photograph-of-missiles-in-cuba).

### 17. shenzhou-5-earlier-nations

Shenzhou 5 made China the third country to send a person into space on its own spacecraft. Which two countries did it first?

A. The Soviet Union and the United States **(correct)**

B. The United States and France

C. The Soviet Union and Japan

D. The United States and India

**Explanation:** On October 15, 2003, Yang Liwei orbited Earth aboard Shenzhou 5. China became the third country, after the Soviet Union and the United States, to achieve human spaceflight.

**Type:** multiple choice. **Difficulty:** medium. **Date:** 10-15.
**Related event:** `shenzhou-5-yang-liwei-2003`.

Sources: [Encyclopaedia Britannica - Shenzhou](https://www.britannica.com/topic/Shenzhou).

### 18. huygens-titan-landing

The Cassini mission carried the Huygens probe, which landed on which moon of Saturn?

A. Enceladus

B. Mimas

C. Titan **(correct)**

D. Iapetus

**Explanation:** Huygens landed on Titan, Saturn's largest moon, on January 14, 2005, after riding to Saturn aboard Cassini.

**Type:** multiple choice. **Difficulty:** medium. **Date:** 10-15.
**Related event:** `cassini-launches-to-saturn-1997`.

Sources: [NASA Science - Huygens probe](https://science.nasa.gov/mission/cassini/spacecraft/huygens-probe/).

### 19. gregorian-century-leap-year

Under the Gregorian calendar, which of these years was NOT a leap year?

A. 1600

B. 1896

C. 1900 **(correct)**

D. 2000

**Explanation:** A century year is a leap year only if it divides exactly by 400. That makes 1600 and 2000 leap years but not 1900; 1896 follows the ordinary four-year rule.

**Type:** multiple choice. **Difficulty:** medium. **Date:** 10-15.
**Related event:** `gregorian-calendar-takes-effect-1582`.

Sources: [Encyclopaedia Britannica - Gregorian calendar](https://www.britannica.com/topic/Gregorian-calendar).

## Existing Questions: Proposed Links Only

| Date | Existing question | Linked event | Rationale |
| --- | --- | --- | --- |
| 10-10 | `xinhai-revolution-qing-dynasty` | `wuchang-uprising-1911` (featured) | Britannica regards the October 10 Wuchang mutiny as the formal beginning of the revolution that overthrew the Qing. The question asks which dynasty that revolution ended. |
| 10-10 | `identify-sun-yat-sen` | `wuchang-uprising-1911` (featured) | Britannica: after the Wuchang rising Sun returned from abroad and was elected provisional president. The portrait identifies the revolution's best-known leader. |
| 10-10 | `order-pacific-independence-milestones` | `fiji-independence-1970` (additional) | The ordering includes 'Fiji becomes independent' (1970), the event itself. |
| 10-12 | `order-americas-milestones` | `columbus-sights-land-1492` (additional) | The ordering includes 'Columbus reaches the Caribbean' (1492), the landfall itself. |
| 10-12 | `columbian-exchange-meaning` | `columbus-sights-land-1492` (additional) | The exchange named after Columbus began with the 1492 crossing; the question explains its consequences rather than the landfall. |
| 10-12 | `voskhod-1-multiperson-crew` | `voskhod-1-launches-1964` (additional) | Exact match: the question asks which spacecraft first carried a multi-person crew. Hard; kept as published. |
| 10-12 | `order-african-independence-1960s` | `equatorial-guinea-independence-1968` (featured) | The ordering includes 'Equatorial Guinea becomes independent' (October 12, 1968), the featured event. |
| 10-13 | `chile-miners-days-underground` | `chilean-miners-rescued-2010` (featured) | Exact match on the rescue. The event summary states 69 days, so reading the story first reveals the answer; that is learning, not leakage in the quiz itself. |
| 10-14 | `identify-napoleon-bonaparte` | `battles-of-jena-auerstedt-1806` (additional) | Britannica names Napoleon as the victor at Jena-Auerstedt. The 1812 portrait identifies him without depending on the battle. |
| 10-14 | `identify-martin-luther-king-jr` | `mlk-awarded-nobel-peace-prize-1964` (additional) | The portrait's explanation already cites his 1964 Nobel Peace Prize. Depends on the proposed supplementary event. |

Image questions (Sun Yat-sen, Napoleon, King) keep their approved owned
renditions; rights evidence is inherited from the ledgers named in
`proposed-links.json`. No image was downloaded, uploaded or re-validated.

## Skipped Or Reserved Hooks

- `cry-of-yara-1868`, `us-naval-academy-founded-1845`,
  `south-african-war-begins-1899`, `italy-declares-war-on-germany-1943`,
  `thrustssc-land-speed-record-1997`: only name, date or belligerent recall was
  available. No-link events are legitimate.
- `identify-george-washington` was not linked to the Washington Monument: it
  is already linked to 10-03, six days earlier.
- No new image-identification questions: any new image needs rights,
  phone-size fairness and an owned upload, which this cycle does not do.

## Checks Run

- Strict canonical event and quiz validators on a scratch merge of canonical
  content, all runway event drafts, the supplementary event and day
  addition, both draft packs (as published) and all link edits:
  `events valid=true quiz valid=true`. Only pre-existing warnings (featured
  days without images, two small collections).
- All 89 question-event relations in that merge resolve to an event.
- `EditorialReviewCheckCommand` passes for both Oct 9-15 batches.
- Not run: imports, database checks, Daily selection, link liveness sweep.
