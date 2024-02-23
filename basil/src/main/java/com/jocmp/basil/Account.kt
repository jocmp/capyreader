package com.jocmp.basil

import android.util.Log
import androidx.annotation.OpenForTesting
import com.jocmp.basil.accounts.AccountDelegate
import com.jocmp.basil.accounts.FeedbinAccountDelegate
import com.jocmp.basil.accounts.ParsedItem
import com.jocmp.basil.accounts.asOPML
import com.jocmp.basil.common.nowUTCInSeconds
import com.jocmp.basil.common.orEmpty
import com.jocmp.basil.common.upsert
import com.jocmp.basil.db.Database
import com.jocmp.basil.opml.OPMLImporter
import com.jocmp.basil.opml.Outline
import com.jocmp.basil.opml.asFeed
import com.jocmp.basil.opml.asFolder
import com.jocmp.basil.persistence.ArticleRecords
import com.jocmp.basil.persistence.FeedRecords
import com.jocmp.feedfinder.DefaultFeedFinder
import com.jocmp.feedfinder.FeedFinder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
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
    private val delegate: AccountDelegate = FeedbinAccountDelegate(this)

    var folders = mutableSetOf<Folder>()

    var feeds = mutableSetOf<Feed>()

    val opmlFile = OPMLFile(
        path = path.resolve("subscriptions.opml"),
        account = this,
    )

    internal val articleRecords: ArticleRecords = ArticleRecords(database)

    private val feedRecords: FeedRecords = FeedRecords(database)

    init {
        loadOPML(opmlFile.load())
    }

    val flattenedFeeds: Set<Feed>
        get() = mutableSetOf<Feed>().apply {
            addAll(feeds)
            addAll(folders.flatMap { it.feeds })
        }

    suspend fun addFolder(title: String): Folder {
        val folder = Folder(title = title)

        folders.add(folder)

        saveOPMLFile()

        return folder
    }

    suspend fun removeFolder(title: String) {
        val folder = findFolder(title) ?: return

        val folderFeeds = folder.feeds

        folders.remove(folder)

        val allFeeds = flattenedFeeds

        val orphanedFeeds = folderFeeds.filter { feed ->
            !allFeeds.contains(feed)
        }

        feeds.addAll(orphanedFeeds)

        saveOPMLFile()
    }

    suspend fun removeFeed(feedID: String) {
        feedRecords.removeFeed(feedID = feedID)
        removeFeedFromOPML(feedID)

        saveOPMLFile()
    }

    suspend fun addFeed(form: AddFeedForm): Result<Feed> {
        return saveNewFeed(form)
    }

    private suspend fun saveNewFeed(form: AddFeedForm): Result<Feed> {
        val externalID = delegate
            .createFeed(feedURL = form.url)
            .getOrNull() ?: return Result.failure(Throwable("Could not create external feed"))

        val record = feedRecords.findOrCreate(feedURL = form.url.toString())

        removeFeedFromOPML(record.id.toString())

        val feed = Feed(
            id = record.id.toString(),
            externalID = externalID,
            name = entryNameOrDefault(form.name),
            feedURL = form.url.toString(),
            siteURL = form.siteURL.orEmpty,
        )

        if (form.folderTitles.isEmpty()) {
            feeds.add(feed)
        } else {
            form.folderTitles.forEach { folderTitle ->
                val folder = findOrBuildFolder(title = folderTitle)

                folder.feeds.add(feed)

                if (folders.contains(folder)) {
                    folders.remove(folder)
                }

                folders.add(folder)
            }
        }

        coroutineScope {
            launch {
                val items = delegate.fetchAll(feed)

                updateArticles(feed, items)
            }
        }

        saveOPMLFile()

        return Result.success(feed)
    }

    suspend fun editFeed(form: EditFeedForm): Result<Feed> {
        val feed = findFeed(form.feedID) ?: return Result.failure(Throwable("Feed not found"))

        val editedFeed = feed.copy(name = entryNameOrDefault(form.name))

        if (form.folderTitles.isEmpty()) {
            feeds.upsert(editedFeed)
        } else {
            feeds.remove(editedFeed)
        }

        val removedFolders = folders
            .filter { it.feeds.contains(feed) && !form.folderTitles.contains(it.title) }
            .toSet()

        removedFolders.forEach { folder ->
            folder.feeds.remove(feed)
        }

        val emptyFolders = folders.filter { folder -> folder.feeds.isEmpty() }.toSet()

        folders.removeAll(emptyFolders)

        form.folderTitles.forEach { folderTitle ->
            val folder = findOrBuildFolder(title = folderTitle)
            folder.feeds.upsert(editedFeed)
            folders.upsert(folder)
        }

        saveOPMLFile()

        return Result.success(editedFeed)
    }

    suspend fun editFolder(form: EditFolderForm): Result<Folder> {
        val folder =
            findFolder(form.existingTitle) ?: return Result.failure(Throwable("Folder not found"))

        val updatedFolder = folder.copy(title = form.title)

        folders.remove(folder)
        folders.add(updatedFolder)

        saveOPMLFile()

        return Result.success(updatedFolder)
    }

    suspend fun refreshAll() {
        refreshCompactedFeeds(flattenedFeeds)
    }

    suspend fun refreshFeed(feed: Feed) {
        refreshFeeds(listOf(feed))
    }

    suspend fun refreshFeeds(feeds: List<Feed>) {
        val ids = feeds.map { it.id }.toSet()

        refreshCompactedFeeds(flattenedFeeds.filter { ids.contains(it.id) })
    }

    fun findFeed(feedID: String): Feed? {
        return flattenedFeeds.find { it.id == feedID }
    }

    fun findFolder(title: String): Folder? {
        return folders.find { it.title == title }
    }

    fun findArticle(articleID: String): Article? {
        if (articleID.isBlank()) {
            return null
        }

        return articleRecords.fetch(articleID = articleID)
    }

    fun addStar(articleID: String) {
        articleRecords.addStar(articleID = articleID)
    }

    fun removeStar(articleID: String) {
        articleRecords.removeStar(articleID = articleID)
    }

    fun markRead(articleID: String) {
        articleRecords.markRead(articleID)
    }

    fun markUnread(articleID: String) {
        articleRecords.markUnread(articleID = articleID)
    }

    suspend fun import(inputStream: InputStream) {
        OPMLImporter(this).import(inputStream) {
            Log.d(TAG, "import: progress=$it")
        }
    }

    private fun removeFeedFromOPML(feedID: String) {
        feeds.removeIf { it.id == feedID }

        folders.forEach { folder ->
            folder.feeds.removeIf { it.id == feedID }
        }

        folders.removeIf { it.feeds.isEmpty() }
    }

    private fun updateArticles(feed: Feed, items: List<ParsedItem>) {
        items.forEach { item ->
            val publishedAt = item.publishedAt?.toEpochSecond()

            database.transaction {
                database.articlesQueries.create(
                    feed_id = feed.primaryKey,
                    external_id = item.externalID,
                    title = item.title,
                    content_html = item.contentHTML,
                    url = item.url,
                    summary = item.summary,
                    image_url = item.imageURL,
                    published_at = publishedAt,
                )

                database.articlesQueries.updateStatus(
                    feed_id = feed.primaryKey,
                    external_id = item.externalID,
                    arrived_at = publishedAt ?: nowUTCInSeconds()
                )
            }

        }
    }

    private fun findOrBuildFolder(title: String): Folder {
        return folders.find { folder -> folder.title == title } ?: Folder(title = title)
    }

    private suspend fun refreshCompactedFeeds(feeds: Collection<Feed>) =
        withContext(Dispatchers.IO) {
            feeds.map { feed ->
                async {
                    val items = delegate.fetchAll(feed)
                    updateArticles(feed, items)
                }
            }.awaitAll()
        }

    private fun entryNameOrDefault(entryName: String): String {
        if (entryName.isNotBlank()) {
            return entryName
        }

        return DEFAULT_TITLE
    }

    private suspend fun saveOPMLFile() = withContext(Dispatchers.IO) {
        opmlFile.save()
    }

    private fun loadOPML(items: List<Outline>) {
        val ids = mutableListOf<Long>()

        items.forEach {
            when (it) {
                is Outline.FolderOutline -> ids.addAll(it.folder.feeds.mapNotNull { feed -> feed.id?.toLongOrNull() })
                is Outline.FeedOutline -> it.feed.id?.toLongOrNull()?.let { id -> ids.add(id) }
            }
        }

        val dbFeeds = database
            .feedsQueries
            .allByID(ids)
            .executeAsList()
            .associateBy { it.id }

        items.forEach { item ->
            when (item) {
                is Outline.FolderOutline -> folders.add(item.asFolder(dbFeeds))
                is Outline.FeedOutline -> item.feed.asFeed(dbFeeds)?.let { feeds.add(it) }
            }
        }
    }
}

internal fun Account.asOPML(): String {
    var opml = ""

    feeds.sortedBy { it.name }.forEach { feed ->
        opml += feed.asOPML(indentLevel = 2)
    }

    folders.sortedBy { it.title }.forEach { folder ->
        opml += folder.asOPML(indentLevel = 2)
    }

    return opml
}

private const val DEFAULT_TITLE = "(No title)"
