package com.jocmp.capy

import com.jocmp.capy.accounts.AddFeedResult
import java.time.ZonedDateTime

interface AccountDelegate {
    suspend fun refresh(filter: ArticleFilter, cutoffDate: ZonedDateTime? = null): Result<Unit>

    suspend fun markRead(articleIDs: List<String>): Result<Unit>

    suspend fun markUnread(articleIDs: List<String>): Result<Unit>

    suspend fun addStar(articleIDs: List<String>): Result<Unit>

    suspend fun removeStar(articleIDs: List<String>): Result<Unit>

    suspend fun addSavedSearch(articleID: String, savedSearchID: String): Result<Unit>

    suspend fun removeSavedSearch(articleID: String, savedSearchID: String): Result<Unit>

    suspend fun createSavedSearch(name: String): Result<String>

    suspend fun createPage(url: String): Result<Unit>

    suspend fun addFeed(
        url: String,
        title: String?,
        folderTitles: List<String>?
    ): AddFeedResult

    suspend fun updateFeed(
        feed: Feed,
        title: String,
        folderTitles: List<String>,
    ): Result<Feed>

    suspend fun updateFolder(
        oldTitle: String,
        newTitle: String
    ): Result<Unit>

    suspend fun removeFeed(feed: Feed): Result<Unit>

    suspend fun removeFolder(folderTitle: String): Result<Unit>

    suspend fun deletePage(articleID: String): Result<Unit> =
        Result.failure(UnsupportedOperationException())

    suspend fun saveArticleExternally(articleID: String): Result<Unit> =
        Result.failure(UnsupportedOperationException())
}
