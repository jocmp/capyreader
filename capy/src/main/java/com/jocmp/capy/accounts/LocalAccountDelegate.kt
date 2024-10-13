package com.jocmp.capy.accounts

import com.jocmp.capy.AccountDelegate
import com.jocmp.capy.Article
import com.jocmp.capy.Feed
import com.jocmp.capy.articles.ArticleContent
import com.jocmp.capy.common.TimeHelpers.nowUTC
import com.jocmp.capy.common.TimeHelpers.published
import com.jocmp.capy.common.transactionWithErrorHandling
import com.jocmp.capy.db.Database
import com.jocmp.capy.persistence.FeedRecords
import com.jocmp.capy.persistence.TaggingRecords
import com.jocmp.feedfinder.DefaultFeedFinder
import com.jocmp.feedfinder.FeedFinder
import com.prof18.rssparser.model.RssItem
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import java.net.UnknownHostException
import java.time.ZonedDateTime
import com.jocmp.feedfinder.parser.Feed as ParserFeed

class LocalAccountDelegate(
    private val database: Database,
    private val httpClient: OkHttpClient,
    private val feedFinder: FeedFinder = DefaultFeedFinder(httpClient),
) : AccountDelegate {
    private val articleContent = ArticleContent(httpClient)
    private val feedRecords = FeedRecords(database)
    private val taggingRecords = TaggingRecords(database)

    override suspend fun refresh(cutoffDate: ZonedDateTime?): Result<Unit> {
        refreshFeeds(cutoffDate)

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

            if (feeds.size > 1) {
                val choices = feeds.map {
                    FeedOption(feedURL = it.feedURL.toString(), title = it.name)
                }

                return AddFeedResult.MultipleChoices(choices)
            } else if (feeds.size == 1) {
                val resultFeed = feeds.first()
                upsertFeed(resultFeed, title = title)

                val feed = feedRecords.findBy(id = resultFeed.feedURL.toString())

                return if (feed != null) {
                    upsertFolders(feed, folderTitles)
                    saveArticles(resultFeed.items, cutoffDate = null, feed = feed)

                    AddFeedResult.Success(feed)
                } else {
                    AddFeedResult.Failure(AddFeedResult.AddFeedError.SaveFailure())
                }
            } else {
                return AddFeedResult.Failure(AddFeedResult.AddFeedError.FeedNotFound())
            }
        } catch (e: UnknownHostException) {
            return AddFeedResult.Failure(AddFeedResult.AddFeedError.NetworkError())
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
            feedRecords.findBy(feed.id) ?: return Result.failure(Throwable("Feed not found"))

        return Result.success(updatedFeed)
    }

    override suspend fun addStar(articleIDs: List<String>): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun removeStar(articleIDs: List<String>): Result<Unit> {
        return Result.success(Unit)
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

    override suspend fun fetchFullContent(article: Article): Result<String> {
        return articleContent.fetch(article.url)
    }

    private suspend fun refreshFeeds(cutoffDate: ZonedDateTime?) {
        try {
            val feeds = feedRecords.feeds().firstOrNull() ?: return

            coroutineScope {
                feeds.forEach { feed ->
                    launch {
                        feedFinder.fetch(feed.feedURL).onSuccess { channel ->
                            saveArticles(channel.items, cutoffDate = cutoffDate, feed = feed)
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            // continue
        }
    }

    private fun saveArticles(
        items: List<RssItem>,
        feed: Feed,
        cutoffDate: ZonedDateTime?,
        updatedAt: ZonedDateTime = nowUTC()
    ) {
        database.transactionWithErrorHandling {
            val updatedAtSeconds = updatedAt.toEpochSecond()

            items.forEach { item ->
                val publishedAt = published(item.pubDate, fallback = updatedAt).toEpochSecond()
                val parsedItem = ParsedItem(
                    item,
                    siteURL = feed.siteURL
                )

                val withinCutoff = cutoffDate == null || publishedAt > cutoffDate.toEpochSecond()

                if (parsedItem.url != null && withinCutoff) {
                    database.articlesQueries.create(
                        id = item.link!!,
                        feed_id = feed.id,
                        title = parsedItem.title,
                        author = item.author,
                        content_html = parsedItem.contentHTML,
                        url = parsedItem.url,
                        summary = item.summary,
                        extracted_content_url = null,
                        image_url = parsedItem.imageURL,
                        published_at = publishedAt,
                    )

                    database.articlesQueries.updateStatus(
                        article_id = item.link!!,
                        updated_at = updatedAtSeconds,
                        read = false
                    )
                }
            }
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
            favicon_url = feed.faviconURL?.toString()
        )
    }

    private fun upsertFolders(feed: Feed, folderTitles: List<String>?) {
        folderTitles ?: return

        folderTitles.forEach { folderTitle ->
            taggingRecords.upsert(
                id = "${feed.id}:$folderTitle",
                feedID = feed.id,
                name = folderTitle
            )
        }
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
