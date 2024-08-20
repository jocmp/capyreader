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

class XmlParserFeedRuTest : BaseXmlParserTest(
    feedPath = "feed-test-ru.xml",
    channelTitle = "Аргументы и Факты",
    channelLink = "http://www.aif.ru/",
    channelDescription = "Аргументы и Факты: объясняем, что происходит",
    channelLastBuildDate = "2019-05-18 10:58:46 +0300",
    articleTitle = "СМИ: собака в Таиланде спасла заживо похороненного младенца",
    articleLink = "http://www.aif.ru/incidents/smi_sobaka_v_tailande_spasla_zazhivo_pohoronennogo_mladenca",
    articlePubDate = "Sat, 18 May 2019 10:52:50 +0300",
    articleDescription = "15-летняя мать ребенка решила избавиться от него, побоявшись гнева родителей",
    articleImage = "https://images.aif.ru/017/020/025bc5cb4cc5d0f8347bbb27f5e4d13b.jpg",
    articleCategories = listOf("Происшествия"),
)
