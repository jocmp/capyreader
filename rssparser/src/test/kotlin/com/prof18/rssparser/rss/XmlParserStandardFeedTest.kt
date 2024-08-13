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

class XmlParserStandardFeedTest : BaseXmlParserTest(
    feedPath = "feed-test.xml",
    channelTitle = "Android Authority",
    channelLink = "https://www.androidauthority.com",
    channelDescription = "Android News, Reviews, How To",
    channelLastBuildDate = "Wed, 15 May 2019 20:48:02 +0000",
    channelUpdatePeriod = "hourly",
    articleGuid = "https://www.androidauthority.com/?p=986823",
    articleTitle = "Japan’s plans for 5G include 10 billion 14-digit phone numbers",
    articleAuthor = "Williams Pelegrin",
    articleLink = "https://www.androidauthority.com/japan-phone-numbers-14-digits-5g-986823/",
    articlePubDate = "Wed, 15 May 2019 20:48:02 +0000",
    articleDescription = "Japan expects a huge surge of 5G-enabled devices to hit the market in the future.",
    articleContent = "<p><a href=\"https://cdn57.androidauthority.net/wp-content/uploads/2019/02/Whats-next-with-5g-.jpg\" rel=\"lightbox[986823]\"><img class=\"aligncenter size-large wp-image-959882\" src=\"https://cdn57.androidauthority.net/wp-content/uploads/2019/02/Whats-next-with-5g--840x473.jpg\" alt=\"\" width=\"840\" height=\"473\" srcset=\"https://cdn57.androidauthority.net/wp-content/uploads/2019/02/Whats-next-with-5g--840x472.jpg 840w, https://cdn57.androidauthority.net/wp-content/uploads/2019/02/Whats-next-with-5g--300x170.jpg 300w, https://cdn57.androidauthority.net/wp-content/uploads/2019/02/Whats-next-with-5g--768x432.jpg 768w, https://cdn57.androidauthority.net/wp-content/uploads/2019/02/Whats-next-with-5g--16x9.jpg 16w, https://cdn57.androidauthority.net/wp-content/uploads/2019/02/Whats-next-with-5g--32x18.jpg 32w, https://cdn57.androidauthority.net/wp-content/uploads/2019/02/Whats-next-with-5g--28x16.jpg 28w, https://cdn57.androidauthority.net/wp-content/uploads/2019/02/Whats-next-with-5g--56x32.jpg 56w, https://cdn57.androidauthority.net/wp-content/uploads/2019/02/Whats-next-with-5g--64x36.jpg 64w, https://cdn57.androidauthority.net/wp-content/uploads/2019/02/Whats-next-with-5g--712x400.jpg 712w, https://cdn57.androidauthority.net/wp-content/uploads/2019/02/Whats-next-with-5g--1000x563.jpg 1000w, https://cdn57.androidauthority.net/wp-content/uploads/2019/02/Whats-next-with-5g--1200x675.jpg 1200w, https://cdn57.androidauthority.net/wp-content/uploads/2019/02/Whats-next-with-5g--792x446.jpg 792w, https://cdn57.androidauthority.net/wp-content/uploads/2019/02/Whats-next-with-5g--1280x720.jpg 1280w, https://cdn57.androidauthority.net/wp-content/uploads/2019/02/Whats-next-with-5g--1340x754.jpg 1340w, https://cdn57.androidauthority.net/wp-content/uploads/2019/02/Whats-next-with-5g--770x433.jpg 770w, https://cdn57.androidauthority.net/wp-content/uploads/2019/02/Whats-next-with-5g--356x200.jpg 356w\" sizes=\"(max-width: 840px) 100vw, 840px\" /></a></p>\n" +
            "<p>With <a href=\"https://www.androidauthority.com/what-is-5g-explained-944868/\">5G</a> promising to bring billions of devices into the fold, Japan hopes to meet that demand by announcing plans to create 10 billion 14-digit phone numbers. As <a href=\"https://www.japantimes.co.jp/news/2019/05/15/national/japan-plans-create-10-billion-14-digit-phone-numbers-5g-era-approaching/#.XNxWwo7Yo3F\"><em>The Japan Times</em></a> reported today, all the new numbers will start with the code &#8220;020.&#8221;</p>\n" +
            "<p>The plan is to introduce the new numbers by the end of 2021 at the latest. Three of Japan&#8217;s largest mobile operators — <a href=\"https://fave.co/2HxymX8\">NTT Docomo</a>, <a href=\"https://fave.co/2HlI5AQ\">KDDI</a>, and <a href=\"https://fave.co/2VCVCwI\">SoftBank</a> — agreed to the idea and will update their systems to accommodate the 10 billion new numbers. Japan&#8217;s communications ministry plans to reward carriers for ahead-of-schedule support.</p>\n" +
            "<p>Japan expects to run out of 11-digit numbers as early as fiscal 2022, which starts April 1 of that year for the Japanese government.</p>\n" +
            "<div class=\"aa_srma_container shortcodes_wrapper right\"><div class=\"shortcodes-header\"><div class=\"shortcodes-title\">Editor's Pick</div></div><div class=\"related_articles_wrapper\"><div class=\"related_article_item\"><a class=\"overlay-link\" href=\"https://www.androidauthority.com/5g-device-price-959959/\" title=\"Here’s what every 5G phone will cost you\"></a><h4 class=\"ra-title\">Here’s what every 5G phone will cost you</h4><div class=\"ra-excerpt\">\n" +
            "\n" +
            "If you want to be one of the first consumers to get your hands on a 5G phone, you’ll have to pay up. A lot of manufacturers including Samsung, LG, and Xiaomi have announced 5G …</div></div></div></div>\n" +
            "<p>The 14 billion new numbers sound excessive, but keep in mind that <a href=\"https://www.androidauthority.com/what-is-the-internet-of-things-592491/\">Internet of Things</a> (IoT) devices have been using 11-digit numbers that start with &#8220;020&#8221; since January 2017. Also, carriers have already taken 32.6 million of the 11-digit 020 numbers that were allocated two years ago.</p>\n" +
            "<p>14-digit numbers could prove troublesome for the masses, since there will be more numbers to remember. That issue could be exacerbated among Japan&#8217;s aging population. It&#8217;s estimated that people older than 60 <a href=\"http://www.stat.go.jp/english/data/nenkan/1431-02.htm\">make up 33 percent</a> of the Japanese population.</p>\n" +
            "<p>Even so, we could see similar announcements in other countries that plan to deploy 5G networks and are running low on allocated phone numbers. It&#8217;s not as much of an issue in the U.S., where a <a href=\"http://www.nationalnanpa.com/pdf/NRUF/April_2014_NANP_Exhaust_Analysis.pdf\">North American Numbering Plan exhaust analysis</a> estimates that the country&#8217;s current numbering system is sufficient until 2044.</p>\n" +
            "<p style=\"text-align: center;\"><strong>NEXT:</strong> <a href=\"https://www.androidauthority.com/vodafone-5g-launch-986496/\">Vodafone issues 5G launch details: Here&#8217;s what you need to know</a></p>\n" +
            "<div class=\"container-fluid builder-html\"><div class=\"container-inner\"><div class=\"affiliate-text\"><i>Affiliate disclosure: We may receive compensation in connection with your purchase of products via links on this page. The compensation received will never influence the content, topics or posts made in this blog. See our <a href=\"https://www.androidauthority.com/external-links/\">disclosure policy</a> for more details.</i></div></div></div>",
    articleImage = "https://cdn57.androidauthority.net/wp-content/uploads/2019/02/Whats-next-with-5g--500x260.jpg",
    articleCategories = listOf("News", "5G", "Japan"),
    articleCommentsUrl = "https://www.androidauthority.com/japan-phone-numbers-14-digits-5g-986823/#respond",
)
