package com.capyreader.app.ui.articles

import androidx.paging.PagingSource.LoadParams
import androidx.paging.PagingSource.LoadResult
import com.jocmp.capy.Article
import com.jocmp.capy.persistence.MAX_PAGE_IDS
import java.time.ZonedDateTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SnapshotPagingSourceTest {
    private val ids = (0 until 10).map { "a$it" }

    @Test
    fun `refresh loads from the key with placeholder counts`() = runTest {
        val page = source().load(LoadParams.Refresh(key = 2, loadSize = 3, placeholdersEnabled = true))

        assertTrue(page is LoadResult.Page)
        page as LoadResult.Page
        assertEquals(listOf("a2", "a3", "a4"), page.data.map { it.id })
        assertEquals(2, page.prevKey)
        assertEquals(5, page.nextKey)
        assertEquals(2, page.itemsBefore)
        assertEquals(5, page.itemsAfter)
    }

    @Test
    fun `append and prepend walk the snapshot`() = runTest {
        val source = source()

        val append = source.load(LoadParams.Append(key = 8, loadSize = 5, placeholdersEnabled = true))
        val prepend = source.load(LoadParams.Prepend(key = 2, loadSize = 5, placeholdersEnabled = true))

        assertTrue(append is LoadResult.Page)
        append as LoadResult.Page
        assertEquals(listOf("a8", "a9"), append.data.map { it.id })
        assertNull(append.nextKey)

        assertTrue(prepend is LoadResult.Page)
        prepend as LoadResult.Page
        assertEquals(listOf("a0", "a1"), prepend.data.map { it.id })
        assertNull(prepend.prevKey)
    }

    @Test
    fun `caps each load under the bound parameter limit`() = runTest {
        val many = (0 until MAX_PAGE_IDS * 2).map { "a$it" }
        var requested = 0
        val source = SnapshotPagingSource(
            articleIDs = many,
            loadArticles = { slice -> requested = slice.size; slice.map(::article) },
            onMissing = {},
        )

        source.load(LoadParams.Refresh(key = 0, loadSize = MAX_PAGE_IDS * 2, placeholdersEnabled = true))

        assertEquals(MAX_PAGE_IDS, requested)
    }

    @Test
    fun `missing rows prune the snapshot and invalidate`() = runTest {
        var missing = emptySet<String>()
        val source = SnapshotPagingSource(
            articleIDs = ids,
            loadArticles = { slice -> slice.filterNot { it == "a1" }.map(::article) },
            onMissing = { missing = it },
        )

        val result = source.load(LoadParams.Refresh(key = 0, loadSize = 3, placeholdersEnabled = true))

        assertTrue(result is LoadResult.Invalid)
        assertTrue(missing == setOf("a1"))
    }

    @Test
    fun `reports appended pages but not refreshes`() = runTest {
        val reported = mutableListOf<List<String>>()
        val source = SnapshotPagingSource(
            articleIDs = ids,
            loadArticles = { slice -> slice.map(::article) },
            onMissing = {},
            onPageLoaded = { loadedIDs, _ -> reported.add(loadedIDs) },
        )

        source.load(LoadParams.Refresh(key = 0, loadSize = 3, placeholdersEnabled = true))
        source.load(LoadParams.Append(key = 3, loadSize = 2, placeholdersEnabled = true))

        assertEquals(listOf(listOf("a3", "a4")), reported)
    }

    private fun source() = SnapshotPagingSource(
        articleIDs = ids,
        loadArticles = { slice -> slice.map(::article) },
        onMissing = {},
    )

    private fun article(id: String): Article {
        val now = ZonedDateTime.now()

        return Article(
            id = id,
            feedID = "feed",
            title = id,
            author = null,
            contentHTML = "",
            url = null,
            summary = "",
            imageURL = null,
            updatedAt = now,
            publishedAt = now,
            read = false,
            starred = false,
        )
    }
}
