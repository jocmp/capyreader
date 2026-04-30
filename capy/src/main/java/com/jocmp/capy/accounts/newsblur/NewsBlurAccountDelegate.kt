/*
 * Created by Josiah Campbell.
 */
package com.jocmp.capy.accounts.newsblur

import com.jocmp.capy.AccountDelegate
import com.jocmp.capy.AccountPreferences
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.Feed
import com.jocmp.capy.accounts.AddFeedResult
import com.jocmp.capy.db.Database
import com.jocmp.newsblurclient.NewsBlur
import java.time.ZonedDateTime

private const val PHASE_2 = "NewsBlur Phase 2"

internal class NewsBlurAccountDelegate(
    private val database: Database,
    private val newsblur: NewsBlur,
    private val preferences: AccountPreferences,
) : AccountDelegate {
    override suspend fun refresh(filter: ArticleFilter, cutoffDate: ZonedDateTime?): Result<Unit> {
        throw NotImplementedError(PHASE_2)
    }

    override suspend fun markRead(articleIDs: List<String>): Result<Unit> {
        throw NotImplementedError(PHASE_2)
    }

    override suspend fun markUnread(articleIDs: List<String>): Result<Unit> {
        throw NotImplementedError(PHASE_2)
    }

    override suspend fun addStar(articleIDs: List<String>): Result<Unit> {
        throw NotImplementedError(PHASE_2)
    }

    override suspend fun removeStar(articleIDs: List<String>): Result<Unit> {
        throw NotImplementedError(PHASE_2)
    }

    override suspend fun addSavedSearch(articleID: String, savedSearchID: String): Result<Unit> {
        return Result.failure(UnsupportedOperationException("Labels not supported"))
    }

    override suspend fun removeSavedSearch(articleID: String, savedSearchID: String): Result<Unit> {
        return Result.failure(UnsupportedOperationException("Labels not supported"))
    }

    override suspend fun createSavedSearch(name: String): Result<String> {
        return Result.failure(UnsupportedOperationException("Labels not supported"))
    }

    override suspend fun createPage(url: String): Result<Unit> {
        throw NotImplementedError(PHASE_2)
    }

    override suspend fun addFeed(
        url: String,
        title: String?,
        folderTitles: List<String>?,
    ): AddFeedResult {
        throw NotImplementedError(PHASE_2)
    }

    override suspend fun updateFeed(
        feed: Feed,
        title: String,
        folderTitles: List<String>,
    ): Result<Feed> {
        throw NotImplementedError(PHASE_2)
    }

    override suspend fun updateFolder(oldTitle: String, newTitle: String): Result<Unit> {
        throw NotImplementedError(PHASE_2)
    }

    override suspend fun removeFeed(feed: Feed): Result<Unit> {
        throw NotImplementedError(PHASE_2)
    }

    override suspend fun removeFolder(folderTitle: String): Result<Unit> {
        throw NotImplementedError(PHASE_2)
    }
}
