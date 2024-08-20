/*
*   Copyright 2019 Marco Gomiero
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*
*/

package com.prof18.rssparser.rss

import com.prof18.rssparser.BaseXmlParserTest
import com.prof18.rssparser.model.RssImage
import com.prof18.rssparser.model.ItunesItemData
import com.prof18.rssparser.model.ItunesChannelData
import com.prof18.rssparser.model.ItunesOwner

class XmlParserCharsetFeedTest : BaseXmlParserTest(
    feedPath = "feed-test-charset.xml",
    channelTitle = "Lørdagsrådet",
    channelLink = "https://radio.nrk.no/podkast/loerdagsraadet",
    channelDescription = "Hver uke blar tre (mer eller mindre) kvalifiserte rådgivere i sitt livserfaringsarkiv for å hjelpe deg. Lurer du på om du skal gjøre det slutt, er naboen din en tyrann eller er du håpløst forelsket? Vi skal prøve å gi deg gode råd. <a href=\"https://radio.nrk.no/podkast/loerdagsraadet?utm_source=thirdparty&utm_medium=rss&utm_content=podcastseries%3Aloerdagsraadet\">Hør alle episodene først i appen NRK Radio</a>",
    channelImage = RssImage(
        title = "Lørdagsrådet",
        url = "https://gfx.nrk.no/B4Wi9oIWqfMo0PBc0Hk28AhpXzswFi4Ir3NcS4uO23PA.png",
        link = "https://radio.nrk.no/podkast/loerdagsraadet",
        description = null
    ),
    channelItunesData = ItunesChannelData(
        author = "NRK",
        categories = listOf("Comedy"),
        duration = null,
        explicit = "no",
        image = "https://gfx.nrk.no/B4Wi9oIWqfMo0PBc0Hk28AhpXzswFi4Ir3NcS4uO23PA.png",
        keywords = listOf(),
        newsFeedUrl = null,
        owner = ItunesOwner(
            name = "NRK",
            email = "nrkpodkast@nrk.no",
        ),
        subtitle = null,
        summary = "Hver uke blar tre (mer eller mindre) kvalifiserte rådgivere i sitt\n" +
                "            livserfaringsarkiv for å hjelpe deg. Lurer du på om du skal gjøre det slutt, er naboen\n" +
                "            din en tyrann eller er du håpløst forelsket? Vi skal prøve å gi deg gode råd. <a\n" +
                "            href=\"https://radio.nrk.no/podkast/loerdagsraadet?utm_source=thirdparty&utm_medium=rss&utm_content=podcastseries%3Aloerdagsraadet\">Hør\n" +
                "            alle episodene først i appen NRK Radio</a>",
        type = null

    ),
    articleGuid = "l_e6338d4f-2777-43c4-b38d-4f2777e3c4de",
    articleTitle = "Høydepunkter fra 2022 - nr.11",
    articlePubDate = "Sat, 31 Dec 2022 08:00:00 GMT",
    articleDescription = "Høydepunkter fra 2022 - nr.11. <a href=\"https://radio.nrk.no/podkast/loerdagsraadet/l_e6338d4f-2777-43c4-b38d-4f2777e3c4de?utm_source=thirdparty&utm_medium=rss&utm_content=podcast%3Al_e6338d4f-2777-43c4-b38d-4f2777e3c4de\">Hør episoden i appen NRK Radio</a>",
    articleAudio = "https://podkast.nrk.no/fil/loerdagsraadet/23ee45fc-d869-4110-bf97-34138fcfaba6_0_ID192MP3.mp3",
    articleItunesData = ItunesItemData(
        author = "NRK",
        duration = "01:03:44",
        episode = null,
        episodeType = null,
        explicit = "no",
        image = "https://gfx.nrk.no/sMxLsuFaNFBBmKJXuT2L6g6EwwmgKMvBujBOVc4N01pw.png",
        keywords = listOf("NRK", " Lørdagsrådet"),
        subtitle = null,
        summary = "Høydepunkter fra 2022 - nr.11. <a\n" +
                "                href=\"https://radio.nrk.no/podkast/loerdagsraadet/l_e6338d4f-2777-43c4-b38d-4f2777e3c4de?utm_source=thirdparty&utm_medium=rss&utm_content=podcast%3Al_e6338d4f-2777-43c4-b38d-4f2777e3c4de\">Hør\n" +
                "                episoden i appen NRK Radio</a>",
        season = null,
    ),
)
