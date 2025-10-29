package com.jocmp.capy.accounts.miniflux

import com.jocmp.capy.AccountDelegate
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.Feed
import com.jocmp.capy.accounts.AddFeedResult
import com.jocmp.capy.accounts.withErrorHandling
import com.jocmp.capy.common.TimeHelpers
import com.jocmp.capy.common.UnauthorizedError
import com.jocmp.capy.common.host
import com.jocmp.capy.common.toDateTime
import com.jocmp.capy.common.transactionWithErrorHandling
import com.jocmp.capy.common.withResult
import com.jocmp.capy.db.Database
import com.jocmp.capy.persistence.ArticleRecords
import com.jocmp.capy.persistence.EnclosureRecords
import com.jocmp.capy.persistence.FeedRecords
import com.jocmp.minifluxclient.Category
import com.jocmp.minifluxclient.CreateCategoryRequest
import com.jocmp.minifluxclient.CreateFeedRequest
import com.jocmp.minifluxclient.Entry
import com.jocmp.minifluxclient.Miniflux
import com.jocmp.minifluxclient.UpdateCategoryRequest
import com.jocmp.minifluxclient.UpdateEntriesRequest
import com.jocmp.minifluxclient.UpdateFeedRequest
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import okio.IOException
import org.jsoup.Jsoup
import java.time.ZonedDateTime

internal class MinifluxAccountDelegate(
    private val database: Database,
    private val miniflux: Miniflux
) : AccountDelegate {
    private val articleRecords = ArticleRecords(database)
    private val enclosureRecords = EnclosureRecords(database)
    private val feedRecords = FeedRecords(database)

    override suspend fun refresh(filter: ArticleFilter, cutoffDate: ZonedDateTime?): Result<Unit> {
        return try {
            refreshFeeds()
            refreshCategories()
            refreshArticles()

            Result.success(Unit)
        } catch (exception: IOException) {
            Result.failure(exception)
        } catch (e: UnauthorizedError) {
            Result.failure(e)
        }
    }

    override suspend fun markRead(articleIDs: List<String>): Result<Unit> {
        val entryIDs = articleIDs.map { it.toLong() }

        return withErrorHandling {
            miniflux.updateEntries(UpdateEntriesRequest(entry_ids = entryIDs, status = "read"))
            Unit
        }
    }

    override suspend fun markUnread(articleIDs: List<String>): Result<Unit> {
        val entryIDs = articleIDs.map { it.toLong() }

        return withErrorHandling {
            miniflux.updateEntries(UpdateEntriesRequest(entry_ids = entryIDs, status = "unread"))
            Unit
        }
    }

    override suspend fun addStar(articleIDs: List<String>): Result<Unit> {
        val entryIDs = articleIDs.map { it.toLong() }

        return withErrorHandling {
            entryIDs.forEach { entryID ->
                miniflux.toggleBookmark(entryID)
            }
            Unit
        }
    }

    override suspend fun removeStar(articleIDs: List<String>): Result<Unit> {
        val entryIDs = articleIDs.map { it.toLong() }

        return withErrorHandling {
            entryIDs.forEach { entryID ->
                miniflux.toggleBookmark(entryID)
            }
            Unit
        }
    }

    override suspend fun addFeed(
        url: String,
        title: String?,
        folderTitles: List<String>?
    ): AddFeedResult {
        return try {
            val categoryId = folderTitles?.firstOrNull()?.let { folderTitle ->
                getOrCreateCategory(folderTitle)
            }

            val response = miniflux.createFeed(
                CreateFeedRequest(feed_url = url, category_id = categoryId)
            )
            val createResponse = response.body()

            if (response.code() > 300 || createResponse == null) {
                return AddFeedResult.Failure(AddFeedResult.Error.FeedNotFound())
            }

            val feedResponse = miniflux.feed(createResponse.feed_id)
            val feed = feedResponse.body()

            return if (feed != null) {
                upsertFeed(feed)

                val localFeed = feedRecords.find(feed.id.toString())

                if (localFeed != null) {
                    coroutineScope {
                        launch { refreshArticles() }
                    }

                    AddFeedResult.Success(localFeed)
                } else {
                    AddFeedResult.Failure(AddFeedResult.Error.SaveFailure())
                }
            } else {
                AddFeedResult.Failure(AddFeedResult.Error.FeedNotFound())
            }
        } catch (e: IOException) {
            AddFeedResult.networkError()
        }
    }

    override suspend fun updateFeed(
        feed: Feed,
        title: String,
        folderTitles: List<String>,
    ): Result<Feed> = withErrorHandling {
        val categoryId = folderTitles.firstOrNull()?.let { folderTitle ->
            getOrCreateCategory(folderTitle)
        }

        miniflux.updateFeed(
            feedID = feed.id.toLong(),
            request = UpdateFeedRequest(title = title, category_id = categoryId)
        )

        feedRecords.update(
            feedID = feed.id,
            title = title,
        )

        feedRecords.find(feed.id)
    }

    override suspend fun updateFolder(
        oldTitle: String,
        newTitle: String
    ): Result<Unit> = withErrorHandling {
        // Find category by old title
        val categories = miniflux.categories().body() ?: emptyList()
        val category = categories.find { it.title == oldTitle }

        if (category != null) {
            miniflux.updateCategory(
                categoryID = category.id,
                request = UpdateCategoryRequest(title = newTitle)
            )
        }

        Unit
    }

    override suspend fun removeFeed(feed: Feed): Result<Unit> = withErrorHandling {
        miniflux.deleteFeed(feedID = feed.id.toLong())

        Unit
    }

    override suspend fun removeFolder(folderTitle: String): Result<Unit> = withErrorHandling {
        val categories = miniflux.categories().body() ?: emptyList()
        val category = categories.find { it.title == folderTitle }

        if (category != null) {
            miniflux.deleteCategory(categoryID = category.id)
        }

        Unit
    }

    private suspend fun refreshFeeds() {
        withResult(miniflux.feeds()) { feeds ->
            database.transactionWithErrorHandling {
                feeds.forEach { feed ->
                    upsertFeed(feed)
                }
            }

            val feedsToKeep = feeds.map { it.id.toString() }
            database.feedsQueries.deleteAllExcept(feedsToKeep)
        }
    }

    private suspend fun refreshCategories() {
        withResult(miniflux.categories()) { categories ->
            database.transactionWithErrorHandling {
                categories.forEach { category ->
                    database.taggingsQueries.upsert(
                        id = category.id.toString(),
                        feed_id = "", // Miniflux categories are not directly tied to a single feed
                        name = category.title,
                    )
                }
            }
        }
    }

    private suspend fun refreshArticles() {
        refreshStarredEntries()
        refreshUnreadEntries()
        fetchAllEntries()
    }

    private suspend fun refreshStarredEntries() {
        withResult(miniflux.entries(starred = true)) { result ->
            val ids = result.entries.map { it.id.toString() }
            articleRecords.markAllStarred(articleIDs = ids)
        }
    }

    private suspend fun refreshUnreadEntries() {
        withResult(miniflux.entries(status = "unread")) { result ->
            val ids = result.entries.map { it.id.toString() }
            articleRecords.markAllUnread(articleIDs = ids)
        }
    }

    private suspend fun fetchAllEntries() {
        var offset = 0
        val limit = MAX_ENTRY_LIMIT

        do {
            val response = miniflux.entries(
                limit = limit,
                offset = offset,
                order = "published_at",
                direction = "desc"
            )
            val result = response.body()

            if (result != null) {
                saveEntries(result.entries)
                offset += limit

                // Continue if we got a full page
                if (result.entries.size < limit) {
                    break
                }
            } else {
                break
            }
        } while (true)
    }

    private fun saveEntries(entries: List<Entry>) {
        database.transactionWithErrorHandling {
            entries.forEach { entry ->
                val updated = TimeHelpers.nowUTC()
                val articleID = entry.id.toString()

                database.articlesQueries.create(
                    id = articleID,
                    feed_id = entry.feed_id.toString(),
                    title = Jsoup.parse(entry.title).text(),
                    author = entry.author,
                    content_html = entry.content,
                    extracted_content_url = null,
                    url = entry.url,
                    summary = null,
                    image_url = null,
                    published_at = entry.published_at.toDateTime?.toEpochSecond(),
                )

                articleRecords.createStatus(
                    articleID = articleID,
                    updatedAt = updated,
                    read = entry.status == "read"
                )

                entry.enclosures?.forEach { enclosure ->
                    enclosureRecords.create(
                        url = enclosure.url,
                        type = enclosure.mime_type,
                        articleID = articleID,
                        itunesDurationSeconds = null,
                        itunesImage = null,
                    )
                }
            }
        }
    }

    private fun upsertFeed(feed: com.jocmp.minifluxclient.Feed) {
        database.feedsQueries.upsert(
            id = feed.id.toString(),
            subscription_id = feed.id.toString(),
            title = feed.title,
            feed_url = feed.feed_url,
            site_url = feed.site_url,
            favicon_url = feed.icon?.data
        )
    }

    private suspend fun getOrCreateCategory(title: String): Long {
        val categories = miniflux.categories().body() ?: emptyList()
        val existing = categories.find { it.title == title }

        return if (existing != null) {
            existing.id
        } else {
            val response = miniflux.createCategory(CreateCategoryRequest(title = title))
            response.body()?.id ?: throw IOException("Failed to create category")
        }
    }

    companion object {
        const val MAX_ENTRY_LIMIT = 100
    }
}
