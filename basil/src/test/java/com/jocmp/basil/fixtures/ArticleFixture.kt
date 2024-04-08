package com.jocmp.basil.fixtures

import com.jocmp.basil.Article
import com.jocmp.basil.Feed
import com.jocmp.basil.RandomUUID
import com.jocmp.basil.common.nowUTCInSeconds
import com.jocmp.basil.db.Database
import com.jocmp.basil.persistence.articleMapper

class ArticleFixture(private val database: Database) {
    private val feedFixture = FeedFixture(database)

    fun create(
        id: String = RandomUUID.generate(),
        title: String = "Test Title",
        feed: Feed = feedFixture.create(feedURL = "https://example.com/${RandomUUID.generate()}"),
        publishedAt: Long = nowUTCInSeconds()
    ): Article {
        database.transaction {
            database.articlesQueries.create(
                id = id,
                feed_id = feed.id,
                title = title,
                content_html = "<div>Test</div>",
                image_url = null,
                published_at = publishedAt,
                summary = "Test article here",
                url = "https://example.com/test-article"
            )
            database.articlesQueries.updateStatus(
                article_id = id,
                updated_at = publishedAt
            )
        }

        return database.articlesQueries.findBy(
            articleID = id,
            mapper = ::articleMapper
        ).executeAsOne()
    }
}
