package com.jocmp.basil.persistence

import app.cash.sqldelight.Query
import com.jocmp.basil.Article
import com.jocmp.basil.ArticleStatus
import com.jocmp.basil.db.Database

internal class ArticleRecords(val database: Database) {
    val byStatus = ByStatus(database)
    val byFeed = ByFeed(database)
    val forArticle = ForArticle(database)

    class ForArticle(private val database: Database) {
        fun markRead(articleID: String) {
            database.articlesQueries.markRead(articleID = articleID.toLong())
        }
    }

    class ByFeed(private val database: Database) {
        fun all(
            feedIDs: List<Long>,
            status: ArticleStatus,
            limit: Long,
            offset: Long
        ): Query<Article> {
            val (read, starred) = status.toStatusPair

            return database.articlesQueries.findByFeeds(
                feedIDs = feedIDs,
                read = read,
                starred = starred,
                limit = limit,
                offset = offset,
                mapper = ::articleMapper
            )
        }

        fun count(
            feedIDs: List<Long>,
            status: ArticleStatus
        ): Query<Long> {
            val (read, starred) = status.toStatusPair

            return database.articlesQueries.countByFeeds(
                feedIDs = feedIDs,
                read = read,
                starred = starred,
            )
        }
    }

    class ByStatus(private val database: Database) {
        fun all(
            status: ArticleStatus,
            limit: Long,
            offset: Long
        ): Query<Article> {
            val (read, starred) = status.toStatusPair

            return database.articlesQueries.findByStatus(
                read = read,
                starred = starred,
                limit = limit,
                offset = offset,
                mapper = ::articleMapper
            )
        }

        fun count(status: ArticleStatus): Query<Long> {
            val (read, starred) = status.toStatusPair

            return database.articlesQueries.countByStatus(
                read = read,
                starred = starred,
            )
        }
    }
}
