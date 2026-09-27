from lib import Batch
b = Batch("2026-11-27-12-03-historical-events")
BR = "Encyclopaedia Britannica - "
MONTH = {11: "November", 12: "December"}
def ev(month, day, short, id, title, year, summary, desc, quote, regions, eras, nt=None, nb=None, note=None):
    name = f"{BR}On This Day: {MONTH[month]} {day}"
    url = f"https://www.britannica.com/on-this-day/{MONTH[month]}-{day}"
    b.add(f"{month:02d}-{day:02d}", short, id, title, year, f"{MONTH[month]} {day}, {year}", summary, desc,
          [(name, url, f"Britannica On This Day ({MONTH[month][:3]} {day}): {quote}")], regions, eras, nt, nb, note)
# Nov 27
ev(11,27,"nobel-will","nobel-prizes-established-1895","Alfred Nobel's will creates the Nobel Prizes","1895",
 "The inventor of dynamite's will creates the prizes.",
 "On November 27, 1895, the Nobel Prizes were established through the will of Alfred Nobel, the Swedish chemist, engineer, and industrialist who invented dynamite and other powerful explosives.",
 "'Through the will drawn up by Alfred Bernhard Nobel - the Swedish chemist, engineer, and industrialist who invented dynamite and other, more powerful explosives - the Nobel Prizes were established on this day in 1895.'",
 ["europe","global"],["1800-1945"],"The will behind the Nobel Prizes","In 1895, Alfred Nobel's will established the Nobel Prizes.")
ev(11,27,"macys-parade","first-macys-parade-1924","The first Macy's Thanksgiving Day Parade","1924",
 "A New York tradition begins; the giant balloons arrive three years later.",
 "On November 27, 1924, the first Macy's Thanksgiving Day Parade was held in New York City. It became an American tradition, known especially for the huge balloons introduced in 1927.",
 "1924: 'The first Macy's Thanksgiving Day Parade was held in New York City, and it became an American tradition, especially known for its huge balloons, which were introduced in 1927.'",
 ["americas"],["1800-1945"])
ev(11,27,"neuilly","treaty-of-neuilly-1919","The Treaty of Neuilly sets peace terms for Bulgaria","1919",
 "Bulgaria and the Allies sign a post-World War I settlement.",
 "On November 27, 1919, the Treaty of Neuilly, setting out the post-World War I peace terms for Bulgaria, was signed between Bulgaria and the Allied powers.",
 "1919: 'The Treaty of Neuilly, outlining the post-World War I peace terms for Bulgaria, was signed between the defeated country and the Allied powers.'",
 ["europe"],["1800-1945"])
ev(11,27,"celsius","anders-celsius-born-1701","Anders Celsius is born","1701",
 "The Swedish astronomer is born.",
 "On November 27, 1701, the Swedish astronomer Anders Celsius was born.",
 "Famous birthdays: '1701 Anders Celsius - Swedish astronomer'.",
 ["europe"],["early-modern"],note="Date as given by Britannica; Sweden then used its own transitional calendar.")
# Nov 28
ev(11,28,"albania","albania-independence-1912","Albania declares independence","1912",
 "Delegates at Vlore proclaim an independent Albania.",
 "On November 28, 1912, Albanian national delegates led by Ismail Qemal issued the Vlore proclamation, declaring Albania's independence.",
 "1912: 'Albanian national delegates, led by Ismail Qemal, issued the Vlore proclamation, which declared Albania's independence.'",
 ["europe"],["1800-1945"],"Albania's day of independence","In 1912, Albania declared its independence at Vlore.")
ev(11,28,"lady-astor","lady-astor-takes-seat-1919","Nancy Astor becomes the first woman to sit in the House of Commons","1919",
 "A milestone for women in British politics.",
 "On November 28, 1919, Lady Astor became the first woman to sit in the British House of Commons.",
 "1919: 'Lady Astor became the first woman to sit in the British House of Commons.'",
 ["europe"],["1800-1945"])
ev(11,28,"mauritania","mauritania-independence-1960","Mauritania declares independence","1960",
 "The West African country leaves the French Community.",
 "On November 28, 1960, Mauritania declared its independence and left the French Community.",
 "1960: 'Mauritania declared its independence and left the French Community.'",
 ["africa"],["decolonization","cold-war"])
ev(11,28,"tehran-conference","tehran-conference-opens-1943","The Tehran Conference opens","1943",
 "Roosevelt, Churchill, and Stalin meet in Iran.",
 "On November 28, 1943, the Tehran Conference opened, bringing together US President Franklin D. Roosevelt, British Prime Minister Winston Churchill, and Soviet Premier Joseph Stalin, who pressed for an invasion of France.",
 "'The Tehran Conference, attended by U.S. President Franklin D. Roosevelt, British Prime Minister Winston Churchill, and Soviet Premier Joseph Stalin, at which Stalin pressed for an invasion of France, opened this day in 1943.'",
 ["middle-east","global"],["1800-1945"])
# Nov 29
ev(11,29,"byrd-south-pole","byrd-flies-over-south-pole-1929","Richard Byrd flies over the South Pole","1929",
 "An American aviator crosses the bottom of the world by air.",
 "On November 29, 1929, American pioneer aviator Richard E. Byrd flew over the South Pole.",
 "1929: 'American pioneer aviator Richard E. Byrd flew over the South Pole.'",
 ["americas","global"],["1800-1945"],"Flying over the bottom of the world","In 1929, Richard Byrd flew over the South Pole.")
ev(11,29,"november-uprising","november-insurrection-1830","Polish cadets launch the November Insurrection","1830",
 "An uprising in Warsaw challenges Russian rule.",
 "On November 29, 1830, a Polish secret society of infantry cadets staged an uprising in Warsaw, beginning the November Insurrection.",
 "1830: 'A Polish secret society of infantry cadets staged an uprising in Warsaw, beginning the November Insurrection.'",
 ["europe"],["1800-1945"])
ev(11,29,"alcott","louisa-may-alcott-born-1832","Louisa May Alcott is born","1832",
 "The author of Little Women is born in Pennsylvania.",
 "On November 29, 1832, Louisa May Alcott, best known for her novel Little Women, was born in Pennsylvania.",
 "1832: 'Author Louisa May Alcott, who is best known for her novel Little Women, was born in Pennsylvania.'",
 ["americas"],["1800-1945"])
ev(11,29,"resolution-181","un-partition-resolution-1947","The UN votes to partition Palestine","1947",
 "Resolution 181 proposes Arab and Jewish states and an international Jerusalem.",
 "On November 29, 1947, the United Nations General Assembly passed Resolution 181, which called for the partition of Palestine into Arab and Jewish states and for placing Jerusalem under a special international regime.",
 "'On this day in 1947, the United Nations General Assembly passed Resolution 181, which called for the partition of Palestine into Arab and Jewish states and for placing the city of Jerusalem under a special international regime.'",
 ["middle-east","global"],["1945-present"])
# Nov 30
ev(11,30,"barbados","barbados-independence-1966","Barbados becomes independent","1966",
 "The Caribbean island gains full independence from Britain.",
 "On November 30, 1966, Barbados, which had gained internal self-rule in 1961, achieved full independence from Britain.",
 "'Barbados, an island nation in the Caribbean ... had gained internal self-rule in 1961 and achieved its full independence from Britain on this day in 1966.'",
 ["americas"],["decolonization","cold-war"],"Barbados becomes a nation","In 1966, Barbados achieved full independence from Britain.")
ev(11,30,"treaty-of-paris-prelim","treaty-of-paris-preliminary-1782","Britain and the US sign preliminary peace articles","1782",
 "The first step toward ending the American Revolution is agreed in Paris.",
 "On November 30, 1782, Britain and the United States signed the preliminary articles of the Treaty of Paris, part of the Peace of Paris that concluded the American Revolution.",
 "1782: 'Britain and the United States signed the preliminary articles of the Treaty of Paris as part of the Peace of Paris, a collection of treaties concluding the American Revolution.'",
 ["europe","americas"],["revolutionary"])
ev(11,30,"thriller","thriller-released-1982","Michael Jackson releases Thriller","1982",
 "It becomes the best-selling album in the world.",
 "On November 30, 1982, Michael Jackson released Thriller, which became the best-selling album in the world and won a record eight Grammy Awards.",
 "1982: 'American singer and songwriter Michael Jackson released Thriller, which became the best-selling album in the world and won a record-setting eight Grammy Awards.'",
 ["americas","global"],["contemporary"])
ev(11,30,"stone-of-scone","stone-of-scone-returned-1996","The Stone of Scone returns to Scotland","1996",
 "The coronation stone comes home 700 years after Edward I took it.",
 "On November 30, 1996, the Stone of Scone, a block of gray sandstone, was returned to Scotland 700 years after King Edward I took it to England as war booty.",
 "1996: 'A block of gray sandstone known as the Stone of Scone was returned to Scotland, 700 years after it had been taken to England as war booty by King Edward I.'",
 ["europe"],["contemporary"])
# Dec 1
ev(12,1,"rosa-parks","rosa-parks-arrested-1955","Rosa Parks refuses to give up her bus seat","1955",
 "Her arrest in Montgomery sparks a 381-day boycott.",
 "On December 1, 1955, in Montgomery, Alabama, Rosa Parks refused to give up her bus seat to a white passenger in violation of segregation laws and was arrested, sparking a 381-day bus boycott led by Martin Luther King, Jr.",
 "'This day in 1955, in violation of segregation laws in Montgomery, Alabama, Rosa Parks refused to surrender her bus seat to a white passenger and was arrested, sparking a 381-day bus boycott led by Martin Luther King, Jr.'",
 ["americas"],["1945-present"],"One seat, a movement","In 1955, Rosa Parks refused to give up her bus seat in Montgomery.")
ev(12,1,"assembly-line","ford-moving-assembly-line-1913","Ford debuts the moving assembly line","1913",
 "Model T production is transformed at Highland Park, Michigan.",
 "On December 1, 1913, the world's first moving assembly line debuted at a Ford factory in Highland Park, Michigan, manufacturing Model Ts. Henry Ford's innovation revolutionized the auto industry.",
 "1913: 'The world's first moving assembly line debuted, used in manufacturing Model Ts at a Ford factory in Highland Park, Michigan; the innovation was the idea of owner Henry Ford, and it revolutionized the auto industry.'",
 ["americas"],["1800-1945"])
ev(12,1,"world-aids-day","first-world-aids-day-1988","The first World AIDS Day is held","1988",
 "A global day of awareness begins.",
 "On December 1, 1988, the first World AIDS Day was held.",
 "1988: 'The first World AIDS Day was held.'",
 ["global"],["contemporary"])
ev(12,1,"locarno","pact-of-locarno-signed-1925","The Pact of Locarno is signed","1925",
 "Five European powers sign agreements meant to secure peace in western Europe.",
 "On December 1, 1925, Germany, France, Belgium, Great Britain, and Italy signed the Pact of Locarno, a series of agreements intended to guarantee peace in western Europe.",
 "1925: 'Germany, France, Belgium, Great Britain, and Italy signed the Pact of Locarno, a series of agreements intended to guarantee peace in western Europe.'",
 ["europe"],["1800-1945"])
# Dec 2
ev(12,2,"chicago-pile","first-nuclear-chain-reaction-1942","Scientists achieve the first controlled nuclear chain reaction","1942",
 "Enrico Fermi's team makes history at the University of Chicago.",
 "On December 2, 1942, scientists led by Enrico Fermi conducted the world's first controlled, self-sustaining nuclear chain reaction at the University of Chicago.",
 "1942: 'Scientists led by Enrico Fermi conducted the world's first controlled self-sustaining nuclear chain reaction, at the University of Chicago.'",
 ["americas"],["1800-1945"],"The day the atom was harnessed","In 1942, Enrico Fermi's team ran the first controlled nuclear chain reaction.")
ev(12,2,"uae","uae-formed-1971","The United Arab Emirates is formed","1971",
 "Six emirates on the Arabian Peninsula unite.",
 "On December 2, 1971, the United Arab Emirates was formed by the union of six small emirates on the Arabian Peninsula; a seventh joined in February 1972.",
 "1971: 'The United Arab Emirates was formed by the union of six small emirates on the Arabian Peninsula; a seventh emirate joined in February 1972.'",
 ["middle-east"],["cold-war","decolonization"])
ev(12,2,"napoleon-crowned","napoleon-crowned-emperor-1804","Napoleon crowns himself emperor","1804",
 "The ceremony in Paris takes place in the presence of the pope.",
 "On December 2, 1804, Napoleon crowned himself emperor of France in the presence of Pope Pius VII.",
 "1804: 'Napoleon crowned himself emperor of France in the presence of Pope Pius VII.'",
 ["europe"],["revolutionary"])
ev(12,2,"monroe-doctrine","monroe-doctrine-1823","President Monroe sets out the Monroe Doctrine","1823",
 "The US warns European powers away from the Western Hemisphere.",
 "On December 2, 1823, in his annual message to Congress, President James Monroe declared that the United States would not interfere in European affairs and would treat any European attempt to control a nation in the Western Hemisphere as a hostile act. The policy became known as the Monroe Doctrine.",
 "'On this day in 1823, U.S. President James Monroe used his annual message to Congress to assert that the United States would not interfere in internal European affairs and that any attempt by a European power to control any nation in the Western Hemisphere would be viewed as a hostile act against the United States. His policy became known as the Monroe Doctrine.'",
 ["americas","europe"],["1800-1945"])
# Dec 3
ev(12,3,"heart-transplant","first-heart-transplant-1967","Christiaan Barnard performs the first human heart transplant","1967",
 "A Cape Town surgical team makes medical history.",
 "On December 3, 1967, South African surgeon Christiaan Barnard performed the first human heart transplant, at Groote Schuur Hospital in Cape Town.",
 "1967: 'Christiaan Barnard of South Africa performed the first human heart transplant, at Groote Schuur Hospital in Cape Town.'",
 ["africa"],["1945-present"],"A new heart in Cape Town","In 1967, Christiaan Barnard performed the first human heart transplant.")
ev(12,3,"eureka-stockade","eureka-stockade-1854","Gold miners fight at the Eureka Stockade","1854",
 "Diggers in Victoria, Australia, clash with government forces.",
 "On December 3, 1854, gold miners at the Eureka goldfield in Victoria, Australia, who had barricaded themselves inside a hastily built stockade, opened fire on the government forces surrounding them, the culmination of long-standing grievances.",
 "1854: 'After hastily constructing a fortification and barricading themselves inside, miners (\"diggers\") working in the Eureka goldfield in Victoria, Australia, opened fire on government forces surrounding the stockade, the culmination of long-standing grievances on the part of the diggers.'",
 ["oceania"],["1800-1945"])
ev(12,3,"streetcar","streetcar-named-desire-premieres-1947","A Streetcar Named Desire opens on Broadway","1947",
 "Tennessee Williams's play premieres with Marlon Brando.",
 "On December 3, 1947, Tennessee Williams's A Streetcar Named Desire premiered on Broadway, starring Jessica Tandy, Kim Hunter, and Marlon Brando.",
 "1947: 'Tennessee Williams's A Streetcar Named Desire premiered on Broadway, starring Jessica Tandy, Kim Hunter, and Marlon Brando.'",
 ["americas"],["1945-present"])
ev(12,3,"francis-xavier","francis-xavier-dies-1552","Missionary Francis Xavier dies off the coast of China","1552",
 "The leading Catholic missionary of his era dies of fever.",
 "On December 3, 1552, St. Francis Xavier, the leading Roman Catholic missionary of modern times, died of fever off the coast of China.",
 "1552: 'St. Francis Xavier, the leading Roman Catholic missionary of modern times, died of fever off the coast of China.'",
 ["asia","europe"],["early-modern"],note="Julian calendar date.")
b.write()
