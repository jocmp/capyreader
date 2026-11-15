package com.jocmp.capy.opml

import com.jocmp.capy.Account
import com.jocmp.capy.common.optionalURL
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.io.InputStream
import java.net.URL
import java.util.concurrent.atomic.AtomicInteger

internal class OPMLImporter(private val account: Account) {
    internal suspend fun import(
        onProgress: (progress: ImportProgress) -> Unit = {},
        inputStream: InputStream,
        concurrency: Int = 1,
    ) {
        val outlines = OPMLHandler.parse(inputStream)

        val entries = findEntries(outlines)

        val groupedForms = entries.groupBy { it.url.toString() }.toMap()
        val size = groupedForms.size
        val counter = AtomicInteger(0)
        val gate = Semaphore(concurrency)

        onProgress(ImportProgress(currentCount = 0, total = size))

        coroutineScope {
            groupedForms.forEach { (feedURL, forms) ->
                launch {
                    gate.withPermit {
                        val folderTitles = forms.flatMap { it.folderTitles }.distinct()
                        val title = forms.first().title

                        account.importFeed(
                            url = feedURL,
                            title = title,
                            folderTitles = folderTitles
                        )
                    }

                    onProgress(
                        ImportProgress(
                            currentCount = counter.incrementAndGet(),
                            total = size,
                        )
                    )
                }
            }
        }
    }

    private fun findEntries(outlines: List<Outline>): List<AddFeedForm> {
        val feedEntries = mutableListOf<AddFeedForm>()

        outlines.forEach { outline ->
            if (outline is Outline.FeedOutline) {
                val feed = outline.feed
                val xmlURL = optionalURL(feed.xmlUrl)

                if (xmlURL != null) {
                    feedEntries.add(
                        AddFeedForm(
                            url = xmlURL,
                            title = feed.title.orEmpty()
                        )
                    )
                }
            } else if (outline is Outline.FolderOutline) {
                val feeds = flattenFolder(outline.folder)

                feeds.forEach {
                    val xmlURL = optionalURL(it.xmlUrl)

                    if (xmlURL != null) {
                        feedEntries.add(
                            AddFeedForm(
                                url = xmlURL,
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
