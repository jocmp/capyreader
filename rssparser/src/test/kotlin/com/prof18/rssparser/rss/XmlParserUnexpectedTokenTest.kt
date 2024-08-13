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

package com.prof18.rssparser.rss

import com.prof18.rssparser.BaseXmlParserTest

class XmlParserUnexpectedTokenTest : BaseXmlParserTest(
    feedPath = "feed-test-unexpected-token.xml",
    channelTitle = "Wheels Off-Road & 4x4",
    channelLink = "https://www.wheels24.co.za/",
    channelLastBuildDate = "Wed, 23 Sep 2020 20:51:07 +0200",
    articleTitle = "Wheels24.co.za | WATCH | Range Rover spices up its Velar line-up, adds new hybrid model to the range",
    articleLink = "https://www.wheels24.co.za/OffRoad_and_4x4/Bakkie_and_SUV/watch-range-rover-spices-up-its-velar-line-up-adds-new-hybrid-model-to-the-range-20200923-3",
    articlePubDate = "Wed, 23 Sep 2020 14:45:10 +0200",
    articleDescription = "Range Rover updated its desirable Velar range by adding new engine choices and a 640Nm hybrid model.",
    articleImage = "https://scripts.24.co.za/img/sites/wheels24.png",
)
