package com.jocmp.capy.accounts.reader

import com.jocmp.capy.AccountDelegate
import com.jocmp.capy.Article
import com.jocmp.capy.Feed
import com.jocmp.capy.accounts.AddFeedResult
import com.jocmp.capy.common.UnauthorizedError
import com.jocmp.capy.common.transactionWithErrorHandling
import com.jocmp.capy.common.withResult
import com.jocmp.capy.db.Database
import com.jocmp.capy.persistence.ArticleRecords
import com.jocmp.feedbinclient.pagingInfo
import com.jocmp.readerclient.Category
import com.jocmp.readerclient.GoogleReader
import com.jocmp.readerclient.Subscription
import java.io.IOException
import java.time.ZonedDateTime

/**
 * Save Auth Token for later use
 * self.credentials = Credentials(type: .readerAPIKey, username: credentials.username, secret: authString)
 */
internal class ReaderAccountDelegate(
    private val database: Database,
    private val googleReader: GoogleReader,
) : AccountDelegate {
    private val articleRecords = ArticleRecords(database)

    override suspend fun addFeed(
        url: String,
        title: String?,
        folderTitles: List<String>?
    ): AddFeedResult {
        return AddFeedResult.Failure(error = AddFeedResult.AddFeedError.NetworkError())
    }

    override suspend fun addStar(articleIDs: List<String>): Result<Unit> {
        return Result.failure(Throwable(""))
    }

    override suspend fun refresh(cutoffDate: ZonedDateTime?): Result<Unit> {
        return try {
            val since = articleRecords.maxUpdatedAt()

            refreshFeeds()
            refreshArticles(since = since)

            Result.success(Unit)
        } catch (exception: IOException) {
            Result.failure(exception)
        } catch (e: UnauthorizedError) {
            Result.failure(e)
        }
    }

    override suspend fun removeStar(articleIDs: List<String>): Result<Unit> {
        return Result.failure(Throwable(""))
    }

    override suspend fun markRead(articleIDs: List<String>): Result<Unit> {
        return Result.failure(Throwable(""))
    }

    override suspend fun markUnread(articleIDs: List<String>): Result<Unit> {
        return Result.failure(Throwable(""))
    }

    override suspend fun updateFeed(
        feed: Feed,
        title: String,
        folderTitles: List<String>
    ): Result<Feed> {
        return Result.failure(Throwable(""))
    }

    override suspend fun removeFeed(feed: Feed): Result<Unit> {
        return Result.failure(Throwable(""))
    }

    override suspend fun fetchFullContent(article: Article): Result<String> {
        return Result.failure(Throwable(""))
    }

    private suspend fun refreshFeeds() {
        withResult(googleReader.subscriptionList()) { result ->
            database.transactionWithErrorHandling {
                result.subscriptions.forEach { subscription ->
                    upsertFeed(subscription)
                    upsertTaggings(subscription)
                }

                cleanUpTaggings(result.subscriptions)
            }
        }
    }

    private fun upsertTaggings(subscription: Subscription) {
        subscription.categories.forEach { category ->
            database.taggingsQueries.upsert(
                id = taggingID(subscription, category),
                feed_id = subscription.id,
                name = category.label.orEmpty(),
            )
        }
    }

    private fun upsertFeed(subscription: Subscription) {
        database.feedsQueries.upsert(
            id = subscription.id,
            subscription_id = subscription.id,
            title = subscription.title,
            feed_url = subscription.url,
            site_url = subscription.htmlUrl,
            favicon_url = subscription.iconUrl
        )
    }

    private fun cleanUpTaggings(subscriptions: List<Subscription>) {
        val excludedIDs = subscriptions.flatMap {
            it.categories.map { category ->
                taggingID(it, category)
            }
        }

        database.taggingsQueries.deleteOrphanedTags(
            excludedIDs = excludedIDs
        )
    }

    private suspend fun refreshAllArticles(since: String) {
        fetchPaginatedEntries(since = since)
    }

    private suspend fun fetchPaginatedEntries(
        since: String? = null,
        nextPage: Int? = 1,
        ids: List<Long>? = null
    ) {
        nextPage ?: return

        val response = googleReader.items(
            since = since,
            page = nextPage.toString(),
            ids = ids?.joinToString(",")
        )
        val entries = response.body()

        if (entries != null) {
            saveEntries(entries)
        }

        fetchPaginatedEntries(
            since = since,
            nextPage = response.pagingInfo?.nextPage,
            ids = ids
        )
    }

    private fun taggingID(subscription: Subscription, category: Category): String {
        return "${subscription.id}:${category.id}"
    }
}
