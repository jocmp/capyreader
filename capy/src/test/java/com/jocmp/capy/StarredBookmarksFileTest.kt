package com.jocmp.capy

import com.jocmp.capy.db.Database
import com.jocmp.capy.fixtures.AccountFixture
import com.jocmp.capy.fixtures.ArticleFixture
import com.jocmp.capy.fixtures.FeedFixture
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlin.test.assertEquals

class StarredBookmarksFileTest {
    @JvmField
    @Rule
    val folder = TemporaryFolder()

    private val accountID = "777"
    private lateinit var account: Account
    private lateinit var database: Database
    private lateinit var articleFixture: ArticleFixture
    private lateinit var feedFixture: FeedFixture

    @Before
    fun setup() {
        database = InMemoryDatabaseProvider.build(accountID)
        articleFixture = ArticleFixture(database)
        feedFixture = FeedFixture(database)

        account = AccountFixture.create(
            id = accountID,
            database = database,
            parentFolder = folder,
        )
    }

    @Test
    fun bookmarksDocument_emptyWhenNoneStarred() = runTest {
        val document = StarredBookmarksFile(account).bookmarksDocument()

        val expected = """
            |<!DOCTYPE NETSCAPE-Bookmark-file-1>
            |<!-- This is an automatically generated file. -->
            |<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
            |<TITLE>Starred Articles</TITLE>
            |<H1>Starred Articles</H1>
            |<DL><p>
            |</DL><p>
            |""".trimMargin()

        assertEquals(expected, document)
    }

    @Test
    fun bookmarksDocument_onlyIncludesStarredArticles() = runTest {
        val feed = feedFixture.create(feedURL = "https://example.com/feed")

        articleFixture.create(
            id = "article-1",
            feed = feed,
            title = "First Post",
            url = "https://example.com/first",
            publishedAt = 1700000000,
            starred = true,
        )

        articleFixture.create(
            id = "article-2",
            feed = feed,
            title = "Second Post",
            url = "https://example.com/second",
            publishedAt = 1700001000,
            starred = true,
        )

        articleFixture.create(
            id = "article-3",
            feed = feed,
            title = "Unstarred Post",
            url = "https://example.com/unstarred",
            publishedAt = 1700002000,
            starred = false,
        )

        val document = StarredBookmarksFile(account).bookmarksDocument()

        val expected = """
            |<!DOCTYPE NETSCAPE-Bookmark-file-1>
            |<!-- This is an automatically generated file. -->
            |<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
            |<TITLE>Starred Articles</TITLE>
            |<H1>Starred Articles</H1>
            |<DL><p>
            |    <DT><A HREF="https://example.com/second" ADD_DATE="1700001000">Second Post</A>
            |    <DT><A HREF="https://example.com/first" ADD_DATE="1700000000">First Post</A>
            |</DL><p>
            |""".trimMargin()

        assertEquals(expected, document)
    }

    @Test
    fun bookmarksDocument_escapesHtmlInTitles() = runTest {
        val feed = feedFixture.create(feedURL = "https://example.com/feed")

        articleFixture.create(
            id = "article-html",
            feed = feed,
            title = "R&A <Enterprise> \"Architecture\"",
            url = "https://example.com/r&a?foo=bar",
            publishedAt = 1700000000,
            starred = true,
        )

        val document = StarredBookmarksFile(account).bookmarksDocument()

        assert(document.contains("R&amp;A &lt;Enterprise&gt; &quot;Architecture&quot;"))
        assert(document.contains("HREF=\"https://example.com/r&amp;a?foo=bar\""))
    }
}
