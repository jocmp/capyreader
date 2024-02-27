package com.jocmp.basil

import android.util.Log
import com.jocmp.basil.accounts.FeedbinAccountDelegate
import com.jocmp.basil.db.Database
import com.jocmp.basil.opml.OPMLImporter
import com.jocmp.basil.persistence.ArticleRecords
import com.jocmp.basil.persistence.FeedRecords
import com.jocmp.feedbinclient.Feedbin
import com.jocmp.feedfinder.DefaultFeedFinder
import com.jocmp.feedfinder.FeedFinder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.URI

private const val TAG = "Account"

data class Account(
    val id: String,
    val path: URI,
    val database: Database,
    val preferences: AccountPreferences,
    val feedFinder: FeedFinder = DefaultFeedFinder(),
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
            .map { Folder(title = it.key, feeds = it.value) }
    }

    val flattenedFeeds: Set<Feed>
        get() = mutableSetOf<Feed>().apply {
//            addAll(topLevelFeeds)
//            addAll(folders.flatMap { it.feeds })
        }

    suspend fun addFolder(title: String): Folder {
        val folder = Folder(title = title)

//        folders.add(folder)

        return folder
    }

    suspend fun removeFolder(title: String) {
        val folder = findFolder(title) ?: return

        val folderFeeds = folder.feeds

//        folders.remove(folder)

        val allFeeds = flattenedFeeds

        val orphanedFeeds = folderFeeds.filter { feed ->
            !allFeeds.contains(feed)
        }

//        topLevelFeeds.addAll(orphanedFeeds)
    }

    suspend fun removeFeed(feedID: String) {
        feedRecords.removeFeed(feedID = feedID)
        removeFeedFromOPML(feedID)
    }

    suspend fun addFeed(form: AddFeedForm): Result<Feed> {
        return saveNewFeed(form)
    }

    private suspend fun saveNewFeed(form: AddFeedForm): Result<Feed> {
        return Result.failure(Throwable("Sorry charlie"))
    }

    suspend fun editFeed(form: EditFeedForm): Result<Feed> {
        val feed = findFeed(form.feedID) ?: return Result.failure(Throwable("Feed not found"))

        val editedFeed = feed.copy(name = entryNameOrDefault(form.name))

//        if (form.folderTitles.isEmpty()) {
//            topLevelFeeds.upsert(editedFeed)
//        } else {
//            topLevelFeeds.remove(editedFeed)
//        }
//
//        val removedFolders = folders
//            .filter { it.feeds.contains(feed) && !form.folderTitles.contains(it.title) }
//            .toSet()
//
//        removedFolders.forEach { folder ->
////            folder.feeds.remove(feed)
//        }
//
//        val emptyFolders = folders.filter { folder -> folder.feeds.isEmpty() }.toSet()
//
//        folders.removeAll(emptyFolders)
//
//        form.folderTitles.forEach { folderTitle ->
//            val folder = findOrBuildFolder(title = folderTitle)
////            folder.feeds.upsert(editedFeed)
//            folders.upsert(folder)
//        }

        return Result.success(editedFeed)
    }

    suspend fun editFolder(form: EditFolderForm): Result<Folder> {
        val folder =
            findFolder(form.existingTitle) ?: return Result.failure(Throwable("Folder not found"))

        val updatedFolder = folder.copy(title = form.title)

//        folders.remove(folder)
//        folders.add(updatedFolder)
//
        return Result.success(updatedFolder)
    }

    suspend fun refreshAll() {
        delegate.refreshAll()
    }

    suspend fun refreshFeed(feed: Feed) {
        refreshFeeds(listOf(feed))
    }

    suspend fun refreshFeeds(feeds: List<Feed>) {
        val ids = feeds.map { it.id }.toSet()

        refreshCompactedFeeds(flattenedFeeds.filter { ids.contains(it.id) })
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

        return articleRecords.fetch(articleID = articleID)
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

    suspend fun import(inputStream: InputStream) {
        OPMLImporter(this).import(inputStream) {
            Log.d(TAG, "import: progress=$it")
        }
    }

    private fun removeFeedFromOPML(feedID: String) {
    }

//
//    private fun findOrBuildFolder(title: String): Folder {
////        return folders.find { folder -> folder.title == title } ?: Folder(title = title)
//    }

    private suspend fun refreshCompactedFeeds(feeds: Collection<Feed>) =
        withContext(Dispatchers.IO) {
            feeds.map { feed ->
                async {
                    val items = delegate.fetchAll(feed)
//                    updateArticles(feed, items)
                }
            }.awaitAll()
        }

    private fun entryNameOrDefault(entryName: String): String {
        if (entryName.isNotBlank()) {
            return entryName
        }

        return DEFAULT_TITLE
    }
}

internal fun Account.asOPML(): String {
    var opml = ""

//    topLevelFeeds.sortedBy { it.name }.forEach { feed ->
//        opml += feed.asOPML(indentLevel = 2)
//    }

//    folders.sortedBy { it.title }.forEach { folder ->
//        opml += folder.asOPML(indentLevel = 2)
//    }

    return opml
}

private const val DEFAULT_TITLE = "(No title)"
