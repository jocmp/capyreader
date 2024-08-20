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

class XmlParserXSLFeedTest : BaseXmlParserTest(
    feedPath = "feed-test-xsl.xml",
    channelTitle = "SkySports | Liverpool",
    channelLink = "http://www.skysports.com",
    channelDescription = "Liverpool News",
    channelImage = RssImage(
        title = "Sky Sports",
        url = "https://www.skysports.com/images/site/ss-logo-07.gif",
        link = "https://www.skysports.com",
        description = null,
    ),
    channelLastBuildDate = "Fri, 17 May 2019 23:21:44 BST",
    articleTitle = "Insight: Who should Liverpool sign?",
    articleLink = "https://www.skysports.com/football/news/11669/11719097/premier-league-transfer-window-who-should-liverpool-sign",
    articlePubDate = "Fri, 17 May 2019 06:00:00 BST",
    articleDescription = "Liverpool just missed out on clinching the Premier League title and have a Champions League final to look forward to - so where could they improve?",
    articleImage = "https://e2.365dm.com/19/04/128x67/skysports-jurgen-klopp-liverpool_4654732.jpg?20190430113948",
    articleCategories = listOf("News Story"),
)
