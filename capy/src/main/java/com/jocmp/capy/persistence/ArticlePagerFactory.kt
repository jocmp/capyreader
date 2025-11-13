package com.jocmp.capy.persistence

import androidx.paging.PagingSource
import app.cash.sqldelight.paging3.QueryPagingSource
import com.jocmp.capy.Article
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.FeedPriority
import com.jocmp.capy.articles.SortOrder
import com.jocmp.capy.db.Database
import kotlinx.coroutines.Dispatchers
import java.time.OffsetDateTime

class ArticlePagerFactory(private val database: Database) {
    private val articles = ArticleRecords(database)

    fun findArticles(
        filter: ArticleFilter,
        query: String?,
        sortOrder: SortOrder,
        since: OffsetDateTime
    ): PagingSource<Int, Article> {
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
    ): PagingSource<Int, Article> {
        return QueryPagingSource(
            countQuery = articles.byStatus.count(
                status = filter.status,
                query = query,
                since = since
            ),
            transacter = database.articlesQueries,
            context = Dispatchers.IO,
            queryProvider = { limit, offset ->
                articles.byStatus.all(
                    status = filter.status,
                    query = query,
                    since = since,
                    limit = limit,
                    sortOrder = sortOrder,
                    offset = offset,
                )
            }
        )
    }

    private fun feedSource(
        filter: ArticleFilter.Feeds,
        query: String?,
        sortOrder: SortOrder,
        since: OffsetDateTime,
    ): PagingSource<Int, Article> {
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
    ): PagingSource<Int, Article> {
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
    ): PagingSource<Int, Article> {
        return QueryPagingSource(
            countQuery = articles.byFeed.count(
                feedIDs = feedIDs,
                status = filter.status,
                query = query,
                since = since,
                priority = priority,
            ),
            transacter = database.articlesQueries,
            context = Dispatchers.IO,
            queryProvider = { limit, offset ->
                articles.byFeed.all(
                    feedIDs = feedIDs,
                    status = filter.status,
                    query = query,
                    since = since,
                    limit = limit,
                    sortOrder = sortOrder,
                    offset = offset,
                    priority = priority,
                )
            }
        )
    }

    private fun savedSearchSource(
        filter: ArticleFilter.SavedSearches,
        query: String?,
        sortOrder: SortOrder,
        since: OffsetDateTime
    ): PagingSource<Int, Article> {
        return QueryPagingSource(
            countQuery = articles.bySavedSearch.count(
                savedSearchID = filter.savedSearchID,
                status = filter.status,
                query = query,
                since = since
            ),
            transacter = database.articlesQueries,
            context = Dispatchers.IO,
            queryProvider = { limit, offset ->
                articles.bySavedSearch.all(
                    savedSearchID = filter.savedSearchID,
                    status = filter.status,
                    query = query,
                    since = since,
                    limit = limit,
                    sortOrder = sortOrder,
                    offset = offset,
                )
            }
        )
    }

    private fun todaySource(
        filter: ArticleFilter.Today,
        query: String?,
        sortOrder: SortOrder,
        since: OffsetDateTime
    ): PagingSource<Int, Article> {
        return QueryPagingSource(
            countQuery = articles.byToday.count(
                status = filter.status,
                query = query,
                since = since
            ),
            transacter = database.articlesQueries,
            context = Dispatchers.IO,
            queryProvider = { limit, offset ->
                articles.byToday.all(
                    status = filter.status,
                    query = query,
                    limit = limit,
                    sortOrder = sortOrder,
                    offset = offset,
                    since = since,
                )
            }
        )
    }
}
