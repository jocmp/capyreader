package com.jocmp.capy.persistence

import androidx.paging.PagingSource
import app.cash.sqldelight.paging3.QueryPagingSource
import com.jocmp.capy.Account
import com.jocmp.capy.Article
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.articles.UnreadSortOrder
import kotlinx.coroutines.Dispatchers
import java.time.OffsetDateTime

class ArticlePagerFactory(account: Account) {
    private val database = account.database
    private val articles = ArticleRecords(database)

    fun findArticles(
        filter: ArticleFilter,
        query: String?,
        unreadSort: UnreadSortOrder,
        since: OffsetDateTime
    ): PagingSource<Int, Article> {
        return when (filter) {
            is ArticleFilter.Articles -> articleSource(filter, query, unreadSort, since)
            is ArticleFilter.Feeds -> feedSource(filter, query, unreadSort, since)
            is ArticleFilter.Folders -> folderSource(filter, query, unreadSort, since)
            is ArticleFilter.SavedSearches -> savedSearchSource(filter, query, unreadSort, since)
        }
    }

    private fun articleSource(
        filter: ArticleFilter.Articles,
        query: String?,
        unreadSort: UnreadSortOrder,
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
                    unreadSort = unreadSort,
                    offset = offset,
                )
            }
        )
    }

    private fun feedSource(
        filter: ArticleFilter.Feeds,
        query: String?,
        unreadSort: UnreadSortOrder,
        since: OffsetDateTime
    ): PagingSource<Int, Article> {
        val feedIDs = listOf(filter.feedID)

        return feedsSource(
            feedIDs = feedIDs,
            filter = filter,
            query = query,
            unreadSort = unreadSort,
            since = since,
        )
    }

    private fun folderSource(
        filter: ArticleFilter.Folders,
        query: String?,
        unreadSort: UnreadSortOrder,
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
            unreadSort = unreadSort,
            since = since,
        )
    }

    private fun feedsSource(
        feedIDs: List<String>,
        query: String?,
        filter: ArticleFilter,
        unreadSort: UnreadSortOrder,
        since: OffsetDateTime
    ): PagingSource<Int, Article> {
        return QueryPagingSource(
            countQuery = articles.byFeed.count(
                feedIDs = feedIDs,
                status = filter.status,
                query = query,
                since = since
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
                    unreadSort = unreadSort,
                    offset = offset,
                )
            }
        )
    }

    private fun savedSearchSource(
        filter: ArticleFilter.SavedSearches,
        query: String?,
        unreadSort: UnreadSortOrder,
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
                    unreadSort = unreadSort,
                    offset = offset,
                )
            }
        )
    }
}
