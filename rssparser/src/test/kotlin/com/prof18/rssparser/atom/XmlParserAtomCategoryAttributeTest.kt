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

class XmlParserAtomCategoryAttributeTest : BaseXmlParserTest(
    feedPath = "feed-test-atom-category-attribute.xml",
    articleImage = "https://a.fsdn.com/sd/twitter_icon_large.png",
    channelTitle = "Slashdot",
    channelLink = "http://pubsubhubbub.appspot.com/",
    channelDescription = "News for nerds, stuff that matters",
    channelLastBuildDate = "2023-05-29T00:14:18+00:00",
    articleTitle = "Automakers Ask Judge to Block Pending Enforcement of Massachusetts' Right-to-Repair Law",
    articlePubDate = "2023-05-28T22:44:00+00:00",
    articleLink = "https://yro.slashdot.org/story/23/05/28/2213240/automakers-ask-judge-to-block-pending-enforcement-of-massachusetts-right-to-repair-law?utm_source=atom1.0mainlinkanon&utm_medium=feed",
    articleCategories = listOf("government"),
    articleAuthor = "EditorDavid",
    articleGuid = "https://yro.slashdot.org/story/23/05/28/2213240/automakers-ask-judge-to-block-pending-enforcement-of-massachusetts-right-to-repair-law?utm_source=atom1.0mainlinkanon&utm_medium=feed",
    articleDescription = "\"Beginning next Thursday, Massachusetts Attorney General Andrea Joy Campbell plans to start enforcing the state's automotive right-to-repair law,\" reports the Boston Globe.\n" +
            "\n" +
            "            \"But this week, the world's top automakers asked a federal judge to stop her.\"\n" +
            "\n" +
            "            The Alliance for Automotive Innovation, a car industry trade group, sued to block enforcement of the law almost from the moment it was passed by voter referendum in 2020. Ever since, the law has been tied up in the courtroom of US District Judge Douglas Woodlock. Now the alliance has asked Woodlock to grant a temporary injunction that would stop Campbell from enforcing the law until he issues a final ruling in the case.\n" +
            "            Campbell's predecessor, now-Governor Maura Healey, repeatedly refrained from enforcing the law, pending Woodlock's decision. But Healey always reserved the right to reverse this policy if a ruling took too long. In March, Campbell said she would start enforcing the law effective June 1. \"The people of Massachusetts deserve the benefit of the law they approved more than two years ago,\" she said in a document filed with the court.\n" +
            "            But the carmakers say that only the federal government has the authority to enact such a law. They claim the law is so poorly drafted that they can't comply with it, and even if they could, compliance would weaken vehicle security, making it easier for cyber criminals to steal digital data about vehicles and their owners. Two carmakers, Kia and Subaru, have tried to comply with the law by switching off the telematic services in their cars. But the carmakers argue that this deprives consumers of the right to use these features, which include emergency roadside assistance that could potentially save lives.<p><div class=\"share_submission\" style=\"position:relative;\">\n" +
            "            <a class=\"slashpop\" href=\"http://twitter.com/home?status=Automakers+Ask+Judge+to+Block+Pending+Enforcement+of+Massachusetts'+Right-to-Repair+Law%3A+https%3A%2F%2Fyro.slashdot.org%2Fstory%2F23%2F05%2F28%2F2213240%2F%3Futm_source%3Dtwitter%26utm_medium%3Dtwitter\"><img src=\"https://a.fsdn.com/sd/twitter_icon_large.png\"></a>\n" +
            "            <a class=\"slashpop\" href=\"http://www.facebook.com/sharer.php?u=https%3A%2F%2Fyro.slashdot.org%2Fstory%2F23%2F05%2F28%2F2213240%2Fautomakers-ask-judge-to-block-pending-enforcement-of-massachusetts-right-to-repair-law%3Futm_source%3Dslashdot%26utm_medium%3Dfacebook\"><img src=\"https://a.fsdn.com/sd/facebook_icon_large.png\"></a>\n" +
            "\n" +
            "\n" +
            "\n" +
            "            </div></p><p><a href=\"https://yro.slashdot.org/story/23/05/28/2213240/automakers-ask-judge-to-block-pending-enforcement-of-massachusetts-right-to-repair-law?utm_source=atom1.0moreanon&amp;utm_medium=feed\">Read more of this story</a> at Slashdot.</p>",
)
