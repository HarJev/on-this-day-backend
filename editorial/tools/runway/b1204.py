from lib import Batch
b = Batch("2026-12-04-10-historical-events")
BR = "Encyclopaedia Britannica - "
def ev(day, short, id, title, year, summary, desc, quote, regions, eras, nt=None, nb=None, note=None):
    b.add(f"12-{day:02d}", short, id, title, year, f"December {day}, {year}", summary, desc,
          [(f"{BR}On This Day: December {day}", f"https://www.britannica.com/on-this-day/December-{day}",
            f"Britannica On This Day (Dec {day}): {quote}")], regions, eras, nt, nb, note)
# Dec 4
ev(4,"mars-pathfinder","mars-pathfinder-launched-1996","Mars Pathfinder launches","1996",
 "NASA sends a lander to explore the Martian surface.",
 "On December 4, 1996, the Mars Pathfinder spacecraft was launched from Cape Canaveral, Florida, to explore the surface of Mars.",
 "1996: 'The Mars Pathfinder spacecraft was launched from Cape Canaveral, Florida, in order to explore the surface of Mars.'",
 ["americas","global"],["contemporary"],"Next stop: the surface of Mars","In 1996, Mars Pathfinder launched to explore the Red Planet's surface.")
ev(4,"ivan-iv","ivan-the-terrible-proclaimed-1533","Three-year-old Ivan IV becomes grand prince of Moscow","1533",
 "The future Ivan the Terrible inherits the throne as a child.",
 "On December 4, 1533, on the death of his father, Grand Prince Vasily III, the three-year-old who would become Ivan the Terrible was proclaimed grand prince of Moscow. His mother ruled in his name until her death in 1538.",
 "'On this day in 1533, the three-year-old who became Ivan the Terrible was proclaimed grand prince of Moscow upon the death of his father, Grand Prince Vasily III, with his mother ruling in Ivan's name until her death in 1538.'",
 ["europe","asia"],["early-modern"],note="Julian calendar date.")
ev(4,"grey-cup","first-grey-cup-1909","The first Grey Cup is awarded","1909",
 "The Toronto Varsity Blues win Canada's amateur football championship.",
 "On December 4, 1909, the Grey Cup was awarded for the first time, as the Toronto Varsity Blues defeated the Toronto Parkdale team in Canada's amateur football championship game.",
 "1909: 'The Grey Cup was first awarded, as the Toronto Varsity Blues defeated the Toronto Parkdale team in Canada's amateur football championship game.'",
 ["americas"],["1800-1945"])
ev(4,"adrian-iv","adrian-iv-elected-pope-1154","Adrian IV becomes the only English pope","1154",
 "An Englishman is elected to the papal throne.",
 "On December 4, 1154, Adrian IV was elected pope, becoming the only Englishman ever to occupy the papal throne.",
 "1154: 'Adrian IV was elected pope, becoming the only Englishman to occupy the papal throne.'",
 ["europe"],["medieval"],note="Julian calendar date.")
# Dec 5
ev(5,"mary-celeste","mary-celeste-found-abandoned-1872","The Mary Celeste is found abandoned","1872",
 "A ship is discovered adrift near the Azores with no one aboard.",
 "On December 5, 1872, the American ship Mary Celeste was found abandoned about 400 nautical miles from the Azores. The fate of the 10 people who had been aboard remains a mystery.",
 "1872: 'The American ship Mary Celeste was found abandoned some 400 nautical miles (740 km) from the Azores, Portugal; the fate of the 10 people aboard remains a mystery.'",
 ["europe","americas"],["1800-1945"],"The ghost ship of the Atlantic","In 1872, the Mary Celeste was found adrift with no one aboard.")
ev(5,"walt-disney","walt-disney-born-1901","Walt Disney is born","1901",
 "The creator of Mickey Mouse is born in Chicago.",
 "On December 5, 1901, Walt Disney was born in Chicago. He pioneered animated cartoon films, created characters such as Mickey Mouse and Donald Duck, and founded what became one of the world's largest entertainment companies.",
 "'Walt Disney, born in Chicago this day in 1901, pioneered animated cartoon films, created such characters as Mickey Mouse and Donald Duck, and founded what is now one of the world's largest entertainment conglomerates.'",
 ["americas"],["1800-1945"])
ev(5,"mozart","mozart-dies-1791","Wolfgang Amadeus Mozart dies","1791",
 "The composer dies in Vienna at 35.",
 "On December 5, 1791, the Austrian composer Wolfgang Amadeus Mozart died in Vienna at the age of 35.",
 "1791: 'Austrian composer Wolfgang Amadeus Mozart died in Vienna at age 35.'",
 ["europe"],["early-modern"])
ev(5,"mandela-dies","nelson-mandela-dies-2013","Nelson Mandela dies","2013",
 "South Africa's first Black president dies at 95.",
 "On December 5, 2013, Nelson Mandela, who helped end South Africa's apartheid system and became the country's first Black president, died at the age of 95.",
 "2013: 'Nelson Mandela, who helped end South Africa's apartheid system of racial segregation and became the country's first Black president, died at age 95.'",
 ["africa"],["contemporary"])
# Dec 6
ev(6,"anglo-irish-treaty","anglo-irish-treaty-1921","The Anglo-Irish Treaty establishes the Irish Free State","1921",
 "The agreement concludes the Irish War of Independence.",
 "On December 6, 1921, representatives of the British government and Irish leaders including Arthur Griffith and Michael Collins signed the Anglo-Irish Treaty, concluding the Irish War of Independence and establishing the Irish Free State.",
 "'Representatives of the British government and Irish leaders Arthur Griffith, Michael Collins, and others signed the Anglo-Irish Treaty this day in 1921, concluding the Irish War of Independence and establishing the Irish Free State.'",
 ["europe"],["1800-1945"],"The treaty that made the Irish Free State","In 1921, the Anglo-Irish Treaty was signed, creating the Irish Free State.")
ev(6,"finland","finland-independence-1917","Finland declares independence from Russia","1917",
 "The declaration follows the Bolshevik Revolution.",
 "On December 6, 1917, following the Bolshevik Revolution, Finland declared itself independent of Russia.",
 "1917: 'Finland declared itself independent of Russia, following the Bolshevik Revolution.'",
 ["europe"],["1800-1945"])
ev(6,"nefertiti","nefertiti-bust-discovered-1912","The bust of Nefertiti is discovered","1912",
 "Excavators at Tell el-Amarna uncover a famous sculpture.",
 "On December 6, 1912, a bust of Nefertiti was discovered during excavations at Tell el-Amarna, Egypt. It later went on display in a Berlin museum, and calls for its repatriation have made it a source of controversy.",
 "1912: 'A bust of Nefertiti was discovered during excavations at Tell el-Amarna, Egypt. The sculpture later went on display in a Berlin museum, and it became a source of controversy as an alleged plundered artifact, provoking calls for its repatriation.'",
 ["africa","middle-east","europe"],["1800-1945"])
ev(6,"ulysses-ruling","ulysses-not-obscene-ruling-1933","A US judge rules Ulysses is not obscene","1933",
 "The landmark decision opens the way for James Joyce's novel.",
 "On December 6, 1933, in a landmark ruling, a US federal judge held that James Joyce's Ulysses was not obscene, allowing greater freedom for literary works.",
 "1933: 'In what was considered a landmark ruling, a U.S. federal judge held that James Joyce's Ulysses was not obscene, thus allowing for greater freedoms in literary works.'",
 ["americas","europe"],["1800-1945"])
# Dec 7
b.add("12-07","apollo-17","apollo-17-launches-1972","Apollo 17, the last crewed Moon mission, launches","1972","December 7, 1972",
 "Eugene Cernan commands the final Apollo flight to the Moon.",
 "On December 7, 1972, Apollo 17 launched on the Apollo program's last lunar landing mission, commanded by Eugene Cernan. It landed in the Moon's Taurus-Littrow Valley and was the first mission to include an astronaut-scientist.",
 [("NASA - Apollo 17","https://www.nasa.gov/mission/apollo-17/","NASA: launch Dec. 7, 1972; 'The Apollo Program's last lunar landing mission, and the first to include an astronaut-scientist, landed in the Moon's Taurus-Littrow Valley'; Cernan commander of Apollo 17."),
  (BR+"On This Day: December 7","https://www.britannica.com/on-this-day/December-7","Britannica On This Day (Dec 7), 1972: 'American astronaut Eugene Andrew Cernan commanded the last crewed flight to the Moon, effectively ending the Apollo program.'")],
 ["americas","global"],["space-age","cold-war"],"The last voyage to the Moon","In 1972, Apollo 17 set off on the last crewed Moon mission.")
ev(7,"pearl-harbor","pearl-harbor-attack-1941","Japan attacks Pearl Harbor","1941",
 "A surprise air raid brings the United States into World War II.",
 "On December 7, 1941, Japanese bombers launched a surprise attack on the US naval base at Pearl Harbor on Oahu, Hawaii, bringing the United States into World War II.",
 "'On this day in 1941, Japanese bombers launched a surprise aerial attack on the U.S. naval base at Pearl Harbor on the island of Oahu, Hawaii, precipitating the entry of the United States into World War II.'",
 ["americas","asia","oceania"],["1800-1945"])
ev(7,"delaware","delaware-ratifies-constitution-1787","Delaware becomes the first state to ratify the Constitution","1787",
 "Delaware is first to approve the new US Constitution.",
 "On December 7, 1787, Delaware became the first state to ratify the US Constitution.",
 "1787: 'Delaware became the first state to ratify the U.S. Constitution.'",
 ["americas"],["revolutionary"])
ev(7,"bernini","bernini-born-1598","Gian Lorenzo Bernini is born","1598",
 "The sculptor-architect who shaped the Baroque is born.",
 "On December 7, 1598, Gian Lorenzo Bernini was born. Perhaps the greatest sculptor-architect of the 17th century, he created the Baroque style and produced masterpieces such as The Ecstasy of St. Teresa.",
 "'Perhaps the greatest sculptor-architect of the 17th century, Italian Gian Lorenzo Bernini, born this day in 1598, created the Baroque style and refined it with such works as his masterpiece, The Ecstasy of St. Teresa.'",
 ["europe"],["early-modern"])
# Dec 8
ev(8,"notre-dame","notre-dame-reopens-2024","Notre-Dame Cathedral reopens","2024",
 "Paris's cathedral welcomes the public again after the 2019 fire.",
 "On December 8, 2024, the restored Notre-Dame Cathedral in Paris reopened to the public, a little more than five years after it was heavily damaged by fire.",
 "2024: 'The restored Notre-Dame Cathedral in Paris reopened to the public, a little more than five years after it had been heavily damaged by fire.'",
 ["europe"],["contemporary"],"Notre-Dame opens its doors again","In 2024, Paris's Notre-Dame reopened five years after the fire.")
ev(8,"spacex-dragon","spacex-dragon-orbit-return-2010","SpaceX's Dragon returns from orbit","2010",
 "A private company recovers a spacecraft from orbit for the first time.",
 "On December 8, 2010, SpaceX became the first commercial company to launch a spacecraft, the Dragon capsule, into orbit and successfully return it to Earth.",
 "2010: 'SpaceX became the first commercial company to release a spacecraft - the Dragon capsule - into orbit and successfully return it to Earth.'",
 ["americas","global"],["contemporary"])
ev(8,"cis","commonwealth-of-independent-states-1991","Russia, Ukraine, and Belarus agree to form the CIS","1991",
 "The Commonwealth of Independent States emerges as the Soviet Union ends.",
 "On December 8, 1991, Russia, Ukraine, and Belarus signed an agreement to form the Commonwealth of Independent States in the wake of the Soviet Union's demise.",
 "1991: 'Russia, Ukraine, and Belarus signed an agreement to form the Commonwealth of Independent States in the wake of the demise of the Soviet Union.'",
 ["europe","asia"],["contemporary"])
ev(8,"diego-rivera","diego-rivera-born-1886","Diego Rivera is born","1886",
 "The Mexican muralist who revived fresco painting is born.",
 "On December 8, 1886, Diego Rivera was born in Mexico. His bold, large-scale murals stimulated a revival of fresco painting in Latin America.",
 "1886: 'Diego Rivera, whose bold large-scale murals stimulated a revival of fresco painting in Latin America, was born in Mexico.'",
 ["americas"],["1800-1945"])
# Dec 9
ev(9,"smallpox-eradicated","smallpox-declared-eradicated-1979","Smallpox is declared eradicated","1979",
 "A global vaccination campaign defeats the disease.",
 "On December 9, 1979, some 10 years after the World Health Organization began a global vaccination program against smallpox, the disease was officially declared eradicated.",
 "1979: 'Some 10 years after the World Health Organization began a global vaccination program against smallpox, the disease was officially declared eradicated.'",
 ["global"],["contemporary"],"A disease wiped off the Earth","In 1979, smallpox was officially declared eradicated.")
ev(9,"tanganyika","tanganyika-independence-1961","Tanganyika becomes independent","1961",
 "Julius Nyerere becomes the first prime minister of the future Tanzania.",
 "On December 9, 1961, Tanganyika became independent, with Julius Nyerere as its first prime minister. In 1964 it united with Zanzibar to form Tanzania.",
 "1961: 'Tanganyika became independent, with Julius Nyerere as its first prime minister, and in 1964 the territory united with the island of Zanzibar to form Tanzania.'",
 ["africa"],["decolonization","cold-war"])
ev(9,"ayacucho","battle-of-ayacucho-1824","Sucre defeats the Spanish at Ayacucho","1824",
 "A decisive revolutionary victory in South America.",
 "On December 9, 1824, revolutionary forces led by Antonio Jose de Sucre defeated the Spanish royal army at the Battle of Ayacucho.",
 "1824: 'Revolutionary forces under the leadership of Venezuelan Antonio Jose de Sucre defeated the Spanish royal army at the Battle of Ayacucho.'",
 ["americas","europe"],["1800-1945"])
ev(9,"traffic-light","first-traffic-light-1868","The world's first traffic light goes up in London","1868",
 "A gas-lit signal near Westminster Bridge lasts only a month.",
 "On December 9, 1868, the world's first traffic light was erected near Westminster Bridge in London. It was removed a month later after a gas leak caused one of its lights to explode.",
 "1868: 'The world's first traffic light was erected near Westminster Bridge in London; however, it was removed a month later after a gas leak caused one of the lights to explode.'",
 ["europe"],["1800-1945"])
# Dec 10
ev(10,"udhr","universal-declaration-of-human-rights-1948","The UN adopts the Universal Declaration of Human Rights","1948",
 "The General Assembly sets out rights for all people.",
 "On December 10, 1948, the United Nations General Assembly adopted the Universal Declaration of Human Rights.",
 "1948: 'The General Assembly of the United Nations adopted the Universal Declaration of Human Rights.'",
 ["global"],["1945-present"],"Rights for every human being","In 1948, the UN adopted the Universal Declaration of Human Rights.")
ev(10,"first-nobels","first-nobel-prizes-awarded-1901","The first Nobel Prizes are awarded","1901",
 "The prizes are presented on the anniversary of Alfred Nobel's death.",
 "On December 10, 1901, the first Nobel Prizes were distributed, on the fifth anniversary of the death of Alfred Nobel, who founded and endowed the awards through his will.",
 "1901: 'The first Nobel Prizes were distributed, marking the fifth anniversary of the death of Alfred Nobel, the Swedish industrialist and inventor of dynamite, who founded and endowed the awards through his will.'",
 ["europe","global"],["1800-1945"])
ev(10,"ada-lovelace","ada-lovelace-born-1815","Ada Lovelace is born","1815",
 "The woman often called the first computer programmer is born in London.",
 "On December 10, 1815, Ada Lovelace, often considered the first computer programmer, was born in what is now London.",
 "1815: 'Ada Lovelace, who is often considered the first computer programmer, was born in what is today London.'",
 ["europe"],["1800-1945"])
ev(10,"emu-war","emu-war-ends-1932","Australia gives up its 'Emu War'","1932",
 "A month-long military campaign against flightless birds ends.",
 "On December 10, 1932, the Australian government officially gave up after a month-long campaign against thousands of emus, an episode known as the Emu War.",
 "'The Australian government officially surrendered on this day in 1932 after a monthlong battle against thousands of large flightless birds.' (The end of the Emu War.)",
 ["oceania"],["1800-1945"])
ev(10,"sa-constitution","south-africa-constitution-signed-1996","Mandela signs South Africa's democratic constitution","1996",
 "The new constitution completes the transition from apartheid.",
 "On December 10, 1996, President Nelson Mandela signed South Africa's new constitution, completing the transition from white minority rule under apartheid to full democracy.",
 "1996: 'President Nelson Mandela signed a new constitution that completed a transition from a long period of white minority rule (apartheid) to full-fledged democracy in South Africa.'",
 ["africa"],["contemporary"])
b.write()
