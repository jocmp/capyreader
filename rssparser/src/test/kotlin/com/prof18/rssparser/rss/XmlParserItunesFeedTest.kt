package com.prof18.rssparser.rss

import com.prof18.rssparser.BaseXmlParserTest
import com.prof18.rssparser.model.RssImage
import com.prof18.rssparser.model.ItunesItemData
import com.prof18.rssparser.model.ItunesChannelData
import com.prof18.rssparser.model.ItunesOwner

class XmlParserItunesFeedTest : BaseXmlParserTest(
    feedPath = "feed-itunes.xml",
    channelTitle = "The Joe Rogan Experience",
    channelLink = "https://www.joerogan.com",
    channelDescription = "Conduit to the Gaian Mind",
    channelImage = RssImage(
        title = "The Joe Rogan Experience",
        url = "http://static.libsyn.com/p/assets/7/1/f/3/71f3014e14ef2722/JREiTunesImage2.jpg",
        link = "https://www.joerogan.com",
        description = null
    ),
    channelLastBuildDate = "Thu, 29 Jul 2021 05:45:54 +0000",
    channelUpdatePeriod = null,
    channelItunesData = ItunesChannelData(
        author = "Joe Rogan",
        categories = listOf("Comedy", "Society & Culture", "Technology"),
        duration = "02:02:35",
        explicit = "yes",
        image = "http://static.libsyn.com/p/assets/7/1/f/3/71f3014e14ef2722/JREiTunesImage2.jpg",
        keywords = listOf("Talking", "comedian", "joe", "monkey", "redban", "rogan", "ufc"),
        newsFeedUrl = "https://joeroganexp.libsyn.com/rss",
        owner = ItunesOwner(
            name = "Joe Rogan",
            email = "joe@joerogan.net",
        ),
        subtitle = "Joe Rogan's Weekly Podcast",
        summary = "Conduit to the Gaian Mind",
        type = "episodic",
    ),
    articleGuid = "00a5d989b6b2cd8267cf8239f3b5585c",
    articleTitle = "#1109 - Matthew Walker",
    articleAuthor = null,
    articleLink = "http://traffic.libsyn.com/joeroganexp/p1109.mp3",
    articlePubDate = "Wed, 25 Apr 2018 22:37:10 +0000",
    articleDescription = "Matthew Walker is Professor of Neuroscience and Psychology at the University of California, Berkeley, and Founder and Director of the Center for Human Sleep Science. Check out his book \"\" on Amazon.",
    articleContent = "Matthew Walker is Professor of Neuroscience and Psychology at the University of California, Berkeley, and Founder and Director of the Center for Human Sleep Science. Check out his book \"\" on Amazon.",
    articleImage = null,
    articleAudio = "http://traffic.libsyn.com/joeroganexp/p1109.mp3?dest-id=19997",
    articleVideo = null,
    articleSourceName = null,
    articleSourceUrl = null,
    articleCategories = listOf(),
    articleCommentsUrl = null,
    articleItunesData = ItunesItemData(
        author = "Joe Rogan",
        duration = "02:02:35",
        episode = "1109",
        episodeType = "full",
        explicit = "yes",
        image = "http://static.libsyn.com/p/assets/6/f/b/6/6fb68f57fbe00fb1/JRE1109.jpg",
        keywords = listOf(
            "podcast",
            "joe",
            "party",
            "Experience",
            "walker",
            "matthew",
            "freak",
            "rogan",
            "deathsquad",
            "jre",
            "1109"
        ),
        subtitle = "#1109 - Matthew Walker",
        summary = null,
        season = null,
    )
)
