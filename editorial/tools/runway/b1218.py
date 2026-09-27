from lib import Batch
b = Batch("2026-12-18-25-historical-events")
BR = "Encyclopaedia Britannica - "
def ev(day, short, id, title, year, summary, desc, quote, regions, eras, nt=None, nb=None, note=None):
    b.add(f"12-{day:02d}", short, id, title, year, f"December {day}, {year}", summary, desc,
          [(f"{BR}On This Day: December {day}", f"https://www.britannica.com/on-this-day/December-{day}",
            f"Britannica On This Day (Dec {day}): {quote}")], regions, eras, nt, nb, note)
# Dec 18
ev(18,"nutcracker","nutcracker-premieres-1892","Tchaikovsky's The Nutcracker premieres","1892",
 "The ballet is first performed at the Mariinsky Theatre in St. Petersburg.",
 "On December 18, 1892, Pyotr Ilyich Tchaikovsky's ballet The Nutcracker was first presented at the Mariinsky Theatre in St. Petersburg, Russia.",
 "1892: 'Pyotr Ilyich Tchaikovsky's ballet The Nutcracker was first presented at the Mariinsky Theatre in St. Petersburg, Russia.'",
 ["europe"],["1800-1945"],"The Nutcracker's first curtain","In 1892, Tchaikovsky's The Nutcracker premiered in St. Petersburg.")
ev(18,"thirteenth-amendment","thirteenth-amendment-in-force-1865","The Thirteenth Amendment abolishes slavery in the US","1865",
 "The constitutional amendment officially enters into force.",
 "On December 18, 1865, the Thirteenth Amendment to the US Constitution officially entered into force, abolishing slavery in the United States.",
 "1865: 'The Thirteenth Amendment to the U.S. Constitution officially entered into force, abolishing slavery in the United States.'",
 ["americas"],["1800-1945"])
ev(18,"piltdown","piltdown-man-announced-1912","The Piltdown Man is announced","1912",
 "A supposed 'missing link' fossil later proves to be a fraud.",
 "On December 18, 1912, a British Museum paleontologist announced the discovery in England of fossil remains said to be an extinct human species. Presented as a missing link between apes and early humans, the Piltdown Man was exposed as a fraud by the 1950s.",
 "'A British Museum paleontologist announced on this day in 1912 that an amateur geologist had discovered the fossil remains of an extinct human species in England. The remains, known as the Piltdown man, were presented as the missing evolutionary link ... but by the 1950s they had been exposed as a fraud.'",
 ["europe"],["1800-1945"])
ev(18,"stradivari","stradivari-dies-1737","Antonio Stradivari dies in Cremona","1737",
 "The famed Italian violin maker dies.",
 "On December 18, 1737, the famed Italian violin maker Antonio Stradivari died in Cremona.",
 "1737: 'Famed Italian violin maker Antonio Stradivari died in Cremona.'",
 ["europe"],["early-modern"])
# Dec 19
ev(19,"christmas-carol","a-christmas-carol-published-1843","Charles Dickens publishes A Christmas Carol","1843",
 "Scrooge's story of redemption becomes an instant classic.",
 "On December 19, 1843, Charles Dickens's A Christmas Carol was published for the first time. Its story of Ebenezer Scrooge's redemption became an instant classic and is still retold on stage and screen.",
 "'A Christmas Carol by Charles Dickens was published for the first time on this day in 1843. It became an instant classic, its story of Ebenezer Scrooge's redemption and the Cratchit family's joy often repeated on stage and screen today.'",
 ["europe"],["1800-1945"],"Bah, humbug, and a classic is born","In 1843, Charles Dickens published A Christmas Carol.")
ev(19,"valley-forge","washington-at-valley-forge-1777","Washington's army goes into winter quarters at Valley Forge","1777",
 "About 11,000 Continental troops camp near British-occupied Philadelphia.",
 "On December 19, 1777, during the American Revolution, General George Washington led 11,000 regulars into winter quarters at Valley Forge, on the Schuylkill River near British-occupied Philadelphia.",
 "1777: 'During the American Revolution, General George Washington led 11,000 regulars to take up winter quarters at Valley Forge on the west bank of the Schuylkill River near Philadelphia, which was occupied by the British.'",
 ["americas"],["revolutionary"])
ev(19,"edith-piaf","edith-piaf-born-1915","Edith Piaf is born in Paris","1915",
 "The singer who made the French chanson world famous is born.",
 "On December 19, 1915, the singer and actress Edith Piaf, whose interpretation of the French chanson made her internationally famous, was born in Paris.",
 "1915: 'Singer and actress Edith Piaf, whose interpretation of the chanson (French ballad) made her internationally famous, was born in Paris.'",
 ["europe"],["1800-1945"])
ev(19,"park-geun-hye","park-geun-hye-elected-2012","South Korea elects its first woman president","2012",
 "Park Geun-hye wins the presidential election.",
 "On December 19, 2012, Park Geun-hye was elected the first female president of South Korea. She later became the country's first democratically elected president to be removed from office, after her impeachment in 2017.",
 "2012: 'Park Geun-Hye became the first female to be elected president of South Korea. She also became the country's first democratically elected president to be removed from office after she was impeached in 2017.'",
 ["asia"],["contemporary"])
# Dec 20
ev(20,"grimm","grimms-fairy-tales-published-1812","The Brothers Grimm publish their first volume of tales","1812",
 "Stories like Hansel and Gretel and Snow White reach print.",
 "On December 20, 1812, the brothers Jacob and Wilhelm Grimm published the first volume of the stories that made them famous. Their collection, known as Grimm's Fairy Tales, includes 'Hansel and Gretel', 'Snow White', 'Little Red Riding Hood', and 'Sleeping Beauty'.",
 "'On this day in 1812 brothers Jacob and Wilhelm Grimm published the first volume of stories that would make them famous. Among the 200 or so stories that became known collectively as Grimm's Fairy Tales are \"Hansel and Gretel,\" \"Snow White,\" \"Little Red Riding Hood,\" and \"Sleeping Beauty.\"'",
 ["europe"],["1800-1945"],"Once upon a time, in print","In 1812, the Brothers Grimm published their first volume of fairy tales.")
ev(20,"macau","macau-handover-1999","Macau returns to Chinese sovereignty","1999",
 "Centuries of Portuguese rule come to an end.",
 "On December 20, 1999, centuries of Portuguese rule in Macau ended when it became a special administrative region under Chinese sovereignty, 12 years after China and Portugal agreed on its status.",
 "1999: 'Centuries of Portuguese rule ended in Macau when it became a special administrative region under Chinese sovereignty, 12 years after China and Portugal reached an agreement on its status.'",
 ["asia","europe"],["contemporary"])
ev(20,"wonderful-life","its-a-wonderful-life-premieres-1946","It's a Wonderful Life premieres","1946",
 "Frank Capra's drama becomes a holiday classic.",
 "On December 20, 1946, Frank Capra's It's a Wonderful Life, starring Jimmy Stewart, premiered. It later became a holiday classic.",
 "1946: 'Frank Capra's It's a Wonderful Life, a drama starring Jimmy Stewart, premiered and later became a holiday classic.'",
 ["americas"],["1945-present"])
ev(20,"south-carolina-secedes","south-carolina-secedes-1860","South Carolina secedes from the Union","1860",
 "The first state to leave follows Lincoln's election.",
 "On December 20, 1860, following Abraham Lincoln's election as president, South Carolina became the first US state to secede from the Union.",
 "1860: 'Following Abraham Lincoln's election as U.S. president, South Carolina became the first U.S. state to secede from the Union.'",
 ["americas"],["1800-1945"])
# Dec 21
ev(21,"basketball","first-basketball-game-1891","The first game of basketball is played","1891",
 "James Naismith's new game debuts in a Springfield school gym.",
 "On December 21, 1891, the first game of basketball, organized by gym teacher James Naismith, was played at a school in Springfield, Massachusetts. The chaotic game prompted Naismith to develop the sport's original rules.",
 "'Gym teacher James Naismith organized the first game of basketball, which was played on this day in 1891 in a school in Springfield, Massachusetts. Chaos reigned and a fight broke out, which prompted Naismith to develop the sport's original rules.'",
 ["americas"],["1800-1945"],"Tip-off for a new sport","In 1891, the first game of basketball was played in Springfield.")
ev(21,"apollo-8","apollo-8-launched-1968","Apollo 8 launches toward the Moon","1968",
 "The mission will complete ten lunar orbits.",
 "On December 21, 1968, Apollo 8 was launched from Cape Kennedy and went on to complete 10 orbits of the Moon.",
 "1968: 'Apollo 8 was launched from Cape Kennedy (Cape Canaveral) and eventually completed 10 lunar orbits.'",
 ["americas","global"],["space-age","cold-war"])
ev(21,"radium","curies-discover-radium-1898","Marie and Pierre Curie discover radium","1898",
 "The radioactive element is later used in early cancer treatment.",
 "On December 21, 1898, Marie and Pierre Curie discovered the radioactive element radium, a silvery white metal later used in early cancer treatment.",
 "1898: 'Future Nobel Prize winners Marie Curie and Pierre Curie discovered the radioactive chemical element radium, a silvery white metal used in early cancer treatment.'",
 ["europe"],["1800-1945"])
ev(21,"crossword","first-crossword-puzzle-1913","The first modern crossword puzzle is published","1913",
 "The New York World introduces a new kind of word game.",
 "On December 21, 1913, the New York World published the first modern crossword puzzle.",
 "1913: 'The New York World published the first modern crossword puzzle.'",
 ["americas"],["1800-1945"])
# Dec 22
ev(22,"brandenburg-gate","brandenburg-gate-reopens-1989","The Brandenburg Gate reopens","1989",
 "Berliners pass through the gate for the first time since 1961.",
 "On December 22, 1989, the Brandenburg Gate in Berlin reopened as East and West Germany moved toward reunification. Berliners had been unable to use the gate since the Berlin Wall blocked it in 1961.",
 "'On this day in 1989, the Brandenburg Gate in Berlin was reopened as East and West Germany continued moving toward reunification. Berliners had been unable to use the gate since 1961, when the newly built Berlin Wall blocked access to it.'",
 ["europe"],["cold-war"],"Through the Brandenburg Gate again","In 1989, Berlin's Brandenburg Gate reopened after 28 years.")
ev(22,"ramanujan","srinivasa-ramanujan-born-1887","Srinivasa Ramanujan is born","1887",
 "The self-taught Indian mathematician is born in Erode.",
 "On December 22, 1887, Srinivasa Ramanujan, a self-taught Indian mathematician who made many pioneering discoveries, was born in Erode in present-day Tamil Nadu.",
 "1887: 'Srinivasa Ramanujan, a self-taught Indian mathematician who made many pioneering discoveries ... was born in Erode in what is today Tamil Nadu.'",
 ["asia"],["1800-1945"])
ev(22,"dominican-order","dominican-order-sanctioned-1216","The pope sanctions the Dominican order","1216",
 "Honorius III approves the Order of Preachers.",
 "On December 22, 1216, the Dominican order was sanctioned by Pope Honorius III.",
 "1216: 'The Dominican order was sanctioned by Pope Honorius III.'",
 ["europe"],["medieval"],note="Julian calendar date.")
ev(22,"eclipse-968","earliest-solar-corona-account-968","Leo the Deacon records the solar corona during an eclipse","968",
 "Leo the Deacon's account is the earliest datable description of the corona.",
 "On December 22, 968, an eclipse of the Sun occurred, and Leo the Deacon described a feeble glow, like a narrow headband, around the darkened Sun: the earliest account of the solar corona that can be linked to a datable eclipse.",
 "968: 'At the winter solstice there was an eclipse of the Sun ... wrote Leo the Deacon in the earliest account of the solar corona that can be definitely linked to a datable eclipse.'",
 ["europe","middle-east"],["medieval"],note="Julian calendar date.")
# Dec 23
ev(23,"washington-resigns","washington-resigns-commission-1783","George Washington resigns as commander in chief","1783",
 "The victorious general returns his commission to Congress.",
 "On December 23, 1783, before the Continental Congress, George Washington resigned as commander in chief of the Continental Army.",
 "1783: 'Before the Continental Congress, George Washington resigned as commander in chief of the Continental Army.'",
 ["americas"],["revolutionary"],"The general who gave back his power","In 1783, George Washington resigned as commander in chief.")
ev(23,"ottoman-constitution","ottoman-constitution-1876","The Ottoman Empire's first constitution takes effect","1876",
 "The document gives the sultan full executive power.",
 "On December 23, 1876, the first comprehensive constitution of the Ottoman Empire went into effect, giving the sultan full executive power.",
 "1876: 'The first comprehensive constitution of the Ottoman Empire went into effect, giving the sultan full executive power.'",
 ["europe","middle-east"],["1800-1945"])
ev(23,"federal-reserve","federal-reserve-act-1913","The Federal Reserve System is created","1913",
 "President Wilson signs the Federal Reserve Act.",
 "On December 23, 1913, President Woodrow Wilson signed the Federal Reserve Act, bringing the Federal Reserve System into being.",
 "1913: 'With the signing of the Federal Reserve Act by U.S. President Woodrow Wilson, the Federal Reserve System came into being.'",
 ["americas"],["1800-1945"])
ev(23,"champollion","champollion-born-1790","Jean-Francois Champollion is born","1790",
 "The French linguist and historian is born.",
 "On December 23, 1790, the French historian and linguist Jean-Francois Champollion was born.",
 "Famous birthdays: '1790 Jean-Francois Champollion - French historian and linguist'.",
 ["europe"],["revolutionary"])
# Dec 24
ev(24,"phonograph","edison-phonograph-patent-application-1877","Edison applies to patent the phonograph","1877",
 "The 'speaking machine' is filed with the US Patent Office.",
 "On December 24, 1877, the US Patent Office received Thomas Edison's application for a 'Phonograph or Speaking Machine'. He had first demonstrated the device weeks earlier by playing back his recorded voice.",
 "'On this day in 1877, the U.S. Patent Office received an application for a \"Phonograph or Speaking Machine\" from inventor Thomas Edison. He had demonstrated the device for the first time a few weeks earlier, when he played his recorded ... voice to magazine staffers in New York City.'",
 ["americas"],["1800-1945"],"A machine that talks back","In 1877, Thomas Edison applied to patent the phonograph.")
ev(24,"treaty-of-ghent","treaty-of-ghent-1814","The Treaty of Ghent ends the War of 1812","1814",
 "The United States and Britain make peace in Belgium.",
 "On December 24, 1814, the United States and Great Britain signed the Treaty of Ghent in Belgium, ending the War of 1812.",
 "1814: 'The United States and Great Britain signed the Treaty of Ghent in Belgium, ending the War of 1812.'",
 ["americas","europe"],["1800-1945"])
ev(24,"libya","libya-independence-1951","Libya becomes independent under King Idris I","1951",
 "The new North African kingdom crowns its first monarch.",
 "On December 24, 1951, Idris I became the first king of newly independent Libya.",
 "1951: 'Idris I became the first king of newly independent Libya.'",
 ["africa","middle-east"],["decolonization","1945-present"])
ev(24,"king-john","king-john-born-1167","King John of England is born","1167",
 "The king who sealed Magna Carta is born.",
 "On December 24, 1167, the future King John of England was born. He lost Normandy and most of his French lands and was forced by rebellious barons to seal Magna Carta in 1215.",
 "'King John of England (1199-1216), born this day in 1167, lost Normandy and almost all his other French possessions in a war with France and was forced to seal the Magna Carta (1215) following an English baronial revolt.'",
 ["europe"],["medieval"],note="Julian calendar date.")
# Dec 25
ev(25,"jwst","james-webb-telescope-launched-2021","The James Webb Space Telescope launches","2021",
 "Hubble's successor lifts off on Christmas Day.",
 "On December 25, 2021, the James Webb Space Telescope, designed and built by US, Canadian, and European space agencies as the successor to the Hubble Space Telescope, was launched into space.",
 "2021: 'The James Webb Space Telescope - designed and built by U.S., Canadian, and European space agencies as the successor to the Hubble Space Telescope - was launched into space.'",
 ["global"],["contemporary"],"A Christmas gift to astronomy","On Christmas Day 2021, the James Webb Space Telescope launched.")
ev(25,"charlemagne","charlemagne-crowned-emperor-800","Charlemagne is crowned emperor","800",
 "The king of the Franks becomes the first emperor of the Holy Roman Empire.",
 "On December 25, 800, Charlemagne, king of the Franks, became the first emperor of what became the Holy Roman Empire.",
 "800: 'Charlemagne, king of the Franks, became the first emperor of the Holy Roman Empire.'",
 ["europe"],["medieval"],note="Julian calendar date.")
ev(25,"william-crowned","william-i-crowned-1066","William the Conqueror is crowned king of England","1066",
 "The coronation completes the Norman Conquest.",
 "On December 25, 1066, William I was crowned king of England, formally completing the Norman Conquest.",
 "1066: 'William I was crowned king of England, formally completing the Norman Conquest.'",
 ["europe"],["medieval"],note="Julian calendar date.")
ev(25,"delaware-crossing","washington-crosses-delaware-1776","Washington crosses the Delaware","1776",
 "A Christmas night crossing leads to a surprise attack at Trenton.",
 "On December 25, 1776, during the American Revolution, General George Washington crossed the Delaware River and surprised the British at Trenton, New Jersey.",
 "1776: 'During the American Revolution, General George Washington crossed the Delaware River and surprised the British at Trenton, New Jersey.'",
 ["americas"],["revolutionary"])
ev(25,"gorbachev-resigns","gorbachev-resigns-1991","Mikhail Gorbachev resigns as Soviet president","1991",
 "The Soviet Union ceases to exist at the end of the year.",
 "On December 25, 1991, Mikhail Gorbachev resigned the presidency of the Soviet Union, which ceased to exist at the end of the year.",
 "1991: 'Mikhail Gorbachev resigned the presidency of the Soviet Union, which ceased to exist at the end of the year.'",
 ["europe","asia"],["cold-war","contemporary"])
b.write()
