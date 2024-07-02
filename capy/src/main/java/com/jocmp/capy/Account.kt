package com.jocmp.capy

import com.jocmp.capy.accounts.AddFeedResult
import com.jocmp.capy.accounts.FeedbinAccountDelegate
import com.jocmp.capy.accounts.FeedbinOkHttpClient
import com.jocmp.capy.accounts.LocalAccountDelegate
import com.jocmp.capy.accounts.Source
import com.jocmp.capy.accounts.asOPML
import com.jocmp.capy.common.sortedByTitle
import com.jocmp.capy.db.Database
import com.jocmp.capy.persistence.ArticleRecords
import com.jocmp.capy.persistence.FeedRecords
import com.jocmp.feedbinclient.Feedbin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File
import java.net.URI

data class Account(
    val id: String,
    val path: URI,
    val database: Database,
    val preferences: AccountPreferences,
    val source: Source = Source.LOCAL,
    val delegate: AccountDelegate = when (source) {
        Source.LOCAL -> LocalAccountDelegate(
            database = database
        )

        Source.FEEDBIN -> FeedbinAccountDelegate(
            database = database,
            feedbin = Feedbin.forAccount(path = path, preferences = preferences)
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

    suspend fun addFeed(
        url: String,
        title: String? = null,
        folderTitles: List<String>? = null
    ): AddFeedResult {
        return delegate.addFeed(
            url = url,
            title = title,
            folderTitles = folderTitles
        )
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
        val feed = feedRecords.findBy(feedID) ?: return Result.failure(Throwable("Feed not found"))

        return delegate.removeFeed(feed = feed).fold(
            onSuccess = {
                feedRecords.removeFeed(feedID = feed.id)
                Result.success(Unit)
            },
            onFailure = {
                Result.failure(it)
            }
        )
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

    suspend fun opmlDocument(): String {
        return OPMLFile(this).opmlDocument()
    }

    internal suspend fun asOPML(): String {
        var opml = ""

        feeds.first().forEach { feed ->
            opml += feed.asOPML(indentLevel = 2)
        }

        folders.first().forEach { folder ->
            opml += folder.asOPML(indentLevel = 2)
        }

        return opml
    }
}

private fun Feedbin.Companion.forAccount(path: URI, preferences: AccountPreferences) =
    create(client = FeedbinOkHttpClient.forAccount(path, preferences))
