from lib import Batch
b = Batch("2026-11-20-26-historical-events")
BR = "Encyclopaedia Britannica - "
def otd(day):
    return (BR+"On This Day: November "+str(day), "https://www.britannica.com/on-this-day/November-"+str(day))
def ev(day, short, id, title, year, summary, desc, quote, regions, eras, nt=None, nb=None, note=None):
    n, u = otd(day)
    b.add(f"11-{day}", short, id, title, year, f"November {day}, {year}", summary, desc,
          [(n, u, f"Britannica On This Day (Nov {day}): {quote}")], regions, eras, nt, nb, note)
# Nov 20
ev(20,"mexican-revolution","mexican-revolution-begins-1910","Francisco Madero launches the Mexican Revolution","1910",
 "A failed revolt inspires the leaders who topple Porfirio Diaz.",
 "On November 20, 1910, Francisco Madero launched a revolt that failed but sparked the Mexican Revolution, inspiring leaders such as Pancho Villa and Emiliano Zapata to mobilize their armies against President Porfirio Diaz.",
 "'On this day in 1910, Francisco Madero launched a failed revolt that sparked the Mexican Revolution by inspiring hope in such leaders as Pancho Villa and Emiliano Zapata, who then mobilized their armies against the government of Porfirio Diaz.'",
 ["americas"],["1800-1945"],"The spark of the Mexican Revolution","In 1910, Francisco Madero's revolt set the Mexican Revolution in motion.")
ev(20,"qatar-world-cup","qatar-world-cup-begins-2022","The first World Cup in the Middle East kicks off","2022",
 "Ecuador beats host Qatar in the opening match.",
 "On November 20, 2022, the first FIFA World Cup held in the Middle East began in Qatar, with Ecuador defeating the host country in the opening match.",
 "2022: 'The first World Cup to be held in the Middle East began. Ecuador defeated Qatar, the host country, in the tournament's first match.'",
 ["middle-east","global"],["contemporary"])
ev(20,"essex","whaleship-essex-sunk-1820","A sperm whale rams the whaleship Essex","1820",
 "The sinking later inspires the climax of Moby-Dick.",
 "On November 20, 1820, the American whaling ship Essex was rammed by a sperm whale and later sank, an event that inspired the climactic scene of Herman Melville's Moby-Dick.",
 "1820: 'The American whaling ship Essex was rammed by a sperm whale and later sank, inspiring the climactic scene in Herman Melville's novel Moby Dick (1851).'",
 ["americas","oceania"],["1800-1945"])
ev(20,"cambrai","battle-of-cambrai-tanks-1917","Tanks are first used effectively at Cambrai","1917",
 "British armor changes the character of World War I battle.",
 "On November 20, 1917, at the Battle of Cambrai during World War I, the British used tanks effectively in warfare for the first time.",
 "1917: 'For the first time, tanks were used effectively in warfare, by the British at the Battle of Cambrai.'",
 ["europe"],["1800-1945"])
# Nov 21
ev(21,"montgolfier-balloon","first-crewed-balloon-flight-1783","The first crewed hot-air balloon flight","1783",
 "Two Frenchmen float over Paris in a Montgolfier balloon.",
 "On November 21, 1783, Jean-Francois Pilatre de Rozier and the marquis d'Arlandes made the first crewed hot-air balloon flight, traveling from the Chateau de la Muette across the Bois de Boulogne near Paris in a balloon built by the Montgolfier brothers.",
 "1783: 'The first crewed hot-air balloon flight was made by Jean-Francois Pilatre de Rozier and Francois Laurent, marquis d'Arlandes, traveling from the Chateau de la Muette across the Bois de Boulogne on the edge of Paris in a balloon made by Joseph-Michel and Jacques-Etienne Montgolfier.'",
 ["europe"],["revolutionary","early-modern"],"The first people to fly","In 1783, two Frenchmen made the first crewed hot-air balloon flight.")
ev(21,"dayton","dayton-accords-1995","The Dayton Accords end the Bosnian War","1995",
 "Leaders of Bosnia, Croatia, and Serbia reach a peace agreement.",
 "On November 21, 1995, the presidents of Bosnia, Croatia, and Serbia reached the peace agreement known as the Dayton Accords, ending the Bosnian War.",
 "1995: 'A peace agreement, known as the Dayton Accords, was reached by the presidents of Bosnia, Croatia, and Serbia, ending the Bosnian War.'",
 ["europe"],["contemporary"])
ev(21,"voltaire","voltaire-born-1694","Voltaire is born","1694",
 "The French writer famed for his satiric wit is born.",
 "On November 21, 1694, Voltaire was born. One of the greatest French writers, he is remembered for his critical capacity and satiric wit and as a crusader against tyranny, bigotry, and cruelty.",
 "'One of the greatest French writers, famous for his critical capacity and satiric wit and still widely revered as a courageous crusader against tyranny, bigotry, and cruelty, Voltaire was born this day in 1694.'",
 ["europe"],["early-modern"])
ev(21,"mugabe","mugabe-resigns-2017","Robert Mugabe resigns in Zimbabwe","2017",
 "The leader of 37 years steps down as impeachment begins.",
 "On November 21, 2017, after some 37 years as leader of Zimbabwe, first as prime minister and later as president, Robert Mugabe resigned as parliament began impeachment proceedings against him.",
 "2017: 'After some 37 years as leader of Zimbabwe - first as prime minister and later as president - Robert Mugabe resigned from office as the parliament began impeachment proceedings against him.'",
 ["africa"],["contemporary"])
# Nov 22
ev(22,"toy-story","toy-story-released-1995","Toy Story, the first computer-animated feature, is released","1995",
 "Pixar's film launches a new era of animation.",
 "On November 22, 1995, Pixar's Toy Story, the first entirely computer-animated feature-length film, was released. It became a critical and commercial hit and started a long-running franchise.",
 "1995: 'Pixar's Toy Story, the first entirely computer-animated feature-length film, was released. It became a critical and commercial hit and established a long-running franchise.'",
 ["americas"],["contemporary"],"To infinity and the box office","In 1995, Toy Story became the first fully computer-animated feature film.")
ev(22,"jfk-assassinated","kennedy-assassinated-1963","President John F. Kennedy is assassinated","1963",
 "The 35th US president is shot in Dallas.",
 "On November 22, 1963, John F. Kennedy, the 35th president of the United States, was shot and killed in Dallas, Texas.",
 "'John F. Kennedy, the 35th president of the United States (1961-63), was shot and killed in Dallas, Texas, on this day in 1963.'",
 ["americas"],["cold-war"])
ev(22,"lebanon","lebanon-independence-1943","Lebanon proclaims independence from France","1943",
 "The declaration begins Lebanon's path to full sovereignty.",
 "On November 22, 1943, Lebanon proclaimed its independence from France, though it did not become wholly independent until 1946.",
 "1943: 'Lebanon proclaimed its independence from France, though it did not become wholly independent until 1946.'",
 ["middle-east"],["1800-1945","decolonization"])
ev(22,"merkel","merkel-becomes-chancellor-2005","Angela Merkel becomes Germany's first woman chancellor","2005",
 "She is sworn in to lead the German government.",
 "On November 22, 2005, Angela Merkel was sworn in as chancellor of Germany, the first woman to hold the post.",
 "2005: 'Angela Merkel was sworn in as Germany's chancellor, becoming the first woman to hold the post.'",
 ["europe"],["contemporary"])
# Nov 23
ev(23,"doctor-who","doctor-who-first-episode-1963","Doctor Who airs for the first time","1963",
 "The BBC science-fiction series begins its long journey through time.",
 "On November 23, 1963, the first episode of the British science-fiction series Doctor Who aired, and the show soon became a landmark of British popular culture.",
 "1963: 'The first episode of the British science-fiction television series Doctor Who aired, and the show soon became a landmark of British popular culture.'",
 ["europe"],["1945-present"],"The Doctor's first journey","In 1963, Doctor Who aired its first episode on the BBC.")
ev(23,"ley-juarez","ley-juarez-passed-1855","Mexico passes the Ley Juarez","1855",
 "Special courts for clergy and military are abolished.",
 "On November 23, 1855, Mexico passed the Ley Juarez, which abolished special courts for the clergy and military in an effort by justice minister Benito Juarez to remove remnants of colonialism and promote equality.",
 "'Passed this day in 1855 in Mexico, the Ley Juarez abolished special courts for the clergy and military in an attempt by justice minister Benito Juarez to eliminate the remnants of colonialism in Mexico and promote equality.'",
 ["americas"],["1800-1945"])
ev(23,"life-magazine","life-magazine-first-issue-1936","The first issue of Life magazine is published","1936",
 "The weekly becomes a pioneer of photojournalism.",
 "On November 23, 1936, the first issue of Life was published. The magazine became a pioneer in photojournalism and a major force in the field's development.",
 "1936: 'The first issue of Life was published. The magazine later became a pioneer in photojournalism and was one of the major forces in that field's development.'",
 ["americas"],["1800-1945"])
ev(23,"otto-i","otto-i-born-912","Otto I, future Holy Roman emperor, is born","912",
 "The ruler who consolidated the German realm is born.",
 "On November 23, 912, Otto I was born. As Holy Roman emperor he consolidated the German realm by suppressing rebellious vassals and winning a decisive victory over the Hungarians.",
 "912: 'The Holy Roman emperor Otto I - who, during his reign in the 10th century, consolidated the German Reich by his suppression of rebellious vassals and his decisive victory over the Hungarians - was born.'",
 ["europe"],["medieval"],note="Julian calendar date.")
# Nov 24
ev(24,"tasman","tasman-reaches-tasmania-1642","Abel Tasman reaches Tasmania","1642",
 "The Dutch navigator skirts the island's southern shores.",
 "On November 24, 1642, Dutch navigator Abel Janszoon Tasman, sailing from Batavia (Jakarta) to seek an eastward sea passage to Chile and explore New Guinea, skirted the southern shores of Tasmania.",
 "'Dutch navigator Abel Janszoon Tasman, who sailed from Batavia (Jakarta) to investigate the practicality of a sea passage eastward to Chile and to explore New Guinea, skirted the southern shores of Tasmania this day in 1642.'",
 ["oceania","europe","asia"],["early-modern"],"A Dutch sighting at the edge of the world","In 1642, Abel Tasman reached the island now called Tasmania.")
ev(24,"black-beauty","black-beauty-published-1877","Anna Sewell publishes Black Beauty","1877",
 "Her only novel becomes a landmark animal story for children.",
 "On November 24, 1877, shortly before her death, Anna Sewell published her only novel, Black Beauty, the first major story about an animal in children's literature.",
 "1877: 'Shortly before her death, Anna Sewell published her only novel, Black Beauty, the first major story about an animal in children's literature.'",
 ["europe"],["1800-1945"])
ev(24,"turkey-equality","turkey-legal-equality-reform-2001","Turkey reforms its civil code to make women equal before the law","2001",
 "Parliament ends wives' legal subordination to husbands.",
 "On November 24, 2001, the Grand National Assembly of Turkey ratified changes to the legal code that made women equal to men before the law and no longer subject to their husbands.",
 "2001: 'The Grand National Assembly of Turkey ratified changes to the country's legal code that made women equal to men before the law and no longer subject to their husbands.'",
 ["europe","middle-east"],["contemporary"])
ev(24,"toulouse-lautrec","toulouse-lautrec-born-1864","Henri de Toulouse-Lautrec is born","1864",
 "The artist of Parisian nightlife is born in France.",
 "On November 24, 1864, the French artist Henri de Toulouse-Lautrec was born. He documented Parisian nightlife and the world of entertainment in the 1890s with great psychological insight.",
 "'French artist Henri de Toulouse-Lautrec, born this day in 1864, documented with great psychological insight the personalities and facets of Parisian nightlife and the French world of entertainment in the 1890s.'",
 ["europe"],["1800-1945"])
# Nov 25
ev(25,"mousetrap","the-mousetrap-opens-1952","Agatha Christie's The Mousetrap opens in London","1952",
 "The mystery play begins a run that lasts for decades.",
 "On November 25, 1952, Agatha Christie's play The Mousetrap opened in London. By its 50th anniversary in 2002, it had been performed more than 20,000 times.",
 "2002: 'In London the Agatha Christie play The Mousetrap celebrated its 50th anniversary with a royal gala, having opened on November 25, 1952, and this performance being its 20,807th.'",
 ["europe"],["1945-present"],"The play that never closed","In 1952, Agatha Christie's The Mousetrap opened in London.")
ev(25,"suriname","suriname-independence-1975","Suriname becomes independent","1975",
 "The South American nation gains independence from the Netherlands.",
 "On November 25, 1975, Suriname gained its independence from the Netherlands.",
 "1975: 'Suriname gained its independence from the Netherlands.'",
 ["americas","europe"],["decolonization","cold-war"])
ev(25,"lope-de-vega","lope-de-vega-born-1562","Lope de Vega is born","1562",
 "The Spanish author is born.",
 "On November 25, 1562, the Spanish author Lope de Vega was born.",
 "Famous birthdays: '1562 Lope de Vega - Spanish author'.",
 ["europe"],["early-modern"],note="Julian calendar date.")
ev(25,"los-alamos","los-alamos-chosen-1942","Los Alamos is chosen for the atomic bomb project","1942",
 "Groves and Oppenheimer pick a New Mexico mesa for Project Y.",
 "On November 25, 1942, Leslie Groves and J. Robert Oppenheimer chose Los Alamos, New Mexico, as the site of Project Y, which developed the first atomic bomb.",
 "1942: 'Leslie Groves and J. Robert Oppenheimer chose Los Alamos, New Mexico, as the site of Project Y, which developed the first atomic bomb.'",
 ["americas"],["1800-1945"])
# Nov 26
ev(26,"casablanca","casablanca-premieres-1942","Casablanca premieres","1942",
 "Bogart and Bergman's wartime romance becomes a Hollywood classic.",
 "On November 26, 1942, Casablanca premiered. Set in occupied Morocco during World War II, directed by Michael Curtiz, and starring Humphrey Bogart, Ingrid Bergman, and Paul Henreid, it became one of Hollywood's most revered films.",
 "'Casablanca premiered this day in 1942. Set in occupied Morocco during World War II, directed by Michael Curtiz, and starring Humphrey Bogart, Ingrid Bergman, and Paul Henreid, it became one of Hollywood's most-revered films.'",
 ["americas","africa"],["1800-1945"],"Here's looking at a classic","In 1942, Casablanca premiered and became a Hollywood legend.")
ev(26,"mongolia","mongolian-peoples-republic-1924","The Mongolian People's Republic is proclaimed","1924",
 "A new state is declared after the defeat of White Russian and Chinese forces.",
 "On November 26, 1924, after the defeat of the White Russians and the Chinese, the Mongolian People's Republic was proclaimed.",
 "1924: 'After the defeat of the White Russians and the Chinese, the Mongolian People's Republic was proclaimed.'",
 ["asia"],["1800-1945"])
ev(26,"nhl","nhl-founded-1917","The National Hockey League is founded","1917",
 "The league begins with four Canadian teams.",
 "On November 26, 1917, the National Hockey League was founded with four Canadian teams. Its first American club, the Boston Bruins, joined in 1924.",
 "1917: 'The National Hockey League was founded and featured four Canadian teams; the first American club, the Boston Bruins, was added in 1924.'",
 ["americas"],["1800-1945"])
ev(26,"curiosity","curiosity-rover-launched-2011","NASA launches the Curiosity rover to Mars","2011",
 "The rover lifts off from Cape Canaveral.",
 "On November 26, 2011, NASA's Mars rover Curiosity was launched from Cape Canaveral, Florida.",
 "2011: 'The Mars rover Curiosity was launched from Cape Canaveral, Florida.'",
 ["americas","global"],["contemporary"])
b.write()
