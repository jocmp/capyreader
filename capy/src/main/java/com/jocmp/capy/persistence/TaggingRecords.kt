package com.jocmp.capy.persistence

import app.cash.sqldelight.db.QueryResult
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

    fun deleteByFolderName(folderTitle: String) {
        database.taggingsQueries.deleteByFolderTitle(folderTitle)
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
    ): QueryResult<Long> {
        return database
            .taggingsQueries
            .upsert(
                id = id,
                feed_id = feedID,
                name = name
            )
    }

    fun updateTitle(
        previousTitle: String,
        title: String,
    ) {
        val expanded = database.foldersQueries.find(previousTitle).executeAsOneOrNull() ?: false

        database.taggingsQueries.updateTitle(
            previousTitle = previousTitle,
            title = title,
            expanded = expanded,
        )
    }
}
