package com.jocmp.basil

import com.jocmp.feedfinder.parser.Feed
import java.net.URL

class FakeParserFeed(
    override val name: String = "The Verge - All Posts",
    override val feedURL: URL = URL("https://theverge.com/rss/index.xml"),
    override val siteURL: URL? = null,
    private val valid: Boolean = true
) : Feed {
    override fun isValid() = valid
}
