package com.jocmp.basil.fixtures

import com.jocmp.basil.Article
import com.jocmp.basil.RandomUUID
import com.jocmp.basil.db.Database
import com.jocmp.basil.persistence.articleMapper
import com.jocmp.basil.shared.nowUTC
import com.jocmp.basil.db.Feeds as DBFeed

class ArticleFixture(private val database: Database) {
    private val feedFixture = FeedFixture(database)

    fun create(
        externalID: String = RandomUUID.generate(),
        title: String = "Test Title",
        feed: DBFeed = feedFixture.create(feedURL = "https://example.com/${RandomUUID.generate()}"),
        publishedAt: Long = nowUTC()
    ): Article {
        database.transaction {
            database.articlesQueries.create(
                feed_id = feed.id,
                title = title,
                content_html = "<div>Test</div>",
                image_url = null,
                published_at = publishedAt,
                external_id = externalID,
                summary = "Test article here",
                url = "https://example.com/test-article"
            )
            database.articlesQueries.updateStatus(
                feed_id = feed.id,
                external_id = externalID,
                arrived_at = publishedAt
            )
        }

        return database.articlesQueries.findByExternalID(
            externalID = externalID,
            feedID = feed.id,
            mapper = ::articleMapper
        ).executeAsOne()
    }
}
