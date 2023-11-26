package com.jocmp.basil

import com.jocmp.basil.extensions.asFeed
import com.jocmp.basil.extensions.asFolder
import com.jocmp.basil.opml.Outline
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URI
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

    suspend fun addFeed(url: String = ""): Feed {
        val randomID = UUID.randomUUID().toString()
        val feed = Feed(id = randomID, name = randomID)

        feeds.add(feed)

        saveOPMLFile()

        return feed
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

    feeds.sorted().forEach { feed ->
        opml += feed.asOPML(indentLevel = 2)
    }

    folders.sorted().forEach { folder ->
        opml += folder.asOPML(indentLevel = 2)
    }

    return opml
}
