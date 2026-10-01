#!/usr/bin/env python3
"""Writes the October 9-15 connected-quiz drafts and the October 1-8 supplement.

Scratch editorial tooling, not used by the runtime or importers. Run from the
repository root:

    python3 editorial/tools/connected-quiz/build_oct_cycle.py

It (re)writes three draft batches from the records below:

- editorial/batches/2026-10-09-15-connected-quiz/
- editorial/batches/2026-10-09-15-supplementary-events/
- editorial/batches/2026-10-01-08-connected-quiz-supplement/

Every ledger entry is source_verified, never approved. Existing-question link
proposals record a snapshot hash of the canonical question as it was inspected
(json.dumps(question, separators=(",", ":")) in file key order, the same scheme
as the October 2-8 link file), so a later change to that question is detectable.
"""
import collections
import glob
import hashlib
import json
import os

CHECKED = "2026-10-01"
BR = "Encyclopaedia Britannica - "
NOTE_NEW = ("Draft researched and source-checked by Claude; awaiting owner content review. "
            "Relation: {rel} Distractors checked for exclusivity under the question's wording; "
            "difficulty is an editorial estimate, not user-tested.")
NOTE_LINK = ("Relationship review only; original question and answer are unchanged and remain "
             "published. New relation is unapproved. {extra}")

SRC = {
    # url: (display name, what the page states, as read on CHECKED)
    "https://www.upu.int/en/universal-postal-union/about-upu/history": (
        "Universal Postal Union - History",
        "UPU history: on 9 October 1874 the Treaty of Bern establishing the General Postal Union was signed; the day is now celebrated as World Post Day."),
    "https://blog.nationalmuseum.ch/en/2024/10/the-story-of-the-universal-postal-union/": (
        "Swiss National Museum - The story of the Universal Postal Union",
        "Swiss National Museum: delegates of the International Postal Congress, from 22 countries, convened in Bern's former guild hall for the last meeting on 9 October 1874."),
    "https://www.nasa.gov/history/420-years-ago-astronomer-johannes-kepler-observes-a-supernova/": (
        "NASA - 420 years ago, Johannes Kepler observes a supernova",
        "NASA: first observed on Oct. 9, 1604; 'the last known supernova to occur in the Milky Way Galaxy' and the last visible to the naked eye until 1987 (SN 1987A, in the Large Magellanic Cloud)."),
    "https://www.britannica.com/biography/Che-Guevara": (
        BR + "Che Guevara",
        "Britannica: 'born June 14, 1928, Rosario, Argentina - died October 9, 1967, La Higuera, Bolivia'; Argentine-Cuban revolutionary."),
    "https://www.korea.net/NewsFocus/Culture/view?articleId=145583": (
        "Korea.net - The story of Hangeul",
        "Korea.net: Sejong's scholars created the alphabet in 1443; the Hunminjeongeum was published and officially proclaimed in 1446; Hanja (Chinese characters) were hard for people who did not speak Chinese; Hangeul Day is celebrated annually on Oct. 9."),
    "https://www.unesco.org/en/memory-world/hunminjeongum-manuscript": (
        "UNESCO Memory of the World - Hunminjeongum manuscript",
        "UNESCO: the manuscript, published in the ninth lunar month of 1446, contains Sejong the Great's promulgation of the Korean alphabet; the alphabet was completed in 1443."),
    "https://2009-2017.state.gov/t/isn/5181.htm": (
        "U.S. Department of State (archive) - Outer Space Treaty",
        "State Department text: entered into force October 10, 1967; Art. I outer space incl. the Moon free for exploration and scientific investigation by all States; Art. II not subject to national appropriation by claim of sovereignty; Art. IV no nuclear weapons in orbit."),
    "https://www.britannica.com/event/Chinese-Revolution-1911-1912": (
        BR + "Chinese Revolution (1911-12)",
        "Britannica: on October 10 a mutiny broke out among the troops in Wuchang, 'regarded as the formal beginning of the revolution' that overthrew the Qing dynasty in 1912; Sun Yat-sen returned from abroad and was elected provisional president."),
    "https://www.britannica.com/biography/Sun-Yat-sen": (
        BR + "Sun Yat-sen",
        "Britannica: influential in overthrowing the Qing dynasty (1911/12); first provisional president of the Republic of China (1911-12)."),
    "https://thecommonwealth.org/our-member-countries/fiji": (
        "The Commonwealth - Fiji",
        "Commonwealth: Fiji joined the Commonwealth in 1970 following independence from Britain, matching the 1970 item in the ordering question."),
    "https://www.nasa.gov/mission/apollo-7/": (
        "NASA - Apollo 7",
        "NASA: the first crewed flight of the Apollo program, a crewed orbital flight, Oct. 11-22, 1968, with Schirra, Eisele and Cunningham."),
    "https://www.nasa.gov/mission/apollo-8/": (
        "NASA - Apollo 8",
        "NASA: launched Dec. 21, 1968; Borman, Lovell and Anders were the first humans to orbit the Moon."),
    "https://www.britannica.com/event/Second-Vatican-Council": (
        BR + "Second Vatican Council",
        "Britannica: the Constitution on the Sacred Liturgy 'authorized the use of vernacular languages instead of Latin in the celebration of mass.'"),
    "https://maryrose.org/discover/history/recovering-the-mary-rose/": (
        "Mary Rose Trust - Raising the Mary Rose",
        "Mary Rose Trust: Henry VIII's ship returned to Portsmouth; she served for 34 years before sinking; raised on 11 October 1982."),
    "https://history.state.gov/countries/equatorial-guinea": (
        "U.S. Office of the Historian - Equatorial Guinea",
        "Office of the Historian: 'On October 12, 1968, Equatorial Guinea gained independence from Spain.'"),
    "https://www.britannica.com/topic/Oktoberfest": (
        BR + "Oktoberfest",
        "Britannica: the festival originated on October 12, 1810, in celebration of the marriage of the Bavarian crown prince (later King Louis I) to Princess Therese."),
    "https://www.britannica.com/biography/Christopher-Columbus": (
        BR + "Christopher Columbus",
        "Britannica: on October 12 [1492] land was sighted from the Pinta; the first Caribbean landfall, Guanahani, is usually identified with San Salvador in the Bahamas."),
    "https://nationalhumanitiescenter.org/tserve/nattrans/ntecoindian/essays/columbian.htm": (
        "National Humanities Center - The Columbian Exchange",
        "Essay: the wave of biological exchange did not begin until 1492, when Europeans initiated contacts across the Atlantic that never ceased."),
    "https://www.britannica.com/technology/Voskhod-spacecraft": (
        BR + "Voskhod",
        "Britannica: on October 12, 1964, Voskhod 1 carried three cosmonauts into Earth orbit; chronology lists it as the first multiperson spacecraft."),
    "https://www.britannica.com/event/Chile-mine-rescue-of-2010": (
        BR + "Chile mine rescue of 2010",
        "Britannica: rescue of 33 workers on October 13, 2010, 69 days after the mine's collapse on August 5; 17 days without contact."),
    "https://www.britannica.com/topic/Templars": (
        BR + "Templar",
        "Britannica: 'Originally founded to protect Christian pilgrims to the Holy Land'; Philip IV ordered the arrest of every Templar in France on October 13, 1307."),
    "https://www.whitehousehistory.org/building-the-white-house": (
        "White House Historical Association - Building the White House",
        "WHHA: construction began with a cornerstone on October 13, 1792; President John Adams moved into the residence on November 1, 1800."),
    "https://www.britannica.com/on-this-day/October-13": (
        BR + "On This Day, October 13",
        "Britannica: the White House cornerstone was laid in 1792; it has been home to every U.S. president since 1800, when John and Abigail Adams moved in."),
    "https://www.gutenberg.org/files/17759/17759-h/17759-h.htm": (
        "Project Gutenberg - Protocols of the International Meridian Conference (1884)",
        "Official protocols, session of October 13, 1884: the resolution proposing the meridian through the transit instrument at the Observatory of Greenwich as the initial meridian for longitude passed, ayes 21, noes 1 (San Domingo), abstaining 2 (Brazil, France)."),
    "https://www.britannica.com/topic/International-Prime-Meridian-Conference": (
        BR + "International Prime Meridian Conference",
        "Britannica: the 1884 conference designated the meridian through the Royal Observatory in Greenwich as the prime meridian (0 degrees longitude); it became the basis of the world's time zone system."),
    "https://www.britannica.com/event/Battle-of-Hastings": (
        BR + "Battle of Hastings",
        "Britannica: battle on October 14, 1066, in which William of Normandy defeated Harold II; illustrated with Bayeux Tapestry scenes of the battle."),
    "https://www.britannica.com/topic/Bayeux-Tapestry": (
        BR + "Bayeux Tapestry",
        "Britannica: a band of embroidered linen from the Middle Ages depicting more than 70 scenes of the Norman Conquest of England in 1066, including the Battle of Hastings."),
    "https://www.britannica.com/biography/Chuck-Yeager": (
        BR + "Chuck Yeager",
        "Britannica: on October 14, 1947, Yeager rode the X-1, attached to a B-29 mother ship, to 25,000 feet; the X-1 then rocketed separately to 40,000 feet and broke the sound barrier."),
    "https://www.archives.gov/milestone-documents/aerial-photograph-of-missiles-in-cuba": (
        "National Archives - Aerial photograph of missiles in Cuba (1962)",
        "National Archives: the October 14, 1962 U-2 flight photographed Soviet missile installations in Cuba; in a televised address on October 22, 1962, President Kennedy informed Americans of the missile sites."),
    "https://www.britannica.com/event/Battle-of-Jena": (
        BR + "Battle of Jena",
        "Britannica: October 14, 1806, at Jena and Auerstadt; 'Napoleon smashed the outdated Prussian army'; he completed his conquest of Prussia within six weeks."),
    "https://www.nga.gov/artworks/46114-emperor-napoleon-his-study-tuileries": (
        "National Gallery of Art - The Emperor Napoleon in His Study at the Tuileries",
        "Existing approved source of the portrait question; identifies the sitter as Napoleon. Not re-fetched; inherited from the image ledger."),
    "https://www.nobelprize.org/prizes/peace/1964/king/facts/": (
        "NobelPrize.org - Martin Luther King Jr., Facts",
        "NobelPrize.org: Nobel Peace Prize 1964; prize motivation 'for his non-violent struggle for civil rights for the Afro-American population'."),
    "https://www.britannica.com/on-this-day/October-14": (
        BR + "On This Day, October 14",
        "Britannica, 1964 entry: Martin Luther King, Jr., was named the winner of the Nobel Prize for Peace, cited for his work involving civil rights and social justice."),
    "https://www.britannica.com/topic/Shenzhou": (
        BR + "Shenzhou",
        "Britannica: on October 15, 2003, Shenzhou 5 carried Yang Liwei; China became the third country, after the Soviet Union and the United States, to achieve human spaceflight."),
    "https://science.nasa.gov/mission/cassini/spacecraft/huygens-probe/": (
        "NASA Science - Huygens probe",
        "NASA: the Huygens probe successfully landed on Saturn's largest moon Titan on January 14, 2005."),
    "https://science.nasa.gov/mission/cassini/": (
        "NASA Science - Cassini",
        "NASA mission page lists Cassini's launch as Oct. 15, 1997."),
    "https://www.britannica.com/topic/Gregorian-calendar": (
        BR + "Gregorian calendar",
        "Britannica: proclaimed in 1582 as a reform of the Julian calendar; 'no century year is a leap year unless it is exactly divisible by 400 (e.g., 1600 and 2000).'"),
    # October 1-8 supplement
    "https://www.britannica.com/place/Lagos-Nigeria": (
        BR + "Lagos",
        "Britannica: 'In 1960 Lagos became the capital of independent Nigeria'; Abuja replaced it as federal capital in December 1991."),
    "https://www.fmprc.gov.cn/eng./zy/jj/zggcddwjw100ggs/jszgddzg/202406/t20240606_11377939.html": (
        "Ministry of Foreign Affairs of the PRC - Founding of New China",
        "PRC Foreign Ministry: at 3 pm on October 1, 1949, Mao Zedong proclaimed the founding of the Central People's Government on the Tian'anmen Gate Tower."),
    "https://schulzmuseum.org/about-schulz/schulz-biography/": (
        "Charles M. Schulz Museum - Schulz biography",
        "Schulz Museum: the first Peanuts strip appeared on October 2, 1950; describes Snoopy's evolution and rising popularity within the strip."),
    "https://www.nps.gov/moru/index.htm": (
        "National Park Service - Mount Rushmore National Memorial",
        "NPS: the memorial's figures are George Washington, Thomas Jefferson, Theodore Roosevelt and Abraham Lincoln."),
    "https://www.nps.gov/moru/learn/historyculture/carving-history.htm": (
        "National Park Service - Carving history",
        "NPS: carving ran from October 4, 1927 to October 31, 1941."),
    "https://www.britannica.com/biography/Rembrandt-van-Rijn": (
        BR + "Rembrandt",
        "Britannica: Rembrandt died October 4, 1669; the Night Watch (1640/42), a commissioned militia group portrait, was a turning point in his work."),
    "https://www.britannica.com/biography/Ferdinand-king-of-Bulgaria": (
        BR + "Ferdinand (king of Bulgaria)",
        "Britannica: on October 5, 1908, Ferdinand proclaimed Bulgaria's full independence from the Ottoman Empire and assumed the title of king, or tsar."),
    "https://www.britannica.com/biography/Anwar-Sadat": (
        BR + "Anwar Sadat",
        "Britannica: Sadat died October 6, 1981, assassinated in Cairo; Carter mediated the Sadat-Begin negotiations that produced the Camp David Accords (1978)."),
    "https://www.britannica.com/event/Battle-of-Lepanto": (
        BR + "Battle of Lepanto",
        "Britannica: October 7, 1571, Holy League against the Ottoman Turks; the allies had about 8,000 wounded, 'among them Miguel de Cervantes.'"),
    "https://www.britannica.com/biography/Miguel-de-Cervantes": (
        BR + "Miguel de Cervantes",
        "Britannica: Spanish novelist, creator of Don Quixote; sailed with Don Juan de Austria's fleet that engaged the enemy on October 7 [1571] in the Gulf of Lepanto."),
    "https://www.britannica.com/topic/Opium-Wars": (
        BR + "Opium Wars",
        "Britannica: the Treaty of Nanjing (1842) required China to cede Hong Kong Island to the British; the supplementary Treaty of the Bogue was signed October 8, 1843."),
}


def mc(qid, difficulty, prompt, options, correct, explanation, sources, collections_, related):
    return {"id": qid, "type": "multiple_choice", "difficulty": difficulty, "publicationState": "draft",
            "prompt": prompt, "options": [{"id": i, "text": t} for i, t in options],
            "correctOptionId": correct, "explanation": explanation,
            "sources": [{"displayName": SRC[u][0], "url": u} for u in sources],
            "collectionIds": collections_, "relatedEventIds": related}


def tf(qid, difficulty, prompt, answer, explanation, sources, collections_, related):
    q = mc(qid, difficulty, prompt, [("true", "True"), ("false", "False")], "true" if answer else "false",
           explanation, sources, collections_, related)
    q["type"] = "true_false"
    return q


# (question, calendarDay, role, regions, eras, relation rationale)
OCT_09_15 = [
    (mc("upu-founding-city-bern", "medium",
        "The Universal Postal Union was founded in 1874 by a treaty signed in which Swiss city?",
        [("geneva", "Geneva"), ("bern", "Bern"), ("zurich", "Zurich"), ("basel", "Basel")], "bern",
        "Delegates meeting in Bern signed the Treaty of Bern on October 9, 1874, creating the union for international mail. The anniversary is now marked as World Post Day.",
        ["https://www.upu.int/en/universal-postal-union/about-upu/history",
         "https://blog.nationalmuseum.ch/en/2024/10/the-story-of-the-universal-postal-union/"],
        ["exploration-and-exchange"], ["universal-postal-union-founded-1874"]),
     "10-09", "featured", ["europe", "global"], ["1800-1945"],
     "Asks where the founding treaty was signed, not the date; Geneva is the tempting international-city distractor."),
    (tf("kepler-supernova-last-naked-eye", "medium",
        "Kepler's Supernova, first seen in 1604, is the most recent supernova in our own galaxy to be seen with the naked eye.",
        True,
        "No later supernova in the Milky Way has been seen with the naked eye. The bright Supernova 1987A was in the Large Magellanic Cloud, a neighboring galaxy.",
        ["https://www.nasa.gov/history/420-years-ago-astronomer-johannes-kepler-observes-a-supernova/"],
        ["science-and-innovation"], ["keplers-supernova-first-sighted-1604"]),
     "10-09", "additional", ["europe"], ["early-modern"],
     "Explains why the 1604 sighting still matters; the 1987 supernova is the natural misconception."),
    (mc("che-guevara-birth-country", "medium",
        "In which country was the revolutionary Che Guevara born?",
        [("cuba", "Cuba"), ("bolivia", "Bolivia"), ("mexico", "Mexico"), ("argentina", "Argentina")], "argentina",
        "Ernesto 'Che' Guevara was born in Rosario, Argentina, in 1928. He became a leader of the Cuban Revolution and was killed in Bolivia in 1967.",
        ["https://www.britannica.com/biography/Che-Guevara"],
        ["revolutions"], ["che-guevara-executed-1967"]),
     "10-09", "additional", ["americas"], ["cold-war"],
     "Recognizable figure; asks about his origins rather than the circumstances of his death. Cuba and Bolivia are plausible because of his later career."),
    (mc("hangeul-earlier-writing", "medium",
        "Before King Sejong introduced the Hangeul alphabet, Koreans mainly wrote using what?",
        [("chinese-characters", "Chinese characters"), ("japanese-kana", "Japanese kana"),
         ("mongolian-script", "Mongolian script"), ("sanskrit-script", "Sanskrit script")], "chinese-characters",
        "Korean was written mainly with Chinese characters (Hanja), which were hard to learn for people who spoke Korean rather than Chinese. Sejong's alphabet, completed in 1443 and proclaimed in 1446, gave Korean its own script.",
        ["https://www.korea.net/NewsFocus/Culture/view?articleId=145583"],
        ["society-culture-and-ideas"], ["hangeul-proclaimed-1446"]),
     "10-09", "additional", ["asia"], ["medieval"],
     "Explains why the new alphabet mattered; depends on the proposed supplementary event hangeul-proclaimed-1446."),
    (mc("outer-space-treaty-prohibition", "medium",
        "Under the 1967 Outer Space Treaty, which of these may a country NOT do?",
        [("send-astronauts", "Send astronauts to the Moon"), ("claim-moon", "Claim the Moon as its own territory"),
         ("research-moon", "Carry out scientific research on the Moon"),
         ("launch-satellites", "Launch satellites into Earth orbit")], "claim-moon",
        "The treaty says outer space, including the Moon, is not subject to national appropriation by claim of sovereignty. Exploration and scientific investigation remain free for all states.",
        ["https://2009-2017.state.gov/t/isn/5181.htm"],
        ["science-and-innovation"], ["outer-space-treaty-enters-force-1967"]),
     "10-10", "additional", ["global"], ["space-age", "cold-war"],
     "Tests the treaty's best-known principle rather than its date; the three permitted activities are explicitly free under Article I."),
    (tf("apollo-7-flew-around-moon", "easy",
        "Apollo 7, the first crewed Apollo mission, flew around the Moon.",
        False,
        "Apollo 7 stayed in Earth orbit for about 11 days in October 1968. Apollo 8 carried the first humans to orbit the Moon that December.",
        ["https://www.nasa.gov/mission/apollo-7/", "https://www.nasa.gov/mission/apollo-8/"],
        ["science-and-innovation"], ["apollo-7-launches-1968"]),
     "10-11", "featured", ["americas"], ["space-age", "cold-war"],
     "Corrects a natural assumption about the first crewed Apollo flight without asking for crew names or dates."),
    (mc("vatican-ii-mass-language", "easy",
        "The Second Vatican Council allowed Mass to be celebrated in local languages instead of only in which language?",
        [("latin", "Latin"), ("greek", "Greek"), ("hebrew", "Hebrew"), ("italian", "Italian")], "latin",
        "The council's Constitution on the Sacred Liturgy authorized vernacular languages instead of Latin in the Mass, one of its most visible changes to Catholic worship.",
        ["https://www.britannica.com/event/Second-Vatican-Council"],
        ["society-culture-and-ideas"], ["second-vatican-council-opens-1962"]),
     "10-11", "additional", ["europe", "global"], ["1945-present"],
     "The council's most widely felt change; Italian is a tempting distractor because of the Vatican's location."),
    (mc("mary-rose-monarch", "easy",
        "The Mary Rose, raised from the seabed in 1982, was the warship of which English monarch?",
        [("elizabeth-i", "Elizabeth I"), ("richard-iii", "Richard III"), ("henry-viii", "Henry VIII"),
         ("charles-i", "Charles I")], "henry-viii",
        "The Mary Rose served Henry VIII for 34 years before sinking in 1545. Its hull was raised on October 11, 1982.",
        ["https://maryrose.org/discover/history/recovering-the-mary-rose/"],
        ["wars-and-conflicts"], ["mary-rose-raised-1982"]),
     "10-11", "additional", ["europe"], ["early-modern"],
     "Connects the 1982 recovery to the Tudor king, a recognizable anchor. All four options are English monarchs."),
    (mc("equatorial-guinea-colonial-power", "easy",
        "Equatorial Guinea became independent in 1968 from which European country?",
        [("portugal", "Portugal"), ("france", "France"), ("belgium", "Belgium"), ("spain", "Spain")], "spain",
        "Equatorial Guinea gained independence from Spain on October 12, 1968.",
        ["https://history.state.gov/countries/equatorial-guinea"],
        ["leaders-and-power"], ["equatorial-guinea-independence-1968"]),
     "10-12", "featured", ["africa"], ["decolonization", "1945-present"],
     "Same accessible pattern as the published Malta question; all four were colonial powers in Africa."),
    (mc("oktoberfest-original-celebration", "medium",
        "Munich's first Oktoberfest, in 1810, celebrated what?",
        [("military-victory", "A military victory"), ("royal-wedding", "A royal wedding"),
         ("harvest", "The end of the harvest"), ("coronation", "A king's coronation")], "royal-wedding",
        "The festival began on October 12, 1810, to celebrate the marriage of the Bavarian crown prince, later King Louis I, to Princess Therese. The Theresienwiese grounds are named after her.",
        ["https://www.britannica.com/topic/Oktoberfest"],
        ["society-culture-and-ideas"], ["first-oktoberfest-1810"]),
     "10-12", "additional", ["europe"], ["1800-1945"],
     "A fun origin story; the harvest is the common assumption. The coronation distractor is separate from the wedding."),
    (mc("templars-original-purpose", "medium",
        "The Knights Templar were originally founded to do what?",
        [("protect-pilgrims", "Protect Christian pilgrims to the Holy Land"),
         ("guard-pope", "Guard the pope in Rome"), ("defend-france", "Defend the king of France"),
         ("escort-merchants", "Escort merchants on the Silk Roads")], "protect-pilgrims",
        "The order began around 1119-20 to protect Christian pilgrims travelling to Jerusalem. It later took on wider military and banking roles before Philip IV of France ordered its members arrested in 1307.",
        ["https://www.britannica.com/topic/Templars"],
        ["wars-and-conflicts"], ["templars-arrested-in-france-1307"]),
     "10-13", "additional", ["europe", "middle-east"], ["medieval"],
     "Asks why the order existed instead of the arrest details; the French-king distractor echoes the event without being correct."),
    (mc("white-house-first-president", "medium",
        "Which U.S. president was the first to live in the White House?",
        [("george-washington", "George Washington"), ("john-adams", "John Adams"),
         ("thomas-jefferson", "Thomas Jefferson"), ("james-madison", "James Madison")], "john-adams",
        "Construction began with a cornerstone on October 13, 1792. John Adams moved in on November 1, 1800, near the end of his term, so Washington never lived there.",
        ["https://www.whitehousehistory.org/building-the-white-house", "https://www.britannica.com/on-this-day/October-13"],
        ["leaders-and-power"], ["white-house-cornerstone-laid-1792"]),
     "10-13", "additional", ["americas"], ["early-modern"],
     "Classic misconception: Washington oversaw construction but the house was first occupied in 1800."),
    (mc("prime-meridian-greenwich", "easy",
        "In 1884, an international conference chose the meridian through an observatory in which place as the standard for longitude?",
        [("paris", "Paris"), ("washington", "Washington, D.C."), ("rome", "Rome"), ("greenwich", "Greenwich")], "greenwich",
        "On October 13, 1884, the International Meridian Conference in Washington voted to propose the Greenwich meridian as the initial meridian for longitude. France abstained and kept the Paris meridian for some years.",
        ["https://www.gutenberg.org/files/17759/17759-h/17759-h.htm",
         "https://www.britannica.com/topic/International-Prime-Meridian-Conference"],
        ["science-and-innovation"], ["prime-meridian-greenwich-1884"]),
     "10-13", "additional", ["europe", "global"], ["1800-1945"],
     "Depends on the proposed supplementary event prime-meridian-greenwich-1884. Washington (the host city) and Paris (France's rival meridian) are deliberate near-misses."),
    (mc("bayeux-tapestry-hastings", "easy",
        "Which famous work of medieval textile art shows the Norman Conquest and the Battle of Hastings?",
        [("bayeux", "The Bayeux Tapestry"), ("apocalypse", "The Apocalypse Tapestry"),
         ("lady-unicorn", "The Lady and the Unicorn tapestries"),
         ("devonshire-hunting", "The Devonshire Hunting Tapestries")], "bayeux",
        "The Bayeux Tapestry is an embroidered linen band with more than 70 scenes of the Norman Conquest of 1066, including the battle at Hastings.",
        ["https://www.britannica.com/topic/Bayeux-Tapestry", "https://www.britannica.com/event/Battle-of-Hastings"],
        ["wars-and-conflicts"], ["battle-of-hastings-1066"]),
     "10-14", "featured", ["europe"], ["medieval"],
     "The best-known artwork tied to the battle; text-only, so no new image rights or delivery are needed. Distractors are other famous medieval tapestries."),
    (tf("yeager-x1-runway-takeoff", "medium",
        "In 1947, Chuck Yeager's Bell X-1 took off from a runway under its own power before breaking the sound barrier.",
        False,
        "The X-1 was carried aloft attached to a B-29 mother ship and released at about 25,000 feet. It then climbed on rocket power to about 40,000 feet, where Yeager flew faster than sound.",
        ["https://www.britannica.com/biography/Chuck-Yeager"],
        ["science-and-innovation"], ["yeager-breaks-sound-barrier-1947"]),
     "10-14", "additional", ["americas"], ["cold-war"],
     "A surprising detail of how the record flight worked, rather than the speed figure."),
    (mc("cuban-missile-crisis-president", "easy",
        "Which U.S. president told the nation in October 1962 that Soviet missile sites had been found in Cuba?",
        [("eisenhower", "Dwight D. Eisenhower"), ("kennedy", "John F. Kennedy"),
         ("johnson", "Lyndon B. Johnson"), ("nixon", "Richard Nixon")], "kennedy",
        "U-2 photographs taken on October 14, 1962, revealed Soviet missile installations. President Kennedy announced them in a televised address on October 22, beginning the public crisis.",
        ["https://www.archives.gov/milestone-documents/aerial-photograph-of-missiles-in-cuba"],
        ["leaders-and-power"], ["u2-photographs-missiles-in-cuba-1962"]),
     "10-14", "additional", ["americas"], ["cold-war"],
     "Anchors the reconnaissance flight in the crisis most players know; options are the four consecutive Cold War presidents."),
    (mc("shenzhou-5-earlier-nations", "medium",
        "Shenzhou 5 made China the third country to send a person into space on its own spacecraft. Which two countries did it first?",
        [("ussr-us", "The Soviet Union and the United States"), ("us-france", "The United States and France"),
         ("ussr-japan", "The Soviet Union and Japan"), ("us-india", "The United States and India")], "ussr-us",
        "On October 15, 2003, Yang Liwei orbited Earth aboard Shenzhou 5. China became the third country, after the Soviet Union and the United States, to achieve human spaceflight.",
        ["https://www.britannica.com/topic/Shenzhou"],
        ["science-and-innovation"], ["shenzhou-5-yang-liwei-2003"]),
     "10-15", "featured", ["asia"], ["contemporary"],
     "Places China's milestone in the global space race; each distractor pairs a real space power with a country that had not launched a person."),
    (mc("huygens-titan-landing", "medium",
        "The Cassini mission carried the Huygens probe, which landed on which moon of Saturn?",
        [("enceladus", "Enceladus"), ("mimas", "Mimas"), ("titan", "Titan"), ("iapetus", "Iapetus")], "titan",
        "Huygens landed on Titan, Saturn's largest moon, on January 14, 2005, after riding to Saturn aboard Cassini.",
        ["https://science.nasa.gov/mission/cassini/spacecraft/huygens-probe/"],
        ["science-and-innovation"], ["cassini-launches-to-saturn-1997"]),
     "10-15", "additional", ["global"], ["contemporary"],
     "The mission's headline achievement; all four options are moons of Saturn."),
    (mc("gregorian-century-leap-year", "medium",
        "Under the Gregorian calendar, which of these years was NOT a leap year?",
        [("1600", "1600"), ("1896", "1896"), ("1900", "1900"), ("2000", "2000")], "1900",
        "A century year is a leap year only if it divides exactly by 400. That makes 1600 and 2000 leap years but not 1900; 1896 follows the ordinary four-year rule.",
        ["https://www.britannica.com/topic/Gregorian-calendar"],
        ["science-and-innovation"], ["gregorian-calendar-takes-effect-1582"]),
     "10-15", "additional", ["europe", "global"], ["early-modern"],
     "Tests the reform's lasting rule rather than the 1582 date or the pope's name."),
]

# (question id, event id, day, role, regions, eras, source urls, rationale, rights evidence or None)
LINKS_09_15 = [
    ("xinhai-revolution-qing-dynasty", "wuchang-uprising-1911", "10-10", "featured", ["asia"], ["1800-1945"],
     ["https://www.britannica.com/event/Chinese-Revolution-1911-1912"],
     "Britannica regards the October 10 Wuchang mutiny as the formal beginning of the revolution that overthrew the Qing. The question asks which dynasty that revolution ended.", None),
    ("identify-sun-yat-sen", "wuchang-uprising-1911", "10-10", "featured", ["asia"], ["1800-1945"],
     ["https://www.britannica.com/event/Chinese-Revolution-1911-1912", "https://www.britannica.com/biography/Sun-Yat-sen"],
     "Britannica: after the Wuchang rising Sun returned from abroad and was elected provisional president. The portrait identifies the revolution's best-known leader.",
     "editorial/batches/2026-09-l7-second-accessible-global-history/review-ledger.json"),
    ("order-pacific-independence-milestones", "fiji-independence-1970", "10-10", "additional", ["oceania"], ["1945-present"],
     ["https://thecommonwealth.org/our-member-countries/fiji"],
     "The ordering includes 'Fiji becomes independent' (1970), the event itself.", None),
    ("order-americas-milestones", "columbus-sights-land-1492", "10-12", "additional", ["americas", "europe"], ["early-modern"],
     ["https://www.britannica.com/biography/Christopher-Columbus"],
     "The ordering includes 'Columbus reaches the Caribbean' (1492), the landfall itself.", None),
    ("columbian-exchange-meaning", "columbus-sights-land-1492", "10-12", "additional", ["americas", "europe"], ["early-modern"],
     ["https://nationalhumanitiescenter.org/tserve/nattrans/ntecoindian/essays/columbian.htm"],
     "The exchange named after Columbus began with the 1492 crossing; the question explains its consequences rather than the landfall.", None),
    ("voskhod-1-multiperson-crew", "voskhod-1-launches-1964", "10-12", "additional", ["europe", "asia"], ["space-age", "cold-war"],
     ["https://www.britannica.com/technology/Voskhod-spacecraft"],
     "Exact match: the question asks which spacecraft first carried a multi-person crew. Hard; kept as published.", None),
    ("order-african-independence-1960s", "equatorial-guinea-independence-1968", "10-12", "featured", ["africa"], ["decolonization", "1945-present"],
     ["https://history.state.gov/countries/equatorial-guinea"],
     "The ordering includes 'Equatorial Guinea becomes independent' (October 12, 1968), the featured event.", None),
    ("chile-miners-days-underground", "chilean-miners-rescued-2010", "10-13", "featured", ["americas"], ["contemporary"],
     ["https://www.britannica.com/event/Chile-mine-rescue-of-2010"],
     "Exact match on the rescue. The event summary states 69 days, so reading the story first reveals the answer; that is learning, not leakage in the quiz itself.", None),
    ("identify-napoleon-bonaparte", "battles-of-jena-auerstedt-1806", "10-14", "additional", ["europe"], ["1800-1945", "revolutionary"],
     ["https://www.britannica.com/event/Battle-of-Jena", "https://www.nga.gov/artworks/46114-emperor-napoleon-his-study-tuileries"],
     "Britannica names Napoleon as the victor at Jena-Auerstedt. The 1812 portrait identifies him without depending on the battle.",
     "editorial/batches/2026-09-l7-image-identification/review-ledger.json"),
    ("identify-martin-luther-king-jr", "mlk-awarded-nobel-peace-prize-1964", "10-14", "additional", ["americas"], ["cold-war"],
     ["https://www.nobelprize.org/prizes/peace/1964/king/facts/"],
     "The portrait's explanation already cites his 1964 Nobel Peace Prize. Depends on the proposed supplementary event.",
     "editorial/batches/2026-09-l7-image-identification/review-ledger.json"),
    ("order-korean-cultural-printing-milestones", "hangeul-proclaimed-1446", "10-09", "additional", ["asia"], ["medieval"],
     ["https://www.unesco.org/en/memory-world/hunminjeongum-manuscript"],
     "The ordering ends with 'Hunminjeongeum is published' (1446), the event itself. Depends on the proposed supplementary event.", None),
]

SUPP_EVENTS = [
    ("10-09", {
        "id": "hangeul-proclaimed-1446", "title": "King Sejong proclaims the Korean alphabet", "year": "1446",
        "historicalDate": "October 9, 1446",
        "dateNote": "The Hunminjeongeum was published in the ninth lunar month of 1446; South Korea marks the anniversary as Hangeul Day on October 9.",
        "summary": "The Hunminjeongeum introduces Hangeul, a new alphabet for writing Korean.",
        "description": "On October 9, 1446, as South Korea reckons the date, King Sejong the Great proclaimed the Korean alphabet in the Hunminjeongeum, 'the proper sounds for instructing the people.' His scholars had completed the script in 1443 so that ordinary people could read and write more easily than with Chinese characters.",
        "notificationTitle": None, "notificationBody": None,
        "sources": [
            {"name": "Korea.net - The story of Hangeul", "url": "https://www.korea.net/NewsFocus/Culture/view?articleId=145583"},
            {"name": "UNESCO Memory of the World - Hunminjeongum manuscript", "url": "https://www.unesco.org/en/memory-world/hunminjeongum-manuscript"}],
        "images": []}, ["asia"], ["medieval"],
     "Adds the Korean alphabet's proclamation, an accessible Asian cultural milestone the slate lacks, and lets the published Korean printing ordering question link to the day."),
    ("10-13", {
        "id": "prime-meridian-greenwich-1884", "title": "Greenwich is chosen as the prime meridian", "year": "1884",
        "historicalDate": "October 13, 1884", "dateNote": None,
        "summary": "An international conference votes for the meridian through Greenwich as the world's zero of longitude.",
        "description": "On October 13, 1884, the International Meridian Conference in Washington, D.C., voted 21 to 1, with two abstentions, to propose the meridian through the Royal Observatory at Greenwich as the standard for longitude. It became the basis of the world's time zones.",
        "notificationTitle": None, "notificationBody": None,
        "sources": [
            {"name": "Project Gutenberg - Protocols of the International Meridian Conference (1884)", "url": "https://www.gutenberg.org/files/17759/17759-h/17759-h.htm"},
            {"name": BR + "International Prime Meridian Conference", "url": "https://www.britannica.com/topic/International-Prime-Meridian-Conference"}],
        "images": []}, ["europe", "global"], ["1800-1945"],
     "A globally relevant science and standards milestone with a clean quiz hook; the day currently has no science event."),
    ("10-14", {
        "id": "mlk-awarded-nobel-peace-prize-1964", "title": "Martin Luther King Jr. is named winner of the Nobel Peace Prize", "year": "1964",
        "historicalDate": "October 14, 1964", "dateNote": None,
        "summary": "The civil rights leader is honored for his nonviolent struggle against racial discrimination.",
        "description": "On October 14, 1964, Martin Luther King Jr. was named the winner of the Nobel Peace Prize. The prize recognized his nonviolent struggle for civil rights for Black Americans.",
        "notificationTitle": None, "notificationBody": None,
        "sources": [
            {"name": BR + "On This Day, October 14", "url": "https://www.britannica.com/on-this-day/October-14"},
            {"name": "NobelPrize.org - Martin Luther King Jr., Facts", "url": "https://www.nobelprize.org/prizes/peace/1964/king/facts/"}],
        "images": []}, ["americas"], ["cold-war"],
     "A widely resonant social-change milestone; lets the published King portrait link to the day."),
]

OCT_01_08 = [
    (mc("nigeria-capital-at-independence", "medium",
        "Which city was Nigeria's capital when the country became independent in 1960?",
        [("abuja", "Abuja"), ("lagos", "Lagos"), ("kano", "Kano"), ("ibadan", "Ibadan")], "lagos",
        "Lagos became the capital of independent Nigeria in 1960. Abuja replaced it as the federal capital in December 1991, and Lagos remains the country's leading commercial city.",
        ["https://www.britannica.com/place/Lagos-Nigeria"],
        ["leaders-and-power"], ["nigeria-independence-1960"]),
     "10-01", "additional", ["africa"], ["decolonization", "1945-present"],
     "Abuja, today's capital, is the deliberate trap; the question rewards knowing the country's history rather than the independence date."),
    (mc("prc-proclamation-tiananmen", "medium",
        "From which Beijing landmark did Mao Zedong proclaim the People's Republic of China in 1949?",
        [("temple-of-heaven", "The Temple of Heaven"), ("summer-palace", "The Summer Palace"),
         ("tiananmen", "Tiananmen, the Gate of Heavenly Peace"), ("drum-tower", "The Drum Tower")], "tiananmen",
        "On October 1, 1949, Mao proclaimed the founding of the new government from the Tiananmen Gate Tower in Beijing.",
        ["https://www.fmprc.gov.cn/eng./zy/jj/zggcddwjw100ggs/jszgddzg/202406/t20240606_11377939.html"],
        ["revolutions"], ["peoples-republic-china-proclaimed-1949"]),
     "10-01", "additional", ["asia"], ["1945-present"],
     "A recognizable landmark tied to the proclamation; all four options are historic Beijing sites."),
    (mc("snoopy-comic-strip", "easy",
        "Snoopy is a character from which comic strip?",
        [("garfield", "Garfield"), ("calvin-hobbes", "Calvin and Hobbes"), ("blondie", "Blondie"), ("peanuts", "Peanuts")], "peanuts",
        "Snoopy belongs to Charles Schulz's Peanuts, which first appeared on October 2, 1950, in seven newspapers. His popularity grew in the 1960s as he became a more imaginative character.",
        ["https://schulzmuseum.org/about-schulz/schulz-biography/"],
        ["society-culture-and-ideas"], ["peanuts-debuts-1950"]),
     "10-02", "additional", ["americas"], ["1945-present"],
     "Light, widely recognized hook for an imageless event; all four are long-running newspaper strips."),
    (mc("mount-rushmore-not-carved", "medium",
        "Which of these presidents is NOT carved on Mount Rushmore?",
        [("washington", "George Washington"), ("jefferson", "Thomas Jefferson"), ("lincoln", "Abraham Lincoln"),
         ("fdr", "Franklin D. Roosevelt")], "fdr",
        "The memorial shows George Washington, Thomas Jefferson, Theodore Roosevelt and Abraham Lincoln. Carving began on October 4, 1927, and Franklin D. Roosevelt is often confused with his cousin Theodore.",
        ["https://www.nps.gov/moru/index.htm", "https://www.nps.gov/moru/learn/historyculture/carving-history.htm"],
        ["leaders-and-power"], ["mount-rushmore-carving-begins-1927"]),
     "10-04", "additional", ["americas"], ["1800-1945"],
     "Plays on the familiar Theodore/Franklin Roosevelt mix-up without listing Theodore."),
    (mc("night-watch-painter", "easy",
        "Which Dutch artist painted The Night Watch?",
        [("vermeer", "Johannes Vermeer"), ("rembrandt", "Rembrandt"), ("hals", "Frans Hals"),
         ("van-gogh", "Vincent van Gogh")], "rembrandt",
        "Rembrandt painted The Night Watch, a commissioned group portrait of a militia company, around 1640-42. He died in Amsterdam on October 4, 1669.",
        ["https://www.britannica.com/biography/Rembrandt-van-Rijn"],
        ["society-culture-and-ideas"], ["rembrandt-dies-1669"]),
     "10-04", "additional", ["europe"], ["early-modern"],
     "Identifies his most famous work instead of his death; all four options are Dutch painters."),
    (mc("lepanto-wounded-writer", "medium",
        "Which famous writer was wounded fighting at the Battle of Lepanto in 1571?",
        [("cervantes", "Miguel de Cervantes"), ("shakespeare", "William Shakespeare"), ("lope-de-vega", "Lope de Vega"),
         ("montaigne", "Michel de Montaigne")], "cervantes",
        "Cervantes, later the author of Don Quixote, sailed with the Holy League fleet and was among its wounded at Lepanto.",
        ["https://www.britannica.com/event/Battle-of-Lepanto", "https://www.britannica.com/biography/Miguel-de-Cervantes"],
        ["wars-and-conflicts"], ["battle-of-lepanto-1571"]),
     "10-07", "additional", ["europe", "middle-east"], ["early-modern"],
     "A memorable human link to a remote battle; distractors are contemporary European writers."),
    (mc("opium-war-ceded-island", "medium",
        "The settlement of the First Opium War, extended by the 1843 Treaty of the Bogue, made China cede which island to Britain?",
        [("macau", "Macau"), ("taiwan", "Taiwan"), ("hainan", "Hainan"), ("hong-kong", "Hong Kong Island")], "hong-kong",
        "The 1842 Treaty of Nanjing required China to cede Hong Kong Island to Britain. The supplementary Treaty of the Bogue, signed October 8, 1843, added extraterritoriality and most-favoured-nation rights.",
        ["https://www.britannica.com/topic/Opium-Wars"],
        ["wars-and-conflicts"], ["treaty-of-the-bogue-1843"]),
     "10-08", "additional", ["asia", "europe"], ["1800-1945"],
     "Gives the treaty's lasting consequence rather than clause recall; Macau is the nearby Portuguese-held trap."),
]

LINKS_01_08 = [
    ("camp-david-egyptian-president", "anwar-sadat-assassinated-1981", "10-06", "additional", ["middle-east", "africa"], ["cold-war"],
     ["https://www.britannica.com/biography/Anwar-Sadat"],
     "Britannica covers both the 1978 Camp David Accords and Sadat's 1981 assassination. The question names the peace-making president the event describes.", None),
    ("ferdinand-bulgarian-independence", "bulgaria-declares-independence-1908", "10-05", "additional", ["europe"], ["1800-1945"],
     ["https://www.britannica.com/biography/Ferdinand-king-of-Bulgaria"],
     "Exact match. The October 2-8 slate reserved it because it is hard and name-heavy; proposed now as supplementary supply, not as the day's accessible hook.", None),
]


def canonical_questions():
    found = {}
    for path in sorted(glob.glob("content/quizzes/questions/*.json")):
        for q in json.load(open(path))["questions"]:
            found[q["id"]] = (path, q)
    return found


def write(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")


def check(url):
    return {"url": url, "status": "verified_supporting", "checkedOn": CHECKED, "note": SRC[url][1]}


def build_quiz_batch(batch_id, pack, new, links, bank, events):
    out = f"editorial/batches/{batch_id}"
    candidates, entries, link_records = [], [], []
    for q, day, role, regions, eras, rel in new:
        assert q["id"] not in bank, q["id"]
        assert all(r in events for r in q["relatedEventIds"]), q["id"]
        urls = [s["url"] for s in q["sources"]]
        candidates.append({"candidateId": q["id"], "kind": "quiz", "proposedCanonicalId": q["id"],
                           "workingClaim": f"{q['prompt']} Answer: {next(o['text'] for o in q['options'] if o['id'] == q['correctOptionId'])}. Proposed {role} link on {day}: {', '.join(q['relatedEventIds'])}. {rel}",
                           "sourceUrls": urls})
        entries.append({"candidateId": q["id"], "canonicalId": q["id"], "reviewStatus": "source_verified",
                        "sourceChecks": [check(u) for u in urls], "imageRightsStatus": "not_applicable",
                        "regions": regions, "eras": eras, "calendarDays": [day],
                        "notes": NOTE_NEW.format(rel=rel)})
    for qid, event_id, day, role, regions, eras, urls, rel, rights in links:
        path, q = bank[qid]
        assert q["publicationState"] == "published", qid
        assert event_id in events, event_id
        assert event_id not in q.get("relatedEventIds", []), qid
        snapshot = hashlib.sha256(json.dumps(q, separators=(",", ":")).encode()).hexdigest()
        link_records.append({"questionId": qid, "canonicalPack": path, "canonicalSnapshotSha256": snapshot,
                             "currentRelatedEventIds": q.get("relatedEventIds", []),
                             "addRelatedEventIds": [event_id], "calendarDay": day, "eventRole": role,
                             "relationshipRationale": rel,
                             "existingImageRightsEvidence": rights})
        candidates.append({"candidateId": f"link-{qid}", "kind": "quiz", "proposedCanonicalId": qid,
                           "workingClaim": f"Propose {role} relation on {day} from unchanged published {qid} to {event_id}. {rel}",
                           "sourceUrls": urls})
        extra = (f"Image rights inherited from {rights}; owned URL and provenance unchanged, no new download."
                 if rights else "No image.")
        entries.append({"candidateId": f"link-{qid}", "canonicalId": qid, "reviewStatus": "source_verified",
                        "sourceChecks": [check(u) for u in urls],
                        "imageRightsStatus": "verified" if rights else "not_applicable",
                        "regions": regions, "eras": eras, "calendarDays": [day],
                        "notes": NOTE_LINK.format(extra=extra)})
    write(f"{out}/batch.json", {"schemaVersion": 1, "batchId": batch_id, "candidateFile": "candidates.json",
                                "reviewLedgerFile": "review-ledger.json", "eventIds": [], "monthDays": [],
                                "quizPackFilenames": [pack]})
    write(f"{out}/candidates.json", {"schemaVersion": 1, "batchId": batch_id, "candidates": candidates})
    write(f"{out}/review-ledger.json", {"schemaVersion": 1, "batchId": batch_id, "entries": entries})
    write(f"{out}/draft-questions.json", {"schemaVersion": 1, "questions": [n[0] for n in new]})
    write(f"{out}/proposed-links.json", {"schemaVersion": 1, "batchId": batch_id, "existingQuestionLinks": link_records})
    write(f"{out}/distribution-audit.json", audit(batch_id, new, links, bank))


def audit(batch_id, new, links, bank):
    qs = [n[0] for n in new]
    hooks = qs + [bank[l[0]][1] for l in links]
    tags = [(n[1], n[3], n[4]) for n in new] + [(l[2], l[4], l[5]) for l in links]
    return {
        "schemaVersion": 1, "batchId": batch_id, "auditedOn": CHECKED,
        "state": "editorial_only_awaiting_owner_content_approval",
        "newQuestionCount": len(qs), "reusedQuestionCount": len(links),
        "proposedNewRelationCount": len(qs) + len(links),
        "newQuestionTypes": dict(collections.Counter(q["type"] for q in qs)),
        "newQuestionDifficulties": dict(collections.Counter(q["difficulty"] for q in qs)),
        "hookTypes": dict(collections.Counter(q["type"] for q in hooks)),
        "hookDifficulties": dict(collections.Counter(q["difficulty"] for q in hooks)),
        "newMultipleChoiceCorrectPositions": dict(sorted(collections.Counter(
            str([o["id"] for o in q["options"]].index(q["correctOptionId"]) + 1)
            for q in qs if q["type"] == "multiple_choice").items())),
        "newTrueFalseAnswers": dict(collections.Counter(q["correctOptionId"] for q in qs if q["type"] == "true_false")),
        "hookCollectionMemberships": dict(collections.Counter(c for q in hooks for c in q["collectionIds"])),
        "regionTagMemberships": dict(collections.Counter(r for t in tags for r in t[1])),
        "eraTagMemberships": dict(collections.Counter(e for t in tags for e in t[2])),
        "calendarDayHookCounts": dict(sorted(collections.Counter(t[0] for t in tags).items())),
    }


def build_supplementary_events(events):
    batch_id = "2026-10-09-15-supplementary-events"
    out = f"editorial/batches/{batch_id}"
    drafts, candidates, entries, additions = [], [], [], []
    for day, event, regions, eras, why in SUPP_EVENTS:
        assert event["id"] not in events, event["id"]
        urls = [s["url"] for s in event["sources"]]
        drafts.append(event)
        candidates.append({"candidateId": f"oct-{day[3:]}-{event['id'].rsplit('-', 1)[0]}", "kind": "event",
                           "proposedCanonicalId": event["id"],
                           "workingClaim": f"{event['historicalDate']}: {event['summary']} {why}",
                           "sourceUrls": urls})
        entries.append({"candidateId": candidates[-1]["candidateId"], "canonicalId": event["id"],
                        "reviewStatus": "source_verified", "sourceChecks": [check(u) for u in urls],
                        "imageRightsStatus": "not_applicable", "regions": regions, "eras": eras,
                        "calendarDays": [day],
                        "notes": "Drafted and source-checked by Claude; awaiting owner editorial review. Supplements the 2026-10-09-15-historical-events batch without editing it."})
        additions.append({"monthDay": day, "addAdditionalEventId": event["id"], "rationale": why})
    write(f"{out}/batch.json", {"schemaVersion": 1, "batchId": batch_id, "candidateFile": "candidates.json",
                                "reviewLedgerFile": "review-ledger.json", "eventIds": [], "monthDays": [],
                                "quizPackFilenames": []})
    write(f"{out}/candidates.json", {"schemaVersion": 1, "batchId": batch_id, "candidates": candidates})
    write(f"{out}/review-ledger.json", {"schemaVersion": 1, "batchId": batch_id, "entries": entries})
    write(f"{out}/draft-events.json", {"events": drafts})
    write(f"{out}/proposed-day-additions.json", {"schemaVersion": 1, "batchId": batch_id,
                                                 "targetBatch": "2026-10-09-15-historical-events",
                                                 "additions": additions})
    return {e["id"] for e in drafts}


def main():
    bank = canonical_questions()
    canonical_events = {e["id"] for e in json.load(open("content/events.json"))["events"]}
    week = {e["id"] for e in json.load(open("editorial/batches/2026-10-09-15-historical-events/draft-events.json"))["events"]}
    supplementary = build_supplementary_events(canonical_events | week)
    build_quiz_batch("2026-10-09-15-connected-quiz", "150-connected-october-09-15.json",
                     OCT_09_15, LINKS_09_15, bank, week | supplementary)
    build_quiz_batch("2026-10-01-08-connected-quiz-supplement", "160-connected-october-01-08-supplement.json",
                     OCT_01_08, LINKS_01_08, bank, canonical_events)
    new_ids = [n[0]["id"] for n in OCT_09_15 + OCT_01_08]
    assert len(new_ids) == len(set(new_ids))
    print(f"{len(OCT_09_15)} + {len(OCT_01_08)} new drafts, {len(LINKS_09_15)} + {len(LINKS_01_08)} links, "
          f"{len(supplementary)} supplementary events")


if __name__ == "__main__":
    main()
