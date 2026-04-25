package com.jocmp.capy.persistence

import com.jocmp.capy.SyncStatus
import com.jocmp.capy.common.transactionWithErrorHandling
import com.jocmp.capy.db.Database

class SyncStatusRecords(
    private val database: Database
) {
    private val queries get() = database.article_sync_statusesQueries

    fun insertStatuses(articleIDs: Collection<String>, key: SyncStatus.Key, flag: Boolean) {
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

    fun selectForProcessing(): List<SyncStatus> {
        return database.transactionWithResult {
            queries.markAllSelected()
            queries.selectSelected().executeAsList().mapNotNull(::toDomain)
        }
    }

    fun resetAllSelected() {
        queries.resetAllSelected()
    }

    fun deleteSelected(articleIDs: Collection<String>, key: SyncStatus.Key) {
        if (articleIDs.isEmpty()) return
        queries.deleteSelectedByID(articleIDs = articleIDs.toList(), key = key.raw)
    }

    fun resetSelected(articleIDs: Collection<String>, key: SyncStatus.Key) {
        if (articleIDs.isEmpty()) return
        queries.resetSelectedByID(articleIDs = articleIDs.toList(), key = key.raw)
    }

    fun pendingCount(): Long {
        return queries.selectPendingCount().executeAsOne()
    }

    fun pendingArticleIDs(key: SyncStatus.Key): List<String> {
        return queries.selectPendingByKey(key.raw).executeAsList()
    }

    private fun toDomain(
        row: com.jocmp.capy.db.Article_sync_statuses
    ): SyncStatus? {
        val key = SyncStatus.Key.from(row.key) ?: return null
        return SyncStatus(
            articleID = row.article_id,
            key = key,
            flag = row.flag,
            selected = row.selected,
        )
    }
}
