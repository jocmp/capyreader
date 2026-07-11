package com.jocmp.capy.persistence

import com.jocmp.capy.SyncStatus
import com.jocmp.capy.common.transactionWithErrorHandling
import com.jocmp.capy.db.Database

// SQLite's SQLITE_MAX_VARIABLE_NUMBER is 999 on the engine bundled with Android
// (< 3.32). An IN-clause with more bound IDs than that fails to even compile
// ("too many SQL variables"), which on a large account crashed every sync. Chunk
// the ID list below the limit, leaving headroom for the extra "key" bind param.
private const val SQL_VARIABLE_CHUNK = 500

class SyncStatusRecords(
    private val database: Database
) {
    private val queries get() = database.articleSyncStatusQueries

    fun insertStatuses(articleIDs: List<String>, key: SyncStatus.Key, flag: Boolean) {
        if (articleIDs.isEmpty()) return

        database.transactionWithErrorHandling {
            articleIDs.forEach { articleID ->
                queries.insertStatus(
                    articleID = articleID,
                    key = key.raw,
                    flag = flag,
                )
            }
        }
    }

    fun insertStatus(articleID: String, key: SyncStatus.Key, flag: Boolean) {
        insertStatuses(listOf(articleID), key = key, flag = flag)
    }

    fun selectForSync(): List<SyncStatus> {
        return database.transactionWithResult {
            queries.markAllSelected()
            queries.selectSelected(::mapSyncStatus).executeAsList()
        }
    }

    fun resetAllSelected() {
        queries.resetAllSelected()
    }

    fun deleteSelected(articleIDs: List<String>, key: SyncStatus.Key) {
        if (articleIDs.isEmpty()) return

        database.transactionWithErrorHandling {
            articleIDs.chunked(SQL_VARIABLE_CHUNK).forEach { chunk ->
                queries.deleteSelectedByID(articleIDs = chunk, key = key.raw)
            }
        }
    }

    fun resetSelected(articleIDs: List<String>, key: SyncStatus.Key) {
        if (articleIDs.isEmpty()) return

        database.transactionWithErrorHandling {
            articleIDs.chunked(SQL_VARIABLE_CHUNK).forEach { chunk ->
                queries.resetSelectedByID(articleIDs = chunk, key = key.raw)
            }
        }
    }

    fun pendingCount(): Long {
        return queries.selectPendingCount().executeAsOne()
    }

    fun pendingArticleIDs(key: SyncStatus.Key): List<String> {
        return queries.selectPendingByKey(key.raw).executeAsList()
    }
}

private fun mapSyncStatus(
    articleID: String,
    key: String,
    flag: Boolean,
    selected: Boolean,
): SyncStatus = SyncStatus(
    articleID = articleID,
    key = SyncStatus.Key.from(key)!!,
    flag = flag,
    selected = selected,
)
