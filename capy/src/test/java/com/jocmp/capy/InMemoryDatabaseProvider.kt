package com.jocmp.capy

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.jocmp.capy.db.Database
import com.jocmp.capy.persistence.ArticleRecords
import com.jocmp.capy.persistence.FeedRecords

object InMemoryDatabaseProvider : DatabaseProvider {
    override fun build(accountID: String): Database {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        Database.Schema.create(driver)
        return Database(driver)
    }

    override fun delete(accountID: String) {
    }
}

internal fun Database.reload(article: Article) = ArticleRecords(this).reload(article)

internal fun Database.reload(feed: Feed) = FeedRecords(this).reload(feed)

internal fun FeedRecords.reload(feed: Feed) = find(feed.id)!!

internal fun ArticleRecords.reload(article: Article) = findImmediate(article.id)
