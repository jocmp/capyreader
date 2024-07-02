package com.jocmp.capy

import com.jocmp.capy.accounts.AddFeedResult

interface AccountDelegate {
    suspend fun addFeed(
        url: String,
        title: String?,
        folderTitles: List<String>?
    ): AddFeedResult

    suspend fun addStar(articleIDs: List<String>): Result<Unit>

    suspend fun refresh(): Result<Unit>

    suspend fun removeStar(articleIDs: List<String>): Result<Unit>

    suspend fun markRead(articleIDs: List<String>): Result<Unit>

    suspend fun markUnread(articleIDs: List<String>): Result<Unit>

    suspend fun updateFeed(feed: Feed, title: String, folderTitles: List<String>): Result<Feed>

    suspend fun removeFeed(feed: Feed): Result<Unit>

    suspend fun fetchFullContent(article: Article): Result<String>
}
