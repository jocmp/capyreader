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

class XmlParserImageEmptyTag : BaseXmlParserTest(
    feedPath = "feed-test-image-empty-tag.xml",
    channelTitle = "Hacker Noon",
    channelLink = "https://hackernoon.com",
    channelDescription = "How hackers start their afternoons.",
    channelImage = null,
    channelLastBuildDate = "Sun, 29 Oct 2023 10:00:20 GMT",
    channelUpdatePeriod = null,
    channelItunesData = null,
    articleGuid = "https://hackernoon.com/miscellaneous-directions?source=rss",
    articleTitle = "MISCELLANEOUS DIRECTIONS.",
    articleAuthor = "Catharine Esther Beecher",
    articleLink = "https://hackernoon.com/miscellaneous-directions?source=rss",
    articlePubDate = "Sun, 29 Oct 2023 09:00:01 GMT",
    articleDescription = "Every woman should know how to direct in regard to the proper care of domestic animals.",
    articleContent = null,
    articleImage = "https://hackernoon.com/https://cdn.hackernoon.com/images/3cu1ROR1ocaekNNTdramI0w9qNj2-xl93swo.jpeg",
    articleAudio = null,
    articleVideo = null,
    articleSourceName = null,
    articleSourceUrl = null,
    articleCategories = listOf("domestic-manuals"),
    articleCommentsUrl = null,
    articleItunesData = null,
)
