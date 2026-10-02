# Story-Quality Audit: October 2 to 8

Audit date 2026-10-02, standard in `docs/EVENT_STORY_QUALITY_GUIDE.md`.
Existing Event Detail copy for these 28 events is a median of about 25 words:
one dated fact, with little on why it happened or what followed. IDs, daily
assignments and notification copy are unchanged.

## Rewritten in this batch (October 2, sourced)

| Event | Change |
| --- | --- |
| Gandhi born | Longer detail: father's post, South Africa turning point, Salt March, partition, Dr King, UN day. Teaser now says why he matters. Four sources added. |
| Saladin / Jerusalem | Why it was possible (Hattin), terms of surrender, Western reaction, Third Crusade. |
| Peanuts | Schulz's path to the strip and its reach at his 1999 retirement. |
| Beagle returns | Purpose of the voyage, FitzRoy's companion request, how it led to the theory and the 1859 book. |

Each is `source_verified` in the ledger, not `approved`, until the owner
re-reviews the longer copy.

## Pending (one dated fact, no cause or consequence)

Priority first. "Source" lists what to read to support the added context.

| Date | Event | Needs | Source to read |
| --- | --- | --- | --- |
| 10-03 | German reunification (featured) | Why 1990 (Wall, free elections), the treaty, Day of German Unity | Britannica reunification page; bpb |
| 10-03 | Washington Thanksgiving proclamation | Why a national day, who asked (Congress), what the text says | Founders Online |
| 10-03 | Iraq joins League of Nations | End of the mandate and conditions | Britannica Iraq |
| 10-03 | Francis of Assisi dies | What he founded and why the order mattered | Britannica |
| 10-04 | Sputnik 1 (featured) | Surprise in the US, link to NASA and the space race | State Dept milestones; NASA |
| 10-04 | Lesotho independence | Basutoland background, relationship with South Africa | State Dept country page |
| 10-04 | Mount Rushmore carving begins | Who, why, how long, Indigenous land (Black Hills) context | NPS |
| 10-04 | Rembrandt dies | What he is known for and his final years | Britannica |
| 10-05 | Love Me Do (featured) | Chart position is there; add band lineup and what came next | thebeatles.com |
| 10-05 | Portugal republic | Why the monarchy fell (1908 assassination), consequences | Britannica Portugal history |
| 10-05 | Bulgaria independence | Context of the 1908 Ottoman crisis | Britannica Ferdinand |
| 10-05 | Monty Python first airs | Why it was new; no image yet (no free licence found) | montypython.com |
| 10-06 | 51 Pegasi b (featured) | Why it surprised astronomers (hot Jupiter), the 2019 Nobel link exists; add what followed | UNIGE |
| 10-06 | The Jazz Singer | What 'talkie' meant for the industry | AFI; Britannica |
| 10-06 | Yom Kippur War | Aims, outcome, link to oil embargo and peace process | Britannica |
| 10-06 | Sadat assassinated | Why (Camp David and the 1979 treaty), who | Britannica |
| 10-07 | KLM founded (featured) | First flight, route network | KLM history |
| 10-07 | Lepanto | Why it mattered strategically, and the limits of the victory | Britannica |
| 10-07 | Project Mercury approved | What it led to (Shepard, Glenn) | NASA |
| 10-07 | GDR founded | Context: the 1949 FRG founding and division | bpb |
| 10-08 | Larsen perfect game (featured) | The 27 up, 27 down detail exists; add the context (Game 5, Yankees won series) | Britannica; Baseball Hall of Fame |
| 10-08 | Great Chicago Fire | Scale of destruction and rebuilding | Chicago Historical Society or Britannica |
| 10-08 | First Balkan War | Why the League formed, outcome | Britannica |
| 10-08 | Treaty of the Bogue | Link to the unequal treaties | Britannica |

## Image opportunities

Checked every featured event image from October 2 to November 5 (aspect
ratio and subject). All have a CloudFront image except 10-24 (UN Charter in
force), which the earlier pass could not find a licensed image for.

Portraits at risk of a bad Today crop at 16:9.5 (cover from top and bottom),
worth a landscape or face-safe alternative:

| Date | Event | Image | Note |
| --- | --- | --- | --- |
| 10-02 | Gandhi | portrait, ratio 0.80 | **Replacement proposed, see below.** |
| 10-08 | Don Larsen | 306x502, ratio 0.61 | Low resolution as well as a tall crop; find another Larsen image or the scoreboard / Yankee Stadium photograph |
| 10-29 | Atatürk | 591x774, ratio 0.76 | Tall portrait |
| 10-18 | Persons Case | 768x960, ratio 0.80 | Statue of Nellie McClung, tall |
| 10-11 | Apollo 7 | 709x960 | Rocket, so the crop mostly loses sky |
| 10-21 | French women vote | 536x960 | Elector card, document crop |
| 10-22 | Chandrayaan-1 | 640x960 | Rocket |
| 10-28 | Statue of Liberty dedication | 756x960 | Painting |
| 11-01 | Maastricht Treaty | 703x960 | Memorial stone |

## Gandhi hero proposal

Candidate: `editorial/event-images/candidates/2026-10-02-gandhi-salt-march.json`
(not uploaded, not attached; staged under `build/event-images/2026-10-02-gandhi/`).

- Wikimedia Commons file "Gandhi during the Salt March 30 C2-12-105 n.jpg",
  dated March 1930, scan of a photograph from the Gandhi Study Centre archive.
- Landscape (1920 by 1240 source, 960 by 620 rendition). Gandhi walks at the
  centre of the frame with his face in the upper third, so a 16:9.5 cover crop
  trims only about 25 px of the 620 at the top and bottom and keeps his face.
- It also matches the story: the Salt March is in the detail copy.
- Rights: Commons tags `PD-India` and `PD-India-URAA`; photographer unknown.
  Commons dates the photograph to March 1930. I have not independently checked
  the US or Indian copyright terms behind those tags, and the weak point is the
  unknown author and the unseen original publication record. Owner to accept or
  reject this basis before upload.
- Alt text: "Mohandas Gandhi, carrying a walking staff, leads a column of marchers along a tree-lined road during the Salt March in March 1930".
- Attribution: "Photographer unknown, March 1930, via Wikimedia Commons (public domain: PD-India and PD-India-URAA)".
- The existing Elliott & Fry studio portrait (1931, verified) stays as the
  canonical image until the replacement is approved, uploaded and attached.

## Added events (widening, 2026-10-02)

Ten new additional events, each with a direct source read and a dated ledger
check (`source_verified`, no image; only featured events need a hero image).
Oct 6 now has five events; Oct 2, 4 and 8 have five; Oct 3, 5 and 7 have six.

| Date | Event | Main source |
| --- | --- | --- |
| 10-02 | Guinea becomes independent from France (1958) | Britannica; Office of the Historian |
| 10-03 | Britain tests its first atomic bomb (1952) | Britannica |
| 10-03 | First successful V-2 launch (1942) | Britannica |
| 10-04 | Madrid Protocol signed (1991) | Antarctic Treaty Secretariat |
| 10-05 | Parisians march on Versailles (1789) | Britannica |
| 10-05 | Wright Flyer No. 3 record flight (1905) | NPS; National Air and Space Museum |
| 10-06 | Moulin Rouge opens (1889) | Moulin Rouge official site; Britannica |
| 10-07 | Stamp Act Congress meets (1765) | Britannica; National Constitution Center; Americans at War |
| 10-07 | US-led strikes begin in Afghanistan (2001) | Presidential address, White House archive |
| 10-08 | Alvin York captures 132 German soldiers (1918) | Congressional Medal of Honor Society; Britannica |

Notes for the reviewer:

- Wright flight: NPS and the Smithsonian disagree on the flight time (38:03
  vs 39 minutes) and which brother flew, so the copy says only "about 24 miles".
- Stamp Act Congress: Britannica says only "October 1765". The Constitution
  Center page dates the meeting October 7 in its headline but October 9 in its
  body, so review added Americans at War (Encyclopedia.com), which gives
  October 7 to 25, 1765.
- Moulin Rouge: Britannica gives only 1889; the October 6 date rests on the
  club's official history.
- Considered and dropped: the Gregorian calendar jump (the October 15
  event already covers it), Che Guevara's capture (the October 9 event covers
  it), Baird's first face on television and Milosevic's fall (no source for the
  exact day), and Austria's annexation of Bosnia (sources split between
  October 6 and 7).
