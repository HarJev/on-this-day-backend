"""February 1 to March 7 event drafts (five batches).

Run from the repository root:
    PYTHONPATH=editorial/tools/runway python3 editorial/tools/runway/b0201.py

Most events cite Britannica's dated On This Day page for their day, read on
2026-10-01; the check note records what that page states. Where the day page
was missing a fact, framed it doubtfully, or got it wrong, the event cites (or
adds) an article page instead. The first event added for a day is featured.
"""
from lib import Batch

BR = "Encyclopaedia Britannica - "
MONTH = {2: "February", 3: "March"}
BATCHES = [
    ("2027-02-01-07-historical-events", [(2, d) for d in range(1, 8)]),
    ("2027-02-08-14-historical-events", [(2, d) for d in range(8, 15)]),
    ("2027-02-15-21-historical-events", [(2, d) for d in range(15, 22)]),
    ("2027-02-22-29-historical-events", [(2, d) for d in range(22, 30)]),
    ("2027-03-01-07-historical-events", [(3, d) for d in range(1, 8)]),
]
EVENTS = []


def day_page(m, d, quote):
    name = MONTH[m]
    return (f"{BR}On This Day: {name} {d}", f"https://www.britannica.com/on-this-day/{name}-{d}",
            f"Britannica On This Day ({name[:3]} {d}): {quote}")


def ev(m, d, short, id, title, year, summary, desc, quote, regions, eras,
       nt=None, nb=None, note=None, extra=None, only=None):
    """quote: what the day's Britannica On This Day page states.
    extra: article sources (name, url, note) cited after the day page.
    only: replacement source list, when the day page is not cited."""
    hd = f"{MONTH[m]} {d}, {year}"
    if only:
        sources = only
    else:
        sources = [day_page(m, d, quote)] + (extra or [])
    EVENTS.append(((m, d), (f"{m:02d}-{d:02d}", short, id, title, year, hd, summary,
                            desc.replace("{hd}", hd), sources, regions, eras, nt, nb, note)))


def art(title, url, note):
    return (BR + title, url, "Britannica: " + note)


J = "Julian calendar date."

# ---------------------------------------------------------------- Feb 1
ev(2, 1, "greensboro-sit-in", "greensboro-sit-in-begins-1960", "The Greensboro lunch counter sit-in begins", "1960",
   "Four African Americans sit down at a segregated Woolworth's counter and refuse to leave.",
   "On {hd}, four African Americans began a sit-in at a segregated Woolworth's lunch counter in Greensboro, North Carolina, a protest that helped spark sit-ins across the American South.",
   "1960: 'Protesting a segregated lunch counter at a Woolworth's in Greensboro, North Carolina, four African Americans began a sit-in.'",
   ["americas"], ["cold-war"], "Four people, one lunch counter",
   "In 1960, four African Americans sat in at a segregated Woolworth's counter in Greensboro.")
ev(2, 1, "la-boheme", "la-boheme-premieres-1896", "Puccini's La Boheme premieres in Turin", "1896",
   "Giacomo Puccini's opera opens at the Teatro Regio.",
   "On {hd}, Giacomo Puccini's opera La Boheme had its premiere at the Teatro Regio in Turin, Italy.",
   "1896: 'Giacomo Puccini premiered his opera La Boheme at the Teatro Regio in Turin, Italy.'",
   ["europe"], ["1800-1945"])
ev(2, 1, "trygve-lie", "trygve-lie-first-un-secretary-general-1946", "Trygve Lie is elected the first UN secretary-general", "1946",
   "A Norwegian statesman becomes the United Nations' first chief.",
   "On {hd}, the Norwegian politician and diplomat Trygve Lie was elected the first secretary-general of the United Nations.",
   "1946: 'Politician and diplomat Trygve Lie was elected the first secretary-general of the United Nations.'",
   ["europe", "global"], ["cold-war"],
   extra=[art("Trygve Lie", "https://www.britannica.com/biography/Trygve-Lie",
              "'Norwegian statesman'; 'On Feb. 1, 1946, Lie was elected UN secretary-general'; resigned November 10, 1952.")])
ev(2, 1, "columbia-disaster", "columbia-disaster-2003", "Space shuttle Columbia breaks up over Texas", "2003",
   "All seven astronauts are lost as the shuttle returns to Earth.",
   "On {hd}, the US space shuttle Columbia broke up at an altitude of about 40 miles (60 km) over Texas while returning to Earth, killing all seven crew members.",
   "2003: 'The U.S. space shuttle Columbia broke up catastrophically at an altitude of about 40 miles (60 km) over Texas, killing all seven crew members.'",
   ["americas"], ["contemporary", "space-age"])
ev(2, 1, "johanna-sigurdardottir", "johanna-sigurdardottir-iceland-prime-minister-2009", "Johanna Sigurdardottir becomes Iceland's first woman prime minister", "2009",
   "Iceland swears in its first female head of government.",
   "On {hd}, Johanna Sigurdardottir was sworn in as prime minister of Iceland, the first woman to hold the post.",
   "2009: 'Politician Johanna Sigurdardottir was sworn in as Iceland's prime minister, becoming the first woman to hold the post.'",
   ["europe"], ["contemporary"])

# ---------------------------------------------------------------- Feb 2
ev(2, 2, "groundhog-day", "first-punxsutawney-groundhog-day-1887", "Punxsutawney turns to a groundhog for a forecast", "1887",
   "A Pennsylvania town begins its famous Groundhog Day tradition.",
   "On {hd}, a group in Punxsutawney, Pennsylvania, went looking for a groundhog to predict the weather, beginning the town's Groundhog Day tradition.",
   "1887: 'On this day in 1887, a group in Punxsutawney, Pennsylvania, searched for a rodent to predict the weather.'",
   ["americas"], ["1800-1945"], "A rodent weather forecaster",
   "In 1887, a group in Punxsutawney, Pennsylvania, sought a groundhog to predict the weather.")
ev(2, 2, "guadalupe-hidalgo", "treaty-of-guadalupe-hidalgo-1848", "The Treaty of Guadalupe Hidalgo is signed", "1848",
   "The treaty ends the Mexican-American War.",
   "On {hd}, the United States and Mexico signed the Treaty of Guadalupe Hidalgo, ending the Mexican-American War.",
   "1848: 'The United States and Mexico signed the Treaty of Guadalupe Hidalgo, which ended the Mexican-American War.'",
   ["americas"], ["1800-1945"])
ev(2, 2, "anc-ban-lifted", "de-klerk-lifts-anc-ban-1990", "F.W. de Klerk lifts the ban on the African National Congress", "1990",
   "South Africa's president opens the way to Mandela's release.",
   "On {hd}, South African President F.W. de Klerk lifted the 30-year ban on the African National Congress, leading to Nelson Mandela's release from prison and marking the beginning of the end of apartheid.",
   "1990: 'South African President F.W. de Klerk lifted the 30-year ban on the African National Congress, resulting in Nelson Mandela's release from prison and marking the beginning of the end of apartheid.'",
   ["africa"], ["contemporary"])
ev(2, 2, "stalingrad-ends", "battle-of-stalingrad-ends-1943", "The Battle of Stalingrad ends", "1943",
   "The last German troops in the city surrender to the Soviets.",
   "On {hd}, the Battle of Stalingrad ended with the surrender of German troops to the Soviet army, a turning point of World War II.",
   "1943: 'The Battle of Stalingrad in World War II ended with the surrender of German troops to the Soviets.'",
   ["europe"], ["1800-1945"],
   extra=[art("Battle of Stalingrad", "https://www.britannica.com/event/Battle-of-Stalingrad",
              "battle August 22, 1942 - February 2, 1943; 'Stalingrad (now Volgograd, Russia)'.")])
ev(2, 2, "joyce-born", "james-joyce-born-1882", "James Joyce is born in Dublin", "1882",
   "The author of Ulysses is born.",
   "On {hd}, the Irish writer James Joyce, author of Ulysses, was born in Dublin.",
   "", ["europe"], ["1800-1945"],
   only=[art("James Joyce", "https://www.britannica.com/biography/James-Joyce",
             "born 'February 2, 1882, Dublin, Ireland'; Ulysses published 1922."),
         art("Ulysses", "https://www.britannica.com/topic/Ulysses-novel-by-Joyce",
             "'first published in book form in Paris in 1922' by Sylvia Beach (Shakespeare and Company); set in 'Dublin on a single day (June 16, 1904)'.")])

# ---------------------------------------------------------------- Feb 3
ev(2, 3, "eileen-collins", "eileen-collins-first-woman-shuttle-pilot-1995", "Eileen Collins becomes the first woman to pilot a space shuttle", "1995",
   "She takes the pilot's seat aboard Discovery.",
   "On {hd}, the American astronaut Eileen Collins became the first woman to pilot a space shuttle, flying aboard Discovery.",
   "1995: 'American astronaut Eileen Collins became the first woman to pilot a space shuttle, the Discovery.'",
   ["americas"], ["contemporary", "space-age"], "A first in the shuttle's front seat",
   "In 1995, Eileen Collins became the first woman to pilot a space shuttle.")
ev(2, 3, "fifteenth-amendment", "fifteenth-amendment-ratified-1870", "The Fifteenth Amendment is ratified", "1870",
   "The US Constitution bars denying the vote on the basis of race.",
   "On {hd}, the Fifteenth Amendment to the US Constitution was ratified, guaranteeing Americans the right to vote regardless of race.",
   "1870: 'The Fifteenth Amendment to the Constitution of the United States was ratified, guaranteeing Americans the right to vote regardless of race.'",
   ["americas"], ["1800-1945"])
ev(2, 3, "mendelssohn-born", "felix-mendelssohn-born-1809", "Felix Mendelssohn is born", "1809",
   "The German composer who bridged Classical and Romantic music is born.",
   "On {hd}, the German composer Felix Mendelssohn was born. He largely followed Classical models while helping to shape Romanticism.",
   "1809: German composer born this day in 1809, who 'largely observed Classical models and practices while initiating key aspects of Romanticism.'",
   ["europe"], ["1800-1945"])
ev(2, 3, "umm-kulthum", "umm-kulthum-dies-1975", "Umm Kulthum dies in Cairo", "1975",
   "One of the most famous Arab singers of the 20th century dies.",
   "On {hd}, the Egyptian singer Umm Kulthum, one of the most famous Arab singers and public figures of the 20th century, died in Cairo.",
   "1975: 'Egyptian singer Umm Kulthum, who was one of the most famous Arab singers and public personalities of the 20th century, died in Cairo.'",
   ["middle-east", "africa"], ["cold-war"])
ev(2, 3, "buddy-holly", "buddy-holly-dies-1959", "Buddy Holly is killed in a plane crash", "1959",
   "The rock 'n' roll singer dies at age 22.",
   "On {hd}, the American rock 'n' roll singer Buddy Holly was killed in a plane crash at age 22.",
   "1959: 'American rock 'n' roll singer Buddy Holly was killed in a plane crash at age 22.'",
   ["americas"], ["cold-war"])

# ---------------------------------------------------------------- Feb 4
ev(2, 4, "facebook", "thefacebook-launched-2004", "TheFacebook.com is launched at Harvard", "2004",
   "Four Harvard students launch the social network that becomes Facebook.",
   "On {hd}, Harvard students Mark Zuckerberg, Eduardo Saverin, Dustin Moskovitz and Chris Hughes founded and launched the website TheFacebook.com, which became Facebook.",
   "2004: 'The website Facebook was founded and launched by Harvard students Mark Zuckerberg, Eduardo Saverin, Dustin Moskovitz, and Chris Hughes as TheFacebook.com.'",
   ["americas", "global"], ["contemporary"], "The dorm-room site that went global",
   "In 2004, Harvard students launched TheFacebook.com, the site that became Facebook.")
ev(2, 4, "ceylon-independence", "ceylon-independence-1948", "Ceylon gains independence from Britain", "1948",
   "The island now called Sri Lanka ends more than 150 years of British control.",
   "On {hd}, Ceylon, now Sri Lanka, gained independence from Great Britain, which had controlled the island since 1796.",
   "1948: 'Ceylon (now Sri Lanka) gained independence from Great Britain, which had controlled the island since 1796.'",
   ["asia"], ["decolonization"])
ev(2, 4, "yalta-opens", "yalta-conference-opens-1945", "The Yalta Conference opens", "1945",
   "Roosevelt, Churchill and Stalin meet as World War II nears its end.",
   "On {hd}, in the final stages of World War II, the Yalta Conference opened with Franklin D. Roosevelt, Winston Churchill and Joseph Stalin.",
   "1945: 'During the final stages of World War II, the Yalta Conference opened with Franklin D. Roosevelt, Winston Churchill, and Joseph Stalin.'",
   ["europe"], ["1800-1945"])
ev(2, 4, "washington-elected", "washington-elected-first-president-1789", "George Washington is elected the first US president", "1789",
   "The first electoral college votes for him unanimously.",
   "On {hd}, George Washington was elected the first president of the United States by a unanimous vote of the first electoral college.",
   "1789: 'George Washington was elected to serve as the first U.S. president by a unanimous vote in the first electoral college.'",
   ["americas"], ["revolutionary"])
ev(2, 4, "lake-placid-1932", "lake-placid-winter-olympics-open-1932", "Lake Placid hosts the first Winter Olympics in the United States", "1932",
   "The Winter Games come to New York state.",
   "On {hd}, the United States hosted its first Winter Olympic Games, in Lake Placid, New York.",
   "1932: 'The United States hosted its first Winter Olympic Games in Lake Placid, New York.'",
   ["americas"], ["1800-1945"])

# ---------------------------------------------------------------- Feb 5
ev(2, 5, "mexico-constitution", "mexican-constitution-adopted-1917", "Mexico adopts its present constitution", "1917",
   "The constitution that still governs Mexico is adopted.",
   "On {hd}, Mexico adopted its present constitution, which holds that the government should take an active role in the country's day-to-day affairs.",
   "1917: 'Mexico adopted its present constitution, which posits that the Mexican government should take an active, rather than passive, role in day-to-day affairs.'",
   ["americas"], ["1800-1945"], "A constitution still in force",
   "In 1917, Mexico adopted the constitution that still governs the country today.")
ev(2, 5, "hay-pauncefote", "first-hay-pauncefote-treaty-1900", "The first Hay-Pauncefote Treaty is signed", "1900",
   "The United States and Britain sign a treaty on a projected Central American canal.",
   "On {hd}, the United States and Great Britain signed the first Hay-Pauncefote Treaty, concerning a projected Central American canal. The US Senate declined to ratify it, and a second treaty in 1901 gave the United States a free hand.",
   "1900: 'The first of two Hay-Pauncefote treaties ... was signed between the United States and Great Britain.' (Date only; the page's 'proposed Panama Canal' is not used, as the route was not yet chosen.)",
   ["americas", "europe"], ["1800-1945"],
   extra=[art("Hay-Pauncefote Treaty", "https://www.britannica.com/event/Hay-Pauncefote-Treaty",
              "first treaty, February 5, 1900, concerned 'a projected Central American canal'; the US Senate 'declined to ratify it because it still restricted U.S. rights over the proposed canal'; the second treaty (1901) 'definitely abrogated the agreement of 1850 and gave the United States a free hand'.")])
ev(2, 5, "apollo-14", "apollo-14-lands-on-moon-1971", "Apollo 14 lands on the Moon", "1971",
   "Alan Shepard and Edgar Mitchell touch down in the Fra Mauro highlands.",
   "On {hd}, Apollo 14's lunar module landed in the Fra Mauro highlands of the Moon. Commander Alan Shepard later swung at two golf balls with a makeshift club.",
   "", ["americas"], ["space-age", "cold-war"],
   only=[art("Apollo 14", "https://www.britannica.com/topic/Apollo-14",
             "mission January 31 to February 9, 1971; photo caption dates the landing February 5, 1971; 'The first landing in the lunar Fra Mauro highlands'; crew Alan Shepard, Jr., Stuart A. Roosa and Edgar Mitchell; Shepard 'swung at two golf balls with a makeshift six-iron club'."),
         ("NASA - Apollo 14", "https://www.nasa.gov/mission/apollo-14/",
          "NASA: launched Jan. 31, 1971; entered lunar orbit Feb. 4, 1971; landing site Fra Mauro; Shepard commander, Mitchell lunar module pilot, Roosa command module pilot.")])
ev(2, 5, "ronaldo-born", "cristiano-ronaldo-born-1985", "Cristiano Ronaldo is born in Funchal", "1985",
   "The Portuguese soccer star is born.",
   "On {hd}, the soccer player Cristiano Ronaldo was born in Funchal, Portugal.",
   "1985: 'Soccer player Cristiano Ronaldo ... was born in Funchal, Portugal.'",
   ["europe"], ["contemporary"])
ev(2, 5, "powell-un", "powell-un-iraq-presentation-2003", "Colin Powell presents the case against Iraq at the UN", "2003",
   "The US secretary of state argues that Iraq holds weapons of mass destruction.",
   "On {hd}, US Secretary of State Colin Powell appeared before the United Nations Security Council to present evidence that Iraq possessed weapons of mass destruction.",
   "2003: 'U.S. Secretary of State Colin Powell appeared before the United Nations Security Council to present evidence ... that Iraq possessed weapons of mass destruction.'",
   ["middle-east", "americas", "global"], ["contemporary"])

# ---------------------------------------------------------------- Feb 6
ev(2, 6, "waitangi", "treaty-of-waitangi-signed-1840", "The Treaty of Waitangi is signed", "1840",
   "The British Crown and Maori chiefs sign New Zealand's founding document.",
   "On {hd}, the British and Maori chiefs of the North Island signed the Treaty of Waitangi (Te Tiriti o Waitangi).",
   "1840: 'the British and the Maori tribes of North Island signed Te Tiriti o Waitangi (the Treaty of Waitangi).'",
   ["oceania", "europe"], ["1800-1945"], "The treaty at the heart of New Zealand",
   "In 1840, the British Crown and Maori chiefs signed the Treaty of Waitangi.")
ev(2, 6, "elizabeth-ii-accession", "elizabeth-ii-accession-1952", "Elizabeth II becomes queen", "1952",
   "She succeeds her father, King George VI.",
   "On {hd}, Elizabeth II ascended the throne of the United Kingdom following the death of her father, King George VI.",
   "1952: 'Elizabeth II ascended the throne of the United Kingdom ... following the death of her father, King George VI.'",
   ["europe"], ["cold-war"])
ev(2, 6, "marley-born", "bob-marley-born-1945", "Bob Marley is born", "1945",
   "The Jamaican singer-songwriter who took reggae worldwide is born.",
   "On {hd}, the singer-songwriter Bob Marley was born. He found stardom blending ska, rock steady and reggae.",
   "1945: 'Singer-songwriter Bob Marley ... achieved stardom by blending early ska, rock steady, and reggae.'",
   ["americas"], ["1800-1945"])
ev(2, 6, "babe-ruth-born", "babe-ruth-born-1895", "Babe Ruth is born", "1895",
   "The home-run hitter becomes one of America's most celebrated athletes.",
   "On {hd}, the baseball player Babe Ruth was born. His home-run hitting made him one of the most celebrated athletes in American sports.",
   "1895: 'Baseball player Babe Ruth, whose home-run hitting helped make him one of the most celebrated athletes in American sports, was born.'",
   ["americas"], ["1800-1945"])
ev(2, 6, "falcon-heavy", "falcon-heavy-first-flight-2018", "SpaceX's Falcon Heavy makes its first test flight", "2018",
   "A new heavy-lift rocket launches for the first time.",
   "On {hd}, SpaceX's Falcon Heavy launch vehicle made its first test flight.",
   "2018: 'SpaceX's Falcon Heavy launch vehicle had its first test flight.'",
   ["americas"], ["contemporary", "space-age"])

# ---------------------------------------------------------------- Feb 7
ev(2, 7, "beatles-new-york", "beatles-land-in-new-york-1964", "The Beatles land in New York City", "1964",
   "Beatlemania crosses the Atlantic.",
   "On {hd}, the Beatles landed in New York City.",
   "1964: 'The Beatles landed in New York City.'",
   ["americas", "europe"], ["cold-war"], "Beatlemania touches down",
   "In 1964, the Beatles landed in New York City.")
ev(2, 7, "little-tramp", "chaplin-little-tramp-debut-1914", "Charlie Chaplin's Little Tramp debuts", "1914",
   "Chaplin's most famous character first appears on screen.",
   "On {hd}, Charlie Chaplin debuted his most famous screen character, the Little Tramp, in the film Kid Auto Races at Venice.",
   "1914: 'Charlie Chaplin debuted his most famous screen character - the Little Tramp' in the film Kid Auto Races at Venice.",
   ["americas", "europe"], ["1800-1945"])
ev(2, 7, "dickens-born", "charles-dickens-born-1812", "Charles Dickens is born in Portsmouth", "1812",
   "The author of Oliver Twist and A Christmas Carol is born.",
   "On {hd}, the novelist Charles Dickens was born in Portsmouth, England.",
   "1812: 'Charles Dickens ... was born in England on this day in 1812.'",
   ["europe"], ["1800-1945"],
   extra=[art("Charles Dickens", "https://www.britannica.com/biography/Charles-Dickens-British-novelist",
              "born 'February 7, 1812, Portsmouth, Hampshire, England'; Oliver Twist (1837-39), A Christmas Carol (1843), David Copperfield (1850), Great Expectations (1861).")])
ev(2, 7, "grenada-independence", "grenada-independence-1974", "Grenada gains independence from Britain", "1974",
   "The Caribbean island becomes an independent nation.",
   "On {hd}, Grenada gained independence from the United Kingdom.",
   "1974: 'Grenada gained independence from the United Kingdom.'",
   ["americas"], ["decolonization"])
ev(2, 7, "abdullah-ii", "abdullah-ii-becomes-king-of-jordan-1999", "Abdullah II becomes king of Jordan", "1999",
   "He succeeds his father, King Hussein, hours after his death.",
   "On {hd}, Abdullah II became king of Jordan, hours after the death of his father, King Hussein.",
   "1999: 'Abdullah II became king of Jordan hours after the death of his father, Hussein.'",
   ["middle-east"], ["contemporary"])

# ---------------------------------------------------------------- Feb 8
ev(2, 8, "verne-born", "jules-verne-born-1828", "Jules Verne is born in Nantes", "1828",
   "The author of Twenty Thousand Leagues Under the Sea is born.",
   "On {hd}, the French writer Jules Verne was born in Nantes. His Extraordinary Journeys included Journey to the Centre of the Earth and Around the World in Eighty Days.",
   "", ["europe"], ["1800-1945"], "The writer who imagined the future",
   "In 1828, Jules Verne, author of Twenty Thousand Leagues Under the Sea, was born in Nantes.",
   only=[art("Jules Verne", "https://www.britannica.com/biography/Jules-Verne",
             "born 'February 8, 1828, Nantes, France'; Journey to the Centre of the Earth (1864), From the Earth to the Moon (1865), Twenty Thousand Leagues Under the Sea (1870), Around the World in Eighty Days (1873); series Voyages extraordinaires; submarine Nautilus.")])
ev(2, 8, "skylab-4", "skylab-4-splashdown-1974", "Skylab 4 returns after a record 84 days in space", "1974",
   "The crew of the last Skylab mission splashes down in the Pacific.",
   "On {hd}, the Skylab 4 crew splashed down in the Pacific Ocean after 84 days in space, a new record for space endurance.",
   "1974: 'The Skylab 4 space station ... splashed down into the Pacific Ocean. The crew ... had spent 84 days in space, set a new record for space endurance.'",
   ["americas"], ["space-age", "cold-war"])
ev(2, 8, "mary-queen-of-scots", "mary-queen-of-scots-executed-1587", "Mary, Queen of Scots, is beheaded", "1587",
   "Elizabeth I's rival is executed at Fotheringhay Castle.",
   "On {hd}, Mary, Queen of Scots, the rival of Queen Elizabeth I of England, was beheaded at Fotheringhay Castle.",
   "1587: 'Mary, Queen of Scots, rival of Queen Elizabeth I of England, was beheaded at Fotheringhay Castle.'",
   ["europe"], ["early-modern"], note=J)
ev(2, 8, "dawes-act", "dawes-act-1887", "The Dawes General Allotment Act becomes law", "1887",
   "The US law breaks up Native American reservation land into allotments.",
   "On {hd}, the United States passed the Dawes General Allotment Act, providing for the distribution of Native American reservation land.",
   "1887: 'The United States passed the Dawes General Allotment Act, providing for the distribution of Native American reservation land.'",
   ["americas"], ["1800-1945"])
ev(2, 8, "james-dean-born", "james-dean-born-1931", "James Dean is born", "1931",
   "The actor who came to symbolize restless youth is born.",
   "On {hd}, the American actor James Dean was born.",
   "1931: 'Actor James Dean ... was born.'",
   ["americas"], ["1800-1945"])

# ---------------------------------------------------------------- Feb 9
ev(2, 9, "copernicium", "element-112-synthesized-1996", "Element 112 is created in Germany", "1996",
   "Physicists make the heavy element later named copernicium.",
   "On {hd}, the German physicist Peter Armbruster and his team synthesized chemical element 112, a heavy element later named copernicium in honor of the astronomer Nicolaus Copernicus.",
   "1996: 'German physicist Peter Armbruster and his team of scientists synthesized chemical element 112, a heavy transuranium element that was later named copernicium.'",
   ["europe"], ["contemporary"], "A new element joins the table",
   "In 1996, scientists in Germany made element 112, later named copernicium after Copernicus.",
   extra=[art("Copernicium", "https://www.britannica.com/science/copernicium",
              "atomic number 112; named for Polish astronomer Nicolaus Copernicus; first produced in 1996 at the Institute for Heavy Ion Research (GSI) in Darmstadt, Germany; name approved by IUPAC in February 2010.")])
ev(2, 9, "alinagar", "treaty-of-alinagar-1757", "The Treaty of Alinagar puts Calcutta under British control", "1757",
   "The agreement is a prelude to the British seizure of Bengal.",
   "On {hd}, the Treaty of Alinagar placed what is today Kolkata under British control, a prelude to the British seizure of Bengal.",
   "1757: 'The Treaty of Alinagar placed what is today Kolkata under British control and served as a prelude to the seizure of Bengal.'",
   ["asia", "europe"], ["early-modern"])
ev(2, 9, "coetzee-born", "jm-coetzee-born-1940", "J.M. Coetzee is born", "1940",
   "The South African novelist and future Nobel laureate is born.",
   "On {hd}, the South African novelist, critic and translator J.M. Coetzee was born. He won the 2003 Nobel Prize for Literature.",
   "1940: 'South African novelist, critic, and translator J.M. Coetzee, who won the 2003 Nobel Prize for Literature, was born.'",
   ["africa"], ["1800-1945"])
ev(2, 9, "alice-walker-born", "alice-walker-born-1944", "Alice Walker is born", "1944",
   "The author of The Color Purple is born.",
   "On {hd}, the American writer Alice Walker was born. She is perhaps best known for her Pulitzer Prize-winning novel The Color Purple (1982).",
   "1944: 'American writer Alice Walker, born this day in 1944 and perhaps best known for the Pulitzer Prize-winning The Color Purple (1982).'",
   ["americas"], ["1800-1945"])
ev(2, 9, "andropov-dies", "yury-andropov-dies-1984", "Soviet leader Yury Andropov dies", "1984",
   "He is replaced by Konstantin Chernenko after 15 months in power.",
   "On {hd}, the Soviet leader Yury Andropov died, 15 months after succeeding Leonid Brezhnev. He was replaced by Konstantin Chernenko.",
   "1984: 'Soviet Premier Yury Andropov died 15 months after succeeding Leonid Brezhnev and was replaced by Konstantin Chernenko.'",
   ["europe", "asia"], ["cold-war"])

# ---------------------------------------------------------------- Feb 10
ev(2, 10, "powers-abel", "powers-abel-spy-exchange-1962", "U-2 pilot Francis Gary Powers is exchanged for a Soviet spy", "1962",
   "The Cold War foes swap Powers for Rudolf Abel.",
   "On {hd}, Francis Gary Powers, the American U-2 pilot shot down over the Soviet Union in 1960, was exchanged for the jailed Soviet spy Rudolf Abel.",
   "1962: 'Francis Gary Powers ... was exchanged for jailed Soviet informant Rudolf Abel.'",
   ["europe", "americas"], ["cold-war"], "A Cold War spy swap",
   "In 1962, U-2 pilot Francis Gary Powers was traded for the Soviet spy Rudolf Abel.",
   extra=[art("U-2 Incident", "https://www.britannica.com/event/U-2-Incident",
              "U-2 reconnaissance plane shot down May 1, 1960, near Sverdlovsk; Powers sentenced to 10 years' confinement; 'exchanged for the Soviet spy Rudolf Abel on February 10, 1962'.")])
ev(2, 10, "treaty-of-paris-1763", "treaty-of-paris-1763", "The Treaty of Paris ends the Seven Years' War", "1763",
   "France and Britain settle the conflicts behind the war.",
   "On {hd}, the Treaty of Paris was signed, ending the territorial conflicts between France and Britain that had caused the Seven Years' War.",
   "1763: 'The Treaty of Paris was signed, ending the territorial conflicts between France and Britain that had caused the Seven Years' War.'",
   ["europe", "americas"], ["early-modern"])
ev(2, 10, "victoria-albert", "queen-victoria-marries-albert-1840", "Queen Victoria marries Prince Albert", "1840",
   "The young queen weds her German cousin.",
   "On {hd}, Queen Victoria of Great Britain married Prince Albert.",
   "1840: 'Queen Victoria of Great Britain married Prince Albert.'",
   ["europe"], ["1800-1945"])
ev(2, 10, "brecht-born", "bertolt-brecht-born-1898", "Bertolt Brecht is born in Augsburg", "1898",
   "The German poet and playwright is born.",
   "On {hd}, the poet and playwright Bertolt Brecht was born in Augsburg, Germany.",
   "1898: 'Poet and playwright Bertolt Brecht ... was born in Augsburg, Germany.'",
   ["europe"], ["1800-1945"])
ev(2, 10, "mark-spitz-born", "mark-spitz-born-1950", "Mark Spitz is born", "1950",
   "The swimmer who will win seven gold medals at one Olympics is born.",
   "On {hd}, the American swimmer Mark Spitz was born. He became the first athlete to win seven gold medals at a single Olympic Games.",
   "1950: 'American swimmer Mark Spitz, born this day in 1950, became the first athlete to capture seven gold medals in a single Olympics.'",
   ["americas"], ["cold-war"])

# ---------------------------------------------------------------- Feb 11
ev(2, 11, "mandela-released", "nelson-mandela-released-1990", "Nelson Mandela is released from prison", "1990",
   "He walks free after 27 years and begins talks to end apartheid.",
   "On {hd}, Nelson Mandela was released after 27 years in prison. He went on to negotiate with President F.W. de Klerk the end of apartheid in South Africa.",
   "1990: 'After serving 27 years in prison, Nelson Mandela was released, and he subsequently began negotiations with President F.W. de Klerk that ended apartheid in South Africa.'",
   ["africa"], ["contemporary"], "Free after 27 years",
   "In 1990, Nelson Mandela walked out of prison after 27 years.")
ev(2, 11, "lateran-treaty", "lateran-treaty-signed-1929", "The Lateran Treaty creates Vatican City", "1929",
   "Italy recognizes papal sovereignty over an enclave in Rome.",
   "On {hd}, Benito Mussolini of Italy and Pietro Gasparri of the Vatican signed the Lateran Treaty, recognizing papal sovereignty over Vatican City, an enclave in Rome.",
   "1929: 'Benito Mussolini of Italy and Pietro Gasparri of the Vatican signed the Lateran Treaty, recognizing papal sovereignty over Vatican City, an enclave in Rome.'",
   ["europe"], ["1800-1945"])
ev(2, 11, "mubarak-resigns", "hosni-mubarak-steps-down-2011", "Hosni Mubarak steps down in Egypt", "2011",
   "Pro-democracy protests end nearly 30 years of rule.",
   "On {hd}, Egyptian President Hosni Mubarak stepped down after nearly 30 years in power, following the pro-democracy uprisings known as the Arab Spring.",
   "2011: 'Egyptian President Hosni Mubarak stepped down after nearly 30 years in power, following the pro-democracy uprisings known as the Arab Spring.'",
   ["middle-east", "africa"], ["contemporary"])
ev(2, 11, "edison-born", "thomas-edison-born-1847", "Thomas Edison is born in Ohio", "1847",
   "The inventor of the phonograph is born.",
   "On {hd}, the inventor Thomas Edison was born in Ohio.",
   "1847: 'Inventor Thomas Edison ... was born in Ohio.'",
   ["americas"], ["1800-1945"])
ev(2, 11, "thatcher-leader", "thatcher-elected-conservative-leader-1975", "Margaret Thatcher becomes leader of Britain's Conservative Party", "1975",
   "She replaces Edward Heath at the head of the party.",
   "On {hd}, Margaret Thatcher was elected leader of Britain's Conservative Party, replacing Edward Heath.",
   "1975: 'British politician Margaret Thatcher was elected leader of the Conservative Party, replacing Edward Heath.'",
   ["europe"], ["cold-war"])

# ---------------------------------------------------------------- Feb 12
ev(2, 12, "darwin-born", "charles-darwin-born-1809", "Charles Darwin is born", "1809",
   "The naturalist behind the theory of evolution by natural selection is born.",
   "On {hd}, Charles Darwin, who developed the theory of evolution by natural selection, was born in England. Abraham Lincoln was born on the same day.",
   "1809: 'Charles Darwin, who developed the theory of evolution by natural selection, was born in England.' The page also lists Abraham Lincoln's birth in 1809.",
   ["europe"], ["1800-1945"], "Born the same day as Lincoln",
   "In 1809, Charles Darwin was born in England, on the very same day as Abraham Lincoln.")
ev(2, 12, "lincoln-born", "abraham-lincoln-born-1809", "Abraham Lincoln is born", "1809",
   "The 16th US president, who preserved the Union, is born.",
   "On {hd}, Abraham Lincoln was born. As the 16th president of the United States (1861-65), he preserved the Union during the American Civil War.",
   "1809: 'Abraham Lincoln, the 16th U.S. president (1861-65), preserved the Union during the American Civil War.'",
   ["americas"], ["1800-1945"])
ev(2, 12, "puyi-abdicates", "puyi-abdicates-1912", "Puyi, China's last emperor, abdicates", "1912",
   "The abdication ends imperial rule in China.",
   "On {hd}, Puyi, the last emperor of China, abdicated at the end of the Chinese Revolution.",
   "1912: 'Puyi, the last emperor of China, abdicated at the end of the Chinese Revolution.'",
   ["asia"], ["1800-1945"])
ev(2, 12, "chile-independence", "chile-declares-independence-1818", "Chile formally declares independence", "1818",
   "The declaration comes a year after San Martin's revolutionary forces won a key victory.",
   "On {hd}, Chile formally declared its independence, exactly one year after revolutionary forces led by Jose de San Martin won a victory.",
   "1818: 'Chile formally declared independence, exactly one year after revolutionary forces led by Jose de San Martin won.'",
   ["americas"], ["revolutionary"])
ev(2, 12, "scream-stolen", "the-scream-stolen-1994", "Thieves steal The Scream from Oslo's National Gallery", "1994",
   "Edvard Munch's painting is taken in a break-in.",
   "On {hd}, thieves broke into the National Gallery in Oslo and stole a version of Edvard Munch's The Scream. The painting was recovered several months later.",
   "1994: 'Thieves broke into the National Gallery in Oslo on this day in 1994 and stole The Scream.'",
   ["europe"], ["contemporary"],
   extra=[art("The Scream", "https://www.britannica.com/topic/The-Scream-by-Munch",
              "by Norwegian artist Edvard Munch; versions of 1893, 1895 and 1910; a version stolen from Oslo's National Gallery in 1994 was 'recovered several months later'.")])

# ---------------------------------------------------------------- Feb 13
ev(2, 13, "last-peanuts", "last-peanuts-strip-2000", "The last Peanuts comic strip is published", "2000",
   "It appears hours after the death of creator Charles Schulz.",
   "On {hd}, the last Peanuts comic strip was published, just hours after the death of its creator, Charles Schulz.",
   "2000: 'the last Peanuts comic strip was published, just hours after the death of its creator, Charles Schulz.'",
   ["americas"], ["contemporary"], "Good grief: the final strip",
   "In 2000, the last Peanuts strip ran, just hours after its creator, Charles Schulz, died.")
ev(2, 13, "rudd-apology", "kevin-rudd-apology-2008", "Australia's prime minister apologizes to Aboriginal peoples", "2008",
   "Kevin Rudd apologizes for abuses under earlier governments.",
   "On {hd}, Australian Prime Minister Kevin Rudd apologized to Australia's Aboriginal peoples for abuses they had suffered under earlier administrations.",
   "2008: 'Kevin Rudd, the prime minister of Australia, apologized to the Australian Aboriginal peoples for abuses they had suffered under earlier administrations.'",
   ["oceania"], ["contemporary"])
ev(2, 13, "france-atomic-test", "france-first-atomic-bomb-1960", "France tests its first atomic bomb in the Sahara", "1960",
   "France becomes a nuclear power.",
   "On {hd}, France detonated its first atomic bomb, in the Sahara desert.",
   "1960: 'France detonated its first atomic bomb in the Sahara desert.'",
   ["europe", "africa"], ["cold-war"])
ev(2, 13, "wagner-dies", "richard-wagner-dies-1883", "Richard Wagner dies in Venice", "1883",
   "The German composer dies at 69.",
   "On {hd}, the German composer Richard Wagner died in Venice at age 69.",
   "1883: 'Richard Wagner ... died in Venice at age 69.'",
   ["europe"], ["1800-1945"])
ev(2, 13, "simenon-born", "georges-simenon-born-1903", "Georges Simenon is born in Liege", "1903",
   "The creator of Inspector Maigret is born.",
   "On {hd}, the Belgian-French novelist Georges Simenon, creator of the Parisian police inspector Jules Maigret, was born in Liege, Belgium.",
   "Famous Birthdays: 1903, Georges Simenon.",
   ["europe"], ["1800-1945"],
   extra=[art("Georges Simenon", "https://www.britannica.com/biography/Georges-Simenon",
              "'born Feb. 13, 1903, Liege, Belg.'; 'Belgian-French' novelist; created Jules Maigret, a Parisian police inspector.")])

# ---------------------------------------------------------------- Feb 14
ev(2, 14, "eniac", "eniac-announced-1946", "The ENIAC computer is announced to the public", "1946",
   "A 30-ton machine is unveiled as the first general-purpose electronic computer.",
   "On {hd}, the ENIAC (Electronic Numerical Integrator and Computer), the first general-purpose electronic computer, was announced to the public. Built at the University of Pennsylvania, it used about 18,000 vacuum tubes and weighed 30 tons.",
   "1946: 'The first general-purpose high-speed electronic digital computer, the ENIAC ... was demonstrated to the public.'",
   ["americas"], ["1800-1945", "cold-war"], "The 30-ton computer",
   "In 1946, the ENIAC, a 30-ton machine with 18,000 vacuum tubes, was announced to the world.",
   extra=[("Penn Engineering - ENIAC", "https://www.engineering.upenn.edu/about/history-heritage/eniac/",
           "Penn Engineering: 'Originally announced on February 14, 1946, the Electronic Numerical Integrator and Computer (ENIAC), was the first general-purpose electronic computer'; 30 tons; 18,000 vacuum tubes; built by John W. Mauchly and J. Presper Eckert, Jr.")])
ev(2, 14, "cook-killed", "james-cook-killed-1779", "Captain James Cook is killed in Hawaii", "1779",
   "The British explorer dies at Kealakekua Bay.",
   "On {hd}, the British explorer Captain James Cook was killed by Hawaiians at Kealakekua Bay.",
   "1779: 'Captain James Cook ... was killed at Kealakekua Bay by Hawaiians.'",
   ["oceania", "europe"], ["early-modern"])
ev(2, 14, "arizona-statehood", "arizona-statehood-1912", "Arizona becomes the 48th US state", "1912",
   "It is the last of the contiguous states to join the Union.",
   "On {hd}, Arizona became a US state, the last of the 48 contiguous states to be admitted to the Union.",
   "", ["americas"], ["1800-1945"],
   only=[art("Arizona", "https://www.britannica.com/place/Arizona-state",
             "statehood 'February 14, 1912, the last of the 48 conterminous United States to be admitted to the union'; capital Phoenix; the Grand Canyon of the Colorado River.")])
ev(2, 14, "youtube", "youtube-registered-2005", "YouTube is registered", "2005",
   "Three founders register a website for sharing videos.",
   "On {hd}, Steve Chen, Chad Hurley and Jawed Karim registered YouTube, a website for sharing videos.",
   "2005: 'Steve Chen, Chad Hurley, and Jawed Karim registered YouTube, a website for sharing videos.'",
   ["americas", "global"], ["contemporary"])
ev(2, 14, "zuma-resigns", "jacob-zuma-resigns-2018", "South African President Jacob Zuma resigns", "2018",
   "He steps down amid scandals and corruption allegations.",
   "On {hd}, South African President Jacob Zuma resigned amid scandals and corruption allegations.",
   "2018: 'Amid scandals and corruption allegations, South African President Jacob Zuma resigned.'",
   ["africa"], ["contemporary"])

# ---------------------------------------------------------------- Feb 15
ev(2, 15, "maple-leaf-flag", "canada-adopts-maple-leaf-flag-1965", "Canada adopts the Maple Leaf Flag", "1965",
   "A royal proclamation brings in Canada's national flag.",
   "On {hd}, Canada officially adopted the Maple Leaf Flag following a royal proclamation.",
   "1965: 'Canada officially adopted the Maple Leaf Flag following a royal proclamation.'",
   ["americas"], ["cold-war"], "A leaf becomes a nation's flag",
   "In 1965, Canada officially adopted the Maple Leaf Flag.")
ev(2, 15, "uss-maine", "uss-maine-sinks-1898", "The USS Maine explodes in Havana Harbor", "1898",
   "The sinking of the US battleship precedes the Spanish-American War.",
   "On {hd}, an explosion sank the US battleship Maine while it lay at anchor in Havana Harbor, Cuba.",
   "1898: 'On this day in 1898, an explosion sank the USS Maine while it lay at anchor in Havana Harbor.'",
   ["americas"], ["1800-1945"])
ev(2, 15, "afghanistan-withdrawal", "soviet-withdrawal-from-afghanistan-1989", "The last Soviet troops leave Afghanistan", "1989",
   "The occupation that began in 1979 comes to an end.",
   "On {hd}, the Soviet Union withdrew its last troops from Afghanistan, which it had occupied since 1979.",
   "1989: 'The Soviet Union withdrew its last troops from Afghanistan after occupying the country since 1979.'",
   ["asia"], ["cold-war"])
ev(2, 15, "susan-b-anthony", "susan-b-anthony-born-1820", "Susan B. Anthony is born", "1820",
   "The pioneer of women's suffrage is born in Massachusetts.",
   "On {hd}, Susan B. Anthony, a pioneer of the women's suffrage movement, was born in Adams, Massachusetts.",
   "1820: Susan B. Anthony, a pioneer for women's suffrage, was born in Adams, Massachusetts.",
   ["americas"], ["1800-1945"])
ev(2, 15, "galileo-born", "galileo-galilei-born-1564", "Galileo Galilei is born in Pisa", "1564",
   "The astronomer who will discover Jupiter's largest moons is born.",
   "On {hd}, Galileo Galilei was born in Pisa. He later discovered the four largest moons of Jupiter, now called the Galilean moons.",
   "", ["europe"], ["early-modern"], note=J,
   only=[art("Galileo", "https://www.britannica.com/biography/Galileo-Galilei",
             "born 'February 15, 1564, Pisa'; died January 8, 1642, Arcetri; discovered 'the four biggest moons of Jupiter (now called the Galilean moons)'; Dialogue Concerning the Two Chief World Systems; sentenced in 1633.")])

# ---------------------------------------------------------------- Feb 16
ev(2, 16, "kyoto-protocol", "kyoto-protocol-in-force-2005", "The Kyoto Protocol takes effect", "2005",
   "The climate treaty enters into force more than seven years after its adoption.",
   "On {hd}, the Kyoto Protocol, an agreement to reduce emissions of gases that contribute to global warming, went into effect, more than seven years after its adoption in Kyoto, Japan, in December 1997.",
   "2005: 'The Kyoto Protocol ... went into effect, eight years after its adoption.' (Date only; adopted December 1997, so just over seven years, per the article below.)",
   ["global", "asia"], ["contemporary"], "A climate treaty takes effect",
   "In 2005, the Kyoto Protocol to cut greenhouse gas emissions came into force.",
   extra=[art("Kyoto Protocol", "https://www.britannica.com/event/Kyoto-Protocol",
              "to reduce 'the emission of gases that contribute to global warming'; adopted December 1997 in Kyoto, Japan; in force February 2005; the first addition to the UN Framework Convention on Climate Change.")])
ev(2, 16, "lithuania-independence", "lithuania-declares-independence-1918", "Lithuania declares independence", "1918",
   "The 20-member Taryba proclaims an independent state.",
   "On {hd}, the 20-member Taryba, Lithuania's national council, proclaimed the country an independent state.",
   "1918: 'The 20-member Taryba ... proclaimed their country an independent state' (Lithuania).",
   ["europe"], ["1800-1945"])
ev(2, 16, "nylon-patent", "nylon-patented-1937", "Wallace Carothers patents nylon", "1937",
   "The DuPont chemist patents the first fully synthetic fiber.",
   "On {hd}, the DuPont chemist Wallace Hume Carothers patented nylon.",
   "1937: 'DuPont chemist Wallace Hume Carothers patented nylon.'",
   ["americas"], ["1800-1945"])
ev(2, 16, "castro-premier", "fidel-castro-becomes-premier-1959", "Fidel Castro becomes premier of Cuba", "1959",
   "The revolutionary leader takes charge of the government.",
   "On {hd}, Fidel Castro became premier of Cuba.",
   "1959: Fidel Castro became premier of Cuba.",
   ["americas"], ["cold-war"])
ev(2, 16, "grace-bedell", "lincoln-meets-grace-bedell-1861", "Lincoln meets the girl who suggested his beard", "1861",
   "On his way to Washington, he stops to see 12-year-old Grace Bedell.",
   "On {hd}, President-elect Abraham Lincoln stopped at Westfield, New York, and met 12-year-old Grace Bedell, who had written to suggest that he grow a beard.",
   "1861: 'Abraham Lincoln stopped at Westfield, New York' where he met 12-year-old Grace Bedell, who had suggested he grow a beard.",
   ["americas"], ["1800-1945"])

# ---------------------------------------------------------------- Feb 17
ev(2, 17, "kasparov-deep-blue", "kasparov-defeats-deep-blue-1996", "Garry Kasparov defeats IBM's Deep Blue", "1996",
   "The world chess champion wins his first match against the computer.",
   "On {hd}, world chess champion Garry Kasparov triumphed over IBM's Deep Blue computer.",
   "1996: 'World chess champion Garry Kasparov triumphed over IBM's Deep Blue computer.'",
   ["americas", "europe"], ["contemporary"], "Man beats machine, for now",
   "In 1996, world chess champion Garry Kasparov triumphed over IBM's Deep Blue computer.")
ev(2, 17, "kosovo-independence", "kosovo-declares-independence-2008", "Kosovo declares independence from Serbia", "2008",
   "Not every country recognizes the new republic.",
   "On {hd}, Kosovo declared its independence from Serbia, though a number of countries refused to recognize the new republic.",
   "2008: 'Kosovo declared its independence from Serbia, though a number of countries refused to recognize the new republic.'",
   ["europe"], ["contemporary"])
ev(2, 17, "hunley", "hunley-sinks-housatonic-1864", "The Hunley becomes the first submarine to sink an enemy ship", "1864",
   "The Confederate submarine attacks the USS Housatonic off Charleston.",
   "On {hd}, the Confederate submarine Hunley became the first submarine to sink an enemy ship when it attacked the USS Housatonic off Charleston, South Carolina.",
   "1864: 'The Confederate Hunley became the first submarine to sink an enemy ship when it successfully attacked the USS Housatonic in the waters off Charleston, South Carolina.'",
   ["americas"], ["1800-1945"])
ev(2, 17, "madama-butterfly", "madama-butterfly-premieres-1904", "Puccini's Madama Butterfly premieres at La Scala", "1904",
   "The opera opens in Milan and becomes one of the most often performed.",
   "On {hd}, Giacomo Puccini's Madama Butterfly premiered at La Scala in Milan. It became one of the most frequently performed operas.",
   "1904: 'Giacomo Puccini's Madama Butterfly premiered at La Scala in Milan, and it became one the most frequently performed operas.'",
   ["europe"], ["1800-1945"])
ev(2, 17, "jefferson-house", "house-elects-jefferson-1801", "The House of Representatives elects Thomas Jefferson president", "1801",
   "A tie in the electoral college is broken by the House.",
   "On {hd}, following a tie in the electoral college, the US House of Representatives elected Thomas Jefferson president.",
   "1801: 'Following a tie in the electoral college, the U.S. House of Representatives elected Thomas Jefferson president.'",
   ["americas"], ["revolutionary"])

# ---------------------------------------------------------------- Feb 18
ev(2, 18, "pluto-discovered", "pluto-discovered-1930", "Clyde Tombaugh discovers Pluto", "1930",
   "A 24-year-old with no formal training in astronomy finds a new world.",
   "On {hd}, Clyde Tombaugh, a 24-year-old American with no formal training in astronomy, discovered the dwarf planet Pluto.",
   "1930: 'Clyde Tombaugh, a 24-year-old American with no formal training in astronomy, discovered the dwarf planet Pluto.'",
   ["americas"], ["1800-1945"], "A self-taught astronomer finds Pluto",
   "In 1930, Clyde Tombaugh, 24 and with no formal astronomy training, discovered Pluto.")
ev(2, 18, "luther-dies", "martin-luther-dies-1546", "Martin Luther dies in Eisleben", "1546",
   "The leader of the Protestant Reformation dies at 62.",
   "On {hd}, Martin Luther, leader of the Protestant Reformation, died at age 62 in Eisleben, Saxony.",
   "1546: 'Martin Luther, leader of the Protestant Reformation, died at age 62 in Eisleben, Saxony.'",
   ["europe"], ["early-modern"], note=J)
ev(2, 18, "shani-davis", "shani-davis-olympic-gold-2006", "Shani Davis wins Olympic gold in speed skating", "2006",
   "He is the first Black athlete to win an individual Winter Olympics gold medal.",
   "On {hd}, the American speed skater Shani Davis won the men's 1,000-meter long-track final, becoming the first Black athlete to win an individual Winter Olympics gold medal.",
   "2006: 'American speed skater Shani Davis became the first Black athlete to win an individual Winter Olympics gold medal when he placed first in the men's 1,000-meter long-track final.'",
   ["americas", "europe"], ["contemporary"])
ev(2, 18, "yoko-ono-born", "yoko-ono-born-1933", "Yoko Ono is born", "1933",
   "The Japanese artist and musician is born.",
   "On {hd}, the artist and musician Yoko Ono was born.",
   "1933: 'Artist and musician Yoko Ono ... was born.'",
   ["asia"], ["1800-1945"])
ev(2, 18, "ramakrishna-born", "ramakrishna-born-1836", "Ramakrishna is born in Bengal", "1836",
   "The Hindu mystic and priest is born.",
   "On {hd}, the Hindu mystic and priest Ramakrishna was born in Hooghly (now Hugli), Bengal, India.",
   "1836: 'Hindu mystic and priest Ramakrishna was born in Hoogly (now Hugli), Bengal state, India.'",
   ["asia"], ["1800-1945"])

# ---------------------------------------------------------------- Feb 19
ev(2, 19, "copernicus-born", "nicolaus-copernicus-born-1473", "Nicolaus Copernicus is born in Torun", "1473",
   "The astronomer who put the Sun at the center of the planets is born.",
   "On {hd}, the astronomer Nicolaus Copernicus was born in Torun, Poland. He argued that Earth is a planet that orbits the Sun.",
   "1473: 'Astronomer Nicolaus Copernicus, born this day in 1473, reintroduced the heliocentric system.'",
   ["europe"], ["medieval", "early-modern"], "The man who moved the Earth",
   "In 1473, Nicolaus Copernicus, who argued that Earth orbits the Sun, was born.", note=J,
   extra=[art("Nicolaus Copernicus", "https://www.britannica.com/biography/Nicolaus-Copernicus",
              "born 'February 19, 1473, Torun, Royal Prussia, Poland'; died May 24, 1543; De revolutionibus orbium coelestium (1543); Earth 'is a planet which, besides orbiting the Sun annually, also turns once daily on its own axis'.")])
ev(2, 19, "phonograph-patent", "edison-patents-phonograph-1878", "Thomas Edison patents the phonograph", "1878",
   "A needle's vibrations are used to record and play back sound.",
   "On {hd}, the American inventor Thomas Edison patented the phonograph, a device that reproduced sound through a needle's vibration.",
   "1878: 'American inventor Thomas Edison patented the phonograph, a device used to reproduce sound through a needle's vibration.'",
   ["americas"], ["1800-1945"])
ev(2, 19, "executive-order-9066", "executive-order-9066-signed-1942", "Roosevelt signs Executive Order 9066", "1942",
   "The order forces Japanese Americans into detention camps.",
   "On {hd}, US President Franklin D. Roosevelt signed Executive Order 9066, which forced Japanese Americans into detention camps during World War II.",
   "1942: 'U.S. President Franklin D. Roosevelt signed Executive Order 9066, which forced Japanese Americans into detention camps during World War II.'",
   ["americas"], ["1800-1945"])
ev(2, 19, "deng-dies", "deng-xiaoping-dies-1997", "Deng Xiaoping dies", "1997",
   "China's paramount leader dies at 92.",
   "On {hd}, the Chinese leader Deng Xiaoping died at age 92.",
   "1997: 'Deng Xiaoping died at age 92.'",
   ["asia"], ["contemporary"])
ev(2, 19, "castro-resigns", "fidel-castro-resigns-2008", "Fidel Castro resigns as president of Cuba", "2008",
   "He steps down after 19 months out of public view.",
   "On {hd}, Fidel Castro, after 19 months out of the public eye, formally resigned as president of Cuba.",
   "2008: 'Fidel Castro, after being out of the public eye for 19 months, formally resigned as president of Cuba.'",
   ["americas"], ["contemporary"])

# ---------------------------------------------------------------- Feb 20
ev(2, 20, "glenn-orbits", "john-glenn-orbits-earth-1962", "John Glenn becomes the first American to orbit Earth", "1962",
   "He circles the planet three times.",
   "On {hd}, John H. Glenn, Jr., became the first American to orbit Earth, circling it three times.",
   "1962: 'John H. Glenn, Jr. became the first American to orbit Earth, doing so three times.'",
   ["americas"], ["space-age", "cold-war"], "Three laps around the planet",
   "In 1962, John Glenn became the first American to orbit Earth, circling it three times.")
ev(2, 20, "paricutin", "paricutin-begins-erupting-1943", "The volcano Paricutin begins erupting in Mexico", "1943",
   "A new volcano bursts out in Michoacan.",
   "On {hd}, the volcano Paricutin, in Michoacan state, Mexico, began erupting.",
   "1943: 'The volcano Paricutin in Michoacan state, Mexico, began erupting.'",
   ["americas"], ["1800-1945"])
ev(2, 20, "futurism", "futurism-named-in-le-figaro-1909", "Marinetti names Futurism in Le Figaro", "1909",
   "An Italian writer launches an art movement in a Paris newspaper.",
   "On {hd}, the Italian writer Filippo Tommaso Marinetti coined the term Futurism in the Paris newspaper Le Figaro.",
   "1909: 'Italian author Filippo Tommaso Marinetti coined the term Futurism in the Parisian newspaper Le Figaro.'",
   ["europe"], ["1800-1945"])
ev(2, 20, "ansel-adams-born", "ansel-adams-born-1902", "Ansel Adams is born", "1902",
   "The photographer of the American West is born.",
   "On {hd}, the American photographer Ansel Adams was born.",
   "Births: 1902, Ansel Adams.",
   ["americas"], ["1800-1945"])
ev(2, 20, "boltzmann-born", "ludwig-boltzmann-born-1844", "Ludwig Boltzmann is born", "1844",
   "The Austrian physicist is born.",
   "On {hd}, the Austrian physicist Ludwig Boltzmann was born.",
   "Births: 1844, Ludwig Boltzmann.",
   ["europe"], ["1800-1945"])

# ---------------------------------------------------------------- Feb 21
ev(2, 21, "nixon-china", "nixon-visits-china-1972", "Richard Nixon becomes the first sitting US president to visit China", "1972",
   "The visit opens a new chapter in Cold War diplomacy.",
   "On {hd}, Richard Nixon became the first sitting US president to visit China.",
   "1972: 'U.S. Pres. Richard Nixon became the first sitting U.S. president to visit China.'",
   ["asia", "americas"], ["cold-war"], "A trip that reshaped diplomacy",
   "In 1972, Richard Nixon became the first sitting US president to visit China.")
ev(2, 21, "verdun", "battle-of-verdun-begins-1916", "The Battle of Verdun begins", "1916",
   "One of the most devastating battles of World War I opens.",
   "On {hd}, the Battle of Verdun, one of the most devastating engagements of World War I, began.",
   "1916: 'The Battle of Verdun, one of the most devastating engagements of World War I, began.'",
   ["europe"], ["1800-1945"])
ev(2, 21, "malcolm-x", "malcolm-x-assassinated-1965", "Malcolm X is assassinated", "1965",
   "The Black leader is killed while giving a speech in Manhattan.",
   "On {hd}, Malcolm X was assassinated while giving a speech in Manhattan.",
   "1965: 'Black revolutionary leader Malcolm X was assassinated while giving a speech in Manhattan.'",
   ["americas"], ["cold-war"])
ev(2, 21, "new-yorker", "new-yorker-first-issue-1925", "The New Yorker begins publication", "1925",
   "Harold Ross launches the weekly magazine.",
   "On {hd}, the American weekly magazine The New Yorker began publication under its editor Harold W. Ross.",
   "1925: 'The American weekly magazine The New Yorker began publication under Harold W. Ross.'",
   ["americas"], ["1800-1945"])
ev(2, 21, "language-movement", "bengali-language-movement-1952", "Police fire on Bangla language protesters in Dhaka", "1952",
   "The killings make February 21 a day to honor mother languages.",
   "On {hd}, students and activists protested at the University of Dhaka for recognition of their language, Bangla (Bengali). Police opened fire, killing seven people. UNESCO later chose February 21 as International Mother Language Day.",
   "", ["asia"], ["decolonization"],
   only=[("University of Cambridge - Celebrating language diversity", "https://www.cam.ac.uk/stories/celebrating-language-diversity",
          "Cambridge: 'On the 21st of February, 1952, a group of students and political activists staged a protest at the University of Dhaka to protect their native language, Bangla (or Bengali)'; 'Police opened fire, killing 7 people in total, including 4 of the students'; Pakistan recognized Bangla in 1956."),
         ("UNESCO - International Mother Language Day", "https://www.unesco.org/en/days/mother-language",
          "UNESCO: International Mother Language Day was Bangladesh's initiative, approved at the 1999 UNESCO General Conference, and observed since 2000. (The page does not give the 1952 background.)")])

# ---------------------------------------------------------------- Feb 22
ev(2, 22, "miracle-on-ice", "miracle-on-ice-1980", "The US hockey team beats the Soviet Union in the Miracle on Ice", "1980",
   "An Olympic upset becomes one of sport's most famous games.",
   "On {hd}, the US men's ice hockey team defeated the Soviet Union's team at the Winter Olympics in Lake Placid, New York, in the game known as the Miracle on Ice.",
   "1980: 'U.S. men's hockey team defeats Soviet Union team in 'Miracle on Ice''.",
   ["americas", "europe"], ["cold-war"], "The Miracle on Ice",
   "In 1980, the US men's hockey team stunned the Soviet Union in the Miracle on Ice.",
   extra=[art("Miracle on Ice", "https://www.britannica.com/event/Miracle-on-Ice",
              "February 22, 1980, at the Winter Olympics in Lake Placid, New York; United States 4, Soviet Union 3; coach Herb Brooks, a roster mostly of college players; the US beat Finland on February 24 to win gold.")])
ev(2, 22, "washington-born", "george-washington-born-1732", "George Washington is born in Virginia", "1732",
   "The future first US president is born in Westmoreland County.",
   "On {hd}, George Washington, commander of the colonial armies in the American Revolution and the first US president, was born in Westmoreland County, Virginia.",
   "1732: George Washington born; 'general and commander in chief of the colonial armies in the American Revolution (1775-83) and first president'.",
   ["americas"], ["early-modern", "revolutionary"],
   note="Gregorian date. Under the Julian (Old Style) calendar then used in the British colonies, he was born on February 11, 1731.",
   extra=[art("George Washington", "https://www.britannica.com/biography/George-Washington",
              "born 'February 22 [February 11, Old Style], 1732', Westmoreland county, Virginia.")])
ev(2, 22, "white-rose", "white-rose-members-executed-1943", "White Rose resisters are executed in Munich", "1943",
   "Hans and Sophie Scholl and Christoph Probst are put to death for opposing the Nazis.",
   "On {hd}, three members of the White Rose, an anti-Nazi student group at the University of Munich, were beheaded in Munich for distributing leaflets against the regime.",
   "", ["europe"], ["1800-1945"],
   only=[art("White Rose", "https://www.britannica.com/topic/White-Rose",
             "Hans Scholl, Sophie Scholl and Christoph Probst 'were beheaded on February 22, 1943'; the group was founded by students at the University of Munich and distributed anti-Nazi leaflets. (The Feb 22 day page dates this to 1942, which conflicts with the article; the article's 1943 is used.)")])
ev(2, 22, "daytona-500", "first-daytona-500-1959", "The first Daytona 500 is run", "1959",
   "Lee Petty wins NASCAR's new race.",
   "On {hd}, NASCAR held the first Daytona 500, which was won by Lee Petty.",
   "1959: 'NASCAR held the first Daytona 500, which was won by Lee Petty.'",
   ["americas"], ["cold-war"])
ev(2, 22, "christchurch-earthquake", "christchurch-earthquake-2011", "An earthquake devastates Christchurch, New Zealand", "2011",
   "A magnitude 6.3 aftershock strikes the city.",
   "On {hd}, Christchurch, New Zealand, and its surrounding area were struck by a massively destructive magnitude 6.3 aftershock.",
   "2011: 'Christchurch, New Zealand, and its surrounding area were struck by a massively destructive 6.3 magnitude aftershock.'",
   ["oceania"], ["contemporary"])

# ---------------------------------------------------------------- Feb 23
ev(2, 23, "iwo-jima-flag", "iwo-jima-flag-raising-1945", "US servicemen raise the flag on Iwo Jima", "1945",
   "Six men raise the American flag on Mount Suribachi.",
   "On {hd}, during World War II, six US servicemen raised the American flag over Mount Suribachi on the island of Iwo Jima.",
   "1945: 'Six U.S. servicemen raised the American flag over Mount Suribachi on the island of Iwo Jima during World War II.'",
   ["asia", "americas"], ["1800-1945"], "The flag over Suribachi",
   "In 1945, six US servicemen raised the American flag on Mount Suribachi, Iwo Jima.")
ev(2, 23, "alamo-siege", "siege-of-the-alamo-begins-1836", "The siege of the Alamo begins", "1836",
   "Santa Anna's army surrounds the Texan defenders.",
   "On {hd}, during the Texas Revolution, Mexican General Antonio Lopez de Santa Anna began a siege of the Alamo, which was captured after 13 days.",
   "1836: 'During the Texas Revolution, Mexican General Antonio Lopez de Santa Anna began a siege of the Alamo, which was captured after 13 days.'",
   ["americas"], ["1800-1945"])
ev(2, 23, "handel-born", "george-frideric-handel-born-1685", "George Frideric Handel is born in Halle", "1685",
   "The composer of Messiah is born.",
   "On {hd}, the composer George Frideric Handel, a leading figure of late Baroque music, was born in Halle, Germany.",
   "1685: 'Composer George Frideric Handel, a leading figure of late Baroque music, was born in Germany.'",
   ["europe"], ["early-modern"], note=J,
   extra=[art("George Frideric Handel", "https://www.britannica.com/biography/George-Frideric-Handel",
              "born 'February 23, 1685, Halle, Brandenburg [Germany]'; died April 14, 1759, London; Messiah first performed in Dublin on April 13, 1742; Water Music (1717).")])
ev(2, 23, "du-bois-born", "web-du-bois-born-1868", "W.E.B. Du Bois is born", "1868",
   "The scholar and civil rights leader is born in Massachusetts.",
   "On {hd}, W.E.B. Du Bois, one of the most important African American protest leaders of the early 20th century, was born in Great Barrington, Massachusetts.",
   "1868: 'Born this day in 1868, W.E.B. Du Bois was one of the most important African American protest leaders during the first half of the 20th century.'",
   ["americas"], ["1800-1945"],
   extra=[art("W.E.B. Du Bois", "https://www.britannica.com/biography/W-E-B-Du-Bois",
              "born 'February 23, 1868, Great Barrington, Massachusetts'; The Souls of Black Folk (1903); helped create the NAACP in 1909 and edited The Crisis (1910-34); Harvard Ph.D. in 1895.")])
ev(2, 23, "boston-charter", "boston-granted-city-charter-1822", "Boston is granted a city charter", "1822",
   "The Massachusetts town officially becomes a city.",
   "On {hd}, Boston was granted a charter to become a city.",
   "1822: 'Boston ... was granted a charter to become a city.'",
   ["americas"], ["1800-1945"])

# ---------------------------------------------------------------- Feb 24
ev(2, 24, "gregorian-bull", "gregorian-calendar-papal-bull-1582", "Pope Gregory XIII decrees the Gregorian calendar", "1582",
   "A papal bull orders ten days dropped from the year.",
   "On {hd}, Pope Gregory XIII issued a papal bull reforming the calendar: Thursday, October 4, 1582, would be followed by Friday, October 15.",
   "1582: 'Pope Gregory XIII issued a papal bull that would erase ten days of the year - Thursday, October 4, would be followed by Friday, October 15.'",
   ["europe", "global"], ["early-modern"], "Ten days vanish from the calendar",
   "In 1582, Pope Gregory XIII decreed that October 4 would be followed by October 15.",
   note="Julian calendar date; the bull introduced the Gregorian calendar later that year.")
ev(2, 24, "marbury", "marbury-v-madison-1803", "The Supreme Court decides Marbury v. Madison", "1803",
   "The court strikes down an act of Congress as unconstitutional.",
   "On {hd}, in Marbury v. Madison, the US Supreme Court declared an act of Congress unconstitutional, establishing the principle of judicial review.",
   "1803: 'In Marbury v. Madison, the U.S. Supreme Court declared an act of Congress unconstitutional.'",
   ["americas"], ["revolutionary"])
ev(2, 24, "ukraine-invasion", "russia-invades-ukraine-2022", "Russia launches a full-scale invasion of Ukraine", "2022",
   "The largest war in Europe in decades begins.",
   "On {hd}, Russia launched a full-scale invasion of Ukraine.",
   "2022: 'Russia launched a full-scale invasion of Ukraine.'",
   ["europe"], ["contemporary"])
ev(2, 24, "johnson-impeached", "andrew-johnson-impeached-1868", "The House impeaches President Andrew Johnson", "1868",
   "The vote is 126 to 47.",
   "On {hd}, the US House of Representatives voted 126 to 47 to impeach President Andrew Johnson.",
   "1868: 'The U.S. House of Representatives voted 126-47 to impeach President Andrew Johnson.'",
   ["americas"], ["1800-1945"])
ev(2, 24, "louis-philippe", "louis-philippe-abdicates-1848", "King Louis-Philippe abdicates as revolution reaches France", "1848",
   "The Revolutions of 1848 topple the French monarchy.",
   "On {hd}, the anti-monarchical Revolutions of 1848 reached France and led to the abdication of King Louis-Philippe.",
   "1848: 'The anti-monarchical Revolutions of 1848 reached France ... led to the abdication of King Louis-Philippe.'",
   ["europe"], ["1800-1945"])

# ---------------------------------------------------------------- Feb 25
ev(2, 25, "clay-liston", "cassius-clay-defeats-liston-1964", "Cassius Clay defeats Sonny Liston for the heavyweight title", "1964",
   "The young boxer, soon to be Muhammad Ali, becomes world champion.",
   "On {hd}, Cassius Clay, later known as Muhammad Ali, defeated reigning world heavyweight champion Sonny Liston.",
   "1964: 'Muhammad Ali (known at the time as Cassius Clay) defeated reigning world champion Sonny Liston in the boxing ring.'",
   ["americas"], ["cold-war"], "The upset that made a legend",
   "In 1964, Cassius Clay, later Muhammad Ali, beat Sonny Liston for the heavyweight title.")
ev(2, 25, "secret-speech", "khrushchev-secret-speech-1956", "Khrushchev denounces Stalin in a secret speech", "1956",
   "The Soviet leader attacks his late predecessor.",
   "On {hd}, the Twentieth Congress of the Communist Party of the Soviet Union closed after First Secretary Nikita Khrushchev delivered a secret speech denouncing the late Joseph Stalin.",
   "1956: 'The Twentieth Congress of the Communist Party of the Soviet Union came to a close after First Secretary Nikita S. Khrushchev delivered a secret speech denouncing the late Soviet leader Joseph Stalin.'",
   ["europe", "asia"], ["cold-war"])
ev(2, 25, "revels", "hiram-revels-sworn-in-1870", "Hiram Revels becomes the first African American in Congress", "1870",
   "He is sworn in to the US Senate.",
   "On {hd}, Hiram Rhodes Revels was sworn in to the US Senate, becoming the first African American to serve in Congress.",
   "1870: 'Hiram Rhodes Revels was sworn in to the U.S. Senate, becoming the first African American to serve in Congress.'",
   ["americas"], ["1800-1945"])
ev(2, 25, "marcos-flees", "marcos-flees-philippines-1986", "Ferdinand Marcos flees the Philippines", "1986",
   "The president leaves for Hawaii under US pressure.",
   "On {hd}, Philippine President Ferdinand Marcos, under pressure from the United States, fled the country for Hawaii.",
   "1986: 'Philippine President Ferdinand Marcos, under pressure from the United States, fled the country for Hawaii.'",
   ["asia"], ["cold-war"])
ev(2, 25, "harrison-born", "george-harrison-born-1943", "George Harrison is born", "1943",
   "The Beatles' lead guitarist is born.",
   "On {hd}, the British musician George Harrison, lead guitarist of the Beatles, was born.",
   "1943: 'British musician George Harrison - lead guitarist of the Beatles, one of the most influential bands in rock and roll - was born.'",
   ["europe"], ["1800-1945"])

# ---------------------------------------------------------------- Feb 26
ev(2, 26, "napoleon-elba", "napoleon-escapes-elba-1815", "Napoleon escapes from exile on Elba", "1815",
   "The deposed emperor slips away to begin the Hundred Days.",
   "On {hd}, Napoleon, banished to the island of Elba by a coalition of European powers the year before, escaped from exile.",
   "1815: 'Napoleon, who had been banished to the island of Elba by a coalition of European powers the previous year, escaped exile.'",
   ["europe"], ["revolutionary", "1800-1945"], "An emperor slips out of exile",
   "In 1815, Napoleon escaped from exile on the island of Elba.")
ev(2, 26, "hugo-born", "victor-hugo-born-1802", "Victor Hugo is born in Besancon", "1802",
   "The author of Les Miserables is born.",
   "On {hd}, the French writer Victor Hugo, author of The Hunchback of Notre-Dame and Les Miserables, was born in Besancon, France.",
   "", ["europe"], ["1800-1945"],
   only=[art("Victor Hugo", "https://www.britannica.com/biography/Victor-Hugo",
             "born 'February 26, 1802, Besancon, France'; died May 22, 1885, Paris; Notre-Dame de Paris (1831; The Hunchback of Notre-Dame), hunchback Quasimodo; Les Miserables (1862).")])
ev(2, 26, "grand-teton", "grand-teton-national-park-1929", "Grand Teton National Park is established", "1929",
   "Wyoming's mountain park joins the national park system.",
   "On {hd}, Grand Teton National Park was established in Wyoming. In 1950 it was expanded to include most of Jackson Hole National Monument.",
   "1929: 'Grand Teton National Park was established in Wyoming; in 1950 it was expanded to include most of Jackson Hole National Monument.'",
   ["americas"], ["1800-1945"])
ev(2, 26, "johnny-cash-born", "johnny-cash-born-1932", "Johnny Cash is born", "1932",
   "The 'Man in Black' of country music is born.",
   "On {hd}, the singer-songwriter Johnny Cash, known to fans as the Man in Black, was born. He broadened the scope of American country music.",
   "1932: 'Born this day in 1932, singer-songwriter Johnny Cash, known to his fans as the 'Man in Black' ... broadened the scope of American country and western music.'",
   ["americas"], ["1800-1945"])
ev(2, 26, "wtc-bombing", "world-trade-center-bombing-1993", "A truck bomb explodes beneath the World Trade Center", "1993",
   "The attack in New York kills six people.",
   "On {hd}, a truck bomb exploded in the garage of the World Trade Center in New York City, killing six people and injuring more than 1,000.",
   "1993: 'A truck bomb exploded in the garage of the World Trade Center in New York City, killing six people and injuring more than 1,000.'",
   ["americas"], ["contemporary"])

# ---------------------------------------------------------------- Feb 27
ev(2, 27, "solar-radio", "hey-detects-solar-radio-waves-1942", "Radar operators pick up radio waves from the Sun", "1942",
   "Physicist James Stanley Hey traces wartime radar jamming to the Sun.",
   "On {hd}, the British physicist James Stanley Hey noticed strange interference on British radar equipment, which he traced to radio waves from the Sun, a founding discovery of radio astronomy.",
   "1942 (featured): 'British physicist discovers solar radio waves' - 'physicist James Stanley Hey noticed strange interference while operating British radar equipment during World War II.'",
   ["europe"], ["1800-1945"], "Radar hears the Sun",
   "In 1942, James Stanley Hey discovered that the Sun gives off radio waves.")
ev(2, 27, "reichstag-fire", "reichstag-fire-1933", "The Reichstag building burns in Berlin", "1933",
   "Fire guts the home of Germany's parliament.",
   "On {hd}, the Reichstag, the building of Germany's parliament in Berlin, caught fire.",
   "1933: 'In Berlin the Reichstag (parliament) building caught fire.'",
   ["europe"], ["1800-1945"])
ev(2, 27, "chile-earthquake", "chile-earthquake-2010", "A magnitude 8.8 earthquake strikes Chile", "2010",
   "The quake and tsunami kill more than 500 people.",
   "On {hd}, a magnitude 8.8 earthquake struck Chile, triggering a tsunami that devastated coastal areas. It was the region's most powerful earthquake since 1960 and caused more than 500 deaths.",
   "2010: 'A magnitude-8.8 earthquake struck Chile, causing widespread damage and triggering a tsunami that devastated coastal areas; it was the most powerful earthquake to strike the region since 1960 and caused more than 500 deaths.'",
   ["americas"], ["contemporary"])
ev(2, 27, "wounded-knee", "wounded-knee-occupation-1973", "The American Indian Movement occupies Wounded Knee", "1973",
   "About 200 activists take over the South Dakota hamlet.",
   "On {hd}, about 200 members of the American Indian Movement occupied Wounded Knee, a hamlet on a reservation in South Dakota.",
   "1973: 'Two hundred members of the American Indian Movement (AIM) ... occupied the reservation hamlet of Wounded Knee, South Dakota.'",
   ["americas"], ["cold-war"])
ev(2, 27, "steinbeck-born", "john-steinbeck-born-1902", "John Steinbeck is born in Salinas", "1902",
   "The author of The Grapes of Wrath is born in California.",
   "On {hd}, the writer John Steinbeck, author of The Grapes of Wrath and winner of the 1962 Nobel Prize for Literature, was born in Salinas, California.",
   "1902: 'Writer John Steinbeck was born.'",
   ["americas"], ["1800-1945"],
   extra=[art("John Steinbeck", "https://www.britannica.com/biography/John-Steinbeck",
              "born 'February 27, 1902, Salinas, California'; Of Mice and Men (1937); The Grapes of Wrath (1939; Pulitzer Prize 1940); East of Eden (1952); Nobel Prize for Literature 1962.")])

# ---------------------------------------------------------------- Feb 28
ev(2, 28, "mash-finale", "mash-finale-1983", "M*A*S*H ends with a record-setting finale", "1983",
   "More than 106 million viewers watch the last episode.",
   "On {hd}, more than 106 million people watched 'Goodbye, Farewell and Amen', the final episode of the television series M*A*S*H. It remains the most-watched episode of scripted television.",
   "1983: 'the U.S. tuned in to watch the finale of the long-running television series M*A*S*H'.",
   ["americas"], ["cold-war"], "Goodbye, Farewell and Amen",
   "In 1983, more than 106 million people watched the final episode of M*A*S*H.",
   extra=[(BR + "Today in History: M*A*S*H Series Finale", "https://www.britannica.com/today-in-history/February-28-MASH-Series-Finale",
           "Britannica: set in South Korea; ran 1972-83, 'more than three times longer than the war it depicted'; finale 'Goodbye, Farewell and Amen'; 'More than 106 million people watched'; 'remains the most-watched episode of scripted television in history'.")])
ev(2, 28, "benedict-resigns", "benedict-xvi-resigns-2013", "Pope Benedict XVI resigns", "2013",
   "He is the first pope to step down since 1415.",
   "On {hd}, Benedict XVI became the first pope to resign since Gregory XII in 1415.",
   "2013: 'Benedict XVI became the first pope to resign since Gregory XII in 1415.'",
   ["europe", "global"], ["contemporary"])
ev(2, 28, "egypt-independence", "egypt-declared-independent-1922", "Britain declares Egypt independent", "1922",
   "The British protectorate over Egypt ends, though Britain keeps certain powers.",
   "On {hd}, Britain unilaterally declared Egypt independent, ending its protectorate while reserving certain powers. Fuad I became king of Egypt.",
   "1922: 'Egypt declared its independence, ending the British protectorate.' (Date only; the framing follows the article below.)",
   ["africa", "middle-east"], ["1800-1945"],
   extra=[art("Egypt: The Wafd and independence", "https://www.britannica.com/place/Egypt/The-Wafd-and-independence",
              "'The British declared Egypt independent in 1922, albeit reserving certain powers, and Fu'ad I became king.'")])
ev(2, 28, "taiwan-228", "taiwan-228-incident-1947", "The 228 Incident spreads across Taiwan", "1947",
   "Protests against the ruling Kuomintang sweep the island.",
   "On {hd}, protests against the ruling Kuomintang (KMT) spread across Taiwan, in what became known as the 228 Incident.",
   "1947: 'protests against the ruling Kuomintang (KMT) spread across Taiwan. Known as the 228 Incident'.",
   ["asia"], ["cold-war"])
ev(2, 28, "palme", "olof-palme-assassinated-1986", "Swedish Prime Minister Olof Palme is assassinated", "1986",
   "The internationally prominent leader is shot in Stockholm.",
   "On {hd}, Olof Palme, the internationally prominent prime minister of Sweden, was assassinated.",
   "1986: 'Olof Palme, the internationally prominent prime minister of Sweden ... was assassinated.'",
   ["europe"], ["cold-war"])

# ---------------------------------------------------------------- Feb 29
ev(2, 29, "hattie-mcdaniel", "hattie-mcdaniel-wins-oscar-1940", "Hattie McDaniel becomes the first African American to win an Oscar", "1940",
   "She is honored for her role in Gone with the Wind.",
   "On {hd}, Hattie McDaniel became the first African American to win an Academy Award, for best supporting actress in Gone with the Wind (1939).",
   "1940: 'For her performance in Gone with the Wind (1939), Hattie McDaniel became the first African American to win an Academy Award, for best supporting actress.'",
   ["americas"], ["1800-1945"], "A leap-day first at the Oscars",
   "In 1940, Hattie McDaniel became the first African American to win an Academy Award.")
ev(2, 29, "rossini-born", "gioachino-rossini-born-1792", "Gioachino Rossini is born in Pesaro", "1792",
   "The composer of The Barber of Seville is born on a leap day.",
   "On {hd}, the Italian composer Gioachino Rossini, best known for comic operas such as The Barber of Seville, was born in Pesaro.",
   "1792: 'Italian composer Gioachino Rossini, born this day in 1792, wrote numerous works but was perhaps best known for his comic operas.'",
   ["europe"], ["revolutionary"],
   extra=[art("Gioachino Rossini", "https://www.britannica.com/biography/Gioachino-Rossini",
              "born 'February 29, 1792, Pesaro, Papal States'; died November 13, 1868; The Barber of Seville (1816); William Tell (1829).")])
ev(2, 29, "desai-born", "morarji-desai-born-1896", "Morarji Desai is born", "1896",
   "India's first prime minister from outside the Congress party is born.",
   "On {hd}, Morarji Desai was born. As prime minister of India (1977-79), he was the first leader of independent India not to represent the Indian National Congress party.",
   "1896: 'Indian politician Morarji Desai - who, as prime minister of India (1977-79), was the first leader of sovereign India not to represent the long-ruling Indian National Congress party - was born.'",
   ["asia"], ["1800-1945"])
ev(2, 29, "howe-800", "gordie-howe-800th-goal-1980", "Gordie Howe scores his 800th NHL goal", "1980",
   "At 51, he is the first player to reach the mark.",
   "On {hd}, at the age of 51, the Canadian hockey player Gordie Howe became the first to score 800 goals in the National Hockey League.",
   "1980: 'At the age of 51, Canadian hockey player Gordie Howe becomes the first to score 800 goals in the NHL.'",
   ["americas"], ["cold-war"])
ev(2, 29, "return-of-the-king", "return-of-the-king-wins-11-oscars-2004", "The Return of the King wins 11 Academy Awards", "2004",
   "The final Lord of the Rings film sweeps the Oscars.",
   "On {hd}, The Return of the King, the last film in the adaptation of J.R.R. Tolkien's The Lord of the Rings, received 11 Academy Awards.",
   "2004: 'The Return of the King - the last installment in the film adaptation of J.R.R. Tolkien's epic fantasy The Lord of the Rings - received 11 Academy Awards.'",
   ["oceania", "americas"], ["contemporary"])

# ---------------------------------------------------------------- Mar 1
ev(3, 1, "yellowstone", "yellowstone-established-1872", "Yellowstone becomes the first US national park", "1872",
   "Ulysses S. Grant signs the act creating the park.",
   "On {hd}, US President Ulysses S. Grant signed the act that established Yellowstone National Park, the country's first national park and generally considered the first in the world.",
   "1872: 'U.S. Pres. Ulysses S. Grant signed the act that established Yellowstone National Park.'",
   ["americas"], ["1800-1945"], "The first national park",
   "In 1872, Ulysses S. Grant signed the act creating Yellowstone, the first US national park.",
   extra=[art("Yellowstone National Park", "https://www.britannica.com/place/Yellowstone-National-Park",
              "'established by the U.S. Congress on March 1, 1872, as the country's first national park. It is also generally considered to have been the first national park in the world'; mainly in northwestern Wyoming, partly in Montana and Idaho; Old Faithful geyser.")])
ev(3, 1, "march-first", "march-first-movement-1919", "The March First Movement begins in Korea", "1919",
   "Protesters in Seoul demand independence from Japan.",
   "On {hd}, protesters in Seoul launched the March First Movement, a series of demonstrations for Korean independence from Japan.",
   "1919: 'Protesters in Seoul launched the March First Movement, a series of demonstrations for Korean national independence from Japan.'",
   ["asia"], ["1800-1945"])
ev(3, 1, "peace-corps", "peace-corps-established-1961", "John F. Kennedy establishes the Peace Corps", "1961",
   "An executive order creates the volunteer program.",
   "On {hd}, US President John F. Kennedy established the Peace Corps by executive order. Congress authorized it through the Peace Corps Act that September.",
   "1961: 'The Peace Corps was established by U.S. Pres. John F. Kennedy by means of the Peace Corps Act.' (The article below gives the March 1 executive order and the September 22, 1961, act.)",
   ["americas", "global"], ["cold-war"],
   extra=[art("Peace Corps", "https://www.britannica.com/topic/Peace-Corps",
              "'established by executive order by Pres. John F. Kennedy on March 1, 1961, and authorized by the U.S. Congress through the Peace Corps Act of September 22, 1961'; first director R. Sargent Shriver; volunteers serve abroad for two years.")])
ev(3, 1, "hoover-dam", "hoover-dam-completed-1936", "The Hoover Dam is completed", "1936",
   "The dam on the Colorado River is finished at the Arizona-Nevada border.",
   "On {hd}, the Hoover Dam on the Colorado River, at the Arizona-Nevada border, was completed.",
   "1936: 'the Hoover Dam on the Colorado River at the Arizona-Nevada border was completed'.",
   ["americas"], ["1800-1945"])
ev(3, 1, "native-son", "native-son-published-1940", "Richard Wright publishes Native Son", "1940",
   "The novel confronts systemic racism in the United States.",
   "On {hd}, Richard Wright published Native Son, a groundbreaking novel that explored systemic racism in the United States.",
   "1940: 'Richard Wright published Native Son, a groundbreaking novel that explored systemic racism in the United States.'",
   ["americas"], ["1800-1945"])

# ---------------------------------------------------------------- Mar 2
ev(3, 2, "concorde-first-flight", "concorde-first-flight-1969", "Concorde makes its first flight", "1969",
   "The Anglo-French supersonic airliner takes to the air.",
   "On {hd}, Concorde, the supersonic airliner built by Britain and France, made its first flight. It would cruise at more than twice the speed of sound.",
   "", ["europe"], ["cold-war"], "Concorde takes to the sky",
   "In 1969, the supersonic airliner Concorde made its first flight.",
   only=[art("Concorde", "https://www.britannica.com/technology/Concorde",
             "first flight 'March 2, 1969'; cruising speed 'Mach 2.04 (more than twice the speed of sound)'; commercial service January 21, 1976; retired 2003; airframe by British Aerospace and Aerospatiale, engines by Rolls-Royce and SNECMA.")])
ev(3, 2, "wilt-100", "wilt-chamberlain-scores-100-1962", "Wilt Chamberlain scores 100 points in an NBA game", "1962",
   "His single-game record still stands.",
   "On {hd}, Wilt Chamberlain scored 100 points in a single NBA game, a record that has never been surpassed.",
   "1962 (featured): 'Wilt Chamberlain scored a record 100 points in a single NBA game - a record that has not been surpassed to this day.'",
   ["americas"], ["cold-war"])
ev(3, 2, "morocco-independence", "morocco-independence-1956", "Morocco proclaims independence from France", "1956",
   "France's protectorate over Morocco ends.",
   "On {hd}, Morocco proclaimed its independence from France.",
   "1956: 'Morocco proclaimed independence from France.'",
   ["africa"], ["decolonization"])
ev(3, 2, "gorbachev-born", "mikhail-gorbachev-born-1931", "Mikhail Gorbachev is born", "1931",
   "The last leader of the Soviet Union is born.",
   "On {hd}, Mikhail Gorbachev, who would become the last leader of the Soviet Union, was born.",
   "Famous Birthdays: 1931, Mikhail Gorbachev was born.",
   ["europe"], ["1800-1945"])
ev(3, 2, "dr-seuss-born", "dr-seuss-born-1904", "Dr. Seuss is born", "1904",
   "The beloved children's author is born in Springfield, Massachusetts.",
   "On {hd}, Theodor Seuss Geisel, the children's author known as Dr. Seuss, was born in Springfield, Massachusetts.",
   "1904: Dr. Seuss was 'born in Springfield, Massachusetts'.",
   ["americas"], ["1800-1945"])

# ---------------------------------------------------------------- Mar 3
ev(3, 3, "serfs-freed", "russia-emancipates-serfs-1861", "Alexander II issues the manifesto freeing Russia's serfs", "1861",
   "The emperor's Emancipation Manifesto ends serfdom.",
   "On {hd}, the Russian emperor Alexander II issued the Emancipation Manifesto, which declared the freeing of the serfs.",
   "1861: 'The Russian emperor Alexander II issued the Emancipation Manifesto, which declared the freeing of the serfs.'",
   ["europe", "asia"], ["1800-1945"], "Millions of serfs declared free",
   "In 1861, Russia's Alexander II issued the manifesto that freed the serfs.",
   note="Gregorian date. In the Julian calendar then used in Russia, the manifesto is dated February 19.")
ev(3, 3, "anthem", "star-spangled-banner-anthem-1931", "The Star-Spangled Banner becomes the US national anthem", "1931",
   "Congress makes the song official.",
   "On {hd}, The Star-Spangled Banner was officially adopted as the national anthem of the United States.",
   "1931: 'The Star-Spangled Banner ... was officially adopted as the national anthem of the United States.'",
   ["americas"], ["1800-1945"])
ev(3, 3, "brest-litovsk", "treaty-of-brest-litovsk-1918", "The Treaty of Brest-Litovsk takes Soviet Russia out of World War I", "1918",
   "Soviet Russia makes peace with the Central Powers.",
   "On {hd}, the second of two treaties of Brest-Litovsk ended hostilities between the Central Powers and Soviet Russia.",
   "1918: 'The second of two treaties of Brest-Litovsk concluded hostilities between the Central Powers and Soviet Russia.'",
   ["europe"], ["1800-1945"])
ev(3, 3, "bell-born", "alexander-graham-bell-born-1847", "Alexander Graham Bell is born in Edinburgh", "1847",
   "The future inventor of the telephone is born.",
   "On {hd}, Alexander Graham Bell, who would patent the telephone, was born in Edinburgh, Scotland.",
   "1847: 'Alexander Graham Bell ... was born in Edinburgh.'",
   ["europe", "americas"], ["1800-1945"])
ev(3, 3, "time-magazine", "time-magazine-first-issue-1923", "Time magazine publishes its first issue", "1923",
   "The American weekly newsmagazine debuts.",
   "On {hd}, the first issue of the American weekly magazine Time was published.",
   "1923: 'The first issue of the American weekly magazine Time was published.'",
   ["americas"], ["1800-1945"])

# ---------------------------------------------------------------- Mar 4
ev(3, 4, "fdr-inaugurated", "fdr-first-inauguration-1933", "Franklin D. Roosevelt is inaugurated", "1933",
   "He is the last president sworn in on March 4.",
   "On {hd}, in the midst of the Great Depression, Franklin D. Roosevelt was inaugurated president of the United States. He was the last president to take office on March 4.",
   "1933: 'On this day in 1933, in the midst of the Great Depression, Franklin D. Roosevelt became the last U.S. president to be inaugurated on March 4.'",
   ["americas"], ["1800-1945"], "The last March inauguration",
   "In 1933, Franklin D. Roosevelt became the last US president inaugurated on March 4.")
ev(3, 4, "constitution-in-effect", "us-constitution-takes-effect-1789", "The US Constitution goes into effect", "1789",
   "The new framework of government begins operating.",
   "On {hd}, the US Constitution went into effect as the governing law of the United States, on a date set by Congress.",
   "1789: 'The U.S. Constitution went into effect as the governing law of the United States, the date having been established by Congress.'",
   ["americas"], ["revolutionary"])
ev(3, 4, "frances-perkins", "frances-perkins-sworn-in-1933", "Frances Perkins becomes the first woman in a US cabinet", "1933",
   "She is sworn in as secretary of labor.",
   "On {hd}, Frances Perkins was sworn in as US secretary of labor under Franklin D. Roosevelt, the first woman appointed to a cabinet post. She served until 1945.",
   "1933: 'Frances Perkins was sworn in as U.S. secretary of labor in the administration of President Franklin D. Roosevelt. She was the first woman appointed to a cabinet post and served one of the longest terms of any Roosevelt appointee (1933-45).'",
   ["americas"], ["1800-1945"])
ev(3, 4, "vivaldi-born", "antonio-vivaldi-born-1678", "Antonio Vivaldi is born in Venice", "1678",
   "The composer of The Four Seasons is born.",
   "On {hd}, the composer Antonio Vivaldi, best known for the violin concertos The Four Seasons, was born in Venice.",
   "Famous Birthdays: 1678, Antonio Vivaldi.",
   ["europe"], ["early-modern"],
   extra=[art("Antonio Vivaldi", "https://www.britannica.com/biography/Antonio-Vivaldi",
              "born 'March 4, 1678, Venice'; died July 28, 1741, Vienna; nicknamed 'Il Prete Rosso' ('The Red Priest') for his red hair; best known for The Four Seasons, 'a group of four violin concerti'.")])
ev(3, 4, "castle-hill", "castle-hill-rising-1804", "Irish convicts rise in Australia's Castle Hill Rising", "1804",
   "Australia's first rebellion is crushed.",
   "On {hd}, Irish convicts rose up in the Castle Hill Rising, Australia's first rebellion. Troops of the New South Wales Corps suppressed it, leaving 15 dead.",
   "1804: 'Irish convicts rose up in the Castle Hill Rising, Australia's first rebellion. The uprising was suppressed by the troops of the New South Wales Corps and Loyal Associations, leaving 15 dead and many wounded.'",
   ["oceania", "europe"], ["revolutionary", "1800-1945"])

# ---------------------------------------------------------------- Mar 5
ev(3, 5, "iron-curtain", "iron-curtain-speech-1946", "Churchill's Iron Curtain speech", "1946",
   "Speaking in Missouri, he popularizes a phrase for Europe's Cold War divide.",
   "On {hd}, Winston Churchill popularized the term Iron Curtain in a speech at Fulton, Missouri.",
   "1946: 'British prime minister Winston Churchill popularized the term 'Iron Curtain'' in a speech at Fulton, Missouri.",
   ["europe", "americas"], ["cold-war"], "Churchill names the Iron Curtain",
   "In 1946, Winston Churchill popularized the phrase Iron Curtain in a speech in Missouri.")
ev(3, 5, "boston-massacre", "boston-massacre-1770", "British troops fire on a crowd in the Boston Massacre", "1770",
   "Crispus Attucks and four others are killed.",
   "On {hd}, British troops opened fire on a crowd in Boston, killing Crispus Attucks and four others in what became known as the Boston Massacre.",
   "1770: 'British troops on this day in 1770 opened fire, killing Crispus Attucks and four others in the Boston Massacre.'",
   ["americas", "europe"], ["revolutionary"])
ev(3, 5, "stalin-dies", "joseph-stalin-dies-1953", "Joseph Stalin dies", "1953",
   "The Soviet leader is succeeded by Georgy Malenkov.",
   "On {hd}, the Soviet leader Joseph Stalin died at age 74 and was succeeded by Georgy Malenkov.",
   "1953: 'Soviet premier Joseph Stalin died at age 74 and was succeeded by Georgy Malenkov.'",
   ["europe", "asia"], ["cold-war"])
ev(3, 5, "voyager-jupiter", "voyager-1-jupiter-flyby-1979", "Voyager 1 makes its closest approach to Jupiter", "1979",
   "The probe's flyby reveals close-up views of Jupiter's moons.",
   "On {hd}, NASA's Voyager 1 made its closest approach to Jupiter. Its close-up photographs of moons such as Io, Europa, Ganymede and Callisto opened new worlds to planetary scientists.",
   "", ["global"], ["space-age", "cold-war"],
   only=[("NASA Science - Voyager 1", "https://science.nasa.gov/mission/voyager/voyager-1/",
          "NASA: launched Sept. 5, 1977; 'March 5, 1979: Jupiter flyby - closest approach'; encountered Amalthea, Io, Europa, Ganymede and Callisto; 'Spectacular close-up photos of the moons opened up completely new worlds for planetary scientists'; first to exit the heliosphere, Aug. 25, 2012. (Not used: the Mar 5 day page's claim that nine Io volcanoes were observed that day.)")])
ev(3, 5, "villa-lobos-born", "heitor-villa-lobos-born-1887", "Heitor Villa-Lobos is born in Rio de Janeiro", "1887",
   "Brazil's best-known composer is born.",
   "On {hd}, the Brazilian musician and composer Heitor Villa-Lobos was born in Rio de Janeiro.",
   "1887: 'Brazilian musician and composer Heitor Villa-Lobos was born in Rio de Janeiro.'",
   ["americas"], ["1800-1945"])

# ---------------------------------------------------------------- Mar 6
ev(3, 6, "ghana-independence", "ghana-becomes-independent-1957", "Ghana becomes independent", "1957",
   "Led by Kwame Nkrumah, the former Gold Coast becomes a nation.",
   "On {hd}, Ghana became an independent nation, led by Prime Minister Kwame Nkrumah.",
   "1957: 'Ghana became an independent nation, led by Prime Minister Kwame Nkrumah.'",
   ["africa"], ["decolonization"], "A new nation in West Africa",
   "In 1957, Ghana became independent, led by Prime Minister Kwame Nkrumah.")
ev(3, 6, "michelangelo-born", "michelangelo-born-1475", "Michelangelo is born", "1475",
   "The Renaissance artist of the Sistine Chapel ceiling is born.",
   "On {hd}, the Renaissance artist Michelangelo was born in Caprese, in the Republic of Florence. He later painted the ceiling of the Sistine Chapel.",
   "1475: 'Renaissance artist Michelangelo ... was born in the Republic of Florence.'",
   ["europe"], ["medieval", "early-modern"], note=J,
   extra=[art("Michelangelo", "https://www.britannica.com/biography/Michelangelo",
              "born 'March 6, 1475, Caprese, Republic of Florence'; died February 18, 1564, Rome; Pieta (1499); David (1501-04); Sistine Chapel ceiling (1508-12).")])
ev(3, 6, "alamo-falls", "fall-of-the-alamo-1836", "The Alamo falls to Santa Anna", "1836",
   "The 13-day siege ends in San Antonio.",
   "On {hd}, the Alamo fell to Mexican General Antonio Lopez de Santa Anna after a 13-day siege.",
   "1836: 'The Alamo ... fell to Mexican General Antonio Lopez de Santa Anna after a 13-day siege.'",
   ["americas"], ["1800-1945"])
ev(3, 6, "garcia-marquez-born", "gabriel-garcia-marquez-born-1927", "Gabriel Garcia Marquez is born in Colombia", "1927",
   "The author of One Hundred Years of Solitude is born in Aracataca.",
   "On {hd}, the novelist Gabriel Garcia Marquez, author of One Hundred Years of Solitude and winner of the 1982 Nobel Prize for Literature, was born in Aracataca, Colombia.",
   "1927: 'Gabriel Garcia Marquez ... was born in Aracataca, Colombia.'",
   ["americas"], ["1800-1945"],
   extra=[art("Gabriel Garcia Marquez", "https://www.britannica.com/biography/Gabriel-Garcia-Marquez",
              "born 'March 6, 1927, Aracataca, Colombia'; One Hundred Years of Solitude (1967), set in the town of Macondo; Nobel Prize 1982; known for magic realism.")])
ev(3, 6, "la-traviata", "la-traviata-premieres-1853", "Verdi's La Traviata premieres in Venice", "1853",
   "The opera opens at La Fenice.",
   "On {hd}, Giuseppe Verdi's opera La Traviata premiered at La Fenice opera house in Venice.",
   "1853: 'Giuseppe Verdi's opera La traviata premiered at La Fenice opera house in Venice.'",
   ["europe"], ["1800-1945"])

# ---------------------------------------------------------------- Mar 7
ev(3, 7, "bell-patent", "bell-telephone-patent-1876", "Alexander Graham Bell receives a patent for the telephone", "1876",
   "The patent that launches the telephone age is granted.",
   "On {hd}, Alexander Graham Bell received a US patent for the telephone.",
   "1876: 'Alexander Graham Bell received a patent for the telephone.'",
   ["americas"], ["1800-1945"], "A patent that rang in a new era",
   "In 1876, Alexander Graham Bell received a patent for the telephone.")
ev(3, 7, "selma", "selma-bloody-sunday-1965", "Marchers are attacked at Selma's Edmund Pettus Bridge", "1965",
   "State troopers beat civil rights marchers on what became known as Bloody Sunday.",
   "On {hd}, state troopers used nightsticks and tear gas to attack civil rights activists crossing a bridge in Selma, Alabama, at the start of their attempted march to the state capitol in Montgomery.",
   "1965: 'State troopers used nightsticks and tear gas to attack civil rights activists as they crossed a bridge in Selma, Alabama, during their attempted march to the state capitol in Montgomery.'",
   ["americas"], ["cold-war"])
ev(3, 7, "marcus-aurelius", "marcus-aurelius-and-lucius-verus-emperors-161", "Marcus Aurelius and Lucius Verus become joint Roman emperors", "161",
   "For the first time, two emperors rule Rome together.",
   "On {hd}, Marcus Aurelius declared himself and his adoptive brother Lucius Verus emperors of Rome, the first time two emperors ruled the empire together.",
   "161 CE (featured): 'Marcus Aurelius declared himself and his adoptive brother Lucius Verus emperors of Rome, marking the first time two emperors would rule the Roman Empire simultaneously.'",
   ["europe"], ["ancient"], note=J)
ev(3, 7, "bigelow", "kathryn-bigelow-best-director-2010", "Kathryn Bigelow becomes the first woman to win the best director Oscar", "2010",
   "She is honored for The Hurt Locker.",
   "On {hd}, Kathryn Bigelow became the first woman to win the Academy Award for best director, for The Hurt Locker.",
   "2010: 'Kathryn Bigelow became the first woman to win an Academy Award for best director, for The Hurt Locker (2008).'",
   ["americas"], ["contemporary"])
ev(3, 7, "ravel-born", "maurice-ravel-born-1875", "Maurice Ravel is born", "1875",
   "The French composer of Bolero is born.",
   "On {hd}, the French composer Maurice Ravel, best known for Bolero, was born in Ciboure, France.",
   "Famous Births: 1875, Maurice Ravel (composer).",
   ["europe"], ["1800-1945"],
   extra=[art("Maurice Ravel", "https://www.britannica.com/biography/Maurice-Ravel",
              "born 'March 7, 1875, Ciboure, France'; died December 28, 1937, Paris; Bolero (1928), 'a one-movement orchestral work known for beginning softly and ending as loudly as possible'; Daphnis et Chloe for the Ballets Russes (1912).")])


def main():
    for batch_id, days in BATCHES:
        b = Batch(batch_id)
        for key, row in EVENTS:
            if key in days:
                day, short, id, title, year, hd, summary, desc, sources, regions, eras, nt, nb, note = row
                b.add(day, short, id, title, year, hd, summary, desc, sources, regions, eras, nt, nb, note)
        b.write()


if __name__ == "__main__":
    main()
