package com.jocmp.capy.fixtures

import com.jocmp.capy.Article
import com.jocmp.capy.Feed
import com.jocmp.capy.RandomUUID
import com.jocmp.capy.common.TimeHelpers
import com.jocmp.capy.db.Database
import com.jocmp.capy.persistence.articleMapper

class ArticleFixture(private val database: Database) {
    private val feedFixture = FeedFixture(database)

    fun create(
        id: String = RandomUUID.generate(),
        title: String = "Test Title",
        summary: String = "Test article here",
        feed: Feed = feedFixture.create(feedURL = "https://example.com/${RandomUUID.generate()}"),
        read: Boolean = true,
        publishedAt: Long = TimeHelpers.nowUTC().toEpochSecond(),
    ): Article {
        database.transaction {
            database.articlesQueries.create(
                id = id,
                feed_id = feed.id,
                title = title,
                author = "John Writer",
                content_html = "<div>Test</div>",
                extracted_content_url = null,
                image_url = null,
                published_at = publishedAt,
                summary = summary,
                url = "https://example.com/test-article",
                enclosure_type = null
            )
            database.articlesQueries.createStatus(
                article_id = id,
                updated_at = publishedAt,
                read = read,
            )
        }

        return database.articlesQueries.findBy(
            articleID = id,
            mapper = ::articleMapper
        ).executeAsOne()
    }
}
