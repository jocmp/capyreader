package com.jocmp.capy.persistence

import com.jocmp.capy.Enclosure
import com.jocmp.capy.common.optionalURL
import com.jocmp.capy.db.Database

internal class EnclosureRecords internal constructor(
    private val database: Database
) {
    fun create(
        url: String,
        type: String,
        articleID: String,
        itunesDurationSeconds: String?,
        itunesImage: String?,
    ) {
        val parsedURL = optionalURL(url)?.toString() ?: return

        database.enclosuresQueries.create(
            url = parsedURL,
            type = type,
            article_id = articleID,
            itunes_duration_seconds = itunesDurationSeconds,
            itunes_image = optionalURL(itunesImage)?.toString(),
        )
    }

    fun byArticle(id: String): List<Enclosure> {
        return emptyList()
    }
}
