package com.jocmp.capy.persistence

import com.jocmp.capy.InMemoryDatabaseProvider
import com.jocmp.capy.db.Database
import com.jocmp.capy.db.FoldersQueries
import com.jocmp.capy.fixtures.FeedFixture
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.BeforeTest
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class TaggingRecordsTest {
    private lateinit var database: Database
    private lateinit var feedRecords: FeedRecords
    private lateinit var taggingRecords: TaggingRecords
    private lateinit var feedFixture: FeedFixture
    private lateinit var folders: FoldersQueries

    @BeforeTest
    fun setup() {
        database = InMemoryDatabaseProvider.build("777")
        feedRecords = FeedRecords(database)
        taggingRecords = TaggingRecords(database)
        feedFixture = FeedFixture(database)
        folders = database.foldersQueries
    }

    @Test
    fun deleteOrphaned() = runTest {
        val allFolders = listOf("Tech", "News", "Tech/Culture")
        val techFeed = feedFixture.create(folderNames = listOf("Tech", "Tech/Culture"))
        val newsFeed = feedFixture.create(folderNames = allFolders)

        allFolders.forEach {
            folders.upsert(it, expanded = false)
        }

        var names = folders.all().executeAsList().map { it.name }

        assertContentEquals(expected = listOf("Tech", "News", "Tech/Culture"), actual = names)

        taggingRecords.deleteOrphaned(
            excludedIDs = listOf(
                "${techFeed.id}:Tech",
                "${techFeed.id}:Tech/Culture",
                "${newsFeed.id}:Tech",
            )
        )

        names = folders.all().executeAsList().map { it.name }
        assertContentEquals(expected = listOf("Tech", "Tech/Culture"), actual = names)
    }

    @Test
    fun deleteTagging() {
        val allFolders = listOf("Tech", "News", "Tech/Culture")
        feedFixture.create(folderNames = listOf("Tech", "Tech/Culture"))
        val newsFeed = feedFixture.create(folderNames = allFolders)

        allFolders.forEach {
            folders.upsert(it, expanded = false)
        }

        var names = folders.all().executeAsList().map { it.name }

        assertContentEquals(expected = listOf("Tech", "News", "Tech/Culture"), actual = names)

        taggingRecords.deleteTaggings(
            taggingIDs = listOf(
                "${newsFeed.id}:Tech/Culture",
                "${newsFeed.id}:News",
            )
        )

        names = folders.all().executeAsList().map { it.name }
        assertContentEquals(expected = listOf("Tech", "Tech/Culture"), actual = names)
    }

    @Test
    fun updateTitle() = runTest {
        val title = "Tech/Culture"
        val feed = feedFixture.create(folderNames = listOf("Tech"))

        taggingRecords.updateTitle(previousTitle = "Tech", title)

        val refreshedFeed = feedRecords.taggedFeeds().first().find { it.id == feed.id }!!
        assertEquals(expected = title, refreshedFeed.folderName)
    }

    @Test
    fun updateTitle_onConflictingTitle() = runTest {
        val title = "Tech"
        feedFixture.create(folderNames = listOf(title))
        folders.upsert(title, expanded = true)
        val feed = feedFixture.create(folderNames = listOf("Tech!!"))

        taggingRecords.updateTitle(previousTitle = "Tech!!", title)

        val refreshedFeed = feedRecords.taggedFeeds().first().find { it.id == feed.id }!!

        assertEquals(expected = title, actual = refreshedFeed.folderName)
        assertEquals(expected = true, actual = database.foldersQueries.find(title).executeAsOne())
    }
}
