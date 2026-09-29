package com.capyreader.app.testing

import com.jocmp.capy.AccountDelegate
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.Feed
import com.jocmp.capy.accounts.AddFeedResult
import com.jocmp.capy.db.Database
import kotlinx.coroutines.CompletableDeferred
import java.time.ZonedDateTime
import java.util.concurrent.ConcurrentLinkedQueue

data class OfflineArticle(
    val id: String,
    val title: String,
    val publishedAt: ZonedDateTime,
)

class OfflineAccountDelegate(private val database: Database) : AccountDelegate {
    private val pendingRefreshes = ConcurrentLinkedQueue<PendingRefresh>()

    fun seed(articles: List<OfflineArticle>) {
        database.transaction {
            database.feedsQueries.upsert(
                id = FEED_ID,
                subscription_id = FEED_ID,
                title = FEED_TITLE,
                feed_url = FEED_URL,
                site_url = FEED_URL,
                favicon_url = null,
                priority = null,
                itunes_image_url = null,
                read_later = false,
            )

            articles.forEach(::insert)
        }
    }

    fun enqueueRefresh(
        articles: List<OfflineArticle>,
        release: CompletableDeferred<Unit> = CompletableDeferred(Unit),
    ) {
        pendingRefreshes.add(PendingRefresh(articles = articles, release = release))
    }

    fun isRead(articleID: String): Boolean {
        return database.articlesQueries.findStatus(articleID).executeAsOne().read
    }

    override suspend fun refresh(filter: ArticleFilter, cutoffDate: ZonedDateTime?): Result<Unit> {
        val pending = pendingRefreshes.poll() ?: return Result.success(Unit)

        pending.release.await()

        database.transaction {
            pending.articles.forEach(::insert)
        }

        return Result.success(Unit)
    }

    override suspend fun markRead(articleIDs: List<String>) = Result.success(Unit)

    override suspend fun markUnread(articleIDs: List<String>) = Result.success(Unit)

    override suspend fun addStar(articleIDs: List<String>) = Result.success(Unit)

    override suspend fun removeStar(articleIDs: List<String>) = Result.success(Unit)

    override suspend fun addSavedSearch(articleID: String, savedSearchID: String) =
        Result.success(Unit)

    override suspend fun removeSavedSearch(articleID: String, savedSearchID: String) =
        Result.success(Unit)

    override suspend fun createSavedSearch(name: String): Result<String> =
        Result.failure(UnsupportedOperationException())

    override suspend fun createPage(url: String): Result<Unit> =
        Result.failure(UnsupportedOperationException())

    override suspend fun addFeed(
        url: String,
        title: String?,
        folderTitles: List<String>?
    ): AddFeedResult = AddFeedResult.networkError()

    override suspend fun updateFeed(
        feed: Feed,
        title: String,
        folderTitles: List<String>
    ): Result<Feed> = Result.failure(UnsupportedOperationException())

    override suspend fun updateFolder(oldTitle: String, newTitle: String): Result<Unit> =
        Result.failure(UnsupportedOperationException())

    override suspend fun removeFeed(feed: Feed): Result<Unit> =
        Result.failure(UnsupportedOperationException())

    override suspend fun removeFolder(folderTitle: String): Result<Unit> =
        Result.failure(UnsupportedOperationException())

    private fun insert(article: OfflineArticle) {
        database.articlesQueries.create(
            id = article.id,
            feed_id = FEED_ID,
            title = article.title,
            author = null,
            content_html = "<p>${article.title}</p>",
            extracted_content_url = null,
            url = "$FEED_URL/${article.id}",
            summary = article.title,
            image_url = null,
            published_at = article.publishedAt.toEpochSecond(),
            enclosure_type = null,
        )

        database.articlesQueries.createStatus(
            article_id = article.id,
            updated_at = article.publishedAt.toEpochSecond(),
            read = false,
        )
    }

    private data class PendingRefresh(
        val articles: List<OfflineArticle>,
        val release: CompletableDeferred<Unit>,
    )

    companion object {
        const val FEED_ID = "offline-feed"
        const val FEED_TITLE = "Offline Feed"
        const val FEED_URL = "https://offline.invalid"
    }
}
