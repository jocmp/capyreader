package com.prof18.rssparser.rdf

import com.prof18.rssparser.BaseXmlParserTest
import com.prof18.rssparser.model.RssImage

class XmlParserRdfDistroWatchTest : BaseXmlParserTest(
    feedPath = "feed-rdf-distrowatch.xml",
    channelTitle = "DistroWatch.com: DistroWatch Weekly",
    channelLink = "https://distrowatch.com/",
    channelDescription = "Latest news on Linux distributions and BSD projects",
    channelLastBuildDate = "2024-08-19T00:03:40-00:00",
    channelImage = RssImage(
        title = null,
        link = null,
        description = null,
        url = "https://distrowatch.com/images/other/dw.png"
    ),
    articleTitle = "DistroWatch Weekly, Issue 1084",
    articleLink = "https://distrowatch.com/weekly.php?issue=20240819",
    articleDescription = """
        The DistroWatch Weekly news feed is brought to you by <a href="https://www.tuxedocomputers.com/">TUXEDO COMPUTERS</a>.  This week in DistroWatch Weekly: <br>
        Review: Liya 2.0<br>
        News: Haiku introduces performance improvements, Redcore merges major upgrade, Gentoo dropping IA-64 support<br>
        Questions and answers: Dual boot with encryption<br>
        Released last week: Tails 6.6, RebeccaBlackOS 2024-08-12, deepin 23<br>
        Torrent corner: deepin, Endless OS, SparkyLinux, Tails<br>
        Upcoming releases: FreeBSD 13.4-RC1<br>
        Opinion poll: Do you encrypt your root filesystem or home directories?<br>
        New distributions: BredOS, Red OS<br>
        Reader comments Read more in this week's issue of DistroWatch Weekly....
    """.trimIndent(),
    articlePubDate = "2024-08-19T00:03:40+00:00",
    articleCategories = listOf("weekly")
)
