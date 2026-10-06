package com.jocmp.rssparser.rss

import com.jocmp.rssparser.BaseParserTest

class XmlParserSpacedLinkTest : BaseParserTest(
    feedPath = "feed-spaced-link.xml",
    channelTitle = "Sixth Tone RSS",
    channelLink = "https://www.sixthtone.com",
    channelDescription = "Sixth Tone RSS",
    articleGuid = null,
    articleTitle = "Feathered Dinosaur From China Points to Another Path to Flight",
    articleAuthor = "Fan Yiying",
    articleLink = "https://www.sixthtone.com/news/1019094/Feathered Dinosaur From China Points to Another Path to Flight",
    articlePubDate = "Oct 05, 2026",
    articleDescription = "<a href=\"https://www.sixthtone.com/news/1019094/Feathered Dinosaur From China Points to Another Path to Flight\"><img src=\"https://image5.sixthtone.com/image/5/100/695.jpg\" width=\"204\" height=\"109\" alt=\"\"></a></br>Its nearly complete skeleton suggests birds and microraptorines developed similar flight-related features in a different order.",
    articleImage = "https://image5.sixthtone.com/image/5/100/695.jpg",
    articleCategories = listOf("NEWS"),
)
