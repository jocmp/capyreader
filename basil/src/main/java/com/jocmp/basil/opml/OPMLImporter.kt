package com.jocmp.basil.opml

import com.jocmp.basil.Account
import com.jocmp.basil.AddFeedForm
import com.jocmp.basil.OPMLFile
import java.io.BufferedReader
import java.io.InputStream
import java.net.URI

/**
 * 1. Load file via `OPMLFile`
 * 2. Normalize feeds
 * 3. When normalized, iterate through each feed and call `createFeed` on account
 */
internal class OPMLImporter(private val account: Account) {
    internal suspend fun import(inputStream: InputStream) {
        val outlines = OPMLHandler.parse(inputStream)

        val entries = findEntries(outlines)

        entries.forEach {
            account.addFeed(it)
        }
    }

    private fun findEntries(outlines: List<Outline>): List<AddFeedForm> {
        val feedEntries = mutableListOf<AddFeedForm>()

        outlines.forEach { outline ->
            if (outline is Outline.FeedOutline) {
                val feed = outline.feed

                if (!feed.xmlUrl.isNullOrBlank()) {
                    feedEntries.add(AddFeedForm(url = feed.xmlUrl, name = feed.title ?: ""))
                }
            } else if (outline is Outline.FolderOutline) {
                val feeds = flattenFolder(outline.folder)

                feeds.forEach {
                    if (!it.xmlUrl.isNullOrBlank()) {
                        feedEntries.add(
                            AddFeedForm(
                                url = it.xmlUrl,
                                name = it.title ?: "",
                                folderTitles = folderTitle(outline.title)
                            )
                        )
                    }
                }
            }
        }

        return feedEntries
    }

    private fun flattenFolder(folder: Folder): List<Feed> {
        return if (folder.folders.isEmpty()) {
            folder.feeds
        } else {
            folder.feeds + folder.folders.flatMap { flattenFolder(it) }
        }
    }

    private fun folderTitle(title: String?): List<String> {
        return if (title.isNullOrBlank()) {
            emptyList()
        } else {
            listOf(title)
        }
    }
}
