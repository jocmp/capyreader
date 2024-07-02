package com.jocmp.capy.fixtures

import com.jocmp.capy.Feed
import com.jocmp.capy.RandomUUID
import com.jocmp.capy.db.Database
import com.jocmp.capy.persistence.TaggingRecords

class TagFixture(private val database: Database) {
    private val feedFixture = FeedFixture(database)

    fun create(
        name: String = "My Folder",
        feed: Feed = feedFixture.create(feedURL = "https://example.com/${RandomUUID.generate()}"),
        id: String = "${feed.title}:$name",
    ) {
        TaggingRecords(database).upsert(
            id = id,
            feedID = feed.id,
            name = name
        )
    }
}
