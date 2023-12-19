package com.jocmp.basil.accounts

import com.jocmp.basil.Account
import com.jocmp.basil.Feed
import java.net.URL
import java.util.UUID

internal class LocalAccountDelegate(private val account: Account): AccountDelegate {
    override fun createFeed(feedURL: URL): ExternalFeed {
        val allFeeds = mutableSetOf<Feed>().apply {
            addAll(account.feeds)
            addAll(account.folders.flatMap { it.feeds })
        }

        val existingFeed = allFeeds.find { it.feedURL == feedURL.toString() }

        if (existingFeed != null) {
            return ExternalFeed(externalID = existingFeed.id)
        }

        return ExternalFeed(externalID = UUID.randomUUID().toString())
    }
}
