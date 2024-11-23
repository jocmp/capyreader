package com.jocmp.capy.fixtures

import com.jocmp.feedfinder.parser.Feed
import com.jocmp.rssparser.model.RssItem
import java.net.URL

class GenericFeed(
    override val name: String,
    url: String,
    override val siteURL: URL? = null,
    private val valid: Boolean = true,
    override val faviconURL: URL? = null,
    override val items: List<RssItem> = emptyList(),
) : Feed {
    override fun isValid() = valid

    override val feedURL = URL(url)
}
