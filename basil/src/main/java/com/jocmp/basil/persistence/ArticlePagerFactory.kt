package com.jocmp.basil.persistence

import androidx.paging.PagingSource
import app.cash.sqldelight.paging3.QueryPagingSource
import com.jocmp.basil.Article
import com.jocmp.basil.Filter
import com.jocmp.basil.db.Database
import kotlinx.coroutines.Dispatchers

internal class ArticlePagerFactory(
    private val database: Database,
    private val filter: Filter,
) {
    fun find(): PagingSource<Int, Article> {
        return when (filter) {
            is Filter.Articles -> articleSource(filter)
            is Filter.Feeds -> feedSource(filter)
            is Filter.Folders -> folderSource(filter)
        }
    }

    private fun articleSource(filter: Filter.Articles): PagingSource<Int, Article> {
        return QueryPagingSource(
            countQuery = database.articlesQueries.countByStatus(
                read = listOf(false),
                starred = listOf(false),
            ),
            transacter = database.articlesQueries,
            context = Dispatchers.IO,
            queryProvider = { limit, offset ->
                database.articlesQueries.findByStatus(
                    read = listOf(false),
                    starred = listOf(false),
                    limit = limit,
                    offset = offset,
                    mapper = ::articleMapper
                )
            }
        )
    }

    private fun feedSource(filter: Filter.Feeds): PagingSource<Int, Article> {
        val feedIDs = listOf(filter.feed.id.toLong())

        return feedsSource(feedIDs, filter.status)
    }

    private fun folderSource(filter: Filter.Folders): PagingSource<Int, Article> {
        val feedIDs = filter.folder.feeds.mapNotNull { it.id.toLongOrNull() }

        return feedsSource(feedIDs, filter.status)
    }

    private fun feedsSource(
        feedIDs: List<Long>,
        status: Filter.Status
    ): PagingSource<Int, Article> {
        return QueryPagingSource(
            countQuery = database.articlesQueries.countByFeeds(
                feedIDs = feedIDs,
                read = listOf(false),
                starred = listOf(false),
            ),
            transacter = database.articlesQueries,
            context = Dispatchers.IO,
            queryProvider = { limit, offset ->
                database.articlesQueries.findByFeeds(
                    feedIDs = feedIDs,
                    read = listOf(false),
                    starred = listOf(false),
                    limit = limit,
                    offset = offset,
                    mapper = ::articleMapper
                )
            }
        )
    }
}
