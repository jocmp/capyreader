package com.jocmp.basil.fixtures

import com.jocmp.feedfinder.parser.Feed
import java.net.URL

const val THE_VERGE_URL = "https://theverge.com/rss/index.xml"

class TheVergeFeed(
    override val name: String = "The Verge - All Posts",
    override val feedURL: URL = URL(THE_VERGE_URL),
    override val siteURL: URL? = null,
    private val valid: Boolean = true
) : Feed {
    override fun isValid() = valid
}
