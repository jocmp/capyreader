package com.jocmp.capy.persistence

import com.jocmp.capy.Feed
import com.jocmp.capy.db.Database

internal class TaggingRecords(
    private val database: Database
) {
    fun deleteTagging(taggingID: String) {
        deleteTaggings(listOf(taggingID))
    }

    fun deleteTaggings(taggingIDs: List<String>) {
        database.taggingsQueries.deleteTaggings(taggingIDs)
    }

    fun deleteOrphaned(excludedIDs: List<String>) {
        database.taggingsQueries.deleteOrphanedTags(excludedIDs = excludedIDs)
    }

    fun findFeedTaggingsToDelete(
        feed: Feed,
        excludedTaggingNames: List<String> = emptyList()
    ): List<String> {
        return database
            .taggingsQueries
            .findFeedTaggingsToDelete(
                feedID = feed.id,
                excludedNames = excludedTaggingNames
            )
            .executeAsList()
    }

    fun upsert(
        id: String,
        feedID: String,
        name: String,
    ) {
        return database
            .taggingsQueries
            .upsert(
                id = id,
                feed_id = feedID,
                name = name
            )
    }
}
