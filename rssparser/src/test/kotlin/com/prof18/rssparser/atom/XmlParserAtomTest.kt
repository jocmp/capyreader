package com.prof18.rssparser.atom

import com.prof18.rssparser.BaseXmlParserTest
import com.prof18.rssparser.model.RssImage

class XmlParserAtomTest: BaseXmlParserTest(
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
<figure>
            <img alt="A photo of the Sonos Era 300 on a kitchen dining table." src="https://cdn.vox-cdn.com/thumbor/oCea2Vc5FYLWqQXGUmA4O-rRrM0=/0x0:2040x1360/1310x873/cdn.vox-cdn.com/uploads/chorus_image/image/72316887/DSCF0491.0.jpg" />
            <figcaption>Photo by Chris Welch / The Verge</figcaption>
            </figure>

            <p id="b46vcm">Google has been ordered to pay Sonos ${'$'}32.5 million for infringing on the company’s smart speaker patent. A <a href="https://www.documentcloud.org/documents/23826599-google-sonos-trial-verdict?responsive=1&amp;title=1">jury verdict</a> issued in a San Francisco courtroom on Friday found that Google’s smart speakers and media players infringed on one of two Sonos patents at issue.</p>
            <p id="keHPBL"><a href="https://www.theverge.com/2020/1/7/21055048/sonos-google-lawsuit-sues-speakers-assistant-amazon">The legal battle started in 2020</a> when Sonos accused Google of copying its patented multiroom audio technology after the companies partnered in 2013. <a href="https://www.theverge.com/2022/1/6/22871121/sonos-google-patent-itc-ruling-decision-import-ban">Sonos went on to win its case at the US International Trade Commission</a>, resulting in a limited import ban on some of the Google devices in question. Google has also <a href="https://www.theverge.com/2022/1/6/22871304/google-home-speaker-group-volume-control-changes-sonos-patent-decision">had to pull some features</a> from its lineup of smart speakers and smart displays.</p>
            <figure class="e-image">

            <cite>Image: United States District Court for the Northern District of...</cite></figure>
            <p>
            <a href="https://www.theverge.com/2023/5/26/23739273/google-sonos-smart-speaker-patent-lawsuit-ruling">Continue reading&hellip;</a>
            </p>
    """.trimIndent(),
    articleImage = "https://cdn.vox-cdn.com/thumbor/oCea2Vc5FYLWqQXGUmA4O-rRrM0=/0x0:2040x1360/1310x873/cdn.vox-cdn.com/uploads/chorus_image/image/72316887/DSCF0491.0.jpg"
)
