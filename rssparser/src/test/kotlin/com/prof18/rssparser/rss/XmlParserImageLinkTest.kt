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

class XmlParserImageLinkTest : BaseXmlParserTest(
    feedPath = "feed-test-image-link.xml",
    channelTitle = "Bleacher Report - Front Page",
    channelLink = "https://bleacherreport.com",
    channelDescription = "Bleacher Report - The latest articles about sports news, rumors, teams, events, and more.",
    articleTitle = "Orange Cassidy Retains AEW International Title at Double or Nothing 2023",
    articleImage = "https://media.bleacherreport.com/image/upload/x_0,y_43,w_1800,h_1200,c_crop/v1685197298/ieh5bpyilj43f4dzpgua.jpg",
    articlePubDate = "2023-05-29T00:34:27Z",
    articleLink = "https://bleacherreport.com/articles/10077424-orange-cassidy-retains-aew-international-title-at-double-or-nothing-2023",
    articleGuid = "https://bleacherreport.com/articles/10077424-orange-cassidy-retains-aew-international-title-at-double-or-nothing-2023",
    articleCommentsUrl = "https://bleacherreport.com/articles/10077424-orange-cassidy-retains-aew-international-title-at-double-or-nothing-2023",
    articleDescription = "Orange Cassidy won a 21-man Blackjack Battle Royal at Double or Nothing on Sunday to retain the AEW International Championship. The action-packed opener ended…",
    articleCategories = listOf("Breaking News", "All Elite Wrestling", "BNT MISC", "Orange Cassidy"),
)
