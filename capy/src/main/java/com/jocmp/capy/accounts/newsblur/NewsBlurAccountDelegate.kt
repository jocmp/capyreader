package com.jocmp.capy.accounts.newsblur

import com.jocmp.capy.AccountDelegate
import com.jocmp.capy.AccountPreferences
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.Feed
import com.jocmp.capy.accounts.AddFeedResult
import com.jocmp.capy.accounts.orThrow
import com.jocmp.capy.accounts.willAutoDelete
import com.jocmp.capy.accounts.withErrorHandling
import com.jocmp.capy.common.ContentFormatter
import com.jocmp.capy.common.TimeHelpers
import com.jocmp.capy.common.UnauthorizedError
import com.jocmp.capy.common.stripTitleMarkup
import com.jocmp.capy.common.transactionWithErrorHandling
import com.jocmp.capy.db.Database
import com.jocmp.capy.logging.CapyLog
import com.jocmp.capy.persistence.ArticleRecords
import com.jocmp.capy.persistence.FeedRecords
import com.jocmp.capy.persistence.TaggingRecords
import com.jocmp.newsblurclient.FeedsResponse
import com.jocmp.newsblurclient.NewsBlur
import com.jocmp.newsblurclient.Story
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import okio.IOException
import retrofit2.Response
import java.time.ZonedDateTime
import com.jocmp.newsblurclient.Feed as NewsBlurFeed

internal class NewsBlurAccountDelegate(
    private val database: Database,
    private val newsBlur: NewsBlur,
    private val preferences: AccountPreferences,
) : AccountDelegate {
    private val articleRecords = ArticleRecords(database)
    private val feedRecords = FeedRecords(database)
    private val taggingRecords = TaggingRecords(database)

    override suspend fun refresh(filter: ArticleFilter, cutoffDate: ZonedDateTime?): Result<Unit> {
        return withErrorHandling {
            refreshFeeds()
            refreshArticleState()
            fetchMissingArticles(cutoffDate = cutoffDate)
            preferences.touchLastRefreshedAt()
        }
    }

    override suspend fun markRead(articleIDs: List<String>): Result<Unit> {
        return sendStatus(articleIDs, newsBlur::markStoryHashesAsRead)
    }

    override suspend fun markUnread(articleIDs: List<String>): Result<Unit> {
        return sendStatus(articleIDs, newsBlur::markStoryHashAsUnread)
    }

    override suspend fun addStar(articleIDs: List<String>): Result<Unit> {
        return sendStatus(articleIDs, newsBlur::markStoryHashAsStarred)
    }

    override suspend fun removeStar(articleIDs: List<String>): Result<Unit> {
        return sendStatus(articleIDs, newsBlur::markStoryHashAsUnstarred)
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

    override suspend fun createPage(url: String) =
        Result.failure<Unit>(UnsupportedOperationException("Pages not supported"))

    override suspend fun addFeed(
        url: String,
        title: String?,
        folderTitles: List<String>?
    ): AddFeedResult {
        return try {
            val folders = folderTitles.orEmpty().filter { it.isNotBlank() }
            val primaryFolder = folders.firstOrNull().orEmpty()

            addFolders(folders)

            val response = newsBlur.addURL(url = url, folder = primaryFolder).orThrowAuthorized()

            val feed = response.body()?.feed ?: return AddFeedResult.feedNotFound()

            if (folders.size > 1) {
                newsBlur.moveFeedToFolders(
                    feedID = feed.id,
                    inFolders = listOf(primaryFolder),
                    toFolders = folders,
                ).orThrowAuthorized()
            }

            if (!title.isNullOrBlank() && title != feed.feed_title) {
                newsBlur.renameFeed(feedID = feed.id, title = title).orThrowAuthorized()
            }

            database.transactionWithErrorHandling {
                upsertFeed(feed, title = title)
                folders.forEach { folder ->
                    upsertTagging(feedID = feed.id, folder = folder)
                }
            }

            val localFeed = feedRecords.find(feed.id.toString()) ?: return AddFeedResult.saveFailure()

            coroutineScope {
                launch {
                    fetchNewFeedStories(feedID = feed.id)
                }
            }

            AddFeedResult.Success(localFeed)
        } catch (e: IOException) {
            AddFeedResult.networkError()
        } catch (e: UnauthorizedError) {
            AddFeedResult.connectionError()
        } catch (e: Exception) {
            CapyLog.error("newsblur_add_feed", e)
            AddFeedResult.feedNotFound()
        }
    }

    override suspend fun updateFeed(
        feed: Feed,
        title: String,
        folderTitles: List<String>,
    ): Result<Feed> = withErrorHandling {
        val feedID = feed.id.toLong()

        if (title != feed.title) {
            newsBlur.renameFeed(feedID = feedID, title = title).orThrowAuthorized()
        }

        val currentFolders = serverFolders(feedID)
        val nextFolders = folderTitles.ifEmpty { listOf(TOP_LEVEL) }

        if (currentFolders.toSet() != nextFolders.toSet()) {
            addFolders(folderTitles)

            newsBlur.moveFeedToFolders(
                feedID = feedID,
                inFolders = currentFolders,
                toFolders = nextFolders,
            ).orThrowAuthorized()
        }

        database.transactionWithErrorHandling {
            feedRecords.update(feedID = feed.id, title = title)

            folderTitles.forEach { folder ->
                upsertTagging(feedID = feedID, folder = folder)
            }

            taggingRecords.findFeedTaggingsToDelete(
                feed = feed,
                excludedTaggingNames = folderTitles
            ).forEach { taggingID ->
                taggingRecords.deleteTagging(taggingID = taggingID)
            }
        }

        feedRecords.find(feed.id)
    }

    override suspend fun updateFolder(oldTitle: String, newTitle: String): Result<Unit> =
        withErrorHandling {
            newsBlur.renameFolder(folder = oldTitle, newFolderName = newTitle).orThrowAuthorized()
            Unit
        }

    override suspend fun removeFeed(feed: Feed): Result<Unit> = withErrorHandling {
        val feedID = feed.id.toLong()
        val placements = serverFolders(feedID).ifEmpty { listOf(TOP_LEVEL) }

        placements.forEach { folder ->
            newsBlur.deleteFeed(feedID = feedID, inFolder = folder).orThrowAuthorized()
        }

        Unit
    }

    override suspend fun removeFolder(folderTitle: String): Result<Unit> = withErrorHandling {
        val folders = fetchFeeds().flat_folders
        val removed = folders.filterKeys { it == folderTitle || it.startsWith("$folderTitle$NESTED_SEPARATOR") }
        val kept = folders - removed.keys

        removed.forEach { (folder, feedIDs) ->
            feedIDs.forEach { feedID ->
                val inOtherFolder = kept.values.any { it.contains(feedID) }

                val toFolders = if (inOtherFolder) {
                    emptyList()
                } else {
                    listOf(TOP_LEVEL)
                }

                newsBlur.moveFeedToFolders(
                    feedID = feedID,
                    inFolders = listOf(folder.substringAfterLast(NESTED_SEPARATOR)),
                    toFolders = toFolders,
                ).orThrowAuthorized()
            }
        }

        newsBlur.deleteFolder(folder = folderTitle).orThrowAuthorized()

        Unit
    }

    private suspend fun refreshFeeds() {
        val response = fetchFeeds()
        val feeds = response.feeds.values

        database.transactionWithErrorHandling {
            feeds.forEach { feed ->
                upsertFeed(feed)
            }

            val taggingIDs = response.flat_folders
                .filterKeys { it.isNotBlank() }
                .flatMap { (folder, feedIDs) ->
                    feedIDs.map { feedID ->
                        upsertTagging(feedID = feedID, folder = folder)
                        taggingID(feedID = feedID, folder = folder)
                    }
                }

            database.feedsQueries.deleteAllExcept(feeds.map { it.id.toString() })
            taggingRecords.deleteOrphaned(excludedIDs = taggingIDs)
        }
    }

    private suspend fun refreshArticleState() {
        val unreadBody = newsBlur.unreadStoryHashes().orThrowAuthorized().body()

        if (unreadBody?.authenticated != true) {
            throw UnauthorizedError()
        }

        articleRecords.markAllUnread(
            articleIDs = unreadBody.unread_feed_story_hashes.values.flatten()
        )

        val starredBody = newsBlur.starredStoryHashes().orThrowAuthorized().body()

        if (starredBody?.authenticated != true) {
            throw UnauthorizedError()
        }

        articleRecords.markAllStarred(articleIDs = starredBody.starred_story_hashes)
    }

    private suspend fun fetchNewFeedStories(feedID: Long) {
        try {
            fetchFeedStories(feedID = feedID, page = 1)
        } catch (e: Exception) {
            CapyLog.warn("newsblur_fetch_new_feed", mapOf("error" to e.message))
        } catch (e: UnauthorizedError) {
            CapyLog.warn("newsblur_fetch_new_feed", mapOf("error" to "unauthorized"))
        }
    }

    private suspend fun fetchFeedStories(feedID: Long, page: Int) {
        if (page > MAX_NEW_FEED_PAGES) {
            return
        }

        val stories = newsBlur.feedStories(feedID = feedID, page = page)
            .orThrowAuthorized()
            .body()
            ?.stories
            .orEmpty()

        if (stories.isEmpty()) {
            return
        }

        saveStories(stories, cutoffDate = null)

        fetchFeedStories(feedID = feedID, page = page + 1)
    }

    private suspend fun fetchMissingArticles(cutoffDate: ZonedDateTime?) {
        val ids = articleRecords.findMissingArticles()

        if (ids.isEmpty()) {
            return
        }

        val semaphore = Semaphore(MAX_CONCURRENT_FETCHES)

        coroutineScope {
            ids.chunked(MAX_STORY_HASHES).forEach { chunk ->
                launch {
                    semaphore.withPermit {
                        val response = newsBlur.riverStories(storyHashes = chunk).orThrowAuthorized()

                        val stories = response.body()?.stories ?: return@withPermit

                        saveStories(stories, cutoffDate = cutoffDate)
                    }
                }
            }
        }
    }

    private fun saveStories(stories: List<Story>, cutoffDate: ZonedDateTime?) {
        val summaries = stories.associate { story ->
            story.story_hash to ContentFormatter.summary(story.story_content)
        }

        database.transactionWithErrorHandling {
            stories.forEach { story ->
                val updated = TimeHelpers.nowUTC()
                val publishedAt = story.story_timestamp.toLongOrNull() ?: updated.toEpochSecond()
                val read = story.read_status == 1

                val skip = willAutoDelete(
                    publishedAt = publishedAt,
                    read = read,
                    starred = story.starred,
                    cutoffDate = cutoffDate,
                )

                if (skip) {
                    return@forEach
                }

                database.articlesQueries.create(
                    id = story.story_hash,
                    feed_id = story.story_feed_id.toString(),
                    title = story.story_title.stripTitleMarkup(),
                    author = story.story_authors?.ifBlank { null },
                    content_html = story.story_content,
                    extracted_content_url = null,
                    url = story.story_permalink,
                    summary = summaries[story.story_hash],
                    image_url = story.image_urls.firstOrNull(),
                    published_at = publishedAt,
                    enclosure_type = null,
                )

                articleRecords.updateStatus(
                    articleID = story.story_hash,
                    updatedAt = updated,
                    read = read,
                    starred = story.starred,
                )
            }
        }
    }

    private suspend fun sendStatus(
        articleIDs: List<String>,
        send: suspend (storyHashes: List<String>) -> Response<*>,
    ): Result<Unit> = withErrorHandling {
        articleIDs.chunked(MAX_STATUS_HASHES).forEach { chunk ->
            send(chunk).orThrowAuthorized()
        }
    }

    private suspend fun fetchFeeds(): FeedsResponse {
        return newsBlur.feeds().orThrowAuthorized().body()
            ?: throw IOException("Missing feeds response")
    }

    private suspend fun serverFolders(feedID: Long): List<String> {
        return fetchFeeds().flat_folders
            .filterValues { it.contains(feedID) }
            .keys
            .map { it.ifBlank { TOP_LEVEL } }
    }

    private suspend fun addFolders(folders: List<String>) {
        folders.forEach { folder ->
            newsBlur.addFolder(folder = folder).orThrowAuthorized()
        }
    }

    private fun upsertFeed(feed: NewsBlurFeed, title: String? = null) {
        database.feedsQueries.upsert(
            id = feed.id.toString(),
            subscription_id = feed.id.toString(),
            title = title?.ifBlank { null } ?: feed.feed_title,
            feed_url = feed.feed_address,
            site_url = feed.feed_link,
            favicon_url = feed.favicon_url,
            priority = null,
            itunes_image_url = null,
            read_later = false,
        )
    }

    private fun upsertTagging(feedID: Long, folder: String) {
        taggingRecords.upsert(
            id = taggingID(feedID = feedID, folder = folder),
            feedID = feedID.toString(),
            name = folder,
        )
    }

    private fun taggingID(feedID: Long, folder: String) = "$feedID-$folder"

    private fun <T> Response<T>.orThrowAuthorized(): Response<T> {
        if (code() == 401 || code() == 403) {
            throw UnauthorizedError()
        }

        return orThrow()
    }

    companion object {
        private const val TOP_LEVEL = ""
        private const val NESTED_SEPARATOR = " - "
        private const val MAX_STORY_HASHES = 100
        private const val MAX_NEW_FEED_PAGES = 5
        private const val MAX_STATUS_HASHES = 50
        private const val MAX_CONCURRENT_FETCHES = 4
    }
}
