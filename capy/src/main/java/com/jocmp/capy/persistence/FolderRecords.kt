package com.jocmp.capy.persistence

import com.jocmp.capy.common.withIOContext
import com.jocmp.capy.db.Database

internal class FolderRecords(private val database: Database) {
    suspend fun expand(folderName: String, expanded: Boolean) = withIOContext {
        database.foldersQueries.upsert(
            name = folderName,
            expanded = expanded,
        )
    }
}
