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

class XmlParserAudioFeedTest : BaseXmlParserTest(
    feedPath = "feed-test-audio.xml",
    channelTitle = "Stuff You Should Know",
    channelLink = "https://www.howstuffworks.com",
    channelDescription = "If you've ever wanted to know about champagne, satanism, the Stonewall Uprising, chaos theory, LSD, El Nino, true crime and Rosa Parks, then look no further. Josh and Chuck have you covered.",
    channelImage = RssImage(
        title = "Stuff You Should Know",
        url = "https://megaphone-prod.s3.amazonaws.com/podcasts/1e705dd4-2de6-11e8-b55d-9ba6ddb3f75e/image/uploads_2F1546996139536-0o3pw93d8mk-d5f1143c14a746754c55efb478c66988_2FSKSKLogo-FINAL-iHR-3000x3000.png",
        link = "https://www.howstuffworks.com",
        description = null
    ),
    channelItunesData = ItunesChannelData(
        author = "iHeartRadio",
        categories = listOf("Society & Culture"),
        duration = null,
        explicit = "no",
        image = "https://megaphone-prod.s3.amazonaws.com/podcasts/1e705dd4-2de6-11e8-b55d-9ba6ddb3f75e/image/uploads_2F1546996139536-0o3pw93d8mk-d5f1143c14a746754c55efb478c66988_2FSKSKLogo-FINAL-iHR-3000x3000.png",
        keywords = listOf(),
        newsFeedUrl = "https://feeds.megaphone.fm/stuffyoushouldknow",
        owner = ItunesOwner(
            name = "iHeartRadio",
            email = "applepodcast@howstuffworks.com",
        ),
        subtitle = "Stuff You Should Know",
        summary = "If you've ever wanted to know about champagne, satanism, the Stonewall Uprising, chaos theory, LSD, El Nino, true crime and Rosa Parks, then look no further. Josh and Chuck have you covered.",
        type = "episodic"
    ),
    articleGuid = "c41042ae-5460-11e8-b38c-5f5faf9dd0c5",
    articleTitle = "SYSK Selects: How Gold Works",
    articlePubDate = "Sat, 07 Mar 2020 10:00:00 -0000",
    articleDescription = "As of early 2013, only 161,00 metric tons of gold had been mined in the entire history of the world. Considering about 85 percent of it is recycled, there's a decent chance your jewelry may once have been part of an Incan headdress or Mycenaean face mask. Dive in to gold in this classic episode.\n" +
            "                Learn more about your ad-choices at https://news.iheart.com/podcast-advertisers",
    articleContent = "<p>As of early 2013, only 161,00 metric tons of gold had been mined in the entire history of the world. Considering about 85 percent of it is recycled, there's a decent chance your jewelry may once have been part of an Incan headdress or Mycenaean face mask. Dive in to gold in this classic episode.</p><p> </p> Learn more about your ad-choices at <a href=\"https://news.iheart.com/podcast-advertisers\">https://news.iheart.com/podcast-advertisers</a>",
    articleAudio = "https://www.podtrac.com/pts/redirect.mp3/chtbl.com/track/5899E/traffic.megaphone.fm/HSW3157672398.mp3",
    articleItunesData = ItunesItemData(
        author = "iHeartRadio",
        duration = "3122",
        episode = null,
        episodeType = "full",
        explicit = null,
        image = "http://megaphone-prod.s3.amazonaws.com/podcasts/c41042ae-5460-11e8-b38c-5f5faf9dd0c5/image/uploads_2F1583260134090-ua5pbey0yx-25644ab8bbd946b90180c8601dbe70c1_2FSKSKLogo-FINAL-iHR-3000x3000.png",
        keywords = listOf(),
        subtitle = "As of early 2013, only 161,00 metric tons of gold had been mined in the entire history of the world. Considering about 85 percent of it is recycled, decent chance your jewelry may once have been part of an Incan headdress or Mycenaean face mask. Dive in to gold in this classic episode.",
        summary = "As of early 2013, only 161,00 metric tons of gold had been mined in the entire history of the world. Considering about 85 percent of it is recycled, there's a decent chance your jewelry may once have been part of an Incan headdress or Mycenaean face mask. Dive in to gold in this classic episode.\n" +
                "                Learn more about your ad-choices at https://news.iheart.com/podcast-advertisers",
        season = null,
    ),
)
