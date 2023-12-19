package com.jocmp.basil.accounts

import java.net.URL

interface AccountDelegate {
    fun createFeed(feedURL: URL): ExternalFeed
}
