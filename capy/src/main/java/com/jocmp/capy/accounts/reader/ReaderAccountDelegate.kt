package com.jocmp.capy.accounts.reader

import com.jocmp.capy.AccountDelegate
import com.jocmp.capy.Article
import com.jocmp.capy.Feed
import com.jocmp.capy.accounts.AddFeedResult
import java.time.ZonedDateTime

/**
 * Save Auth Token for later use
 * self.credentials = Credentials(type: .readerAPIKey, username: credentials.username, secret: authString)
 */
internal class ReaderAccountDelegate: AccountDelegate {
    override suspend fun addFeed(
        url: String,
        title: String?,
        folderTitles: List<String>?
    ): AddFeedResult {
        return AddFeedResult.Failure(error = AddFeedResult.AddFeedError.NetworkError())
    }

    override suspend fun addStar(articleIDs: List<String>): Result<Unit> {
        return Result.failure(Throwable(""))
    }

    override suspend fun refresh(cutoffDate: ZonedDateTime?): Result<Unit> {
        return Result.failure(Throwable(""))
    }

    override suspend fun removeStar(articleIDs: List<String>): Result<Unit> {
        return Result.failure(Throwable(""))
    }

    override suspend fun markRead(articleIDs: List<String>): Result<Unit> {
        return Result.failure(Throwable(""))
    }

    override suspend fun markUnread(articleIDs: List<String>): Result<Unit> {
        return Result.failure(Throwable(""))
    }

    override suspend fun updateFeed(
        feed: Feed,
        title: String,
        folderTitles: List<String>
    ): Result<Feed> {
        return Result.failure(Throwable(""))
    }

    override suspend fun removeFeed(feed: Feed): Result<Unit> {
        return Result.failure(Throwable(""))
    }

    override suspend fun fetchFullContent(article: Article): Result<String> {
        return Result.failure(Throwable(""))
    }
}
