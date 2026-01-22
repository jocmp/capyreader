/*
*   Copyright 2020 Marco Gomiero
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

package com.jocmp.rssparser.rss

import com.jocmp.rssparser.BaseParserTest

class XmlParserCommentsTest : BaseParserTest(
    feedPath = "feed-comment.xml",
    channelTitle = "Hacker News",
    channelLink = "https://news.ycombinator.com/",
    channelDescription = "Links for the intellectually curious, ranked by readers.",
    articleTitle = "GPTZero case study discovers it's only accurate on less than 50% of text",
    articleLink = "https://gonzoknows.com/posts/GPTZero-Case-Study/",
    articlePubDate = "Sun, 19 Feb 2023 15:22:39 +0000",
    articleDescription = "<a href=\"https://news.ycombinator.com/item?id=34858307\">Comments</a>",
    articleCommentsUrl = "https://news.ycombinator.com/item?id=34858307",
)
