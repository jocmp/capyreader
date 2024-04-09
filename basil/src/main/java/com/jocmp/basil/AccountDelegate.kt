package com.jocmp.basil

import com.jocmp.basil.accounts.AddFeedResult

interface AccountDelegate {
    suspend fun addFeed(url: String): Result<AddFeedResult>
    suspend fun addStar(articleIDs: List<String>)
    suspend fun refresh()
    suspend fun removeStar(articleIDs: List<String>)
    suspend fun markRead(articleIDs: List<String>)
    suspend fun markUnread(articleIDs: List<String>)
    suspend fun updateFeed(feed: Feed, title: String, folderTitles: List<String>): Result<Feed>

    suspend fun removeFeed(feedID: String)
}
