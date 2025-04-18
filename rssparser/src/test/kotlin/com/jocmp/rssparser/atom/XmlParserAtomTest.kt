package com.jocmp.rssparser.atom

import com.jocmp.rssparser.BaseParserTest
import com.jocmp.rssparser.model.RssImage

class XmlParserAtomTest: BaseParserTest(
    feedPath = "feed-atom-test.xml",
    channelTitle = "The Verge -  All Posts",
    channelLink = "https://www.theverge.com/",
    channelImage = RssImage(
        title = null,
        link = null,
        description = null,
        url = "https://cdn.vox-cdn.com/community_logos/52801/VER_Logomark_32x32..png"
    ),
    channelLastBuildDate = "2023-05-26T17:30:31-04:00",
    channelUpdatePeriod = null,
    articleGuid = "https://www.theverge.com/2023/5/26/23739273/google-sonos-smart-speaker-patent-lawsuit-ruling",
    articleTitle = "Sonos wins \$32.5 million patent infringement victory over Google",
    articleAuthor = "Chris Welch",
    articleLink = "https://www.theverge.com/2023/5/26/23739273/google-sonos-smart-speaker-patent-lawsuit-ruling",
    articlePubDate = "2023-05-26T17:30:31-04:00",
    articleContent = """
        &lt;figure&gt;
                    &lt;img alt="A photo of the Sonos Era 300 on a kitchen dining table." src="https://cdn.vox-cdn.com/thumbor/oCea2Vc5FYLWqQXGUmA4O-rRrM0=/0x0:2040x1360/1310x873/cdn.vox-cdn.com/uploads/chorus_image/image/72316887/DSCF0491.0.jpg" /&gt;
                    &lt;figcaption&gt;Photo by Chris Welch / The Verge&lt;/figcaption&gt;
                    &lt;/figure&gt;

                    &lt;p id="b46vcm"&gt;Google has been ordered to pay Sonos ${'$'}32.5 million for infringing on the company’s smart speaker patent. A &lt;a href="https://www.documentcloud.org/documents/23826599-google-sonos-trial-verdict?responsive=1&amp;amp;title=1"&gt;jury verdict&lt;/a&gt; issued in a San Francisco courtroom on Friday found that Google’s smart speakers and media players infringed on one of two Sonos patents at issue.&lt;/p&gt;
                    &lt;p id="keHPBL"&gt;&lt;a href="https://www.theverge.com/2020/1/7/21055048/sonos-google-lawsuit-sues-speakers-assistant-amazon"&gt;The legal battle started in 2020&lt;/a&gt; when Sonos accused Google of copying its patented multiroom audio technology after the companies partnered in 2013. &lt;a href="https://www.theverge.com/2022/1/6/22871121/sonos-google-patent-itc-ruling-decision-import-ban"&gt;Sonos went on to win its case at the US International Trade Commission&lt;/a&gt;, resulting in a limited import ban on some of the Google devices in question. Google has also &lt;a href="https://www.theverge.com/2022/1/6/22871304/google-home-speaker-group-volume-control-changes-sonos-patent-decision"&gt;had to pull some features&lt;/a&gt; from its lineup of smart speakers and smart displays.&lt;/p&gt;
                    &lt;figure class="e-image"&gt;

                    &lt;cite&gt;Image: United States District Court for the Northern District of...&lt;/cite&gt;&lt;/figure&gt;
                    &lt;p&gt;
                    &lt;a href="https://www.theverge.com/2023/5/26/23739273/google-sonos-smart-speaker-patent-lawsuit-ruling"&gt;Continue reading&amp;hellip;&lt;/a&gt;
                    &lt;/p&gt;
    """.trimIndent(),
    articleImage = "https://cdn.vox-cdn.com/thumbor/oCea2Vc5FYLWqQXGUmA4O-rRrM0=/0x0:2040x1360/1310x873/cdn.vox-cdn.com/uploads/chorus_image/image/72316887/DSCF0491.0.jpg"
)
