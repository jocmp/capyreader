package com.jocmp.capy.persistence

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.jocmp.capy.SavedSearch
import com.jocmp.capy.db.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

internal class SavedSearchRecords(private val database: Database) {
    internal fun all(): Flow<List<SavedSearch>> {
        return savedSearchQueries
            .all(mapper = ::mapper)
            .asFlow()
            .mapToList(Dispatchers.IO)
    }

    internal fun allIDs(): List<String> {
        return savedSearchQueries.allIDs().executeAsList()
    }

    internal fun find(savedSearchID: String): SavedSearch? {
        return savedSearchQueries.find(savedSearchID, mapper = ::mapper).executeAsOneOrNull()
    }

    internal fun upsert(
        id: String,
        name: String,
        query: String? = null
    ) {
        savedSearchQueries.upsert(id = id, name = name, query_text = query)
    }

    internal fun upsertArticle(articleID: String, savedSearchID: String) {
        savedSearchQueries.upsertArticle(
            saved_search_id = savedSearchID,
            article_id = articleID
        )
    }

    internal fun deleteOrphaned(excludedIDs: List<String>) {
        savedSearchQueries.deleteOrphaned(excludedIDs = excludedIDs)
    }

    internal fun deleteOrphanedEntries(savedSearchID: String, excludedIDs: List<String>) {
        savedSearchQueries.deleteOrphanedEntries(savedSearchID, excludedIDs = excludedIDs)
    }

    private fun mapper(
        id: String,
        name: String,
        query: String?,
    ) =
        SavedSearch(
            id = id,
            name = name,
            query = query,
        )

    private val savedSearchQueries
        get() = database.saved_searchesQueries
}
