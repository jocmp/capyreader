package com.jocmp.capy.persistence

import com.jocmp.capy.InMemoryDatabaseProvider
import com.jocmp.capy.db.Database
import com.jocmp.capy.fixtures.SavedSearchFixture
import com.jocmp.capy.repeated
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SavedSearchRecordsTest {
    private lateinit var database: Database
    private lateinit var savedSearchRecords: SavedSearchRecords
    private lateinit var savedSearchFixture: SavedSearchFixture

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
}
