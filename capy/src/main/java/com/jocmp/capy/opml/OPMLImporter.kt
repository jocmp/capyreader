package com.jocmp.capy.opml

import com.jocmp.capy.Account
import java.io.InputStream
import java.net.URL
import kotlin.math.roundToInt

internal class OPMLImporter(private val account: Account) {
    internal suspend fun import(
        onProgress: (progress: ImportProgress) -> Unit = {},
        inputStream: InputStream,
    ) {
        var counter = 0
        val outlines = OPMLHandler.parse(inputStream)

        val entries = findEntries(outlines)

        val groupedForms = entries.groupBy { it.url.toString() }.toMap()
        val size = groupedForms.size

        onProgress(ImportProgress(currentCount = 0, total = size))

        groupedForms.forEach { (feedURL, forms) ->
            val folderTitles = forms.flatMap { it.folderTitles }.distinct()
            val title = forms.first().title

            account.addFeed(url = feedURL, title = title, folderTitles = folderTitles)
            counter += 1

            onProgress(
                ImportProgress(
                    currentCount = counter,
                    total = size,
                )
            )
        }
    }

    private fun findEntries(outlines: List<Outline>): List<AddFeedForm> {
        val feedEntries = mutableListOf<AddFeedForm>()

        outlines.forEach { outline ->
            if (outline is Outline.FeedOutline) {
                val feed = outline.feed

                if (!feed.xmlUrl.isNullOrBlank()) {
                    feedEntries.add(
                        AddFeedForm(
                            url = URL(feed.xmlUrl),
                            title = feed.title.orEmpty()
                        )
                    )
                }
            } else if (outline is Outline.FolderOutline) {
                val feeds = flattenFolder(outline.folder)

                feeds.forEach {
                    if (!it.xmlUrl.isNullOrBlank()) {
                        feedEntries.add(
                            AddFeedForm(
                                url = URL(it.xmlUrl),
                                title = it.title.orEmpty(),
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
        return folder.feeds + folder.folders.flatMap { flattenFolder(it) }
    }

    private fun folderTitle(title: String?): List<String> {
        return if (title.isNullOrBlank()) {
            emptyList()
        } else {
            listOf(title)
        }
    }

    private data class AddFeedForm(
        val url: URL,
        val title: String = "",
        val siteURL: URL? = null,
        val folderTitles: List<String> = emptyList()
    )
}

data class ImportProgress(
    val currentCount: Int = 0,
    val total: Int = 0,
) {
    val percent: Float
        get() = (currentCount.toFloat() / total.toFloat()).coerceIn(0f, 1f)
}
