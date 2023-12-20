package com.jocmp.basil

import com.jocmp.basil.accounts.AccountDelegate
import com.jocmp.basil.accounts.LocalAccountDelegate
import com.jocmp.basil.db.Database
import com.jocmp.basil.extensions.asFeed
import com.jocmp.basil.extensions.asFolder
import com.jocmp.basil.extensions.feeds
import com.jocmp.basil.extensions.findOrCreate
import com.jocmp.basil.opml.Outline
import com.jocmp.feedfinder.FeedFinder
import kotlinx.coroutines.Dispatchers
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

    val displayName = "Local"

    init {
        loadOPML(opmlFile.load())
    }

    suspend fun addFolder(title: String): Folder {
        val folder = Folder(title = title)

        folders.add(folder)

        saveOPMLFile()

        return folder
    }

    suspend fun addFeed(entry: FeedFormEntry): Feed {
        val result = FeedFinder.find(feedURL = entry.url)

        if (result is FeedFinder.Result.Failure) {
            throw Exception(result.error.toString())
        }

        val found = (result as FeedFinder.Result.Success).feeds.first()

        val externalFeed = delegate.createFeed(feedURL = found.feedURL)

        val record = database.feeds.findOrCreate(
            externalFeed = externalFeed,
            feedURL = found.feedURL
        )

        val feed = Feed(
            id = record.id.toString(),
            externalID = externalFeed.externalID,
            name = entryNameOrDefault(entry, found.name),
            feedURL = found.feedURL.toString(),
            siteURL = entrySiteURL(found.siteURL)
        )

        coroutineScope {
            launch {
                val items = delegate.fetchAll(feed)

                items.forEach { item ->
                    database.articlesQueries.create(
                        feed_id = record.id,
                        external_id = item.externalID,
                        title = item.title,
                        content_html = item.contentHTML,
                        url = item.url,
                        summary = item.summary,
                        image_url = item.imageURL
                    )
                }
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

        return feed
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
            .feeds
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

fun Account.asOPML(): String {
    var opml = ""

    feeds.sortedBy { it.name }.forEach { feed ->
        opml += feed.asOPML(indentLevel = 2)
    }

    folders.sortedBy { it.title }.forEach { folder ->
        opml += folder.asOPML(indentLevel = 2)
    }

    return opml
}
