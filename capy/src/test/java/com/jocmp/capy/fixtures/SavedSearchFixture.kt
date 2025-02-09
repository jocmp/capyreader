package com.jocmp.capy.fixtures

import com.jocmp.capy.SavedSearch
import com.jocmp.capy.db.Database
import com.jocmp.capy.persistence.SavedSearchRecords
import kotlinx.coroutines.runBlocking
import java.security.SecureRandom

internal class SavedSearchFixture(
    database: Database,
    private val records: SavedSearchRecords = SavedSearchRecords(database)
) {
    fun create(
        id: String = randomID(),
        name: String = "My Saved Search",
    ): SavedSearch = runBlocking {
        records.upsert(
            id = id,
            name = name
        )

        records.find(id)!!
    }

    fun createSavedSearchArticle(
        articleID: String,
        id: String = randomID(),
        name: String = "My Saved Search",
    ) {
        create(id, name)

        records.upsertArticle(articleID = articleID, id = id)
    }

    private fun randomID() = SecureRandom.getInstanceStrong().nextInt().toString()
}
