package com.jocmp.rssparser.json

import com.jocmp.rssparser.BaseParserTest

class JsonKibtyTownTest : BaseParserTest(
    feedPath = "feed-kibty-town.json",
    channelTitle = "xyzeva's blog",
    channelLink = "https://kibty.town/",
    channelDescription = "random thoughts and other stuff",
    articleTitle = "gaining access to anyones browser without them even visiting a website",
    articleLink = "https://kibty.town/blog/arc/",
    articleContent = """
        <p>we start at the homepage of arc. where i first landed when i first heard of it. i snatched a download and started analysing, the first thing i realised was that arc requires an account to use, why do they require an account?
    """.trimIndent(),
    articlePubDate = "Sat, 07 Sep 2024 00:00:00 GMT",
)
