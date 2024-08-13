package com.prof18.rssparser.rss

import com.prof18.rssparser.BaseXmlParserTest
import com.prof18.rssparser.model.RssImage

class XmlParserCharEscape : BaseXmlParserTest(
    feedPath = "feed-char-escape.xml",
    channelTitle = "NYT > Health",
    channelLink = "https://www.nytimes.com/section/health",
    channelDescription = "",
    channelImage = RssImage(
        title = "NYT > Health",
        url = "https://static01.nyt.com/images/misc/NYT_logo_rss_250x40.png",
        link = "https://www.nytimes.com/section/health",
        description = null
    ),
    channelLastBuildDate = "Mon, 14 Jun 2021 15:26:42 +0000",
    articleGuid = "https://www.nytimes.com/2021/06/11/science/drought-las-vegas-grass-mars.html",
    articleTitle = "Where the Grass is Greener, Except When It’s ‘Nonfunctional Turf’",
    articleAuthor = "Alan Burdick",
    articleLink = "https://www.nytimes.com/2021/06/11/science/drought-las-vegas-grass-mars.html",
    articlePubDate = "Sun, 13 Jun 2021 16:21:42 +0000",
    articleDescription = "Plus, mammoths in Vegas, watermelon snow, Miami’s looming sea wall and more in the Friday edition of the Science Times newsletter.",
    articleCategories = listOf(
        "Conservation of Resources",
        "Grass",
        "Water",
        "Deserts",
        "Shortages",
        "Mars (Planet)",
        "Colorado River",
        "Hoover Dam",
        "Lake Mead",
        "Las Vegas (Nev)",
        "Mojave Desert (Calif)",
        "Western States (US)",
        "your-feed-science"
    ),
)
