# Quiz Source Link Audit (2026-09-28)

Owner-requested check of every source cited by the 150 published quiz
questions, after a broken D-Day link was found in the World War ordering
question.

## Method

1. Collected all 203 unique source and image URLs from published questions.
2. Ran a paced HTTP check (`editorial/tools/check_quiz_links.py`): 92 returned
   200, 20 returned 404/410/no response, and 91 returned 403/429.
3. Opened every non-200 URL in a real browser. Most 403/429 results were bot
   blocking on live pages (Britannica, UNESCO, the Met). Pages that loaded a
   "page not found" screen, or never loaded, were marked dead.
4. For each dead source, opened a replacement page and confirmed it directly
   states the question's answer and explanation before substituting it.

Result: 30 dead URLs across 25 questions; 19 of those questions had no live
source left. All 25 now cite live pages that support them.

Pages behind a Cloudflare "Just a moment" challenge in the browser were left
unchanged (NZHistory, Library of Congress, National Portrait Gallery,
Smithsonian NMAAHC and NMAI, NHCP Philippines). They are live for readers but
could not be re-read by automation; recheck them by hand when convenient.

## Replacements

| Question(s) | Dead URL | Replacement (verified 2026-09-28) |
| --- | --- | --- |
| aksum-conversion-ruler, order-africa-middle-east-milestones | https://www.metmuseum.org/essays/the-kingdom-of-aksum | [The Metropolitan Museum of Art - African Christianity in Ethiopia](https://www.metmuseum.org/essays/african-christianity-in-ethiopia) |
| kush-royal-city-meroe | https://www.britannica.com/place/Meroe-ancient-city-Sudan | [Encyclopaedia Britannica - Meroe](https://www.britannica.com/place/Meroe) |
| india-independence-1947 | https://www.parliament.uk/about/living-heritage/transformingsociety/tradeindustry/empire/collections1/collections2/indian-independence-act-1947/ | [Encyclopaedia Britannica - Indian Independence Act](https://www.britannica.com/topic/Indian-Independence-Act-1947) |
| maya-writing-system | https://www.metmuseum.org/essays/maya-writing | [Encyclopaedia Britannica - Maya hieroglyphic writing](https://www.britannica.com/topic/Maya-hieroglyphic-writing) |
| d-day-operation-name, order-world-war-milestones | https://www.archives.gov/milestone-documents/general-order-143 | [Encyclopaedia Britannica - Normandy Invasion](https://www.britannica.com/event/Normandy-Invasion) |
| bletchley-park-purpose | https://bletchleypark.org.uk/our-story/the-story-of-bletchley-park/ | [Encyclopaedia Britannica - Bletchley Park](https://www.britannica.com/place/Bletchley-Park) |
| manhattan-project-objective | https://www.energy.gov/lm/manhattan-project-national-historical-park | [Encyclopaedia Britannica - Manhattan Project](https://www.britannica.com/event/Manhattan-Project) |
| united-states-league-member | https://www.senate.gov/about/powers-procedures/treaties/versailles-treaty.htm | [U.S. Office of the Historian - The League of Nations, 1920](https://history.state.gov/milestones/1914-1920/league) |
| identify-winston-churchill | https://www.iwm.org.uk/history/winston-churchill-the-war-leader | [Encyclopaedia Britannica - Winston Churchill](https://www.britannica.com/biography/Winston-Churchill) |
| first-world-war-tanks-flers-courcelette, order-september-15-milestones | https://www.iwm.org.uk/history/how-tanks-first-appeared-on-the-battlefield | [Encyclopaedia Britannica - First Battle of the Somme](https://www.britannica.com/event/First-Battle-of-the-Somme) |
| nightingale-sanitation-statistics | https://www.nationalarchives.gov.uk/education/resources/florence-nightingale/ | [Science Museum - Florence Nightingale: The pioneer statistician](https://www.sciencemuseum.org.uk/objects-and-stories/florence-nightingale-pioneer-statistician) |
| mossadegh-oil-nationalization | https://history.state.gov/milestones/1953-1960/iran | [Encyclopaedia Britannica - Mohammad Mosaddegh](https://www.britannica.com/biography/Mohammad-Mosaddegh) |
| australian-referendum-1967-misconception | https://australian.museum/learn/first-nations/1967-referendum/ | [Australian Electoral Commission - Electoral milestones for Indigenous Australians](https://www.aec.gov.au/indigenous/milestones.htm) |
| triple-alliance-italy-member | https://www.iwm.org.uk/history/the-causes-of-world-war-one | [Encyclopaedia Britannica - Triple Alliance](https://www.britannica.com/event/Triple-Alliance-Europe-1882-1915) |
| order-pacific-independence-milestones | https://www.nationalarchives.gov.fj/ | [Commonwealth - Fiji](https://thecommonwealth.org/our-member-countries/fiji) |
| order-pacific-independence-milestones | https://vanuatu.gov.vu/ | [Commonwealth - Vanuatu](https://thecommonwealth.org/our-member-countries/vanuatu) |
| order-women-heads-government | https://www.parliament.uk/about/living-heritage/evolutionofparliament/parliamentaryauthority/premiers/overview/margaret-thatcher/ | [Encyclopaedia Britannica - Margaret Thatcher](https://www.britannica.com/biography/Margaret-Thatcher) |
| dutch-east-india-company-purpose | https://www.nationaalarchief.nl/en/research/archive-collection/1.04.02 | [Encyclopaedia Britannica - Dutch East India Company](https://www.britannica.com/topic/Dutch-East-India-Company) |
| british-slavery-abolition-act-1833 | https://www.parliament.uk/about/living-heritage/transformingsociety/tradeindustry/slavetrade/overview/abolition/ | [Encyclopaedia Britannica - Slavery Abolition Act](https://www.britannica.com/topic/Slavery-Abolition-Act) |
| order-constitutional-milestones | https://www.parliament.uk/about/living-heritage/evolutionofparliament/legislativescrutiny/parliament-and-the-constitution/overview/magnacarta/ | [Encyclopaedia Britannica - Magna Carta](https://www.britannica.com/topic/Magna-Carta) |
| order-constitutional-milestones | https://www.parliament.uk/about/living-heritage/evolutionofparliament/parliamentaryauthority/revolution/overview/rights/ | [Encyclopaedia Britannica - Bill of Rights (British history)](https://www.britannica.com/topic/Bill-of-Rights-British-history) |
| order-modern-communication-milestones | https://www.uspto.gov/about-us/news-updates/telegraph-patent | [IEEE REACH - Samuel Morse, American Electro-Magnet Telegraph Patent and Morse Code](https://reach.ieee.org/primary-sources/samuel-morse-american-electro-magnet-telegraph-patent-and-morse-code/) |
| order-modern-communication-milestones | https://www.uspto.gov/about-us/news-updates/bells-telephone-patent | [Encyclopaedia Britannica - Alexander Graham Bell](https://www.britannica.com/biography/Alexander-Graham-Bell) |
| order-modern-communication-milestones | https://www.internethistory.ucla.edu/internet/msg.html | [University of California - Lo and behold: The internet](https://www.universityofcalifornia.edu/news/lo-and-behold-internet) |
| order-modern-communication-milestones | https://home.cern/science/computing/birth-web/short-history-web | [CERN - The birth of the Web](https://home.cern/science/computing/the-birth-of-the-web/) |
| order-cold-war-milestones | https://www.nato.int/cps/en/natohq/declassified_137930.htm | [Encyclopaedia Britannica - North Atlantic Treaty Organization](https://www.britannica.com/topic/North-Atlantic-Treaty-Organization) |
| order-cold-war-milestones | https://www.wilsoncenter.org/article/warsaw-pact | [Encyclopaedia Britannica - Warsaw Pact](https://www.britannica.com/event/Warsaw-Pact) |
| order-cold-war-milestones | https://www.dhm.de/lemo/kapitel/geteiltes-deutschland-gruenderjahre/berlin-mauerbau.html | [Encyclopaedia Britannica - Berlin Wall](https://www.britannica.com/topic/Berlin-Wall) |
| order-cold-war-milestones | https://www.dhm.de/lemo/kapitel/friedliche-revolution/der-mauerfall.html | [U.S. Office of the Historian - Fall of Communism in Eastern Europe, 1989](https://history.state.gov/milestones/1989-1992/fall-of-communism) |
| identify-charles-darwin | https://www.nhm.ac.uk/discover/charles-darwin-most-famous-scientist.html | [Encyclopaedia Britannica - Charles Darwin](https://www.britannica.com/biography/Charles-Darwin) |

## Wording changes

Where a replacement source did not state every detail of an explanation, the
wording was narrowed to what the live source supports. The answer and options
of every question are unchanged.

| Question | Field | Change |
| --- | --- | --- |
| aksum-conversion-ruler | explanation | Christianity was adopted in Ethiopia during the fourth-century reign of the Aksumite emperor Ezana, making Aksum one of the earliest Christian states. |
| kush-royal-city-meroe | prompt, explanation | Which city became the capital and principal royal residence of the Kingdom of Kush? / Meroe, in what is now Sudan, became the capital of Kush and the main residence of its rulers; most later royal burials were made there. |
| maya-writing-system | explanation | Maya scribes used a script of more than 800 characters, combining word signs with signs for syllables, in stone inscriptions and a few surviving books. |
| nightingale-sanitation-statistics | explanation | Nightingale's polar area diagram showed that more soldiers died of preventable diseases caused by unsanitary conditions than of battlefield wounds, strengthening the case for sanitary reform. |
| mossadegh-oil-nationalization | explanation | Mossadegh, Iran's premier from 1951 to 1953, nationalized the huge British oil holdings in Iran. |
| australian-referendum-1967-misconception | explanation | The 1967 referendum let Indigenous Australians be counted in the census and let the Commonwealth make laws for them; the federal right to vote had been extended to all Indigenous Australians in 1962. |
| british-slavery-abolition-act-1833 | explanation | The 1833 act abolished slavery in most British colonies, freeing more than 800,000 enslaved Africans in the Caribbean and South Africa as well as a small number in Canada. |
| identify-charles-darwin | explanation | Charles Darwin's theory of evolution by natural selection became the foundation of modern evolutionary studies. Alfred Russel Wallace formulated the theory independently, before Darwin's published work. |

## World War ordering question

`order-world-war-milestones` mixed a First World War event (the 1918
Armistice) into an otherwise Second World War set, under a prompt that only
said "World War". Per owner direction it is now `retired`, keeping its stable
ID for existing history, and replaced by `order-second-world-war-milestones`:

- Prompt: "Put these Second World War events in chronological order, earliest first."
- Items: Germany invades Poland (1 Sep 1939), Japan attacks Pearl Harbor
  (7 Dec 1941), Allied forces land in Normandy on D-Day (6 Jun 1944), Japan
  formally surrenders aboard USS Missouri (2 Sep 1945).
- Sources: USHMM (Poland), Britannica (Pearl Harbor, Normandy Invasion),
  U.S. National Archives (surrender), each opened and confirmed.

The published total stays at 150.

## Rerunning

```sh
python3 editorial/tools/check_quiz_links.py
```

Exit code 1 means at least one URL returned 404/410. Treat `blocked` and
`error` rows as "open in a browser", not as broken.
