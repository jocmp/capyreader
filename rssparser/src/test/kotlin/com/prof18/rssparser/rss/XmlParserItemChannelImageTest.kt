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

class XmlParserItemChannelImageTest : BaseXmlParserTest(
    feedPath = "feed-item-channel-image.xml",
    channelTitle = "www.espn.com - TOP",
    channelLink = "https://www.espn.com",
    channelDescription = "Latest TOP news from www.espn.com",
    channelImage = RssImage(
        title = "www.espn.com - TOP",
        url = "https://a.espncdn.com/i/espn/teamlogos/lrg/trans/espn_dotcom_black.gif",
        link = "https://www.espn.com",
        description = null,
    ),
    channelLastBuildDate = "Fri, 7 May 2021 18:43:18 GMT",
    articleGuid = "31393791",
    articleTitle = "Inside the mysterious world of missing sports memorabilia",
    articleLink = "https://www.espn.com/mlb/story/_/id/31393791/inside-mysterious-world-missing-sports-memorabilia",
    articlePubDate = "Fri, 7 May 2021 10:44:02 EST",
    articleDescription = "Some of the most treasured pieces of sports memorabilia are missing, can't be authenticated or... currently reside on the moon. A look at those mysterious historic items -- and what they'd be worth in a red-hot sports memorabilia market.",
    articleImage = "https://a.espncdn.com/photo/2021/0506/r850492_1296x1296_1-1.jpg",
)
