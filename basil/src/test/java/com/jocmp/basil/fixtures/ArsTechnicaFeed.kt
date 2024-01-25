package com.jocmp.basil.fixtures

import com.jocmp.feedfinder.parser.Feed
import java.net.URL

const val ARS_TECHNICA_URL = "https://feeds.arstechnica.com/arstechnica/index"

class ArsTechnicaFeed(
    override val name: String = "Ars Technica",
    override val feedURL: URL = URL(ARS_TECHNICA_URL),
    override val siteURL: URL? = null,
    private val valid: Boolean = true
) : Feed {
    override fun isValid() = valid
}
