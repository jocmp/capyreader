package com.jocmp.capy.accounts.miniflux

import com.jocmp.capy.AccountDelegate
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.Feed
import com.jocmp.capy.accounts.AddFeedResult
import com.jocmp.capy.accounts.withErrorHandling
import com.jocmp.capy.common.TimeHelpers
import com.jocmp.capy.common.UnauthorizedError
import com.jocmp.capy.common.toDateTime
import com.jocmp.capy.common.transactionWithErrorHandling
import com.jocmp.capy.db.Database
import com.jocmp.capy.logging.CapyLog
import com.jocmp.capy.persistence.ArticleRecords
import com.jocmp.capy.persistence.EnclosureRecords
import com.jocmp.capy.persistence.FeedRecords
import com.jocmp.capy.persistence.TaggingRecords
import com.jocmp.minifluxclient.CreateCategoryRequest
import com.jocmp.minifluxclient.CreateFeedRequest
import com.jocmp.minifluxclient.Entry
import com.jocmp.minifluxclient.EntryResultSet
import com.jocmp.minifluxclient.EntryStatus
import com.jocmp.minifluxclient.Miniflux
import com.jocmp.minifluxclient.UpdateCategoryRequest
import com.jocmp.minifluxclient.UpdateEntriesRequest
import com.jocmp.minifluxclient.UpdateFeedRequest
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import okio.IOException
import org.jsoup.Jsoup
import retrofit2.Response
import java.time.ZonedDateTime
import com.jocmp.minifluxclient.Feed as MinifluxFeed

internal class MinifluxAccountDelegate(
    private val database: Database,
    private val miniflux: Miniflux
) : AccountDelegate {
    private val articleRecords = ArticleRecords(database)
    private val enclosureRecords = EnclosureRecords(database)
    private val feedRecords = FeedRecords(database)
    private val taggingRecords = TaggingRecords(database)

    override suspend fun refresh(filter: ArticleFilter, cutoffDate: ZonedDateTime?): Result<Unit> {
        return try {
            refreshFeeds()
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
            miniflux.updateEntries(
                UpdateEntriesRequest(
                    entry_ids = entryIDs,
                    status = EntryStatus.READ
                )
            )
            Unit
        }
    }

    override suspend fun markUnread(articleIDs: List<String>): Result<Unit> {
        val entryIDs = articleIDs.map { it.toLong() }

        return withErrorHandling {
            miniflux.updateEntries(
                UpdateEntriesRequest(
                    entry_ids = entryIDs,
                    status = EntryStatus.UNREAD
                )
            )
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

    override suspend fun addSavedSearch(articleID: String, savedSearchID: String): Result<Unit> {
        return Result.failure(UnsupportedOperationException("Labels not supported"))
    }

    override suspend fun removeSavedSearch(articleID: String, savedSearchID: String): Result<Unit> {
        return Result.failure(UnsupportedOperationException("Labels not supported"))
    }

    override suspend fun createSavedSearch(name: String): Result<String> {
        return Result.failure(UnsupportedOperationException("Labels not supported"))
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
                val icons = fetchIcons(listOf(feed))
                upsertFeed(feed, icons)

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
        val categories = miniflux.categories().body() ?: emptyList()
        val category = categories.find { it.title == oldTitle }

        if (category != null) {
            miniflux.updateCategory(
                categoryID = category.id,
                request = UpdateCategoryRequest(title = newTitle)
            )

            taggingRecords.updateTitle(previousTitle = oldTitle, title = newTitle)
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
            taggingRecords.deleteByFolderName(folderTitle)
        }

        Unit
    }

    private suspend fun refreshFeeds() {
        val refreshResponse = miniflux.feeds()
        val initialFeeds = refreshResponse.body()

        if (!refreshResponse.isSuccessful || initialFeeds == null) {
            return
        }

        val feedsResponse = miniflux.feeds()
        val feeds = feedsResponse.body() ?: return

        val icons = fetchIcons(feeds)

        database.transactionWithErrorHandling {
            feeds.forEach { feed ->
                upsertFeed(feed, icons)
            }
        }

        val feedsToKeep = feeds.map { it.id.toString() }
        database.feedsQueries.deleteAllExcept(feedsToKeep)
    }

    private suspend fun refreshArticles() {
        refreshStarredEntries()
        refreshUnreadEntries()
        fetchAllEntries()
    }

    private suspend fun refreshStarredEntries() {
        val ids = fetchAllEntryIDs { offset ->
            miniflux.entries(starred = true, limit = MAX_ENTRY_LIMIT, offset = offset)
        }

        articleRecords.markAllStarred(articleIDs = ids)
    }

    private suspend fun refreshUnreadEntries() {
        val ids = fetchAllEntryIDs { offset ->
            miniflux.entries(
                status = EntryStatus.UNREAD.value,
                limit = MAX_ENTRY_LIMIT,
                offset = offset
            )
        }

        articleRecords.markAllUnread(articleIDs = ids)
    }

    private suspend fun fetchAllEntryIDs(
        fetch: suspend (offset: Int) -> Response<EntryResultSet>
    ): List<String> {
        val firstPage = fetch(0).body() ?: return emptyList()
        val ids = firstPage.entries.map { it.id.toString() }.toMutableList()

        var offset = MAX_ENTRY_LIMIT
        while (ids.size < firstPage.total) {
            val page = fetch(offset).body() ?: break
            ids.addAll(page.entries.map { it.id.toString() })
            offset += MAX_ENTRY_LIMIT
        }

        return ids
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
                val imageURL = MinifluxEnclosureParsing.parsedImageURL(entry)
                val enclosures = entry.enclosures.orEmpty()

                database.articlesQueries.create(
                    id = articleID,
                    feed_id = entry.feed_id.toString(),
                    title = Jsoup.parse(entry.title).text(),
                    author = entry.author,
                    content_html = entry.content,
                    extracted_content_url = null,
                    url = entry.url,
                    summary = null,
                    image_url = imageURL,
                    published_at = entry.published_at.toDateTime?.toEpochSecond(),
                    enclosure_type = enclosures.firstOrNull()?.mime_type,
                )

                articleRecords.createStatus(
                    articleID = articleID,
                    updatedAt = updated,
                    read = entry.status == EntryStatus.READ
                )

                enclosures.forEach { enclosure ->
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

    private fun upsertFeed(feed: MinifluxFeed, icons: Map<Long, String>) {
        val icon = feed.icon?.icon_id?.let { icons[it] }

        database.feedsQueries.upsert(
            id = feed.id.toString(),
            subscription_id = feed.id.toString(),
            title = feed.title,
            feed_url = feed.feed_url,
            site_url = feed.site_url,
            favicon_url = icon,
            priority = null
        )

        feed.category?.let { category ->
            database.taggingsQueries.upsert(
                id = "${feed.id}-${category.id}",
                feed_id = feed.id.toString(),
                name = category.title
            )
        }
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

    private suspend fun fetchIcons(feeds: List<MinifluxFeed>): Map<Long, String> = coroutineScope {
        val iconIds = feeds.mapNotNull { it.icon?.icon_id }.filter { it > 0 }.distinct()

        iconIds.map { iconId ->
            async {
                try {
                    val response = miniflux.icon(iconId)
                    val iconData = response.body()

                    if (response.isSuccessful && iconData != null) {
                        iconId to "data:${iconData.data}"
                    } else {
                        null
                    }
                } catch (_: Exception) {
                    CapyLog.warn("fetch_icon", mapOf("icon_id" to iconId.toString()))
                    null
                }
            }
        }.awaitAll().filterNotNull().toMap()
    }

    companion object {
        const val MAX_ENTRY_LIMIT = 100
    }
}
