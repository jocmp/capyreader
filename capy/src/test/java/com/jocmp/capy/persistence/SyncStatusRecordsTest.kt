package com.jocmp.capy.persistence

import com.jocmp.capy.InMemoryDatabaseProvider
import com.jocmp.capy.SyncStatus
import com.jocmp.capy.db.Database
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SyncStatusRecordsTest {
    private lateinit var database: Database
    private lateinit var records: SyncStatusRecords

    @BeforeTest
    fun setup() {
        database = InMemoryDatabaseProvider.build("sync-test")
        records = SyncStatusRecords(database)
    }

    @Test
    fun insertStatus_upsertsByArticleAndKey() {
        records.insertStatus("a", SyncStatus.Key.READ, flag = true)
        records.insertStatus("a", SyncStatus.Key.READ, flag = false)
        records.insertStatus("a", SyncStatus.Key.STARRED, flag = true)

        assertEquals(2, records.pendingCount())
        assertEquals(listOf("a"), records.pendingArticleIDs(SyncStatus.Key.READ))
        assertEquals(listOf("a"), records.pendingArticleIDs(SyncStatus.Key.STARRED))
    }

    @Test
    fun selectForProcessing_marksRowsSelected() {
        records.insertStatus("a", SyncStatus.Key.READ, flag = true)
        records.insertStatus("b", SyncStatus.Key.READ, flag = true)

        val processing = records.selectForProcessing()
        assertEquals(2, processing.size)
        assertTrue(processing.all { it.selected })

        // Once selected, pendingArticleIDs (which only sees selected = 0) returns empty.
        assertTrue(records.pendingArticleIDs(SyncStatus.Key.READ).isEmpty())
    }

    @Test
    fun deleteSelected_clearsOnlyMatchingSelectedRows() {
        records.insertStatus("a", SyncStatus.Key.READ, flag = true)
        records.insertStatus("b", SyncStatus.Key.READ, flag = true)
        records.selectForProcessing()

        // User re-toggles "a" while in flight: upsert resets selected to 0 with new flag.
        records.insertStatus("a", SyncStatus.Key.READ, flag = false)

        records.deleteSelected(listOf("a", "b"), SyncStatus.Key.READ)

        // "b" was deleted (still selected). "a" survives because the upsert reset selected.
        val pending = records.pendingArticleIDs(SyncStatus.Key.READ)
        assertEquals(listOf("a"), pending)
    }

    @Test
    fun resetSelected_returnsRowsForRetry() {
        records.insertStatus("a", SyncStatus.Key.READ, flag = true)
        records.selectForProcessing()

        records.resetSelected(listOf("a"), SyncStatus.Key.READ)

        assertEquals(listOf("a"), records.pendingArticleIDs(SyncStatus.Key.READ))
    }

    @Test
    fun resetAllSelected_clearsLeases() {
        records.insertStatus("a", SyncStatus.Key.READ, flag = true)
        records.insertStatus("b", SyncStatus.Key.STARRED, flag = true)
        records.selectForProcessing()

        records.resetAllSelected()

        assertEquals(listOf("a"), records.pendingArticleIDs(SyncStatus.Key.READ))
        assertEquals(listOf("b"), records.pendingArticleIDs(SyncStatus.Key.STARRED))
    }
}
