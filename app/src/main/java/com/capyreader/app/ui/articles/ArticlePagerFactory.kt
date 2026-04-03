package com.capyreader.app.ui.articles

import androidx.paging.PagingSource
import app.cash.sqldelight.paging3.QueryPagingSource
import com.jocmp.capy.Article
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.FeedPriority
import com.jocmp.capy.articles.SortOrder
import com.jocmp.capy.db.Database
import com.jocmp.capy.persistence.ArticleRecords
import kotlinx.coroutines.Dispatchers
import java.time.OffsetDateTime

class ArticlePagerFactory(private val database: Database) {
    private val articles = ArticleRecords(database)

    fun findArticles(
        filter: ArticleFilter,
        query: String?,
        sortOrder: SortOrder,
        since: OffsetDateTime
    ): PagingSource<Long, Article> {
        return when (filter) {
            is ArticleFilter.Articles -> articleSource(filter, query, sortOrder, since)
            is ArticleFilter.Feeds -> feedSource(filter, query, sortOrder, since)
            is ArticleFilter.Folders -> folderSource(filter, query, sortOrder, since)
            is ArticleFilter.SavedSearches -> savedSearchSource(filter, query, sortOrder, since)
            is ArticleFilter.Today -> todaySource(filter, query, sortOrder, since)
        }
    }

    private fun articleSource(
        filter: ArticleFilter.Articles,
        query: String?,
        sortOrder: SortOrder,
        since: OffsetDateTime
    ): PagingSource<Long, Article> {
        return QueryPagingSource(
            transacter = database.articlesQueries,
            context = Dispatchers.IO,
            pageBoundariesProvider = { anchor, limit ->
                articles.byStatus.pageBoundaries(
                    status = filter.status,
                    query = query,
                    since = since,
                    sortOrder = sortOrder,
                    anchor = anchor,
                    limit = limit,
                )
            },
            queryProvider = { beginInclusive, endExclusive ->
                articles.byStatus.keyed(
                    status = filter.status,
                    query = query,
                    since = since,
                    sortOrder = sortOrder,
                    beginInclusive = beginInclusive,
                    endExclusive = endExclusive,
                )
            }
        )
    }

    private fun feedSource(
        filter: ArticleFilter.Feeds,
        query: String?,
        sortOrder: SortOrder,
        since: OffsetDateTime,
    ): PagingSource<Long, Article> {
        val feedIDs = listOf(filter.feedID)

        return feedsSource(
            feedIDs = feedIDs,
            filter = filter,
            query = query,
            sortOrder = sortOrder,
            since = since,
            priority = FeedPriority.FEED,
        )
    }

    private fun folderSource(
        filter: ArticleFilter.Folders,
        query: String?,
        sortOrder: SortOrder,
        since: OffsetDateTime
    ): PagingSource<Long, Article> {
        val feedIDs = database
            .taggingsQueries
            .findFeedIDs(folderTitle = filter.folderTitle)
            .executeAsList()

        return feedsSource(
            feedIDs = feedIDs,
            filter = filter,
            query = query,
            sortOrder = sortOrder,
            since = since,
            priority = FeedPriority.CATEGORY,
        )
    }

    private fun feedsSource(
        feedIDs: List<String>,
        query: String?,
        filter: ArticleFilter,
        sortOrder: SortOrder,
        priority: FeedPriority,
        since: OffsetDateTime
    ): PagingSource<Long, Article> {
        return QueryPagingSource(
            transacter = database.articlesQueries,
            context = Dispatchers.IO,
            pageBoundariesProvider = { anchor, limit ->
                articles.byFeed.pageBoundaries(
                    feedIDs = feedIDs,
                    status = filter.status,
                    query = query,
                    since = since,
                    sortOrder = sortOrder,
                    priority = priority,
                    anchor = anchor,
                    limit = limit,
                )
            },
            queryProvider = { beginInclusive, endExclusive ->
                articles.byFeed.keyed(
                    feedIDs = feedIDs,
                    status = filter.status,
                    query = query,
                    since = since,
                    sortOrder = sortOrder,
                    priority = priority,
                    beginInclusive = beginInclusive,
                    endExclusive = endExclusive,
                )
            }
        )
    }

    private fun savedSearchSource(
        filter: ArticleFilter.SavedSearches,
        query: String?,
        sortOrder: SortOrder,
        since: OffsetDateTime
    ): PagingSource<Long, Article> {
        return QueryPagingSource(
            transacter = database.articlesQueries,
            context = Dispatchers.IO,
            pageBoundariesProvider = { anchor, limit ->
                articles.bySavedSearch.pageBoundaries(
                    savedSearchID = filter.savedSearchID,
                    status = filter.status,
                    query = query,
                    since = since,
                    sortOrder = sortOrder,
                    anchor = anchor,
                    limit = limit,
                )
            },
            queryProvider = { beginInclusive, endExclusive ->
                articles.bySavedSearch.keyed(
                    savedSearchID = filter.savedSearchID,
                    status = filter.status,
                    query = query,
                    since = since,
                    sortOrder = sortOrder,
                    beginInclusive = beginInclusive,
                    endExclusive = endExclusive,
                )
            }
        )
    }

    private fun todaySource(
        filter: ArticleFilter.Today,
        query: String?,
        sortOrder: SortOrder,
        since: OffsetDateTime
    ): PagingSource<Long, Article> {
        return QueryPagingSource(
            transacter = database.articlesQueries,
            context = Dispatchers.IO,
            pageBoundariesProvider = { anchor, limit ->
                articles.byToday.pageBoundaries(
                    status = filter.status,
                    query = query,
                    since = since,
                    sortOrder = sortOrder,
                    anchor = anchor,
                    limit = limit,
                )
            },
            queryProvider = { beginInclusive, endExclusive ->
                articles.byToday.keyed(
                    status = filter.status,
                    query = query,
                    sortOrder = sortOrder,
                    since = since,
                    beginInclusive = beginInclusive,
                    endExclusive = endExclusive,
                )
            }
        )
    }
}
