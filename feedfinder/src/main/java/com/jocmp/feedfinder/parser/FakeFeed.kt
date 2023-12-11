package com.jocmp.feedfinder.parser

import java.net.URL

class FakeFeed: Feed {
    override fun isValid(): Boolean {
        return false
    }

    override val feedURL: URL
        get() = URL("https://arstechnica.com/feed")
}
