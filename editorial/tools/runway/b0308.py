"""March 8 to April 12 event drafts (five batches).

Run from the repository root:
    PYTHONPATH=editorial/tools/runway python3 editorial/tools/runway/b0308.py

Most events cite Britannica's dated On This Day page for their day, read on
2026-10-01; the check note records what that page states. Where the day page
was missing a fact, framed it doubtfully, or got it wrong, the event cites (or
adds) an article page instead. The first event added for a day is featured.
"""
from lib import Batch

BR = "Encyclopaedia Britannica - "
MONTH = {3: "March", 4: "April"}
BATCHES = [
    ("2027-03-08-14-historical-events", [(3, d) for d in range(8, 15)]),
    ("2027-03-15-21-historical-events", [(3, d) for d in range(15, 22)]),
    ("2027-03-22-28-historical-events", [(3, d) for d in range(22, 29)]),
    ("2027-03-29-04-04-historical-events", [(3, d) for d in range(29, 32)] + [(4, d) for d in range(1, 5)]),
    ("2027-04-05-12-historical-events", [(4, d) for d in range(5, 13)]),
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
RU = "Gregorian date. Russia then used the Julian calendar, 13 days behind."

# ---------------------------------------------------------------- Mar 8
ev(3, 8, "fight-of-the-century", "frazier-defeats-ali-1971", "Joe Frazier beats Muhammad Ali in the Fight of the Century", "1971",
   "Frazier keeps the heavyweight title in a 15-round decision.",
   "On {hd}, Joe Frazier retained his world heavyweight championship by winning a 15-round decision over the former champion Muhammad Ali.",
   "1971: 'Joe Frazier retained his world heavyweight championship by winning a 15-round decision over former champion Muhammad Ali.'",
   ["americas"], ["cold-war"], "Frazier and Ali go 15 rounds",
   "In 1971, Joe Frazier kept his heavyweight title, outpointing Muhammad Ali over 15 rounds.")
ev(3, 8, "february-revolution", "february-revolution-begins-1917", "Rioting in Petrograd begins Russia's February Revolution", "1917",
   "Unrest in the capital starts the revolution that topples the tsar.",
   "On {hd}, rioting in Petrograd (now St. Petersburg) marked the beginning of the February Revolution, the first stage of the Russian Revolution, in which the monarchy was overthrown.",
   "1917: 'Rioting in Petrograd (today St. Petersburg) marked the beginning of the February Revolution.'",
   ["europe"], ["1800-1945"], note=RU,
   extra=[art("February Revolution", "https://www.britannica.com/event/February-Revolution",
              "'(March 8-12 [Feb. 24-28, old style], 1917), the first stage of the Russian Revolution of 1917, in which the monarchy was overthrown and replaced by the Provisional Government.'")])
ev(3, 8, "senate-cloture", "senate-adopts-cloture-1917", "The US Senate adopts a rule to limit filibusters", "1917",
   "Cloture gives senators a way to end debate.",
   "On {hd}, the US Senate voted to limit filibusters by adopting the rule of cloture.",
   "1917: 'The U.S. Senate voted to limit filibusters by adopting the rule of cloture.'",
   ["americas"], ["1800-1945"])
ev(3, 8, "nyse", "new-york-stock-exchange-constituted-1817", "The New York Stock Exchange is formally created", "1817",
   "Brokers who first met under a buttonwood tree organize a formal exchange.",
   "On {hd}, the New York Stock Exchange was formally created, as the New York Stock and Exchange Board. It grew out of a 1792 meeting of 24 brokers under a buttonwood tree on what is now Wall Street.",
   "1817: 'The New York Stock Exchange was formally created.'",
   ["americas"], ["1800-1945"],
   extra=[art("New York Stock Exchange", "https://www.britannica.com/topic/New-York-Stock-Exchange",
              "'evolved from a meeting of 24 stockbrokers under a buttonwood tree in 1792 on what is now Wall Street'; 'formally constituted as the New York Stock and Exchange Board in 1817'; present name adopted 1863.")])
ev(3, 8, "holmes-born", "oliver-wendell-holmes-jr-born-1841", "Oliver Wendell Holmes, Jr., is born in Boston", "1841",
   "The future Supreme Court justice and legal historian is born.",
   "On {hd}, Oliver Wendell Holmes, Jr., the future US Supreme Court justice and legal historian, was born in Boston.",
   "1841: 'U.S. Supreme Court justice and legal historian Oliver Wendell Holmes, Jr. ... was born in Boston.'",
   ["americas"], ["1800-1945"])

# ---------------------------------------------------------------- Mar 9
ev(3, 9, "barbie", "barbie-debuts-1959", "Barbie debuts at the American International Toy Fair", "1959",
   "Mattel introduces the doll in New York City.",
   "On {hd}, the Barbie doll debuted at the American International Toy Fair in New York City. Mattel cofounder Ruth Handler led its introduction.",
   "1959: 'Barbie ... was introduced by Mattel, Inc.'",
   ["americas"], ["cold-war"], "A doll is born at the Toy Fair",
   "In 1959, Mattel introduced Barbie at the American International Toy Fair in New York.",
   extra=[art("Barbie", "https://www.britannica.com/topic/Barbie",
              "'Barbie officially debuted on March 9, 1959, at the American International Toy Fair in New York City'; Ruth Handler, who cofounded Mattel with her husband Elliot, led the introduction; full name Barbara Millicent Roberts.")])
ev(3, 9, "monitor-virginia", "monitor-and-virginia-battle-1862", "The ironclads Monitor and Virginia fight at Hampton Roads", "1862",
   "Two armored warships meet in the first battle between ironclads.",
   "On {hd}, the ironclads Monitor and Virginia (the former Merrimack) battled at Hampton Roads, Virginia, during the American Civil War, the first fight between ironclad warships.",
   "1862: 'the ironclads Monitor and Virginia ... battled' (the first battle between ironclad warships, at Hampton Roads during the American Civil War).",
   ["americas"], ["1800-1945"])
ev(3, 9, "gagarin-born", "yuri-gagarin-born-1934", "Yuri Gagarin is born", "1934",
   "The first human to travel into space is born near Gzhatsk.",
   "On {hd}, Yuri Gagarin, who in 1961 became the first human to travel into space, was born near Gzhatsk in the Soviet Union (the town is now called Gagarin).",
   "1934: 'Yuri Gagarin ... was born in Russia.'",
   ["europe"], ["1800-1945"],
   extra=[art("Yuri Gagarin", "https://www.britannica.com/biography/Yuri-Gagarin",
              "born 'March 9, 1934, near Gzhatsk, Russia, U.S.S.R. [now Gagarin, Russia]'; flew Vostok 1 on April 12, 1961, one orbit of Earth in 1 hour 29 minutes; died March 27, 1968, near Moscow.")])
ev(3, 9, "napoleon-josephine", "napoleon-marries-josephine-1796", "Napoleon Bonaparte marries Josephine", "1796",
   "The young general weds the widow Marie-Josephe-Rose Tascher.",
   "On {hd}, Napoleon Bonaparte married Marie-Josephe-Rose Tascher de La Pagerie, the widowed Josephine de Beauharnais, in Paris.",
   "1796: 'Napoleon Bonaparte ... married Marie-Josephe-Rose Tascher.'",
   ["europe"], ["revolutionary"])
ev(3, 9, "fischer-born", "bobby-fischer-born-1943", "Bobby Fischer is born in Chicago", "1943",
   "The future world chess champion is born.",
   "On {hd}, the chess player Bobby Fischer, who in 1972 became the first native-born American to win the world chess championship, was born in Chicago.",
   "1943: 'Bobby Fischer was born in Chicago.'",
   ["americas"], ["1800-1945"],
   extra=[art("Bobby Fischer", "https://www.britannica.com/biography/Bobby-Fischer",
              "born 'March 9, 1943, Chicago, Illinois'; in 1972 became the first native-born American to win the world chess championship, defeating Boris Spassky in Reykjavik, Iceland, 12.5-8.5.")])

# ---------------------------------------------------------------- Mar 10
ev(3, 10, "mr-watson", "bell-first-telephone-speech-1876", "Bell calls 'Mr. Watson, come here'", "1876",
   "Alexander Graham Bell first transmits intelligible speech.",
   "On {hd}, Alexander Graham Bell first produced intelligible speech with his telephone, summoning his assistant Thomas A. Watson with the words 'Mr. Watson, come here, I want to see you.'",
   "1876: Alexander Graham Bell's 'liquid' transmitter design permitted the first speech transmission.",
   ["americas"], ["1800-1945"], "Mr. Watson, come here",
   "In 1876, Alexander Graham Bell first sent clear speech by telephone to his assistant.",
   extra=[art("Alexander Graham Bell", "https://www.britannica.com/biography/Alexander-Graham-Bell",
              "'He first produced intelligible speech on March 10, 1876, when he summoned his laboratory assistant, Thomas A. Watson, with words that Bell transcribed in his lab notes as \"Mr. Watson-come here-I want to see you.\"'; patent awarded March 7, 1876.")])
ev(3, 10, "park-removed", "park-geun-hye-removed-2017", "South Korea's Constitutional Court removes President Park Geun-hye", "2017",
   "The court upholds her impeachment, ending her presidency.",
   "On {hd}, South Korea's Constitutional Court upheld the impeachment of President Park Geun-hye, ending her presidency.",
   "2017: 'South Korean politician Park Geun-Hye's presidency ended when the country's Constitutional Court upheld her impeachment.'",
   ["asia"], ["contemporary"])
ev(3, 10, "mro", "mars-reconnaissance-orbiter-arrives-2006", "Mars Reconnaissance Orbiter enters orbit around Mars", "2006",
   "NASA's spacecraft begins searching for signs of water.",
   "On {hd}, NASA's Mars Reconnaissance Orbiter entered orbit around Mars and began searching for signs of water on the planet.",
   "2006: 'NASA's Mars Reconnaissance Orbiter entered Mars orbit and began searching for signs of water on the planet.'",
   ["global"], ["contemporary", "space-age"])
ev(3, 10, "james-earl-ray", "james-earl-ray-pleads-guilty-1969", "James Earl Ray pleads guilty to killing Martin Luther King, Jr.", "1969",
   "He is sentenced to 99 years in prison.",
   "On {hd}, James Earl Ray pleaded guilty to murdering the civil rights leader Martin Luther King, Jr., and was sentenced to 99 years in prison.",
   "1969: 'James Earl Ray pled guilty to murdering American civil rights leader Martin Luther King, Jr., and was sentenced to 99 years in prison.'",
   ["americas"], ["cold-war"])
ev(3, 10, "buffy", "buffy-the-vampire-slayer-premieres-1997", "Buffy the Vampire Slayer premieres", "1997",
   "The television series makes its debut.",
   "On {hd}, the television series Buffy the Vampire Slayer premiered.",
   "1997: 'The television series Buffy the Vampire Slayer premiered.'",
   ["americas"], ["contemporary"])

# ---------------------------------------------------------------- Mar 11
ev(3, 11, "raisin-in-the-sun", "a-raisin-in-the-sun-opens-1959", "A Raisin in the Sun opens on Broadway", "1959",
   "Lorraine Hansberry becomes the first Black woman to have a play produced on Broadway.",
   "On {hd}, Lorraine Hansberry's play A Raisin in the Sun debuted on Broadway. Hansberry, then 29, became the first Black woman to have a play produced on Broadway, and the play won the New York Drama Critics' Circle Award.",
   "1959 (featured): 'the play A Raisin in the Sun debuted on Broadway. The three-act drama was written by Lorraine Hansberry, who was 29 years old when she became the first Black woman to have a play produced on Broadway.'",
   ["americas"], ["cold-war"], "A Broadway first for Lorraine Hansberry",
   "In 1959, A Raisin in the Sun opened, the first Broadway play by a Black woman.",
   extra=[art("Lorraine Hansberry", "https://www.britannica.com/biography/Lorraine-Hansberry",
              "'The play opened in March 1959 at the Ethel Barrymore Theatre on Broadway, meeting with great success. It won the New York Drama Critics' Circle Award.'")])
ev(3, 11, "tohoku-earthquake", "japan-earthquake-and-tsunami-2011", "An earthquake and tsunami strike northeastern Japan", "2011",
   "The disaster triggers a major nuclear accident.",
   "On {hd}, an earthquake struck off the northeastern coast of Honshu, Japan, causing widespread damage and triggering a devastating tsunami that led to a major nuclear accident.",
   "2011: 'An earthquake struck off the northeastern coast of Honshu, Japan, causing widespread damage in the country and triggering a devastating tsunami that instigated a major nuclear accident.'",
   ["asia"], ["contemporary"])
ev(3, 11, "lithuania", "lithuania-declares-independence-1990", "Lithuania declares independence from the Soviet Union", "1990",
   "It is the first Soviet republic to do so.",
   "On {hd}, Lithuania became the first Soviet republic to declare independence from the USSR.",
   "1990: 'Lithuania became the first Soviet republic to declare independence from the U.S.S.R.'",
   ["europe"], ["cold-war"])
ev(3, 11, "lend-lease", "lend-lease-act-1941", "The US Congress passes the Lend-Lease Act", "1941",
   "The president gains power to aid nations vital to US defense.",
   "On {hd}, the Lend-Lease Act became law in the United States, giving the president authority to aid any nation whose defense was believed vital to US interests.",
   "1941: 'U.S. Congress passed the Lend-Lease Act, which gave the president authority to aid any nation whose defense was believed vital to U.S. interests.'",
   ["americas", "europe"], ["1800-1945"])
ev(3, 11, "gorbachev-leader", "gorbachev-becomes-soviet-leader-1985", "Mikhail Gorbachev becomes the Soviet leader", "1985",
   "He succeeds Konstantin Chernenko.",
   "On {hd}, Mikhail Gorbachev succeeded Konstantin Chernenko as leader of the Soviet Union.",
   "1985: Mikhail Gorbachev succeeded Konstantin Chernenko as Soviet leader.",
   ["europe"], ["cold-war"])

# ---------------------------------------------------------------- Mar 12
ev(3, 12, "salt-march", "gandhi-salt-march-begins-1930", "Gandhi begins the Salt March", "1930",
   "A walk to the sea challenges Britain's salt laws in India.",
   "On {hd}, Mohandas Gandhi set out from his ashram at Sabarmati, near Ahmadabad, on the Salt March, a walk of some 240 miles (385 km) to the sea at Dandi to protest British control of salt in India.",
   "1930 (featured): 'a small group of Indian citizens walked towards the sea, intending to produce salt from seawater rather than purchasing it from the British.'",
   ["asia"], ["1800-1945", "decolonization"], "A march to the sea for salt",
   "In 1930, Gandhi set out on the Salt March, a 240-mile walk to the sea at Dandi.",
   extra=[art("Salt March", "https://www.britannica.com/event/Salt-March",
              "began March 12, 1930, from Gandhi's religious retreat at Sabarmati near Ahmadabad; reached Dandi on April 5 after some 240 miles (385 km); on the morning of April 6 Gandhi and his followers picked up salt along the shore, 'technically \"producing\" salt and breaking the law.'")])
ev(3, 12, "girl-scouts", "first-american-girl-guides-troop-1912", "Juliette Gordon Low forms the first American Girl Guides troop", "1912",
   "The movement later becomes the Girl Scouts.",
   "On {hd}, Juliette Gordon Low formed the first troop of American Girl Guides, in Savannah, Georgia. By 1915 the movement was called the Girl Scouts of the United States of America.",
   "1912: 'Juliette Gordon Low formed the first troop of American Girl Guides.'",
   ["americas"], ["1800-1945"],
   extra=[art("Juliette Gordon Low", "https://www.britannica.com/biography/Juliette-Gordon-Low",
              "'organized the nation's first troop of Girl Guides in Savannah in March 1912'; by 1915 'the name had been changed to the Girl Scouts of the United States of America'.")])
ev(3, 12, "truman-doctrine", "truman-doctrine-1947", "President Truman sets out the Truman Doctrine", "1947",
   "The policy pledges US support against communist pressure.",
   "On {hd}, US President Harry S. Truman articulated what became known as the Truman Doctrine.",
   "1947: 'U.S. President Harry S. Truman articulated what became known as the Truman Doctrine.'",
   ["americas", "europe"], ["cold-war"])
ev(3, 12, "fireside-chat", "first-fireside-chat-1933", "Franklin D. Roosevelt gives his first fireside chat", "1933",
   "The president speaks to Americans by radio.",
   "On {hd}, US President Franklin D. Roosevelt gave his first fireside chat.",
   "1933: 'U.S. President Franklin D. Roosevelt gave his first fireside chat.'",
   ["americas"], ["1800-1945"])
ev(3, 12, "nato-expansion", "poland-hungary-czechia-join-nato-1999", "Poland, Hungary and the Czech Republic join NATO", "1999",
   "Three former Warsaw Pact countries enter the alliance.",
   "On {hd}, Poland, Hungary and the Czech Republic became members of the North Atlantic Treaty Organization (NATO).",
   "1999: 'Poland, Hungary, and the Czech Republic became members of the North Atlantic Treaty Organization (NATO).'",
   ["europe"], ["contemporary"])

# ---------------------------------------------------------------- Mar 13
ev(3, 13, "uranus", "herschel-discovers-uranus-1781", "William Herschel discovers Uranus", "1781",
   "A telescope reveals the seventh planet from the Sun.",
   "On {hd}, the astronomer William Herschel observed Uranus, the seventh planet from the Sun. It was the first planet discovered with the aid of a telescope.",
   "1781: 'English astronomer William Herschel observed the seventh planet from the Sun, Uranus.'",
   ["europe"], ["early-modern"], "A new planet in the night sky",
   "In 1781, William Herschel discovered Uranus, the seventh planet from the Sun.",
   extra=[art("Uranus", "https://www.britannica.com/place/Uranus-planet",
              "'Uranus was discovered on March 13, 1781, by the English astronomer William Herschel with the aid of a telescope'; 'the first planet to be discovered that had not been recognized in prehistoric times'; the article lists Georgium Sidus as an earlier name.")])
ev(3, 13, "pope-francis", "pope-francis-elected-2013", "Jorge Mario Bergoglio is elected Pope Francis", "2013",
   "The archbishop of Buenos Aires becomes pope.",
   "On {hd}, Jorge Mario Bergoglio, the archbishop of Buenos Aires, was elected pope and took the name Francis.",
   "2013: 'Jorge Mario Bergoglio, the archbishop of Buenos Aires, was elected pope' and took the name Francis.",
   ["europe", "americas"], ["contemporary"])
ev(3, 13, "alexander-ii", "alexander-ii-assassinated-1881", "Tsar Alexander II is assassinated", "1881",
   "The Russian emperor is killed in St. Petersburg.",
   "On {hd}, Tsar Alexander II of Russia was assassinated in St. Petersburg.",
   "1881: 'Tsar Alexander II of Russia was assassinated in St. Petersburg.'",
   ["europe"], ["1800-1945"], note=RU)
ev(3, 13, "anschluss", "anschluss-announced-1938", "Germany announces the Anschluss with Austria", "1938",
   "Austria is declared part of Nazi Germany.",
   "On {hd}, the Anschluss, the union of Austria with Nazi Germany, was announced.",
   "1938: The Anschluss between Austria and Germany was announced.",
   ["europe"], ["1800-1945"])
ev(3, 13, "britannica-print", "britannica-ends-print-edition-2012", "Encyclopaedia Britannica ends its print edition", "2012",
   "The encyclopedia moves to digital only.",
   "On {hd}, Encyclopaedia Britannica, Inc., announced that it was ceasing publication of its print edition.",
   "2012: 'Encyclopaedia Britannica, Inc., announced that it was ceasing publication of its print version.'",
   ["global"], ["contemporary"])

# ---------------------------------------------------------------- Mar 14
ev(3, 14, "einstein-born", "albert-einstein-born-1879", "Albert Einstein is born in Ulm", "1879",
   "The physicist known for the theories of relativity is born.",
   "On {hd}, Albert Einstein, the physicist known for his theories of relativity, was born in Ulm, Germany.",
   "1879: 'German American physicist Albert Einstein, one of the most creative intellects in human history, known for his groundbreaking theories of relativity, was born in Ulm, Germany.'",
   ["europe"], ["1800-1945"], "Happy birthday, Albert Einstein",
   "In 1879, Albert Einstein, the physicist of relativity, was born in Ulm, Germany.")
ev(3, 14, "cotton-gin", "cotton-gin-patent-1794", "Eli Whitney patents the cotton gin", "1794",
   "The American inventor receives a patent for his machine.",
   "On {hd}, the American inventor Eli Whitney received a patent for the cotton gin.",
   "1794: 'American inventor Eli Whitney received a patent for the cotton gin.'",
   ["americas"], ["revolutionary"])
ev(3, 14, "marx-dies", "karl-marx-dies-1883", "Karl Marx dies", "1883",
   "The coauthor of The Communist Manifesto dies at 64.",
   "On {hd}, the historian and revolutionary Karl Marx, who wrote The Communist Manifesto (1848) with Friedrich Engels, died at age 64.",
   "1883: 'Historian and revolutionary Karl Marx, who wrote (with Friedrich Engels) The Communist Manifesto (1848), died at age 64.'",
   ["europe"], ["1800-1945"])
ev(3, 14, "hawking-dies", "stephen-hawking-dies-2018", "Stephen Hawking dies", "2018",
   "The physicist of black holes and A Brief History of Time dies at 76.",
   "On {hd}, the English theoretical physicist Stephen Hawking, known for his work on black holes and for the book A Brief History of Time (1988), died at age 76.",
   "2018: 'English theoretical physicist Stephen Hawking, who was best known for his work on the physics of black holes and for the book A Brief History of Time: From the Big Bang to Black Holes (1988), died at age 76.'",
   ["europe"], ["contemporary"])
ev(3, 14, "ten-most-wanted", "fbi-ten-most-wanted-list-1950", "The FBI introduces its Ten Most Wanted list", "1950",
   "J. Edgar Hoover names the bureau's most sought fugitives.",
   "On {hd}, FBI Director J. Edgar Hoover introduced the Ten Most Wanted list of US fugitives.",
   "1950 (featured): 'FBI director J. Edgar Hoover introduced the infamous \"Ten Most Wanted\" list, which provided the names of the \"worst of the worst\" U.S. fugitives.'",
   ["americas"], ["cold-war"])

# ---------------------------------------------------------------- Mar 15
ev(3, 15, "ides-of-march", "julius-caesar-assassinated-44bce", "Julius Caesar is assassinated on the Ides of March", "44 BCE",
   "The Roman dictator is killed in the midst of his reforms.",
   "On {hd}, the Roman dictator Julius Caesar was assassinated on the Ides of March while launching a series of political and social reforms.",
   "44 BCE: 'Roman dictator Julius Caesar was launching a series of political and social reforms when he was assassinated on the Ides of March.'",
   ["europe"], ["ancient"], "Beware the Ides of March",
   "In 44 BCE, the Roman dictator Julius Caesar was assassinated on the Ides of March.", note=J)
ev(3, 15, "nicholas-ii-abdicates", "nicholas-ii-abdicates-1917", "Tsar Nicholas II abdicates", "1917",
   "The rule of the Romanov dynasty ends.",
   "On {hd}, Tsar Nicholas II was forced to abdicate, ending the rule of the Romanov dynasty in Russia.",
   "1917: 'Tsar Nicholas II was forced to abdicate, thus ending the rule of the Romanov dynasty.'",
   ["europe"], ["1800-1945"], note=RU)
ev(3, 15, "we-shall-overcome", "johnson-we-shall-overcome-speech-1965", "Lyndon B. Johnson gives his We Shall Overcome speech", "1965",
   "The president introduces voting rights legislation to Congress.",
   "On {hd}, US President Lyndon B. Johnson delivered his We Shall Overcome speech, introducing voting rights legislation.",
   "1965: President Lyndon B. Johnson delivered his We Shall Overcome speech introducing voting rights legislation.",
   ["americas"], ["cold-war"])
ev(3, 15, "rbg-born", "ruth-bader-ginsburg-born-1933", "Ruth Bader Ginsburg is born", "1933",
   "The future Supreme Court justice is born.",
   "On {hd}, Ruth Bader Ginsburg, who became a justice of the US Supreme Court, was born.",
   "Famous births: 1933, Ruth Bader Ginsburg.",
   ["americas"], ["1800-1945"])
ev(3, 15, "jackson-born", "andrew-jackson-born-1767", "Andrew Jackson is born", "1767",
   "The future seventh US president is born.",
   "On {hd}, Andrew Jackson, who became the seventh president of the United States, was born.",
   "1767: Andrew Jackson was born.",
   ["americas"], ["early-modern"],
   extra=[art("Andrew Jackson", "https://www.britannica.com/biography/Andrew-Jackson",
              "born 'March 15, 1767, Waxhaws region, South Carolina'; 'the seventh president of the United States (1829-37)'.")])

# ---------------------------------------------------------------- Mar 16
ev(3, 16, "goddard-rocket", "goddard-liquid-fuel-rocket-1926", "Robert Goddard launches the first liquid-fueled rocket", "1926",
   "The rocket flies for 2.5 seconds over a Massachusetts farm.",
   "On {hd}, Robert Goddard launched the world's first liquid-propelled rocket on his aunt's farm in Auburn, Massachusetts. It flew for 2.5 seconds and reached an altitude of 12.5 meters (41 feet).",
   "", ["americas"], ["1800-1945"], "Two and a half seconds of liftoff",
   "In 1926, Robert Goddard launched the first liquid-fueled rocket from a Massachusetts farm.",
   only=[art("Robert Goddard", "https://www.britannica.com/biography/Robert-Goddard",
             "'On March 16, 1926, the world's first flight of a liquid-propelled rocket engine took place on his Aunt Effie's farm in Auburn, Massachusetts'; it 'flew for 2.5 seconds, reached an altitude of 12.5 metres (41 feet), and landed 56 metres (184 feet) away'.")])
ev(3, 16, "west-point", "west-point-founded-1802", "The US Military Academy at West Point is founded", "1802",
   "It begins as a training center for the Corps of Engineers.",
   "On {hd}, the United States Military Academy at West Point, New York, was founded, originally as a training center for the US Corps of Engineers.",
   "1802: 'The United States Military Academy at West Point, New York ... was originally founded as a training centre for the U.S. Corps of Engineers.'",
   ["americas"], ["revolutionary"])
ev(3, 16, "madison-born", "james-madison-born-1751", "James Madison is born", "1751",
   "The Father of the Constitution is born in Virginia.",
   "On {hd}, James Madison, the fourth US president, known as the Father of the Constitution, was born at Port Conway, Virginia.",
   "1751: 'James Madison, the fourth U.S. president and one of the Founding Fathers, was born in Virginia.'",
   ["americas"], ["early-modern"],
   note="Gregorian date. Under the Julian (Old Style) calendar then used in the British colonies, he was born on March 5, 1751.",
   extra=[art("James Madison", "https://www.britannica.com/biography/James-Madison",
              "'born March 16 [March 5, Old Style], 1751, Port Conway, Virginia'; 'created the basic framework for the U.S. Constitution and helped write the Bill of Rights. He is therefore known as the Father of the Constitution.'")])
ev(3, 16, "amoco-cadiz", "amoco-cadiz-oil-spill-1978", "The Amoco Cadiz runs aground off Brittany", "1978",
   "The tanker breaks in two and spills crude oil along the French coast.",
   "On {hd}, the tanker Amoco Cadiz ran aground and broke in two off the coast of Brittany, France, releasing nearly 69 million gallons of light crude oil.",
   "1978: 'The Amoco Cadiz tanker ran aground and broke in two, releasing nearly 69 million gallons of light crude oil off the coast of Brittany, France.'",
   ["europe"], ["cold-war"])
ev(3, 16, "opry-house", "grand-ole-opry-house-opens-1974", "The Grand Ole Opry moves into the new Opry House", "1974",
   "The country music radio show broadcasts from its new Nashville home.",
   "On {hd}, the Grand Ole Opry radio show was broadcast for the first time from the new Grand Ole Opry House in Nashville, Tennessee.",
   "1974: 'The Grand Ole Opry radio show was broadcast for the first time from the new Grand Ole Opry House in Nashville.'",
   ["americas"], ["cold-war"])

# ---------------------------------------------------------------- Mar 17
ev(3, 17, "kingdom-of-italy", "kingdom-of-italy-proclaimed-1861", "The Kingdom of Italy is proclaimed", "1861",
   "A parliament in Turin declares a unified Italy.",
   "On {hd}, after more than ten years of revolution led by figures such as Giuseppe Garibaldi, a parliament assembled in Turin officially proclaimed the unified Kingdom of Italy.",
   "1861: 'In Turin, Italy, after more than 10 years of revolution led by such figures as Giuseppe Garibaldi, a parliament assembled and officially proclaimed the unified Kingdom of Italy.'",
   ["europe"], ["1800-1945"], "Italy becomes one kingdom",
   "In 1861, a parliament in Turin proclaimed the unified Kingdom of Italy.")
ev(3, 17, "boston-evacuated", "british-evacuate-boston-1776", "The British evacuate Boston", "1776",
   "A siege by Washington's army forces General Howe out.",
   "On {hd}, British General William Howe evacuated Boston after a successful siege by American revolutionaries led by General George Washington.",
   "1776: 'British General William Howe evacuated Boston after a successful siege by American revolutionaries led by General George Washington.'",
   ["americas"], ["revolutionary"])
ev(3, 17, "vanguard-1", "vanguard-1-launched-1958", "Vanguard 1, the first solar-powered satellite, is launched", "1958",
   "The US satellite lifts off from Cape Canaveral.",
   "On {hd}, Vanguard 1, the first solar-powered satellite, was launched from Cape Canaveral, Florida.",
   "1958: 'The first solar-powered satellite, Vanguard 1, was launched from Cape Canaveral, Florida.'",
   ["americas"], ["cold-war", "space-age"])
ev(3, 17, "golda-meir", "golda-meir-prime-minister-1969", "Golda Meir becomes prime minister of Israel", "1969",
   "She is the country's fourth prime minister.",
   "On {hd}, Golda Meir became the fourth prime minister of Israel.",
   "1969: 'Golda Meir became the fourth prime minister of Israel.'",
   ["middle-east"], ["cold-war"])
ev(3, 17, "sa-referendum", "south-africa-reform-referendum-1992", "White South African voters back an end to apartheid", "1992",
   "Nearly 69 percent support F.W. de Klerk's reforms.",
   "On {hd}, nearly 69 percent of white South African voters backed President F.W. de Klerk's reforms, including the repeal of racially discriminatory laws, effectively endorsing the dismantling of apartheid.",
   "1992: 'Nearly 69 percent of white South African voters backed F.W. de Klerk's reforms-which included the repeal of racially discriminatory laws-and effectively endorsed the dismantling of apartheid.'",
   ["africa"], ["contemporary"])

# ---------------------------------------------------------------- Mar 18
ev(3, 18, "leonov-spacewalk", "leonov-first-spacewalk-1965", "Aleksei Leonov makes the first spacewalk", "1965",
   "The Soviet cosmonaut leaves Voskhod 2 through an air lock.",
   "On {hd}, the Soviet cosmonaut Aleksei Leonov passed through an air lock on the spacecraft Voskhod 2 and became the first person to walk in space.",
   "1965: 'Soviet cosmonaut Aleksei Leonov, after passing through an air lock on the spacecraft Voskhod 2, became the first man to walk in space.'",
   ["europe", "global"], ["cold-war", "space-age"], "The first walk in space",
   "In 1965, Aleksei Leonov left Voskhod 2 to become the first person to walk in space.")
ev(3, 18, "pascal-transit", "pascal-paris-carriage-service-1662", "Blaise Pascal's public carriage service begins in Paris", "1662",
   "Shared coaches start running on fixed routes.",
   "On {hd}, a public transit system of shared carriages devised by the mathematician and philosopher Blaise Pascal began operating in Paris.",
   "1662 (featured): 'Blaise Pascal's public transit system began operating in Paris.'",
   ["europe"], ["early-modern"])
ev(3, 18, "stamp-act-repealed", "stamp-act-repealed-1766", "Parliament repeals the Stamp Act", "1766",
   "Protests in the American colonies force the tax's repeal.",
   "On {hd}, the British Parliament repealed the Stamp Act of 1765 after violent protests from American colonists, including the group known as the Sons of Liberty.",
   "1766: 'The British Parliament repealed the Stamp Act of 1765 after violent protests from American colonists, including a group known as the Sons of Liberty.'",
   ["europe", "americas"], ["revolutionary"])
ev(3, 18, "paris-commune", "paris-commune-begins-1871", "The Paris Commune begins", "1871",
   "Parisians rise against the French government.",
   "On {hd}, the Commune of Paris, an insurrection of Parisians against the French government, began. It lasted until May 28.",
   "1871: 'The Commune of Paris, an insurrection of Parisians against the French government, began, lasting until May 28.'",
   ["europe"], ["1800-1945"])
ev(3, 18, "gardner-heist", "gardner-museum-theft-1990", "Thieves steal 13 works from Boston's Gardner Museum", "1990",
   "Paintings by Rembrandt and Vermeer are taken and never recovered.",
   "On {hd}, two men posing as police officers stole 13 works, including paintings by Rembrandt and Johannes Vermeer, from the Isabella Stewart Gardner Museum in Boston. The art was never recovered.",
   "1990: 'Two men pretending to be police officers stole 13 works, including paintings by Rembrandt and Johannes Vermeer, from the Isabella Stewart Gardner Museum in Boston; the stolen art was never recovered.'",
   ["americas"], ["contemporary"])

# ---------------------------------------------------------------- Mar 19
ev(3, 19, "sagrada-familia", "sagrada-familia-cornerstone-1882", "The cornerstone of the Sagrada Familia is laid in Barcelona", "1882",
   "Work begins on the basilica Antoni Gaudi would transform.",
   "On {hd}, the bishop of Barcelona laid the cornerstone of the Basilica de la Sagrada Familia. Antoni Gaudi took over as chief architect the next year.",
   "1882 (featured): 'the bishop of Barcelona laid the cornerstone of the Basilica de la Sagrada Familia.'",
   ["europe"], ["1800-1945"], "A basilica's first stone",
   "In 1882, the cornerstone of Barcelona's Sagrada Familia was laid.",
   extra=[art("Sagrada Familia", "https://www.britannica.com/topic/Sagrada-Familia",
              "'The first stone was laid in 1882' under initial architect Francisco de Paula del Villar; 'Gaudi took over as chief architect in 1883'; structure 'considered largely complete' by the 2026 centennial of Gaudi's death, with work expected into the 2030s.")])
ev(3, 19, "standard-time-act", "standard-time-act-signed-1918", "Woodrow Wilson signs the Standard Time Act", "1918",
   "The law brings daylight saving time to the United States.",
   "On {hd}, US President Woodrow Wilson signed the Standard Time Act, which established daylight saving time in the United States and gave the federal government oversight of the country's time zones.",
   "1918: 'U.S. President Woodrow Wilson signed the Standard Time Act, which established Daylight Saving Time in the United States; the legislation also gave the federal government oversight of the country's time zones.'",
   ["americas"], ["1800-1945"])
ev(3, 19, "nevada-gambling", "nevada-legalizes-gambling-1931", "Nevada legalizes gambling", "1931",
   "The law paves the way for casinos in Las Vegas.",
   "On {hd}, Nevada legalized gambling, paving the way for casinos in the state, most notably in Las Vegas.",
   "1931: 'Nevada legalized gambling, which paved the way for casinos in the state, most notably in Las Vegas.'",
   ["americas"], ["1800-1945"])
ev(3, 19, "dylan-debut", "bob-dylan-debut-album-1962", "Bob Dylan releases his debut album", "1962",
   "The eponymous record gets mixed reviews.",
   "On {hd}, Bob Dylan released his eponymous debut album, to mixed reviews.",
   "1962: 'American musician Bob Dylan released his eponymous debut album to mixed reviews.'",
   ["americas"], ["cold-war"])
ev(3, 19, "clarke-dies", "arthur-c-clarke-dies-2008", "Arthur C. Clarke dies", "2008",
   "The science-fiction writer behind 2001: A Space Odyssey dies at 90.",
   "On {hd}, the English writer Arthur C. Clarke, known for his science-fiction novels and his work on the film 2001: A Space Odyssey (1968), died at age 90.",
   "2008: 'English writer Arthur C. Clarke-who was best known for his visionary science-fiction novels and for his work on Stanley Kubrick's hugely successful motion picture 2001: A Space Odyssey (1968)-died at age 90.'",
   ["europe", "asia"], ["contemporary"])

# ---------------------------------------------------------------- Mar 20
ev(3, 20, "uncle-toms-cabin", "uncle-toms-cabin-published-1852", "Uncle Tom's Cabin is published as a book", "1852",
   "Harriet Beecher Stowe's antislavery novel appears in book form.",
   "On {hd}, Harriet Beecher Stowe's antislavery novel Uncle Tom's Cabin was published as a book, after running as a serial.",
   "1852 (featured): 'Uncle Tom's Cabin is published as a novel' - Harriet Beecher Stowe's novel, which had previously run as a serial.",
   ["americas"], ["1800-1945"], "A novel that stirred a nation",
   "In 1852, Harriet Beecher Stowe's Uncle Tom's Cabin was published as a book.")
ev(3, 20, "tokyo-sarin", "tokyo-subway-sarin-attack-1995", "AUM Shinrikyo releases nerve gas in the Tokyo subway", "1995",
   "The sarin attack kills and injures commuters.",
   "On {hd}, members of the AUM Shinrikyo sect released nerve gas in the Tokyo subway, killing at least 12 people and injuring thousands.",
   "1995: 'Members of AUM Shinrikyo released nerve gas into a Tokyo subway, killing 12 people and injuring thousands.'",
   ["asia"], ["contemporary"])
ev(3, 20, "lennon-ono", "lennon-marries-yoko-ono-1969", "John Lennon marries Yoko Ono in Gibraltar", "1969",
   "The Beatle weds the Japanese artist and musician.",
   "On {hd}, John Lennon of the Beatles married the Japanese artist and musician Yoko Ono in Gibraltar.",
   "1969: 'John Lennon, a leader of the seminal British rock group the Beatles, married Japanese artist and musician Yoko Ono in Gibraltar.'",
   ["europe"], ["cold-war"])
ev(3, 20, "ripon-meeting", "ripon-republican-party-meeting-1854", "A meeting in Ripon, Wisconsin, proposes a new party", "1854",
   "Opponents of slavery's expansion call for what became the Republican Party.",
   "On {hd}, a meeting of Whigs, anti-Nebraska Democrats and Free-Soilers in Ripon, Wisconsin, proposed forming a new party, which became the Republican Party.",
   "1854: 'A meeting of Whigs, anti-Nebraska Democrats, and Free-Soilers in Ripon, Wisconsin, proposed the formation' of the Republican Party.",
   ["americas"], ["1800-1945"])
ev(3, 20, "ibsen-born", "henrik-ibsen-born-1828", "Henrik Ibsen is born", "1828",
   "The Norwegian playwright is born.",
   "On {hd}, the Norwegian playwright Henrik Ibsen was born.",
   "Famous births: 1828, Henrik Ibsen (playwright).",
   ["europe"], ["1800-1945"])

# ---------------------------------------------------------------- Mar 21
ev(3, 21, "alcatraz-closes", "alcatraz-closes-1963", "Alcatraz prison closes", "1963",
   "The federal prison on San Francisco Bay is shut down.",
   "On {hd}, the federal prison on Alcatraz Island in San Francisco Bay was formally shut down.",
   "1963 (featured): 'the prison on San Francisco Bay's Alcatraz Island was formally shut down by the government.'",
   ["americas"], ["cold-war"], "The Rock shuts its doors",
   "In 1963, the federal prison on Alcatraz Island in San Francisco Bay closed.")
ev(3, 21, "first-tweet", "first-public-tweet-2006", "Jack Dorsey sends the first public tweet", "2006",
   "It reads 'just setting up my twttr'.",
   "On {hd}, Twitter cofounder Jack Dorsey sent the first public tweet, which read 'just setting up my twttr.'",
   "2006: 'Twitter cofounder Jack Dorsey sent the first public tweet, which read \"just setting up my twttr.\"'",
   ["americas", "global"], ["contemporary"])
ev(3, 21, "namibia", "namibia-independence-1990", "Namibia becomes independent", "1990",
   "German and then South African rule ends after 106 years.",
   "On {hd}, after 106 years of German and South African rule, Namibia became independent.",
   "1990: 'After 106 years of German and South African rule, Namibia became independent.'",
   ["africa"], ["decolonization", "contemporary"])
ev(3, 21, "sharpeville", "sharpeville-massacre-1960", "Police kill protesters at Sharpeville", "1960",
   "About 70 demonstrators against South Africa's pass laws are killed.",
   "On {hd}, police killed about 70 Black African demonstrators in Sharpeville, South Africa, during a protest against the pass laws.",
   "1960: 'About 70 Black African demonstrators were killed by police in Sharpeville, Gauteng province, during a protest against South Africa's pass laws.'",
   ["africa"], ["cold-war"])
ev(3, 21, "bach-born", "johann-sebastian-bach-born-1685", "Johann Sebastian Bach is born", "1685",
   "The German composer is born.",
   "On {hd}, the German composer Johann Sebastian Bach was born.",
   "1685: 'German composer Johann Sebastian Bach was born.'",
   ["europe"], ["early-modern"], note=J)

# ---------------------------------------------------------------- Mar 22
ev(3, 22, "please-please-me", "please-please-me-released-1963", "The Beatles release their first album, Please Please Me", "1963",
   "The band's debut LP comes out in the United Kingdom.",
   "On {hd}, the Beatles released their first album, Please Please Me, in the United Kingdom.",
   "1963: 'The Beatles released their first album, Please Please Me, in the United Kingdom.'",
   ["europe"], ["cold-war"], "The Beatles' first album",
   "In 1963, the Beatles released their first album, Please Please Me, in the UK.")
ev(3, 22, "stamp-act-passed", "stamp-act-passed-1765", "Parliament passes the Stamp Act", "1765",
   "A tax on printed papers angers the American colonies.",
   "On {hd}, the British Parliament passed the Stamp Act, which taxed various printed papers in the American colonies.",
   "1765: 'British Parliament passed the Stamp Act, which placed taxes on various printed papers.'",
   ["europe", "americas"], ["revolutionary"])
ev(3, 22, "arab-league", "arab-league-founded-1945", "The Arab League is founded in Cairo", "1945",
   "Arab states form a regional organization.",
   "On {hd}, the Arab League, a regional organization of Arab states in the Middle East, was organized in Cairo.",
   "1945: 'The Arab League, a regional organization of Arab states in the Middle East, was organized in Cairo.'",
   ["middle-east", "africa"], ["1800-1945"])
ev(3, 22, "first-masters", "first-masters-tournament-1934", "The first Masters golf tournament begins", "1934",
   "Augusta National hosts the event for the first time.",
   "On {hd}, the Augusta National Golf Club in Augusta, Georgia, hosted the first Masters Tournament.",
   "1934: 'The Augusta National Golf Club hosted the first Masters Tournament in Augusta, Georgia.'",
   ["americas"], ["1800-1945"])
ev(3, 22, "goethe-dies", "goethe-dies-1832", "Johann Wolfgang von Goethe dies in Weimar", "1832",
   "The German author and philosopher dies.",
   "On {hd}, the German author and philosopher Johann Wolfgang von Goethe died in Weimar.",
   "1832: 'German author and philosopher Johann Wolfgang von Goethe ... died in Weimar.'",
   ["europe"], ["1800-1945"])

# ---------------------------------------------------------------- Mar 23
ev(3, 23, "liberty-or-death", "patrick-henry-liberty-or-death-1775", "Patrick Henry declares 'give me liberty or give me death'", "1775",
   "A Virginia speech becomes a rallying cry of the American Revolution.",
   "On {hd}, Patrick Henry, a major figure of the American Revolution, delivered his well-known speech featuring the phrase 'give me liberty or give me death.'",
   "1775: 'Patrick Henry, a major figure of the American Revolution, delivered the well-known speech featuring the phrase \"give me liberty or give me death.\"'",
   ["americas"], ["revolutionary"], "Give me liberty or give me death",
   "In 1775, Patrick Henry gave his famous 'give me liberty or give me death' speech.")
ev(3, 23, "enabling-act", "enabling-act-passed-1933", "The German Reichstag passes the Enabling Act", "1933",
   "The vote gives Hitler's government sweeping powers.",
   "On {hd}, the German Reichstag, dominated by the Nazi Party and the German National People's Party, voted to pass the Enabling Act.",
   "1933: 'The German Reichstag, dominated by the Nazi Party and German National People's Party, voted to pass the Enabling Act.'",
   ["europe"], ["1800-1945"])
ev(3, 23, "mir-reentry", "mir-space-station-reenters-2001", "The Mir space station falls into the Pacific", "2001",
   "Designed for 5 years, Mir ends 15 years in orbit.",
   "On {hd}, the Soviet and Russian space station Mir, designed for only 5 years of service, ended 15 years in orbit when it reentered Earth's atmosphere and fell into the South Pacific Ocean.",
   "2001: 'Although designed for only 5 years of service, the Soviet/Russian space station Mir ended 15 years in orbit when it reentered Earth's atmosphere, falling into the South Pacific Ocean.'",
   ["global"], ["contemporary", "space-age"])
ev(3, 23, "otis-elevator", "otis-passenger-elevator-1857", "Elisha Otis installs his first commercial passenger elevator", "1857",
   "A New York City store gets a passenger elevator.",
   "On {hd}, Elisha Otis installed his first commercial passenger elevator, in a store in New York City.",
   "1857: 'Elisha Otis installed the first commercial elevator in a department store in New York City.'",
   ["americas"], ["1800-1945"])
ev(3, 23, "kurosawa-born", "akira-kurosawa-born-1910", "Akira Kurosawa is born", "1910",
   "The Japanese film director is born.",
   "On {hd}, the Japanese film director Akira Kurosawa was born.",
   "Famous births: 1910, Kurosawa Akira.",
   ["asia"], ["1800-1945"])

# ---------------------------------------------------------------- Mar 24
ev(3, 24, "koch-tb", "koch-announces-tuberculosis-bacterium-1882", "Robert Koch announces the cause of tuberculosis", "1882",
   "He has found the bacterium behind the disease.",
   "On {hd}, Robert Koch announced in a lecture in Berlin that he had discovered the bacterium that causes tuberculosis, opening the way to diagnosing and curing the disease. World TB Day marks the date.",
   "1882 (featured): 'Robert Koch gave a lecture at the Berlin Physiological Society where he introduced the basis of germ theory.' (The WHO page below gives the discovery announced: the tuberculosis bacterium.)",
   ["europe"], ["1800-1945"], "The germ behind tuberculosis",
   "In 1882, Robert Koch announced he had found the bacterium that causes tuberculosis.",
   extra=[("World Health Organization - World TB Day", "https://www.who.int/campaigns/world-tb-day",
           "WHO: 'The date marks the day in 1882 when Dr. Robert Koch announced that he had discovered the bacterium that causes TB, which opened the way towards diagnosing and curing this disease.'"),
          art("Robert Koch", "https://www.britannica.com/biography/Robert-Koch",
              "discovered the bacterium responsible for tuberculosis (1882); Nobel Prize for Physiology or Medicine 1905.")])
ev(3, 24, "exxon-valdez", "exxon-valdez-oil-spill-1989", "The Exxon Valdez runs aground in Alaska", "1989",
   "Some 11 million gallons of oil spill into Prince William Sound.",
   "On {hd}, the oil tanker Exxon Valdez ran aground, spilling some 11 million gallons (41 million liters) of oil into Prince William Sound, Alaska.",
   "1989: 'The oil tanker Exxon Valdez ran aground, spilling some 11 million gallons (41 million liters) of oil into Prince William Sound in Alaska.'",
   ["americas"], ["cold-war"])
ev(3, 24, "elizabeth-i-dies", "elizabeth-i-dies-1603", "Queen Elizabeth I dies", "1603",
   "James VI of Scotland succeeds her as James I of England.",
   "On {hd}, Queen Elizabeth I of England died after ruling for some 45 years, and King James VI of Scotland succeeded her as James I of England.",
   "1603: 'King James VI of Scotland ascended the English throne as James I following the death of Elizabeth I'; featured biography: 'Elizabeth I, who died on this day in 1603, ruled England for some 45 years.'",
   ["europe"], ["early-modern"], note=J)
ev(3, 24, "romero", "oscar-romero-assassinated-1980", "Archbishop Oscar Romero is assassinated", "1980",
   "He is killed while celebrating mass in San Salvador.",
   "On {hd}, Archbishop Oscar Romero was assassinated while celebrating mass in San Salvador, El Salvador. He was canonized in 2018.",
   "1980: 'Archbishop Oscar Romero was assassinated while celebrating mass in San Salvador; he was canonized in 2018.'",
   ["americas"], ["cold-war"])
ev(3, 24, "houdini-born", "harry-houdini-born-1874", "Harry Houdini is born", "1874",
   "The escape artist famous for slipping out of shackles is born.",
   "On {hd}, the magician Harry Houdini, famed for escaping from shackles and locked containers, was born.",
   "1874: 'American magician Harry Houdini, who earned an international reputation for his daring feats of self-extrication from shackles and locked containers, was born.'",
   ["europe", "americas"], ["1800-1945"])

# ---------------------------------------------------------------- Mar 25
ev(3, 25, "treaty-of-rome", "treaty-of-rome-signed-1957", "Six countries sign the Treaty of Rome", "1957",
   "The treaty creates the European Economic Community.",
   "On {hd}, Belgium, France, West Germany, Italy, Luxembourg and the Netherlands signed the Treaty of Rome, establishing the European Economic Community, a common market and customs union.",
   "", ["europe"], ["cold-war"], "Six nations, one common market",
   "In 1957, six nations signed the Treaty of Rome, founding the European Economic Community.",
   only=[art("Treaty of Rome", "https://www.britannica.com/event/Treaty-of-Rome",
             "'signed in Rome on March 25, 1957' by 'Belgium, France, the Federal Republic of Germany (West Germany), Italy, Luxembourg, and the Netherlands'; 'established the European Economic Community (EEC), creating a common market and customs union among its members'.")])
ev(3, 25, "triangle-fire", "triangle-shirtwaist-fire-1911", "The Triangle shirtwaist factory fire kills 146", "1911",
   "A fire in a New York City garment factory leads to workplace reforms.",
   "On {hd}, a fire at the Triangle shirtwaist factory in New York City killed 146 people.",
   "1911: 'A fire at the Triangle shirtwaist factory in New York City killed 146 people.'",
   ["americas"], ["1800-1945"])
ev(3, 25, "mumbles-railway", "mumbles-railway-passengers-1807", "The Mumbles Railway carries its first fare-paying passengers", "1807",
   "A line in Wales becomes the first passenger railway to charge fares.",
   "On {hd}, the first fee-paying passenger railway began operating in Wales, on the line later known as the Mumbles Railway.",
   "1807 (featured): 'the first fee-paying passenger railway began operating in Wales. It was eventually known as Mumbles Railway.'",
   ["europe"], ["1800-1945"])
ev(3, 25, "aretha-born", "aretha-franklin-born-1942", "Aretha Franklin is born", "1942",
   "The Queen of Soul is born.",
   "On {hd}, the American singer Aretha Franklin, later crowned the Queen of Soul, was born.",
   "1942: 'American singer Aretha Franklin, born this day in 1942, was crowned the \"Queen of Soul\".'",
   ["americas"], ["1800-1945"])
ev(3, 25, "bartok-born", "bela-bartok-born-1881", "Bela Bartok is born", "1881",
   "The Hungarian composer is born.",
   "On {hd}, the Hungarian composer Bela Bartok was born in Nagyszentmiklos, Hungary (now Sannicolau Mare, Romania).",
   "1881: 'Hungarian composer Bela Bartok was born in Nagyszentmiklos, Hungary, Austria-Hungary [now Sannicolau Mare, Romania].'",
   ["europe"], ["1800-1945"])

# ---------------------------------------------------------------- Mar 26
ev(3, 26, "egypt-israel-treaty", "egypt-israel-peace-treaty-1979", "Egypt and Israel sign a peace treaty", "1979",
   "Begin and Sadat seal the peace mediated at Camp David.",
   "On {hd}, Israel and Egypt signed a peace treaty agreed to by Menachem Begin and Anwar Sadat and based on the Camp David Accords mediated by US President Jimmy Carter in September 1978.",
   "1979: 'The historic peace treaty between Israel and Egypt, agreed to by Menachem Begin and Anwar Sadat and based on the Camp David Accords mediated by U.S. President Jimmy Carter in September 1978, was signed.'",
   ["middle-east", "africa"], ["cold-war"], "Peace between Egypt and Israel",
   "In 1979, Egypt and Israel signed a peace treaty built on the Camp David Accords.")
ev(3, 26, "beethoven-dies", "beethoven-dies-1827", "Ludwig van Beethoven dies", "1827",
   "The German composer dies in Vienna at 56.",
   "On {hd}, the German composer Ludwig van Beethoven died at age 56.",
   "1827: 'German composer Ludwig van Beethoven died of cirrhosis of the liver at age 56.'",
   ["europe"], ["1800-1945"])
ev(3, 26, "gerrymander", "gerrymander-cartoon-1812", "The 'Gerry-mander' cartoon appears in Boston", "1812",
   "A satiric map turns a redrawn district into a beast.",
   "On {hd}, the Boston Gazette published a satiric cartoon that turned a district redrawn to favor incumbents into a fabulous animal, 'The Gerry-mander,' giving the practice its name.",
   "1812: 'In opposition to the redrawing of districts to favour incumbents in an upcoming election, the Boston Gazette published a satiric cartoon that graphically transformed the districts into a fabulous animal, \"The Gerry-mander.\"'",
   ["americas"], ["1800-1945"])
ev(3, 26, "tennessee-williams-born", "tennessee-williams-born-1911", "Tennessee Williams is born", "1911",
   "The American dramatist is born in Columbus, Mississippi.",
   "On {hd}, the American dramatist Tennessee Williams was born in Columbus, Mississippi.",
   "1911: 'American dramatist Tennessee Williams ... was born in Columbus, Mississippi.'",
   ["americas"], ["1800-1945"])
ev(3, 26, "frost-born", "robert-frost-born-1874", "Robert Frost is born in San Francisco", "1874",
   "The poet of rural New England is born.",
   "On {hd}, the American poet Robert Frost, admired for his depictions of rural New England life, was born in San Francisco.",
   "1874: 'American poet Robert Frost, much admired for his depictions of rural New England life and his realistic verse portraying ordinary people, was born in San Francisco.'",
   ["americas"], ["1800-1945"])

# ---------------------------------------------------------------- Mar 27
ev(3, 27, "alaska-earthquake", "great-alaska-earthquake-1964", "A magnitude 9.2 earthquake strikes Alaska", "1964",
   "It is the strongest earthquake ever recorded in the United States.",
   "On {hd}, south-central Alaska was struck by a magnitude 9.2 earthquake, the strongest ever registered in the United States.",
   "1964: 'South-central Alaska was struck by a 9.2-magnitude earthquake that was the strongest quake ever registered in the United States.'",
   ["americas"], ["cold-war"], "The strongest US earthquake",
   "In 1964, a magnitude 9.2 earthquake, the strongest recorded in the US, struck Alaska.")
ev(3, 27, "khrushchev-premier", "khrushchev-becomes-premier-1958", "Nikita Khrushchev becomes Soviet premier", "1958",
   "He replaces Nikolay Bulganin.",
   "On {hd}, Nikita Khrushchev replaced Nikolay Bulganin as premier of the Soviet Union.",
   "1958: 'Nikita Khrushchev replaced Nikolay Bulganin as premier of the Soviet Union.'",
   ["europe"], ["cold-war"])
ev(3, 27, "typhoid-mary", "typhoid-mary-quarantined-1915", "Typhoid Mary is placed in quarantine", "1915",
   "Mary Mallon is sent to North Brother Island.",
   "On {hd}, Mary Mallon, a domestic worker better known as Typhoid Mary, was placed in quarantine on North Brother Island, New York City.",
   "1915: 'American domestic Mary Mallon, better known as Typhoid Mary, was placed under a quarantine on North Brother Island, New York City.'",
   ["americas"], ["1800-1945"])
ev(3, 27, "horseshoe-bend", "battle-of-horseshoe-bend-1814", "Andrew Jackson's troops win the Battle of Horseshoe Bend", "1814",
   "His force of about 3,000 defeats the Creek.",
   "On {hd}, at the Battle of Horseshoe Bend, Andrew Jackson and about 3,000 troops defeated the Creek.",
   "1814: 'At the Battle of Horseshoe Bend ... Andrew Jackson and his 3,000 troops defeated the Creek Indians.'",
   ["americas"], ["1800-1945"])
ev(3, 27, "mies-born", "mies-van-der-rohe-born-1886", "Ludwig Mies van der Rohe is born", "1886",
   "The modernist architect is born.",
   "On {hd}, the German American architect Ludwig Mies van der Rohe was born.",
   "1886: 'German American architect Ludwig Mies van der Rohe ... was born.'",
   ["europe", "americas"], ["1800-1945"])

# ---------------------------------------------------------------- Mar 28
ev(3, 28, "three-mile-island", "three-mile-island-accident-1979", "An accident begins at the Three Mile Island nuclear plant", "1979",
   "A stuck valve starts the worst crisis in US nuclear power history.",
   "On {hd}, an automatic valve mistakenly closed at the Three Mile Island nuclear power plant near Harrisburg, Pennsylvania, beginning the most serious accident in the history of the American nuclear power industry.",
   "1979: 'At 4:00 am an automatic valve mistakenly closed at the Three Mile Island nuclear power plant.'",
   ["americas"], ["cold-war"], "Trouble at Three Mile Island",
   "In 1979, an accident began at the Three Mile Island nuclear plant in Pennsylvania.",
   extra=[art("Three Mile Island accident", "https://www.britannica.com/event/Three-Mile-Island-accident",
              "began 'At 4:00 am on March 28' 1979; plant 'situated in the Susquehanna River near Harrisburg, Pa.'; 'the most serious in the history of the American nuclear power industry'.")])
ev(3, 28, "woolf-dies", "virginia-woolf-dies-1941", "Virginia Woolf dies", "1941",
   "The Modernist novelist dies.",
   "On {hd}, the English novelist Virginia Woolf, whose experiments with the novel changed Modernist literature, died.",
   "1941 (featured biography): 'Virginia Woolf, who died by suicide this day in 1941, altered the course of Modernist literature through her revisionist experiments with novelistic form.'",
   ["europe"], ["1800-1945"])
ev(3, 28, "madrid-falls", "franco-captures-madrid-1939", "Franco's forces take Madrid", "1939",
   "The Spanish capital falls near the end of the civil war.",
   "On {hd}, Francisco Franco's Nationalist forces captured Madrid, the Spanish capital, near the end of the Spanish Civil War.",
   "1939: 'Francisco Franco ... captured the capital city of Madrid.'",
   ["europe"], ["1800-1945"])
ev(3, 28, "eisenhower-dies", "eisenhower-dies-1969", "Dwight D. Eisenhower dies", "1969",
   "The 34th US president dies at 78.",
   "On {hd}, Dwight D. Eisenhower, the 34th president of the United States, died at age 78.",
   "1969: 'Dwight D. Eisenhower, the 34th president of the United States, died at age 78.'",
   ["americas"], ["cold-war"])
ev(3, 28, "vargas-llosa-born", "mario-vargas-llosa-born-1936", "Mario Vargas Llosa is born in Arequipa", "1936",
   "The Peruvian novelist is born.",
   "On {hd}, the Peruvian novelist Mario Vargas Llosa was born in Arequipa, Peru.",
   "1936: 'Mario Vargas Llosa ... was born in Arequipa, Peru.'",
   ["americas"], ["1800-1945"])

# ---------------------------------------------------------------- Mar 29
ev(3, 29, "terra-cotta-army", "terra-cotta-army-found-1974", "Farmers find the first trace of China's terra-cotta army", "1974",
   "A well near Xi'an leads to the tomb of the first Qin emperor.",
   "On {hd}, farmers digging a well near Xi'an, China, found an underground chamber that led to the discovery of the terra-cotta army in the tomb of Emperor Qin Shi Huang: about 8,000 life-size soldiers and horses.",
   "1974: 'Farmers drilling a well near Xi'an, China, found a subterranean chamber that led to the discovery of the terra-cotta army in the tomb of Emperor Qin Shi Huang, consisting of about 8,000 life-size terra-cotta soldiers and horses.'",
   ["asia"], ["cold-war", "ancient"], "An army buried for 2,000 years",
   "In 1974, farmers digging a well near Xi'an found the first sign of the terra-cotta army.")
ev(3, 29, "vesta", "olbers-discovers-vesta-1807", "Wilhelm Olbers discovers Vesta", "1807",
   "He finds the brightest asteroid in the sky.",
   "On {hd}, the German astronomer Wilhelm Olbers discovered the minor planet Vesta, the brightest asteroid in the sky.",
   "1807: 'German astronomer Wilhelm Olbers discovered the minor planet Vesta, the brightest asteroid in the sky.'",
   ["europe"], ["1800-1945"])
ev(3, 29, "king-and-i", "the-king-and-i-opens-1951", "The King and I opens on Broadway", "1951",
   "Rodgers and Hammerstein's musical makes Yul Brynner a star.",
   "On {hd}, the Rodgers and Hammerstein musical The King and I debuted on Broadway, making Yul Brynner a star.",
   "1951: 'The Rodgers and Hammerstein musical The King and I debuted on Broadway; a classic of the stage, it made Yul Brynner a star.'",
   ["americas"], ["cold-war"])
ev(3, 29, "twenty-third-amendment", "twenty-third-amendment-1961", "The Twenty-third Amendment is certified", "1961",
   "Residents of Washington, D.C., gain a vote for president.",
   "On {hd}, the Twenty-third Amendment to the US Constitution was certified, allowing residents of Washington, D.C., to vote in presidential elections.",
   "1961: 'The Twenty-third Amendment was certified, allowing residents of Washington, D.C., to vote in presidential elections.'",
   ["americas"], ["cold-war"])
ev(3, 29, "tyler-born", "john-tyler-born-1790", "John Tyler is born", "1790",
   "The 10th US president is born.",
   "On {hd}, John Tyler, the 10th president of the United States, was born.",
   "1790: 'John Tyler, the 10th president of the United States (1841-45), was born.'",
   ["americas"], ["revolutionary"])

# ---------------------------------------------------------------- Mar 30
ev(3, 30, "alaska-purchase", "alaska-purchase-treaty-signed-1867", "The United States agrees to buy Alaska from Russia", "1867",
   "William H. Seward negotiates the purchase for $7.2 million.",
   "On {hd}, the United States agreed to buy Alaska from Russia for $7.2 million, in a purchase negotiated by Secretary of State William H. Seward.",
   "1867 (featured): 'U.S. purchases Alaska from Russia' - William H. Seward negotiated the purchase of Alaska from Russia for $7.2 million.",
   ["americas", "europe"], ["1800-1945"], "Alaska for $7.2 million",
   "In 1867, the US agreed to buy Alaska from Russia for $7.2 million.")
ev(3, 30, "reagan-shot", "reagan-shot-1981", "President Ronald Reagan is shot", "1981",
   "He is seriously wounded two months after taking office.",
   "On {hd}, US President Ronald Reagan was shot and seriously wounded by John W. Hinckley, Jr., in Washington, D.C., barely two months after his inauguration.",
   "1981: Ronald Reagan was shot and seriously wounded by would-be assassin John W. Hinckley, Jr. in Washington, D.C., barely two months after his inauguration.",
   ["americas"], ["cold-war"])
ev(3, 30, "treaty-of-paris-1856", "treaty-of-paris-ends-crimean-war-1856", "The Treaty of Paris ends the Crimean War", "1856",
   "The peace settlement is signed in the French capital.",
   "On {hd}, the Treaty of Paris was signed, ending the Crimean War.",
   "1856: 'The Treaty of Paris was signed, ending the Crimean War.'",
   ["europe"], ["1800-1945"])
ev(3, 30, "van-gogh-born", "vincent-van-gogh-born-1853", "Vincent van Gogh is born", "1853",
   "The Dutch Post-Impressionist painter is born.",
   "On {hd}, the Dutch Post-Impressionist painter Vincent van Gogh was born.",
   "Famous births: 1853, Vincent van Gogh, Dutch Post-Impressionist painter.",
   ["europe"], ["1800-1945"])
ev(3, 30, "queen-mother-dies", "queen-mother-dies-2002", "Elizabeth, the Queen Mother, dies at 101", "2002",
   "The former queen consort dies at Windsor Castle.",
   "On {hd}, Elizabeth, the Queen Mother, queen consort of the United Kingdom from 1936 to 1952, died in her sleep at Windsor Castle at age 101.",
   "2002: 'Elizabeth, the Queen Mother, who was queen consort of the United Kingdom of Great Britain and Ireland (1936-52), died in her sleep at Windsor Castle at age 101.'",
   ["europe"], ["contemporary"])

# ---------------------------------------------------------------- Mar 31
ev(3, 31, "eiffel-tower", "eiffel-tower-inaugurated-1889", "The Eiffel Tower is inaugurated in Paris", "1889",
   "Gustave Eiffel's 300-meter tower is officially opened.",
   "On {hd}, the 300-meter (984-foot) Eiffel Tower, designed by Gustave Eiffel for the International Exposition of 1889 marking the centenary of the French Revolution, was officially inaugurated in Paris. It opened to the public on May 15.",
   "1889: 'The 984-foot (300-meter) Eiffel Tower was officially inaugurated in Paris.'",
   ["europe"], ["1800-1945"], "Paris gets a 300-meter tower",
   "In 1889, the Eiffel Tower was inaugurated in Paris, ahead of that year's world's fair.",
   extra=[art("Eiffel Tower", "https://www.britannica.com/topic/Eiffel-Tower-Paris-France",
              "built for the 'International Exposition of 1889 to celebrate the centenary of the French Revolution'; constructed 1887-89; 'opened to the public on May 15, 1889'; engineer Gustave Eiffel; 300 meters (984 feet), later 330 meters with antennas.")])
ev(3, 31, "kanagawa", "treaty-of-kanagawa-1854", "Matthew Perry signs the Treaty of Kanagawa with Japan", "1854",
   "The American commodore concludes a treaty with Japan.",
   "On {hd}, US Commodore Matthew Perry signed the Treaty of Kanagawa in Japan.",
   "1854: 'U.S. Commodore Matthew Perry signed the Treaty of Kanagawa in Japan.'",
   ["asia", "americas"], ["1800-1945"])
ev(3, 31, "lbj-withdraws", "johnson-will-not-seek-reelection-1968", "Lyndon B. Johnson announces he will not seek reelection", "1968",
   "The president ends a speech on Vietnam with a surprise.",
   "On {hd}, US President Lyndon B. Johnson ended a televised speech about the Vietnam War by announcing that he would not seek reelection.",
   "1968: 'U.S. President Lyndon B. Johnson ended a televised speech about the Vietnam War by announcing that he would not seek reelection.'",
   ["americas"], ["cold-war"])
ev(3, 31, "oklahoma", "oklahoma-opens-on-broadway-1943", "Oklahoma! opens on Broadway", "1943",
   "Rodgers and Hammerstein's first musical debuts.",
   "On {hd}, Oklahoma!, the first of 11 musicals by composer Richard Rodgers and lyricist Oscar Hammerstein II, debuted on Broadway.",
   "1943: 'Oklahoma! debuted on Broadway. The first of 11 musicals written by the iconic team of composer Richard Rodgers and lyricist Oscar Hammerstein II.'",
   ["americas"], ["1800-1945"])
ev(3, 31, "descartes-born", "rene-descartes-born-1596", "Rene Descartes is born", "1596",
   "The French mathematician and philosopher is born.",
   "On {hd}, the French mathematician, scientist and philosopher Rene Descartes was born.",
   "1596: 'French mathematician, scientist, and philosopher Rene Descartes was born.'",
   ["europe"], ["early-modern"])

# ---------------------------------------------------------------- Apr 1
ev(4, 1, "nunavut", "nunavut-created-1999", "Nunavut becomes a Canadian territory", "1999",
   "The new territory is carved from the Northwest Territories.",
   "On {hd}, Canada's Northwest Territories were divided to create Nunavut, whose name means 'Our Land' in Inuktitut. Its capital is Iqaluit.",
   "1999 (featured): 'Canada's Northwest Territories were divided to create Nunavut.'",
   ["americas"], ["contemporary"], "Canada gains a new territory",
   "In 1999, Nunavut was created from the eastern Northwest Territories.",
   extra=[art("Nunavut", "https://www.britannica.com/place/Nunavut",
              "'Created in 1999 out of the eastern portion of the Northwest Territories'; 'The capital is Iqaluit, at the head of Frobisher Bay on southern Baffin Island'; 'its name means \"Our Land\" in Inuktitut, the language of the Inuit'.")])
ev(4, 1, "apple-founded", "apple-computer-founded-1976", "Apple Computer is founded", "1976",
   "Steve Jobs, Steve Wozniak and Ronald Wayne form the company.",
   "On {hd}, Steve Jobs, Steve Wozniak and Ronald Wayne formed Apple Computer Inc.",
   "1976: 'Steve Jobs, Steve Wozniak, and Ronald Wayne formed Apple Computer Inc.'",
   ["americas"], ["cold-war"])
ev(4, 1, "raf-formed", "royal-air-force-formed-1918", "The Royal Air Force is formed", "1918",
   "Britain creates an independent air force during World War I.",
   "On {hd}, the United Kingdom's Royal Air Force was formed.",
   "1918: 'The United Kingdom's Royal Air Force was formed.'",
   ["europe"], ["1800-1945"])
ev(4, 1, "dutch-marriage", "netherlands-same-sex-marriage-2001", "The Netherlands grants equal marriage rights to same-sex couples", "2001",
   "It is the first country to do so.",
   "On {hd}, the Netherlands became the first country to grant equal marriage rights to same-sex couples.",
   "2001: 'The Netherlands became the first country to grant equal marriage rights to same-sex couples.'",
   ["europe"], ["contemporary"])
ev(4, 1, "okinawa", "battle-of-okinawa-begins-1945", "US troops land on Okinawa", "1945",
   "The landing begins the Battle of Okinawa.",
   "On {hd}, US troops landed on the Japanese island of Okinawa, beginning the Battle of Okinawa in World War II.",
   "1945: 'U.S. troops landed on the Japanese island of Okinawa ... marking the beginning of the Battle of Okinawa.'",
   ["asia", "americas"], ["1800-1945"])

# ---------------------------------------------------------------- Apr 2
ev(4, 2, "falklands", "argentina-seizes-falklands-1982", "Argentine troops seize the Falkland Islands", "1982",
   "The invasion starts the Falkland Islands War with Britain.",
   "On {hd}, Argentine troops seized the Falkland Islands (Islas Malvinas), starting the Falkland Islands War with Britain.",
   "1982: 'Argentine troops seized the Falkland Islands (Islas Malvinas), precipitating the Falkland Islands War with Britain.'",
   ["americas", "europe"], ["cold-war"], "War comes to the Falklands",
   "In 1982, Argentine troops seized the Falkland Islands, starting a war with Britain.")
ev(4, 2, "john-paul-ii-dies", "pope-john-paul-ii-dies-2005", "Pope John Paul II dies", "2005",
   "The pope since 1978 dies in Vatican City.",
   "On {hd}, Pope John Paul II, bishop of Rome and head of the Roman Catholic Church since 1978, died in Vatican City.",
   "2005: Pope John Paul II, 'Bishop of Rome and head of the Roman Catholic Church from 1978,' died in Vatican City.",
   ["europe"], ["contemporary"])
ev(4, 2, "wilson-war-message", "wilson-asks-for-war-1917", "Woodrow Wilson asks Congress to declare war on Germany", "1917",
   "The president seeks US entry into World War I.",
   "On {hd}, US President Woodrow Wilson asked Congress for a declaration of war against Germany.",
   "1917: President Woodrow Wilson 'asked Congress for a declaration of war against Germany.'",
   ["americas", "europe"], ["1800-1945"])
ev(4, 2, "space-odyssey", "2001-a-space-odyssey-premieres-1968", "2001: A Space Odyssey premieres", "1968",
   "Stanley Kubrick's film opens in Washington, D.C.",
   "On {hd}, Stanley Kubrick's film 2001: A Space Odyssey had its world premiere in Washington, D.C.",
   "1968: Stanley Kubrick's '2001: A Space Odyssey had its world premiere in Washington, D.C.'",
   ["americas"], ["cold-war"])
ev(4, 2, "richmond-evacuated", "confederates-evacuate-richmond-1865", "Confederate troops evacuate Richmond", "1865",
   "The Confederate capital is abandoned.",
   "On {hd}, Confederate troops evacuated Richmond, Virginia, the capital of the Confederate States of America.",
   "1865: 'Confederate troops evacuated Richmond, Virginia, the capital of the Confederate States of America.'",
   ["americas"], ["1800-1945"])

# ---------------------------------------------------------------- Apr 3
ev(4, 3, "pony-express", "pony-express-begins-1860", "The Pony Express begins carrying mail", "1860",
   "Relay riders link Missouri and California.",
   "On {hd}, the Pony Express began delivering mail by horse relay between St. Joseph, Missouri, and Sacramento, California, a route of nearly 2,000 miles covered in about 10 days.",
   "1860 (featured): 'the Pony Express launched, setting a new standard for delivering mail.'",
   ["americas"], ["1800-1945"], "Mail at a gallop",
   "In 1860, the Pony Express began relaying mail between Missouri and California.",
   extra=[art("Pony Express", "https://www.britannica.com/topic/Pony-Express",
              "began April 1860; ran from St. Joseph, Missouri, to Sacramento, California; 'nearly 2,000 miles (3,200 km) long overland'; about 10 days; ended October 1861 after the transcontinental telegraph was completed.")])
ev(4, 3, "first-mobile-call", "first-handheld-mobile-call-1973", "The first handheld mobile phone call is made", "1973",
   "A Motorola employee calls Bell Laboratories.",
   "On {hd}, the first handheld mobile telephone call was made by an employee of Motorola, who called AT&T's Bell Laboratories.",
   "1973: 'The first handheld mobile telephone call was made by an employee of Motorola, who called AT&T's Bell Laboratories.'",
   ["americas"], ["cold-war"])
ev(4, 3, "mountaintop", "mlk-mountaintop-speech-1968", "Martin Luther King, Jr., gives his Mountaintop speech", "1968",
   "He speaks in support of striking Memphis sanitation workers.",
   "On {hd}, Martin Luther King, Jr., delivered his 'I've Been to the Mountaintop' speech in Memphis, Tennessee, at an event for the city's striking sanitation workers.",
   "1968: 'Martin Luther King, Jr. delivered his \"Mountaintop Speech\" at an event for the Memphis sanitation workers' strike.'",
   ["americas"], ["cold-war"])
ev(4, 3, "marshall-plan", "marshall-plan-signed-1948", "Harry Truman signs the Marshall Plan into law", "1948",
   "The program aims to revive Europe's economies after World War II.",
   "On {hd}, US President Harry S. Truman signed into law George C. Marshall's plan to revive the economies of western and southern Europe after World War II.",
   "1948: 'U.S. President Harry S. Truman signed into law George C. Marshall's post-World War II plan to revive the economies of western and southern European countries.'",
   ["americas", "europe"], ["cold-war"])
ev(4, 3, "jesse-james", "jesse-james-killed-1882", "Outlaw Jesse James is shot dead", "1882",
   "Robert Ford kills him at his home.",
   "On {hd}, the American outlaw Jesse James was shot and killed by Robert Ford at his home.",
   "1882: 'American outlaw Jesse James was shot and killed by Robert Ford while adjusting a picture on the wall of his home.'",
   ["americas"], ["1800-1945"])

# ---------------------------------------------------------------- Apr 4
ev(4, 4, "mlk-assassinated", "martin-luther-king-jr-assassinated-1968", "Martin Luther King, Jr., is assassinated in Memphis", "1968",
   "The civil rights leader is shot while standing on a balcony.",
   "On {hd}, the civil rights leader Martin Luther King, Jr., was shot and killed while standing on a balcony in Memphis, Tennessee.",
   "1968 (featured): the civil rights leader was shot and killed while standing on his hotel balcony in Memphis, Tennessee.",
   ["americas"], ["cold-war"], "A civil rights leader is killed",
   "In 1968, Martin Luther King, Jr., was assassinated in Memphis, Tennessee.")
ev(4, 4, "nato-signed", "north-atlantic-treaty-signed-1949", "Twelve countries sign the North Atlantic Treaty", "1949",
   "The treaty creates NATO.",
   "On {hd}, twelve countries signed the North Atlantic Treaty, forming the North Atlantic Treaty Organization (NATO).",
   "1949: 'The North Atlantic Treaty Organization was formed' with twelve founding member countries.",
   ["europe", "americas"], ["cold-war"])
ev(4, 4, "microsoft", "microsoft-founded-1975", "Bill Gates and Paul Allen found Microsoft", "1975",
   "The company becomes the largest maker of PC software.",
   "On {hd}, Bill Gates and Paul Allen founded Microsoft, which became the world's largest personal-computer software company.",
   "1975: Bill Gates and Paul Allen 'founded Microsoft,' which became the world's largest personal-computer software company.",
   ["americas"], ["cold-war"])
ev(4, 4, "harrison-dies", "william-henry-harrison-dies-1841", "William Henry Harrison dies after a month in office", "1841",
   "He is the first US president to die in office.",
   "On {hd}, US President William Henry Harrison died of pneumonia one month after taking office, the first US president to die in office.",
   "1841: President William Henry Harrison 'died of pneumonia,' becoming the first U.S. president to die in office after serving one month.",
   ["americas"], ["1800-1945"])
ev(4, 4, "angelou-born", "maya-angelou-born-1928", "Maya Angelou is born in St. Louis", "1928",
   "The author of I Know Why the Caged Bird Sings is born.",
   "On {hd}, Maya Angelou, the poet and author of I Know Why the Caged Bird Sings, was born in St. Louis, Missouri.",
   "1928: Maya Angelou, poet and author of 'I Know Why the Caged Bird Sings,' was born in St. Louis, Missouri.",
   ["americas"], ["1800-1945"])

# ---------------------------------------------------------------- Apr 5
ev(4, 5, "first-veto", "washington-first-veto-1792", "George Washington issues the first presidential veto", "1792",
   "It is the first veto in US history.",
   "On {hd}, George Washington issued the first presidential veto in US history.",
   "1792: 'George Washington issued the first presidential veto in U.S. history.'",
   ["americas"], ["revolutionary"], "The first presidential veto",
   "In 1792, George Washington issued the first presidential veto in US history.")
ev(4, 5, "maipu", "battle-of-maipu-1818", "Chile wins the Battle of Maipu", "1818",
   "The victory over Spain secures Chile's independence movement.",
   "On {hd}, Chile's independence movement won a decisive victory over Spain at the Battle of Maipu.",
   "1818: 'Chile's independence movement ... won a decisive victory over Spain in the Battle of Maipu.'",
   ["americas"], ["revolutionary"])
ev(4, 5, "war-of-the-pacific", "war-of-the-pacific-begins-1879", "Chile declares war on Peru and Bolivia", "1879",
   "The declaration begins the War of the Pacific.",
   "On {hd}, Chile declared war on Peru and Bolivia, beginning the War of the Pacific.",
   "1879: 'Chile declared war on Peru and Bolivia, beginning the War of the Pacific.'",
   ["americas"], ["1800-1945"])
ev(4, 5, "kareem-record", "kareem-abdul-jabbar-scoring-record-1984", "Kareem Abdul-Jabbar becomes the NBA's top scorer", "1984",
   "He passes Wilt Chamberlain's career points total.",
   "On {hd}, Kareem Abdul-Jabbar surpassed Wilt Chamberlain as the all-time leading scorer in the National Basketball Association.",
   "1984: 'Kareem Abdul-Jabbar surpassed Wilt Chamberlain as the all-time leading scorer in the National Basketball Association.'",
   ["americas"], ["cold-war"])
ev(4, 5, "bette-davis-born", "bette-davis-born-1908", "Bette Davis is born", "1908",
   "The film actress is born in Lowell, Massachusetts.",
   "On {hd}, the American film actress Bette Davis was born in Lowell, Massachusetts.",
   "1908: Bette Davis was born in Lowell, Massachusetts.",
   ["americas"], ["1800-1945"])

# ---------------------------------------------------------------- Apr 6
ev(4, 6, "athens-olympics", "first-modern-olympics-open-1896", "The first modern Olympic Games open in Athens", "1896",
   "Athletes from 12 countries gather in Greece.",
   "On {hd}, the first modern Olympic Games opened in Athens, with as many as 280 athletes, all men, from 12 countries.",
   "1896: 'The first modern Olympics opened in Athens.'",
   ["europe", "global"], ["1800-1945"], "The modern Olympics begin",
   "In 1896, the first modern Olympic Games opened in Athens.",
   note="Gregorian date. Greece then used the Julian calendar, in which the Games opened on March 25.",
   extra=[art("Athens 1896 Olympic Games", "https://www.britannica.com/event/Athens-1896-Olympic-Games",
              "'April 6-15, 1896'; 'attended by as many as 280 athletes, all male, from 12 countries'; marathon won by 'Spyridon Louis, a Greek'.")])
ev(4, 6, "abba-waterloo", "abba-wins-eurovision-1974", "ABBA wins Eurovision with 'Waterloo'", "1974",
   "The Swedish group's victory launches its international career.",
   "On {hd}, the Swedish group ABBA won the Eurovision Song Contest with the song 'Waterloo.'",
   "1974 (featured): 'ABBA won Eurovision with their song \"Waterloo.\"'",
   ["europe"], ["cold-war"])
ev(4, 6, "us-enters-wwi", "us-declares-war-on-germany-1917", "The United States declares war on Germany", "1917",
   "The US enters World War I.",
   "On {hd}, the United States declared war on Germany, entering World War I.",
   "1917: 'The United States declared war on Germany, thus entering World War I.'",
   ["americas", "europe"], ["1800-1945"])
ev(4, 6, "shiloh", "battle-of-shiloh-1862", "The Battle of Shiloh begins in Tennessee", "1862",
   "Union and Confederate armies clash in southwestern Tennessee.",
   "On {hd}, Union troops clashed with Confederates at the Battle of Shiloh in southwestern Tennessee.",
   "1862: 'Union troops clashed with Confederates in southwestern Tennessee at the Battle of Shiloh.'",
   ["americas"], ["1800-1945"])
ev(4, 6, "tony-awards", "first-tony-awards-1947", "The first Tony Awards are presented", "1947",
   "Broadway's awards are handed out for the first time.",
   "On {hd}, the Tony Awards were presented for the first time.",
   "1947: 'The Tony Awards were presented for the first time.'",
   ["americas"], ["cold-war"])

# ---------------------------------------------------------------- Apr 7
ev(4, 7, "who-founded", "world-health-organization-established-1948", "The World Health Organization is established", "1948",
   "The UN health agency is born; the date becomes World Health Day.",
   "On {hd}, the World Health Organization, a specialized agency of the United Nations based in Geneva, was formally established. The date is marked each year as World Health Day.",
   "1948: 'The World Health Organization, a specialized agency of the UN, was formally established.'",
   ["global"], ["cold-war"], "A world agency for health",
   "In 1948, the World Health Organization was established; the date is now World Health Day.",
   extra=[art("World Health Organization", "https://www.britannica.com/topic/World-Health-Organization",
              "established April 7, 1948, with headquarters in Geneva; the founding date is celebrated as World Health Day; first director general Brock Chisholm (1948-53).")])
ev(4, 7, "rwanda", "rwanda-genocide-begins-1994", "The Rwandan genocide begins", "1994",
   "Prime Minister Agathe Uwilingiyimana is assassinated.",
   "On {hd}, Rwandan Prime Minister Agathe Uwilingiyimana was assassinated by Hutu soldiers, marking the beginning of the Rwandan genocide.",
   "1994: Rwandan Prime Minister Agathe Uwilingiyimana was assassinated by Hutu soldiers, marking the beginning of the Rwanda genocide of 1994.",
   ["africa"], ["contemporary"])
ev(4, 7, "henry-ford-dies", "henry-ford-dies-1947", "Henry Ford dies", "1947",
   "The pioneer of assembly-line car making dies at 83.",
   "On {hd}, Henry Ford, who pioneered assembly-line methods at the Ford Motor Company, died at age 83.",
   "1947: Henry Ford, who pioneered assembly-line methods at Ford Motor Company, died at age 83.",
   ["americas"], ["cold-war"])
ev(4, 7, "billie-holiday-born", "billie-holiday-born-1915", "Billie Holiday is born in Philadelphia", "1915",
   "One of the greatest jazz singers is born.",
   "On {hd}, Billie Holiday, one of the greatest American jazz singers, was born in Philadelphia.",
   "1915: Billie Holiday, one of the greatest American jazz singers, was born in Philadelphia.",
   ["americas"], ["1800-1945"])
ev(4, 7, "mars-odyssey", "mars-odyssey-launched-2001", "NASA launches 2001 Mars Odyssey", "2001",
   "The spacecraft heads for Mars.",
   "On {hd}, NASA launched the 2001 Mars Odyssey spacecraft, which reached Mars in October and sent back photos and data.",
   "2001: NASA launched the 2001 Mars Odyssey spacecraft, which reached Mars in October and transmitted photos and data back to Earth.",
   ["global"], ["contemporary", "space-age"])

# ---------------------------------------------------------------- Apr 8
ev(4, 8, "aaron-715", "hank-aaron-715th-home-run-1974", "Hank Aaron hits his 715th home run", "1974",
   "He breaks Babe Ruth's career record.",
   "On {hd}, Hank Aaron hit his 715th career home run, breaking Babe Ruth's record, which had stood since 1935.",
   "1974: 'Hank Aaron hit his 715th career home run, breaking Babe Ruth's record, which had stood since 1935.'",
   ["americas"], ["cold-war"], "Hank Aaron passes Babe Ruth",
   "In 1974, Hank Aaron hit his 715th home run, breaking Babe Ruth's record.")
ev(4, 8, "venus-de-milo", "venus-de-milo-found-1820", "The Venus de Milo is found on Melos", "1820",
   "The ancient marble statue turns up in pieces.",
   "On {hd}, the Venus de Milo, an ancient marble statue thought to represent the goddess Aphrodite, was found in pieces on the Aegean island of Melos. It is now in the Louvre.",
   "1820 (featured): 'The Venus de Milo, one of the most famous ancient statues in the world, was found in pieces on this day in 1820 on the Aegean island of Melos.'",
   ["europe"], ["1800-1945", "ancient"],
   extra=[art("Venus de Milo", "https://www.britannica.com/topic/Venus-de-Milo",
              "'found in pieces on the Aegean island of Melos on April 8, 1820, and was subsequently presented to Louis XVIII'; marble, about 150 BCE; 'commonly thought to represent Aphrodite'; in 'the Louvre, where it remains today'.")])
ev(4, 8, "picasso-dies", "pablo-picasso-dies-1973", "Pablo Picasso dies", "1973",
   "One of the 20th century's most influential artists dies at 91.",
   "On {hd}, Pablo Picasso, one of the most influential artists of the 20th century, died in France at age 91.",
   "1973: 'Pablo Picasso, one of the most influential artists of the 20th century, died in France at age 91.'",
   ["europe"], ["cold-war"])
ev(4, 8, "seventeenth-amendment", "seventeenth-amendment-ratified-1913", "The Seventeenth Amendment is ratified", "1913",
   "Voters gain the right to elect US senators directly.",
   "On {hd}, the Seventeenth Amendment to the US Constitution, providing for the direct election of senators by the voters of each state, was ratified.",
   "1913: 'The Seventeenth Amendment, which called for the direct election of U.S. senators by voters of the states, was ratified.'",
   ["americas"], ["1800-1945"])
ev(4, 8, "kofi-annan-born", "kofi-annan-born-1938", "Kofi Annan is born", "1938",
   "The seventh UN secretary-general is born.",
   "On {hd}, Kofi Annan, who became the seventh secretary-general of the United Nations, was born.",
   "Famous births: 1938, Kofi Annan (seventh secretary-general of the United Nations).",
   ["africa", "global"], ["1800-1945"])

# ---------------------------------------------------------------- Apr 9
ev(4, 9, "appomattox", "lee-surrenders-at-appomattox-1865", "Robert E. Lee surrenders at Appomattox Court House", "1865",
   "The Army of Northern Virginia surrenders to Ulysses S. Grant.",
   "On {hd}, Confederate General Robert E. Lee surrendered the Army of Northern Virginia to Union General Ulysses S. Grant at Appomattox Court House, Virginia, effectively ending the American Civil War in Virginia.",
   "1865: 'General Robert E. Lee, commander of the Army of Northern Virginia of the Confederate States of America, signed a treaty of surrender at Appomattox Court House.' (The article below describes the surrender to Grant.)",
   ["americas"], ["1800-1945"], "Lee surrenders to Grant",
   "In 1865, Robert E. Lee surrendered his army to Ulysses S. Grant at Appomattox Court House.",
   extra=[art("Battle of Appomattox Court House", "https://www.britannica.com/event/Battle-of-Appomattox-Court-House",
              "Lee agreed to Gen. Ulysses S. Grant's terms at 'the home of Wilmer McLean' in Appomattox Court House, Virginia; Lee commanded 'the Army of Northern Virginia'; the surrender 'marked the end of the war in Virginia', with fighting elsewhere continuing.")])
ev(4, 9, "marian-anderson", "marian-anderson-lincoln-memorial-1939", "Marian Anderson sings at the Lincoln Memorial", "1939",
   "The contralto performs for an Easter Sunday crowd of 75,000.",
   "On {hd}, the contralto Marian Anderson gave a concert to an Easter Sunday crowd of 75,000 at the Lincoln Memorial in Washington, D.C.",
   "1939: 'Contralto Marian Anderson gave a concert to an Easter Sunday crowd of 75,000 at the Lincoln Memorial.'",
   ["americas"], ["1800-1945"])
ev(4, 9, "churchill-citizen", "churchill-honorary-us-citizen-1963", "Winston Churchill becomes an honorary US citizen", "1963",
   "An act of Congress confers the honor.",
   "On {hd}, an act of the US Congress conferred honorary US citizenship on Sir Winston Churchill.",
   "1963: 'An act of Congress conferred honorary U.S. citizenship on Sir Winston Churchill.'",
   ["americas", "europe"], ["cold-war"])
ev(4, 9, "robeson-born", "paul-robeson-born-1898", "Paul Robeson is born in Princeton", "1898",
   "The singer, actor and activist is born.",
   "On {hd}, Paul Robeson, the singer, actor and political activist, was born in Princeton, New Jersey.",
   "1898: 'Paul Robeson, a celebrated singer, actor, and political activist who was one of the preeminent figures of American theater in the early 20th century, was born in Princeton, New Jersey.'",
   ["americas"], ["1800-1945"])
ev(4, 9, "la-salle-louisiana", "la-salle-claims-louisiana-1682", "La Salle claims the Mississippi basin for France", "1682",
   "He names the region Louisiana.",
   "On {hd}, the French explorer Rene-Robert Cavelier, sieur de La Salle, claimed the Mississippi River basin for France and named it Louisiana.",
   "1682: 'Rene-Robert Cavelier, sieur de La Salle, claimed the Mississippi River basin for France, naming it Louisiana.'",
   ["americas", "europe"], ["early-modern"])

# ---------------------------------------------------------------- Apr 10
ev(4, 10, "good-friday-agreement", "good-friday-agreement-1998", "The Good Friday Agreement is signed", "1998",
   "The peace deal for Northern Ireland is reached.",
   "On {hd}, the Good Friday Agreement (also called the Belfast Agreement), a peace accord for Northern Ireland, was signed.",
   "1998: The Good Friday Agreement was signed. (The Mar 21 page also names it 'the Good Friday Agreement (Belfast Agreement) of 1998'.)",
   ["europe"], ["contemporary"], "A peace deal for Northern Ireland",
   "In 1998, the Good Friday Agreement for peace in Northern Ireland was signed.",
   extra=[day_page(3, 21, "2017: Martin McGuinness 'played an influential role in negotiating the Good Friday Agreement (Belfast Agreement) of 1998'.")])
ev(4, 10, "great-gatsby", "the-great-gatsby-published-1925", "The Great Gatsby is published", "1925",
   "F. Scott Fitzgerald's novel appears.",
   "On {hd}, F. Scott Fitzgerald published his novel The Great Gatsby.",
   "1925: F. Scott Fitzgerald published The Great Gatsby.",
   ["americas"], ["1800-1945"])
ev(4, 10, "zapata", "emiliano-zapata-killed-1919", "Emiliano Zapata is killed in an ambush", "1919",
   "The Mexican revolutionary leader is shot dead.",
   "On {hd}, the Mexican revolutionary leader Emiliano Zapata was ambushed and shot dead.",
   "1919: Emiliano Zapata was ambushed and fatally shot during the Mexican Revolution period.",
   ["americas"], ["1800-1945"])
ev(4, 10, "black-hole-image", "first-black-hole-image-2019", "Astronomers release the first image of a black hole", "2019",
   "The picture shows the shadow of a black hole for the first time.",
   "On {hd}, astronomers released the first image of a black hole.",
   "2019: Astronomers released the first black hole image.",
   ["global"], ["contemporary"])
ev(4, 10, "safety-pin", "safety-pin-patented-1849", "Walter Hunt patents the safety pin", "1849",
   "He later sells the rights for $400.",
   "On {hd}, Walter Hunt patented the safety pin in the United States. He later sold his rights to it for $400.",
   "1849: 'The safety pin was patented by Walter Hunt in the United States; he later sold his rights to the fastener for $400.'",
   ["americas"], ["1800-1945"])

# ---------------------------------------------------------------- Apr 11
ev(4, 11, "apollo-13", "apollo-13-launched-1970", "Apollo 13 is launched toward the Moon", "1970",
   "An oxygen tank failure later forces the crew to abandon the landing.",
   "On {hd}, Apollo 13 was launched from Cape Kennedy, Florida, with James Lovell, John Swigert and Fred Haise. An oxygen tank failure forced them to abandon the Moon landing, and they returned safely on April 17.",
   "1970: 'Apollo 13 was launched from Cape Kennedy (now Cape Canaveral) in Florida.'",
   ["americas"], ["cold-war", "space-age"], "Apollo 13 lifts off",
   "In 1970, Apollo 13 launched for the Moon; an oxygen tank failure later forced a return.",
   extra=[("NASA - Apollo 13", "https://www.nasa.gov/mission/apollo-13/",
           "NASA: launched April 11, 1970; crew James A. Lovell, Jr., John 'Jack' Swigert and Fred Haise; an oxygen tank failure on the third day forced the crew to abort the landing; splashdown April 17, 1970.")])
ev(4, 11, "macarthur-relieved", "truman-relieves-macarthur-1951", "Truman relieves General Douglas MacArthur of command", "1951",
   "The president removes the commander of UN forces in Korea.",
   "On {hd}, US President Harry S. Truman relieved General Douglas MacArthur of his command of United Nations and US forces during the Korean War.",
   "1951: 'U.S. President Harry S. Truman relieved General Douglas MacArthur of his command of United Nations and U.S. forces during the Korean War.'",
   ["americas", "asia"], ["cold-war"])
ev(4, 11, "eichmann-trial", "eichmann-trial-begins-1961", "The trial of Adolf Eichmann begins in Jerusalem", "1961",
   "The Nazi official faces judgment in Israel.",
   "On {hd}, the trial of the Nazi official Adolf Eichmann began in Jerusalem. It ended eight months later with a death sentence.",
   "1961: 'The trial of Nazi leader Adolf Eichmann began in Jerusalem; eight months later it ended with a death sentence.'",
   ["middle-east"], ["cold-war"])
ev(4, 11, "vonnegut-dies", "kurt-vonnegut-dies-2007", "Kurt Vonnegut dies", "2007",
   "The author of Slaughterhouse-Five dies at 84.",
   "On {hd}, Kurt Vonnegut, the author of satirical novels such as Slaughterhouse-Five (1969), died at age 84.",
   "2007: 'Kurt Vonnegut, who is known for such wryly satirical novels as Slaughterhouse-Five (1969), died at age 84.'",
   ["americas"], ["contemporary"])
ev(4, 11, "wiles-born", "andrew-wiles-born-1953", "Andrew Wiles is born in Cambridge", "1953",
   "The mathematician who proved Fermat's last theorem is born.",
   "On {hd}, the English mathematician Andrew Wiles, who devised a proof of Fermat's last theorem, was born in Cambridge.",
   "1953: 'English mathematician Andrew John Wiles, deviser of a proof of Fermat's last theorem, was born in Cambridge.'",
   ["europe"], ["cold-war"])

# ---------------------------------------------------------------- Apr 12
ev(4, 12, "gagarin-flight", "gagarin-first-human-in-space-1961", "Yuri Gagarin becomes the first human in space", "1961",
   "Vostok 1 carries him once around Earth.",
   "On {hd}, the Soviet cosmonaut Yuri Gagarin became the first human in space, making one orbit of Earth in 1 hour 29 minutes aboard Vostok 1.",
   "1961: Russian cosmonaut Yuri Gagarin became the first human in outer space.",
   ["europe", "global"], ["cold-war", "space-age"], "The first human in space",
   "In 1961, Yuri Gagarin orbited Earth aboard Vostok 1, the first human in space.",
   extra=[art("Yuri Gagarin", "https://www.britannica.com/biography/Yuri-Gagarin",
              "born 'March 9, 1934, near Gzhatsk, Russia, U.S.S.R. [now Gagarin, Russia]'; flew Vostok 1 on April 12, 1961, one orbit of Earth in 1 hour 29 minutes; died March 27, 1968, near Moscow.")])
ev(4, 12, "fort-sumter", "fort-sumter-bombarded-1861", "Confederate guns open fire on Fort Sumter", "1861",
   "The attack begins the American Civil War.",
   "On {hd}, Confederate guns opened fire on Fort Sumter in Charleston Harbor, South Carolina, beginning the American Civil War. The Union garrison under Major Robert Anderson surrendered after a 34-hour bombardment.",
   "1861: Fort Sumter came under fire from Confederate guns, initiating the American Civil War.",
   ["americas"], ["1800-1945"],
   extra=[art("Fort Sumter", "https://www.britannica.com/topic/Fort-Sumter",
              "historic site in Charleston, South Carolina; Confederate attack April 12, 1861, 'the first engagement of the American Civil War'; Maj. Robert Anderson surrendered after a 34-hour bombardment.")])
ev(4, 12, "polio-vaccine", "salk-polio-vaccine-announced-1955", "Jonas Salk's polio vaccine is declared safe and effective", "1955",
   "Results of a huge field trial clear the vaccine for use.",
   "On {hd}, the results of a two-year field trial involving nearly two million children were announced, showing Jonas Salk's inactivated polio vaccine to be safe and effective. It was released for use in the United States that day.",
   "1955 (featured): polio vaccine announced as 'safe, effective, and potent', presenting results of a two-year trial involving nearly two million children, 80-90 percent effective in preventing polio.",
   ["americas"], ["cold-war"],
   extra=[art("Jonas Salk", "https://www.britannica.com/biography/Jonas-Salk",
              "'the first safe and effective inactivated polio vaccine (IPV)', a killed-virus vaccine injected by needle; Thomas Francis conducted a mass field trial in 1954; 'On April 12, 1955, the vaccine was released for use in the United States.'")])
ev(4, 12, "shuttle-columbia", "first-space-shuttle-launch-1981", "NASA launches the first space shuttle, Columbia", "1981",
   "The reusable spacecraft makes its first flight.",
   "On {hd}, NASA launched the first space shuttle, Columbia, designed to orbit Earth and carry people and cargo.",
   "1981: NASA launched the first space shuttle, Columbia, designed to orbit Earth and transport people and cargo.",
   ["americas"], ["cold-war", "space-age"])
ev(4, 12, "fdr-dies", "franklin-roosevelt-dies-1945", "Franklin D. Roosevelt dies", "1945",
   "The US president dies at age 63 while in office.",
   "On {hd}, US President Franklin D. Roosevelt died at age 63.",
   "1945: U.S. President Franklin D. Roosevelt died at age 63.",
   ["americas"], ["1800-1945"])


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
