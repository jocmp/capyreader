package com.jocmp.capy

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOne
import com.jocmp.capy.ArticleStatus.UNREAD
import com.jocmp.capy.accounts.AddFeedResult
import com.jocmp.capy.accounts.AutoDelete
import com.jocmp.capy.accounts.FaviconFetcher
import com.jocmp.capy.accounts.LocalOkHttpClient
import com.jocmp.capy.accounts.Source
import com.jocmp.capy.accounts.asOPML
import com.jocmp.capy.accounts.feedbin.FeedbinAccountDelegate
import com.jocmp.capy.accounts.feedbin.FeedbinOkHttpClient
import com.jocmp.capy.accounts.forAccount
import com.jocmp.capy.accounts.local.LocalAccountDelegate
import com.jocmp.capy.accounts.miniflux.MinifluxAccountDelegate
import com.jocmp.capy.accounts.reader.buildReaderDelegate
import com.jocmp.capy.articles.ArticleContent
import com.jocmp.capy.articles.SortOrder
import com.jocmp.capy.common.TimeHelpers.nowUTC
import com.jocmp.capy.common.sortedByName
import com.jocmp.capy.common.sortedByTitle
import com.jocmp.capy.common.transactionWithErrorHandling
import com.jocmp.capy.common.withIOContext
import com.jocmp.capy.db.Database
import com.jocmp.capy.logging.CapyLog
import com.jocmp.capy.opml.ImportProgress
import com.jocmp.capy.opml.OPMLImporter
import com.jocmp.capy.persistence.ArticleRecords
import com.jocmp.capy.persistence.EnclosureRecords
import com.jocmp.capy.persistence.FeedRecords
import com.jocmp.capy.persistence.FolderRecords
import com.jocmp.capy.persistence.SavedSearchRecords
import com.jocmp.capy.persistence.TaggingRecords
import com.jocmp.feedbinclient.Feedbin
import kotlinx.coroutines.Dispatchers
import com.jocmp.minifluxclient.Miniflux
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
    private val clientCertManager: ClientCertManager,
    private val localHttpClient: OkHttpClient = LocalOkHttpClient.forAccount(path = cacheDirectory),
    val delegate: AccountDelegate = when (source) {
        Source.LOCAL -> LocalAccountDelegate(
            database = database,
            httpClient = localHttpClient,
            faviconFetcher = faviconFetcher,
            preferences = preferences,
        )

        Source.FEEDBIN -> FeedbinAccountDelegate(
            database = database,
            feedbin = Feedbin.forAccount(
                path = cacheDirectory,
                preferences = preferences
            )
        )

        Source.MINIFLUX -> MinifluxAccountDelegate(
            database = database,
            miniflux = Miniflux.forAccount(
                path = cacheDirectory,
                preferences = preferences
            )
        )

        Source.FRESHRSS,
        Source.READER -> buildReaderDelegate(
            source = source,
            database = database,
            path = cacheDirectory,
            preferences = preferences,
            clientCertManager = clientCertManager,
        )
    }
) {
    internal val articleRecords = ArticleRecords(database)
    private val enclosureRecords = EnclosureRecords(database)
    private val feedRecords = FeedRecords(database)
    private val folderRecords = FolderRecords(database)
    private val taggingRecords = TaggingRecords(database)
    private val savedSearchRecords = SavedSearchRecords(database)

    private val articleContent = ArticleContent(localHttpClient)

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
        all.filter { it.folderName.isBlank() }.sortedByTitle()
    }

    val folders: Flow<List<Folder>> = taggedFeeds.map { ungrouped ->
        ungrouped.filter { it.folderName.isNotBlank() }.groupBy { it.folderName }.map {
            Folder(
                title = it.key,
                feeds = it.value.sortedByTitle(),
                expanded = it.value.firstOrNull()?.folderExpanded ?: false
            )
        }.sortedByTitle()
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

    suspend fun editFolder(form: EditFolderFormEntry): Result<Folder> {
        delegate.updateFolder(
            oldTitle = form.previousTitle,
            newTitle = form.folderTitle
        ).fold(
            onSuccess = {
                database.transactionWithErrorHandling {
                    taggingRecords.updateTitle(
                        previousTitle = form.previousTitle,
                        title = form.folderTitle
                    )
                }

                return findFolder(form.folderTitle)?.let { Result.success(it) }
                    ?: missingFolderError()
            },
            onFailure = {
                return Result.failure(it)
            })
    }

    suspend fun removeFeed(feedID: String): Result<Unit> {
        val feed = feedRecords.find(feedID) ?: return Result.failure(Throwable("Feed not found"))

        return delegate.removeFeed(feed = feed)
            .fold(
                onSuccess = {
                    feedRecords.removeFeed(feedID = feed.id)
                    Result.success(Unit)
                },
                onFailure = {
                    Result.failure(it)
                })
    }

    suspend fun removeFolder(folderTitle: String): Result<Unit> {
        return delegate.removeFolder(folderTitle).fold(
            onSuccess = {
                taggingRecords.deleteByFolderName(folderTitle)
                Result.success(Unit)
            },
            onFailure = {
                Result.failure(it)
            })
    }

    suspend fun refresh(filter: ArticleFilter = ArticleFilter.default()): Result<Unit> {
        return try {
            val cutoffDate = preferences.autoDelete.get().cutoffDate()

            val result = delegate.refresh(filter, cutoffDate = cutoffDate)

            if (cutoffDate != null) {
                articleRecords.deleteOldArticles(before = cutoffDate)

                articleRecords.deleteOrphanedStatuses()
            }

            result
        } catch (e: Throwable) {
            CapyLog.error("refresh", e)
            Result.failure(e)
        }
    }

    suspend fun findFeed(feedID: String): Feed? {
        return feedRecords.find(feedID)
    }

    suspend fun findSavedSearch(savedSearchID: String): SavedSearch? {
        return savedSearchRecords.find(savedSearchID)
    }

    suspend fun findFolder(title: String): Folder? {
        return feedRecords.findFolder(title = title)
    }

    suspend fun findArticle(articleID: String): Article? {
        if (articleID.isBlank()) {
            return null
        }

        val enclosures = enclosureRecords.byArticle(articleID)
        return articleRecords.find(articleID = articleID)?.copy(enclosures = enclosures)
    }

    suspend fun addStar(articleID: String): Result<Unit> {
        articleRecords.addStar(articleID = articleID)

        return delegate.addStar(listOf(articleID))
    }

    suspend fun removeStar(articleID: String): Result<Unit> {
        articleRecords.removeStar(articleID = articleID)

        return delegate.removeStar(listOf(articleID))
    }

    suspend fun addSavedSearch(articleID: String, savedSearchID: String): Result<Unit> {
        return delegate.addSavedSearch(articleID, savedSearchID)
    }

    suspend fun removeSavedSearch(articleID: String, savedSearchID: String): Result<Unit> {
        return delegate.removeSavedSearch(articleID, savedSearchID)
    }

    suspend fun createSavedSearch(name: String): Result<String> {
        return delegate.createSavedSearch(name)
    }

    fun getArticleSavedSearches(articleID: String): Flow<List<String>> {
        return savedSearchRecords.savedSearchIDsByArticle(articleID)
    }

    fun unreadArticleIDs(
        filter: ArticleFilter,
        range: MarkRead,
        sortOrder: SortOrder,
        query: String?,
    ): List<String> {
        return articleRecords.unreadArticleIDs(
            filter = filter,
            range = range,
            sortOrder = sortOrder,
            query = query,
        )
    }

    fun countUnread(
        filter: ArticleFilter,
        query: String?,
    ): Flow<Long> {
        return articleRecords.countUnread(
            filter = filter.withStatus(UNREAD),
            query = query,
        )
    }

    suspend fun markAllRead(articleIDs: List<String>, batchSize: Int = 500): Result<Unit> {
        val result = withIOContext {
            articleIDs.chunked(batchSize).map { batchIDs ->
                async {
                    val changesetIDs = articleRecords.filterUnreadStatuses(batchIDs)

                    articleRecords.markAllRead(changesetIDs)

                    delegate.markRead(changesetIDs)
                }
            }.awaitAll()
        }

        if (result.all { it.isSuccess }) {
            return Result.success(Unit)
        } else {
            val failure =
                result.firstNotNullOfOrNull { it.exceptionOrNull() } ?: Throwable("Unknown error")

            return Result.failure(failure)
        }
    }

    suspend fun markRead(articleID: String): Result<Unit> {
        return markAllRead(listOf(articleID))
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

    suspend fun dismissStaleNotifications() {
        articleRecords.dismissStaleNotifications()
    }

    suspend fun countActiveNotifications(): Long {
        return articleRecords.countActiveNotifications()
    }

    fun countAll(status: ArticleStatus) =
        articleRecords.countAll(status)

    fun countAllBySavedSearch(status: ArticleStatus) =
        articleRecords.countAllBySavedSearch(status)

    fun countAllByStatus(status: ArticleStatus): Flow<Long> {
        return articleRecords.byStatus.count(status).asFlow().mapToOne(Dispatchers.IO)
    }

    suspend fun dismissNotifications(ids: List<String>) {
        articleRecords.dismissNotifications(ids)
    }

    suspend fun import(
        inputStream: InputStream,
        onProgress: (ImportProgress) -> Unit
    ) {
        OPMLImporter(this).import(onProgress, inputStream)
    }

    suspend fun isFullContentEnabled(feedID: String): Boolean {
        return feedRecords.isFullContentEnabled(feedID = feedID)
    }

    suspend fun enableStickyContent(feedID: String) {
        feedRecords.updateStickyFullContent(enabled = true, feedID = feedID)
    }

    suspend fun toggleNotifications(feedID: String, enabled: Boolean) {
        feedRecords.enableNotifications(enabled = enabled, feedID = feedID)
    }

    suspend fun toggleAllFeedNotifications(enabled: Boolean) {
        feedRecords.toggleAllNotifications(enabled)
    }

    suspend fun expandFolder(folderName: String, expanded: Boolean) {
        folderRecords.expand(folderName, expanded)
    }

    suspend fun updateOpenInBrowser(feedID: String, enabled: Boolean) {
        feedRecords.updateOpenInBrowser(feedID, enabled)
    }

    suspend fun disableStickyContent(feedID: String) {
        feedRecords.updateStickyFullContent(enabled = false, feedID = feedID)
    }

    suspend fun clearAllArticles() {
        articleRecords.deleteAllArticles()
    }

    suspend fun clearStickyFullContent() {
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

private fun <T> missingFolderError() = Result.failure<T>(Throwable("Folder not found"))

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
