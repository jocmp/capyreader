package com.jocmp.capy.accounts

import com.jocmp.capy.AccountDelegate
import com.jocmp.capy.Article
import com.jocmp.capy.Feed
import com.jocmp.capy.articles.ArticleContent
import com.jocmp.capy.common.nowUTC
import com.jocmp.capy.common.toDateTime
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

    override suspend fun refresh(): Result<Unit> {
        refreshFeeds()

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
        folderTitles: List<String>
    ): Result<Feed> {
        if (title != feed.title) {
            feedRecords.updateTitle(feed = feed, title = title)
        }

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

    private suspend fun refreshFeeds() {
        val feeds = feedRecords.feeds().firstOrNull() ?: return

        feeds.forEach { feed ->
            coroutineScope {
                launch {
                    feedFinder.fetch(feed.feedURL).onSuccess { channel ->
                        val saveableArticles = channel.items.filter { !it.link.isNullOrBlank() }

                        saveArticles(saveableArticles, feed)
                    }
                }
            }
        }
    }

    private fun saveArticles(
        items: List<RssItem>,
        feed: Feed,
        updatedAt: ZonedDateTime = nowUTC()
    ) {
        database.transactionWithErrorHandling {
            items.forEach { item ->
                val updated = updatedAt.toEpochSecond()

                database.articlesQueries.create(
                    id = item.link!!,
                    feed_id = feed.id,
                    title = item.title,
                    author = item.author,
                    content_html = item.content,
                    url = item.link,
                    summary = cleanSummary(item.description),
                    extracted_content_url = null,
                    image_url = item.image,
                    published_at = item.pubDate?.toDateTime?.toEpochSecond() ?: updated,
                )

                database.articlesQueries.updateStatus(
                    article_id = item.link!!,
                    updated_at = updated,
                    read = false
                )
            }

        }
    }

    private fun upsertFeed(
        feed: ParserFeed,
        title: String?,
    ) {
        val feedURL = feed.feedURL.toString()

        database.feedsQueries.upsert(
            id = feedURL,
            subscription_id = feedURL,
            title = title ?: feed.name,
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

private fun cleanSummary(summary: String?): String? {
    if (summary.isNullOrBlank()) {
        return null
    }

    return Jsoup.parse(summary).text()
}
