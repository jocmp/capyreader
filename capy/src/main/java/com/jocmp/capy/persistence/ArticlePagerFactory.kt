package com.jocmp.capy.persistence

import androidx.paging.PagingSource
import app.cash.sqldelight.paging3.QueryPagingSource
import com.jocmp.capy.Article
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.db.Database
import kotlinx.coroutines.Dispatchers
import java.time.OffsetDateTime

class ArticlePagerFactory(private val database: Database) {
    private val articles = ArticleRecords(database)

    fun find(
        filter: ArticleFilter,
        query: String?,
        since: OffsetDateTime
    ): PagingSource<Int, Article> {
        return when (filter) {
            is ArticleFilter.Articles -> articleSource(filter, query, since)
            is ArticleFilter.Feeds -> feedSource(filter, since)
            is ArticleFilter.Folders -> folderSource(filter, since)
        }
    }

    private fun articleSource(
        filter: ArticleFilter.Articles,
        query: String?,
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
                    offset = offset,
                )
            }
        )
    }

    private fun feedSource(
        filter: ArticleFilter.Feeds,
        since: OffsetDateTime
    ): PagingSource<Int, Article> {
        val feedIDs = listOf(filter.feedID)

        return feedsSource(feedIDs, filter, since)
    }

    private fun folderSource(
        filter: ArticleFilter.Folders,
        since: OffsetDateTime
    ): PagingSource<Int, Article> {
        val feedIDs = database
            .taggingsQueries
            .findFeedIDs(folderTitle = filter.folderTitle)
            .executeAsList()

        return feedsSource(feedIDs, filter, since)
    }

    private fun feedsSource(
        feedIDs: List<String>,
        filter: ArticleFilter,
        since: OffsetDateTime
    ): PagingSource<Int, Article> {
        return QueryPagingSource(
            countQuery = articles.byFeed.count(
                feedIDs = feedIDs,
                status = filter.status,
                since = since
            ),
            transacter = database.articlesQueries,
            context = Dispatchers.IO,
            queryProvider = { limit, offset ->
                articles.byFeed.all(
                    feedIDs = feedIDs,
                    status = filter.status,
                    limit = limit,
                    offset = offset,
                    since = since
                )
            }
        )
    }
}
