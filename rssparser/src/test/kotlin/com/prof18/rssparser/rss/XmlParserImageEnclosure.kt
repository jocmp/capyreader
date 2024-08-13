package com.prof18.rssparser.rss

import com.prof18.rssparser.BaseXmlParserTest
import com.prof18.rssparser.model.RssImage

class XmlParserImageEnclosure : BaseXmlParserTest(
    feedPath = "feed-image-enclosure.xml",
    channelTitle = "Centrum dopravního výzkumu, v. v. i. (RSS 2.0)",
    channelLink = "https://www.cdv.cz/",
    channelDescription = "Informace o novinkách na webu Centra dopravního výzkumu, pořádaných akcích v oblasti dopravy a odborných článcích z oblasti dopravy.",
    channelImage = RssImage(
        title = "Logo Centrum dopravního výzkumu, v. v. i.",
        url = "https://www.cdv.cz/",
        link = "https://www.cdv.cz/image/logo-centrum-dopravniho-vyzkumu/",
        description = null,
    ),
    articleTitle = "Podpora udržitelné aktivní mobility a nástroje práce s veřejností",
    articleLink = "https://www.cdv.cz/podpora-udrzitelne-aktivni-mobility-a-nastroje-prace-s-verejnosti/",
    articlePubDate = "22.11.2022 12:16:00",
    articleImage = "https://cdv.cz/image/rss-placeholder-image/",
    articleCategories = listOf("event"),
)
