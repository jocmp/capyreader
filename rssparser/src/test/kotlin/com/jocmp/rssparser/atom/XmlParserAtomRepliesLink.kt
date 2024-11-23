package com.jocmp.rssparser.atom

import com.jocmp.rssparser.BaseParserTest
import com.jocmp.rssparser.model.RssImage

class XmlParserAtomRepliesLink : BaseParserTest(
    feedPath = "atom-replies-link-example.xml",
    channelTitle = "9to5Linux",
    channelLink = "https://9to5linux.com/",
    channelLastBuildDate = "2024-08-21T18:33:48Z",
    channelDescription = "Linux news, reviews, tutorials, and more",
    articleTitle = "Fwupd 1.9.24 Firmware Updater Adds Support for Capsule-on-Disk for Dell Systems",
    articlePubDate = "2024-08-21T18:13:37Z",
    articleLink = "https://9to5linux.com/fwupd-1-9-24-adds-support-for-capsule-on-disk-for-dell-systems",
    articleGuid = "https://9to5linux.com/?p=23839",
    channelImage = RssImage(title = null, link = null, description = null, url = "https://i0.wp.com/9to5linux.com/wp-content/uploads/2021/04/cropped-9to5linux-logo-mini-copy.png?fit=32%2C32&ssl=1"),
    articleAuthor = "Marius Nestor",
    articleCategories = listOf("App", "News", "firmware update", "firmware upgrade", "fwupd", "Fwupd 1.9.24", "Linux firmware"),
    articleDescription = """
        <p>fwupd 1.9.24 Linux firmware updater is now available for download with support for capsule-on-disk for Dell systems, support for more MediaTek scaler devices, support for Parade USB hubs and other changes.</p>
        <p>The post <a href="https://9to5linux.com/fwupd-1-9-24-adds-support-for-capsule-on-disk-for-dell-systems">Fwupd 1.9.24 Firmware Updater Adds Support for Capsule-on-Disk for Dell Systems</a> appeared first on <a href="https://9to5linux.com">9to5Linux</a> - do not reproduce this article without permission. This RSS feed is intended for readers, not scrapers.</p>
    """.trimIndent()
)
