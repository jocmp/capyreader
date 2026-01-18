package com.jocmp.capy.accounts.local

import com.jocmp.capy.AccountDelegate
import com.jocmp.capy.AccountPreferences
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.Feed
import com.jocmp.capy.accounts.AddFeedResult
import com.jocmp.capy.accounts.FaviconFetcher
import com.jocmp.capy.accounts.FeedOption
import com.jocmp.capy.common.TimeHelpers.nowUTC
import com.jocmp.capy.common.TimeHelpers.published
import com.jocmp.capy.common.transactionWithErrorHandling
import com.jocmp.capy.db.Database
import com.jocmp.capy.logging.CapyLog
import com.jocmp.capy.persistence.ArticleRecords
import com.jocmp.capy.persistence.EnclosureRecords
import com.jocmp.capy.persistence.FeedRecords
import com.jocmp.capy.persistence.TaggingRecords
import com.jocmp.feedfinder.DefaultFeedFinder
import com.jocmp.feedfinder.FeedFinder
import com.jocmp.rssparser.model.RssItem
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import java.net.UnknownHostException
import java.time.ZonedDateTime
import com.jocmp.feedfinder.parser.Feed as ParserFeed

internal class LocalAccountDelegate(
    private val database: Database,
    private val httpClient: OkHttpClient,
    private val faviconFetcher: FaviconFetcher,
    private val feedFinder: FeedFinder = DefaultFeedFinder(httpClient),
    private val preferences: AccountPreferences,
) : AccountDelegate {
    private val feedRecords = FeedRecords(database)
    private val articleRecords = ArticleRecords(database)
    private val taggingRecords = TaggingRecords(database)
    private val enclosureRecords = EnclosureRecords(database)

    override suspend fun refresh(filter: ArticleFilter, cutoffDate: ZonedDateTime?): Result<Unit> {
        when (filter) {
            is ArticleFilter.Feeds -> refreshFeedFilter(filter, cutoffDate = cutoffDate)
            is ArticleFilter.Folders -> refreshFolderFilter(filter, cutoffDate = cutoffDate)
            else -> refreshArticleFilter(cutoffDate)
        }

        return Result.success(Unit)
    }

    override suspend fun addFeed(
        url: String,
        title: String?,
        folderTitles: List<String>?
    ): AddFeedResult {
        try {
            val response = feedFinder.find(url = url)

            val feeds = response.getOrDefault(emptyList())

            if (feeds.isEmpty()) {
                val exception = response.exceptionOrNull()
                CapyLog.warn(
                    tag("find"),
                    data = mapOf(
                        "error_type" to exception?.javaClass.toString(),
                        "error_message" to exception?.message,
                        "feed_url" to url
                    )
                )

                if (exception != null && exception is UnknownHostException) {
                    return AddFeedResult.connectionError()
                }

                return AddFeedResult.feedNotFound()
            }

            if (feeds.size > 1) {
                val choices = feeds.map {
                    FeedOption(feedURL = it.feedURL.toString(), title = it.name)
                }

                return AddFeedResult.MultipleChoices(choices)
            } else {
                val resultFeed = feeds.first()
                upsertFeed(resultFeed, title = title)

                val feed = feedRecords.find(id = resultFeed.feedURL.toString())

                return if (feed != null) {
                    upsertFolders(feed, folderTitles)
                    saveArticles(resultFeed.items, cutoffDate = null, feed = feed)
                    verifyFavicon(feed)

                    AddFeedResult.Success(feed)
                } else {
                    AddFeedResult.saveFailure()
                }
            }
        } catch (e: UnknownHostException) {
            CapyLog.error(tag("find"), e)
            return AddFeedResult.networkError()
        }
    }

    override suspend fun updateFeed(
        feed: Feed,
        title: String,
        folderTitles: List<String>,
    ): Result<Feed> {
        feedRecords.update(
            feedID = feed.id,
            title = title,
        )

        val taggingIDsToDelete = taggingRecords.findFeedTaggingsToDelete(
            feed = feed,
            excludedTaggingNames = folderTitles
        )

        upsertFolders(feed, folderTitles = folderTitles)

        taggingRecords.deleteTaggings(taggingIDsToDelete)

        val updatedFeed =
            feedRecords.find(feed.id) ?: return Result.failure(Throwable("Feed not found"))

        return Result.success(updatedFeed)
    }

    override suspend fun updateFolder(oldTitle: String, newTitle: String): Result<Unit> {
        // no-op
        return Result.success(Unit)
    }


    override suspend fun addStar(articleIDs: List<String>): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun removeStar(articleIDs: List<String>): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun addSavedSearch(articleID: String, savedSearchID: String): Result<Unit> {
        return Result.failure(UnsupportedOperationException("Labels not supported"))
    }

    override suspend fun removeSavedSearch(articleID: String, savedSearchID: String): Result<Unit> {
        return Result.failure(UnsupportedOperationException("Labels not supported"))
    }

    override suspend fun createSavedSearch(name: String): Result<String> {
        return Result.failure(UnsupportedOperationException("Labels not supported"))
    }

    override suspend fun markRead(articleIDs: List<String>): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun markUnread(articleIDs: List<String>): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun removeFeed(feed: Feed): Result<Unit> {
        val ids = taggingRecords.findFeedTaggingsToDelete(feed = feed)
        taggingRecords.deleteTaggings(ids)

        return Result.success(Unit)
    }

    override suspend fun removeFolder(folderTitle: String): Result<Unit> {
        // no-op
        return Result.success(Unit)
    }

    private suspend fun refreshFeeds(feeds: List<Feed>, cutoffDate: ZonedDateTime?) {
        coroutineScope {
            feeds.forEach { feed ->
                launch {
                    refreshFeed(feed, cutoffDate = cutoffDate)
                }
            }
        }
    }

    private suspend fun refreshArticleFilter(cutoffDate: ZonedDateTime?) {
        val feeds = feedRecords.feeds().firstOrNull() ?: return

        refreshFeeds(feeds, cutoffDate = cutoffDate)
    }

    private suspend fun refreshFolderFilter(
        filter: ArticleFilter.Folders,
        cutoffDate: ZonedDateTime?
    ) {
        val folder = feedRecords.findFolder(title = filter.folderTitle) ?: return

        refreshFeeds(folder.feeds, cutoffDate = cutoffDate)
    }

    private suspend fun refreshFeedFilter(
        filter: ArticleFilter.Feeds,
        cutoffDate: ZonedDateTime?
    ) {
        val feed = feedRecords.find(id = filter.feedID) ?: return

        refreshFeed(feed, cutoffDate = cutoffDate)
    }

    private suspend fun refreshFeed(feed: Feed, cutoffDate: ZonedDateTime?) {
        try {
            feedFinder.fetch(feed.feedURL).onSuccess { channel ->
                saveArticles(channel.items, cutoffDate = cutoffDate, feed = feed)
            }
        } catch (e: Throwable) {
            CapyLog.error("refresh", e)
        }
    }

    private fun saveArticles(
        items: List<RssItem>,
        feed: Feed,
        cutoffDate: ZonedDateTime?,
        updatedAt: ZonedDateTime = nowUTC()
    ) {
        val blocklist = preferences.keywordBlocklist.get()

        database.transactionWithErrorHandling {
            items.forEach { item ->
                val publishedAt = published(item.pubDate, fallback = updatedAt).toEpochSecond()
                val parsedItem = ParsedItem(
                    item,
                    siteURL = feed.siteURL
                )

                val withinCutoff = cutoffDate == null || publishedAt > cutoffDate.toEpochSecond()
                val blocked = containsBlockedText(parsedItem, blocklist)

                if (parsedItem.id != null && withinCutoff && !blocked) {
                    val enclosureType = parsedItem.enclosures.firstOrNull()?.type

                    database.articlesQueries.create(
                        id = parsedItem.id,
                        feed_id = feed.id,
                        title = parsedItem.title,
                        author = item.author,
                        content_html = parsedItem.contentHTML,
                        url = parsedItem.url,
                        summary = item.summary,
                        extracted_content_url = null,
                        image_url = parsedItem.imageURL,
                        published_at = publishedAt,
                        enclosure_type = enclosureType,
                    )

                    articleRecords.createStatus(
                        articleID = parsedItem.id,
                        updatedAt = updatedAt,
                        read = false
                    )

                    parsedItem.enclosures.forEach {
                        enclosureRecords.create(
                            url = it.url.toString(),
                            type = it.type,
                            articleID = parsedItem.id,
                            itunesDurationSeconds = it.itunesDurationSeconds?.toString(),
                            itunesImage = it.itunesImage,
                        )
                    }
                }
            }
        }
    }

    private fun containsBlockedText(parsedItem: ParsedItem, blocklist: Set<String>): Boolean {
        return blocklist.any { keyword ->
            parsedItem.title.contains(keyword, ignoreCase = true) ||
                    parsedItem.summary.orEmpty().contains(keyword, ignoreCase = true) ||
                    parsedItem.contentHTML.orEmpty().contains(keyword, ignoreCase = true)
        }
    }

    private fun upsertFeed(
        feed: ParserFeed,
        title: String?,
    ) {
        val feedURL = feed.feedURL.toString()

        val feedTitle = if (title.isNullOrBlank()) {
            feed.name
        } else {
            title
        }

        database.feedsQueries.upsert(
            id = feedURL,
            subscription_id = feedURL,
            title = feedTitle,
            feed_url = feedURL,
            site_url = feed.siteURL?.toString(),
            favicon_url = feed.faviconURL?.toString(),
            priority = null
        )
    }

    private fun upsertFolders(feed: Feed, folderTitles: List<String>?) {
        folderTitles ?: return

        database.transactionWithErrorHandling {
            folderTitles.forEach { folderTitle ->
                taggingRecords.upsert(
                    id = "${feed.id}:$folderTitle",
                    feedID = feed.id,
                    name = folderTitle
                )
            }
        }
    }

    private suspend fun verifyFavicon(feed: Feed) {
        if (faviconFetcher.isValid(feed.faviconURL)) {
            return
        }

        CapyLog.warn(
            tag("favicon"), data = mapOf(
                "invalid_favicon_url" to feed.faviconURL,
                "feed_url" to feed.feedURL,
            )
        )

        feedRecords.clearFavicon(feed.id)
    }

    companion object {
        private fun tag(path: String) = "$TAG.$path"

        private const val TAG = "local"
    }
}

internal val RssItem.contentHTML: String?
    get() {
        val currentContent = content.orEmpty().ifBlank {
            description.orEmpty()
        }

        if (currentContent.isBlank()) {
            return null
        }

        return currentContent
    }

internal val RssItem.summary: String?
    get() = description?.let {
        if (it.isBlank()) {
            return null
        }

        return Jsoup.parse(it).text()
    }
