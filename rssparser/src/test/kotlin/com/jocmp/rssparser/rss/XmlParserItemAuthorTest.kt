package com.jocmp.rssparser.rss

import com.jocmp.rssparser.BaseParserTest
import com.jocmp.rssparser.model.RssImage

class XmlParserItemAuthorTest : BaseParserTest(
    feedPath = "feed-item-author.xml",
    channelTitle = "Planet KDE | English",
    channelLink = "https://planet.kde.org/",
    channelDescription = "Planet KDE | English",
    channelLastBuildDate = "2025-03-24T09:52:35+00:00",
    channelImage = RssImage(url = "https://planet.kde.org/img/planet.png"),
    articleTitle = "This Week in KDE Apps",
    articleGuid = "https://blogs.kde.org/2025/03/24/this-week-in-kde-apps/",
    articleLink = "https://blogs.kde.org/2025/03/24/this-week-in-kde-apps/",
    articlePubDate = "Mon, 24 Mar 2025 09:52:35 +0000",
    articleDescription = "<h4>Stability improvements in KDE PIM-land, and polls in NeoChat</h4>",
    articleAuthor = "This Week in KDE Apps",
)
