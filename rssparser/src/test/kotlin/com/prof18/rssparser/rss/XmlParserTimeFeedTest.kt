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

class XmlParserTimeFeedTest : BaseXmlParserTest(
    feedPath = "feed-test-time.xml",
    channelTitle = "Drug Recalls",
    channelLink = "http://www.fda.gov/about-fda/contact-fda/stay-informed/rss-feeds/drug-recalls/rss.xml",
    channelDescription = "",
    articleGuid = "http://www.fda.gov/safety/recalls-market-withdrawals-safety-alerts/vivimed-life-sciences-pvt-ltd-issues-voluntary-nationwide-recall-losartan-potassium-25-mg-50-mg-and",
    articleTitle = "Vivimed Life Sciences Pvt Ltd Issues Voluntary Nationwide Recall of Losartan\n" +
            "                Potassium 25 mg, 50 mg and 100 mg Tablets, USP Due to the Detection of Trace Amounts\n" +
            "                of N-Nitroso-N-methyl-4-aminobutyric acid (NMBA) Impurity",
    articleAuthor = "FDA",
    articleLink = "http://www.fda.gov/safety/recalls-market-withdrawals-safety-alerts/vivimed-life-sciences-pvt-ltd-issues-voluntary-nationwide-recall-losartan-potassium-25-mg-50-mg-and",
    articlePubDate = "Fri, 05/03/2019 - 15:21",
    articleDescription = "Vivimed Life Sciences Pvt Ltd (Vivimed) is recalling 19 lots of Losartan\n" +
            "                Potassium Tablets USP 25 mg, 50 mg, and 100 mg to consumer level. Due to the\n" +
            "                detection of an impurity – N-Nitroso-N-methyl-4-aminobutyric acid (NMBA) – that is\n" +
            "                above the US Food & Drug Administration’s interim acceptable exposu",
)
