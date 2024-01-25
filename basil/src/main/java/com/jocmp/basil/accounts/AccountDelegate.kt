package com.jocmp.basil.accounts

import com.jocmp.basil.Feed
import java.net.URL

internal interface AccountDelegate {
    suspend fun createFeed(feedURL: URL): Result<String>

    suspend fun fetchAll(feed: Feed): List<ParsedItem>
}
