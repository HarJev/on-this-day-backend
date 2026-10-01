#!/usr/bin/env python3
"""Writes the February 1 to March 7 connected-quiz drafts.

Scratch editorial tooling, not used by the runtime or importers. Run from the
repository root after editorial/tools/runway/b0201.py:

    python3 editorial/tools/connected-quiz/build_feb_mar_cycle.py

It (re)writes editorial/batches/2027-02-01-03-07-connected-quiz/ from the
records below: new draft questions (proposed pack
180-connected-february-01-march-07.json) and relation proposals that link
unchanged published questions to the new draft events.

Sources for new questions are the pages the event drafts cite; their check
notes are read from the event batches' review ledgers, so each question's
ledger entry records what that page was verified to state on 2026-10-01.
Inherited sources of reused published questions were not re-fetched.

Every ledger entry is source_verified, never approved. Link proposals record a
snapshot hash of the canonical question as inspected
(json.dumps(question, separators=(",", ":")) in file key order, the same scheme
as the earlier cycles), so a later change to that question is detectable.

Options are written in display order; the correct option is marked with a
leading "*".
"""
import collections
import glob
import hashlib
import json
import os
import re

CHECKED = "2026-10-01"
BATCH_ID = "2027-02-01-03-07-connected-quiz"
PACK = "180-connected-february-01-march-07.json"
EVENT_BATCHES = [
    "2027-02-01-07-historical-events",
    "2027-02-08-14-historical-events",
    "2027-02-15-21-historical-events",
    "2027-02-22-29-historical-events",
    "2027-03-01-07-historical-events",
]
BR = "Encyclopaedia Britannica - "
NOTE_NEW = ("Draft researched and source-checked by Claude; awaiting owner content review. "
            "Relation: {rel} Distractors checked for exclusivity under the question's wording; "
            "difficulty is an editorial estimate, not user-tested.")
NOTE_LINK = ("Relationship review only; original question and answer are unchanged and remain "
             "published. New relation is unapproved. {extra}")


def event_sources():
    """url -> (display name, combined check notes) from the event drafts and their ledgers."""
    names, notes = {}, collections.defaultdict(list)
    for b in EVENT_BATCHES:
        for e in json.load(open(f"editorial/batches/{b}/draft-events.json"))["events"]:
            for s in e["sources"]:
                names[s["url"]] = s["name"]
        for entry in json.load(open(f"editorial/batches/{b}/review-ledger.json"))["entries"]:
            for c in entry["sourceChecks"]:
                if c["note"] not in notes[c["url"]]:
                    notes[c["url"]].append(c["note"])
    return {u: (names[u], " | ".join(notes[u])) for u in names}


SRC = event_sources()
INHERITED = {
    "https://nzhistory.govt.nz/politics/treaty/the-treaty-in-brief": "NZHistory - The Treaty in brief",
    "https://www.nelsonmandela.org/biography": "Nelson Mandela Foundation - Biography",
    "https://www.loc.gov/pictures/item/2008680391/": "Library of Congress - Abraham Lincoln portrait",
    "https://www.britannica.com/biography/Charles-Darwin": BR + "Charles Darwin",
    "https://www.britannica.com/topic/Gregorian-calendar": BR + "Gregorian calendar",
    "https://schulzmuseum.org/about-schulz/schulz-biography/": "Charles M. Schulz Museum - Schulz biography",
    "https://www.thebeatles.com/please-please-me": "The Beatles - Please Please Me",
    "https://www.metmuseum.org/art/collection/search/16584": "The Metropolitan Museum of Art - George Washington (Gilbert Stuart)",
    "https://www.britannica.com/biography/Winston-Churchill": BR + "Winston Churchill",
    "https://mfa.gov.gh/wp-content/uploads/2021/02/The-Ghanaian-Envoy-Newsletter-2019.pdf": "Ghana Ministry of Foreign Affairs - The Ghanaian Envoy (2019)",
    "https://www.britannica.com/on-this-day/November-28": BR + "On This Day: November 28",
    "https://www.britannica.com/biography/Alexander-Graham-Bell": BR + "Alexander Graham Bell",
}
for _u, _n in INHERITED.items():
    SRC.setdefault(_u, (_n, "Existing source of the reused published question. Not re-fetched; inherited."))


def U(month, day):
    return f"https://www.britannica.com/on-this-day/{month}-{day}"


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


def order(qid, difficulty, prompt, items, correct, explanation, sources, collections_, related):
    return {"id": qid, "type": "chronological_ordering", "difficulty": difficulty, "publicationState": "draft",
            "prompt": prompt, "items": [{"id": i, "text": t} for i, t in items],
            "correctOrderItemIds": correct, "explanation": explanation,
            "sources": [{"displayName": SRC[u][0], "url": u} for u in sources],
            "collectionIds": collections_, "relatedEventIds": related}


S = "society-culture-and-ideas"
SCI = "science-and-innovation"
EX = "exploration-and-exchange"
LP = "leaders-and-power"
WC = "wars-and-conflicts"
REV = "revolutions"
WW = "world-wars"
AH = "ancient-history"
AR = "ancient-rome"
F, A = "featured", "additional"
FEB, MAR = "February", "March"
ART = "https://www.britannica.com/"

# (question, calendarDay, role, regions, eras, relation rationale)
NEW = [
    # Feb 1
    (mc("greensboro-sit-in-store", "easy",
        "The Greensboro sit-in of 1960 began at a segregated lunch counter in which store?",
        ["Kresge's", "Sears", "*Woolworth's", "Walgreens"],
        "On February 1, 1960, four African Americans sat down at the segregated Woolworth's lunch counter in Greensboro, North Carolina, and began a sit-in.",
        [U(FEB, 1)], [S, REV], ["greensboro-sit-in-begins-1960"]),
     "02-01", F, ["americas"], ["cold-war"], "Exact match on the featured sit-in; the distractors are other US chains of the era."),
    (mc("la-boheme-premiere-city", "medium",
        "In which Italian city did Puccini's opera La Boheme premiere in 1896?",
        ["*Turin", "Milan", "Venice", "Naples"],
        "La Boheme premiered at the Teatro Regio in Turin on February 1, 1896.",
        [U(FEB, 1)], [S], ["la-boheme-premieres-1896"]),
     "02-01", A, ["europe"], ["1800-1945"], "Milan, home of La Scala, is the natural guess."),
    (mc("trygve-lie-country", "medium",
        "Trygve Lie, elected the first secretary-general of the United Nations in 1946, came from which country?",
        ["Sweden", "Denmark", "*Norway", "Finland"],
        "Lie, a Norwegian statesman, was elected UN secretary-general on February 1, 1946, and resigned in 1952.",
        [ART + "biography/Trygve-Lie"], [LP], ["trygve-lie-first-un-secretary-general-1946"]),
     "02-01", A, ["europe", "global"], ["cold-war"], "Nordic neighbors as distractors (Dag Hammarskjold, his successor, was Swedish)."),
    # Feb 2
    (mc("groundhog-day-town", "easy",
        "In 1887, a group in which Pennsylvania town went looking for a groundhog to predict the weather?",
        ["Hershey", "*Punxsutawney", "Gettysburg", "Scranton"],
        "On February 2, 1887, a group in Punxsutawney, Pennsylvania, searched for a groundhog to forecast the weather, the start of the town's Groundhog Day tradition.",
        [U(FEB, 2)], [S], ["first-punxsutawney-groundhog-day-1887"]),
     "02-02", F, ["americas"], ["1800-1945"], "Exact match on the featured event; other well-known Pennsylvania towns as distractors."),
    (mc("guadalupe-hidalgo-war", "easy",
        "The Treaty of Guadalupe Hidalgo, signed in 1848, ended which war?",
        ["The War of 1812", "*The Mexican-American War", "The Spanish-American War", "The Texas Revolution"],
        "The United States and Mexico signed the Treaty of Guadalupe Hidalgo on February 2, 1848, ending the Mexican-American War.",
        [U(FEB, 2)], [WC], ["treaty-of-guadalupe-hidalgo-1848"]),
     "02-02", A, ["americas"], ["1800-1945"], "Other 19th-century US wars, including the earlier Texas conflict with Mexico."),
    (mc("ulysses-single-day-city", "medium",
        "James Joyce's novel Ulysses follows its characters through a single day in which city?",
        ["London", "Paris", "*Dublin", "Trieste"],
        "Joyce, born in Dublin on February 2, 1882, set Ulysses in Dublin on June 16, 1904. The book was first published in Paris in 1922.",
        [ART + "topic/Ulysses-novel-by-Joyce", ART + "biography/James-Joyce"], [S], ["james-joyce-born-1882"]),
     "02-02", A, ["europe"], ["1800-1945"], "Paris (where it was published) and Trieste (where Joyce lived) are the tempting wrong answers."),
    # Feb 3
    (mc("collins-first-shuttle-piloted", "medium",
        "In 1995 Eileen Collins became the first woman to pilot a space shuttle. Which shuttle did she fly?",
        ["Columbia", "Atlantis", "Endeavour", "*Discovery"],
        "Collins piloted the space shuttle Discovery, launched on February 3, 1995.",
        [U(FEB, 3)], [SCI], ["eileen-collins-first-woman-shuttle-pilot-1995"]),
     "02-03", F, ["americas"], ["contemporary", "space-age"], "The other orbiters flying in 1995 as distractors."),
    (tf("fifteenth-amendment-women-vote", "medium",
        "The Fifteenth Amendment to the US Constitution, ratified in 1870, gave women the right to vote.",
        False,
        "The Fifteenth Amendment, ratified on February 3, 1870, guaranteed the right to vote regardless of race. It did not address sex.",
        [U(FEB, 3)], [S, LP], ["fifteenth-amendment-ratified-1870"]),
     "02-03", A, ["americas"], ["1800-1945"], "Targets a common confusion between voting-rights amendments."),
    (mc("umm-kulthum-country", "easy",
        "The singer Umm Kulthum, one of the most famous Arab performers of the 20th century, came from which country?",
        ["Lebanon", "*Egypt", "Syria", "Morocco"],
        "Umm Kulthum was Egyptian; she died in Cairo on February 3, 1975.",
        [U(FEB, 3)], [S], ["umm-kulthum-dies-1975"]),
     "02-03", A, ["middle-east", "africa"], ["cold-war"], "Other Arabic-speaking countries with strong musical traditions."),
    # Feb 4
    (mc("facebook-founding-university", "easy",
        "TheFacebook.com was launched in 2004 by students at which university?",
        ["Stanford", "MIT", "*Harvard", "Yale"],
        "Mark Zuckerberg, Eduardo Saverin, Dustin Moskovitz and Chris Hughes launched TheFacebook.com at Harvard on February 4, 2004.",
        [U(FEB, 4)], [SCI], ["thefacebook-launched-2004"]),
     "02-04", F, ["americas", "global"], ["contemporary"], "Exact match on the featured launch."),
    (mc("ceylon-modern-name", "easy",
        "Which country was called Ceylon when it gained independence from Britain in 1948?",
        ["Mauritius", "Maldives", "Madagascar", "*Sri Lanka"],
        "Ceylon, now Sri Lanka, gained independence on February 4, 1948, after British control that began in 1796.",
        [U(FEB, 4)], [EX], ["ceylon-independence-1948"]),
     "02-04", A, ["asia"], ["decolonization"], "Other Indian Ocean island nations."),
    (mc("yalta-not-present", "medium",
        "Which of these leaders did NOT take part in the Yalta Conference of 1945?",
        ["Franklin D. Roosevelt", "Winston Churchill", "Joseph Stalin", "*Charles de Gaulle"],
        "The Yalta Conference opened on February 4, 1945, with Roosevelt, Churchill and Stalin. De Gaulle was not among them.",
        [U(FEB, 4)], [WW], ["yalta-conference-opens-1945"]),
     "02-04", A, ["europe"], ["1800-1945"], "De Gaulle's absence is a frequent surprise."),
    (tf("washington-narrow-election", "medium",
        "George Washington won the first US presidential election by a narrow margin in the electoral college.",
        False,
        "On February 4, 1789, the first electoral college voted unanimously for George Washington.",
        [U(FEB, 4)], [LP], ["washington-elected-first-president-1789"]),
     "02-04", A, ["americas"], ["revolutionary"], "Many assume a contested first election."),
    # Feb 5
    (mc("apollo-14-golf", "medium",
        "What did Apollo 14 commander Alan Shepard swing at with a makeshift club on the Moon?",
        ["*Two golf balls", "A baseball", "A hockey puck", "A tennis ball"],
        "Apollo 14 landed in the Fra Mauro highlands on February 5, 1971. Near the end of his moonwalk, Shepard swung at two golf balls with a makeshift six-iron.",
        [ART + "topic/Apollo-14"], [SCI], ["apollo-14-lands-on-moon-1971"]),
     "02-05", A, ["americas"], ["space-age", "cold-war"], "A memorable human moment from the landing mission."),
    (tf("mexico-1917-constitution-in-force", "easy",
        "The constitution Mexico adopted in 1917 is still the country's constitution.",
        True,
        "Britannica describes the constitution adopted on February 5, 1917, as Mexico's present constitution.",
        [U(FEB, 5)], [LP, REV], ["mexican-constitution-adopted-1917"]),
     "02-05", F, ["americas"], ["1800-1945"], "Exact match on the featured event; its longevity is the surprise."),
    # Feb 6
    (mc("elizabeth-ii-father", "easy",
        "Elizabeth II became queen in 1952 on the death of her father. Who was he?",
        ["Edward VII", "George V", "Edward VIII", "*George VI"],
        "Elizabeth II ascended the throne on February 6, 1952, after the death of her father, King George VI.",
        [U(FEB, 6)], [LP], ["elizabeth-ii-accession-1952"]),
     "02-06", A, ["europe"], ["cold-war"], "Edward VIII, her uncle, is the classic confusion."),
    (mc("marley-music-style", "easy",
        "Bob Marley, born in 1945, found stardom by blending ska and rock steady with which kind of music?",
        ["Calypso", "*Reggae", "Salsa", "Zydeco"],
        "Britannica says Marley, born on February 6, 1945, achieved stardom by blending early ska, rock steady and reggae.",
        [U(FEB, 6)], [S], ["bob-marley-born-1945"]),
     "02-06", A, ["americas"], ["1800-1945"], "Other Caribbean and Gulf Coast styles."),
    (mc("falcon-heavy-company", "easy",
        "Which company flew its Falcon Heavy rocket for the first time in 2018?",
        ["Blue Origin", "Boeing", "*SpaceX", "Rocket Lab"],
        "SpaceX's Falcon Heavy made its first test flight on February 6, 2018.",
        [U(FEB, 6)], [SCI], ["falcon-heavy-first-flight-2018"]),
     "02-06", A, ["americas"], ["contemporary", "space-age"], "Other private launch companies."),
    # Feb 7
    (mc("chaplin-screen-character", "easy",
        "Which famous screen character did Charlie Chaplin introduce in 1914?",
        ["*The Little Tramp", "The Lone Ranger", "Charlie Chan", "Felix the Cat"],
        "Chaplin debuted the Little Tramp in Kid Auto Races at Venice, released on February 7, 1914.",
        [U(FEB, 7)], [S], ["chaplin-little-tramp-debut-1914"]),
     "02-07", A, ["americas", "europe"], ["1800-1945"], "Other early screen characters."),
    (mc("dickens-novel", "easy",
        "Which of these novels did Charles Dickens write?",
        ["Middlemarch", "Jane Eyre", "*Great Expectations", "Vanity Fair"],
        "Dickens, born in Portsmouth on February 7, 1812, wrote Oliver Twist, A Christmas Carol, David Copperfield and Great Expectations (1861).",
        [ART + "biography/Charles-Dickens-British-novelist"], [S], ["charles-dickens-born-1812"]),
     "02-07", A, ["europe"], ["1800-1945"], "Novels by his contemporaries George Eliot, Charlotte Bronte and Thackeray."),
    (mc("grenada-independence-from", "medium",
        "From which country did Grenada gain independence in 1974?",
        ["France", "Spain", "The Netherlands", "*The United Kingdom"],
        "Grenada gained independence from the United Kingdom on February 7, 1974.",
        [U(FEB, 7)], [EX], ["grenada-independence-1974"]),
     "02-07", A, ["americas"], ["decolonization"], "Other European powers in the Caribbean."),
    (mc("abdullah-ii-father", "medium",
        "Abdullah II became king of Jordan in 1999, succeeding his father. Who was his father?",
        ["Faisal", "*Hussein", "Talal", "Ghazi"],
        "Abdullah II became king on February 7, 1999, hours after the death of his father, King Hussein.",
        [U(FEB, 7)], [LP], ["abdullah-ii-becomes-king-of-jordan-1999"]),
     "02-07", A, ["middle-east"], ["contemporary"], "Names of other Hashemite kings of Jordan and Iraq."),
    # Feb 8
    (mc("verne-submarine-name", "easy",
        "What is the name of the submarine in Jules Verne's Twenty Thousand Leagues Under the Sea?",
        ["Neptune", "Leviathan", "*Nautilus", "Argonaut"],
        "Verne, born in Nantes on February 8, 1828, published Twenty Thousand Leagues Under the Sea in 1870. Its fictional submarine was the Nautilus.",
        [ART + "biography/Jules-Verne"], [S, SCI], ["jules-verne-born-1828"]),
     "02-08", F, ["europe"], ["1800-1945"], "Exact match on the featured birth; sea-themed distractors."),
    (mc("skylab-4-days", "medium",
        "How long did the Skylab 4 crew spend in space, a record when they splashed down in 1974?",
        ["14 days", "28 days", "59 days", "*84 days"],
        "The Skylab 4 crew splashed down in the Pacific on February 8, 1974, after 84 days in space.",
        [U(FEB, 8)], [SCI], ["skylab-4-splashdown-1974"]),
     "02-08", A, ["americas"], ["space-age", "cold-war"], "Spaced durations; 28 and 59 days match earlier Skylab stays."),
    (mc("mary-queen-of-scots-rival", "easy",
        "Mary, Queen of Scots, executed in 1587, was the rival of which English monarch?",
        ["*Elizabeth I", "Mary I", "Henry VIII", "James I"],
        "Mary, Queen of Scots, rival of Elizabeth I, was beheaded at Fotheringhay Castle on February 8, 1587.",
        [U(FEB, 8)], [LP], ["mary-queen-of-scots-executed-1587"]),
     "02-08", A, ["europe"], ["early-modern"], "James I was her son; Mary I and Henry VIII are near Tudor confusions."),
    # Feb 9
    (mc("element-112-named-after", "easy",
        "Element 112, first made in Germany in 1996, is named after which astronomer?",
        ["Galileo", "Kepler", "*Copernicus", "Tycho Brahe"],
        "Element 112 was synthesized on February 9, 1996, at GSI in Darmstadt and later named copernicium after Nicolaus Copernicus.",
        [U(FEB, 9), ART + "science/copernicium"], [SCI], ["element-112-synthesized-1996"]),
     "02-09", F, ["europe"], ["contemporary"], "Exact match on the featured discovery; other great astronomers as distractors."),
    (mc("color-purple-author", "easy",
        "Who wrote the Pulitzer Prize-winning novel The Color Purple (1982)?",
        ["Toni Morrison", "*Alice Walker", "Maya Angelou", "Zora Neale Hurston"],
        "Alice Walker, born on February 9, 1944, is perhaps best known for The Color Purple.",
        [U(FEB, 9)], [S], ["alice-walker-born-1944"]),
     "02-09", A, ["americas"], ["1800-1945"], "Other major African American women writers."),
    (mc("alinagar-modern-city", "medium",
        "The Treaty of Alinagar of 1757 put which present-day Indian city under British control?",
        ["Mumbai", "Chennai", "*Kolkata", "Delhi"],
        "The Treaty of Alinagar, signed on February 9, 1757, placed what is now Kolkata under British control, a prelude to the seizure of Bengal.",
        [U(FEB, 9)], [EX], ["treaty-of-alinagar-1757"]),
     "02-09", A, ["asia", "europe"], ["early-modern"], "Other major Indian cities, two of them also British presidency towns."),
    # Feb 10
    (mc("powers-aircraft-type", "medium",
        "Francis Gary Powers, swapped for a Soviet spy in 1962, had been shot down flying which kind of plane?",
        ["*A U-2", "An SR-71", "A B-52", "A MiG-21"],
        "Powers's U-2 reconnaissance plane was shot down near Sverdlovsk on May 1, 1960. He was exchanged for Rudolf Abel on February 10, 1962.",
        [ART + "event/U-2-Incident"], [WC], ["powers-abel-spy-exchange-1962"]),
     "02-10", F, ["europe", "americas"], ["cold-war"], "Exact match on the featured exchange; other Cold War aircraft as distractors."),
    (mc("treaty-of-paris-1763-war", "medium",
        "The Treaty of Paris of 1763 ended which war?",
        ["The Thirty Years' War", "The War of the Spanish Succession", "*The Seven Years' War", "The American Revolutionary War"],
        "The Treaty of Paris, signed on February 10, 1763, ended the conflicts between France and Britain that had caused the Seven Years' War. A later Treaty of Paris ended the American Revolutionary War.",
        [U(FEB, 10)], [WC], ["treaty-of-paris-1763"]),
     "02-10", A, ["europe", "americas"], ["early-modern"], "The 1783 Treaty of Paris is the deliberate trap."),
    (tf("spitz-seven-golds-first", "easy",
        "Mark Spitz was the first athlete to win seven gold medals at a single Olympic Games.",
        True,
        "Spitz, born on February 10, 1950, became the first athlete to capture seven gold medals at a single Olympics.",
        [U(FEB, 10)], [S], ["mark-spitz-born-1950"]),
     "02-10", A, ["americas"], ["cold-war"], "Many credit the feat first to a later swimmer."),
    # Feb 11
    (mc("mandela-years-in-prison", "easy",
        "How many years had Nelson Mandela spent in prison when he was released in 1990?",
        ["10", "18", "*27", "35"],
        "Mandela was released on February 11, 1990, after 27 years in prison.",
        [U(FEB, 11)], [REV], ["nelson-mandela-released-1990"]),
     "02-11", F, ["africa"], ["contemporary"], "Exact match on the featured release."),
    (mc("lateran-treaty-territory", "easy",
        "The Lateran Treaty of 1929 recognized papal sovereignty over which territory?",
        ["*Vatican City", "San Marino", "Monaco", "Avignon"],
        "Signed on February 11, 1929, the Lateran Treaty recognized papal sovereignty over Vatican City, an enclave in Rome.",
        [U(FEB, 11)], [LP], ["lateran-treaty-signed-1929"]),
     "02-11", A, ["europe"], ["1800-1945"], "Other small European states and the medieval papal city of Avignon."),
    (mc("mubarak-country", "easy",
        "Hosni Mubarak stepped down in 2011 after nearly 30 years as president of which country?",
        ["Tunisia", "*Egypt", "Libya", "Syria"],
        "Mubarak stepped down on February 11, 2011, after nearly 30 years in power, following the Arab Spring uprisings.",
        [U(FEB, 11)], [REV], ["hosni-mubarak-steps-down-2011"]),
     "02-11", A, ["middle-east", "africa"], ["contemporary"], "Other countries shaken by the Arab Spring."),
    (mc("thatcher-predecessor", "medium",
        "In 1975 Margaret Thatcher replaced whom as leader of Britain's Conservative Party?",
        ["Harold Wilson", "*Edward Heath", "Harold Macmillan", "John Major"],
        "Thatcher was elected Conservative leader on February 11, 1975, replacing Edward Heath.",
        [U(FEB, 11)], [LP], ["thatcher-elected-conservative-leader-1975"]),
     "02-11", A, ["europe"], ["cold-war"], "Wilson was the Labour prime minister at the time; Major was her successor."),
    # Feb 12
    (tf("darwin-lincoln-same-birthday", "easy",
        "Charles Darwin and Abraham Lincoln were born on the same day.",
        True,
        "Both were born on February 12, 1809: Darwin in England and Lincoln in the United States.",
        [U(FEB, 12)], [S], ["charles-darwin-born-1809", "abraham-lincoln-born-1809"]),
     "02-12", F, ["europe", "americas"], ["1800-1945"], "The famous coincidence behind the featured birth."),
    (mc("puyi-last-emperor-of", "easy",
        "Puyi, who abdicated in 1912, was the last emperor of which country?",
        ["Japan", "Korea", "*China", "Vietnam"],
        "Puyi abdicated on February 12, 1912, at the end of the Chinese Revolution.",
        [U(FEB, 12)], [REV], ["puyi-abdicates-1912"]),
     "02-12", A, ["asia"], ["1800-1945"], "Other East Asian monarchies."),
    (mc("scream-painter", "easy",
        "Who painted The Scream, a version of which was stolen from Oslo's National Gallery in 1994?",
        ["*Edvard Munch", "Vincent van Gogh", "Gustav Klimt", "Egon Schiele"],
        "The Norwegian artist Edvard Munch painted The Scream. The version stolen on February 12, 1994, was recovered months later.",
        [U(FEB, 12), ART + "topic/The-Scream-by-Munch"], [S], ["the-scream-stolen-1994"]),
     "02-12", A, ["europe"], ["contemporary"], "Other painters of the same era."),
    (mc("chile-independence-general", "medium",
        "Chile formally declared independence in 1818, one year after revolutionary forces led by whom won a key victory?",
        ["Simon Bolivar", "*Jose de San Martin", "Miguel Hidalgo", "Antonio Jose de Sucre"],
        "Chile declared independence on February 12, 1818, exactly one year after revolutionary forces led by Jose de San Martin won.",
        [U(FEB, 12)], [REV], ["chile-declares-independence-1818"]),
     "02-12", A, ["americas"], ["revolutionary"], "Other independence leaders of Spanish America."),
    # Feb 13
    (mc("peanuts-creator", "easy",
        "Who created the comic strip Peanuts?",
        ["Jim Davis", "Bill Watterson", "Charles Addams", "*Charles Schulz"],
        "The last Peanuts strip was published on February 13, 2000, hours after its creator, Charles Schulz, died.",
        [U(FEB, 13)], [S], ["last-peanuts-strip-2000"]),
     "02-13", F, ["americas"], ["contemporary"], "Creators of Garfield, Calvin and Hobbes and The Addams Family."),
    (mc("rudd-apology-country", "easy",
        "Kevin Rudd apologized to Aboriginal peoples in 2008 as prime minister of which country?",
        ["New Zealand", "Canada", "*Australia", "South Africa"],
        "On February 13, 2008, Australian Prime Minister Kevin Rudd apologized to Aboriginal peoples for abuses under earlier governments.",
        [U(FEB, 13)], [S], ["kevin-rudd-apology-2008"]),
     "02-13", A, ["oceania"], ["contemporary"], "Other countries with Indigenous reconciliation debates."),
    (mc("france-first-atomic-test-site", "medium",
        "Where did France detonate its first atomic bomb in 1960?",
        ["Mururoa Atoll", "*The Sahara desert", "Corsica", "French Guiana"],
        "France detonated its first atomic bomb in the Sahara desert on February 13, 1960.",
        [U(FEB, 13)], [SCI, WC], ["france-first-atomic-bomb-1960"]),
     "02-13", A, ["europe", "africa"], ["cold-war"], "Mururoa, used for later French tests, is the trap."),
    (mc("simenon-detective", "medium",
        "Which fictional detective did Georges Simenon create?",
        ["Hercule Poirot", "*Jules Maigret", "Arsene Lupin", "Auguste Dupin"],
        "Simenon, born in Liege on February 13, 1903, created the Parisian police inspector Jules Maigret.",
        [ART + "biography/Georges-Simenon"], [S], ["georges-simenon-born-1903"]),
     "02-13", A, ["europe"], ["1800-1945"], "Poirot is the famous Belgian detective; Lupin is a French gentleman thief."),
    # Feb 14
    (mc("eniac-vacuum-tubes", "medium",
        "Roughly how many vacuum tubes did the ENIAC computer use?",
        ["About 180", "About 1,800", "*About 18,000", "About 180,000"],
        "ENIAC, announced on February 14, 1946, used about 18,000 vacuum tubes and weighed 30 tons.",
        ["https://www.engineering.upenn.edu/about/history-heritage/eniac/"], [SCI], ["eniac-announced-1946"]),
     "02-14", F, ["americas"], ["1800-1945", "cold-war"], "Orders of magnitude apart, so only one fits."),
    (mc("cook-killed-islands", "medium",
        "Captain James Cook was killed in 1779 at Kealakekua Bay, in which island group?",
        ["Tahiti and the Society Islands", "*The Hawaiian Islands", "Tonga", "Fiji"],
        "Cook was killed by Hawaiians at Kealakekua Bay on February 14, 1779.",
        [U(FEB, 14)], [EX], ["james-cook-killed-1779"]),
     "02-14", A, ["oceania", "europe"], ["early-modern"], "Other Pacific islands Cook visited."),
    (tf("arizona-last-contiguous-state", "easy",
        "Arizona, admitted in 1912, was the last of the 48 contiguous US states to join the Union.",
        True,
        "Arizona became a state on February 14, 1912, the last of the 48 contiguous states to be admitted.",
        [ART + "place/Arizona-state"], [LP], ["arizona-statehood-1912"]),
     "02-14", A, ["americas"], ["1800-1945"], "Many guess New Mexico, admitted weeks earlier."),
    # Feb 15
    (mc("maple-leaf-flag-year", "medium",
        "In which year did Canada officially adopt its Maple Leaf Flag?",
        ["1867", "1931", "*1965", "1982"],
        "Canada officially adopted the Maple Leaf Flag on February 15, 1965, following a royal proclamation.",
        [U(FEB, 15)], [S], ["canada-adopts-maple-leaf-flag-1965"]),
     "02-15", F, ["americas"], ["cold-war"], "Distractors are other landmark years in Canadian history (Confederation, Westminster, patriation)."),
    (mc("uss-maine-harbor", "easy",
        "The battleship USS Maine exploded and sank in 1898 in the harbor of which city?",
        ["*Havana", "San Juan", "Manila", "Veracruz"],
        "An explosion sank the USS Maine at anchor in Havana Harbor on February 15, 1898.",
        [U(FEB, 15)], [WC], ["uss-maine-sinks-1898"]),
     "02-15", A, ["americas"], ["1800-1945"], "Other Spanish-speaking ports, two linked to the Spanish-American War."),
    (mc("galilean-moons-planet", "medium",
        "The Galilean moons, discovered by Galileo, orbit which planet?",
        ["Saturn", "*Jupiter", "Mars", "Uranus"],
        "Galileo, born in Pisa on February 15, 1564, discovered the four largest moons of Jupiter, now called the Galilean moons.",
        [ART + "biography/Galileo-Galilei"], [SCI], ["galileo-galilei-born-1564"]),
     "02-15", A, ["europe"], ["early-modern"], "Other planets with notable moons."),
    (mc("soviet-afghanistan-start-year", "medium",
        "The last Soviet troops left Afghanistan in 1989. In which year had the occupation begun?",
        ["1968", "1975", "*1979", "1985"],
        "The Soviet Union withdrew its last troops on February 15, 1989, after occupying Afghanistan since 1979.",
        [U(FEB, 15)], [WC], ["soviet-withdrawal-from-afghanistan-1989"]),
     "02-15", A, ["asia"], ["cold-war"], "Spaced Cold War years."),
    # Feb 16
    (mc("kyoto-protocol-aim", "easy",
        "The Kyoto Protocol, which took effect in 2005, was meant to reduce what?",
        ["Stockpiles of nuclear weapons", "Chemicals that deplete the ozone layer",
         "*Emissions of gases that contribute to global warming", "Plastic waste in the oceans"],
        "The Kyoto Protocol, adopted in 1997 and in force from February 16, 2005, aimed to reduce emissions of gases that contribute to global warming.",
        [ART + "event/Kyoto-Protocol"], [SCI], ["kyoto-protocol-in-force-2005"]),
     "02-16", F, ["global", "asia"], ["contemporary"], "The ozone option (the Montreal Protocol's aim) is the deliberate trap."),
    (mc("nylon-company", "medium",
        "Wallace Carothers patented nylon in 1937 while working for which company?",
        ["*DuPont", "3M", "Dow", "Bayer"],
        "DuPont chemist Wallace Hume Carothers patented nylon on February 16, 1937.",
        [U(FEB, 16)], [SCI], ["nylon-patented-1937"]),
     "02-16", A, ["americas"], ["1800-1945"], "Other big chemical companies."),
    (mc("grace-bedell-suggestion", "easy",
        "What had 12-year-old Grace Bedell suggested to Abraham Lincoln in a letter?",
        ["Wear a taller hat", "*Grow a beard", "Run for president", "Move to New York"],
        "Lincoln met Grace Bedell at Westfield, New York, on February 16, 1861. She had suggested he grow a beard.",
        [U(FEB, 16)], [S], ["lincoln-meets-grace-bedell-1861"]),
     "02-16", A, ["americas"], ["1800-1945"], "Light distractors; the beard is the iconic answer."),
    # Feb 17
    (mc("deep-blue-maker", "easy",
        "Which company built the Deep Blue chess computer that Garry Kasparov beat in 1996?",
        ["Apple", "Microsoft", "Intel", "*IBM"],
        "On February 17, 1996, world champion Garry Kasparov triumphed over IBM's Deep Blue.",
        [U(FEB, 17)], [SCI], ["kasparov-defeats-deep-blue-1996"]),
     "02-17", F, ["americas", "europe"], ["contemporary"], "Exact match on the featured match; other computer companies."),
    (mc("kosovo-independence-from", "easy",
        "From which country did Kosovo declare independence in 2008?",
        ["Albania", "*Serbia", "Montenegro", "North Macedonia"],
        "Kosovo declared independence from Serbia on February 17, 2008, though some countries refused to recognize it.",
        [U(FEB, 17)], [LP], ["kosovo-declares-independence-2008"]),
     "02-17", A, ["europe"], ["contemporary"], "Neighboring Balkan states."),
    (tf("first-submarine-sinking-world-war-i", "medium",
        "The first sinking of an enemy ship by a submarine took place during World War I.",
        False,
        "It happened during the American Civil War: on February 17, 1864, the Confederate submarine Hunley sank the USS Housatonic off Charleston, South Carolina.",
        [U(FEB, 17)], [WC], ["hunley-sinks-housatonic-1864"]),
     "02-17", A, ["americas"], ["1800-1945"], "Most people place the first submarine sinking in the world wars."),
    (mc("madama-butterfly-opera-house", "medium",
        "Puccini's Madama Butterfly premiered in 1904 at which opera house?",
        ["La Fenice", "Teatro Regio", "*La Scala", "Vienna State Opera"],
        "Madama Butterfly premiered at La Scala in Milan on February 17, 1904.",
        [U(FEB, 17)], [S], ["madama-butterfly-premieres-1904"]),
     "02-17", A, ["europe"], ["1800-1945"], "La Boheme premiered at the Teatro Regio, which makes it the sharp distractor."),
    # Feb 18
    (mc("pluto-discoverer", "easy",
        "Who discovered Pluto in 1930?",
        ["Percival Lowell", "*Clyde Tombaugh", "Edwin Hubble", "William Herschel"],
        "Clyde Tombaugh, a 24-year-old with no formal training in astronomy, discovered Pluto on February 18, 1930.",
        [U(FEB, 18)], [SCI], ["pluto-discovered-1930"]),
     "02-18", F, ["americas"], ["1800-1945"], "Exact match; Lowell, who predicted a planet beyond Neptune, is the trap."),
    (mc("luther-movement", "easy",
        "Martin Luther, who died in 1546, led which religious movement?",
        ["The Counter-Reformation", "*The Protestant Reformation", "The Great Schism", "The Methodist revival"],
        "Luther, leader of the Protestant Reformation, died in Eisleben on February 18, 1546.",
        [U(FEB, 18)], [S], ["martin-luther-dies-1546"]),
     "02-18", A, ["europe"], ["early-modern"], "Other movements in Christian history."),
    (mc("shani-davis-sport", "easy",
        "Shani Davis won his 2006 Olympic gold medal in which sport?",
        ["*Speed skating", "Bobsled", "Ski jumping", "Figure skating"],
        "On February 18, 2006, Davis won the men's 1,000-meter long-track speed skating final.",
        [U(FEB, 18)], [S], ["shani-davis-olympic-gold-2006"]),
     "02-18", A, ["americas", "europe"], ["contemporary"], "Other Winter Olympic sports."),
    # Feb 19
    (tf("copernicus-book-year-of-death", "medium",
        "Copernicus's book De revolutionibus, which set out his Sun-centered system, was published in the year he died.",
        True,
        "Copernicus, born on February 19, 1473, died in 1543, the year De revolutionibus was published.",
        [ART + "biography/Nicolaus-Copernicus"], [SCI], ["nicolaus-copernicus-born-1473"]),
     "02-19", F, ["europe"], ["early-modern"], "A striking fact about the featured astronomer's life."),
    (mc("phonograph-patent-inventor", "easy",
        "Who patented the phonograph in 1878?",
        ["Alexander Graham Bell", "*Thomas Edison", "Emile Berliner", "Nikola Tesla"],
        "Thomas Edison patented the phonograph, which reproduced sound through a needle's vibration, on February 19, 1878.",
        [U(FEB, 19)], [SCI], ["edison-patents-phonograph-1878"]),
     "02-19", A, ["americas"], ["1800-1945"], "Berliner, inventor of the gramophone, is the sharp distractor."),
    (mc("executive-order-9066-president", "medium",
        "Which US president signed Executive Order 9066, which forced Japanese Americans into detention camps?",
        ["Herbert Hoover", "*Franklin D. Roosevelt", "Harry S. Truman", "Woodrow Wilson"],
        "Roosevelt signed Executive Order 9066 on February 19, 1942, during World War II.",
        [U(FEB, 19)], [WW], ["executive-order-9066-signed-1942"]),
     "02-19", A, ["americas"], ["1800-1945"], "Presidents either side of the war years."),
    # Feb 20
    (mc("glenn-orbit-count", "medium",
        "How many times did John Glenn orbit Earth on his 1962 flight?",
        ["One", "*Three", "Seven", "Sixteen"],
        "On February 20, 1962, Glenn became the first American to orbit Earth, circling it three times.",
        [U(FEB, 20)], [SCI], ["john-glenn-orbits-earth-1962"]),
     "02-20", F, ["americas"], ["space-age", "cold-war"], "Exact match on the featured flight."),
    (mc("paricutin-country", "easy",
        "The volcano Paricutin, which began erupting in 1943, is in which country?",
        ["*Mexico", "Guatemala", "Italy", "Iceland"],
        "Paricutin, in Michoacan state, Mexico, began erupting on February 20, 1943.",
        [U(FEB, 20)], [SCI], ["paricutin-begins-erupting-1943"]),
     "02-20", A, ["americas"], ["1800-1945"], "Other volcanic countries."),
    (mc("futurism-newspaper", "hard",
        "In which newspaper did Filippo Tommaso Marinetti coin the term Futurism in 1909?",
        ["*Le Figaro", "Corriere della Sera", "The Times", "Le Petit Journal"],
        "Marinetti coined the term Futurism in the Paris newspaper Le Figaro on February 20, 1909.",
        [U(FEB, 20)], [S], ["futurism-named-in-le-figaro-1909"]),
     "02-20", A, ["europe"], ["1800-1945"], "An Italian, a British and another Paris daily of the time."),
    # Feb 21
    (tf("nixon-china-after-office", "easy",
        "Richard Nixon made his famous 1972 visit to China after he had left the presidency.",
        False,
        "Nixon visited while in office: on February 21, 1972, he became the first sitting US president to visit China.",
        [U(FEB, 21)], [LP], ["nixon-visits-china-1972"]),
     "02-21", F, ["asia", "americas"], ["cold-war"], "Exact match on the featured visit."),
    (mc("verdun-war", "easy",
        "The Battle of Verdun, which began in 1916, was fought during which war?",
        ["The Franco-Prussian War", "*World War I", "World War II", "The Crimean War"],
        "The Battle of Verdun, one of the most devastating engagements of World War I, began on February 21, 1916.",
        [U(FEB, 21)], [WW], ["battle-of-verdun-begins-1916"]),
     "02-21", A, ["europe"], ["1800-1945"], "Other wars fought by France."),
    (mc("dhaka-1952-language", "medium",
        "Protesters in Dhaka on February 21, 1952, were demanding recognition of which language?",
        ["Urdu", "Hindi", "*Bangla (Bengali)", "Punjabi"],
        "Students and activists protested at the University of Dhaka to protect Bangla (Bengali). The date is now International Mother Language Day.",
        ["https://www.cam.ac.uk/stories/celebrating-language-diversity"], [S], ["bengali-language-movement-1952"]),
     "02-21", A, ["asia"], ["decolonization"], "Urdu, the language Pakistan's government favored, is the trap."),
    # Feb 22
    (mc("miracle-on-ice-opponent", "easy",
        "In the Miracle on Ice game of February 22, 1980, the US hockey team beat which team?",
        ["Canada", "*The Soviet Union", "Finland", "West Germany"],
        "The US beat the Soviet Union 4-3 at Lake Placid, then beat Finland two days later to win gold.",
        [ART + "event/Miracle-on-Ice"], [S], ["miracle-on-ice-1980"]),
     "02-22", F, ["americas", "europe"], ["cold-war"], "The date in the prompt rules out Finland, beaten two days later."),
    (mc("white-rose-university", "medium",
        "The White Rose, an anti-Nazi resistance group, was founded by students at which university?",
        ["Heidelberg", "*Munich", "Berlin", "Hamburg"],
        "The White Rose began among students at the University of Munich. Three members were beheaded on February 22, 1943.",
        [ART + "topic/White-Rose"], [WW], ["white-rose-members-executed-1943"]),
     "02-22", A, ["europe"], ["1800-1945"], "Other major German universities."),
    (mc("first-daytona-500-winner", "hard",
        "Who won the first Daytona 500, in 1959?",
        ["Richard Petty", "Junior Johnson", "Fireball Roberts", "*Lee Petty"],
        "NASCAR's first Daytona 500, on February 22, 1959, was won by Lee Petty.",
        [U(FEB, 22)], [S], ["first-daytona-500-1959"]),
     "02-22", A, ["americas"], ["cold-war"], "Richard Petty, Lee's son, is the trap."),
    # Feb 23
    (mc("iwo-jima-flag-mountain", "medium",
        "On which mountain did US servicemen raise the American flag on Iwo Jima in 1945?",
        ["*Mount Suribachi", "Mount Fuji", "Mount Tapochau", "Mount Austen"],
        "Six US servicemen raised the flag over Mount Suribachi on February 23, 1945.",
        [U(FEB, 23)], [WW], ["iwo-jima-flag-raising-1945"]),
     "02-23", F, ["asia", "americas"], ["1800-1945"], "Exact match; other Pacific war peaks (Saipan, Guadalcanal) and Japan's best-known mountain."),
    (mc("messiah-premiere-city", "medium",
        "In which city was Handel's Messiah first performed, in 1742?",
        ["London", "*Dublin", "Halle", "Hamburg"],
        "Handel, born in Halle on February 23, 1685, settled in England. Messiah was first performed in Dublin on April 13, 1742.",
        [ART + "biography/George-Frideric-Handel"], [S], ["george-frideric-handel-born-1685"]),
     "02-23", A, ["europe"], ["early-modern"], "London, Handel's home, is the trap."),
    (mc("du-bois-the-crisis", "medium",
        "W.E.B. Du Bois edited The Crisis from 1910 to 1934. It was the magazine of which organization?",
        ["*The NAACP", "The National Urban League", "SNCC", "The Southern Christian Leadership Conference"],
        "Du Bois, born on February 23, 1868, helped create the NAACP in 1909 and edited its magazine, The Crisis.",
        [ART + "biography/W-E-B-Du-Bois"], [S, REV], ["web-du-bois-born-1868"]),
     "02-23", A, ["americas"], ["1800-1945"], "Other civil rights organizations; two were founded long after 1910."),
    # Feb 24
    (mc("gregorian-reform-days-dropped", "easy",
        "How many days did Pope Gregory XIII's calendar reform remove from 1582?",
        ["Three", "Seven", "*Ten", "Fourteen"],
        "The bull of February 24, 1582, ordered that Thursday, October 4, be followed by Friday, October 15, erasing ten days.",
        [U(FEB, 24)], [SCI, S], ["gregorian-calendar-papal-bull-1582"]),
     "02-24", F, ["europe", "global"], ["early-modern"], "Exact match on the featured bull."),
    (tf("marbury-act-unconstitutional", "medium",
        "In Marbury v. Madison (1803), the US Supreme Court declared an act of Congress unconstitutional.",
        True,
        "Marbury v. Madison, decided on February 24, 1803, struck down an act of Congress.",
        [U(FEB, 24)], [LP], ["marbury-v-madison-1803"]),
     "02-24", A, ["americas"], ["revolutionary"], "Tests the core holding of a landmark case."),
    (mc("johnson-impeachment-chamber", "medium",
        "Which body voted 126 to 47 in 1868 to impeach President Andrew Johnson?",
        ["The Senate", "*The House of Representatives", "The Supreme Court", "The Electoral College"],
        "The House of Representatives voted 126 to 47 to impeach Johnson on February 24, 1868.",
        [U(FEB, 24)], [LP], ["andrew-johnson-impeached-1868"]),
     "02-24", A, ["americas"], ["1800-1945"], "The Senate, which tries impeachments, is the trap."),
    # Feb 25
    (mc("ali-name-in-1964", "easy",
        "Under what name did Muhammad Ali fight when he beat Sonny Liston in 1964?",
        ["Joe Frazier", "Floyd Patterson", "Archie Moore", "*Cassius Clay"],
        "Ali, known at the time as Cassius Clay, defeated Liston on February 25, 1964.",
        [U(FEB, 25)], [S], ["cassius-clay-defeats-liston-1964"]),
     "02-25", F, ["americas"], ["cold-war"], "Other heavyweights of the era as distractors."),
    (mc("khrushchev-secret-speech-target", "medium",
        "In his 1956 secret speech, Nikita Khrushchev denounced which late Soviet leader?",
        ["Vladimir Lenin", "*Joseph Stalin", "Leon Trotsky", "Georgy Malenkov"],
        "Khrushchev's speech, delivered as the Twentieth Party Congress closed on February 25, 1956, denounced Joseph Stalin.",
        [U(FEB, 25)], [LP], ["khrushchev-secret-speech-1956"]),
     "02-25", A, ["europe", "asia"], ["cold-war"], "Lenin was also dead; Malenkov was alive. Only Stalin fits."),
    (tf("first-african-american-congress-20th-century", "medium",
        "The first African American to serve in the US Congress took his seat in the 20th century.",
        False,
        "Hiram Revels became the first African American in Congress when he was sworn in to the US Senate on February 25, 1870.",
        [U(FEB, 25)], [LP], ["hiram-revels-sworn-in-1870"]),
     "02-25", A, ["americas"], ["1800-1945"], "Many place this first in the 20th century."),
    (mc("marcos-country", "easy",
        "Ferdinand Marcos fled which country in 1986?",
        ["Indonesia", "Thailand", "*The Philippines", "Malaysia"],
        "Under US pressure, President Ferdinand Marcos fled the Philippines for Hawaii on February 25, 1986.",
        [U(FEB, 25)], [REV], ["marcos-flees-philippines-1986"]),
     "02-25", A, ["asia"], ["cold-war"], "Neighboring Southeast Asian nations."),
    # Feb 26
    (mc("napoleon-escape-island", "easy",
        "From which island did Napoleon escape in 1815?",
        ["Corsica", "*Elba", "Saint Helena", "Sardinia"],
        "Napoleon escaped exile on Elba on February 26, 1815.",
        [U(FEB, 26)], [LP], ["napoleon-escapes-elba-1815"]),
     "02-26", F, ["europe"], ["revolutionary", "1800-1945"], "Corsica (his birthplace) and Saint Helena (his final exile) are the traps."),
    (mc("hunchback-name", "easy",
        "What is the name of the hunchback in Victor Hugo's Notre-Dame de Paris?",
        ["*Quasimodo", "Jean Valjean", "Javert", "Cyrano"],
        "Hugo, born in Besancon on February 26, 1802, published Notre-Dame de Paris (The Hunchback of Notre-Dame) in 1831.",
        [ART + "biography/Victor-Hugo"], [S], ["victor-hugo-born-1802"]),
     "02-26", A, ["europe"], ["1800-1945"], "Valjean and Javert are from Les Miserables."),
    (mc("johnny-cash-nickname", "easy",
        "By which nickname was Johnny Cash known to his fans?",
        ["The King", "The Boss", "*The Man in Black", "The Gambler"],
        "Cash, born on February 26, 1932, was known as the Man in Black.",
        [U(FEB, 26)], [S], ["johnny-cash-born-1932"]),
     "02-26", A, ["americas"], ["1800-1945"], "Nicknames of other music stars."),
    # Feb 27
    (mc("hey-radio-source", "medium",
        "In 1942, James Stanley Hey traced strange interference on British radar to radio waves coming from what?",
        ["Enemy jamming transmitters", "The Moon", "*The Sun", "Jupiter"],
        "Hey's discovery on February 27, 1942, showed that the Sun emits radio waves.",
        [U(FEB, 27)], [SCI], ["hey-detects-solar-radio-waves-1942"]),
     "02-27", F, ["europe"], ["1800-1945"], "Enemy jamming was the natural wartime assumption."),
    (mc("reichstag-function", "easy",
        "The Reichstag building in Berlin, which caught fire in 1933, housed what?",
        ["*Germany's parliament", "The chancellor's residence", "Germany's supreme court", "The national bank"],
        "The Reichstag, the parliament building in Berlin, caught fire on February 27, 1933.",
        [U(FEB, 27)], [LP], ["reichstag-fire-1933"]),
     "02-27", A, ["europe"], ["1800-1945"], "Other seats of state power."),
    (mc("grapes-of-wrath-author", "easy",
        "Who wrote The Grapes of Wrath (1939)?",
        ["William Faulkner", "Ernest Hemingway", "*John Steinbeck", "F. Scott Fitzgerald"],
        "Steinbeck, born in Salinas, California, on February 27, 1902, won a Pulitzer Prize for The Grapes of Wrath.",
        [ART + "biography/John-Steinbeck"], [S], ["john-steinbeck-born-1902"]),
     "02-27", A, ["americas"], ["1800-1945"], "Other major American novelists of the period."),
    # Feb 28
    (mc("mash-setting-country", "easy",
        "In which country was the TV series M*A*S*H set?",
        ["Vietnam", "*South Korea", "Japan", "The Philippines"],
        "M*A*S*H was set in South Korea. Its finale on February 28, 1983, drew more than 106 million viewers.",
        ["https://www.britannica.com/today-in-history/February-28-MASH-Series-Finale"], [S], ["mash-finale-1983"]),
     "02-28", F, ["americas"], ["cold-war"], "Vietnam, the war under way when the show began, is the trap."),
    (mc("last-pope-to-resign-before-benedict", "medium",
        "When Benedict XVI resigned in 2013, who had been the last pope before him to resign?",
        ["*Gregory XII", "Celestine V", "Pius VII", "John Paul I"],
        "Benedict XVI, who resigned on February 28, 2013, was the first pope to resign since Gregory XII in 1415.",
        [U(FEB, 28)], [LP], ["benedict-xvi-resigns-2013"]),
     "02-28", A, ["europe", "global"], ["contemporary"], "Celestine V also resigned, but earlier (1294); the others did not resign."),
    (mc("egypt-protectorate-power", "medium",
        "In 1922, which country unilaterally declared Egypt independent, ending its protectorate there?",
        ["France", "The Ottoman Empire", "*Britain", "Italy"],
        "On February 28, 1922, Britain declared Egypt independent, ending its protectorate while reserving certain powers.",
        [U(FEB, 28), ART + "place/Egypt/The-Wafd-and-independence"], [EX], ["egypt-declared-independent-1922"]),
     "02-28", A, ["africa", "middle-east"], ["1800-1945"], "Other powers active in the region."),
    (mc("palme-country", "easy",
        "Olof Palme, assassinated in 1986, was prime minister of which country?",
        ["Norway", "Denmark", "*Sweden", "Finland"],
        "Olof Palme, prime minister of Sweden, was assassinated on February 28, 1986.",
        [U(FEB, 28)], [LP], ["olof-palme-assassinated-1986"]),
     "02-28", A, ["europe"], ["cold-war"], "Nordic neighbors."),
    # Feb 29
    (mc("mcdaniel-oscar-film", "easy",
        "Hattie McDaniel won her 1940 Academy Award for which film?",
        ["*Gone with the Wind", "The Wizard of Oz", "Imitation of Life", "Casablanca"],
        "McDaniel won best supporting actress for Gone with the Wind (1939) on February 29, 1940.",
        [U(FEB, 29)], [S], ["hattie-mcdaniel-wins-oscar-1940"]),
     "02-29", F, ["americas"], ["1800-1945"], "Exact match on the featured award; Casablanca came later."),
    (mc("rossini-comic-opera", "medium",
        "Which comic opera did Gioachino Rossini compose?",
        ["The Marriage of Figaro", "*The Barber of Seville", "Carmen", "La Boheme"],
        "Rossini, born in Pesaro on February 29, 1792, wrote The Barber of Seville (1816).",
        [ART + "biography/Gioachino-Rossini"], [S], ["gioachino-rossini-born-1792"]),
     "02-29", A, ["europe"], ["revolutionary", "1800-1945"], "Mozart's Figaro opera shares a character, which makes it the trap."),
    (mc("return-of-the-king-oscar-count", "medium",
        "How many Academy Awards did The Return of the King win in 2004?",
        ["7", "9", "*11", "14"],
        "The last film of The Lord of the Rings trilogy received 11 Academy Awards on February 29, 2004.",
        [U(FEB, 29)], [S], ["return-of-the-king-wins-11-oscars-2004"]),
     "02-29", A, ["oceania", "americas"], ["contemporary"], "Spaced counts."),
    (tf("howe-800th-goal-age", "hard",
        "Gordie Howe was in his fifties when he became the first to score 800 NHL goals.",
        True,
        "Howe scored his 800th goal on February 29, 1980, at the age of 51.",
        [U(FEB, 29)], [S], ["gordie-howe-800th-goal-1980"]),
     "02-29", A, ["americas"], ["cold-war"], "His age at the milestone is the surprise."),
    # Mar 1
    (mc("yellowstone-president", "medium",
        "Which US president signed the act establishing Yellowstone National Park in 1872?",
        ["Abraham Lincoln", "*Ulysses S. Grant", "Theodore Roosevelt", "Rutherford B. Hayes"],
        "Grant signed the act on March 1, 1872, creating the first US national park.",
        [U(MAR, 1), ART + "place/Yellowstone-National-Park"], [EX], ["yellowstone-established-1872"]),
     "03-01", F, ["americas"], ["1800-1945"], "Theodore Roosevelt, famous for conservation, is the trap."),
    (mc("march-first-movement-ruler", "medium",
        "The March First Movement of 1919 demanded Korea's independence from which country?",
        ["China", "Russia", "*Japan", "The United States"],
        "On March 1, 1919, protesters in Seoul launched demonstrations for Korean independence from Japan.",
        [U(MAR, 1)], [REV], ["march-first-movement-1919"]),
     "03-01", A, ["asia"], ["1800-1945"], "Other powers involved in Korea's history."),
    (mc("peace-corps-first-director", "hard",
        "Who was the first director of the Peace Corps?",
        ["Robert F. Kennedy", "Hubert Humphrey", "*R. Sargent Shriver", "Adlai Stevenson"],
        "The Peace Corps, established by executive order on March 1, 1961, was first led by Kennedy's brother-in-law, R. Sargent Shriver.",
        [ART + "topic/Peace-Corps"], [LP], ["peace-corps-established-1961"]),
     "03-01", A, ["americas", "global"], ["cold-war"], "Other prominent Democrats of the era."),
    (mc("hoover-dam-river", "easy",
        "The Hoover Dam, completed in 1936, stands on which river?",
        ["*Colorado", "Columbia", "Missouri", "Rio Grande"],
        "The Hoover Dam on the Colorado River, at the Arizona-Nevada border, was completed on March 1, 1936.",
        [U(MAR, 1)], [SCI], ["hoover-dam-completed-1936"]),
     "03-01", A, ["americas"], ["1800-1945"], "Other great western rivers."),
    # Mar 2
    (mc("concorde-builders", "easy",
        "Which two countries built the supersonic airliner Concorde?",
        ["France and Germany", "*Britain and France", "Britain and the United States", "The Soviet Union and France"],
        "Concorde, which first flew on March 2, 1969, was built by Britain and France and cruised at more than twice the speed of sound.",
        [ART + "technology/Concorde"], [SCI], ["concorde-first-flight-1969"]),
     "03-02", F, ["europe"], ["cold-war"], "Exact match on the featured flight."),
    (mc("wilt-points-record", "easy",
        "How many points did Wilt Chamberlain score in a single NBA game in 1962?",
        ["72", "81", "*100", "120"],
        "Chamberlain scored 100 points on March 2, 1962, a record still unsurpassed.",
        [U(MAR, 2)], [S], ["wilt-chamberlain-scores-100-1962"]),
     "03-02", A, ["americas"], ["cold-war"], "81 echoes the next-highest famous total."),
    (mc("morocco-independence-from", "easy",
        "From which country did Morocco proclaim independence in 1956?",
        ["Britain", "Italy", "Portugal", "*France"],
        "Morocco proclaimed independence from France on March 2, 1956.",
        [U(MAR, 2)], [EX], ["morocco-independence-1956"]),
     "03-02", A, ["africa"], ["decolonization"], "Other European colonial powers."),
    # Mar 3
    (mc("serfs-emancipation-emperor", "medium",
        "Which Russian emperor issued the 1861 manifesto freeing the serfs?",
        ["Peter the Great", "*Alexander II", "Nicholas II", "Catherine the Great"],
        "Alexander II issued the Emancipation Manifesto on March 3, 1861 (February 19 in Russia's calendar).",
        [U(MAR, 3)], [REV], ["russia-emancipates-serfs-1861"]),
     "03-03", F, ["europe", "asia"], ["1800-1945"], "Exact match; other famous Russian rulers."),
    (tf("star-spangled-banner-anthem-1812", "medium",
        "The Star-Spangled Banner has been the official US national anthem since the War of 1812.",
        False,
        "The Star-Spangled Banner was officially adopted as the national anthem on March 3, 1931.",
        [U(MAR, 3)], [S], ["star-spangled-banner-anthem-1931"]),
     "03-03", A, ["americas"], ["1800-1945"], "The song is old; its official status is not."),
    (mc("bell-birthplace", "medium",
        "In which city was Alexander Graham Bell born?",
        ["Boston", "*Edinburgh", "London", "Toronto"],
        "Bell was born in Edinburgh on March 3, 1847.",
        [U(MAR, 3)], [SCI], ["alexander-graham-bell-born-1847"]),
     "03-03", A, ["europe", "americas"], ["1800-1945"], "Cities linked to his later life."),
    # Mar 4
    (tf("fdr-last-march-4-inauguration", "medium",
        "Franklin D. Roosevelt was the last US president to be inaugurated on March 4.",
        True,
        "Roosevelt's first inauguration, on March 4, 1933, was the last held on that date.",
        [U(MAR, 4)], [LP], ["fdr-first-inauguration-1933"]),
     "03-04", F, ["americas"], ["1800-1945"], "Exact match on the featured inauguration."),
    (mc("perkins-cabinet-post", "medium",
        "Frances Perkins, the first woman appointed to a US cabinet post, served as secretary of what?",
        ["*Labor", "Education", "State", "Health"],
        "Perkins was sworn in as secretary of labor on March 4, 1933, and served until 1945.",
        [U(MAR, 4)], [LP], ["frances-perkins-sworn-in-1933"]),
     "03-04", A, ["americas"], ["1800-1945"], "Other cabinet departments."),
    (mc("four-seasons-composer", "easy",
        "Which composer wrote the violin concertos known as The Four Seasons?",
        ["Johann Sebastian Bach", "George Frideric Handel", "Wolfgang Amadeus Mozart", "*Antonio Vivaldi"],
        "Vivaldi, born in Venice on March 4, 1678, is best known for The Four Seasons.",
        [ART + "biography/Antonio-Vivaldi"], [S], ["antonio-vivaldi-born-1678"]),
     "03-04", A, ["europe"], ["early-modern"], "Other Baroque and Classical composers."),
    (mc("castle-hill-rising-country", "medium",
        "The Castle Hill Rising of 1804, in which Irish convicts rebelled, took place in which present-day country?",
        ["Ireland", "*Australia", "New Zealand", "Canada"],
        "The Castle Hill Rising of March 4, 1804, was Australia's first rebellion.",
        [U(MAR, 4)], [REV], ["castle-hill-rising-1804"]),
     "03-04", A, ["oceania", "europe"], ["revolutionary", "1800-1945"], "Ireland, the rebels' homeland, is the trap."),
    # Mar 5
    (mc("iron-curtain-speech-state", "medium",
        "In which US state did Winston Churchill give his 1946 Iron Curtain speech?",
        ["*Missouri", "Virginia", "Massachusetts", "Illinois"],
        "Churchill popularized the term Iron Curtain in a speech at Fulton, Missouri, on March 5, 1946.",
        [U(MAR, 5)], [LP], ["iron-curtain-speech-1946"]),
     "03-05", F, ["europe", "americas"], ["cold-war"], "Exact match on the featured speech."),
    (mc("boston-massacre-victim", "medium",
        "Which of these men was killed in the Boston Massacre of 1770?",
        ["Paul Revere", "Samuel Adams", "*Crispus Attucks", "John Hancock"],
        "British troops killed Crispus Attucks and four others in Boston on March 5, 1770.",
        [U(MAR, 5)], [REV], ["boston-massacre-1770"]),
     "03-05", A, ["americas", "europe"], ["revolutionary"], "Famous Boston patriots who survived."),
    (mc("stalin-successor-1953", "hard",
        "Who succeeded Joseph Stalin as Soviet premier when Stalin died in 1953?",
        ["Nikita Khrushchev", "*Georgy Malenkov", "Leonid Brezhnev", "Lavrenty Beria"],
        "Stalin died on March 5, 1953, and was succeeded by Georgy Malenkov.",
        [U(MAR, 5)], [LP], ["joseph-stalin-dies-1953"]),
     "03-05", A, ["europe", "asia"], ["cold-war"], "Khrushchev, who later outmaneuvered Malenkov, is the trap."),
    (mc("voyager-1-jupiter-moon", "medium",
        "Which spacecraft made its closest approach to Jupiter on March 5, 1979?",
        ["Pioneer 10", "*Voyager 1", "Galileo", "Juno"],
        "Voyager 1, launched in 1977, made its closest approach to Jupiter on March 5, 1979.",
        ["https://science.nasa.gov/mission/voyager/voyager-1/"], [SCI], ["voyager-1-jupiter-flyby-1979"]),
     "03-05", A, ["global"], ["space-age", "cold-war"], "Other Jupiter missions, earlier and later."),
    # Mar 6
    (mc("sistine-ceiling-artist", "easy",
        "Which artist painted the ceiling of the Sistine Chapel, from 1508 to 1512?",
        ["Raphael", "*Michelangelo", "Leonardo da Vinci", "Sandro Botticelli"],
        "Michelangelo, born on March 6, 1475, painted the Sistine Chapel ceiling between 1508 and 1512.",
        [ART + "biography/Michelangelo"], [S], ["michelangelo-born-1475"]),
     "03-06", A, ["europe"], ["early-modern"], "Botticelli painted the chapel's walls, which makes him a fair trap."),
    (mc("macondo-novel", "medium",
        "The fictional town of Macondo is the setting of which novel?",
        ["*One Hundred Years of Solitude", "Pedro Paramo", "Don Quixote", "The House of the Spirits"],
        "Gabriel Garcia Marquez, born on March 6, 1927, set One Hundred Years of Solitude (1967) in Macondo.",
        [ART + "biography/Gabriel-Garcia-Marquez"], [S], ["gabriel-garcia-marquez-born-1927"]),
     "03-06", A, ["americas"], ["1800-1945"], "Other landmark novels in Spanish."),
    (mc("alamo-siege-length", "medium",
        "How long did the siege of the Alamo last before the mission fell in 1836?",
        ["3 days", "*13 days", "30 days", "3 months"],
        "Santa Anna began the siege on February 23, 1836, and the Alamo fell on March 6, after 13 days.",
        [U(MAR, 6), U(FEB, 23)], [WC], ["fall-of-the-alamo-1836", "siege-of-the-alamo-begins-1836"]),
     "03-06", A, ["americas"], ["1800-1945"], "Joins the siege's start and end, both in this batch."),
    (mc("la-traviata-composer", "easy",
        "Who composed La Traviata, which premiered in Venice in 1853?",
        ["Giacomo Puccini", "Gioachino Rossini", "*Giuseppe Verdi", "Gaetano Donizetti"],
        "Verdi's La Traviata premiered at La Fenice on March 6, 1853.",
        [U(MAR, 6)], [S], ["la-traviata-premieres-1853"]),
     "03-06", A, ["europe"], ["1800-1945"], "Other Italian opera composers."),
    # Mar 7
    (mc("selma-march-destination", "medium",
        "The civil rights marchers attacked at Selma, Alabama, in March 1965 were trying to reach which city?",
        ["Birmingham", "*Montgomery", "Atlanta", "Memphis"],
        "On March 7, 1965, troopers attacked marchers in Selma heading for the state capitol in Montgomery.",
        [U(MAR, 7)], [REV], ["selma-bloody-sunday-1965"]),
     "03-07", A, ["americas"], ["cold-war"], "Other cities central to the civil rights movement."),
    (mc("marcus-aurelius-co-emperor", "hard",
        "In 161 CE, Marcus Aurelius made which man his co-emperor?",
        ["Commodus", "*Lucius Verus", "Antoninus Pius", "Hadrian"],
        "On March 7, 161, Marcus Aurelius declared himself and his adoptive brother Lucius Verus emperors, the first time two ruled Rome together.",
        [U(MAR, 7)], [AR, AH], ["marcus-aurelius-and-lucius-verus-emperors-161"]),
     "03-07", A, ["europe"], ["ancient"], "Commodus, his son, was made co-emperor years later."),
    (mc("bigelow-oscar-film", "easy",
        "Kathryn Bigelow became the first woman to win the best director Oscar for which film?",
        ["*The Hurt Locker", "Zero Dark Thirty", "Lost in Translation", "The Piano"],
        "Bigelow won on March 7, 2010, for The Hurt Locker.",
        [U(MAR, 7)], [S], ["kathryn-bigelow-best-director-2010"]),
     "03-07", A, ["americas"], ["contemporary"], "Another Bigelow film and films by other women directors."),
    (mc("bolero-composer", "easy",
        "Which French composer wrote Bolero (1928)?",
        ["Claude Debussy", "Erik Satie", "Camille Saint-Saens", "*Maurice Ravel"],
        "Ravel, born in Ciboure on March 7, 1875, wrote Bolero, a piece that begins softly and ends as loudly as possible.",
        [ART + "biography/Maurice-Ravel"], [S], ["maurice-ravel-born-1875"]),
     "03-07", A, ["europe"], ["1800-1945"], "Other French composers of the period."),
    # Cross-day ordering questions
    (order("order-crewed-spaceflight-feb-mar", "medium",
           "Put these spaceflight milestones in order from earliest to latest.",
           [("skylab-4", "Skylab 4 crew returns after 84 days"), ("glenn", "John Glenn orbits Earth"),
            ("falcon-heavy", "Falcon Heavy's first test flight"), ("apollo-14", "Apollo 14 lands on the Moon")],
           ["glenn", "apollo-14", "skylab-4", "falcon-heavy"],
           "Glenn orbited Earth in 1962, Apollo 14 landed in 1971, Skylab 4 returned in 1974, and Falcon Heavy first flew in 2018.",
           [U(FEB, 20), ART + "topic/Apollo-14", U(FEB, 8), U(FEB, 6)], [SCI],
           ["john-glenn-orbits-earth-1962", "apollo-14-lands-on-moon-1971", "skylab-4-splashdown-1974", "falcon-heavy-first-flight-2018"]),
     "02-06", A, ["americas"], ["space-age", "contemporary"], "Every item is an event in this batch."),
    (order("order-independence-feb-mar", "medium",
           "Put these declarations or grants of independence in order from earliest to latest.",
           [("ghana", "Ghana"), ("grenada", "Grenada"), ("chile", "Chile"), ("ceylon", "Ceylon (Sri Lanka)")],
           ["chile", "ceylon", "ghana", "grenada"],
           "Chile declared independence in 1818, Ceylon gained it in 1948, Ghana in 1957 and Grenada in 1974.",
           [U(FEB, 12), U(FEB, 4), U(MAR, 6), U(FEB, 7)], [EX],
           ["chile-declares-independence-1818", "ceylon-independence-1948", "ghana-becomes-independent-1957", "grenada-independence-1974"]),
     "02-04", A, ["global"], ["revolutionary", "decolonization"], "Every item is an event in this batch."),
    (order("order-composer-births-feb-mar", "medium",
           "Put the births of these composers in order from earliest to latest.",
           [("rossini", "Gioachino Rossini"), ("vivaldi", "Antonio Vivaldi"), ("mendelssohn", "Felix Mendelssohn"), ("handel", "George Frideric Handel")],
           ["vivaldi", "handel", "rossini", "mendelssohn"],
           "Vivaldi was born in 1678, Handel in 1685, Rossini in 1792 and Mendelssohn in 1809.",
           [ART + "biography/Antonio-Vivaldi", ART + "biography/George-Frideric-Handel",
            ART + "biography/Gioachino-Rossini", U(FEB, 3)], [S],
           ["antonio-vivaldi-born-1678", "george-frideric-handel-born-1685", "gioachino-rossini-born-1792", "felix-mendelssohn-born-1809"]),
     "02-29", A, ["europe"], ["early-modern", "1800-1945"], "Every item is a birth in this batch."),
]

# Balance correct-option positions: these questions are written with the answer in another slot
# above for readability; it is moved to the given position (0-based) in the written drafts.
REPOSITION = {q: 3 for q in ["groundhog-day-town", "umm-kulthum-country", "marley-music-style", "abdullah-ii-father",
                             "color-purple-author", "mubarak-country", "thatcher-predecessor", "simenon-detective",
                             "galilean-moons-planet", "kosovo-independence-from", "luther-movement", "verdun-war",
                             "white-rose-university", "castle-hill-rising-country"]}
REPOSITION.update({q: 0 for q in ["trygve-lie-country", "falcon-heavy-company", "puyi-last-emperor-of", "palme-country"]})
for _q, *_ in NEW:
    if _q["id"] in REPOSITION:
        _opts = _q["options"]
        _c = next(o for o in _opts if o["id"] == _q["correctOptionId"])
        _opts.remove(_c)
        _opts.insert(REPOSITION[_q["id"]], _c)

# (existing question, event, calendarDay, role, regions, eras, sources, rationale, image-rights evidence)
LINKS = [
    ("south-africa-1994-president", "de-klerk-lifts-anc-ban-1990", "02-02", A, ["africa"], ["contemporary"],
     ["https://www.nelsonmandela.org/biography", U(FEB, 2)],
     "Lifting the ban on the ANC led to Mandela's release and the 1994 election the question asks about.", None),
    ("tehran-conference-big-three", "yalta-conference-opens-1945", "02-04", A, ["europe"], ["1800-1945"],
     ["https://www.britannica.com/on-this-day/November-28", U(FEB, 4)],
     "The same three leaders met again at Yalta; the day page names all three.", None),
    ("treaty-waitangi-parties", "treaty-of-waitangi-signed-1840", "02-06", F, ["oceania", "europe"], ["1800-1945"],
     ["https://nzhistory.govt.nz/politics/treaty/the-treaty-in-brief", U(FEB, 6)],
     "Exact match: the question asks who signed the treaty with the British Crown in 1840.", None),
    ("beatles-home-city", "beatles-land-in-new-york-1964", "02-07", F, ["europe", "americas"], ["cold-war"],
     ["https://www.thebeatles.com/please-please-me", U(FEB, 7)],
     "Accessible Beatles hook for their arrival in America; already related to Love Me Do.", None),
    ("identify-nelson-mandela", "nelson-mandela-released-1990", "02-11", F, ["africa"], ["contemporary"],
     ["https://www.nelsonmandela.org/biography", U(FEB, 11)],
     "The portrait bust identifies Mandela, whose release the featured event marks.",
     "docs/QUIZ_CONTENT_REVIEW.md (Q7 image-rights table)"),
    ("identify-charles-darwin", "charles-darwin-born-1809", "02-12", F, ["europe"], ["1800-1945"],
     ["https://www.britannica.com/biography/Charles-Darwin", U(FEB, 12)],
     "The portrait identifies Darwin, whose birth is the featured event; already related to the Beagle's return.",
     "editorial/batches/2026-09-l7-second-accessible-global-history/review-ledger.json"),
    ("identify-abraham-lincoln", "abraham-lincoln-born-1809", "02-12", A, ["americas"], ["1800-1945"],
     ["https://www.loc.gov/pictures/item/2008680391/", U(FEB, 12)],
     "The portrait identifies Lincoln, born on this day.",
     "editorial/batches/2026-09-l7-second-accessible-global-history/review-ledger.json"),
    ("snoopy-comic-strip", "last-peanuts-strip-2000", "02-13", F, ["americas"], ["contemporary"],
     ["https://schulzmuseum.org/about-schulz/schulz-biography/", U(FEB, 13)],
     "Peanuts ended on this day; already related to its 1950 debut.", None),
    ("copernican-model-earth-orbits-sun", "nicolaus-copernicus-born-1473", "02-19", F, ["europe"], ["early-modern"],
     ["https://www.britannica.com/biography/Nicolaus-Copernicus"],
     "Exact match: the question is about Copernicus's Sun-centered model; the event is his birth.", None),
    ("identify-george-washington", "george-washington-born-1732", "02-22", A, ["americas"], ["early-modern", "revolutionary"],
     ["https://www.metmuseum.org/art/collection/search/16584", U(FEB, 22)],
     "The portrait identifies Washington, born on this day.",
     "editorial/batches/2026-09-l7-accessible-global-history/review-ledger.json"),
    ("gregorian-century-leap-year", "gregorian-calendar-papal-bull-1582", "02-24", F, ["europe", "global"], ["early-modern"],
     ["https://www.britannica.com/topic/Gregorian-calendar", U(FEB, 24)],
     "The leap-year rule the question tests is part of the reform the bull decreed; already related to the calendar taking effect.", None),
    ("identify-winston-churchill", "iron-curtain-speech-1946", "03-05", F, ["europe", "americas"], ["cold-war"],
     ["https://www.britannica.com/biography/Winston-Churchill", U(MAR, 5)],
     "The portrait identifies the speaker of the Iron Curtain speech.",
     "docs/QUIZ_CONTENT_REVIEW.md (Q7 image-rights table)"),
    ("ghana-independence-1957", "ghana-becomes-independent-1957", "03-06", F, ["africa"], ["decolonization"],
     ["https://mfa.gov.gh/wp-content/uploads/2021/02/The-Ghanaian-Envoy-Newsletter-2019.pdf", U(MAR, 6)],
     "Exact match: the question asks which country became independent under Nkrumah in 1957.", None),
    ("order-modern-communication-milestones", "bell-telephone-patent-1876", "03-07", F, ["americas"], ["1800-1945"],
     ["https://www.britannica.com/biography/Alexander-Graham-Bell", U(MAR, 7)],
     "One ordering item is 'Bell receives a telephone patent', the featured event.", None),
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
    return {"url": url, "status": "verified_supporting", "checkedOn": CHECKED, "note": SRC[url][1]}


def answer_text(q):
    if q["type"] == "chronological_ordering":
        items = {i["id"]: i["text"] for i in q["items"]}
        return " < ".join(items[i] for i in q["correctOrderItemIds"])
    return next(o["text"] for o in q["options"] if o["id"] == q["correctOptionId"])


def build(bank, events):
    out = f"editorial/batches/{BATCH_ID}"
    taken = draft_question_ids()
    candidates, entries, link_records = [], [], []
    for q, day, role, regions, eras, rel in NEW:
        assert q["id"] not in bank and q["id"] not in taken, q["id"]
        assert all(r in events for r in q["relatedEventIds"]), q["id"]
        urls = [s["url"] for s in q["sources"]]
        candidates.append({"candidateId": q["id"], "kind": "quiz", "proposedCanonicalId": q["id"],
                           "workingClaim": f"{q['prompt']} Answer: {answer_text(q)}. Proposed {role} link on {day}: {', '.join(q['relatedEventIds'])}. {rel}",
                           "sourceUrls": urls})
        entries.append({"candidateId": q["id"], "canonicalId": q["id"], "reviewStatus": "source_verified",
                        "sourceChecks": [check(u) for u in urls], "imageRightsStatus": "not_applicable",
                        "regions": regions, "eras": eras, "calendarDays": [day],
                        "notes": NOTE_NEW.format(rel=rel)})
    for qid, event_id, day, role, regions, eras, urls, rel, rights in LINKS:
        path, q = bank[qid]
        assert q["publicationState"] == "published", qid
        assert event_id in events, event_id
        assert event_id not in (q.get("relatedEventIds") or []), qid
        snapshot = hashlib.sha256(json.dumps(q, separators=(",", ":")).encode()).hexdigest()
        link_records.append({"questionId": qid, "canonicalPack": path, "canonicalSnapshotSha256": snapshot,
                             "currentRelatedEventIds": q.get("relatedEventIds") or [],
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
        "state": "editorial_only_awaiting_owner_content_approval",
        "newQuestionCount": len(qs), "reusedQuestionCount": len(LINKS),
        "proposedNewRelationCount": sum(len(q["relatedEventIds"]) for q in qs) + len(LINKS),
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


def main():
    bank = canonical_questions()
    events = set()
    for b in EVENT_BATCHES:
        events |= {e["id"] for e in json.load(open(f"editorial/batches/{b}/draft-events.json"))["events"]}
    build(bank, events)
    ids = [n[0]["id"] for n in NEW]
    assert len(ids) == len(set(ids))
    print(f"{len(NEW)} new drafts, {len(LINKS)} links")


if __name__ == "__main__":
    main()
