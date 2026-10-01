"""December 26 to January 31 event drafts (five batches).

Run from the repository root:
    PYTHONPATH=editorial/tools/runway python3 editorial/tools/runway/b1226.py

Most events cite Britannica's dated On This Day page for their day, read on
2026-10-01; the check note records what that page states. The first event
added for a day is featured.
"""
from lib import Batch

BR = "Encyclopaedia Britannica - "
MONTH = {12: "December", 1: "January"}
BATCHES = [
    ("2026-12-26-01-01-historical-events", [(12, d) for d in range(26, 32)] + [(1, 1)]),
    ("2027-01-02-08-historical-events", [(1, d) for d in range(2, 9)]),
    ("2027-01-09-15-historical-events", [(1, d) for d in range(9, 16)]),
    ("2027-01-16-22-historical-events", [(1, d) for d in range(16, 23)]),
    ("2027-01-23-31-historical-events", [(1, d) for d in range(23, 32)]),
]
EVENTS = []


def ev(m, d, short, id, title, year, summary, desc, quote, regions, eras,
       nt=None, nb=None, note=None, extra=None):
    """quote: what the day's Britannica On This Day page states.
    extra: optional replacement source list of (name, url, note)."""
    name = MONTH[m]
    hd = f"{name} {d}, {year}"
    if extra:
        sources = extra
    else:
        sources = [(f"{BR}On This Day: {name} {d}", f"https://www.britannica.com/on-this-day/{name}-{d}",
                    f"Britannica On This Day ({name[:3]} {d}): {quote}")]
    EVENTS.append(((m, d), (f"{m:02d}-{d:02d}", short, id, title, year, hd, summary,
                            desc.replace("{hd}", hd), sources, regions, eras, nt, nb, note)))


J = "Julian calendar date."

# ---------------------------------------------------------------- Dec 26
ev(12, 26, "frisbee-patent", "frisbee-patent-issued-1967", "Wham-O is granted a patent for the Frisbee", "1967",
   "A US patent covers 'a saucer-shaped throwing implement'.",
   "On {hd}, the Wham-O Manufacturing Company was issued a US patent for what it called a saucer-shaped throwing implement, better known as the Frisbee.",
   "1967: 'The Wham-O Manufacturing Company was issued a U.S. patent on this day in 1967 for \"a saucer-shaped throwing implement\".'",
   ["americas"], ["cold-war"], "A patent that took flight", "In 1967, Wham-O was granted a patent for its flying disc, the Frisbee.")
ev(12, 26, "mao-born", "mao-zedong-born-1893", "Mao Zedong is born", "1893",
   "The future first chairman of the People's Republic of China is born.",
   "On {hd}, Mao Zedong was born. A Marxist theorist and revolutionary, he became the first chairman of the People's Republic of China, serving from 1949 to 1959.",
   "1893: 'Mao Zedong, born this day in 1893, was a Marxist theorist, revolutionary, and, from 1949 to 1959, the first chairman of the People's Republic of China.'",
   ["asia"], ["1800-1945"])
ev(12, 26, "indian-ocean-tsunami", "indian-ocean-tsunami-2004", "An earthquake off Sumatra triggers the Indian Ocean tsunami", "2004",
   "A massive undersea earthquake sends a devastating tsunami across the Indian Ocean.",
   "On {hd}, a massive earthquake shook the Indian Ocean floor west of the island of Sumatra, triggering a devastating tsunami that struck coastlines across the region.",
   "2004: 'A massive earthquake shook the Indian Ocean floor west of the island of Sumatra, triggering a devastating tsunami.'",
   ["asia", "africa"], ["contemporary"])
ev(12, 26, "babbage-born", "charles-babbage-born-1791", "Charles Babbage is born in London", "1791",
   "The mathematician and inventor of early calculating machines is born.",
   "On {hd}, the mathematician and inventor Charles Babbage, a pioneer of mechanical computing, was born in London.",
   "1791: 'Mathematician and inventor Charles Babbage ... was born in London.'",
   ["europe"], ["early-modern"])
ev(12, 26, "jack-johnson", "jack-johnson-wins-heavyweight-title-1908", "Jack Johnson wins the world heavyweight title", "1908",
   "He becomes the first Black world heavyweight boxing champion.",
   "On {hd}, Jack Johnson defeated Tommy Burns in Sydney, Australia, to become the first Black fighter to win the world heavyweight boxing championship.",
   "1908: 'Jack Johnson defeated Tommy Burns in Sydney to become the first Black fighter to win the world heavyweight boxing championship.'",
   ["oceania", "americas"], ["1800-1945"])

# ---------------------------------------------------------------- Dec 27
ev(12, 27, "hagia-sophia", "hagia-sophia-consecrated-537", "Hagia Sophia is consecrated in Constantinople", "537",
   "The great domed church becomes the largest in the world.",
   "On {hd}, the Hagia Sophia in Constantinople was consecrated as a church. It was then the largest church in the world.",
   "537: Hagia Sophia was consecrated as an Eastern Orthodox church, becoming the world's largest church at that time.",
   ["europe", "middle-east"], ["medieval"], "A dome that awed an empire",
   "In 537, the Hagia Sophia was consecrated in Constantinople.", note=J)
ev(12, 27, "beagle-sails", "darwin-sails-on-beagle-1831", "Charles Darwin sets sail on HMS Beagle", "1831",
   "The voyage will shape his theory of evolution.",
   "On {hd}, Charles Darwin set sail on HMS Beagle, beginning the voyage during which he would formulate his theory of evolution.",
   "1831: 'Charles Darwin set sail on the HMS Beagle, beginning the voyage on which he would formulate his theory of evolution.'",
   ["europe", "global"], ["1800-1945"])
ev(12, 27, "kepler-born", "johannes-kepler-born-1571", "Johannes Kepler is born", "1571",
   "The astronomer will discover three laws of planetary motion.",
   "On {hd}, the German astronomer Johannes Kepler was born. He discovered three major laws of planetary motion.",
   "1571: German astronomer Johannes Kepler, who discovered three major laws of planetary motion, was born.",
   ["europe"], ["early-modern"], note=J)
ev(12, 27, "indonesia-sovereignty", "dutch-transfer-sovereignty-indonesia-1949", "The Netherlands transfers sovereignty to Indonesia", "1949",
   "Dutch control ends four years after Indonesia declared independence.",
   "On {hd}, the Dutch formally relinquished control over Indonesia, which had declared its independence in 1945.",
   "1949: 'The Dutch formally relinquished control over Indonesia', after its declaration of independence.",
   ["asia", "europe"], ["decolonization"])
ev(12, 27, "bhutto-assassinated", "benazir-bhutto-assassinated-2007", "Benazir Bhutto is assassinated", "2007",
   "Pakistan's former prime minister is killed while campaigning.",
   "On {hd}, Benazir Bhutto, former prime minister of Pakistan, was assassinated in Rawalpindi while campaigning in parliamentary elections.",
   "2007: 'Benazir Bhutto ... was assassinated in Rawalpindi while campaigning for parliamentary elections.'",
   ["asia"], ["contemporary"])

# ---------------------------------------------------------------- Dec 28
ev(12, 28, "lumiere-cinematographe", "lumiere-first-public-film-screening-1895", "The Lumiere brothers' Cinematographe makes its public debut", "1895",
   "Moving pictures are shown to the public at the Grand Cafe in Paris.",
   "On {hd}, the first public demonstration of the Lumiere brothers' Cinematographe took place at the Grand Cafe in Paris, a landmark in the birth of cinema.",
   "1895: 'The first public demonstration of the Cinematographe ... took place at the Grand Cafe in Paris.'",
   ["europe"], ["1800-1945"], "The first night at the movies",
   "In 1895, the Lumiere brothers screened films to the public in Paris.")
ev(12, 28, "westminster-abbey", "westminster-abbey-consecrated-1065", "Westminster Abbey is consecrated", "1065",
   "Edward the Confessor's church opens in London.",
   "On {hd}, Westminster Abbey in London was consecrated and opened by Edward the Confessor.",
   "1065: 'Westminster Abbey ... was consecrated and opened by Edward the Confessor.'",
   ["europe"], ["medieval"], note=J)
ev(12, 28, "indian-national-congress", "indian-national-congress-first-session-1885", "The Indian National Congress meets for the first time", "1885",
   "The party that will lead India's independence movement holds its first session.",
   "On {hd}, the first session of the Indian National Congress convened. The party later led the movement for India's independence.",
   "1885: 'The first session of the Indian National Congress convened.'",
   ["asia"], ["1800-1945"])
ev(12, 28, "markievicz", "constance-markievicz-elected-1918", "Constance Markievicz becomes the first woman elected to the House of Commons", "1918",
   "The Irish revolutionary is elected but does not take her seat.",
   "On {hd}, Constance Markievicz became the first woman elected to the British House of Commons.",
   "1918: 'Constance Markievicz became the first woman elected to the British House of Commons.'",
   ["europe"], ["1800-1945"], note="The general election was held on December 14; results were declared on December 28.")
ev(12, 28, "endangered-species-act", "endangered-species-act-signed-1973", "The US Endangered Species Act is signed", "1973",
   "Richard Nixon signs a landmark wildlife protection law.",
   "On {hd}, US President Richard Nixon signed the Endangered Species Act into law.",
   "1973: 'U.S. President Richard Nixon signed the Endangered Species Act.'",
   ["americas"], ["cold-war"])

# ---------------------------------------------------------------- Dec 29
ev(12, 29, "ireland-constitution", "irish-constitution-in-force-1937", "The Irish Free State becomes Ireland", "1937",
   "A new constitution renames the state.",
   "On {hd}, with the enactment of a new constitution, the Irish Free State became known as Ireland.",
   "1937: 'With the enactment of a new constitution, the Irish Free State became known as Ireland.'",
   ["europe"], ["1800-1945"], "A new name for a nation",
   "In 1937, a new constitution turned the Irish Free State into Ireland.")
ev(12, 29, "texas-annexed", "texas-annexed-1845", "The United States annexes Texas", "1845",
   "The independent Republic of Texas joins the Union.",
   "On {hd}, the US Congress approved the annexation of the independent Republic of Texas by the United States.",
   "1845: 'The U.S. Congress approved the annexation of the independent Republic of Texas by the United States.'",
   ["americas"], ["1800-1945"])
ev(12, 29, "gladstone-born", "william-gladstone-born-1809", "William Gladstone is born in Liverpool", "1809",
   "The future four-time British prime minister is born.",
   "On {hd}, William Ewart Gladstone was born in Liverpool. He served as prime minister of Great Britain four times between 1868 and 1894.",
   "1809: 'William Ewart Gladstone, who served as prime minister of Great Britain four times (1868-74, 1880-85, 1886, 1892-94), was born in Liverpool.'",
   ["europe"], ["1800-1945"])
ev(12, 29, "wounded-knee", "wounded-knee-massacre-1890", "The Wounded Knee Massacre", "1890",
   "US troops kill Lakota people at Wounded Knee Creek in South Dakota.",
   "On {hd}, US Army troops carried out the Wounded Knee Massacre in South Dakota. It was the last major armed conflict between the United States and the Plains peoples.",
   "1890: 'U.S. Army troops carried out the Wounded Knee Massacre, which was the last major armed conflict between the United States and the Plains tribes.'",
   ["americas"], ["1800-1945"])
ev(12, 29, "guinea-ebola-free", "guinea-declared-ebola-free-2015", "Guinea is declared free of Ebola", "2015",
   "The WHO declares the end of transmission two years after the outbreak began.",
   "On {hd}, the World Health Organization declared Guinea free of Ebola, some two years after the deadly disease was reported in the country.",
   "2015: 'Guinea was declared free of ebola by the World Health Organization, some two years after the deadly disease was reported in the country.'",
   ["africa"], ["contemporary"])

# ---------------------------------------------------------------- Dec 30
ev(12, 30, "ussr-established", "soviet-union-established-1922", "The Soviet Union is established", "1922",
   "The USSR is founded with its capital in Moscow.",
   "On {hd}, the Union of Soviet Socialist Republics was established, with its capital in Moscow.",
   "1922: 'The Union of Soviet Socialist Republics was established, with its capital in Moscow.'",
   ["europe", "asia"], ["1800-1945"], "The birth of the USSR",
   "In 1922, the Union of Soviet Socialist Republics was established.")
ev(12, 30, "rizal-executed", "jose-rizal-executed-1896", "Jose Rizal is executed in Manila", "1896",
   "The Philippine national hero is put to death by Spanish authorities.",
   "On {hd}, Jose Rizal, the writer and reformer regarded as a national hero of the Philippines, was publicly executed by the country's Spanish rulers.",
   "1896: 'Jose Rizal ... was publicly executed by the Spanish rulers of the country.'",
   ["asia"], ["1800-1945"])
ev(12, 30, "gadsden-purchase", "gadsden-purchase-signed-1853", "The Gadsden Purchase is signed", "1853",
   "The United States buys nearly 30,000 square miles from Mexico.",
   "On {hd}, the United States acquired nearly 30,000 square miles of northern Mexican territory with the signing of the Gadsden Purchase.",
   "1853: 'The United States acquired nearly 30,000 square miles of northern Mexican territory with the signing of the Gadsden Purchase.'",
   ["americas"], ["1800-1945"])
ev(12, 30, "scott-farthest-south", "scott-sets-farthest-south-record-1902", "Robert Falcon Scott sets a farthest-south record", "1902",
   "His party pushes deeper into Antarctica than anyone before.",
   "On {hd}, the British explorer Robert Falcon Scott set a new record for reaching the farthest point south, on the Ross Ice Shelf.",
   "1902: 'Robert Falcon Scott ... set a new record for reaching the farthest point south, on the Ross Ice Shelf.'",
   ["antarctica", "europe"], ["1800-1945"])
ev(12, 30, "surji-arjungaon", "treaty-of-surji-arjungaon-1803", "The Treaty of Surji-Arjungaon is signed", "1803",
   "A Maratha chief cedes territory and power in India to the British.",
   "On {hd}, the Maratha chief Daulat Rao Sindhia signed the Treaty of Surji-Arjungaon, ceding political power and territory in India to the British.",
   "1803: 'Maratha chief Daulat Rao Sindhia signed the Treaty of Surji-Arjungaon, ceding political power and territory in India to the British.'",
   ["asia", "europe"], ["early-modern"])

# ---------------------------------------------------------------- Dec 31
ev(12, 31, "times-square-ball", "times-square-ball-drop-1907", "The first ball drops in Times Square", "1907",
   "New York City starts a New Year's Eve tradition.",
   "On {hd}, the first ball was dropped at Times Square in New York City to celebrate New Year's Eve, beginning a tradition that continues today.",
   "1907: 'The first ball was dropped at Times Square in New York City to celebrate New Year's Eve on this day in 1907.'",
   ["americas"], ["1800-1945"], "Counting down since 1907",
   "In 1907, the first New Year's Eve ball dropped in Times Square.")
ev(12, 31, "east-india-company", "east-india-company-chartered-1600", "The English East India Company is chartered", "1600",
   "A royal charter launches the company for trade with Asia.",
   "On {hd}, the East India Company, formed to exploit trade with East and Southeast Asia and India, was incorporated by English royal charter.",
   "1600: 'The East India Company, formed for the exploitation of trade with East and Southeast Asia and India, was incorporated by English royal charter.'",
   ["europe", "asia"], ["early-modern"], note=J)
ev(12, 31, "ottawa-capital", "ottawa-named-capital-of-canada-1857", "Queen Victoria names Ottawa the capital of Canada", "1857",
   "The city is chosen as the national capital.",
   "On {hd}, Queen Victoria named Ottawa the national capital of Canada.",
   "1857: 'Queen Victoria named Ottawa the national capital of Canada.'",
   ["americas"], ["1800-1945"])
ev(12, 31, "panama-canal-handover", "panama-canal-handed-to-panama-1999", "The United States hands the Panama Canal to Panama", "1999",
   "Panama takes full control of the canal.",
   "On {hd}, the United States officially handed over control of the Panama Canal to Panama.",
   "1999: 'The United States officially handed over control of the Panama Canal to Panama.'",
   ["americas"], ["contemporary"])
ev(12, 31, "taipei-101", "taipei-101-opens-2004", "Taipei 101 opens as the world's tallest building", "2004",
   "The 508-metre tower opens in Taiwan's capital.",
   "On {hd}, Taipei 101 opened in Taipei, Taiwan. At 1,667 feet (508 metres), it was then the tallest building in the world.",
   "2004: 'Taipei 101 opened in Taipei, Taiwan. At 1,667 feet (508 metres), it was the tallest building in the world.'",
   ["asia"], ["contemporary"])

# ---------------------------------------------------------------- Jan 1
ev(1, 1, "euro-launched", "euro-introduced-1999", "Eleven countries adopt the euro", "1999",
   "Europe's single currency is launched.",
   "On {hd}, eleven European Union countries adopted the euro as their currency.",
   "1999: 'Eleven European Union countries changed their money to the euro on this day in 1999.'",
   ["europe"], ["contemporary"], "A new money for Europe",
   "In 1999, eleven European countries adopted the euro.")
ev(1, 1, "haiti-independence", "haiti-declares-independence-1804", "Haiti declares independence from France", "1804",
   "The Haitian Revolution ends with a new independent nation.",
   "On {hd}, Haiti declared its independence from France, bringing the Haitian Revolution to an end.",
   "1804: 'Haiti declared its independence from France, bringing the Haitian Revolution to an end.'",
   ["americas"], ["revolutionary"])
ev(1, 1, "emancipation-proclamation", "emancipation-proclamation-issued-1863", "Lincoln issues the Emancipation Proclamation", "1863",
   "The order frees enslaved people in the Confederacy.",
   "On {hd}, US President Abraham Lincoln issued the Emancipation Proclamation, which freed enslaved people in the Confederacy.",
   "1863: 'The Emancipation Proclamation, which freed enslaved people in the Confederacy, was issued by U.S. President Abraham Lincoln.'",
   ["americas"], ["1800-1945"])
ev(1, 1, "wto-established", "world-trade-organization-established-1995", "The World Trade Organization is established", "1995",
   "A new global body oversees international trade rules.",
   "On {hd}, the World Trade Organization was formally established.",
   "1995: 'The World Trade Organization was formally established.'",
   ["global"], ["contemporary"])
ev(1, 1, "batista-flees", "batista-flees-cuba-1959", "Fulgencio Batista flees Cuba", "1959",
   "Fidel Castro's rebels topple the dictator's regime.",
   "On {hd}, the dictator Fulgencio Batista fled Cuba after his regime was toppled by rebel forces led by Fidel Castro.",
   "1959: 'Dictator Fulgencio Batista fled Cuba after his regime was toppled by rebel forces led by Fidel Castro.'",
   ["americas"], ["cold-war"])

# ---------------------------------------------------------------- Jan 2
ev(1, 2, "granada", "granada-surrenders-1492", "Ferdinand and Isabella capture Granada", "1492",
   "The fall of the last Muslim kingdom in Spain ends centuries of campaigns.",
   "On {hd}, Ferdinand II and Isabella I, the first monarchs of a unified Spain, captured Granada, ending a centuries-long series of military campaigns on the Iberian Peninsula.",
   "1492: 'Ferdinand II and Isabella I, the first monarchs of unified Spain, captured Granada, ending a centuries-long series of military campaigns.'",
   ["europe"], ["medieval"], "The end of an era in Spain",
   "In 1492, Ferdinand and Isabella captured Granada.", note=J)
ev(1, 2, "port-arthur", "port-arthur-surrenders-1905", "Russia surrenders Port Arthur to Japan", "1905",
   "A major Russian defeat in the Russo-Japanese War.",
   "On {hd}, during the Russo-Japanese War, Russian forces surrendered Port Arthur (later Lushun, China) to the Japanese.",
   "1905: 'Russian forces surrendered Port Arthur (later Lushun, China) to the Japanese in the Russo-Japanese War.'",
   ["asia", "europe"], ["1800-1945"])
ev(1, 2, "stardust-comet", "stardust-collects-comet-dust-2004", "NASA's Stardust collects dust from a comet", "2004",
   "The spacecraft gathers grains from comet Wild 2.",
   "On {hd}, NASA's Stardust spacecraft collected dust grains from the comet Wild 2.",
   "2004: 'NASA's spacecraft Stardust collected dust grains from the comet Wild 2.'",
   ["global"], ["contemporary", "space-age"])
ev(1, 2, "palmer-raids", "palmer-raids-1920", "The Palmer Raids sweep US cities", "1920",
   "Thousands are arrested as suspected radicals.",
   "On {hd}, in the Palmer Raids, thousands of people were arrested in more than 30 US cities, accused of being foreign anarchists, communists, or radical leftists.",
   "1920: thousands of people 'arrested in more than 30 U.S. cities, accused of being foreign anarchists, communists, or radical leftists.'",
   ["americas"], ["1800-1945"])

# ---------------------------------------------------------------- Jan 3
ev(1, 3, "tolkien-born", "jrr-tolkien-born-1892", "J.R.R. Tolkien is born", "1892",
   "The author of The Hobbit and The Lord of the Rings is born.",
   "On {hd}, J.R.R. Tolkien, author of The Hobbit and The Lord of the Rings, was born in Bloemfontein, South Africa.",
   "1892: J.R.R. Tolkien was born.",
   ["europe", "africa"], ["1800-1945"], "Where Middle-earth began",
   "In 1892, J.R.R. Tolkien, author of The Lord of the Rings, was born.",
   extra=[(f"{BR}On This Day: January 3", "https://www.britannica.com/on-this-day/January-3",
           "Britannica On This Day (Jan 3): 1892: J.R.R. Tolkien born."),
          (f"{BR}J.R.R. Tolkien", "https://www.britannica.com/biography/J-R-R-Tolkien",
           "Britannica: 'born January 3, 1892, Bloemfontein, South Africa'; died September 2, 1973, Bournemouth, England.")])
ev(1, 3, "alaska-statehood", "alaska-becomes-49th-state-1959", "Alaska becomes the 49th US state", "1959",
   "The former territory joins the Union.",
   "On {hd}, Alaska became the 49th state of the United States.",
   "1959: 'Alaska became the 49th U.S. state.'",
   ["americas"], ["cold-war"])
ev(1, 3, "change-4", "change-4-lands-far-side-moon-2019", "China's Chang'e 4 lands on the far side of the Moon", "2019",
   "It is the first spacecraft to land on the lunar far side.",
   "On {hd}, China's Chang'e 4 spacecraft, carrying the Yutu-2 rover, landed on the far side of the Moon.",
   "2019: 'Chang'e 4, carrying the Yutu-2 rover, landed on the Moon's far side.'",
   ["asia"], ["contemporary", "space-age"])
ev(1, 3, "luther-excommunicated", "luther-excommunicated-1521", "Pope Leo X excommunicates Martin Luther", "1521",
   "The break between Luther and Rome becomes formal.",
   "On {hd}, Pope Leo X excommunicated Martin Luther.",
   "1521: 'Pope Leo X excommunicated Martin Luther.'",
   ["europe"], ["early-modern"], note=J)
ev(1, 3, "battle-of-princeton", "battle-of-princeton-1777", "Washington wins the Battle of Princeton", "1777",
   "American forces win a battle in New Jersey.",
   "On {hd}, during the American Revolution, the Battle of Princeton was fought in New Jersey.",
   "1777: 'The Battle of Princeton was fought in New Jersey.'",
   ["americas"], ["revolutionary"])

# ---------------------------------------------------------------- Jan 4
ev(1, 4, "braille-born", "louis-braille-born-1809", "Louis Braille is born", "1809",
   "The inventor of the raised-dot writing system is born near Paris.",
   "On {hd}, Louis Braille, who developed the system of printing and writing for the blind that bears his name, was born near Paris.",
   "1809: 'Louis Braille, who developed a system of printing and writing that is extensively used by the blind and that was named for him, was born near Paris.'",
   ["europe"], ["1800-1945"], "Reading by touch",
   "In 1809, Louis Braille, inventor of the braille system, was born.")
ev(1, 4, "burma-independence", "burma-gains-independence-1948", "Burma gains independence from Britain", "1948",
   "The country now called Myanmar becomes independent.",
   "On {hd}, Burma, today called Myanmar, formally gained independence from Great Britain.",
   "1948: 'Burma, today called Myanmar, formally gained independence from Great Britain.'",
   ["asia"], ["decolonization"])
ev(1, 4, "burj-khalifa", "burj-khalifa-opens-2010", "The Burj Khalifa opens in Dubai", "2010",
   "The world's tallest building officially opens.",
   "On {hd}, the Burj Khalifa, the world's tallest building, officially opened in Dubai.",
   "2010: 'Burj Khalifa, the world's tallest building, officially opened in Dubai.'",
   ["middle-east"], ["contemporary"])
ev(1, 4, "camus-dies", "albert-camus-dies-1960", "Albert Camus dies in a car accident", "1960",
   "The novelist and playwright is killed in France.",
   "On {hd}, the novelist and playwright Albert Camus was killed in an automobile accident in France.",
   "1960: 'Novelist and playwright Albert Camus ... was killed in an automobile accident in France.'",
   ["europe"], ["cold-war"])
ev(1, 4, "solomon-northup", "solomon-northup-freed-1853", "Solomon Northup regains his freedom", "1853",
   "A free Black man kidnapped into slavery is legally freed.",
   "On {hd}, Solomon Northup, a free Black man who had been kidnapped in Washington, D.C., and sold into slavery, legally obtained his freedom.",
   "1853: 'Solomon Northup, a free Black man who had been kidnapped in Washington, D.C., and sold into slavery, legally obtained his freedom.'",
   ["americas"], ["1800-1945"])

# ---------------------------------------------------------------- Jan 5
ev(1, 5, "waiting-for-godot", "waiting-for-godot-premieres-1953", "Waiting for Godot is staged for the first time", "1953",
   "Samuel Beckett's play premieres in Paris.",
   "On {hd}, Samuel Beckett's Waiting for Godot was staged for the first time, in Paris.",
   "1953: 'Waiting for Godot ... was staged for the first time, in Paris.'",
   ["europe"], ["cold-war"], "Still waiting, since 1953",
   "In 1953, Beckett's Waiting for Godot premiered in Paris.")
ev(1, 5, "golden-gate", "golden-gate-bridge-construction-begins-1933", "Construction begins on the Golden Gate Bridge", "1933",
   "Work starts on the bridge linking San Francisco and Marin county.",
   "On {hd}, construction began on the Golden Gate Bridge, which connects San Francisco with Marin county.",
   "1933: 'Construction began on the Golden Gate Bridge, which connects San Francisco with Marin county.'",
   ["americas"], ["1800-1945"])
ev(1, 5, "eris-discovered", "eris-discovered-2005", "The dwarf planet Eris is discovered", "2005",
   "Astronomers spot it in images taken two years earlier.",
   "On {hd}, the dwarf planet Eris was discovered in images taken two years earlier at Palomar Observatory.",
   "2005: 'The dwarf planet Eris was discovered in images taken two years earlier at Palomar Observatory.'",
   ["americas", "global"], ["contemporary"])
ev(1, 5, "ford-five-dollar-day", "ford-five-dollar-day-1914", "Henry Ford doubles his workers' pay", "1914",
   "Ford raises daily pay from $2.40 to $5.",
   "On {hd}, Henry Ford raised his workers' pay from $2.40 to $5.00 a day.",
   "1914: 'Henry Ford raised his workers' pay from $2.40 to $5.00 a day.'",
   ["americas"], ["1800-1945"])
ev(1, 5, "nellie-tayloe-ross", "nellie-tayloe-ross-governor-1925", "Nellie Tayloe Ross becomes the first woman US governor", "1925",
   "She takes office in Wyoming.",
   "On {hd}, Nellie Tayloe Ross took office in Wyoming, becoming the first woman governor in the United States.",
   "1925: 'Nellie Tayloe Ross assumed office in Wyoming, making her the first female governor.'",
   ["americas"], ["1800-1945"])

# ---------------------------------------------------------------- Jan 6
ev(1, 6, "four-freedoms", "four-freedoms-speech-1941", "Franklin D. Roosevelt outlines the Four Freedoms", "1941",
   "His State of the Union message names four essential freedoms.",
   "On {hd}, US President Franklin D. Roosevelt outlined his Four Freedoms in his State of the Union message.",
   "1941: 'U.S. President Franklin D. Roosevelt outlined his Four Freedoms in his State of the Union message.'",
   ["americas", "global"], ["1800-1945"], "Four freedoms for the world",
   "In 1941, Franklin D. Roosevelt set out his Four Freedoms.")
ev(1, 6, "anne-of-cleves", "henry-viii-marries-anne-of-cleves-1540", "Henry VIII marries Anne of Cleves", "1540",
   "She becomes the fourth of his six wives.",
   "On {hd}, Henry VIII of England married Anne of Cleves, the fourth of his six wives.",
   "1540: 'Henry VIII of England married Anne of Cleves, the fourth of his six wives.'",
   ["europe"], ["early-modern"], note=J)
ev(1, 6, "britain-recognizes-prc", "britain-recognizes-prc-1950", "Britain recognizes the People's Republic of China", "1950",
   "The UK formally recognizes the new Communist government.",
   "On {hd}, Great Britain announced its recognition of the People's Republic of China.",
   "1950: 'Great Britain announced its recognition of the People's Republic of China.'",
   ["europe", "asia"], ["cold-war"])
ev(1, 6, "max-bruch-born", "max-bruch-born-1838", "Composer Max Bruch is born", "1838",
   "The Romantic composer is born in Cologne.",
   "On {hd}, the composer Max Bruch was born in Cologne, in what is today Germany.",
   "1838: 'Composer Max Bruch ... was born in Cologne, in what is today Germany.'",
   ["europe"], ["1800-1945"])
ev(1, 6, "richard-ii-born", "richard-ii-born-1367", "Richard II of England is born", "1367",
   "The future king will reign from 1377 to 1399.",
   "On {hd}, the future King Richard II of England was born. He reigned from 1377 to 1399.",
   "1367: 'Born this day in 1367, King Richard II of England, an ambitious ruler who reigned from 1377 to 1399.'",
   ["europe"], ["medieval"], note=J)

# ---------------------------------------------------------------- Jan 7
ev(1, 7, "marian-anderson", "marian-anderson-met-debut-1955", "Marian Anderson sings at the Metropolitan Opera", "1955",
   "The contralto performs with the Met for the first time.",
   "On {hd}, the American contralto Marian Anderson first performed with the Metropolitan Opera in New York City, becoming the first Black singer to perform as a member of the company. She sang Ulrica in Verdi's Un ballo in maschera.",
   "1955: 'American contralto Marian Anderson first performed with the Metropolitan Opera in New York City.'",
   ["americas"], ["cold-war"], "A voice that opened the Met",
   "In 1955, Marian Anderson made her Metropolitan Opera debut.",
   extra=[(f"{BR}On This Day: January 7", "https://www.britannica.com/on-this-day/January-7",
           "Britannica On This Day (Jan 7): 1955: 'American contralto Marian Anderson first performed with the Metropolitan Opera in New York City.'"),
          (f"{BR}Marian Anderson", "https://www.britannica.com/biography/Marian-Anderson",
           "Britannica: 'On January 7, 1955, Anderson became the first Black singer to perform as a member of the Metropolitan Opera'; role of Ulrica in Verdi's Un ballo in maschera; 1939 Lincoln Memorial concert for 75,000 after the DAR barred her from Constitution Hall.")])
ev(1, 7, "phnom-penh", "vietnamese-forces-take-phnom-penh-1979", "Vietnamese forces take Phnom Penh", "1979",
   "The Khmer Rouge regime of Pol Pot is driven from power.",
   "On {hd}, Vietnamese forces took control of Phnom Penh, Cambodia, removing the Khmer Rouge and Pol Pot from power.",
   "1979: Vietnamese forces took control of Phnom Penh, Cambodia, removing the Khmer Rouge and Pol Pot from power.",
   ["asia"], ["cold-war"])
ev(1, 7, "surveyor-7", "surveyor-7-launched-1968", "Surveyor 7 launches toward the Moon", "1968",
   "The uncrewed probe will make a soft lunar landing.",
   "On {hd}, the uncrewed US space probe Surveyor 7 was launched; it went on to make a soft landing on the Moon.",
   "1968: 'Uncrewed U.S. space probe Surveyor 7 was launched' and subsequently made a soft landing on the Moon.",
   ["americas"], ["space-age", "cold-war"])
ev(1, 7, "egypt-christmas", "egypt-christmas-national-holiday-2003", "Egypt marks Christmas as a national holiday", "2003",
   "Coptic Christmas becomes a public holiday for the first time.",
   "On {hd}, Christmas was celebrated as a national holiday in Egypt for the first time.",
   "2003: Christmas was celebrated as a national holiday in Egypt for the first time.",
   ["africa", "middle-east"], ["contemporary"])
ev(1, 7, "kufuor-ghana", "kufuor-inaugurated-ghana-2001", "John Kufuor is inaugurated in Ghana", "2001",
   "Ghana completes its first peaceful transfer of power between governments.",
   "On {hd}, John Kufuor was inaugurated as president of Ghana in the country's first peaceful transition of government.",
   "2001: John Kufuor was inaugurated as president of Ghana in the country's first peaceful government transition.",
   ["africa"], ["contemporary"])

# ---------------------------------------------------------------- Jan 8
ev(1, 8, "anc-founded", "african-national-congress-founded-1912", "The African National Congress is founded", "1912",
   "The movement that will lead the struggle against apartheid is born.",
   "On {hd}, the African National Congress was founded in South Africa.",
   "1912: 'The African National Congress was founded.'",
   ["africa"], ["1800-1945"], "A movement is founded",
   "In 1912, the African National Congress was founded in South Africa.")
ev(1, 8, "fourteen-points", "wilson-fourteen-points-1918", "Woodrow Wilson announces his Fourteen Points", "1918",
   "The US president sets out his plan for peace after World War I.",
   "On {hd}, US President Woodrow Wilson announced his Fourteen Points, a program for peace after World War I.",
   "1918: 'U.S. President Woodrow Wilson announced his Fourteen Points.'",
   ["americas", "europe"], ["1800-1945"])
ev(1, 8, "de-gaulle-fifth-republic", "de-gaulle-inaugurated-fifth-republic-1959", "Charles de Gaulle becomes president of the Fifth Republic", "1959",
   "France's new constitutional order gets its first president.",
   "On {hd}, Charles de Gaulle was inaugurated as president of France's Fifth Republic.",
   "1959: 'Charles de Gaulle was inaugurated as president of France's Fifth Republic.'",
   ["europe"], ["cold-war"])
ev(1, 8, "hawking-born", "stephen-hawking-born-1942", "Stephen Hawking is born", "1942",
   "The physicist known for his work on black holes is born.",
   "On {hd}, the physicist Stephen Hawking was born. He developed a theory of exploding black holes.",
   "1942: Stephen Hawking born; 'developed a theory of exploding black holes'.",
   ["europe"], ["1800-1945"])
ev(1, 8, "elvis-born", "elvis-presley-born-1935", "Elvis Presley is born", "1935",
   "The future 'King of Rock and Roll' is born.",
   "On {hd}, Elvis Presley, widely known as the King of Rock and Roll, was born.",
   "1935: Elvis Presley born; 'widely known as the \"King of Rock and Roll\"'.",
   ["americas"], ["1800-1945"])

# ---------------------------------------------------------------- Jan 9
ev(1, 9, "joan-trial", "joan-of-arc-trial-begins-1431", "The trial of Joan of Arc begins", "1431",
   "The 19-year-old faces more than 70 accusations.",
   "On {hd}, Joan of Arc, then 19 years old, was put on trial in Rouen, France, facing more than 70 accusations. Two years earlier she had led the French to victory at Orleans.",
   "1431: 'Joan the Maid,' age 19, 'was put on trial in France on this day in 1431. She faced more than 70 accusations.'",
   ["europe"], ["medieval"], "Joan of Arc goes on trial",
   "In 1431, the trial of Joan of Arc began in Rouen.", note=J,
   extra=[(f"{BR}On This Day: January 9", "https://www.britannica.com/on-this-day/January-9",
           "Britannica On This Day (Jan 9): 1431: 'Joan the Maid,' age 19, 'was put on trial in France on this day in 1431. She faced more than 70 accusations.'"),
          (f"{BR}Saint Joan of Arc", "https://www.britannica.com/biography/Saint-Joan-of-Arc",
           "Britannica: byname the Maid of Orleans, after the 1429 relief of Orleans; 'a prolonged ecclesiastical trial that began on January 9, 1431, in Rouen'; executed May 30, 1431.")])
ev(1, 9, "beauvoir-born", "simone-de-beauvoir-born-1908", "Simone de Beauvoir is born in Paris", "1908",
   "The writer and feminist thinker is born.",
   "On {hd}, Simone de Beauvoir, a writer and feminist who gave literary form to the themes of existentialism, was born in Paris.",
   "1908: Simone de Beauvoir, 'a writer and feminist who gave a literary transcription to the themes of existentialism, was born in Paris.'",
   ["europe"], ["1800-1945"])
ev(1, 9, "abbas-elected", "mahmoud-abbas-elected-2005", "Mahmoud Abbas is elected Palestinian Authority president", "2005",
   "He succeeds Yasser Arafat as leader of the Palestinian Authority.",
   "On {hd}, Mahmoud Abbas was elected president of the Palestinian Authority.",
   "2005: 'Mahmoud Abbas was elected president of the Palestinian Authority.'",
   ["middle-east"], ["contemporary"])
ev(1, 9, "itunes", "apple-introduces-itunes-2001", "Apple introduces iTunes", "2001",
   "The digital media player changes how people buy and play music.",
   "On {hd}, Apple introduced iTunes, a digital media player application that went on to revolutionize digital music.",
   "2001: 'Apple introduced iTunes, a digital media player application that ... revolutionized digital music.'",
   ["americas", "global"], ["contemporary"])

# ---------------------------------------------------------------- Jan 10
ev(1, 10, "rubicon", "caesar-crosses-rubicon-49bce", "Julius Caesar crosses the Rubicon", "49 BCE",
   "Caesar leads his army into Italy, starting a civil war.",
   "On {hd}, Julius Caesar and his army crossed the Rubicon to take control of the Roman Republic, a step that began a civil war.",
   "49 BCE: 'Julius Caesar and his army crossed the Rubicon on this day in 49 bce to take control of the Roman Republic.'",
   ["europe"], ["ancient"], "Crossing the Rubicon",
   "In 49 BCE, Julius Caesar led his army across the Rubicon.",
   note="Traditional date in the pre-Julian Roman calendar.")
ev(1, 10, "common-sense", "common-sense-published-1776", "Thomas Paine publishes Common Sense", "1776",
   "The pamphlet calls for American independence.",
   "On {hd}, Thomas Paine published Common Sense, a 50-page pamphlet that sold more than 500,000 copies within a few months and called for a war of independence.",
   "1776: 'Thomas Paine published Common Sense, a 50-page pamphlet that sold more than 500,000 copies within a few months and called for a war of independence that would become the American Revolution.'",
   ["americas"], ["revolutionary"])
ev(1, 10, "league-established", "league-of-nations-established-1920", "The League of Nations is established", "1920",
   "The first international peacekeeping organization comes into being.",
   "On {hd}, the League of Nations was established, with its headquarters in Geneva.",
   "1920: 'The League of Nations was established in Geneva.'",
   ["europe", "global"], ["1800-1945"])
ev(1, 10, "un-general-assembly", "first-un-general-assembly-1946", "The first UN General Assembly meets in London", "1946",
   "Delegates of the new United Nations gather for the first time.",
   "On {hd}, the first United Nations General Assembly met in London.",
   "1946: 'The first United Nations General Assembly met in London.'",
   ["europe", "global"], ["1945-present"])
ev(1, 10, "moon-radar", "radar-echo-from-moon-1946", "Radar signals are bounced off the Moon", "1946",
   "Echoes from the Moon are detected for the first time.",
   "On {hd}, radar signals bouncing off the Moon were detected for the first time.",
   "1946: 'Radar signals bouncing off the Moon were detected for the first time.'",
   ["americas"], ["1945-present"])

# ---------------------------------------------------------------- Jan 11
ev(1, 11, "ozymandias", "ozymandias-published-1818", "Shelley's 'Ozymandias' is published", "1818",
   "The sonnet about a ruined statue appears in print for the first time.",
   "On {hd}, Percy Bysshe Shelley's sonnet 'Ozymandias' was published for the first time.",
   "1818: 'Percy Bysshe Shelley's sonnet \"Ozymandias\" was published for the first time.'",
   ["europe"], ["1800-1945"], "Look on my works, ye Mighty",
   "In 1818, Shelley's sonnet Ozymandias was first published.")
ev(1, 11, "smoking-report", "surgeon-general-smoking-report-1964", "The US surgeon general links smoking to lung cancer", "1964",
   "A landmark report changes public views of cigarettes.",
   "On {hd}, US Surgeon General Luther L. Terry announced that cigarette smoking is linked to lung cancer.",
   "1964: 'U.S. Surgeon General Luther L. Terry announced that cigarette smoking is linked to lung cancer.'",
   ["americas"], ["cold-war"])
ev(1, 11, "earhart-pacific", "earhart-hawaii-to-california-1935", "Amelia Earhart flies solo from Hawaii to California", "1935",
   "She completes the first successful solo flight of about 2,400 miles.",
   "On {hd}, Amelia Earhart set off on the first successful solo flight from Hawaii to California, a distance of about 2,400 miles.",
   "1935: 'Amelia Earhart made the first successful solo flight from Hawaii to California, a distance of about 2,400 miles.'",
   ["americas", "oceania"], ["1800-1945"], note="She left Honolulu on January 11 and landed in Oakland the next day.",
   extra=[(f"{BR}On This Day: January 11", "https://www.britannica.com/on-this-day/January-11",
           "Britannica On This Day (Jan 11): 1935: 'Amelia Earhart made the first successful solo flight from Hawaii to California, a distance of about 2,400 miles.'"),
          (f"{BR}Amelia Earhart", "https://www.britannica.com/biography/Amelia-Earhart",
           "Britannica: 'She departed from Honolulu on January 11 and, after 17 hours and 7 minutes, landed in Oakland the following day'; earlier crossed the Atlantic alone on May 20-21, 1932.")])
ev(1, 11, "hillary-dies", "edmund-hillary-dies-2008", "Sir Edmund Hillary dies", "2008",
   "The New Zealand mountaineer dies at 88.",
   "On {hd}, Sir Edmund Hillary, the New Zealand mountaineer, died at age 88.",
   "2008: 'Sir Edmund Hillary died at age 88.'",
   ["oceania", "asia"], ["contemporary"])
ev(1, 11, "chretien-born", "jean-chretien-born-1934", "Jean Chretien is born", "1934",
   "The future prime minister of Canada is born.",
   "On {hd}, Jean Chretien was born. He served as prime minister of Canada from 1993 to 2003.",
   "1934: Jean Chretien born this day; 'served as prime minister of Canada from 1993 to 2003'.",
   ["americas"], ["1800-1945"])

# ---------------------------------------------------------------- Jan 12
ev(1, 12, "tamla-records", "berry-gordy-launches-tamla-records-1959", "Berry Gordy launches Tamla Records", "1959",
   "Gordy starts the Detroit record business that becomes Motown.",
   "On {hd}, Berry Gordy launched Tamla Records, a major step toward transforming American music.",
   "1959: 'Berry Gordy took a major step toward transforming American music on this day in 1959, when he launched Tamla Records.'",
   ["americas"], ["cold-war"], "The start of the Motown sound",
   "In 1959, Berry Gordy launched Tamla Records.",
   extra=[(f"{BR}On This Day: January 12", "https://www.britannica.com/on-this-day/January-12",
           "Britannica On This Day (Jan 12): 1959: 'Berry Gordy took a major step toward transforming American music on this day in 1959, when he launched Tamla Records.'"),
          (f"{BR}Motown", "https://www.britannica.com/topic/Motown",
           "Britannica: Motown was 'founded by Berry Gordy, Jr., in Detroit in January 1959'.")])
ev(1, 12, "haiti-earthquake", "haiti-earthquake-2010", "A magnitude-7.0 earthquake strikes Haiti", "2010",
   "The quake devastates Port-au-Prince and the surrounding region.",
   "On {hd}, a magnitude-7.0 earthquake devastated Haiti, killing more than 300,000 people.",
   "2010: 'A magnitude-7.0 earthquake devastated Haiti, killing more than 300,000 people.'",
   ["americas"], ["contemporary"])
ev(1, 12, "deep-impact", "deep-impact-launched-2005", "NASA launches Deep Impact", "2005",
   "The probe is sent to study comet Tempel 1.",
   "On {hd}, the US space probe Deep Impact was launched to study comet Tempel 1.",
   "2005: 'The U.S. space probe Deep Impact was launched' to study comet Tempel 1.",
   ["americas"], ["contemporary", "space-age"])
ev(1, 12, "kenya-emergency-ends", "kenya-emergency-ends-1960", "Britain ends the state of emergency in Kenya", "1960",
   "The emergency declared during the Mau Mau Rebellion is lifted.",
   "On {hd}, the British government ended the state of emergency it had declared in Kenya during the Mau Mau Rebellion.",
   "1960: British government ended Kenya emergency declaration following Mau Mau Rebellion.",
   ["africa", "europe"], ["decolonization"])
ev(1, 12, "hattie-caraway", "hattie-caraway-elected-senate-1932", "Hattie Caraway is elected to the US Senate", "1932",
   "She becomes the first woman elected to the Senate.",
   "On {hd}, Hattie Ophelia Caraway became the first woman elected to the US Senate.",
   "1932: 'Hattie Ophelia Caraway became the first woman elected to the U.S. Senate.'",
   ["americas"], ["1800-1945"])

# ---------------------------------------------------------------- Jan 13
ev(1, 13, "jaccuse", "zola-publishes-jaccuse-1898", "Emile Zola publishes 'J'accuse'", "1898",
   "His open letter denounces the army over the Dreyfus affair.",
   "On {hd}, the novelist Emile Zola published an open letter titled 'J'accuse', denouncing the French general staff for its role in the 1894 treason court-martial of Alfred Dreyfus.",
   "1898: 'Novelist and critic Emile Zola published an open letter titled \"J'accuse,\" denouncing the French general staff for its role in the 1894 treason court-martial of Alfred Dreyfus.'",
   ["europe"], ["1800-1945"], "Two words that shook France",
   "In 1898, Emile Zola published J'accuse over the Dreyfus affair.")
ev(1, 13, "folsom", "johnny-cash-at-folsom-prison-1968", "Johnny Cash records live at Folsom Prison", "1968",
   "He performs for about 2,000 inmates.",
   "On {hd}, Johnny Cash recorded the album Johnny Cash at Folsom Prison in front of an audience of some 2,000 inmates.",
   "1968: 'Johnny Cash recorded the album Johnny Cash at Folsom Prison in front of an audience of some 2,000 inmates.'",
   ["americas"], ["cold-war"])
ev(1, 13, "douglas-wilder", "douglas-wilder-sworn-in-1990", "Douglas Wilder is sworn in as governor of Virginia", "1990",
   "He is the first popularly elected African American governor in the US.",
   "On {hd}, Douglas Wilder was sworn in as governor of Virginia, the first popularly elected African American governor in the United States.",
   "1990: 'Douglas Wilder was sworn in as the governor of Virginia, making him the first popularly elected African American governor in the United States.'",
   ["americas"], ["contemporary"])
ev(1, 13, "costa-concordia", "costa-concordia-capsizes-2012", "The Costa Concordia runs aground off Italy", "2012",
   "The cruise ship capsizes off Giglio Island.",
   "On {hd}, the cruise ship Costa Concordia, carrying some 4,200 people, ran aground and capsized off Giglio Island in Italy. Thirty-two people died.",
   "2012: 'The Costa Concordia, a cruise ship carrying some 4,200 people, ran aground and capsized off Giglio Island in Italy. Thirty-two people died.'",
   ["europe"], ["contemporary"])

# ---------------------------------------------------------------- Jan 14
ev(1, 14, "huygens-titan", "huygens-lands-on-titan-2005", "The Huygens probe lands on Titan", "2005",
   "A spacecraft touches down on Saturn's largest moon.",
   "On {hd}, the Huygens probe landed on Titan, Saturn's largest moon, the first landing on a world in the outer solar system.",
   "2005: 'The Huygens probe ... landed on Saturn's largest moon, Titan.'",
   ["europe", "global"], ["contemporary", "space-age"], "Touchdown on Titan",
   "In 2005, the Huygens probe landed on Saturn's largest moon, Titan.",
   extra=[(f"{BR}On This Day: January 14", "https://www.britannica.com/on-this-day/January-14",
           "Britannica On This Day (Jan 14): 2005: 'The Huygens probe ... landed on Saturn's largest moon, Titan.'"),
          ("NASA Science - Huygens probe", "https://science.nasa.gov/mission/cassini/spacecraft/huygens-probe/",
           "NASA: 'The Huygens probe successfully landed on Saturn's largest moon Titan at about 11:30 UTC on January 14, 2005'; 'the first - and, so far, the only - landing in the outer solar system'.")])
ev(1, 14, "tosca", "tosca-premieres-1900", "Puccini's Tosca premieres in Rome", "1900",
   "The opera opens at the Teatro Costanzi.",
   "On {hd}, Giacomo Puccini's opera Tosca had its world premiere at Rome's Costanzi Theatre.",
   "1900: 'The opera Tosca ... made its world premiere in Rome's Costanzi Theatre.'",
   ["europe"], ["1800-1945"])
ev(1, 14, "ben-ali", "ben-ali-steps-down-2011", "Tunisia's President Ben Ali steps down", "2011",
   "Mass protests force him from power.",
   "On {hd}, Tunisian President Zine al-Abidine Ben Ali stepped down following mass protests in what became known as the Jasmine Revolution.",
   "2011: Tunisian President Zine al-Abidine Ben Ali stepped down following mass protests in the Jasmine Revolution.",
   ["africa", "middle-east"], ["contemporary"])
ev(1, 14, "treaty-of-paris-ratified", "congress-ratifies-treaty-of-paris-1784", "Congress ratifies the Treaty of Paris", "1784",
   "The peace formally ends the American Revolution.",
   "On {hd}, the Continental Congress ratified the Peace of Paris, formally ending the American Revolution.",
   "1784: Continental Congress ratified the Peace of Paris, formally ending the American Revolution.",
   ["americas", "europe"], ["revolutionary"])
ev(1, 14, "margrethe-abdicates", "margrethe-ii-abdicates-2024", "Queen Margrethe II of Denmark abdicates", "2024",
   "Frederik X becomes king after her 52-year reign.",
   "On {hd}, Queen Margrethe II of Denmark abdicated after 52 years on the throne, and Crown Prince Frederik became King Frederik X.",
   "2024: Queen Margrethe II of Denmark abdicated after 52 years; Crown Prince Frederik became King Frederik X.",
   ["europe"], ["contemporary"])

# ---------------------------------------------------------------- Jan 15
ev(1, 15, "british-museum", "british-museum-opens-1759", "The British Museum opens to the public", "1759",
   "London's museum welcomes visitors six years after its founding act.",
   "On {hd}, the British Museum opened to the public in London. It had been established by an act of Parliament in 1753.",
   "1759: 'The British Museum opened to the public in London. It had been established by an act of Parliament in 1753.'",
   ["europe"], ["early-modern"], "A museum for everyone",
   "In 1759, the British Museum opened its doors to the public.")
ev(1, 15, "wikipedia", "wikipedia-launches-2001", "Wikipedia launches", "2001",
   "The free online encyclopedia goes live.",
   "On {hd}, Wikipedia, a free Internet-based encyclopedia with an open-source management style, debuted.",
   "2001: 'Wikipedia, a free Internet-based encyclopaedia that operates under an open-source management style, debuted.'",
   ["global"], ["contemporary"])
ev(1, 15, "molasses-flood", "boston-molasses-flood-1919", "The Great Molasses Flood hits Boston", "1919",
   "A tank collapse sends over two million gallons through the North End.",
   "On {hd}, a storage tank collapsed in Boston, sending more than two million gallons of molasses flowing through the city's North End.",
   "1919: 'A storage tank collapsed in Boston, sending more than two million gallons of molasses flowing through the city's North End.'",
   ["americas"], ["1800-1945"])
ev(1, 15, "hudson-landing", "us-airways-1549-hudson-landing-2009", "US Airways Flight 1549 lands on the Hudson River", "2009",
   "Captain Chesley Sullenberger brings the airliner down on the river.",
   "On {hd}, US Airways Flight 1549, piloted by Captain Chesley ('Sully') Sullenberger, landed in the Hudson River in New York.",
   "2009: 'US Airways flight 1549, piloted by Captain Chesley (\"Sully\") Sullenberger III, landed in the Hudson River.'",
   ["americas"], ["contemporary"])
ev(1, 15, "rosa-luxemburg", "rosa-luxemburg-murdered-1919", "Rosa Luxemburg is murdered in Berlin", "1919",
   "The revolutionary is killed after the Spartacist uprising.",
   "On {hd}, the revolutionary Rosa Luxemburg was arrested and murdered in Berlin, accused of fomenting the uprising known as the Spartacus Revolt.",
   "1919: 'Rosa Luxemburg was arrested and murdered in Berlin for fomenting an uprising known as the Spartacus Revolt.'",
   ["europe"], ["1800-1945"])

# ---------------------------------------------------------------- Jan 16
ev(1, 16, "sirleaf", "ellen-johnson-sirleaf-sworn-in-2006", "Ellen Johnson Sirleaf becomes president of Liberia", "2006",
   "She is the first woman elected head of state of an African country.",
   "On {hd}, Ellen Johnson Sirleaf, the first woman elected head of state of an African country, was sworn in as president of Liberia.",
   "2006: 'Ellen Johnson Sirleaf, the first woman to be elected head of state of an African country, was sworn in as president of Liberia.'",
   ["africa"], ["contemporary"], "A first for Africa",
   "In 2006, Ellen Johnson Sirleaf was sworn in as president of Liberia.")
ev(1, 16, "chapultepec", "el-salvador-peace-accords-1992", "El Salvador's civil war ends with the Chapultepec Peace Accords", "1992",
   "The government and the FMLN sign peace in Mexico City.",
   "On {hd}, the civil war in El Salvador ended as the government and the Farabundo Marti National Liberation Front signed the Chapultepec Peace Accords in Mexico City.",
   "1992: 'The civil war in El Salvador ended as the government and the Farabundo Marti National Liberation Front signed the Chapultepec Peace Accords in Mexico City.'",
   ["americas"], ["contemporary"])
ev(1, 16, "nasa-women", "nasa-selects-first-women-astronauts-1978", "NASA names its first women astronauts", "1978",
   "The new astronaut class includes six women.",
   "On {hd}, NASA announced its newest class of astronauts, which for the first time included six women.",
   "1978: 'NASA announced the members of its newest class of astronauts, which, for the first time in history, included six women.'",
   ["americas"], ["space-age", "cold-war"])
ev(1, 16, "charles-v-spain", "charles-v-renounces-spain-1556", "Charles V gives up the Spanish crown", "1556",
   "The emperor renounces his claim to Spain.",
   "On {hd}, Charles V, Holy Roman emperor and king of Spain, renounced his claim to Spain.",
   "1556: 'Charles V, Holy Roman emperor and king of Spain, renounced his claim to Spain.'",
   ["europe"], ["early-modern"], note=J)
ev(1, 16, "pendleton-act", "pendleton-civil-service-act-1883", "The Pendleton Civil Service Act becomes law", "1883",
   "The US creates a merit-based Civil Service Commission.",
   "On {hd}, the Pendleton Civil Service Act established the Civil Service Commission in the United States.",
   "1883: 'The Pendleton Civil Service Act, a bill sponsored by Senator George H. Pendleton of Ohio, established the Civil Service Commission in the United States.'",
   ["americas"], ["1800-1945"])

# ---------------------------------------------------------------- Jan 17
ev(1, 17, "popeye", "popeye-debuts-1929", "Popeye makes his debut", "1929",
   "The spinach-loving sailor first appears in the comic strip Thimble Theatre.",
   "On {hd}, Popeye, the sailor known for his love of spinach, made his debut in the comic strip Thimble Theatre.",
   "1929: 'Popeye, a sailor known for his love of spinach, made his debut' in the comic strip Thimble Theatre.",
   ["americas"], ["1800-1945"], "Enter Popeye, spinach in hand",
   "In 1929, Popeye the Sailor first appeared in a comic strip.")
ev(1, 17, "franklin-born", "benjamin-franklin-born-1706", "Benjamin Franklin is born", "1706",
   "The future scientist, writer and founding father is born in Boston.",
   "On {hd}, Benjamin Franklin was born in Boston. He later helped draft the US Declaration of Independence.",
   "1706: Benjamin Franklin born; Founding Father who helped draft the Declaration of Independence.",
   ["americas"], ["early-modern"], note="New Style date; January 6 in the Old Style calendar then used in Britain's colonies.",
   extra=[(f"{BR}On This Day: January 17", "https://www.britannica.com/on-this-day/January-17",
           "Britannica On This Day (Jan 17): 1706: Benjamin Franklin born; Founding Father who helped draft the Declaration of Independence."),
          (f"{BR}Benjamin Franklin", "https://www.britannica.com/biography/Benjamin-Franklin",
           "Britannica: 'born January 17 [January 6, Old Style], 1706, Boston, Massachusetts'.")])
ev(1, 17, "liliuokalani-deposed", "liliuokalani-deposed-1893", "Queen Liliuokalani of Hawaii is deposed", "1893",
   "A provisional government ends the Hawaiian monarchy.",
   "On {hd}, Hawaii's Queen Liliuokalani was deposed by a provisional government led by Sanford Ballard Dole.",
   "1893: Hawaiian Queen Liliuokalani was deposed by a provisional government led by Sanford Ballard Dole.",
   ["oceania", "americas"], ["1800-1945"])
ev(1, 17, "eisenhower-farewell", "eisenhower-farewell-address-1961", "Eisenhower warns of the military-industrial complex", "1961",
   "The president's farewell address becomes famous for one phrase.",
   "On {hd}, in his farewell address, US President Dwight D. Eisenhower warned against the acquisition of unwarranted influence by the military-industrial complex.",
   "1961: President Eisenhower's Farewell Address warned against 'the acquisition of unwarranted influence ... by the military-industrial complex.'",
   ["americas"], ["cold-war"])
ev(1, 17, "kobe-earthquake", "great-hanshin-earthquake-1995", "The Great Hanshin earthquake strikes Kobe", "1995",
   "A large earthquake hits the Osaka-Kobe region of Japan.",
   "On {hd}, a large-scale earthquake struck the Osaka-Kobe (Hanshin) metropolitan area of Japan, killing about 6,400 people.",
   "1995: 'A large-scale earthquake struck the Osaka-Kobe (Hanshin) metropolitan area,' killing approximately 6,400 people.",
   ["asia"], ["contemporary"])

# ---------------------------------------------------------------- Jan 18
ev(1, 18, "german-empire", "german-empire-proclaimed-1871", "The German Empire is founded", "1871",
   "Otto von Bismarck's unification creates a new empire.",
   "On {hd}, the German Empire was founded under the leadership of Otto von Bismarck.",
   "1871: 'The German Empire was founded by Otto von Bismarck.'",
   ["europe"], ["1800-1945"], "An empire is declared",
   "In 1871, the German Empire was founded.")
ev(1, 18, "ely-ship-landing", "eugene-ely-first-ship-landing-1911", "Eugene Ely lands a plane on a ship", "1911",
   "The first aircraft landing on a ship's flight deck.",
   "On {hd}, the American pilot Eugene Ely performed the first aircraft landing on a ship's flight deck.",
   "1911: 'The first aircraft landing on a ship's flight deck was performed by American pilot Eugene Ely.'",
   ["americas"], ["1800-1945"])
ev(1, 18, "sierra-leone-peace", "sierra-leone-civil-war-ends-2002", "Sierra Leone's civil war is declared over", "2002",
   "A decade of conflict officially ends.",
   "On {hd}, the civil war in Sierra Leone was officially declared over. More than 50,000 people are estimated to have died.",
   "2002: 'The civil war in Sierra Leone was officially declared over; more than 50,000 people are estimated to have died.'",
   ["africa"], ["contemporary"])
ev(1, 18, "willie-oree", "willie-oree-nhl-debut-1958", "Willie O'Ree breaks the NHL's color barrier", "1958",
   "He is the first Black player in a National Hockey League game.",
   "On {hd}, Willie O'Ree became the first Black athlete to play in a National Hockey League game.",
   "1958: 'Willie O'Ree became the first Black athlete to play in a National Hockey League game.'",
   ["americas"], ["cold-war"])
ev(1, 18, "milne-born", "aa-milne-born-1882", "A.A. Milne is born in London", "1882",
   "The creator of Winnie-the-Pooh is born.",
   "On {hd}, A.A. Milne, the author who created Winnie-the-Pooh, was born in London.",
   "1882: 'A.A. Milne ... was born in London.'",
   ["europe"], ["1800-1945"])

# ---------------------------------------------------------------- Jan 19
ev(1, 19, "il-trovatore", "il-trovatore-premieres-1853", "Verdi's Il trovatore premieres in Rome", "1853",
   "The opera is first performed.",
   "On {hd}, Giuseppe Verdi's opera Il trovatore premiered in Rome.",
   "1853: 'Giuseppe Verdi's opera Il trovatore premiered in Rome.'",
   ["europe"], ["1800-1945"], "Verdi's troubadour takes the stage",
   "In 1853, Verdi's opera Il trovatore premiered in Rome.")
ev(1, 19, "poe-born", "edgar-allan-poe-born-1809", "Edgar Allan Poe is born in Boston", "1809",
   "The master of the macabre is born.",
   "On {hd}, the writer Edgar Allan Poe was born in Boston.",
   "1809: 'Edgar Allan Poe ... was born in Boston.'",
   ["americas"], ["1800-1945"])
ev(1, 19, "tin-can-patent", "tin-can-patent-1825", "A US patent is granted for storing food in tin cans", "1825",
   "Ezra Daggett and Thomas Kensett patent a preserving process.",
   "On {hd}, Ezra Daggett and Thomas Kensett obtained a patent for a preservation process used to store food in tin cans.",
   "1825: 'Ezra Daggett and Thomas Kensett obtained a patent for a preservation process used to store food in tin cans.'",
   ["americas"], ["1800-1945"])
ev(1, 19, "televised-press-conference", "first-televised-presidential-press-conference-1955", "Eisenhower holds the first televised presidential press conference", "1955",
   "Cameras are allowed into a presidential news conference.",
   "On {hd}, US President Dwight D. Eisenhower held the first-ever televised presidential press conference.",
   "1955: 'U.S. President Dwight D. Eisenhower held the first-ever televised presidential press conference.'",
   ["americas"], ["cold-war"])

# ---------------------------------------------------------------- Jan 20
ev(1, 20, "obama-inaugurated", "obama-inaugurated-2009", "Barack Obama is sworn in as president", "2009",
   "He becomes the first African American president of the United States.",
   "On {hd}, Barack Obama was sworn in as the 44th president of the United States, the first African American to hold the office.",
   "2009: 'Barack Obama was sworn in as the 44th president of the United States, becoming the first African American to hold the office.'",
   ["americas"], ["contemporary"], "A historic oath of office",
   "In 2009, Barack Obama was sworn in as the 44th US president.")
ev(1, 20, "iran-hostages-freed", "iran-hostage-crisis-ends-1981", "The Iran hostage crisis ends", "1981",
   "Fifty-two Americans are released after 15 months.",
   "On {hd}, the Iran hostage crisis ended when 52 Americans who had been held hostage for 15 months were released.",
   "1981: 'The Iran hostage crisis ended when Ayatollah Ruhollah Khomeini released 52 Americans who had been held hostage for 15 months.'",
   ["middle-east", "americas"], ["cold-war"])
ev(1, 20, "cook-kauai", "cook-lands-on-kauai-1778", "James Cook lands on Kauai", "1778",
   "The British explorer comes ashore at Waimea in Hawaii.",
   "On {hd}, the British explorer James Cook landed at Waimea, on the island of Kauai in Hawaii.",
   "1778: 'British explorer James Cook landed at Waimea, on Kauai island.'",
   ["oceania", "europe"], ["early-modern"])
ev(1, 20, "amilcar-cabral", "amilcar-cabral-assassinated-1973", "Amilcar Cabral is assassinated", "1973",
   "The leader of Guinea-Bissau's independence struggle is killed.",
   "On {hd}, Amilcar Lopes Cabral was assassinated while leading the effort to secure Guinea-Bissau's independence.",
   "1973: 'Amilcar Lopes Cabral was assassinated as he led efforts to secure Guinea-Bissau's independence.'",
   ["africa"], ["decolonization"])
ev(1, 20, "fellini-born", "federico-fellini-born-1920", "Federico Fellini is born", "1920",
   "The celebrated Italian film director is born.",
   "On {hd}, the Italian director Federico Fellini, one of the most celebrated filmmakers of the post-World War II period, was born.",
   "1920: 'Italian director Federico Fellini, born this day in 1920, was one of the most-celebrated filmmakers in the post-World War II period.'",
   ["europe"], ["1800-1945"])

# ---------------------------------------------------------------- Jan 21
ev(1, 21, "concorde", "concorde-begins-service-1976", "Concorde begins commercial service", "1976",
   "The supersonic airliner starts carrying passengers.",
   "On {hd}, Concorde, a supersonic commercial aircraft built with funding from the British and French governments, began regular service.",
   "1976: 'the Concorde, a commercial aircraft built with funding from the British and French governments, began regular service'.",
   ["europe"], ["cold-war"], "Faster than sound, with seats",
   "In 1976, the supersonic Concorde began commercial flights.")
ev(1, 21, "louis-xvi", "louis-xvi-executed-1793", "Louis XVI is executed in Paris", "1793",
   "The last Bourbon king of France before the Revolution is guillotined.",
   "On {hd}, during the French Revolution, Louis XVI, the last Bourbon king of France before the Revolution, was executed by guillotine in Paris.",
   "1793: 'Louis XVI, the last Bourbon king of France, was executed by guillotine in Paris.'",
   ["europe"], ["revolutionary"])
ev(1, 21, "lenin-dies", "lenin-dies-1924", "Vladimir Lenin dies", "1924",
   "The founder of the Soviet state dies.",
   "On {hd}, Vladimir Lenin, the founder of the Soviet state, died.",
   "1924: Vladimir Lenin died.",
   ["europe"], ["1800-1945"])
ev(1, 21, "dior-born", "christian-dior-born-1905", "Christian Dior is born", "1905",
   "The fashion designer is born in Granville, France.",
   "On {hd}, the fashion designer Christian Dior was born in Granville, France.",
   "1905: Fashion designer Christian Dior born in Granville, France.",
   ["europe"], ["1800-1945"])
ev(1, 21, "domingo-born", "placido-domingo-born-1941", "Placido Domingo is born in Madrid", "1941",
   "The operatic tenor is born.",
   "On {hd}, the singer Placido Domingo was born in Madrid.",
   "1941: Singer Placido Domingo born in Madrid.",
   ["europe"], ["1800-1945"])

# ---------------------------------------------------------------- Jan 22
ev(1, 22, "victoria-dies", "queen-victoria-dies-1901", "Queen Victoria dies", "1901",
   "Britain's queen dies after a reign of more than 60 years.",
   "On {hd}, Queen Victoria died at age 81 after reigning for more than 60 years.",
   "1901: 'Queen Victoria ... died at age 81' after reigning 'more than 60 years.'",
   ["europe"], ["1800-1945"], "The end of the Victorian age",
   "In 1901, Queen Victoria died after more than 60 years on the throne.")
ev(1, 22, "bloody-sunday-1905", "bloody-sunday-st-petersburg-1905", "Bloody Sunday in St. Petersburg", "1905",
   "Troops fire on marching workers, sparking revolution.",
   "On {hd}, Russian workers marching in St. Petersburg were fired on by Russian troops. The day became known as Bloody Sunday.",
   "1905: 'Bloody Sunday,' when 'Russian workers marching on St. Petersburg were fired on by Russian troops.'",
   ["europe"], ["1800-1945"], note="January 9 in the Julian calendar then used in Russia.")
ev(1, 22, "evo-morales", "evo-morales-sworn-in-2006", "Evo Morales becomes president of Bolivia", "2006",
   "He is Bolivia's first president of Indigenous descent.",
   "On {hd}, Evo Morales was sworn in as president of Bolivia, the first person of Indigenous descent to hold that office.",
   "2006: 'Evo Morales was sworn in as president of Bolivia, becoming the first person of indigenous descent' to hold that office.",
   ["americas"], ["contemporary"])
ev(1, 22, "roberta-bondar", "roberta-bondar-in-space-1992", "Roberta Bondar becomes Canada's first woman in space", "1992",
   "The neurologist flies aboard the space shuttle.",
   "On {hd}, Roberta Bondar became the first Canadian woman and the first neurologist to travel into space.",
   "1992: 'Roberta Bondar became the first Canadian woman and the first neurologist to travel into space.'",
   ["americas"], ["contemporary", "space-age"])
ev(1, 22, "byron-born", "lord-byron-born-1788", "Lord Byron is born", "1788",
   "The Romantic poet is born.",
   "On {hd}, Lord Byron, the British Romantic poet and satirist who captured the imagination of Europe, was born.",
   "1788: Lord Byron born; 'British Romantic poet and satirist' who 'captured the imagination of Europe with his personality and work.'",
   ["europe"], ["early-modern"])

# ---------------------------------------------------------------- Jan 23
ev(1, 23, "blackwell", "elizabeth-blackwell-md-1849", "Elizabeth Blackwell earns her medical degree", "1849",
   "She becomes the first woman physician trained in America.",
   "On {hd}, Elizabeth Blackwell received an MD degree from Geneva Medical College in New York, becoming the first American-trained woman physician.",
   "1849: 'Elizabeth Blackwell received an M.D. degree from Geneva Medical College in New York, becoming the first American-trained woman physician.'",
   ["americas"], ["1800-1945"], "Doctor Blackwell graduates",
   "In 1849, Elizabeth Blackwell became America's first woman medical graduate.")
ev(1, 23, "shaanxi-earthquake", "shaanxi-earthquake-1556", "The Shaanxi earthquake strikes China", "1556",
   "An estimated 830,000 people are killed or injured.",
   "On {hd}, an earthquake centered in Shaanxi province, China, killed or injured an estimated 830,000 people.",
   "1556: 'An earthquake in China killed or injured an estimated 830,000 people ... (Shaanxi, where the quake was centered, and neighboring Shanxi).'",
   ["asia"], ["early-modern"], note=J)
ev(1, 23, "albright", "madeleine-albright-secretary-of-state-1997", "Madeleine Albright becomes US secretary of state", "1997",
   "She is the first woman to hold the post.",
   "On {hd}, Madeleine Albright became US secretary of state, the first woman to hold that cabinet post.",
   "1997: 'Madeleine Albright ... became secretary of state ... She was the first woman to hold that cabinet post.'",
   ["americas"], ["contemporary"])
ev(1, 23, "twenty-fourth-amendment", "twenty-fourth-amendment-ratified-1964", "The Twenty-fourth Amendment bans poll taxes", "1964",
   "The amendment ends poll taxes in federal elections.",
   "On {hd}, the Twenty-fourth Amendment to the US Constitution was ratified, prohibiting poll taxes as a condition of voting in federal elections.",
   "1964: 'The Twenty-fourth Amendment to the U.S. Constitution, which prohibited the federal and state governments from imposing poll taxes before a citizen can participate in a federal election, was ratified.'",
   ["americas"], ["cold-war"])

# ---------------------------------------------------------------- Jan 24
ev(1, 24, "california-gold", "gold-discovered-california-1848", "Gold is discovered in California", "1848",
   "A find at Sutter's Mill sets off the Gold Rush.",
   "On {hd}, James Marshall spotted gold while building a sawmill for John Sutter at Coloma, California. The discovery set off the California Gold Rush.",
   "1848: 'Gold discovered in California' - James Marshall spotted gold nuggets while constructing a sawmill, triggering the California Gold Rush.",
   ["americas"], ["1800-1945"], "Gold in the millrace",
   "In 1848, gold was found in California, setting off the Gold Rush.",
   extra=[(f"{BR}On This Day: January 24", "https://www.britannica.com/on-this-day/January-24",
           "Britannica On This Day (Jan 24): 1848: 'Gold discovered in California' - James Marshall spotted gold nuggets while constructing a sawmill, triggering the California Gold Rush."),
          (f"{BR}California Gold Rush", "https://www.britannica.com/topic/California-Gold-Rush",
           "Britannica: on January 24, 1848, James W. Marshall, John Sutter's carpenter, found gold at Sutter's Mill on the American River at Coloma; the 1849 arrivals were called forty-niners.")])
ev(1, 24, "macintosh", "apple-introduces-macintosh-1984", "Apple introduces the Macintosh", "1984",
   "Steve Jobs unveils Apple's new personal computer.",
   "On {hd}, Steve Jobs introduced Apple's Macintosh computer.",
   "1984: 'Steve Jobs introduced Apple's revolutionary computer Macintosh.'",
   ["americas"], ["cold-war"])
ev(1, 24, "churchill-dies", "winston-churchill-dies-1965", "Winston Churchill dies", "1965",
   "Britain's wartime prime minister dies in London.",
   "On {hd}, Winston Churchill, Britain's prime minister during most of World War II, died.",
   "1965: 'Winston Churchill ... died this day in 1965.'",
   ["europe"], ["cold-war"])
ev(1, 24, "wharton-born", "edith-wharton-born-1862", "Edith Wharton is born", "1862",
   "The novelist of New York society is born.",
   "On {hd}, Edith Wharton, an American author best known for her stories and novels about upper-class society, was born in New York.",
   "1862: 'Edith Wharton, an American author best known for her stories and novels about upper-class society, was born in New York.'",
   ["americas"], ["1800-1945"])

# ---------------------------------------------------------------- Jan 25
ev(1, 25, "chamonix", "first-winter-olympics-open-1924", "The first Winter Olympics open in Chamonix", "1924",
   "About 250 athletes from 16 countries gather in the French Alps.",
   "On {hd}, the first Winter Olympic Games opened in Chamonix, France. Some 250 athletes from 16 countries competed in 16 events; the games were originally staged as an International Winter Sports Week.",
   "", ["europe", "global"], ["1800-1945"], "The first Winter Games begin",
   "In 1924, the first Winter Olympics opened in Chamonix, France.",
   extra=[(f"{BR}Chamonix 1924 Olympic Winter Games",
           "https://www.britannica.com/event/Chamonix-1924-Olympic-Winter-Games",
           "Britannica: held January 25-February 5, 1924; 'the first occurrence of the Winter Olympic Games'; some 250 athletes from 16 countries in 16 events; originally staged as International Winter Sports Week; the IOC amended its charter in 1925.")])
ev(1, 25, "sao-paulo", "sao-paulo-founded-1554", "Jesuit missionaries found Sao Paulo", "1554",
   "The settlement will grow into Brazil's largest city.",
   "On {hd}, Jesuit missionaries founded the city of Sao Paulo in Brazil.",
   "1554: 'Jesuit missionaries founded the city of Sao Paulo.'",
   ["americas"], ["early-modern"], note=J)
ev(1, 25, "burns-born", "robert-burns-born-1759", "Robert Burns is born", "1759",
   "Scotland's national poet is born.",
   "On {hd}, Robert Burns, the national poet of Scotland, was born.",
   "1759: Robert Burns born (national poet of Scotland).",
   ["europe"], ["early-modern"])
ev(1, 25, "woolf-born", "virginia-woolf-born-1882", "Virginia Woolf is born in London", "1882",
   "The modernist novelist is born.",
   "On {hd}, the novelist Virginia Woolf was born in London.",
   "1882: Virginia Woolf born in London.",
   ["europe"], ["1800-1945"])
ev(1, 25, "egypt-protests", "egypt-protests-begin-2011", "Mass protests begin in Egypt", "2011",
   "Demonstrators clash with police in Cairo and other cities.",
   "On {hd}, demonstrators in Cairo and several other Egyptian cities clashed with police, the start of mass protests against the government.",
   "2011: 'Demonstrators in Cairo and several other cities in Egypt clashed with police.'",
   ["africa", "middle-east"], ["contemporary"])

# ---------------------------------------------------------------- Jan 26
ev(1, 26, "cullinan", "cullinan-diamond-found-1905", "The Cullinan diamond is found", "1905",
   "The world's largest diamond is discovered at the Premier Mine in South Africa.",
   "On {hd}, the largest diamond ever found, weighing more than a pound, was discovered at the Premier Mine in South Africa.",
   "1905: 'World's largest diamond discovered in South Africa' - a diamond weighing over a pound found at Premier Mine.",
   ["africa"], ["1800-1945"], "A diamond weighing over a pound",
   "In 1905, the record-breaking Cullinan diamond was found in South Africa.")
ev(1, 26, "india-republic", "india-becomes-republic-1950", "India becomes a republic", "1950",
   "The new constitution takes effect.",
   "On {hd}, India became a republic, achieving full independence from Great Britain.",
   "1950: 'India became a republic, achieving full independence from Great Britain.'",
   ["asia"], ["decolonization"])
ev(1, 26, "first-fleet", "first-fleet-sydney-cove-1788", "Arthur Phillip founds the first British settlement in Australia", "1788",
   "The British flag is raised at Sydney Cove.",
   "On {hd}, Arthur Phillip hoisted the British flag at Sydney Cove and established the first permanent European settlement in Australia.",
   "1788: Arthur Phillip hoisted British flag and established first permanent European settlement in Australia.",
   ["oceania", "europe"], ["early-modern"])
ev(1, 26, "gujarat-earthquake", "gujarat-earthquake-2001", "A major earthquake strikes Gujarat, India", "2001",
   "The quake near Bhuj kills more than 20,000 people.",
   "On {hd}, a major earthquake struck near Bhuj in Gujarat, India, killing more than 20,000 people.",
   "2001: Major earthquake struck near Bhuj, India, killing over 20,000 people.",
   ["asia"], ["contemporary"])
ev(1, 26, "phantom-broadway", "phantom-of-the-opera-broadway-1988", "The Phantom of the Opera opens on Broadway", "1988",
   "Andrew Lloyd Webber's musical opens in New York.",
   "On {hd}, The Phantom of the Opera opened in New York City.",
   "1988: 'The Phantom of the Opera' opened in New York City.",
   ["americas"], ["contemporary"])

# ---------------------------------------------------------------- Jan 27
ev(1, 27, "mozart-born", "mozart-born-1756", "Wolfgang Amadeus Mozart is born", "1756",
   "The composer is born in Salzburg.",
   "On {hd}, the composer Wolfgang Amadeus Mozart was born in Salzburg.",
   "1756: 'Wolfgang Amadeus Mozart ... was born this day in 1756.'",
   ["europe"], ["early-modern"], "A prodigy is born",
   "In 1756, Wolfgang Amadeus Mozart was born in Salzburg.",
   extra=[(f"{BR}On This Day: January 27", "https://www.britannica.com/on-this-day/January-27",
           "Britannica On This Day (Jan 27): 1756: 'Wolfgang Amadeus Mozart ... was born this day in 1756.'"),
          (f"{BR}Wolfgang Amadeus Mozart", "https://www.britannica.com/biography/Wolfgang-Amadeus-Mozart",
           "Britannica: 'born January 27, 1756, Salzburg, archbishopric of Salzburg [Austria]'; died December 5, 1791, Vienna.")])
ev(1, 27, "auschwitz", "auschwitz-liberated-1945", "Soviet troops liberate Auschwitz", "1945",
   "Soldiers find about 7,650 surviving prisoners at the Nazi camp.",
   "On {hd}, Soviet troops arrived at Auschwitz, the Nazi concentration and extermination camp near Oswiecim in Poland, and found 7,650 sick and starving prisoners. Between 1.1 and 1.5 million people died there.",
   "", ["europe"], ["1800-1945"],
   extra=[(f"{BR}Auschwitz", "https://www.britannica.com/place/Auschwitz",
           "Britannica: 'When Soviet troops arrived on January 27, 1945, they found 7,650 sick and starving prisoners'; between 1.1 and 1.5 million people died at Auschwitz, 90 percent of them Jews; located near Oswiecim in southern Poland.")])
ev(1, 27, "leningrad", "siege-of-leningrad-ends-1944", "The Siege of Leningrad ends", "1944",
   "The Red Army lifts the 872-day blockade.",
   "On {hd}, the Soviet Red Army drove German and Finnish forces from the Leningrad area, ending an 872-day siege.",
   "1944: 'The Soviet Red Army ousted German and Finnish forces from Leningrad ... concluding an 872-day siege.'",
   ["europe"], ["1800-1945"])
ev(1, 27, "paris-peace-accords", "paris-peace-accords-signed-1973", "The Paris Peace Accords are signed", "1973",
   "The agreements formally end the Vietnam War.",
   "On {hd}, the Paris Peace Accords formally ending the Vietnam War were signed.",
   "1973: 'The Paris Peace Accords formally ending the Vietnam War were signed.'",
   ["asia", "americas", "europe"], ["cold-war"])
ev(1, 27, "dante-exiled", "dante-exiled-from-florence-1302", "Dante is exiled from Florence", "1302",
   "The poet's political enemies banish him.",
   "On {hd}, the Italian poet Dante Alighieri was exiled from Florence by his political enemies.",
   "1302: 'Italian poet Dante Alighieri was exiled from Florence by his political enemies.'",
   ["europe"], ["medieval"], note=J)

# ---------------------------------------------------------------- Jan 28
ev(1, 28, "lego-patent", "lego-brick-patent-1958", "The LEGO brick patent is filed", "1958",
   "The interlocking toy brick goes on to conquer the world.",
   "On {hd}, the patent for the LEGO toy building brick was filed. The brick became hugely popular around the world.",
   "1958: LEGO patent filed - 'toy building block that became hugely popular around the world'.",
   ["europe"], ["cold-war"], "Click: the LEGO brick",
   "In 1958, LEGO filed the patent for its famous interlocking brick.")
ev(1, 28, "challenger", "challenger-disaster-1986", "The space shuttle Challenger is lost", "1986",
   "The shuttle breaks apart 73 seconds after liftoff.",
   "On {hd}, the US space shuttle Challenger exploded 73 seconds after liftoff from Florida, killing all seven crew members.",
   "1986: 'the U.S. space shuttle Challenger exploded 73 seconds after liftoff from Florida, killing all seven aboard'.",
   ["americas"], ["space-age", "cold-war"])
ev(1, 28, "pride-and-prejudice", "pride-and-prejudice-published-1813", "Jane Austen's Pride and Prejudice is published", "1813",
   "The novel enjoys immediate success.",
   "On {hd}, Jane Austen's Pride and Prejudice was published and enjoyed immediate success.",
   "1813: Pride and Prejudice published - 'enjoyed immediate success'.",
   ["europe"], ["1800-1945"])
ev(1, 28, "charlemagne-dies", "charlemagne-dies-814", "Charlemagne dies", "814",
   "The first emperor of the revived Western empire dies at Aachen.",
   "On {hd}, Charlemagne, ruler of the Franks and emperor in the West since 800, died.",
   "814: Charlemagne died - 'ruler of the Holy Roman Empire'.",
   ["europe"], ["medieval"], note=J)
ev(1, 28, "paris-capitulates", "paris-capitulates-1871", "Paris falls after a four-month siege", "1871",
   "The French capital capitulates in the Franco-German War.",
   "On {hd}, Paris fell to German forces following a four-month siege during the Franco-German War.",
   "1871: Paris fell - 'following a four-month siege during the Franco-German War'.",
   ["europe"], ["1800-1945"])

# ---------------------------------------------------------------- Jan 29
ev(1, 29, "benz-patent", "benz-patents-automobile-1886", "Karl Benz patents his motorcar", "1886",
   "The first practical gasoline-powered automobile is patented.",
   "On {hd}, Karl Benz patented the first practical automobile powered by an internal-combustion engine.",
   "1886: 'Karl Benz patented the first practical automobile powered by an internal-combustion engine.'",
   ["europe"], ["1800-1945"], "The car gets its patent",
   "In 1886, Karl Benz patented his motorcar.")
ev(1, 29, "the-raven", "the-raven-published-1845", "Poe's 'The Raven' is first published", "1845",
   "'Nevermore' enters American literature.",
   "On {hd}, Edgar Allan Poe's poem 'The Raven' was first published.",
   "1845: 'Edgar Allan Poe's \"The Raven\" was first published.'",
   ["americas"], ["1800-1945"])
ev(1, 29, "chekhov-born", "anton-chekhov-born-1860", "Anton Chekhov is born", "1860",
   "The Russian playwright and short-story master is born.",
   "On {hd}, the Russian playwright and short-story writer Anton Chekhov was born in Taganrog.",
   "1860: 'Anton Chekhov ... was born.'",
   ["europe"], ["1800-1945"], note="January 17 in the Julian calendar then used in Russia.",
   extra=[(f"{BR}On This Day: January 29", "https://www.britannica.com/on-this-day/January-29",
           "Britannica On This Day (Jan 29): 1860: 'Anton Chekhov ... was born.'"),
          (f"{BR}Anton Chekhov", "https://www.britannica.com/biography/Anton-Chekhov",
           "Britannica: 'born January 29 [January 17, Old Style], 1860, Taganrog, Russia'; plays The Seagull, Uncle Vanya, Three Sisters, The Cherry Orchard.")])
ev(1, 29, "baseball-hof", "baseball-hall-of-fame-first-class-1936", "Baseball's Hall of Fame elects its first players", "1936",
   "Babe Ruth and Ty Cobb are among the first inductees chosen.",
   "On {hd}, Babe Ruth and Ty Cobb were among the first players elected to the Baseball Hall of Fame.",
   "1936: 'Babe Ruth and Ty Cobb were among the first players to be elected to the Baseball Hall of Fame.'",
   ["americas"], ["1800-1945"])

# ---------------------------------------------------------------- Jan 30
ev(1, 30, "beatles-rooftop", "beatles-rooftop-concert-1969", "The Beatles play together for the last time", "1969",
   "The band's final live performance takes place on a London rooftop.",
   "On {hd}, the Beatles, John Lennon, Paul McCartney, George Harrison, and Ringo Starr, performed together in public for the last time.",
   "1969: 'the Beatles - John Lennon, Paul McCartney, George Harrison, and Ringo Starr - performed together for the last time.'",
   ["europe"], ["cold-war"], "The Beatles' final rooftop gig",
   "In 1969, the Beatles performed together in public for the last time.")
ev(1, 30, "gandhi-assassinated", "gandhi-assassinated-1948", "Mahatma Gandhi is assassinated", "1948",
   "India's independence leader is killed in New Delhi.",
   "On {hd}, Mahatma Gandhi was assassinated by the Hindu extremist Nathuram Godse.",
   "1948: 'Mahatma Gandhi ... was assassinated by Hindu extremist Nathuram Godse.'",
   ["asia"], ["decolonization"])
ev(1, 30, "charles-i-executed", "charles-i-executed-1649", "Charles I of England is executed", "1649",
   "The king is beheaded in London after the English Civil Wars.",
   "On {hd}, Charles I of England was executed in London.",
   "1649: Charles I 'was executed in London'.",
   ["europe"], ["early-modern"], note="Julian calendar date; contemporary English records gave the year as 1648.")
ev(1, 30, "hitler-chancellor", "hitler-named-chancellor-1933", "Adolf Hitler is named chancellor of Germany", "1933",
   "President Hindenburg appoints the Nazi leader.",
   "On {hd}, German President Paul von Hindenburg named Adolf Hitler chancellor of Germany.",
   "1933: 'President Paul von Hindenburg named Adolf Hitler chancellor of Germany'.",
   ["europe"], ["1800-1945"])
ev(1, 30, "city-lights", "city-lights-premieres-1931", "Charlie Chaplin's City Lights premieres", "1931",
   "The silent comedy is often called Chaplin's crowning achievement.",
   "On {hd}, Charlie Chaplin's City Lights had its world premiere. It is often considered his crowning achievement.",
   "1931: 'City Lights had its world premiere' and is considered 'Charlie Chaplin's crowning achievement'.",
   ["americas"], ["1800-1945"])

# ---------------------------------------------------------------- Jan 31
ev(1, 31, "brexit", "uk-leaves-european-union-2020", "The United Kingdom leaves the European Union", "2020",
   "Brexit takes effect more than three years after the referendum.",
   "On {hd}, the United Kingdom formally left the European Union, more than three years after voting to leave.",
   "2020: 'The United Kingdom formally left the European Union, more than three years after the country voted for Brexit.'",
   ["europe"], ["contemporary"], "Britain steps out of the EU",
   "In 2020, the United Kingdom formally left the European Union.")
ev(1, 31, "schubert-born", "franz-schubert-born-1797", "Franz Schubert is born near Vienna", "1797",
   "The Austrian composer is born.",
   "On {hd}, the composer Franz Schubert was born near Vienna.",
   "1797: Composer Franz Schubert was born near Vienna.",
   ["europe"], ["revolutionary"])
ev(1, 31, "paulus-surrenders", "paulus-surrenders-at-stalingrad-1943", "Friedrich Paulus surrenders at Stalingrad", "1943",
   "The German field marshal surrenders to the Red Army.",
   "On {hd}, German Field Marshal Friedrich Paulus surrendered to the Soviet Red Army at Stalingrad.",
   "",
   ["europe"], ["1800-1945"],
   extra=[(f"{BR}On This Day: January 31", "https://www.britannica.com/on-this-day/January-31",
           "Britannica On This Day (Jan 31): 1943: Field Marshal Friedrich Paulus surrendered to the Soviet Red Army at Stalingrad."),
          (f"{BR}Battle of Stalingrad", "https://www.britannica.com/event/Battle-of-Stalingrad",
           "Britannica: battle August 22, 1942 - February 2, 1943, on the Volga; 'On January 31 Paulus disobeyed Hitler and agreed to give himself up'; Stalingrad is now Volgograd.")])
ev(1, 31, "jackie-robinson-born", "jackie-robinson-born-1919", "Jackie Robinson is born", "1919",
   "The player who will break baseball's color line is born.",
   "On {hd}, Jackie Robinson was born. He became the first African American to play in baseball's major leagues in the 20th century.",
   "1919: Jackie Robinson born (first African American baseball player in U.S. major leagues in the 20th century).",
   ["americas"], ["1800-1945"])
ev(1, 31, "guy-fawkes-executed", "guy-fawkes-executed-1606", "Guy Fawkes is executed in London", "1606",
   "The Gunpowder Plot conspirator is put to death.",
   "On {hd}, Guy Fawkes was executed in London for his role in the Gunpowder Plot.",
   "1606: Guy Fawkes was executed in London for his role in the Gunpowder Plot.",
   ["europe"], ["early-modern"], note=J)


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
