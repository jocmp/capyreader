package com.jocmp.capy.persistence

import com.jocmp.capy.db.Database

internal class FolderRecords(private val database: Database) {
    fun expand(folderName: String, expanded: Boolean) {
        database.foldersQueries.upsert(
            name = folderName,
            expanded = expanded,
        )
    }
}
