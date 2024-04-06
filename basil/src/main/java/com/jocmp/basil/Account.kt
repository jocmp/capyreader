package com.jocmp.basil

import com.jocmp.basil.accounts.AddFeedResult
import com.jocmp.basil.accounts.FeedbinAccountDelegate
import com.jocmp.basil.accounts.forAccount
import com.jocmp.basil.db.Database
import com.jocmp.basil.persistence.ArticleRecords
import com.jocmp.basil.persistence.FeedRecords
import com.jocmp.feedbinclient.Feedbin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.net.URI

private const val TAG = "Account"

data class Account(
    val id: String,
    val path: URI,
    val database: Database,
    val preferences: AccountPreferences,
) {
    private val delegate = FeedbinAccountDelegate(
        database = database,
        feedbin = Feedbin.forAccount(this)
    )

    internal val articleRecords: ArticleRecords = ArticleRecords(database)

    private val feedRecords: FeedRecords = FeedRecords(database)

    private val allFeeds = feedRecords.feeds()

    val feeds: Flow<List<Feed>> = allFeeds.map { all ->
        all.filter { it.folderName.isBlank() }
    }

    val folders: Flow<List<Folder>> = allFeeds.map { ungrouped ->
        ungrouped
            .filter { it.folderName.isNotBlank() }
            .groupBy { it.folderName }
            .map {
                Folder(
                    title = it.key,
                    feeds = it.value,
                )
            }
    }

    suspend fun removeFolder(title: String) {
    }

    suspend fun removeFeed(feedID: String) {
        feedRecords.removeFeed(feedID = feedID)
    }

    suspend fun addFeed(url: String): Result<AddFeedResult> {
        return delegate.addFeed(url)
    }

    suspend fun editFeed(form: EditFeedForm): Result<Feed> {
        val feed = findFeed(form.feedID) ?: return Result.failure(Throwable("Feed not found"))

        val editedFeed = feed.copy(name = form.name)

        return Result.success(editedFeed)
    }

    suspend fun editFolder(form: EditFolderForm): Result<Folder> {
        val folder =
            findFolder(form.existingTitle) ?: return Result.failure(Throwable("Folder not found"))

        val updatedFolder = folder.copy(title = form.title)

        return Result.success(updatedFolder)
    }

    suspend fun refreshAll() {
        delegate.refreshAll()
    }

    suspend fun refreshFeed(feed: Feed) {
        refreshFeeds(listOf(feed))
    }

    suspend fun refreshFeeds(feeds: List<Feed>) {
    }

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

    suspend fun addStar(articleID: String) {
        articleRecords.addStar(articleID = articleID)

        delegate.addStar(listOf(articleID))
    }

    suspend fun removeStar(articleID: String) {
        articleRecords.removeStar(articleID = articleID)

        delegate.removeStar(listOf(articleID))
    }

    suspend fun markRead(articleID: String) {
        articleRecords.markRead(articleID)

        delegate.markRead(listOf(articleID))
    }

    suspend fun markUnread(articleID: String) {
        articleRecords.markUnread(articleID = articleID)

        delegate.markUnread(listOf(articleID))
    }

    private suspend fun refreshCompactedFeeds(feeds: Collection<Feed>) =
        withContext(Dispatchers.IO) {
            feeds.map { feed ->
                async {
                    val items = delegate.fetchAll(feed)
//                    updateArticles(feed, items)
                }
            }.awaitAll()
        }
}
