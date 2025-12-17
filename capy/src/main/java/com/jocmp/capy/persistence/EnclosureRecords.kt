package com.jocmp.capy.persistence

import com.jocmp.capy.Enclosure
import com.jocmp.capy.common.optionalURL
import com.jocmp.capy.common.withIOContext
import com.jocmp.capy.db.Database
import java.net.URL

internal class EnclosureRecords internal constructor(
    private val database: Database
) {
    fun create(
        url: String,
        type: String,
        articleID: String,
        itunesDurationSeconds: String? = null,
        itunesImage: String? = null,
    ) {
        val parsedURL = optionalURL(url)?.toString() ?: return

        database.enclosuresQueries.create(
            url = parsedURL,
            type = type,
            article_id = articleID,
            itunes_duration_seconds = itunesDurationSeconds?.toLongOrNull(),
            itunes_image = optionalURL(itunesImage)?.toString(),
        )
    }

    suspend fun byArticle(id: String): List<Enclosure> = withIOContext {
        database
            .enclosuresQueries
            .findByArticleID(id, ::mapper)
            .executeAsList()
    }

    fun mapper(
        url: String,
        type: String,
        itunesDurationSeconds: Long?,
        itunesImage: String?
    ): Enclosure {
        return Enclosure(
            url = URL(url),
            type = type,
            itunesDurationSeconds = itunesDurationSeconds,
            itunesImage = itunesImage,
        )
    }
}
