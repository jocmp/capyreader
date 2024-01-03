package com.jocmp.basil

import com.jocmp.basil.accounts.AccountDelegate
import com.jocmp.basil.accounts.LocalAccountDelegate
import com.jocmp.basil.accounts.ParsedItem
import com.jocmp.basil.accounts.asOPML
import com.jocmp.basil.db.Database
import com.jocmp.basil.opml.Outline
import com.jocmp.basil.opml.asFeed
import com.jocmp.basil.opml.asFolder
import com.jocmp.basil.persistence.ArticleRecords
import com.jocmp.basil.persistence.FeedRecords
import com.jocmp.basil.persistence.articleMapper
import com.jocmp.basil.shared.nowUTCInSeconds
import com.jocmp.feedfinder.FeedFinder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URI
import java.net.URL

data class Account(
    val id: String,
    val path: URI,
    val database: Database,
    val source: AccountSource = AccountSource.LOCAL,
) {
    private var delegate: AccountDelegate

    init {
        when (source) {
            AccountSource.LOCAL -> delegate = LocalAccountDelegate(this)
        }
    }

    var folders = mutableSetOf<Folder>()

    var feeds = mutableSetOf<Feed>()

    val opmlFile = OPMLFile(
        path = path.resolve("subscriptions.opml"),
        account = this,
    )

    internal val articles: ArticleRecords = ArticleRecords(database)

    val displayName = "Local"

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

    suspend fun addFeed(entry: FeedFormEntry): Result<Feed> {
        val result = FeedFinder.find(feedURL = entry.url)

        if (result is FeedFinder.Result.Failure) {
            return Result.failure(Throwable(message = result.error.name))
        }

        val found = (result as FeedFinder.Result.Success).feeds.first()

        val externalFeed = delegate.createFeed(feedURL = found.feedURL)

        val record = FeedRecords(database).findOrCreate(externalFeed)

        val feed = Feed(
            id = record.id.toString(),
            externalID = externalFeed.externalID,
            name = entryNameOrDefault(entry, found.name),
            feedURL = externalFeed.feedURL,
            siteURL = entrySiteURL(found.siteURL)
        )

        coroutineScope {
            launch {
                val items = delegate.fetchAll(feed)

                updateArticles(feed, items)
            }
        }

        if (entry.folderTitles.isEmpty()) {
            feeds.add(feed)
        } else {
            entry.folderTitles.forEach { folderTitle ->
                val folder = folders.find { folder -> folder.title == folderTitle }
                    ?: Folder(title = folderTitle)

                folder.feeds.add(feed)

                if (folders.contains(folder)) {
                    folders.remove(folder)
                }

                folders.add(folder)
            }
        }

        saveOPMLFile()

        return Result.success(feed)
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
        return articles.fetch(articleID = articleID)
    }

    fun addStar(articleID: String) {
        articles.addStar(articleID = articleID)
    }

    fun removeStar(articleID: String) {
        articles.removeStar(articleID = articleID)
    }

    fun markRead(articleID: String) {
        articles.markRead(articleID)
    }

    fun markUnread(articleID: String) {
        articles.markUnread(articleID = articleID)
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

    private suspend fun refreshCompactedFeeds(feeds: Collection<Feed>) =
        withContext(Dispatchers.IO) {
            feeds.map { feed ->
                async {
                    val items = delegate.fetchAll(feed)
                    updateArticles(feed, items)
                }
            }.awaitAll()
        }

    private fun entrySiteURL(url: URL?): String {
        return url?.toString() ?: ""
    }

    private fun entryNameOrDefault(entry: FeedFormEntry, feedName: String): String {
        if (entry.name.isBlank()) {
            return feedName
        }

        return entry.name
    }

    private suspend fun saveOPMLFile() = withContext(Dispatchers.IO) {
        opmlFile.save()
    }

    private fun loadOPML(items: List<Outline>) {
        val ids = mutableListOf<Long>()

        items.forEach {
            when (it) {
                is Outline.FeedOutline -> it.feed.id?.toLongOrNull()?.let { id -> ids.add(id) }
                is Outline.FolderOutline -> ids.addAll(it.folder.feeds.mapNotNull { feed -> feed.id?.toLongOrNull() })
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
