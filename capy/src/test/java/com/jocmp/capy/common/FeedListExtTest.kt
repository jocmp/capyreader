package com.jocmp.capy.common

import com.jocmp.capy.InMemoryDatabaseProvider
import com.jocmp.capy.db.Database
import com.jocmp.capy.fixtures.FeedFixture
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FeedListExtTest {
    private lateinit var feedFixture: FeedFixture
    private lateinit var database: Database

    @BeforeTest
    fun setUp() {
        database = InMemoryDatabaseProvider.build("777")
        feedFixture = FeedFixture(database)
    }

    @Test
    fun sortedByTitle_sortsInCaseInsensitiveOrder() {
        val feeds = listOf(
            feedFixture.create(title = "Kiwi"),
            feedFixture.create(title = "apple"),
            feedFixture.create(title = "Élderberry"),
            feedFixture.create(title = "Cherry")
        )

        val sortedTitles = feeds.sortedByTitle().map { it.title }
        val expected = listOf(
            "apple",
            "Cherry",
            "Élderberry",
            "Kiwi"
        )

        assertEquals(expected = expected, actual = sortedTitles)
    }
}
