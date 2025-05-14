package com.jocmp.rssparser.rss

import com.jocmp.rssparser.BaseParserTest
import com.jocmp.rssparser.model.RssImage
import com.jocmp.rssparser.model.RssItemEnclosure

class XmlParserMultipleMediaContentFeedTest : BaseParserTest(
    feedPath = "feed-multiple-media-content.xml",
    channelTitle = "Nuclearpasta",
    channelLink = "https://mastodon.art/@nuclearpasta",
    channelDescription = "Public posts from @nuclearpasta@mastodon.art",
    channelLastBuildDate = "Mon, 12 May 2025 16:17:47 +0000",
    channelImage = RssImage(
        url = "https://cdn.masto.host/mastodonart/accounts/avatars/107/660/901/580/045/662/original/c5b2806580a600d0.jpg",
        link = "https://mastodon.art/@nuclearpasta",
        description = null,
    ),
    articleGuid = "https://mastodon.art/@nuclearpasta/114495761108890085",
    articlePubDate = "Mon, 12 May 2025 16:17:47 +0000",
    articleLink = "https://mastodon.art/@nuclearpasta/114495761108890085",
    articleDescription = "<p>Oh it&#39;s <a href=\"https://mastodon.art/tags/WebcomicDay\" class=\"mention hashtag\" rel=\"tag\">#<span>WebcomicDay</span></a> again! <br />If a fantasy Madmax with giant lizards and a muscle mommy protagonist doesn&#39;t interest you, you should NOT read my comic!</p><p><a href=\"https://tenearthshatteringblows.com\" target=\"_blank\" rel=\"nofollow noopener noreferrer\" translate=\"no\"><span class=\"invisible\">https://</span><span class=\"\">tenearthshatteringblows.com</span><span class=\"invisible\"></span></a></p><p><a href=\"https://mastodon.art/tags/webcomic\" class=\"mention hashtag\" rel=\"tag\">#<span>webcomic</span></a> <a href=\"https://mastodon.art/tags/fantasy\" class=\"mention hashtag\" rel=\"tag\">#<span>fantasy</span></a> <a href=\"https://mastodon.art/tags/comics\" class=\"mention hashtag\" rel=\"tag\">#<span>comics</span></a></p>",
    articleCategories = listOf("webcomicday", "webcomic", "fantasy", "comics"),
    articleImage = "https://cdn.masto.host/mastodonart/media_attachments/files/114/495/754/804/385/848/original/dee105af83a73921.jpg",
    articleEnclosures = listOf(
        RssItemEnclosure(
            url = "https://cdn.masto.host/mastodonart/media_attachments/files/114/495/754/804/385/848/original/dee105af83a73921.jpg",
            type = "image/jpeg",
        ),
        RssItemEnclosure(
            url = "https://cdn.masto.host/mastodonart/media_attachments/files/114/495/755/611/292/322/original/c332f57157abc203.jpg",
            type = "image/jpeg"
        ),
        RssItemEnclosure(
            url = "https://cdn.masto.host/mastodonart/media_attachments/files/114/495/756/453/333/085/original/f0baf8d5c4be0461.jpg",
            type = "image/jpeg",
        ),
        RssItemEnclosure(
            url =
                "https://cdn.masto.host/mastodonart/media_attachments/files/114/495/757/644/563/056/original/884111c48e491831.jpg",
            type = "image/jpeg",
        ),
    )
)
