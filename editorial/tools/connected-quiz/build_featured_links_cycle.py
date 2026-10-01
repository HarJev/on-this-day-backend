#!/usr/bin/env python3
"""Writes the featured-story connected-quiz drafts for October 1 to December 25 and later gaps.

Scratch editorial tooling, not used by the runtime or importers. Run from the
repository root:

    python3 editorial/tools/connected-quiz/build_featured_links_cycle.py

On 2026-10-01 only 18 of the next 90 Daily Challenges could open with a question
about that day's featured story: October 16 to December 25 had no linked questions
at all, and a few later days (January 11, 16 and 19, August 22 to 29) had none for
their featured event. This cycle writes editorial/batches/2026-10-01-12-25-connected-quiz/
with new draft questions (proposed pack 200-connected-featured-stories.json) and
relation proposals linking unchanged published questions to published events.

Every new question's sources were opened in the authoring session on 2026-10-01;
each source check note records what that page was verified to state. For reused
questions the relation was checked against the event; their own original sources
are inherited and were not re-fetched unless a check note says otherwise.

Ledger entries are written source_verified; promote_featured_links.py approves and
publishes them. Link proposals record a snapshot hash of the canonical question as
inspected (json.dumps(question, separators=(",", ":")) in file key order, the same
scheme as the earlier cycles), so a later change to that question is detectable.

Options are written in display order unless a question is listed in ROTATE; the
correct option is marked with a leading "*".
"""
import collections
import glob
import hashlib
import json
import os
import re

CHECKED = "2026-10-01"
BATCH_ID = "2026-10-01-12-25-connected-quiz"
PACK = "200-connected-featured-stories.json"
BR = "Encyclopaedia Britannica - "
B = "https://www.britannica.com/"
NOTE_NEW = ("Draft researched and source-checked by Claude on 2026-10-01. Relation: {rel} "
            "Distractors checked for exclusivity under the question's wording; difficulty is an "
            "editorial estimate, not user-tested.")
NOTE_LINK = ("Relationship review only; original question and answer are unchanged and remain "
             "published. {extra}")
INHERITED = "Existing source of the reused published question. Not re-fetched; inherited."


def day(month, d):
    return f"{B}on-this-day/{month}-{d}"


# url -> (display name, what the page was verified to state on CHECKED)
SRC = {
    "https://www.nasa.gov/history/65-years-ago-nasa-begins-operations/": (
        "NASA - 65 Years Ago: NASA Begins Operations",
        "On October 1, 1958, 'the National Aeronautics and Space Administration (NASA) officially began operations', "
        "incorporating the National Advisory Committee for Aeronautics (NACA); 'President Dwight D. Eisenhower signed "
        "into law the National Aeronautics and Space Act' in July 1958."),
    "https://libguides.massgeneral.org/mghhistory/ether": (
        "Massachusetts General Hospital Archives - Ether",
        "October 16, 1846: 'the first successful public demonstration of the use of ether as an anesthetic agent'; "
        "surgeon John Collins Warren, patient Gilbert Abbott, ether given by Boston dentist William T. G. Morton, "
        "in the room now called the Ether Dome."),
    "https://www.world-nuclear-news.org/Articles/UK-marks-60th-anniversary-of-Calder-Hall": (
        "World Nuclear News - UK marks 60th anniversary of Calder Hall",
        "'Opened on 17 October 1956 by Queen Elizabeth II', Calder Hall in west Cumbria was 'the world's first "
        "commercial nuclear power plant' and was 'in operation for 47 years'."),
    "https://www.nps.gov/york/index.htm": (
        "National Park Service - Yorktown Battlefield",
        "Washington, 'with allied American and French forces, besieged General Charles Lord Cornwallis's British army'; "
        "'On October 19, Cornwallis surrendered, effectively ending the war and ensuring independence.'"),
    B + "topic/Sydney-Opera-House": (
        BR + "Sydney Opera House",
        "In January 1957 the judges chose the entry of 'Danish architect Jorn Utzon' from 233 competition entries; "
        "the building opened in 1973 and became a UNESCO World Heritage site in 2007."),
    "https://www2.assemblee-nationale.fr/decouvrir-l-assemblee/histoire/le-suffrage-universel/la-conquete-de-la-citoyennete-politique-des-femmes/les-33-femmes-elues-deputees-pour-la-premiere-fois-en-1945/": (
        "Assemblee nationale - Les 33 femmes elues deputees pour la premiere fois en 1945",
        "The ordinance of 21 April 1944 made women electors; they first voted in municipal elections on 29 April 1945 "
        "and first in a national ballot on 21 October 1945 (referendum and Constituent Assembly election); 33 women "
        "were elected."),
    "https://www.isro.gov.in/ISRO_EN/Chandrayaan_1.html": (
        "ISRO - Chandrayaan-1",
        "Chandrayaan-1, India's lunar mission, launched on 22 October 2008 on PSLV-C11 from SDSC SHAR, Sriharikota, "
        "and carried a Moon Impact Probe among its instruments."),
    "https://www.apple.com/newsroom/2001/10/23Apple-Presents-iPod/": (
        "Apple Newsroom - Apple Presents iPod (October 23, 2001)",
        "'iPod stores up to 1,000 CD-quality songs on its super-thin 5 GB hard drive'; $399; Auto-Sync downloads a "
        "user's iTunes songs over FireWire to a Mac."),
    "https://www.un.org/en/observances/un-day": (
        "United Nations - United Nations Day",
        "'The United Nations officially came into existence on 24 October 1945, when the Charter had been ratified by "
        "a majority of signatories'; representatives of 50 countries met in San Francisco to draw up the Charter."),
    "https://www.6sqft.com/what-it-was-like-the-day-the-nyc-subway-opened-in-1904/": (
        "6sqft - What it was like the day the NYC subway opened in 1904",
        "'October 27, 1904, the first IRT subway line opened with the City Hall station as its showpiece'; Mayor "
        "George B. McClellan drove the first train ('No, sir! I'm running this train!')."),
    B + "event/Battle-of-Agincourt": (
        BR + "Battle of Agincourt",
        "October 25, 1415, near Agincourt in northern France; Henry V's English army defeated a larger French force; "
        "part of the Hundred Years' War; English archers with longbows on the flanks were decisive."),
    B + "topic/War-of-the-Worlds-radio-drama-by-Welles-1938": (
        BR + "The War of the Worlds (radio drama)",
        "Broadcast October 30, 1938, by Orson Welles and his Mercury Theatre on the Air; adapted H.G. Wells's 1898 "
        "novel; 'a Martian invasion of Earth, beginning in New Jersey'."),
    B + "biography/Kemal-Ataturk": (
        BR + "Kemal Ataturk",
        "Ataturk was 'the founder and first president (1923-38) of the Republic of Turkey'."),
    B + "event/Ninety-five-Theses": (
        BR + "Ninety-five Theses",
        "By tradition posted on the Castle Church door in Wittenberg on October 31, 1517; attacked 'the sale of "
        "indulgences'; 'Written in Latin by Martin Luther' as 95 propositions for debate."),
    B + "event/Maastricht-Treaty": (
        BR + "Maastricht Treaty",
        "Signed February 7, 1992, in Maastricht, Netherlands; 'entered into force on November 1, 1993'; 'established "
        "a European Union' and provided for a common currency, the euro."),
    "https://www.nasa.gov/mission/expedition-1/": (
        "NASA - Expedition 1",
        "Crew: William Shepherd (USA, commander) and Russian flight engineers Sergei Krikalev and Yuri Gidzenko; "
        "launched October 31, 2000, aboard a Soyuz; landed March 21, 2001."),
    "https://www.ndl.go.jp/constitution/e/etc/c01.html": (
        "National Diet Library - The Constitution of Japan",
        "Promulgated November 3, 1946, effective May 3, 1947; Article 1: the Emperor 'shall be the symbol of the State'; "
        "Article 9: 'the Japanese people forever renounce war as a sovereign right of the nation'."),
    B + "biography/Howard-Carter": (
        BR + "Howard Carter",
        "On November 4, 1922, Carter's team 'found the first sign of what proved to be Tutankhamen's tomb' in the "
        "Valley of the Kings; sponsor the 5th earl of Carnarvon; second sealed doorway reached November 26."),
    B + "event/Gunpowder-Plot": (
        BR + "Gunpowder Plot",
        "Plot to kill King James I at the opening of Parliament on November 5, 1605; leader Robert Catesby; Guy Fawkes "
        "was caught guarding gunpowder 'in a cellar under the House of Lords'; marked by Guy Fawkes Day bonfires."),
    B + "event/United-States-presidential-election-of-1860": (
        BR + "United States presidential election of 1860",
        "Republican Abraham Lincoln won 'less than 40 percent of the vote' but 180 electoral votes against Douglas, "
        "Breckinridge and Bell; South Carolina seceded on December 20, 1860."),
    B + "biography/Wilhelm-Rontgen": (
        BR + "Wilhelm Conrad Rontgen",
        "November 8, 1895: a barium platinocyanide screen glowed; he called the rays X-radiation because their nature "
        "was uncertain; he received 'the first Nobel Prize for Physics, in 1901'."),
    B + "topic/Berlin-Wall": (
        BR + "Berlin Wall",
        "First erected on the night of August 12-13, 1961, by the German Democratic Republic (East Germany); on "
        "November 9, 1989, the East German government opened the borders."),
    "https://nssdc.gsfc.nasa.gov/planetary/lunar/lunarussr.html": (
        "NASA NSSDCA - Soviet Lunar Missions",
        "Luna 17: 'Launched 10 Nov 1970', 'Landed on Moon 17 Nov 1970' in Mare Imbrium, carrying the 'Lunar Rover - "
        "Lunokhod 1'."),
    B + "biography/Robert-Louis-Stevenson": (
        BR + "Robert Louis Stevenson",
        "Born November 13, 1850, Edinburgh; best known for Treasure Island, Kidnapped and Strange Case of Dr. Jekyll "
        "and Mr. Hyde; died in Samoa in 1894."),
    B + "biography/Ruby-Bridges": (
        BR + "Ruby Bridges",
        "November 14, 1960, aged six, William Frantz Elementary School, New Orleans, escorted by 'four federal "
        "marshals'; commemorated in Norman Rockwell's The Problem We All Live With (1963)."),
    "https://www.ungeneva.org/en/about/league-of-nations/overview": (
        "UN Geneva - League of Nations overview",
        "'On 15 November 1920, 41 members states gathered in Geneva for the opening of the first session of the "
        "Assembly.'"),
    B + "biography/Benazir-Bhutto": (
        BR + "Benazir Bhutto",
        "Prime minister of Pakistan 1988-90 and 1993-96, 'the first woman leader of a Muslim nation in modern "
        "history'; led the Pakistan People's Party."),
    B + "topic/Steamboat-Willie": (
        BR + "Steamboat Willie",
        "Released November 1928; the first Mickey Mouse cartoon released with synchronized sound; animated by Ub "
        "Iwerks with Walt Disney."),
    B + "event/Gettysburg-Address": (
        BR + "Gettysburg Address",
        "November 19, 1863, dedication of the National Cemetery at Gettysburg; 'just 272 words long'; delivered after "
        "Edward Everett's two-hour oration."),
    B + "biography/Montgolfier-brothers": (
        BR + "Montgolfier brothers",
        "Heated air in a light bag made it rise; on September 19, 1783, at Versailles 'a sheep, a rooster, and a duck' "
        "flew; on November 21, 1783, Pilatre de Rozier and the marquis d'Arlandes made the first manned untethered "
        "flight in a Montgolfier balloon."),
    day("November", 21): (
        BR + "On This Day: November 21",
        "1783: 'The first crewed hot-air balloon flight was made by Jean-Francois Pilatre de Rozier and Francois "
        "Laurent, marquis d'Arlandes' in a balloon by the Montgolfier brothers."),
    B + "topic/Toy-Story": (
        BR + "Toy Story",
        "1995, Pixar; 'the first entirely computer-animated feature film'; Woody voiced by Tom Hanks; directed by John "
        "Lasseter."),
    B + "topic/Doctor-Who": (
        BR + "Doctor Who",
        "BBC series, 1963-89 and from 2005; the Doctor is a Time Lord; the TARDIS 'appears as a blue police box' but "
        "is larger inside."),
    B + "topic/The-Mousetrap": (
        BR + "The Mousetrap",
        "Agatha Christie's play opened in London in 1952; adapted from her radio play Three Blind Mice; audiences are "
        "traditionally asked not to reveal the ending."),
    B + "topic/Casablanca-film-by-Curtiz": (
        BR + "Casablanca",
        "1942 film; Humphrey Bogart's Rick Blaine runs Rick's Cafe Americain; Dooley Wilson 'memorably sings \"As Time "
        "Goes By\"'; won best picture."),
    B + "topic/Nobel-Prize": (
        BR + "Nobel Prize",
        "Established in Alfred Nobel's 1895 will; five prizes: physics, chemistry, physiology or medicine, literature "
        "and peace; the economics prize was created by Sweden's central bank in 1968 and first awarded in 1969; the "
        "peace prize is awarded by the Norwegian Nobel Committee in Oslo."),
    day("November", 28): (
        BR + "On This Day: November 28",
        "1912: 'Albanian national delegates, led by Ismail Qemal, issued the Vlore proclamation, which declared "
        "Albania's independence.' 1943: the Tehran Conference with Roosevelt, Churchill and Stalin opened."),
    B + "biography/Richard-E-Byrd": (
        BR + "Richard E. Byrd",
        "'On November 29, 1929, Byrd, as navigator, and three companions made the first flight over the South Pole'; "
        "his 1926 North Pole flight claim is disputed."),
    B + "place/Barbados": (
        BR + "Barbados",
        "Independence on November 30, 1966; capital Bridgetown; became a parliamentary republic in November 2021."),
    B + "biography/Rosa-Parks": (
        BR + "Rosa Parks",
        "Arrested December 1, 1955, in Montgomery, Alabama; the bus boycott lasted 381 days; she was secretary of the "
        "Montgomery NAACP chapter."),
    B + "biography/Enrico-Fermi": (
        BR + "Enrico Fermi",
        "'Italian-born American scientist'; on December 2, 1942, directed the first controlled nuclear chain reaction "
        "at the University of Chicago in Chicago Pile-1, built beneath the university's football stadium; Nobel Prize "
        "for Physics 1938."),
    B + "topic/Mars-Pathfinder": (
        BR + "Mars Pathfinder",
        "Launched December 4, 1996, landed July 4, 1997, cushioned by 'an enveloping cluster of air bags'; rover "
        "Sojourner was named for civil rights advocate Sojourner Truth."),
    B + "topic/Notre-Dame-de-Paris": (
        BR + "Notre-Dame de Paris",
        "The April 15, 2019, fire destroyed most of the roof and Viollet-le-Duc's 19th-century spire; the cathedral "
        "reopened on December 8, 2024; Victor Hugo's 1831 novel helped spur 19th-century restoration."),
    B + "topic/UNICEF": (
        BR + "UNICEF",
        "Created 1946 as the United Nations International Children's Emergency Fund; headquartered in New York City; "
        "awarded the Nobel Prize for Peace in 1965."),
    B + "topic/Paris-Agreement-2015": (
        BR + "Paris Agreement",
        "Adopted December 2015 by 195 countries; aims to keep warming below 2 C and pursue 1.5 C; replaced the Kyoto "
        "Protocol."),
    B + "biography/Abel-Janszoon-Tasman": (
        BR + "Abel Tasman",
        "Dutch navigator for the Dutch East India Company; on December 13, 1642, sighted the South Island of New "
        "Zealand; named Tasmania Van Diemen's Land after the company's governor-general."),
    B + "topic/Bill-of-Rights-United-States-Constitution": (
        BR + "Bill of Rights (United States)",
        "The first 10 amendments, 'adopted as a single unit on December 15, 1791'; drafted and introduced by James "
        "Madison."),
    B + "event/Boston-Tea-Party": (
        BR + "Boston Tea Party",
        "December 16, 1773; tea of the British East India Company; protest against the Tea Act; Parliament's Boston "
        "Port Bill 'shut off the city's sea trade'."),
    B + "topic/The-Nutcracker": (
        BR + "The Nutcracker",
        "Premiered December 1892 at the Mariinsky Theatre; music by Tchaikovsky, who used the celesta for the Sugar "
        "Plum Fairy; based on E.T.A. Hoffmann's tale."),
    B + "topic/Ebenezer-Scrooge": (
        BR + "Ebenezer Scrooge",
        "In A Christmas Carol (1843), Scrooge is first visited by the ghost of Jacob Marley, his late business partner, "
        "then by the Ghosts of Christmas Past, Present and Yet to Come."),
    B + "topic/Grimms-Fairy-Tales": (
        BR + "Grimm's Fairy Tales",
        "Jacob and Wilhelm Grimm; first volume 1812; tales include Hansel and Gretel, Snow White, Little Red Riding "
        "Hood, Sleeping Beauty, Rapunzel and Rumpelstiltskin."),
    B + "topic/Brandenburg-Gate": (
        BR + "Brandenburg Gate",
        "A quadriga (chariot drawn by four horses) was added in 1793; Napoleon took it to Paris during the occupation "
        "of 1806-08 and it stayed until 1814; reopened December 22, 1989."),
    day("December", 23): (
        BR + "On This Day: December 23",
        "1783: 'Before the Continental Congress, George Washington resigned as commander in chief of the Continental "
        "Army.'"),
    B + "technology/phonograph": (
        BR + "Phonograph",
        "Thomas Edison is 'generally credited with inventing the phonograph' in 1877; his first design recorded on "
        "'a sheet of tinfoil' wrapped around a rotating cylinder."),
    B + "topic/Ozymandias-poem": (
        BR + "Ozymandias",
        "Shelley's sonnet was published January 11, 1818, in The Examiner; it refers to Ramses II."),
    B + "biography/Ellen-Johnson-Sirleaf": (
        BR + "Ellen Johnson Sirleaf",
        "Sworn in as president of Liberia on January 16, 2006, 'the first woman to be elected head of state of an "
        "African country'; Nobel Peace Prize 2011."),
    B + "topic/Il-trovatore": (
        BR + "Il trovatore",
        "Verdi's opera premiered at the Teatro Apollo in Rome on January 19, 1853; Act II contains the 'Anvil Chorus'."),
    "https://www.nasa.gov/mission/apollo-17/": (
        "NASA - Apollo 17",
        "Crew Eugene Cernan, Harrison Schmitt and Ronald Evans; Schmitt 'is the first scientist and twelfth person to "
        "step foot on the Moon'; Cernan was the last person to leave footprints on the Moon."),
    B + "biography/Christiaan-Barnard": (
        BR + "Christiaan Barnard",
        "On December 3, 1967, at Groote Schuur Hospital he replaced the heart of Louis Washkansky, who died 18 days "
        "later of pneumonia."),
    B + "biography/Roald-Amundsen": (
        BR + "Roald Amundsen",
        "Norwegian; reached the South Pole on December 14, 1911, using sled dogs; Robert Falcon Scott reached it on "
        "January 17 and died on the return."),
    B + "biography/James-Naismith": (
        BR + "James Naismith",
        "In 1891 at the YMCA Training School in Springfield, Massachusetts, he used 'half-bushel peach baskets as "
        "targets'; Canadian-born."),
    B + "topic/Statue-of-Liberty": (
        BR + "Statue of Liberty",
        "Sculptor Frederic-Auguste Bartholdi; 'Gustave Eiffel' engineered the internal steel framework; dedicated by "
        "Grover Cleveland on October 28, 1886."),
    B + "biography/Marie-Curie": (
        BR + "Marie Curie",
        "Born November 7, 1867, in Warsaw; named polonium 'in honor of her native land'; Nobel Prizes for physics "
        "(1903) and chemistry (1911)."),
    B + "topic/Armistice-Day": (
        BR + "Armistice Day",
        "The armistice was signed at 5:45 am on November 11, 1918, at Compiegne and 'took effect at 11:00 am'; in the "
        "United States the day became Veterans Day in 1954."),
    B + "biography/Wright-brothers": (
        BR + "Wright brothers",
        "December 17, 1903, Kill Devil Hills, North Carolina; Orville flew first, 120 feet in 12 seconds; the "
        "brothers ran a bicycle sales and repair shop in Dayton, Ohio."),
    B + "topic/Universal-Declaration-of-Human-Rights": (
        BR + "Universal Declaration of Human Rights",
        "Adopted by the UN General Assembly in Paris on December 10, 1948; Eleanor Roosevelt chaired the Commission on "
        "Human Rights that drafted it; it is not legally binding."),
    B + "event/Battle-of-Bosworth-Field": (
        BR + "Battle of Bosworth Field",
        "August 22, 1485; Richard III was killed; Henry Tudor won and founded the Tudor dynasty; the last battle of the "
        "Wars of the Roses."),
    "https://www.archives.gov/milestone-documents/19th-amendment": (
        "National Archives - 19th Amendment",
        "Bars denying the vote 'on account of sex'; passed by Congress June 4, 1919; Tennessee became the 36th state to "
        "ratify on August 18, 1920; certified August 26, 1920."),
    "https://history.state.gov/milestones/1921-1936/kellogg": (
        "U.S. Office of the Historian - The Kellogg-Briand Pact, 1928",
        "Signed at Paris on August 27, 1928; signatories renounced 'war as an instrument of national policy'; Kellogg "
        "was US secretary of state and Briand French foreign minister."),
    B + "event/March-on-Washington": (
        BR + "March on Washington",
        "August 28, 1963; about 250,000 people 'in the shadow of the Lincoln Memorial'; Martin Luther King, Jr., gave "
        "his 'I Have a Dream' speech; organized by A. Philip Randolph and Bayard Rustin."),
    B + "topic/Brennt-Paris": (
        BR + "Brennt Paris?",
        "Hitler ordered Paris's infrastructure destroyed; commander Dietrich von Choltitz disobeyed and 'surrendered "
        "Paris to the Allies on August 25, 1944, leaving the city largely undamaged'."),
    B + "event/Hurricane-Katrina": (
        BR + "Hurricane Katrina",
        "Landfall August 29, 2005, as a strong category 3 storm; levees breached; 'By August 30' New Orleans 'was 80 "
        "percent underwater'; nearly 1,400 deaths."),
}

S = "society-culture-and-ideas"
SCI = "science-and-innovation"
EX = "exploration-and-exchange"
LP = "leaders-and-power"
WC = "wars-and-conflicts"
REV = "revolutions"
WW = "world-wars"
F, A = "featured", "additional"


def slug(text):
    return re.sub(r"[^a-z0-9]+", "-", text.lower()).strip("-")[:40]


def mc(qid, difficulty, prompt, options, explanation, sources, collections_, related):
    correct = [o[1:] for o in options if o.startswith("*")]
    assert len(correct) == 1, qid
    opts = [{"id": slug(o.lstrip("*")), "text": o.lstrip("*")} for o in options]
    assert len({o["id"] for o in opts}) == len(opts), qid
    return {"id": qid, "type": "multiple_choice", "difficulty": difficulty, "publicationState": "draft",
            "prompt": prompt, "options": opts, "correctOptionId": slug(correct[0]),
            "explanation": explanation,
            "sources": [{"displayName": SRC[u][0], "url": u} for u in sources],
            "collectionIds": collections_, "relatedEventIds": related}


def tf(qid, difficulty, prompt, answer, explanation, sources, collections_, related):
    q = mc(qid, difficulty, prompt, ["*True", "False"] if answer else ["True", "*False"],
           explanation, sources, collections_, related)
    q["type"] = "true_false"
    return q


# (question, calendarDay, role, regions, eras, relation rationale)
NEW = [
    # Oct 1
    (mc("nasa-absorbed-agency", "medium",
        "When NASA began operations in 1958, which older US agency did it absorb?",
        ["*The National Advisory Committee for Aeronautics", "The Federal Aviation Agency",
         "The Atomic Energy Commission", "The National Science Foundation"],
        "NASA began operations on October 1, 1958, incorporating the National Advisory Committee for Aeronautics (NACA).",
        ["https://www.nasa.gov/history/65-years-ago-nasa-begins-operations/"], [SCI], ["nasa-begins-operations-1958"]),
     "10-01", F, ["americas"], ["cold-war", "space-age"], "Exact match on the featured event; other federal agencies of the era."),
    (mc("nasa-act-president", "easy",
        "Which US president signed the 1958 law that created NASA?",
        ["Harry S. Truman", "*Dwight D. Eisenhower", "John F. Kennedy", "Lyndon B. Johnson"],
        "Eisenhower signed the National Aeronautics and Space Act in July 1958; NASA began operations that October 1.",
        ["https://www.nasa.gov/history/65-years-ago-nasa-begins-operations/"], [SCI], ["nasa-begins-operations-1958"]),
     "10-01", F, ["americas"], ["cold-war", "space-age"], "The founding law of the featured agency; neighbouring presidents."),
    # Oct 16
    (mc("first-public-anesthesia-substance", "easy",
        "The first successful public demonstration of surgical anesthesia, in Boston in 1846, used which substance?",
        ["Chloroform", "*Ether", "Nitrous oxide", "Cocaine"],
        "On October 16, 1846, dentist William Morton gave ether at Massachusetts General Hospital while John Collins Warren operated.",
        ["https://libguides.massgeneral.org/mghhistory/ether"], [SCI], ["ether-anesthesia-demonstrated-1846"]),
     "10-16", F, ["americas"], ["1800-1945"], "Exact match on the featured demonstration; other historic anesthetics."),
    (tf("ether-given-by-dentist", "medium",
        "The ether at the first public demonstration of surgical anesthesia in 1846 was given by a dentist.",
        True,
        "Boston dentist William T. G. Morton administered the ether; surgeon John Collins Warren performed the operation.",
        ["https://libguides.massgeneral.org/mghhistory/ether"], [SCI], ["ether-anesthesia-demonstrated-1846"]),
     "10-16", F, ["americas"], ["1800-1945"], "A surprising detail of the featured event."),
    # Oct 17
    (mc("calder-hall-power-type", "easy",
        "Calder Hall, opened by Queen Elizabeth II in 1956, was the world's first commercial power station of what kind?",
        ["Wind", "Tidal", "*Nuclear", "Solar"],
        "World Nuclear News describes Calder Hall, opened on October 17, 1956, as the world's first commercial nuclear power plant.",
        ["https://www.world-nuclear-news.org/Articles/UK-marks-60th-anniversary-of-Calder-Hall"], [SCI], ["calder-hall-opens-1956"]),
     "10-17", F, ["europe"], ["cold-war"], "Exact match on the featured opening; other power sources."),
    # Oct 18: reuse persons-case-senate
    # Oct 19
    (mc("yorktown-british-general", "easy",
        "Which British general surrendered his army at Yorktown in 1781?",
        ["John Burgoyne", "William Howe", "Henry Clinton", "*Charles Cornwallis"],
        "Cornwallis surrendered on October 19, 1781, after a siege by American and French forces under George Washington.",
        ["https://www.nps.gov/york/index.htm"], [WC], ["cornwallis-surrenders-at-yorktown-1781"]),
     "10-19", F, ["americas"], ["revolutionary"], "Burgoyne, who surrendered at Saratoga in 1777, is the trap."),
    (mc("yorktown-allied-country", "easy",
        "Which country's forces joined George Washington's army in the siege of Yorktown?",
        ["Spain", "*France", "The Netherlands", "Prussia"],
        "Washington besieged Cornwallis 'with allied American and French forces'.",
        ["https://www.nps.gov/york/index.htm"], [WC], ["cornwallis-surrenders-at-yorktown-1781"]),
     "10-19", F, ["americas", "europe"], ["revolutionary"], "Other European powers of the era."),
    # Oct 20
    (mc("sydney-opera-house-architect", "medium",
        "Which architect designed the Sydney Opera House?",
        ["Frank Gehry", "*Jorn Utzon", "Oscar Niemeyer", "Frank Lloyd Wright"],
        "Danish architect Jorn Utzon won the 1957 design competition; Queen Elizabeth II opened the building on October 20, 1973.",
        [B + "topic/Sydney-Opera-House"], [S], ["sydney-opera-house-opens-1973"]),
     "10-20", F, ["oceania"], ["cold-war"], "Exact match on the featured building; other famous modern architects."),
    # Oct 21
    (mc("france-women-first-national-vote", "medium",
        "In what year did women in France first vote in a national election?",
        ["1919", "1936", "*1945", "1958"],
        "Women in France first voted in a national ballot on October 21, 1945, after municipal elections that April.",
        ["https://www2.assemblee-nationale.fr/decouvrir-l-assemblee/histoire/le-suffrage-universel/la-conquete-de-la-citoyennete-politique-des-femmes/les-33-femmes-elues-deputees-pour-la-premiere-fois-en-1945/"],
        [S], ["french-women-vote-nationally-1945"]),
     "10-21", F, ["europe"], ["1800-1945", "1945-present"], "Exact match on the featured vote; a later date than many expect."),
    # Oct 22
    (mc("chandrayaan-1-destination", "easy",
        "India's Chandrayaan-1 mission, launched in 2008, was sent to explore what?",
        ["Mars", "*The Moon", "Venus", "The Sun"],
        "Chandrayaan-1, India's lunar mission, launched from Sriharikota on October 22, 2008.",
        ["https://www.isro.gov.in/ISRO_EN/Chandrayaan_1.html"], [SCI], ["chandrayaan-1-launches-2008"]),
     "10-22", F, ["asia"], ["contemporary", "space-age"], "Exact match on the featured launch; other destinations of Indian and other missions."),
    # Oct 23
    (mc("first-ipod-song-capacity", "easy",
        "Apple's first iPod, introduced in 2001, could hold up to about how many songs?",
        ["100", "*1,000", "10,000", "100,000"],
        "Apple said the iPod stored up to 1,000 CD-quality songs on its 5 GB hard drive.",
        ["https://www.apple.com/newsroom/2001/10/23Apple-Presents-iPod/"], [SCI], ["apple-introduces-ipod-2001"]),
     "10-23", F, ["americas"], ["contemporary"], "Exact match on the featured launch; order-of-magnitude distractors."),
    # Oct 24
    (mc("un-charter-drafting-city", "medium",
        "The United Nations came into existence in 1945. In which city did delegates draw up its Charter?",
        ["New York", "Geneva", "London", "*San Francisco"],
        "Representatives of 50 countries met in San Francisco to draw up the Charter, which entered into force on October 24, 1945.",
        ["https://www.un.org/en/observances/un-day"], [LP], ["un-charter-enters-into-force-1945"]),
     "10-24", F, ["global"], ["1945-present"], "New York, the later headquarters, and London, site of the first General Assembly, are the traps."),
    # Oct 25
    (mc("agincourt-english-king", "easy",
        "Which English king won the Battle of Agincourt in 1415?",
        ["Edward III", "*Henry V", "Richard II", "Henry VIII"],
        "Henry V's army defeated a larger French force at Agincourt on October 25, 1415.",
        [B + "event/Battle-of-Agincourt"], [WC], ["battle-of-agincourt-1415"]),
     "10-25", F, ["europe"], ["medieval"], "Exact match on the featured battle; other English kings."),
    (mc("agincourt-decisive-troops", "medium",
        "Which troops did much to win the Battle of Agincourt for the English?",
        ["Heavy cavalry", "*Archers with longbows", "Musketeers", "War elephants"],
        "English archers armed with longbows, placed on the flanks, inflicted heavy losses on the advancing French.",
        [B + "event/Battle-of-Agincourt"], [WC], ["battle-of-agincourt-1415"]),
     "10-25", F, ["europe"], ["medieval"], "Other battlefield arms, one of them anachronistic only by a little."),
    (mc("agincourt-war", "medium",
        "The Battle of Agincourt was fought during which war?",
        ["The Wars of the Roses", "*The Hundred Years' War", "The Thirty Years' War", "The Seven Years' War"],
        "Agincourt was part of the Hundred Years' War between England and France.",
        [B + "event/Battle-of-Agincourt"], [WC], ["battle-of-agincourt-1415"]),
     "10-25", F, ["europe"], ["medieval"], "Other wars named for a length of time or for England."),
    # Oct 26: reuse smallpox-last-natural-case-country
    # Oct 27
    (mc("nyc-subway-first-driver", "medium",
        "Who was at the controls of the first train when New York's subway opened in 1904?",
        ["*The city's mayor", "The president of the United States", "The governor of New York", "Thomas Edison"],
        "Mayor George B. McClellan drove the first train and refused to hand over: 'No, sir! I'm running this train!'",
        ["https://www.6sqft.com/what-it-was-like-the-day-the-nyc-subway-opened-in-1904/"], [SCI], ["new-york-subway-opens-1904"]),
     "10-27", F, ["americas"], ["1800-1945"], "Exact match on the featured opening; a fun detail."),
    # Oct 28
    (mc("statue-of-liberty-framework-engineer", "medium",
        "Which engineer designed the internal framework of the Statue of Liberty?",
        ["John A. Roebling", "*Gustave Eiffel", "Ferdinand de Lesseps", "Joseph Paxton"],
        "Bartholdi sculpted the statue; Gustave Eiffel engineered its internal steel framework. It was dedicated on October 28, 1886.",
        [B + "topic/Statue-of-Liberty"], [S], ["statue-of-liberty-dedicated-1886"]),
     "10-28", F, ["americas", "europe"], ["1800-1945"], "Exact match on the featured monument; other famous 19th-century engineers."),
    # Oct 29: reuse turkish-republic-founder
    # Oct 30
    (mc("war-of-the-worlds-novelist", "easy",
        "Orson Welles's 1938 radio drama The War of the Worlds was based on a novel by whom?",
        ["Jules Verne", "*H.G. Wells", "Arthur Conan Doyle", "Edgar Rice Burroughs"],
        "The October 30, 1938, broadcast adapted H.G. Wells's 1898 novel.",
        [B + "topic/War-of-the-Worlds-radio-drama-by-Welles-1938"], [S], ["war-of-the-worlds-broadcast-1938"]),
     "10-30", F, ["americas", "europe"], ["1800-1945"], "Exact match on the featured broadcast; other science-fiction pioneers."),
    (mc("war-of-the-worlds-landing-state", "medium",
        "In the 1938 War of the Worlds broadcast, the Martian invasion began in which US state?",
        ["New York", "Pennsylvania", "*New Jersey", "Connecticut"],
        "Britannica describes the drama as a Martian invasion of Earth 'beginning in New Jersey'.",
        [B + "topic/War-of-the-Worlds-radio-drama-by-Welles-1938"], [S], ["war-of-the-worlds-broadcast-1938"]),
     "10-30", F, ["americas"], ["1800-1945"], "Neighbouring north-eastern states."),
    # Oct 31
    (mc("ninety-five-theses-target", "easy",
        "Martin Luther's Ninety-five Theses of 1517 attacked the sale of what?",
        ["*Indulgences", "Bibles", "Holy relics", "Church candles"],
        "The theses criticized the sale of indulgences.",
        [B + "event/Ninety-five-Theses"], [REV, S], ["luther-ninety-five-theses-1517"]),
     "10-31", F, ["europe"], ["early-modern"], "Exact match on the featured event; other church goods."),
    (mc("ninety-five-theses-language", "medium",
        "In which language did Martin Luther write the Ninety-five Theses?",
        ["German", "*Latin", "Greek", "Hebrew"],
        "The theses were 'Written in Latin by Martin Luther' as propositions for academic debate.",
        [B + "event/Ninety-five-Theses"], [S], ["luther-ninety-five-theses-1517"]),
     "10-31", F, ["europe"], ["early-modern"], "German, the language of Luther's later Bible, is the trap."),
    # Nov 1
    (mc("maastricht-treaty-created", "easy",
        "The Maastricht Treaty, which took effect in 1993, created what?",
        ["NATO", "*The European Union", "The Council of Europe", "The Schengen Area"],
        "The treaty entered into force on November 1, 1993, and established the European Union.",
        [B + "event/Maastricht-Treaty"], [LP], ["maastricht-treaty-in-force-1993"]),
     "11-01", F, ["europe"], ["contemporary"], "Exact match on the featured treaty; other European organizations."),
    (mc("maastricht-country", "medium",
        "In which country is Maastricht, where the treaty creating the European Union was signed?",
        ["Belgium", "Luxembourg", "Germany", "*The Netherlands"],
        "The treaty was signed in Maastricht, in the Netherlands, on February 7, 1992.",
        [B + "event/Maastricht-Treaty"], [LP], ["maastricht-treaty-in-force-1993"]),
     "11-01", F, ["europe"], ["contemporary"], "Maastricht lies near the Belgian and German borders."),
    # Nov 2
    (mc("iss-first-crew-countries", "medium",
        "The first crew to live aboard the International Space Station, in 2000, came from which two countries?",
        ["*The United States and Russia", "The United States and Japan", "Russia and Canada", "The United States and Germany"],
        "Expedition 1 was American commander William Shepherd with Russians Sergei Krikalev and Yuri Gidzenko.",
        ["https://www.nasa.gov/mission/expedition-1/"], [SCI], ["first-iss-crew-arrives-2000"]),
     "11-02", F, ["global"], ["contemporary", "space-age"], "Exact match on the featured crew; other station partners."),
    # Nov 3
    (mc("japan-constitution-article-9", "medium",
        "In Article 9 of Japan's constitution, promulgated in 1946, the Japanese people forever renounce what?",
        ["The monarchy", "Nuclear power", "*War as a sovereign right", "Foreign trade"],
        "Article 9 says the Japanese people 'forever renounce war as a sovereign right of the nation'.",
        ["https://www.ndl.go.jp/constitution/e/etc/c01.html"], [LP], ["constitution-of-japan-promulgated-1946"]),
     "11-03", F, ["asia"], ["1945-present"], "Exact match on the featured constitution; the emperor remains as a symbol."),
    # Nov 4
    (mc("carter-tomb-pharaoh", "easy",
        "In November 1922, Howard Carter's team found the tomb of which pharaoh?",
        ["Ramses II", "Khufu", "*Tutankhamun", "Akhenaten"],
        "Carter's team found the first sign of Tutankhamun's tomb in the Valley of the Kings on November 4, 1922.",
        [B + "biography/Howard-Carter"], ["ancient-history", EX], ["tutankhamun-tomb-found-1922"]),
     "11-04", F, ["africa", "middle-east"], ["ancient", "1800-1945"], "Exact match on the featured find; other famous pharaohs."),
    (mc("carter-sponsor", "medium",
        "Which British aristocrat funded Howard Carter's search for Tutankhamun's tomb?",
        ["*Lord Carnarvon", "Lord Elgin", "Lord Byron", "Lord Kitchener"],
        "The 5th earl of Carnarvon, a collector of antiquities, sponsored Carter's excavations.",
        [B + "biography/Howard-Carter"], [EX], ["tutankhamun-tomb-found-1922"]),
     "11-04", F, ["africa", "europe"], ["1800-1945"], "Lord Elgin, of the Parthenon marbles, is the trap."),
    # Nov 5
    (mc("gunpowder-plot-guard", "easy",
        "Who was caught guarding the gunpowder under the House of Lords in 1605?",
        ["Robert Catesby", "*Guy Fawkes", "Thomas Percy", "Walter Raleigh"],
        "Guy Fawkes was arrested guarding the gunpowder in a cellar under the House of Lords; Robert Catesby led the plot.",
        [B + "event/Gunpowder-Plot"], [LP], ["gunpowder-plot-foiled-1605"]),
     "11-05", F, ["europe"], ["early-modern"], "Catesby, the plot's leader, is the trap."),
    (mc("gunpowder-plot-king", "medium",
        "Which king did the Gunpowder Plot conspirators plan to kill?",
        ["Charles I", "*James I", "Henry VIII", "William III"],
        "The plotters meant to blow up Parliament and King James I at its opening on November 5, 1605.",
        [B + "event/Gunpowder-Plot"], [LP], ["gunpowder-plot-foiled-1605"]),
     "11-05", F, ["europe"], ["early-modern"], "Neighbouring English kings."),
    # Nov 6
    (mc("lincoln-1860-party", "easy",
        "Abraham Lincoln won the 1860 presidential election as the candidate of which party?",
        ["Democratic", "Whig", "*Republican", "Constitutional Union"],
        "Lincoln, the Republican candidate, defeated Douglas, Breckinridge and Bell.",
        [B + "event/United-States-presidential-election-of-1860"], [LP], ["lincoln-elected-president-1860"]),
     "11-06", F, ["americas"], ["1800-1945"], "Exact match on the featured election; the other parties of the period."),
    (tf("lincoln-1860-popular-majority", "medium",
        "Abraham Lincoln won a majority of the popular vote in the 1860 election.",
        False,
        "Lincoln won less than 40 percent of the popular vote but a majority of 180 electoral votes.",
        [B + "event/United-States-presidential-election-of-1860"], [LP], ["lincoln-elected-president-1860"]),
     "11-06", F, ["americas"], ["1800-1945"], "The four-way race is the surprise."),
    # Nov 7
    (mc("polonium-named-for", "medium",
        "Marie Curie named the element polonium after what?",
        ["Her husband, Pierre", "*Her homeland, Poland", "The city of Paris", "The North Pole"],
        "Born in Warsaw on November 7, 1867, Curie named polonium in honour of her native land.",
        [B + "biography/Marie-Curie"], [SCI], ["marie-curie-born-1867"]),
     "11-07", F, ["europe"], ["1800-1945"], "Exact match on the featured scientist; playful distractors."),
    # Nov 8
    (mc("x-rays-name-reason", "medium",
        "Why did Wilhelm Rontgen call the rays he discovered in 1895 'X' rays?",
        ["They formed an X on his screen", "*Their nature was unknown", "They were his tenth discovery",
         "He named them after a colleague"],
        "Rontgen called the radiation X-radiation because its nature was uncertain.",
        [B + "biography/Wilhelm-Rontgen"], [SCI], ["rontgen-discovers-x-rays-1895"]),
     "11-08", F, ["europe"], ["1800-1945"], "Exact match on the featured discovery."),
    (tf("rontgen-first-physics-nobel", "easy",
        "Wilhelm Rontgen received the first Nobel Prize for Physics.",
        True,
        "Rontgen received the first Nobel Prize for Physics, in 1901, for his discovery of X-rays.",
        [B + "biography/Wilhelm-Rontgen"], [SCI], ["rontgen-discovers-x-rays-1895"]),
     "11-08", F, ["europe"], ["1800-1945"], "Links the featured discovery to the first Nobel Prizes."),
    # Nov 9
    (mc("berlin-wall-builder", "easy",
        "Which government built the Berlin Wall in 1961?",
        ["*East Germany", "West Germany", "Poland", "Austria"],
        "The German Democratic Republic (East Germany) built the wall in August 1961 and opened its borders on November 9, 1989.",
        [B + "topic/Berlin-Wall"], [LP], ["berlin-wall-opens-1989"]),
     "11-09", F, ["europe"], ["cold-war"], "Exact match on the featured event's subject; Central European states."),
    # Nov 10
    (mc("luna-17-cargo", "medium",
        "The Soviet Luna 17 mission, launched in 1970, delivered what to the Moon?",
        ["*A remote-controlled rover", "A crew of two cosmonauts", "A space telescope", "A nuclear reactor"],
        "Luna 17 landed in Mare Imbrium on November 17, 1970, carrying the rover Lunokhod 1.",
        ["https://nssdc.gsfc.nasa.gov/planetary/lunar/lunarussr.html"], [SCI], ["luna-17-lunokhod-launch-1970"]),
     "11-10", F, ["europe"], ["cold-war", "space-age"], "Exact match on the featured mission."),
    # Nov 11
    (mc("armistice-1918-time", "easy",
        "At what time on November 11, 1918, did the armistice ending fighting on the Western Front take effect?",
        ["Midnight", "6 a.m.", "*11 a.m.", "Noon"],
        "Signed at 5:45 a.m. at Compiegne, the armistice took effect at 11 a.m.: the eleventh hour of the eleventh day of the eleventh month.",
        [B + "topic/Armistice-Day"], [WW], ["armistice-ends-ww1-fighting-1918"]),
     "11-11", F, ["europe"], ["1800-1945"], "Exact match on the featured armistice."),
    (mc("armistice-day-us-name", "medium",
        "What is Armistice Day called in the United States today?",
        ["Memorial Day", "*Veterans Day", "Remembrance Day", "Independence Day"],
        "In 1954 the US holiday became Veterans Day, honouring all US military veterans.",
        [B + "topic/Armistice-Day"], [WW], ["armistice-ends-ww1-fighting-1918"]),
     "11-11", F, ["americas", "europe"], ["1800-1945", "1945-present"], "Other commemorations; Remembrance Day is the Commonwealth name."),
    # Nov 12: reuse rosetta-comet-name
    # Nov 13
    (mc("stevenson-novel", "easy",
        "Which of these novels did Robert Louis Stevenson write?",
        ["Ivanhoe", "*Treasure Island", "Robinson Crusoe", "Moby-Dick"],
        "Stevenson, born November 13, 1850, wrote Treasure Island, Kidnapped and Strange Case of Dr. Jekyll and Mr. Hyde.",
        [B + "biography/Robert-Louis-Stevenson"], [S], ["robert-louis-stevenson-born-1850"]),
     "11-13", F, ["europe"], ["1800-1945"], "Ivanhoe, by fellow Scot Walter Scott, is the trap."),
    (mc("stevenson-birth-city", "medium",
        "In which city was Robert Louis Stevenson born?",
        ["Glasgow", "*Edinburgh", "London", "Dublin"],
        "Stevenson was born in Edinburgh on November 13, 1850.",
        [B + "biography/Robert-Louis-Stevenson"], [S], ["robert-louis-stevenson-born-1850"]),
     "11-13", F, ["europe"], ["1800-1945"], "Other British Isles cities."),
    # Nov 14
    (mc("ruby-bridges-painter", "medium",
        "Which artist painted Ruby Bridges walking to school in The Problem We All Live With?",
        ["Andy Warhol", "Edward Hopper", "*Norman Rockwell", "Jacob Lawrence"],
        "Norman Rockwell's 1963 painting commemorates six-year-old Bridges's walk into a New Orleans school in 1960.",
        [B + "biography/Ruby-Bridges"], [S], ["ruby-bridges-first-day-1960"]),
     "11-14", F, ["americas"], ["cold-war"], "Exact match on the featured story; other American painters."),
    (mc("ruby-bridges-escort", "easy",
        "Who escorted six-year-old Ruby Bridges into her New Orleans school in 1960?",
        ["Her teachers", "*Federal marshals", "City police officers", "Her classmates"],
        "Four federal marshals escorted Bridges to William Frantz Elementary School on November 14, 1960.",
        [B + "biography/Ruby-Bridges"], [S], ["ruby-bridges-first-day-1960"]),
     "11-14", F, ["americas"], ["cold-war"], "Exact match on the featured day."),
    # Nov 15
    (mc("league-assembly-first-city", "easy",
        "In which city did the League of Nations Assembly first meet, in 1920?",
        ["Paris", "London", "*Geneva", "The Hague"],
        "Forty-one member states gathered in Geneva on November 15, 1920, for the Assembly's first session.",
        ["https://www.ungeneva.org/en/about/league-of-nations/overview"], [LP], ["league-of-nations-first-assembly-1920"]),
     "11-15", F, ["europe", "global"], ["1800-1945"], "Exact match on the featured assembly; other diplomatic capitals."),
    # Nov 16
    (mc("bhutto-country", "easy",
        "Benazir Bhutto, elected in 1988, was the first woman in modern history to lead a Muslim nation. Which country?",
        ["Bangladesh", "Indonesia", "Turkey", "*Pakistan"],
        "Bhutto was prime minister of Pakistan from 1988 to 1990 and from 1993 to 1996.",
        [B + "biography/Benazir-Bhutto"], [LP], ["benazir-bhutto-elected-1988"]),
     "11-16", F, ["asia"], ["cold-war"], "Exact match on the featured election; Bangladesh, Turkey and Indonesia later had women leaders too."),
    # Nov 17: reuse suez questions
    # Nov 18
    (mc("steamboat-willie-star", "easy",
        "Which character starred in Steamboat Willie, released in 1928?",
        ["Donald Duck", "Goofy", "*Mickey Mouse", "Oswald the Lucky Rabbit"],
        "Steamboat Willie was the first Mickey Mouse cartoon released with synchronized sound.",
        [B + "topic/Steamboat-Willie"], [S], ["steamboat-willie-released-1928"]),
     "11-18", F, ["americas"], ["1800-1945"], "Exact match on the featured film; other Disney characters."),
    # Nov 19
    (mc("gettysburg-address-length", "medium",
        "About how long was Lincoln's Gettysburg Address?",
        ["*About 270 words", "About 1,500 words", "About 5,000 words", "About 13,000 words"],
        "Britannica says the address was just 272 words long, delivered after Edward Everett's two-hour oration.",
        [B + "event/Gettysburg-Address"], [LP], ["gettysburg-address-1863"]),
     "11-19", F, ["americas"], ["1800-1945"], "Its brevity is the story."),
    (mc("gettysburg-main-speaker", "medium",
        "Who gave the two-hour main speech at Gettysburg in 1863, before Lincoln spoke?",
        ["Frederick Douglass", "*Edward Everett", "Daniel Webster", "Stephen Douglas"],
        "Edward Everett, the best-known orator of the time, spoke for two hours before Lincoln's brief address.",
        [B + "event/Gettysburg-Address"], [LP], ["gettysburg-address-1863"]),
     "11-19", F, ["americas"], ["1800-1945"], "Other famous orators of the era."),
    # Nov 20: reuse madero / mexican revolution
    # Nov 21
    (mc("first-crewed-balloon-builders", "easy",
        "The first crewed hot-air balloon flight, in 1783, was made in a balloon built by which brothers?",
        ["The Wright brothers", "*The Montgolfier brothers", "The Lumiere brothers", "The Grimm brothers"],
        "Pilatre de Rozier and the marquis d'Arlandes flew in a Montgolfier balloon on November 21, 1783.",
        [day("November", 21), B + "biography/Montgolfier-brothers"], [SCI], ["first-crewed-balloon-flight-1783"]),
     "11-21", F, ["europe"], ["early-modern"], "Exact match on the featured flight; other famous pairs of brothers."),
    (tf("balloon-animal-passengers", "easy",
        "Before people flew in a Montgolfier balloon, one carried a sheep, a rooster and a duck.",
        True,
        "At Versailles on September 19, 1783, a sheep, a rooster and a duck flew safely; people followed on November 21.",
        [B + "biography/Montgolfier-brothers"], [SCI], ["first-crewed-balloon-flight-1783"]),
     "11-21", F, ["europe"], ["early-modern"], "A fun detail leading to the featured flight."),
    # Nov 22
    (mc("toy-story-woody-voice", "easy",
        "Who voiced Woody in Toy Story (1995)?",
        ["*Tom Hanks", "Tim Allen", "Billy Crystal", "Robin Williams"],
        "Tom Hanks voiced the cowboy Woody in Pixar's Toy Story, the first entirely computer-animated feature film.",
        [B + "topic/Toy-Story"], [S], ["toy-story-released-1995"]),
     "11-22", F, ["americas"], ["contemporary"], "Tim Allen, the voice of Buzz Lightyear, is the trap."),
    # Nov 23
    (mc("tardis-disguise", "easy",
        "In Doctor Who, what does the TARDIS look like from the outside?",
        ["A red telephone box", "*A blue police box", "A London bus", "A grandfather clock"],
        "The TARDIS appears as a blue police box but is larger inside than out.",
        [B + "topic/Doctor-Who"], [S], ["doctor-who-first-episode-1963"]),
     "11-23", F, ["europe"], ["cold-war"], "Exact match on the featured show; other British icons."),
    # Nov 24
    (mc("van-diemens-land", "medium",
        "What did Abel Tasman name the island now called Tasmania?",
        ["New Holland", "*Van Diemen's Land", "New Zeeland", "Staten Landt"],
        "Tasman named it Van Diemen's Land after the governor-general of the Dutch East India Company.",
        [B + "biography/Abel-Janszoon-Tasman"], [EX], ["tasman-reaches-tasmania-1642"]),
     "11-24", F, ["oceania", "europe"], ["early-modern"], "Other Dutch-era names in the region."),
    # Nov 25
    (mc("mousetrap-author", "easy",
        "Who wrote The Mousetrap, the London play that opened in 1952?",
        ["Dorothy L. Sayers", "*Agatha Christie", "Noel Coward", "Arthur Conan Doyle"],
        "Agatha Christie adapted the play from her radio drama Three Blind Mice.",
        [B + "topic/The-Mousetrap"], [S], ["the-mousetrap-opens-1952"]),
     "11-25", F, ["europe"], ["cold-war"], "Exact match on the featured play; other British crime and stage writers."),
    (mc("mousetrap-audience-secret", "medium",
        "What are audiences of The Mousetrap traditionally asked not to reveal?",
        ["The theatre's name", "*The ending", "The cast list", "The play's running time"],
        "Audiences are traditionally asked not to divulge the mystery's resolution.",
        [B + "topic/The-Mousetrap"], [S], ["the-mousetrap-opens-1952"]),
     "11-25", F, ["europe"], ["cold-war"], "A famous theatre tradition."),
    # Nov 26
    (mc("casablanca-song", "easy",
        "Which song does Dooley Wilson's character sing in Casablanca (1942)?",
        ["Over the Rainbow", "*As Time Goes By", "Singin' in the Rain", "Moon River"],
        "Dooley Wilson 'memorably sings \"As Time Goes By\"' in Rick's Cafe Americain.",
        [B + "topic/Casablanca-film-by-Curtiz"], [S], ["casablanca-premieres-1942"]),
     "11-26", F, ["americas", "africa"], ["1800-1945"], "Other famous film songs."),
    # Nov 27
    (mc("nobel-not-original-prize", "medium",
        "Which of these was NOT one of the five prizes Alfred Nobel set up in his 1895 will?",
        ["Physics", "Literature", "Peace", "*Economics"],
        "The economics prize was created by Sweden's central bank in 1968; Nobel's will named physics, chemistry, physiology or medicine, literature and peace.",
        [B + "topic/Nobel-Prize"], [S], ["nobel-prizes-established-1895"]),
     "11-27", F, ["europe", "global"], ["1800-1945"], "Exact match on the featured will."),
    (mc("nobel-peace-prize-city", "medium",
        "In which city is the Nobel Peace Prize awarded?",
        ["Stockholm", "*Oslo", "Geneva", "Copenhagen"],
        "The Norwegian Nobel Committee awards the peace prize in Oslo; the other prizes are awarded in Sweden.",
        [B + "topic/Nobel-Prize"], [S], ["nobel-prizes-established-1895"]),
     "11-27", F, ["europe"], ["1800-1945", "1945-present"], "Stockholm, home of the other prizes, is the trap."),
    # Nov 28
    (mc("albania-independence-city", "medium",
        "In which city was Albania's independence proclaimed in 1912?",
        ["Tirana", "Durres", "*Vlore", "Shkoder"],
        "Delegates led by Ismail Qemal issued the Vlore proclamation on November 28, 1912.",
        [day("November", 28)], [LP], ["albania-independence-1912"]),
     "11-28", F, ["europe"], ["1800-1945"], "Tirana, the later capital, is the trap."),
    # Nov 29
    (mc("byrd-1929-flight", "easy",
        "In 1929, Richard Byrd's crew made the first flight over which place?",
        ["The North Pole", "*The South Pole", "Mount Everest", "The Sahara"],
        "On November 29, 1929, Byrd, as navigator, and three companions made the first flight over the South Pole.",
        [B + "biography/Richard-E-Byrd"], [EX], ["byrd-flies-over-south-pole-1929"]),
     "11-29", F, ["antarctica", "americas"], ["1800-1945"], "Byrd's disputed 1926 North Pole claim is the trap."),
    # Nov 30
    (mc("barbados-capital", "medium",
        "What is the capital of Barbados, which became independent in 1966?",
        ["Kingston", "*Bridgetown", "Port of Spain", "Castries"],
        "Bridgetown is the capital, largest town and main seaport of Barbados.",
        [B + "place/Barbados"], [LP], ["barbados-independence-1966"]),
     "11-30", F, ["americas"], ["decolonization"], "Other Caribbean capitals."),
    # Dec 1
    (mc("montgomery-boycott-length", "medium",
        "How long did the Montgomery bus boycott sparked by Rosa Parks's arrest last?",
        ["About a week", "About a month", "*381 days", "Five years"],
        "Parks was arrested on December 1, 1955; the boycott lasted 381 days.",
        [B + "biography/Rosa-Parks"], [S], ["rosa-parks-arrested-1955"]),
     "12-01", F, ["americas"], ["cold-war"], "Exact match on the featured story."),
    (mc("rosa-parks-organization", "medium",
        "Rosa Parks was secretary of the Montgomery chapter of which organization?",
        ["*The NAACP", "The Red Cross", "The YMCA", "The League of Women Voters"],
        "Parks served as secretary of the Montgomery NAACP chapter.",
        [B + "biography/Rosa-Parks"], [S], ["rosa-parks-arrested-1955"]),
     "12-01", F, ["americas"], ["cold-war"], "Other civic organizations."),
    # Dec 2
    (mc("chicago-pile-location", "medium",
        "Enrico Fermi's first nuclear reactor, Chicago Pile-1, was built beneath what?",
        ["A church", "*A football stadium", "A hospital", "A railway station"],
        "The reactor that achieved the first controlled chain reaction on December 2, 1942, was built beneath the University of Chicago's football stadium.",
        [B + "biography/Enrico-Fermi"], [SCI], ["first-nuclear-chain-reaction-1942"]),
     "12-02", F, ["americas"], ["1800-1945"], "Exact match on the featured experiment; a surprising setting."),
    (mc("fermi-birth-country", "easy",
        "Enrico Fermi, who led the first controlled nuclear chain reaction, was born in which country?",
        ["Germany", "Hungary", "*Italy", "Denmark"],
        "Fermi was an Italian-born American scientist who emigrated in 1938.",
        [B + "biography/Enrico-Fermi"], [SCI], ["first-nuclear-chain-reaction-1942"]),
     "12-02", F, ["europe", "americas"], ["1800-1945"], "Other homelands of wartime physicists."),
    # Dec 3
    (mc("washkansky-survival", "medium",
        "How long did Louis Washkansky, the first heart transplant patient, live after the operation in 1967?",
        ["18 hours", "*18 days", "18 months", "18 years"],
        "Washkansky died 18 days later from pneumonia after drugs suppressed his immune system.",
        [B + "biography/Christiaan-Barnard"], [SCI], ["first-heart-transplant-1967"]),
     "12-03", F, ["africa"], ["cold-war"], "Same number, different units."),
    # Dec 4
    (mc("sojourner-named-for", "medium",
        "Mars Pathfinder's rover, Sojourner, was named after whom?",
        ["*Sojourner Truth", "Harriet Tubman", "Rosa Parks", "Frederick Douglass"],
        "The rover was named for the 19th-century civil rights advocate Sojourner Truth.",
        [B + "topic/Mars-Pathfinder"], [SCI], ["mars-pathfinder-launched-1996"]),
     "12-04", F, ["americas"], ["contemporary", "space-age"], "Other civil rights figures."),
    (mc("pathfinder-landing-method", "medium",
        "How did Mars Pathfinder cushion its landing in 1997?",
        ["Landing legs", "*Air bags", "A sky crane", "A splashdown in a lake"],
        "Pathfinder landed inside an enveloping cluster of air bags, the first time the technique was tried.",
        [B + "topic/Mars-Pathfinder"], [SCI], ["mars-pathfinder-launched-1996"]),
     "12-04", F, ["americas"], ["contemporary", "space-age"], "Other landing methods, including the later sky crane."),
    # Dec 5: reuse Mandela questions
    # Dec 6: reuse anglo-irish-treaty-free-state
    # Dec 7
    (mc("schmitt-first-on-moon", "medium",
        "Apollo 17's Harrison Schmitt was the first person of which profession to walk on the Moon?",
        ["Military pilot", "*Scientist", "Physician", "Schoolteacher"],
        "NASA calls Schmitt, a geologist, 'the first scientist and twelfth person to step foot on the Moon'.",
        ["https://www.nasa.gov/mission/apollo-17/"], [SCI], ["apollo-17-launches-1972"]),
     "12-07", F, ["americas"], ["cold-war", "space-age"], "Exact match on the featured mission."),
    # Dec 8
    (mc("notre-dame-2019-fire-loss", "medium",
        "Which landmark part of Notre-Dame de Paris collapsed in the 2019 fire?",
        ["The bell towers", "*The spire", "The rose windows", "The crypt"],
        "The fire destroyed most of the roof and Viollet-le-Duc's 19th-century spire; the cathedral reopened on December 8, 2024.",
        [B + "topic/Notre-Dame-de-Paris"], [S], ["notre-dame-reopens-2024"]),
     "12-08", F, ["europe"], ["contemporary"], "Exact match on the featured reopening's backstory."),
    # Dec 9: reuse smallpox-only-eradicated-infectious-disease
    # Dec 10
    (mc("udhr-commission-chair", "medium",
        "Who chaired the UN commission that drafted the Universal Declaration of Human Rights?",
        ["*Eleanor Roosevelt", "Dag Hammarskjold", "Trygve Lie", "Ralph Bunche"],
        "Eleanor Roosevelt chaired the Commission on Human Rights; the General Assembly adopted the declaration on December 10, 1948.",
        [B + "topic/Universal-Declaration-of-Human-Rights"], [LP, S], ["universal-declaration-of-human-rights-1948"]),
     "12-10", F, ["global"], ["1945-present"], "Other UN figures of the period."),
    # Dec 11
    (mc("unicef-headquarters", "medium",
        "In which city is UNICEF headquartered?",
        ["*New York City", "Geneva", "Paris", "Vienna"],
        "UNICEF, created in 1946, is headquartered in New York City.",
        [B + "topic/UNICEF"], [S], ["unicef-established-1946"]),
     "12-11", F, ["global"], ["1945-present"], "Other UN agency cities."),
    (tf("unicef-nobel-peace-prize", "medium",
        "UNICEF has been awarded the Nobel Peace Prize.",
        True,
        "UNICEF was awarded the Nobel Prize for Peace in 1965.",
        [B + "topic/UNICEF"], [S], ["unicef-established-1946"]),
     "12-11", F, ["global"], ["1945-present"], "A less-known honour for the featured agency."),
    # Dec 12
    (mc("paris-agreement-replaced", "medium",
        "The 2015 Paris climate agreement replaced which earlier treaty?",
        ["The Montreal Protocol", "*The Kyoto Protocol", "The Antarctic Treaty", "The Geneva Protocol"],
        "Britannica says the Paris Agreement replaced the Kyoto Protocol.",
        [B + "topic/Paris-Agreement-2015"], [LP, SCI], ["paris-climate-agreement-2015"]),
     "12-12", F, ["global"], ["contemporary"], "The Montreal Protocol on the ozone layer is the trap."),
    # Dec 13
    (mc("tasman-company", "medium",
        "Abel Tasman, the first European to sight New Zealand in 1642, sailed for which company?",
        ["The English East India Company", "*The Dutch East India Company", "The Hudson's Bay Company",
         "The Virginia Company"],
        "Tasman made his voyages for the Dutch East India Company.",
        [B + "biography/Abel-Janszoon-Tasman"], [EX], ["tasman-sights-new-zealand-1642", "tasman-reaches-tasmania-1642"]),
     "12-13", F, ["oceania", "europe"], ["early-modern"], "Other chartered trading companies."),
    # Dec 14
    (mc("amundsen-rival", "easy",
        "Which explorer reached the South Pole about a month after Roald Amundsen in 1911-12?",
        ["Ernest Shackleton", "*Robert Falcon Scott", "Fridtjof Nansen", "Richard Byrd"],
        "Amundsen arrived on December 14, 1911; Scott reached the pole on January 17 and died on the return.",
        [B + "biography/Roald-Amundsen"], [EX], ["amundsen-reaches-south-pole-1911"]),
     "12-14", F, ["antarctica", "europe"], ["1800-1945"], "Other polar explorers."),
    # Dec 15
    (mc("bill-of-rights-count", "easy",
        "How many amendments make up the US Bill of Rights?",
        ["5", "*10", "12", "27"],
        "The first 10 amendments were adopted together as the Bill of Rights on December 15, 1791.",
        [B + "topic/Bill-of-Rights-United-States-Constitution"], [LP], ["bill-of-rights-adopted-1791"]),
     "12-15", F, ["americas"], ["revolutionary"], "Exact match on the featured adoption."),
    (mc("bill-of-rights-author", "medium",
        "Who drafted and introduced the amendments that became the US Bill of Rights?",
        ["Thomas Jefferson", "Alexander Hamilton", "*James Madison", "John Adams"],
        "James Madison drafted the amendments and submitted them to Congress in 1789.",
        [B + "topic/Bill-of-Rights-United-States-Constitution"], [LP], ["bill-of-rights-adopted-1791"]),
     "12-15", F, ["americas"], ["revolutionary"], "Other founders."),
    # Dec 16
    (mc("boston-tea-party-company", "medium",
        "The tea dumped in the Boston Tea Party belonged to which company?",
        ["The Hudson's Bay Company", "*The British East India Company", "The Dutch East India Company",
         "The Royal African Company"],
        "On December 16, 1773, protesters threw the British East India Company's tea into Boston Harbor.",
        [B + "event/Boston-Tea-Party"], [REV], ["boston-tea-party-1773"]),
     "12-16", F, ["americas"], ["revolutionary"], "Other chartered companies."),
    (mc("boston-port-bill", "medium",
        "How did Britain's Boston Port Bill punish the city after the Boston Tea Party?",
        ["It banned town meetings", "*It closed the port", "It doubled the tea tax", "It expelled Samuel Adams"],
        "The Boston Port Bill shut off the city's sea trade until the destroyed tea was paid for.",
        [B + "event/Boston-Tea-Party"], [REV], ["boston-tea-party-1773"]),
     "12-16", F, ["americas"], ["revolutionary"], "The featured protest's consequence."),
    # Dec 17
    (mc("wright-brothers-business", "easy",
        "What business did the Wright brothers run in Dayton, Ohio?",
        ["A newspaper", "*A bicycle shop", "A hardware store", "A printing press"],
        "Profits from their bicycle sales and repair shop helped fund the flights at Kill Devil Hills.",
        [B + "biography/Wright-brothers"], [SCI], ["wright-brothers-first-flight-1903"]),
     "12-17", F, ["americas"], ["1800-1945"], "Other small businesses of the era."),
    # Dec 18
    (mc("nutcracker-composer", "easy",
        "Who composed the music for The Nutcracker?",
        ["Igor Stravinsky", "*Pyotr Ilyich Tchaikovsky", "Sergei Prokofiev", "Nikolai Rimsky-Korsakov"],
        "Tchaikovsky's ballet was first presented at the Mariinsky Theatre in St. Petersburg in December 1892.",
        [B + "topic/The-Nutcracker"], [S], ["nutcracker-premieres-1892"]),
     "12-18", F, ["europe"], ["1800-1945"], "Other Russian composers."),
    (mc("sugar-plum-fairy-instrument", "medium",
        "Which bell-like instrument did Tchaikovsky use for the Sugar Plum Fairy in The Nutcracker?",
        ["Harp", "Glockenspiel", "*Celesta", "Harpsichord"],
        "Tchaikovsky found the celesta in Paris and used it for the Sugar Plum Fairy.",
        [B + "topic/The-Nutcracker"], [S], ["nutcracker-premieres-1892"]),
     "12-18", F, ["europe"], ["1800-1945"], "Other bright-toned instruments."),
    # Dec 19
    (mc("christmas-carol-first-ghost", "easy",
        "In A Christmas Carol, whose ghost visits Scrooge first?",
        ["Bob Cratchit's", "*Jacob Marley's", "Mr. Fezziwig's", "Tiny Tim's"],
        "Scrooge's late business partner, Jacob Marley, appears first; three spirits follow.",
        [B + "topic/Ebenezer-Scrooge"], [S], ["a-christmas-carol-published-1843"]),
     "12-19", F, ["europe"], ["1800-1945"], "Other characters from the story."),
    # Dec 20
    (mc("grimm-tale", "easy",
        "Which of these stories is in Grimm's Fairy Tales?",
        ["The Little Mermaid", "*Rumpelstiltskin", "Pinocchio", "Peter Pan"],
        "The Grimms' collection, first published in 1812, includes Rumpelstiltskin, Rapunzel, Snow White and Hansel and Gretel.",
        [B + "topic/Grimms-Fairy-Tales"], [S], ["grimms-fairy-tales-published-1812"]),
     "12-20", F, ["europe"], ["1800-1945"], "Hans Christian Andersen's Little Mermaid is the trap."),
    # Dec 21
    (mc("first-basketball-goals", "easy",
        "What did James Naismith use as the goals in the first basketball game?",
        ["Barrels", "*Peach baskets", "Fishing nets", "Iron hoops"],
        "Naismith used half-bushel peach baskets as targets at the YMCA Training School in Springfield in 1891.",
        [B + "biography/James-Naismith"], [S], ["first-basketball-game-1891"]),
     "12-21", F, ["americas"], ["1800-1945"], "Exact match on the featured game."),
    # Dec 22
    (mc("brandenburg-gate-sculpture", "medium",
        "What sculpture stands on top of Berlin's Brandenburg Gate?",
        ["An eagle", "*A chariot drawn by four horses", "A statue of Frederick the Great", "A pair of lions"],
        "A quadriga carrying the goddess of victory was added in 1793.",
        [B + "topic/Brandenburg-Gate"], [S], ["brandenburg-gate-reopens-1989"]),
     "12-22", F, ["europe"], ["early-modern", "cold-war"], "Exact match on the featured landmark."),
    (mc("brandenburg-quadriga-taker", "medium",
        "Which ruler carried the Brandenburg Gate's chariot sculpture off to Paris?",
        ["Louis XIV", "*Napoleon", "Louis XVI", "Charles de Gaulle"],
        "Napoleon took the quadriga to Paris during the French occupation of Berlin; it returned in 1814.",
        [B + "topic/Brandenburg-Gate"], [WC], ["brandenburg-gate-reopens-1989"]),
     "12-22", F, ["europe"], ["revolutionary"], "Other French leaders."),
    # Dec 23
    (mc("washington-resigned-command", "easy",
        "In December 1783, George Washington resigned as commander in chief of which army?",
        ["The Army of the Potomac", "*The Continental Army", "The British Army", "The Grand Army of the Republic"],
        "Washington resigned before the Continental Congress on December 23, 1783.",
        [day("December", 23)], [LP], ["washington-resigns-commission-1783"]),
     "12-23", F, ["americas"], ["revolutionary"], "Exact match on the featured event; later Union forces as distractors."),
    # Dec 24
    (mc("first-phonograph-medium", "medium",
        "Edison's first phonograph recorded sound on what?",
        ["A wax disc", "*Tinfoil wrapped around a cylinder", "Magnetic tape", "A glass plate"],
        "Edison's first design recorded indentations on a sheet of tinfoil wrapped around a rotating cylinder.",
        [B + "technology/phonograph"], [SCI], ["edison-phonograph-patent-application-1877"]),
     "12-24", F, ["americas"], ["1800-1945"], "Later and other recording media."),
    # Dec 25: reuse james-webb-telescope-launch
    # Jan 11
    (mc("ozymandias-pharaoh", "medium",
        "Shelley's sonnet 'Ozymandias' refers to which Egyptian pharaoh?",
        ["Tutankhamun", "*Ramses II", "Khufu", "Akhenaten"],
        "The sonnet, published on January 11, 1818, refers to Ramses II.",
        [B + "topic/Ozymandias-poem"], [S], ["ozymandias-published-1818"]),
     "01-11", F, ["europe", "africa"], ["1800-1945", "ancient"], "Exact match on the featured poem; other famous pharaohs."),
    # Jan 16
    (mc("sirleaf-country", "easy",
        "Ellen Johnson Sirleaf, the first woman elected head of state of an African country, led which country?",
        ["Ghana", "*Liberia", "Sierra Leone", "Nigeria"],
        "Sirleaf was sworn in as president of Liberia on January 16, 2006.",
        [B + "biography/Ellen-Johnson-Sirleaf"], [LP], ["ellen-johnson-sirleaf-sworn-in-2006"]),
     "01-16", F, ["africa"], ["contemporary"], "Other West African countries."),
    # Jan 19
    (mc("trovatore-chorus", "medium",
        "Which famous chorus comes from Verdi's opera Il trovatore?",
        ["The Hallelujah Chorus", "*The Anvil Chorus", "The Humming Chorus", "The Chorus of the Hebrew Slaves"],
        "Act II of Il trovatore, which premiered in Rome on January 19, 1853, contains the Anvil Chorus.",
        [B + "topic/Il-trovatore"], [S], ["il-trovatore-premieres-1853"]),
     "01-19", F, ["europe"], ["1800-1945"], "Verdi's own Hebrew Slaves chorus (Nabucco) is the trap."),
    # Aug 22
    (mc("bosworth-king-killed", "easy",
        "Which English king was killed at the Battle of Bosworth Field in 1485?",
        ["Henry VI", "Edward IV", "*Richard III", "Richard II"],
        "Richard III was killed; Henry Tudor became king, and the battle was the last of the Wars of the Roses.",
        [B + "event/Battle-of-Bosworth-Field"], [WC], ["battle-of-bosworth-field-1485"]),
     "08-22", F, ["europe"], ["medieval"], "Other kings of the Wars of the Roses period."),
    # Aug 25
    (mc("paris-1944-order-disobeyed", "medium",
        "What order from Hitler did Dietrich von Choltitz, the German commander of Paris, disobey in 1944?",
        ["To surrender to the Americans", "*To destroy the city", "To evacuate all civilians", "To retreat to the coast"],
        "Choltitz ignored orders to destroy Paris's infrastructure and surrendered the city on August 25, 1944, largely undamaged.",
        [B + "topic/Brennt-Paris"], [WW], ["liberation-of-paris-1944"]),
     "08-25", F, ["europe"], ["1800-1945"], "Exact match on the featured liberation."),
    # Aug 26
    (mc("nineteenth-amendment-36th-state", "medium",
        "Which state's ratification in 1920 made the Nineteenth Amendment, on women's suffrage, part of the Constitution?",
        ["New York", "Wyoming", "*Tennessee", "Ohio"],
        "Tennessee became the 36th state to ratify on August 18, 1920; the amendment was certified on August 26.",
        ["https://www.archives.gov/milestone-documents/19th-amendment"], [LP, S], ["nineteenth-amendment-certified-1920"]),
     "08-26", F, ["americas"], ["1800-1945"], "Wyoming, an early women's suffrage state, is the trap."),
    # Aug 27
    (mc("kellogg-briand-renounced", "medium",
        "What did nations renounce in the Kellogg-Briand Pact of 1928?",
        ["Chemical weapons", "*War as an instrument of national policy", "New colonies", "Naval arms races"],
        "Signatories renounced war as an instrument of national policy; the 1925 Geneva Protocol, not this pact, covered chemical weapons.",
        ["https://history.state.gov/milestones/1921-1936/kellogg"], [LP], ["kellogg-briand-pact-signed-1928"]),
     "08-27", F, ["global"], ["1800-1945"], "Other interwar arms and peace topics."),
    # Aug 28
    (mc("i-have-a-dream-memorial", "easy",
        "In front of which memorial did Martin Luther King, Jr., give his 'I Have a Dream' speech in 1963?",
        ["The Jefferson Memorial", "*The Lincoln Memorial", "The Washington Monument", "The US Capitol"],
        "The March on Washington gathered about 250,000 people in the shadow of the Lincoln Memorial on August 28, 1963.",
        [B + "event/March-on-Washington"], [S], ["march-on-washington-1963"]),
     "08-28", F, ["americas"], ["cold-war"], "Other National Mall landmarks."),
    # Aug 29
    (mc("katrina-new-orleans-flooded", "medium",
        "By the day after Hurricane Katrina's landfall in 2005, about how much of New Orleans was underwater?",
        ["About 10 percent", "About 40 percent", "*About 80 percent", "All of it"],
        "Britannica says that by August 30 the city was 80 percent underwater after levee failures.",
        [B + "event/Hurricane-Katrina"], [SCI], ["hurricane-katrina-landfall-2005"]),
     "08-29", F, ["americas"], ["contemporary"], "Exact match on the featured disaster."),
]

# Correct-option positions are balanced by moving the answer of each listed question to the
# given 0-based slot. Questions with ordered options (numbers, years, durations) are not listed.
REPOSITION = {
    0: ["first-public-anesthesia-substance", "calder-hall-power-type", "agincourt-english-king", "war-of-the-worlds-landing-state",
        "carter-tomb-pharaoh", "lincoln-1860-party", "stevenson-birth-city", "ruby-bridges-painter", "league-assembly-first-city",
        "steamboat-willie-star", "casablanca-song", "albania-independence-city", "sojourner-named-for", "fermi-birth-country",
        "bill-of-rights-author", "nutcracker-composer", "grimm-tale"],
    1: ["nasa-absorbed-agency", "yorktown-british-general", "ninety-five-theses-target", "maastricht-country",
        "japan-constitution-article-9", "gunpowder-plot-king", "x-rays-name-reason", "luna-17-cargo", "bhutto-country",
        "first-crewed-balloon-builders", "toy-story-woody-voice", "nobel-not-original-prize", "rosa-parks-organization",
        "udhr-commission-chair", "unicef-headquarters", "brandenburg-gate-sculpture", "ozymandias-pharaoh"],
    2: ["yorktown-allied-country", "sydney-opera-house-architect", "agincourt-war", "nyc-subway-first-driver",
        "statue-of-liberty-framework-engineer", "ninety-five-theses-language", "iss-first-crew-countries", "carter-sponsor",
        "polonium-named-for", "berlin-wall-builder", "tardis-disguise", "mousetrap-author", "byrd-1929-flight",
        "notre-dame-2019-fire-loss", "tasman-company", "boston-port-bill", "christmas-carol-first-ghost",
        "trovatore-chorus", "i-have-a-dream-memorial"],
    3: ["nasa-act-president", "chandrayaan-1-destination", "agincourt-decisive-troops", "war-of-the-worlds-novelist",
        "maastricht-treaty-created", "gunpowder-plot-guard", "stevenson-novel", "ruby-bridges-escort", "van-diemens-land",
        "mousetrap-audience-secret", "chicago-pile-location", "pathfinder-landing-method", "schmitt-first-on-moon",
        "paris-agreement-replaced", "amundsen-rival", "boston-tea-party-company", "wright-brothers-business",
        "sugar-plum-fairy-instrument", "first-basketball-goals", "brandenburg-quadriga-taker", "washington-resigned-command",
        "first-phonograph-medium", "sirleaf-country", "bosworth-king-killed", "paris-1944-order-disobeyed",
        "nineteenth-amendment-36th-state", "kellogg-briand-renounced", "nobel-peace-prize-city"],
}
for _slot, _ids in REPOSITION.items():
    for _q, *_ in NEW:
        if _q["id"] in _ids:
            _opts = _q["options"]
            _c = next(o for o in _opts if o["id"] == _q["correctOptionId"])
            _opts.remove(_c)
            _opts.insert(_slot, _c)

IMG7 = "docs/QUIZ_CONTENT_REVIEW.md (Q7 image-rights table)"
IMGL7 = "editorial/batches/2026-09-l7-image-identification/review-ledger.json"

# (existing question, event, calendarDay, role, regions, eras, sources checked, rationale, image-rights evidence)
LINKS = [
    # Featured events
    ("persons-case-senate", "persons-case-decided-1929", "10-18", F, ["americas"], ["1800-1945"],
     ["https://sencanada.ca/en/sencaplus/how-why/why-the-persons-case-matters/"],
     "Exact match: the question asks what the Persons Case opened to women, and cites the event's own source.", None),
    ("smallpox-last-natural-case-country", "last-natural-smallpox-case-1977", "10-26", F, ["africa"], ["cold-war"],
     ["https://www.who.int/emergencies/situations/smallpox/"],
     "Exact match: the question asks where the last natural case occurred in 1977.", None),
    ("identify-statue-of-liberty", "statue-of-liberty-dedicated-1886", "10-28", F, ["americas"], ["1800-1945"],
     ["https://www.loc.gov/item/ny1251/", B + "topic/Statue-of-Liberty"],
     "The photograph identifies the statue whose dedication is the featured event.", IMGL7),
    ("turkish-republic-founder", "turkish-republic-proclaimed-1923", "10-29", F, ["middle-east", "europe"], ["1800-1945"],
     [B + "biography/Kemal-Ataturk"],
     "Exact match: the founding president of the republic proclaimed that day; same source as the event.", None),
    ("luther-movement", "luther-ninety-five-theses-1517", "10-31", F, ["europe"], ["early-modern"],
     [day("February", 18), B + "event/Ninety-five-Theses"],
     "The theses began the Reformation the question asks about; already related to Luther's death.", None),
    ("identify-marie-curie", "marie-curie-born-1867", "11-07", F, ["europe"], ["1800-1945"],
     ["https://www.loc.gov/item/2014687674/", B + "biography/Marie-Curie"],
     "The photograph identifies Curie, whose birth is the featured event.", IMGL7),
    ("order-science-discoveries", "rontgen-discovers-x-rays-1895", "11-08", F, ["europe"], ["1800-1945"],
     [B + "biography/Wilhelm-Rontgen"],
     "One ordering item is 'Wilhelm Rontgen discovers X-rays', the event.", None),
    ("order-cold-war-milestones", "berlin-wall-opens-1989", "11-09", F, ["europe"], ["cold-war"],
     [B + "topic/Berlin-Wall", "https://history.state.gov/milestones/1989-1992/fall-of-communism"],
     "One ordering item is 'Berlin Wall opens', the event; already related to NATO's founding.", None),
    ("rosetta-comet-name", "philae-lands-on-comet-2014", "11-12", F, ["europe", "global"], ["contemporary", "space-age"],
     ["https://www.esa.int/Newsroom/Press_Releases/Mission_complete_Rosetta_s_journey_ends_in_daring_descent_to_comet"],
     "Philae was Rosetta's lander and landed on the comet the question names; already related to Rosetta's end.", None),
    ("order-space-exploration-firsts", "philae-lands-on-comet-2014", "11-12", F, ["global"], ["cold-war", "space-age"],
     ["https://www.esa.int/Science_Exploration/Space_Science/Rosetta/Philae_s_extraordinary_comet_landing_relived"],
     "One ordering item is 'Philae lands on a comet', the event.", None),
    ("order-women-heads-government", "benazir-bhutto-elected-1988", "11-16", F, ["asia"], ["cold-war"],
     [B + "biography/Benazir-Bhutto"],
     "One ordering item is Benazir Bhutto, whose 1988 election is the event.", None),
    ("suez-canal-construction-decade", "suez-canal-opens-1869", "11-17", F, ["africa", "middle-east"], ["1800-1945"],
     [day("November", 17)], "Exact match: the question states the 1869 opening after ten years of work.", None),
    ("suez-canal-seas", "suez-canal-opens-1869", "11-17", F, ["africa", "middle-east"], ["1800-1945"],
     [B + "topic/Suez-Canal"], "Asks what the canal opened that day connects.", None),
    ("order-film-milestones", "steamboat-willie-released-1928", "11-18", F, ["americas"], ["1800-1945"],
     [day("November", 18)], "One ordering item is 'Steamboat Willie is released', the event.", None),
    ("madero-mexican-revolution", "mexican-revolution-begins-1910", "11-20", F, ["americas"], ["1800-1945"],
     [day("November", 20)], "Exact match: Madero's revolt is the event; same source.", None),
    ("mexican-revolution-began-1910", "mexican-revolution-begins-1910", "11-20", F, ["americas"], ["1800-1945"],
     ["https://www.loc.gov/exhibits/mexican-revolution-and-the-united-states/overview.html"],
     "States the year the featured revolution began.", None),
    ("order-film-milestones", "toy-story-released-1995", "11-22", F, ["americas"], ["contemporary"],
     [day("November", 22)], "One ordering item is 'Toy Story is released', the event.", None),
    ("tasman-reaches-tasmania", "tasman-reaches-tasmania-1642", "11-24", F, ["oceania", "europe"], ["early-modern"],
     [day("November", 24)], "Exact match: the question asks who reached Tasmania that day; same source.", None),
    ("order-film-milestones", "casablanca-premieres-1942", "11-26", F, ["americas"], ["1800-1945"],
     [day("November", 26)], "One ordering item is 'Casablanca premieres', the event.", None),
    ("order-science-discoveries", "first-nuclear-chain-reaction-1942", "12-02", F, ["americas"], ["1800-1945"],
     [day("December", 2), B + "biography/Enrico-Fermi"],
     "One ordering item is 'First controlled nuclear chain reaction', the event.", None),
    ("barnard-first-heart-transplant", "first-heart-transplant-1967", "12-03", F, ["africa"], ["cold-war"],
     [day("December", 3), B + "biography/Christiaan-Barnard"], "Exact match on the featured operation.", None),
    ("identify-nelson-mandela", "nelson-mandela-dies-2013", "12-05", F, ["africa"], ["contemporary"],
     ["https://www.nelsonmandela.org/biography"],
     "The bust identifies Mandela, whose death is the featured event; already related to his release.", IMG7),
    ("south-africa-1994-president", "nelson-mandela-dies-2013", "12-05", F, ["africa"], ["contemporary"],
     ["https://www.nelsonmandela.org/biography"],
     "The event description calls Mandela South Africa's first Black president; the question asks who won in 1994.", None),
    ("mandela-years-in-prison", "nelson-mandela-dies-2013", "12-05", F, ["africa"], ["contemporary"],
     [day("February", 11)], "A defining fact of the life the featured event marks.", None),
    ("anglo-irish-treaty-free-state", "anglo-irish-treaty-1921", "12-06", F, ["europe"], ["1800-1945"],
     [day("December", 6)], "Exact match on the featured treaty; same source.", None),
    ("order-space-exploration-firsts", "apollo-17-launches-1972", "12-07", F, ["americas"], ["cold-war", "space-age"],
     ["https://www.nasa.gov/mission/apollo-17/"], "One ordering item is 'Apollo 17 ... launches', the event.", None),
    ("hunchback-name", "notre-dame-reopens-2024", "12-08", F, ["europe"], ["contemporary"],
     [B + "biography/Victor-Hugo", B + "topic/Notre-Dame-de-Paris"],
     "Hugo's novel is named for the cathedral and helped spur its restoration; already related to Hugo's birth.", None),
    ("smallpox-only-eradicated-infectious-disease", "smallpox-declared-eradicated-1979", "12-09", F, ["global"], ["cold-war"],
     ["https://www.who.int/emergencies/situations/smallpox/"], "Exact match: the question states the eradication.", None),
    ("universal-declaration-binding-treaty", "universal-declaration-of-human-rights-1948", "12-10", F, ["global"], ["1945-present"],
     ["https://www.un.org/en/about-us/universal-declaration-of-human-rights", B + "topic/Universal-Declaration-of-Human-Rights"],
     "Exact match on the featured declaration; Britannica confirms it is not legally binding.", None),
    ("tasman-reaches-tasmania", "tasman-sights-new-zealand-1642", "12-13", F, ["oceania", "europe"], ["early-modern"],
     [day("November", 24), B + "biography/Abel-Janszoon-Tasman"],
     "Same voyage and navigator as the featured sighting of New Zealand.", None),
    ("amundsen-first-south-pole", "amundsen-reaches-south-pole-1911", "12-14", F, ["antarctica", "europe"], ["1800-1945"],
     [day("December", 14), B + "biography/Roald-Amundsen"],
     "Exact match on the featured achievement; already related to Scott's 1902 record.", None),
    ("identify-wright-first-flight", "wright-brothers-first-flight-1903", "12-17", F, ["americas"], ["1800-1945"],
     ["https://airandspace.si.edu/collection-objects/1903-wright-flyer/nasm_A19610048000"],
     "Exact match: the photograph records the featured flight.", IMG7),
    ("identify-george-washington", "washington-resigns-commission-1783", "12-23", F, ["americas"], ["revolutionary"],
     ["https://www.metmuseum.org/art/collection/search/16584", day("December", 23)],
     "The portrait identifies Washington, whose resignation is the featured event.", IMGL7),
    ("phonograph-patent-inventor", "edison-phonograph-patent-application-1877", "12-24", F, ["americas"], ["1800-1945"],
     [day("February", 19), B + "technology/phonograph"],
     "The question asks who patented the phonograph; the event is Edison's application for that patent.", None),
    ("naismith-first-basketball", "first-basketball-game-1891", "12-21", F, ["americas"], ["1800-1945"],
     [day("December", 21)], "Exact match on the featured game; same source.", None),
    ("james-webb-telescope-launch", "james-webb-telescope-launched-2021", "12-25", F, ["global"], ["contemporary", "space-age"],
     [day("December", 25)], "Exact match on the featured launch; same source.", None),
    ("identify-martin-luther-king-jr", "march-on-washington-1963", "08-28", F, ["americas"], ["cold-war"],
     [B + "biography/Martin-Luther-King-Jr", B + "event/March-on-Washington"],
     "The photograph identifies King, whose 'I Have a Dream' speech closed the featured march.", IMGL7),
    # Additional events (Daily slot 6)
    ("john-paul-ii-first-non-italian", "john-paul-ii-elected-pope-1978", "10-16", A, ["europe"], ["cold-war"],
     [B + "biography/Saint-John-Paul-II"], "Exact match: the question is about his election on October 16, 1978.", None),
    ("alaska-purchase-seller", "alaska-transferred-to-us-1867", "10-18", A, ["americas", "europe"], ["1800-1945"],
     [day("March", 30)], "The transfer completed the purchase the question asks about.", None),
    ("nightingale-sanitation-statistics", "nightingale-departs-for-crimea-1854", "10-21", A, ["europe"], ["1800-1945"],
     ["https://www.sciencemuseum.org.uk/objects-and-stories/florence-nightingale-pioneer-statistician"],
     "Her diagrams drew on the Crimean War hospital work she set out for that day.", None),
    ("order-peace-treaties", "peace-of-westphalia-signed-1648", "10-24", A, ["europe"], ["early-modern"],
     [B + "event/Peace-of-Westphalia"], "One ordering item is the Peace of Westphalia, the event.", None),
    ("order-african-independence-1960s", "zambia-independence-1964", "10-24", A, ["africa"], ["decolonization"],
     ["https://history.state.gov/countries/zambia"], "One ordering item is Zambia's independence, the event.", None),
    ("amsterdam-toll-privilege", "amsterdam-toll-privilege-1275", "10-27", A, ["europe"], ["medieval"],
     ["https://www.amsterdam.nl/stadsarchief/stukken/handel/tolprivilege/"], "Exact match on the event.", None),
    ("order-modern-communication-milestones", "first-arpanet-message-1969", "10-29", A, ["americas"], ["cold-war"],
     ["https://www.universityofcalifornia.edu/news/lo-and-behold-internet"],
     "One ordering item is the first ARPANET message, the event.", None),
    ("order-political-turning-points", "october-manifesto-1905", "10-30", A, ["europe"], ["1800-1945"],
     [B + "event/October-Manifesto"], "One ordering item is the October Manifesto, the event.", None),
    ("order-political-turning-points", "bolsheviks-take-power-1917", "11-07", A, ["europe"], ["1800-1945"],
     [B + "event/Russian-Revolution"], "One ordering item is the Bolsheviks taking power, the event.", None),
    ("aztec-capital-city", "cortes-enters-tenochtitlan-1519", "11-08", A, ["americas"], ["early-modern"],
     [B + "place/Tenochtitlan"], "Cortes entered the Aztec capital the question names.", None),
    ("order-political-turning-points", "coup-of-18-brumaire-1799", "11-09", A, ["europe"], ["revolutionary"],
     [B + "event/Coup-of-18-19-Brumaire"], "One ordering item is the Brumaire coup, the event.", None),
    ("luther-movement", "martin-luther-born-1483", "11-10", A, ["europe"], ["early-modern"],
     [day("February", 18)], "About the man born that day.", None),
    ("identify-sun-yat-sen", "sun-yat-sen-born-1866", "11-12", A, ["asia"], ["1800-1945"],
     [B + "biography/Sun-Yat-sen"], "The portrait identifies Sun Yat-sen, born that day.", IMGL7),
    ("order-political-turning-points", "velvet-revolution-begins-1989", "11-17", A, ["europe"], ["cold-war"],
     [day("November", 17)], "One ordering item is the start of the Velvet Revolution, the event.", None),
    ("dessalines-vertieres", "battle-of-vertieres-1803", "11-18", A, ["americas"], ["revolutionary"],
     [day("November", 18)], "Exact match on the event; same source.", None),
    ("whaleship-essex-moby-dick", "whaleship-essex-sunk-1820", "11-20", A, ["americas"], ["1800-1945"],
     [day("November", 20)], "Exact match on the event; same source.", None),
    ("order-peace-treaties", "dayton-accords-1995", "11-21", A, ["europe"], ["contemporary"],
     [day("November", 21)], "One ordering item is the Dayton Accords, the event.", None),
    ("ley-juarez-special-courts", "ley-juarez-passed-1855", "11-23", A, ["americas"], ["1800-1945"],
     [day("November", 23)], "Exact match on the event; same source.", None),
    ("order-peace-treaties", "treaty-of-neuilly-1919", "11-27", A, ["europe"], ["1800-1945"],
     [day("November", 27)], "One ordering item is the Treaty of Neuilly, the event.", None),
    ("tehran-conference-big-three", "tehran-conference-opens-1943", "11-28", A, ["middle-east", "global"], ["1800-1945"],
     [day("November", 28)], "Exact match on the event; already related to Yalta.", None),
    ("locarno-western-europe", "pact-of-locarno-signed-1925", "12-01", A, ["europe"], ["1800-1945"],
     [day("December", 1)], "Exact match on the event; same source.", None),
    ("henry-ford-method", "ford-moving-assembly-line-1913", "12-01", A, ["americas"], ["1800-1945"],
     [day("April", 7)], "Asks about the assembly-line methods the event introduced.", None),
    ("napoleon-crowns-himself", "napoleon-crowned-emperor-1804", "12-02", A, ["europe"], ["revolutionary"],
     [day("December", 2)], "Exact match on the event; same source.", None),
    ("mozart-opera", "mozart-dies-1791", "12-05", A, ["europe"], ["early-modern"],
     [B + "biography/Wolfgang-Amadeus-Mozart"], "About the composer who died that day; already related to his birth.", None),
    ("finland-independence-1917", "finland-independence-1917", "12-06", A, ["europe"], ["1800-1945"],
     [day("December", 6)], "Exact match on the event; same source.", None),
    ("order-second-world-war-milestones", "pearl-harbor-attack-1941", "12-07", A, ["americas", "asia"], ["1800-1945"],
     [B + "event/Pearl-Harbor-attack"], "One ordering item is the Pearl Harbor attack, the event.", None),
    ("order-african-independence-1960s", "tanganyika-independence-1961", "12-09", A, ["africa"], ["decolonization"],
     [day("December", 9)], "One ordering item is Tanganyika's independence, the event.", None),
    ("first-nobel-prizes-anniversary", "first-nobel-prizes-awarded-1901", "12-10", A, ["europe", "global"], ["1800-1945"],
     [day("December", 10)], "Exact match on the event; same source.", None),
    ("identify-ada-lovelace", "ada-lovelace-born-1815", "12-10", A, ["europe"], ["1800-1945"],
     [B + "biography/Ada-Lovelace"], "The portrait identifies Lovelace, born that day; already related to Babbage.", IMGL7),
    ("scream-painter", "edvard-munch-born-1863", "12-12", A, ["europe"], ["1800-1945"],
     [B + "topic/The-Scream-by-Munch"], "Asks who painted The Scream; the painter was born that day.", None),
    ("pride-prejudice-heroine", "jane-austen-born-1775", "12-16", A, ["europe"], ["revolutionary"],
     [B + "topic/Pride-and-Prejudice"], "About Austen's best-known novel; she was born that day.", None),
    ("champollion-writing-system", "champollion-born-1790", "12-23", A, ["europe", "africa"], ["revolutionary"],
     ["https://www.bnf.fr/sites/default/files/2022-04/pe%CC%81dago_Champollion_OK%20site.pdf"],
     "About the decipherment by the scholar born that day.", None),
    ("identify-george-washington", "washington-crosses-delaware-1776", "12-25", A, ["americas"], ["revolutionary"],
     ["https://www.metmuseum.org/art/collection/search/16584"], "The portrait identifies Washington, who led the crossing.", IMGL7),
]


def canonical_questions():
    found = {}
    for path in sorted(glob.glob("content/quizzes/questions/*.json")):
        for q in json.load(open(path))["questions"]:
            found[q["id"]] = (path, q)
    return found


def draft_question_ids():
    ids = set()
    for path in glob.glob("editorial/batches/*-connected-quiz/draft-questions.json"):
        if BATCH_ID not in path:
            ids |= {q["id"] for q in json.load(open(path))["questions"]}
    return ids


def write(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")


def check(url):
    note = SRC[url][1] if url in SRC else INHERITED
    return {"url": url, "status": "verified_supporting", "checkedOn": CHECKED, "note": note}


def answer_text(q):
    return next(o["text"] for o in q["options"] if o["id"] == q["correctOptionId"])


def build(bank, events):
    out = f"editorial/batches/{BATCH_ID}"
    taken = draft_question_ids()
    candidates, entries, link_records = [], [], []
    for q, cal, role, regions, eras, rel in NEW:
        assert q["id"] not in bank and q["id"] not in taken, q["id"]
        assert all(r in events for r in q["relatedEventIds"]), q["id"]
        urls = [s["url"] for s in q["sources"]]
        candidates.append({"candidateId": q["id"], "kind": "quiz", "proposedCanonicalId": q["id"],
                           "workingClaim": f"{q['prompt']} Answer: {answer_text(q)}. Proposed {role} link on {cal}: {', '.join(q['relatedEventIds'])}. {rel}",
                           "sourceUrls": urls})
        entries.append({"candidateId": q["id"], "canonicalId": q["id"], "reviewStatus": "source_verified",
                        "sourceChecks": [check(u) for u in urls], "imageRightsStatus": "not_applicable",
                        "regions": regions, "eras": eras, "calendarDays": [cal],
                        "notes": NOTE_NEW.format(rel=rel)})
    grouped = collections.OrderedDict()
    for link in LINKS:
        grouped.setdefault(link[0], []).append(link)
    for qid, links in grouped.items():
        path, q = bank[qid]
        assert q["publicationState"] == "published", qid
        current = q.get("relatedEventIds") or []
        adds = [l[1] for l in links]
        assert len(adds) == len(set(adds)), qid
        for event_id in adds:
            assert event_id in events, event_id
            assert event_id not in current, (qid, event_id)
        snapshot = hashlib.sha256(json.dumps(q, separators=(",", ":")).encode()).hexdigest()
        days = [l[2] for l in links]
        rationale = " ".join(f"{l[2]} ({l[3]}, {l[1]}): {l[7]}" for l in links)
        urls = list(dict.fromkeys(u for l in links for u in l[6]))
        rights = next((l[8] for l in links if l[8]), None)
        link_records.append({"questionId": qid, "canonicalPack": path, "canonicalSnapshotSha256": snapshot,
                             "currentRelatedEventIds": current, "addRelatedEventIds": adds,
                             "calendarDays": days, "eventRoles": [l[3] for l in links],
                             "relationshipRationale": rationale, "existingImageRightsEvidence": rights})
        candidates.append({"candidateId": f"link-{qid}", "kind": "quiz", "proposedCanonicalId": qid,
                           "workingClaim": f"Propose relations from unchanged published {qid}: {rationale}",
                           "sourceUrls": urls})
        extra = (f"Image rights inherited from {rights}; owned URL and provenance unchanged, no new download."
                 if rights else "No image.")
        entries.append({"candidateId": f"link-{qid}", "canonicalId": qid, "reviewStatus": "source_verified",
                        "sourceChecks": [check(u) for u in urls],
                        "imageRightsStatus": "verified" if rights else "not_applicable",
                        "regions": sorted({r for l in links for r in l[4]}),
                        "eras": sorted({e for l in links for e in l[5]}), "calendarDays": days,
                        "notes": NOTE_LINK.format(extra=extra)})
    write(f"{out}/batch.json", {"schemaVersion": 1, "batchId": BATCH_ID, "candidateFile": "candidates.json",
                                "reviewLedgerFile": "review-ledger.json", "eventIds": [], "monthDays": [],
                                "quizPackFilenames": [PACK]})
    write(f"{out}/candidates.json", {"schemaVersion": 1, "batchId": BATCH_ID, "candidates": candidates})
    write(f"{out}/review-ledger.json", {"schemaVersion": 1, "batchId": BATCH_ID, "entries": entries})
    write(f"{out}/draft-questions.json", {"schemaVersion": 1, "questions": [n[0] for n in NEW]})
    write(f"{out}/proposed-links.json", {"schemaVersion": 1, "batchId": BATCH_ID, "existingQuestionLinks": link_records})
    write(f"{out}/distribution-audit.json", audit(bank))


def audit(bank):
    qs = [n[0] for n in NEW]
    hooks = qs + [bank[l[0]][1] for l in LINKS]
    tags = [(n[1], n[3], n[4]) for n in NEW] + [(l[2], l[4], l[5]) for l in LINKS]
    return {
        "schemaVersion": 1, "batchId": BATCH_ID, "auditedOn": CHECKED,
        "newQuestionCount": len(qs), "reusedQuestionCount": len({l[0] for l in LINKS}),
        "proposedNewRelationCount": sum(len(q["relatedEventIds"]) for q in qs) + len(LINKS),
        "newQuestionTypes": dict(collections.Counter(q["type"] for q in qs)),
        "newQuestionDifficulties": dict(collections.Counter(q["difficulty"] for q in qs)),
        "hookTypes": dict(collections.Counter(q["type"] for q in hooks)),
        "newMultipleChoiceCorrectPositions": dict(sorted(collections.Counter(
            str([o["id"] for o in q["options"]].index(q["correctOptionId"]) + 1)
            for q in qs if q["type"] == "multiple_choice").items())),
        "newTrueFalseAnswers": dict(collections.Counter(q["correctOptionId"] for q in qs if q["type"] == "true_false")),
        "hookCollectionMemberships": dict(collections.Counter(c for q in hooks for c in q["collectionIds"])),
        "regionTagMemberships": dict(collections.Counter(r for t in tags for r in t[1])),
        "eraTagMemberships": dict(collections.Counter(e for t in tags for e in t[2])),
        "calendarDayHookCounts": dict(sorted(collections.Counter(t[0] for t in tags).items())),
    }


def main():
    bank = canonical_questions()
    events = {e["id"] for e in json.load(open("content/events.json"))["events"]}
    build(bank, events)
    ids = [n[0]["id"] for n in NEW]
    assert len(ids) == len(set(ids))
    print(f"{len(NEW)} new drafts, {len(LINKS)} links on {len({l[0] for l in LINKS})} existing questions")


if __name__ == "__main__":
    main()
