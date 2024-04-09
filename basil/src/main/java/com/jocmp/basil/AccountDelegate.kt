package com.jocmp.basil

import com.jocmp.basil.accounts.AddFeedResult

interface AccountDelegate {
    suspend fun addFeed(url: String): Result<AddFeedResult>
    suspend fun addStar(articleIDs: List<String>): Result<Unit>
    suspend fun refresh(): Result<Unit>
    suspend fun removeStar(articleIDs: List<String>): Result<Unit>
    suspend fun markRead(articleIDs: List<String>): Result<Unit>
    suspend fun markUnread(articleIDs: List<String>): Result<Unit>
    suspend fun updateFeed(feed: Feed, title: String, folderTitles: List<String>): Result<Feed>

    suspend fun removeFeed(feedID: String)
}
