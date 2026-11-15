package com.capyreader.app.ui.articles

import androidx.paging.PagingSource
import androidx.paging.PagingSource.LoadParams
import androidx.paging.PagingSource.LoadResult
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.jocmp.capy.Article
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.articles.SortOrder
import com.jocmp.capy.db.Database
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.time.OffsetDateTime

@RunWith(RobolectricTestRunner::class)
class ArticlePagerFactoryTest {
    private lateinit var driver: AndroidSqliteDriver
    private lateinit var database: Database

    @Before
    fun setUp() {
        driver = AndroidSqliteDriver(Database.Schema, RuntimeEnvironment.getApplication(), null)
        database = Database(driver)

        driver.execute(null, "INSERT INTO feeds (id, subscription_id, title, feed_url) VALUES ('f', 's', 'Feed', 'https://example.com')", 0)
        (0 until 5).forEach { i ->
            driver.execute(
                null,
                "INSERT INTO articles (id, feed_id, title, published_at) VALUES ('a$i', 'f', 'Article $i', ${1_000 - i})",
                0
            )
            driver.execute(
                null,
                "INSERT INTO article_statuses (article_id, updated_at, read, starred) VALUES ('a$i', 0, 0, 0)",
                0
            )
        }
    }

    @After
    fun tearDown() {
        driver.close()
        stopKoin()
    }

    @Test
    fun `refresh loads a page with placeholder counts`() = runTest {
        val page = source().load(LoadParams.Refresh(key = 0, loadSize = 2, placeholdersEnabled = true))

        assertTrue(page is LoadResult.Page)
        page as LoadResult.Page
        assertEquals(listOf("a0", "a1"), page.data.map { it.id })
        assertNull(page.prevKey)
        assertEquals(2, page.nextKey)
        assertEquals(0, page.itemsBefore)
        assertEquals(3, page.itemsAfter)
    }

    @Test
    fun `append and prepend walk the list`() = runTest {
        val source = source()

        val append = source.load(LoadParams.Append(key = 4, loadSize = 2, placeholdersEnabled = true))
        val prepend = source.load(LoadParams.Prepend(key = 1, loadSize = 2, placeholdersEnabled = true))

        append as LoadResult.Page
        assertEquals(listOf("a4"), append.data.map { it.id })
        assertNull(append.nextKey)

        prepend as LoadResult.Page
        assertEquals(listOf("a0"), prepend.data.map { it.id })
        assertNull(prepend.prevKey)
    }

    @Test
    fun `refresh past the end clamps to the last page`() = runTest {
        val page = source().load(LoadParams.Refresh(key = 40, loadSize = 2, placeholdersEnabled = true))

        page as LoadResult.Page
        assertEquals(listOf("a3", "a4"), page.data.map { it.id })
        assertEquals(0, page.itemsAfter)
    }

    @Test
    fun `unobserved sources ignore status writes`() = runTest {
        val source = source()
        val observing = source(observeChanges = true)
        source.load(LoadParams.Refresh(key = 0, loadSize = 2, placeholdersEnabled = true))
        observing.load(LoadParams.Refresh(key = 0, loadSize = 2, placeholdersEnabled = true))

        database.articlesQueries.markRead(articleIDs = listOf("a0"), read = true, lastReadAt = 1)

        assertFalse(source.invalid)
        assertTrue(observing.invalid)
    }

    private fun source(observeChanges: Boolean = false): PagingSource<Int, Article> {
        return ArticlePagerFactory(database, observeChanges = observeChanges).findArticles(
            filter = ArticleFilter.Articles(articleStatus = ArticleStatus.ALL),
            query = null,
            sortOrder = SortOrder.NEWEST_FIRST,
            since = OffsetDateTime.now(),
        )
    }
}
