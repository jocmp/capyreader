package com.jocmp.basil.persistence

import androidx.paging.PagingSource
import app.cash.sqldelight.paging3.QueryPagingSource
import com.jocmp.basil.Article
import com.jocmp.basil.ArticleFilter
import com.jocmp.basil.db.Database
import kotlinx.coroutines.Dispatchers

class ArticlePagerFactory(private val database: Database) {
    private val articles = ArticleRecords(database)

    fun find(filter: ArticleFilter): PagingSource<Int, Article> {
        return when (filter) {
            is ArticleFilter.Articles -> articleSource(filter)
            is ArticleFilter.Feeds -> feedSource(filter)
            is ArticleFilter.Folders -> folderSource(filter)
        }
    }

    private fun articleSource(filter: ArticleFilter.Articles): PagingSource<Int, Article> {
        return QueryPagingSource(
            countQuery = articles.byStatus.count(status = filter.status),
            transacter = database.articlesQueries,
            context = Dispatchers.IO,
            queryProvider = { limit, offset ->
                articles.byStatus.all(
                    status = filter.status,
                    limit = limit,
                    offset = offset
                )
            }
        )
    }

    private fun feedSource(filter: ArticleFilter.Feeds): PagingSource<Int, Article> {
        val feedIDs = listOf(filter.feed.id.toLong())

        return feedsSource(feedIDs, filter)
    }

    private fun folderSource(filter: ArticleFilter.Folders): PagingSource<Int, Article> {
        val feedIDs = filter.folder.feeds.mapNotNull { it.id.toLongOrNull() }

        return feedsSource(feedIDs, filter)
    }

    private fun feedsSource(feedIDs: List<Long>, filter: ArticleFilter): PagingSource<Int, Article> {
        return QueryPagingSource(
            countQuery = articles.byFeed.count(feedIDs = feedIDs, status = filter.status),
            transacter = database.articlesQueries,
            context = Dispatchers.IO,
            queryProvider = { limit, offset ->
                articles.byFeed.all(
                    feedIDs = feedIDs,
                    status = filter.status,
                    limit = limit,
                    offset = offset
                )
            }
        )
    }
}
