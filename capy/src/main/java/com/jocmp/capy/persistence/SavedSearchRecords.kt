package com.jocmp.capy.persistence

import com.jocmp.capy.db.Database

internal class SavedSearchRecords(private val database: Database) {
    internal fun upsert(id: String, name: String) {
        savedSearchQueries.upsert(id = id, name = name)
    }

    internal fun deleteOrphaned(excludedIDs: List<String>) {
        savedSearchQueries.deleteOrphaned(excludedIDs = excludedIDs)
    }

    private val savedSearchQueries
        get() = database.saved_searchesQueries
}
