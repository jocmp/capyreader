package com.jocmp.basil

import com.jocmp.basil.extensions.asFeed
import com.jocmp.basil.extensions.asFolder
import com.jocmp.basil.opml.Outline
import com.jocmp.feedfinder.FeedFinder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URI
import java.net.URL
import java.util.UUID

data class Account(
    val id: String,
    val path: URI,
) {
    var folders = mutableSetOf<Folder>()

    var feeds = mutableSetOf<Feed>()

    val opmlFile = OPMLFile(
        path = path.resolve("subscriptions.opml"),
        account = this,
    )

    val displayName = "Test Display Name"

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

        val feed = Feed(
            id = UUID.randomUUID().toString(),
            name = entryNameOrDefault(entry, found.name),
            feedURL = found.feedURL.toString(),
            siteURL = entrySiteURL(found.siteURL)
        )

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
        items.forEach { item ->
            when (item) {
                is Outline.FolderOutline -> folders.add(item.asFolder)
                is Outline.FeedOutline -> feeds.add(item.asFeed)
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
