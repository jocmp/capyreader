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

class XmlParserImageFeedTest : BaseXmlParserTest(
    feedPath = "feed-test-image.xml",
    channelTitle = "Movie Reviews",
    channelLink = "https://movieweb.com/movie-reviews/",
    channelDescription = "Movie Reviews at MovieWeb",
    channelImage = RssImage(
        title = "Movie Reviews",
        url = "https://cdn.movieweb.com/assets/1/sites/movieweb.com/chrome-touch-icon-192x192.png",
        link = "https://movieweb.com/movie-reviews/",
        description = null,
    ),
    channelLastBuildDate = "Fri, 17 May 2019 00:24:34 PDT",
    articleGuid = "https://movieweb.com/the-sun-is-also-a-star-review/",
    articleTitle = "The Sun Is Also a Star Review: Yara Shahidi & Charles Melton Elevate Teen Romance",
    articleLink = "https://movieweb.com/the-sun-is-also-a-star-review/",
    articlePubDate = "Wed, 15 May 2019 16:52:24 PDT",
    articleDescription = "The Sun Is Also a Star is a diverse romance that bucks Hollywood's YA genre.",
    articleImage = "https://cdn3.movieweb.com/i/article/ABvTB3C2AERsBFALiokUbPAwoYXIC4/1200:100/The-Sun-Is-Also-A-Star-Review.jpg",
)
