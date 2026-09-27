from lib import Batch
b = Batch("2026-12-11-17-historical-events")
BR = "Encyclopaedia Britannica - "
def ev(day, short, id, title, year, summary, desc, quote, regions, eras, nt=None, nb=None, note=None):
    b.add(f"12-{day:02d}", short, id, title, year, f"December {day}, {year}", summary, desc,
          [(f"{BR}On This Day: December {day}", f"https://www.britannica.com/on-this-day/December-{day}",
            f"Britannica On This Day (Dec {day}): {quote}")], regions, eras, nt, nb, note)
# Dec 11
ev(11,"unicef","unicef-established-1946","UNICEF is established","1946",
 "The UN creates a program for the world's children.",
 "On December 11, 1946, UNICEF, a United Nations program devoted to improving the health, nutrition, education, and general welfare of children, was established.",
 "1946: 'UNICEF - a United Nations program devoted to improving the health, nutrition, education, and general welfare of children - was established.'",
 ["global"],["1945-present"],"A UN agency for every child","In 1946, the United Nations established UNICEF.")
ev(11,"edward-viii","edward-viii-abdication-approved-1936","Edward VIII's abdication takes effect","1936",
 "The king gives up the throne to marry Wallis Simpson.",
 "On December 11, 1936, the abdication of Edward VIII as king of the United Kingdom was formally approved. He remains the only British sovereign to give up the crown voluntarily, which he did to marry the American divorcee Wallis Warfield Simpson.",
 "1936: 'Edward VIII's abdication as king of the United Kingdom was formally approved. He is the only sovereign to voluntarily resign the British crown, which he did to marry American divorcee Wallis Warfield Simpson.'",
 ["europe"],["1800-1945"])
ev(11,"mahfouz","naguib-mahfouz-born-1911","Naguib Mahfouz is born in Cairo","1911",
 "The first Arabic-language Nobel laureate in literature is born.",
 "On December 11, 1911, Naguib Mahfouz, who would become the first Arabic writer awarded the Nobel Prize for Literature, was born in Cairo.",
 "1911: 'Naguib Mahfouz, the first Arabic writer to be awarded the Nobel Prize for Literature, was born in Cairo.'",
 ["africa","middle-east"],["1800-1945"])
ev(11,"sonderbund","sonderbund-formed-1845","Swiss Catholic cantons form the Sonderbund","1845",
 "The league leads to a brief Swiss civil war in 1847.",
 "On December 11, 1845, seven conservative Roman Catholic Swiss cantons formed the Sonderbund to oppose anti-Catholic measures by the Protestant liberal cantons. The tensions ended in a brief civil war in 1847.",
 "1845: 'The Sonderbund was formed by the seven Roman Catholic conservative Swiss cantons ... to oppose anti-Catholic measures by Protestant liberal cantons. These tensions culminated in a brief civil war in 1847.'",
 ["europe"],["1800-1945"])
# Dec 12
ev(12,"paris-agreement","paris-climate-agreement-2015","Nations reach the Paris climate agreement","2015",
 "195 countries agree to limit greenhouse gas emissions.",
 "On December 12, 2015, at a United Nations conference in Paris, 195 countries reached a landmark climate change agreement to limit greenhouse gas emissions; the accord effectively replaced the Kyoto Protocol.",
 "2015: 'A landmark climate change agreement was reached at a UN conference in Paris as 195 countries agreed to limit greenhouse gas emissions; the accord effectively replaced the Kyoto Protocol.'",
 ["global"],["contemporary"],"A global deal on climate","In 2015, 195 countries agreed the Paris climate accord.")
ev(12,"first-motel","first-motel-opens-1925","The world's first motel opens","1925",
 "A roadside stop in California gives the 'mo-tel' its name.",
 "On December 12, 1925, the world's first motel, the Milestone Mo-Tel, opened in San Luis Obispo, California, giving motorists a place to stop between San Francisco and Los Angeles.",
 "'The world's first motel opened on this day in 1925. Located in San Luis Obispo, the Milestone Mo-Tel gave motorists a place to stop as they drove between San Francisco and Los Angeles.'",
 ["americas"],["1800-1945"])
ev(12,"kenya-republic","kenya-becomes-republic-1964","Kenya becomes a republic","1964",
 "The change comes on the first anniversary of independence.",
 "On December 12, 1964, Kenya became a republic on the first anniversary of its independence from Britain.",
 "1964: 'Kenya became a republic on the first anniversary of its independence from Britain.'",
 ["africa"],["decolonization","cold-war"])
ev(12,"munch","edvard-munch-born-1863","Edvard Munch is born","1863",
 "The painter of The Scream is born in Norway.",
 "On December 12, 1863, the Norwegian artist Edvard Munch was born. His painting The Scream became a symbol of modern spiritual anguish, and his work influenced German Expressionism.",
 "'Norwegian artist Edvard Munch, whose painting The Scream (1893) can be seen as a symbol of modern spiritual anguish and whose works greatly influenced German Expressionism in the 20th century, was born this day in 1863.'",
 ["europe"],["1800-1945"])
# Dec 13
ev(13,"tasman-nz","tasman-sights-new-zealand-1642","Abel Tasman sights New Zealand","1642",
 "The Dutch navigator is the first European to see the South Island.",
 "On December 13, 1642, Dutch navigator Abel Tasman became the first European to sight the South Island of New Zealand.",
 "1642: 'Dutch navigator Abel Tasman became the first European to sight South Island, New Zealand.'",
 ["oceania","europe"],["early-modern"],"A first European sighting of New Zealand","In 1642, Abel Tasman sighted New Zealand's South Island.")
ev(13,"council-of-trent","council-of-trent-opens-1545","The Council of Trent opens","1545",
 "The Catholic Church's response to the Reformation begins.",
 "On December 13, 1545, the Council of Trent, the 19th ecumenical council of the Roman Catholic Church, opened in Trent, Italy. It helped revitalize the church in much of Europe after the Protestant Reformation.",
 "1545: 'The Council of Trent, the 19th ecumenical council of the Roman Catholic Church, which helped revitalize the church in many parts of Europe after the Protestant Reformation, opened in Trent, Italy.'",
 ["europe"],["early-modern"],note="Julian calendar date.")
ev(13,"celestine-v","celestine-v-resigns-1294","Pope Celestine V resigns","1294",
 "A hermit pope steps down after five months.",
 "On December 13, 1294, after five months as pope, Celestine V resigned. A former hermit struggling with his duties, he judged that continuing would endanger both the church and his soul.",
 "'After five months as pope, Celestine V resigned on this day in 1294. Because he was struggling to fulfill his duties, after many years living as a hermit in a cave, he decided it would be dangerous for the church and for his soul if he continued.'",
 ["europe"],["medieval"],note="Julian calendar date.")
ev(13,"nova-herculis","nova-herculis-discovered-1934","Nova Herculis flares into view","1934",
 "One of the brightest novas of the 20th century is discovered.",
 "On December 13, 1934, British astronomer J.P.M. Prentice discovered Nova Herculis, one of the brightest novas of the 20th century.",
 "1934: 'British astronomer J.P.M. Prentice discovered Nova Herculis, one of the brightest novas of the 20th century.'",
 ["europe","global"],["1800-1945"])
# Dec 14
ev(14,"amundsen","amundsen-reaches-south-pole-1911","Roald Amundsen reaches the South Pole","1911",
 "The Norwegian explorer's team is the first to reach the pole.",
 "On December 14, 1911, Roald Amundsen, traveling with four companions, 52 dogs, and four sledges, became the first explorer to reach the South Pole.",
 "1911: 'Roald Amundsen - traveling with 4 companions, 52 dogs, and 4 sledges - became the first explorer to reach the South Pole.'",
 ["europe","global"],["1800-1945"],"First to the South Pole","In 1911, Roald Amundsen's team became the first to reach the South Pole.")
ev(14,"oecd","oecd-convention-signed-1960","The OECD convention is signed","1960",
 "Twenty countries sign on to a new economic organization.",
 "On December 14, 1960, the convention establishing the Organisation for Economic Co-operation and Development was signed by 18 European countries, the United States, and Canada.",
 "1960: 'The convention establishing the Organisation for Economic Co-operation and Development was signed by 18 European countries, the United States, and Canada.'",
 ["europe","americas"],["cold-war"])
ev(14,"mankiller","wilma-mankiller-sworn-in-1985","Wilma Mankiller becomes principal chief of the Cherokee Nation","1985",
 "She is the first woman to lead a major Native tribe.",
 "On December 14, 1985, Wilma Mankiller was sworn in as principal chief of the Cherokee Nation, becoming the first woman to serve as chief of a major Native tribe.",
 "1985: 'Wilma Mankiller became the first woman ever to serve as chief of a major Native tribe when she was sworn in as principal chief of the Cherokee Nation.'",
 ["americas"],["contemporary"])
ev(14,"tycho-brahe","tycho-brahe-born-1546","Tycho Brahe is born","1546",
 "The Danish astronomer who mapped the stars before the telescope is born.",
 "On December 14, 1546, the Danish astronomer Tycho Brahe was born. He developed astronomical instruments and fixed the positions of stars before the invention of the telescope, paving the way for later discoveries.",
 "'Danish astronomer Tycho Brahe, born this day in 1546, developed astronomical instruments and measured and fixed the positions of stars in an era before the invention of the telescope, paving the way for future discoveries.'",
 ["europe"],["early-modern"],note="Julian calendar date.")
# Dec 15
ev(15,"bill-of-rights","bill-of-rights-adopted-1791","The US Bill of Rights is adopted","1791",
 "The first ten amendments to the Constitution take effect together.",
 "On December 15, 1791, the first 10 amendments to the US Constitution, known as the Bill of Rights, were adopted as a single unit, guaranteeing individual rights and limiting government.",
 "1791: 'The first 10 amendments to the U.S. Constitution - the Bill of Rights, traditionally defined as a collection of mutually reinforcing guarantees of individual rights and limitations on federal and state governments - were adopted as a single unit.'",
 ["americas"],["revolutionary"],"Ten amendments, one bill of rights","In 1791, the US Bill of Rights was adopted.")
ev(15,"pisa","leaning-tower-reopens-2001","The Leaning Tower of Pisa reopens","2001",
 "The tower welcomes visitors again after a decade of stabilization work.",
 "On December 15, 2001, the Leaning Tower of Pisa reopened after more than 10 years of work to stabilize the structure.",
 "2001: 'The Leaning Tower of Pisa reopened in Pisa, Italy, after more than 10 years of work to stabilize the structure.'",
 ["europe"],["contemporary"])
ev(15,"janet-jagan","janet-jagan-elected-1997","Janet Jagan is elected president of Guyana","1997",
 "She becomes South America's first elected female president.",
 "On December 15, 1997, Janet Jagan was elected president of Guyana, becoming the first elected female president in South America.",
 "1997: 'Janet Jagan was elected president of Guyana, becoming the first elected female president in South America and the first white president of Guyana.'",
 ["americas"],["contemporary"])
ev(15,"nero","nero-born-37","Nero, future Roman emperor, is born","37",
 "The future fifth Roman emperor is born.",
 "On December 15, 37 CE, Nero was born. He became the fifth Roman emperor, infamous for his personal extravagance.",
 "37 ce: 'Nero, who became infamous for his personal debaucheries and extravagances as the fifth Roman emperor, was born.'",
 ["europe"],["ancient"],note="Julian calendar date.")
# Dec 16
ev(16,"boston-tea-party","boston-tea-party-1773","The Boston Tea Party","1773",
 "Protesters dump British East India Company tea into Boston Harbor.",
 "On December 16, 1773, a group of men disguised in Mohawk headdresses, cheered by a crowd of thousands, threw tea belonging to the British East India Company into Boston Harbor in protest against taxes. Britain's punitive response helped push the American colonists closer to war.",
 "'On this day in 1773, a group of men dressed in Mohawk headdresses and cheered by a crowd of thousands threw tea belonging to the British East India Company into Boston Harbor. Britain's punitive response to the Boston Tea Party, which was a protest against taxes, helped push American colonists closer to war.'",
 ["americas","europe"],["revolutionary"],"The tea that stirred a revolution","In 1773, colonists dumped British tea into Boston Harbor.")
ev(16,"jane-austen","jane-austen-born-1775","Jane Austen is born","1775",
 "The novelist of everyday English life is born.",
 "On December 16, 1775, the English writer Jane Austen was born. Her novels of ordinary people in everyday life gave the novel its distinctly modern character.",
 "'English writer Jane Austen, born this day in 1775, gave the novel its distinctly modern character through her treatment of ordinary people in everyday life.'",
 ["europe"],["revolutionary","early-modern"])
ev(16,"cromwell","cromwell-lord-protector-1653","Oliver Cromwell becomes lord protector","1653",
 "Cromwell takes the title as head of England, Scotland, and Ireland.",
 "On December 16, 1653, Oliver Cromwell became lord protector of England, Scotland, and Ireland.",
 "1653: 'Oliver Cromwell became lord protector of England, Scotland, and Ireland.'",
 ["europe"],["early-modern"],note="Julian calendar date.")
ev(16,"avatar","avatar-released-2009","James Cameron's Avatar is released","2009",
 "The science-fiction film earns more than $2.7 billion worldwide.",
 "On December 16, 2009, Avatar, the science-fiction film directed by James Cameron, was released internationally. It went on to earn more than $2.7 billion worldwide and launch a franchise.",
 "2009: 'Avatar, a science-fiction thriller directed by James Cameron, was released internationally. It went on to make more than $2.7 billion worldwide and spawn a film franchise.'",
 ["global"],["contemporary"])
# Dec 17
ev(17,"wright-brothers","wright-brothers-first-flight-1903","The Wright brothers make the first sustained airplane flights","1903",
 "Orville's first flight at Kill Devil Hills lasts 12 seconds.",
 "On December 17, 1903, Orville and Wilbur Wright made the first successful sustained flights in an airplane at Kill Devil Hills, North Carolina. Orville went first, flying 120 feet in 12 seconds.",
 "1903: 'Brothers Orville and Wilbur Wright made the first successful sustained flights in an airplane - Orville first, gliding 120 feet (36.6 metres) through the air in 12 seconds - at Kill Devil Hills, North Carolina.'",
 ["americas"],["1800-1945"],"Twelve seconds that changed travel","In 1903, the Wright brothers made the first sustained airplane flights.")
ev(17,"nzinga","queen-nzinga-dies-1663","Queen Nzinga dies in Matamba","1663",
 "The Mbundu ruler is remembered as the 'mother of the nation' in Angola.",
 "On December 17, 1663, Nzinga, a Mbundu queen, died in Matamba. She is considered the 'mother of the nation' in Angola.",
 "1663: 'Nzinga, a Mbundu queen, died in Matamba. Today she is considered the \"mother of the nation\" in Angola.'",
 ["africa"],["early-modern"])
ev(17,"nafta","nafta-signed-1992","NAFTA is signed","1992",
 "Mexico, Canada, and the United States sign a free trade agreement.",
 "On December 17, 1992, the leaders of Mexico, Canada, and the United States signed the North American Free Trade Agreement. It was replaced by the United States-Mexico-Canada Agreement in 2020.",
 "1992: 'The North American Free Trade Agreement (NAFTA) was signed by the leaders of Mexico, Canada, and the United States. It was replaced by the United States-Mexico-Canada Agreement in 2020.'",
 ["americas"],["contemporary"])
ev(17,"us-cuba","us-cuba-relations-restored-2014","The United States and Cuba restore diplomatic relations","2014",
 "Relations suspended for more than 50 years are reestablished.",
 "On December 17, 2014, the United States and Cuba reestablished diplomatic relations that had been suspended for more than 50 years.",
 "2014: 'The United States and Cuba reestablished diplomatic relations that had been suspended for more than 50 years.'",
 ["americas"],["contemporary"])
b.write()
