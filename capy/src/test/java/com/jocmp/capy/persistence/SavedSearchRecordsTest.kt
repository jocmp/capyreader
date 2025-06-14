package com.jocmp.capy.persistence

import com.jocmp.capy.InMemoryDatabaseProvider
import com.jocmp.capy.db.Database
import com.jocmp.capy.fixtures.ArticleFixture
import com.jocmp.capy.fixtures.SavedSearchFixture
import com.jocmp.capy.repeated
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SavedSearchRecordsTest {
    private lateinit var database: Database
    private lateinit var savedSearchRecords: SavedSearchRecords
    private lateinit var savedSearchFixture: SavedSearchFixture

    suspend fun allRecords() = savedSearchRecords.all().first()

    @Before
    fun setup() {
        database = InMemoryDatabaseProvider.build("777")
        savedSearchRecords = SavedSearchRecords(database)
        savedSearchFixture = SavedSearchFixture(database)
    }

    @Test
    fun all() = runTest {
        val searches = 3.repeated { savedSearchFixture.create() }
        val ids = searches.sortedBy { it.id }.map { it.id }

        val actualIDs = savedSearchRecords.all().first().map { it.id }

        assertEquals(expected = ids, actual = actualIDs)
    }

    @Test
    fun find() = runTest {
        val search = savedSearchFixture.create()

        val result = savedSearchRecords.find(savedSearchID = search.id)!!

        assertEquals(expected = search.id, actual = result.id)
    }

    @Test
    fun find_withInvalidID() {
        val result = savedSearchRecords.find(savedSearchID = "bogus")

        assertNull(result)
    }

    @Test
    fun deleteOrphaned() = runTest {
        val searches = 3.repeated { savedSearchFixture.create() }
        val keep = searches.last()

        assertContentEquals(
            actual = allRecords().map { it.id }.sorted(),
            expected = searches.map { it.id }.sorted()
        )

        savedSearchRecords.deleteOrphaned(excludedIDs = listOf(keep.id))

        assertContentEquals(actual = allRecords().map { it.id }, expected = listOf(keep.id))
    }

    @Test
    fun removeArticleBySavedSearchIDs() = runTest {
        val oldSearch = savedSearchFixture.create()
        val latestSearch = savedSearchFixture.create()
        val article = ArticleFixture(database).create()

        savedSearchRecords.upsertArticle(articleID = article.id, savedSearchID = oldSearch.id)
        savedSearchRecords.upsertArticle(articleID = article.id, savedSearchID = latestSearch.id)

        var oldSearchRecords =
            database.saved_searchesQueries.articlesBySavedSearchID(oldSearch.id).executeAsList()

        var latestSearchRecords =
            database.saved_searchesQueries.articlesBySavedSearchID(latestSearch.id).executeAsList()

        assertEquals(expected = 1, oldSearchRecords.size)
        assertEquals(expected = 1, latestSearchRecords.size)

        savedSearchRecords.removeArticleBySavedSearchIDs(articleID = article.id, excludedIDs = listOf(latestSearch.id))

        oldSearchRecords =
            database.saved_searchesQueries.articlesBySavedSearchID(oldSearch.id).executeAsList()

        latestSearchRecords =
            database.saved_searchesQueries.articlesBySavedSearchID(latestSearch.id).executeAsList()

        assertEquals(expected = 0, oldSearchRecords.size)
        assertEquals(expected = 1, latestSearchRecords.size)
    }

    @Test
    fun deleteOrphanedEntries() = runTest {
        val search = savedSearchFixture.create()
        val articleIDs = 3.repeated { ArticleFixture(database).create() }.map { it.id }
        val keepArticleID = articleIDs.last()

        articleIDs.forEach { articleID ->
            savedSearchRecords.upsertArticle(articleID = articleID, savedSearchID = search.id)
        }

        val allArticleIDs =
            database.saved_searchesQueries.articlesBySavedSearchID(search.id).executeAsList()

        assertEquals(actual = allArticleIDs.sorted(), expected = articleIDs.sorted())

        savedSearchRecords.deleteOrphanedEntries(
            savedSearchID = search.id,
            excludedIDs = listOf(keepArticleID)
        )

        val remainingArticleIDs =
            database.saved_searchesQueries.articlesBySavedSearchID(search.id).executeAsList()

        assertEquals(actual = remainingArticleIDs.sorted(), expected = listOf(keepArticleID))
    }
}
