package com.jocmp.capy

import com.jocmp.capy.accounts.AddFeedResult
import com.jocmp.capy.accounts.FeedbinAccountDelegate
import com.jocmp.capy.accounts.FeedbinOkHttpClient
import com.jocmp.capy.common.sortedByTitle
import com.jocmp.capy.db.Database
import com.jocmp.capy.persistence.ArticleRecords
import com.jocmp.capy.persistence.FeedRecords
import com.jocmp.feedbinclient.Feedbin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.net.URI

data class Account(
    val id: String,
    val path: URI,
    val database: Database,
    val preferences: AccountPreferences,
    val delegate: AccountDelegate = run {
        val client = FeedbinOkHttpClient.forAccount(path, preferences)

        FeedbinAccountDelegate(
            database = database,
            feedbin = Feedbin.create(client = client)
        )
    }
) {
    internal val articleRecords: ArticleRecords = ArticleRecords(database)

    private val feedRecords: FeedRecords = FeedRecords(database)

    val allFeeds = feedRecords.feeds()

    val feeds: Flow<List<Feed>> = allFeeds.map { all ->
        all.filter { it.folderName.isBlank() }
            .sortedByTitle()
    }

    val folders: Flow<List<Folder>> = allFeeds.map { ungrouped ->
        ungrouped
            .filter { it.folderName.isNotBlank() }
            .groupBy { it.folderName }
            .map {
                Folder(
                    title = it.key,
                    feeds = it.value.sortedByTitle(),
                )
            }
            .sortedByTitle()
    }

    suspend fun addFeed(url: String): AddFeedResult {
        return delegate.addFeed(url)
    }

    suspend fun editFeed(form: EditFeedFormEntry): Result<Feed> {
        val feed = findFeed(form.feedID) ?: return Result.failure(Throwable("Feed not found"))

        return delegate.updateFeed(
            feed = feed,
            title = form.title,
            folderTitles = form.folderTitles
        )
    }

    suspend fun removeFeed(feedID: String): Result<Unit> {
        return delegate.removeFeed(feedID = feedID)
    }

    suspend fun refresh(): Result<Unit> = delegate.refresh()

    suspend fun findFeed(feedID: String): Feed? {
        return feedRecords.findBy(feedID)
    }

    suspend fun findFolder(title: String): Folder? {
        return feedRecords.findFolder(title = title)
    }

    fun findArticle(articleID: String): Article? {
        if (articleID.isBlank()) {
            return null
        }

        return articleRecords.find(articleID = articleID)
    }

    suspend fun addStar(articleID: String): Result<Unit> {
        articleRecords.addStar(articleID = articleID)

        return delegate.addStar(listOf(articleID))
    }

    suspend fun removeStar(articleID: String): Result<Unit> {
        articleRecords.removeStar(articleID = articleID)

        return delegate.removeStar(listOf(articleID))
    }

    fun unreadArticleIDs(filter: ArticleFilter, range: MarkRead): List<String> {
        return articleRecords.unreadArticleIDs(
            filter = filter,
            range = range
        )
    }

    suspend fun markAllRead(articleIDs: List<String>): Result<Unit> {
        articleRecords.markAllRead(articleIDs)

        return delegate.markRead(articleIDs)
    }

    suspend fun markRead(articleID: String): Result<Unit> {
        articleRecords.markAllRead(listOf(articleID))

        return delegate.markRead(listOf(articleID))
    }

    suspend fun markUnread(articleID: String): Result<Unit> {
        articleRecords.markUnread(articleID = articleID)

        return delegate.markUnread(listOf(articleID))
    }

    suspend fun fetchFullContent(article: Article): Result<String> {
        return delegate.fetchFullContent(article)
    }
}
