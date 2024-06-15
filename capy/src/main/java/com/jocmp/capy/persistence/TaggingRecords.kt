package com.jocmp.capy.persistence

import com.jocmp.capy.Feed
import com.jocmp.capy.db.Database

internal class TaggingRecords(
    private val database: Database
) {
    fun findFeedTaggingsToDelete(
        feed: Feed,
        excludedTaggingNames: List<String>
    ): List<Long> {
        return database
            .taggingsQueries
            .findFeedTaggingsToDelete(
                feedID = feed.id,
                excludedNames = excludedTaggingNames
            )
            .executeAsList()
    }

    fun upsert(
        id: Long,
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
