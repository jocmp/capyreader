package com.prof18.rssparser.rss

import com.prof18.rssparser.BaseXmlParserTest
import com.prof18.rssparser.model.RssImage
import com.prof18.rssparser.model.ItunesItemData
import com.prof18.rssparser.model.ItunesChannelData
import com.prof18.rssparser.model.ItunesOwner

class XmlParserImageChannelReverseTest : BaseXmlParserTest(
    feedPath = "feed-test-image-channel-reverse.xml",
    channelTitle = "The Joe Rogan Experience",
    channelLink = "https://www.joerogan.com",
    channelDescription = "The podcast of Comedian Joe Rogan..",
    channelImage = RssImage(
        title = "The Joe Rogan Experience",
        url = "http://static.libsyn.com/p/assets/7/1/f/3/71f3014e14ef2722/JREiTunesImage2.jpg",
        link = "https://www.joerogan.com",
        description = null,
    ),
    channelLastBuildDate = "Sat, 04 Jan 2020 01:06:48 +0000",
    channelItunesData = ItunesChannelData(
        author = "Joe Rogan",
        categories = listOf("Comedy", "Society & Culture", "Technology", "Podcasting"),
        duration = null,
        explicit = "yes",
        image = "http://static.libsyn.com/p/assets/7/1/f/3/71f3014e14ef2722/JREiTunesImage2.jpg",
        keywords = listOf("comedian", "joe", "monkey", "redban", "rogan", "talking", "ufc"),
        newsFeedUrl = "http://joeroganexp.joerogan.libsynpro.com/rss",
        owner = ItunesOwner(
            name = "Joe Rogan",
            email = "joe@joerogan.net",
        ),
        subtitle = "Joe Rogan's Weekly Podcast",
        summary = "The podcast of Comedian Joe Rogan..",
        type = "episodic",
    ),
    articleGuid = "0d7147a3-f1c1-4ae6-bbf8-2e0a493639ca",
    articleTitle = "#1405 - Sober October 3 Recap",
    articleLink = "http://traffic.libsyn.com/joeroganexp/p1405.mp3",
    articlePubDate = "Tue, 24 Dec 2019 20:00:00 +0000",
    articleDescription = "Joe is joined by Ari Shaffir, Bert Kreischer & Tom Segura to recap their 3rd annual Sober October challenge.",
    articleContent = "Joe is joined by Ari Shaffir, Bert Kreischer & Tom Segura to recap their 3rd annual Sober October challenge.",
    articleAudio = "http://traffic.libsyn.com/joeroganexp/p1405.mp3?dest-id=19997",
    articleItunesData = ItunesItemData(
        author = null,
        duration = "03:30:48",
        episode = "1405",
        episodeType = "full",
        explicit = "no",
        image = "http://static.libsyn.com/p/assets/2/8/7/9/28797cc6f284596e/JRE1405.jpg",
        keywords = listOf(
            "podcast",
            "3",
            "joe",
            "party",
            "experience",
            "tom",
            "ari",
            "october",
            "bert",
            "freak",
            "rogan",
            "recap",
            "sober",
            "kreischer",
            "shaffir",
            "segura",
            "deathsquad",
            "jre",
            "1405"
        ),
        subtitle = "Joe is joined by Ari Shaffir, Bert Kreischer & Tom Segura to recap their 3rd annual Sober October challenge.",
        summary = null,
        season = null,
    ),
)
