package com.jocmp.capy

import com.jocmp.capy.accounts.AddFeedResult
import com.jocmp.capy.accounts.AutoDelete
import com.jocmp.capy.accounts.FaviconFetcher
import com.jocmp.capy.accounts.LocalOkHttpClient
import com.jocmp.capy.accounts.Source
import com.jocmp.capy.accounts.asOPML
import com.jocmp.capy.accounts.feedbin.FeedbinAccountDelegate
import com.jocmp.capy.accounts.feedbin.FeedbinOkHttpClient
import com.jocmp.capy.accounts.local.LocalAccountDelegate
import com.jocmp.capy.accounts.reader.buildReaderDelegate
import com.jocmp.capy.articles.ArticleContent
import com.jocmp.capy.articles.UnreadSortOrder
import com.jocmp.capy.common.TimeHelpers.nowUTC
import com.jocmp.capy.common.sortedByName
import com.jocmp.capy.common.sortedByTitle
import com.jocmp.capy.db.Database
import com.jocmp.capy.opml.ImportProgress
import com.jocmp.capy.opml.OPMLImporter
import com.jocmp.capy.persistence.ArticleRecords
import com.jocmp.capy.persistence.FeedRecords
import com.jocmp.capy.persistence.SavedSearchRecords
import com.jocmp.feedbinclient.Feedbin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okhttp3.OkHttpClient
import java.io.InputStream
import java.net.URI
import java.time.ZonedDateTime

data class Account(
    val id: String,
    val path: URI,
    val cacheDirectory: URI,
    val database: Database,
    val preferences: AccountPreferences,
    val source: Source = Source.LOCAL,
    val faviconFetcher: FaviconFetcher,
    private val localHttpClient: OkHttpClient = LocalOkHttpClient.forAccount(path = cacheDirectory),
    val delegate: AccountDelegate = when (source) {
        Source.LOCAL -> LocalAccountDelegate(
            database = database,
            httpClient = localHttpClient,
            faviconFetcher = faviconFetcher,
        )

        Source.FEEDBIN -> FeedbinAccountDelegate(
            database = database,
            feedbin = Feedbin.forAccount(
                path = cacheDirectory,
                preferences = preferences
            )
        )

        Source.FRESHRSS, Source.READER -> buildReaderDelegate(
            source = source,
            database = database,
            path = cacheDirectory,
            preferences = preferences
        )
    }
) {
    internal val articleRecords: ArticleRecords = ArticleRecords(database)
    private val feedRecords: FeedRecords = FeedRecords(database)
    private val savedSearchRecords = SavedSearchRecords(database)

    private val articleContent = ArticleContent(httpClient = localHttpClient)

    val taggedFeeds = feedRecords.taggedFeeds().map {
        it.sortedByTitle()
    }

    val allFeeds = feedRecords.feeds().map {
        it.sortedByTitle()
    }

    val savedSearches = savedSearchRecords.all().map {
        it.sortedByName()
    }

    val feeds: Flow<List<Feed>> = taggedFeeds.map { all ->
        all.filter { it.folderName.isBlank() }
            .sortedByTitle()
    }

    val folders: Flow<List<Folder>> = taggedFeeds.map { ungrouped ->
        ungrouped
            .filter { it.folderName.isNotBlank() }
            .groupBy { it.folderName }
            .map {
                Folder(
                    title = it.key,
                    feeds = it.value.sortedByTitle(),
                )
            }
            .sortedByTitle()
    }

    suspend fun addFeed(
        url: String,
        title: String? = null,
        folderTitles: List<String>? = null
    ): AddFeedResult {
        return delegate.addFeed(
            url = url,
            title = title,
            folderTitles = folderTitles
        )
    }

    suspend fun editFeed(form: EditFeedFormEntry): Result<Feed> {
        val feed = findFeed(form.feedID) ?: return Result.failure(Throwable("Feed not found"))

        return delegate.updateFeed(
            feed = feed,
            title = form.title,
            folderTitles = form.folderTitles
        )
    }

    suspend fun removeFeed(feedID: String): Result<Unit> {
        val feed = feedRecords.find(feedID) ?: return Result.failure(Throwable("Feed not found"))

        return delegate.removeFeed(feed = feed).fold(
            onSuccess = {
                feedRecords.removeFeed(feedID = feed.id)
                Result.success(Unit)
            },
            onFailure = {
                Result.failure(it)
            }
        )
    }

    suspend fun refresh(filter: ArticleFilter = ArticleFilter.default()): Result<Unit> {
        return try {
            val cutoffDate = preferences.autoDelete.get().cutoffDate()

            val result = delegate.refresh(filter, cutoffDate = cutoffDate)

            if (cutoffDate != null) {
                articleRecords.deleteOldArticles(before = cutoffDate)
            }

            result
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    fun findFeed(feedID: String): Feed? {
        return feedRecords.find(feedID)
    }

    fun findSavedSearch(savedSearchID: String): SavedSearch? {
        return savedSearchRecords.find(savedSearchID)
    }

    suspend fun findFolder(title: String): Folder? {
        return feedRecords.findFolder(title = title)
    }

    fun findArticle(articleID: String): Article? {
        if (articleID.isBlank()) {
            return null
        }

        return articleRecords.find(articleID = articleID)
    }

    suspend fun addStar(articleID: String): Result<Unit> {
        articleRecords.addStar(articleID = articleID)

        return delegate.addStar(listOf(articleID))
    }

    suspend fun removeStar(articleID: String): Result<Unit> {
        articleRecords.removeStar(articleID = articleID)

        return delegate.removeStar(listOf(articleID))
    }

    fun unreadArticleIDs(
        filter: ArticleFilter,
        range: MarkRead,
        unreadSort: UnreadSortOrder,
    ): List<String> {
        val flipRange = filter.status == ArticleStatus.UNREAD &&
                unreadSort == UnreadSortOrder.OLDEST_FIRST

        val orderedRange = if (flipRange) {
            range.reversed()
        } else {
            range
        }

        return articleRecords.unreadArticleIDs(
            filter = filter,
            range = orderedRange,
        )
    }

    suspend fun markAllRead(articleIDs: List<String>): Result<Unit> {
        articleRecords.markAllRead(articleIDs)

        return delegate.markRead(articleIDs)
    }

    suspend fun markRead(articleID: String): Result<Unit> {
        articleRecords.markAllRead(listOf(articleID))

        return delegate.markRead(listOf(articleID))
    }

    suspend fun markUnread(articleID: String): Result<Unit> {
        articleRecords.markUnread(articleID = articleID)

        return delegate.markUnread(listOf(articleID))
    }

    suspend fun fetchFullContent(article: Article): Result<String> {
        return articleContent.fetch(article.url)
    }

    suspend fun opmlDocument(): String {
        return OPMLFile(this).opmlDocument()
    }

    suspend fun createNotifications(since: ZonedDateTime): List<ArticleNotification> {
        return articleRecords.createNotifications(since = since)
    }

    fun countActiveNotifications(): Long {
        return articleRecords.countActiveNotifications()
    }

    fun dismissNotifications(ids: List<String>) {
        articleRecords.dismissNotifications(ids)
    }

    suspend fun import(inputStream: InputStream, onProgress: (ImportProgress) -> Unit) {
        OPMLImporter(this).import(onProgress, inputStream)
    }

    fun isFullContentEnabled(feedID: String): Boolean {
        return feedRecords.isFullContentEnabled(feedID = feedID)
    }

    fun enableStickyContent(feedID: String) {
        feedRecords.updateStickyFullContent(enabled = true, feedID = feedID)
    }

    fun toggleNotifications(feedID: String, enabled: Boolean) {
        feedRecords.enableNotifications(enabled = enabled, feedID = feedID)
    }

    fun toggleAllFeedNotifications(enabled: Boolean) {
        feedRecords.toggleAllNotifications(enabled)
    }

    fun disableStickyContent(feedID: String) {
        feedRecords.updateStickyFullContent(enabled = false, feedID = feedID)
    }

    fun clearAllArticles() {
        articleRecords.deleteAllArticles()
    }

    fun clearStickyFullContent() {
        feedRecords.clearStickyFullContent()
    }

    val supportsMultiFolderFeeds: Boolean
        get() = source == Source.FEEDBIN || source == Source.LOCAL

    internal suspend fun asOPML(): String {
        var opml = ""

        feeds.first().forEach { feed ->
            opml += feed.asOPML(indentLevel = 2)
        }

        folders.first().forEach { folder ->
            opml += folder.asOPML(indentLevel = 2)
        }

        return opml
    }
}

private fun Feedbin.Companion.forAccount(path: URI, preferences: AccountPreferences) =
    create(client = FeedbinOkHttpClient.forAccount(path, preferences))

private fun AutoDelete.cutoffDate(): ZonedDateTime? {
    val now = nowUTC()

    return when (this) {
        AutoDelete.DISABLED -> null
        AutoDelete.WEEKLY -> now.minusWeeks(1)
        AutoDelete.EVERY_TWO_WEEKS -> now.minusWeeks(2)
        AutoDelete.EVERY_MONTH -> now.minusMonths(1)
        AutoDelete.EVERY_THREE_MONTHS -> now.minusMonths(3)
    }
}
