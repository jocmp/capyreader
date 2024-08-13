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

package com.prof18.rssparser.atom

import com.prof18.rssparser.BaseXmlParserTest

class XmlParserAtomFeedCreatedUpdated : BaseXmlParserTest(
    feedPath = "atom-feed-created-updated.xml",
    channelTitle = "tonsky.me",
    channelLink = "https://tonsky.me/",
    channelLastBuildDate = "2023-12-22T18:46:17Z",
    channelDescription = "Nikita Prokopov’s blog",
    articleTitle = "The Absolute Minimum Every Software Developer Must Know About Unicode in 2023 (Still No Excuses!)",
    articlePubDate = "2023-10-02T00:00:00Z",
    articleLink = "https://tonsky.me/blog/unicode/",
    articleAuthor = "Nikita Prokopov",
    articleGuid = "https://tonsky.me/blog/unicode/",
    articleDescription = "Modern extension to classic 2003 article by Joel Spolsky",
)
