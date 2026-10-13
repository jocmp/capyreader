package com.capyreader.app.refresher

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class RefreshLogTest {
    @get:Rule
    val folder = TemporaryFolder()

    private val file get() = File(folder.root, RefreshLog.FILE_NAME)

    @Test
    fun entries_emptyWithoutFile() = runTest {
        assertTrue(RefreshLog(file).entries().isEmpty())
    }

    @Test
    fun record_storesEntries() = runTest {
        val log = RefreshLog(file)

        log.record(RefreshEvent.STARTED, timestamp = 1_000)
        log.record(RefreshEvent.REFRESHED, notifications = 3, timestamp = 2_000)

        val expected = listOf(
            RefreshLogEntry(timestamp = 1_000, event = RefreshEvent.STARTED),
            RefreshLogEntry(timestamp = 2_000, event = RefreshEvent.REFRESHED, notifications = 3),
        )

        assertEquals(expected, log.entries())
    }

    @Test
    fun record_keepsNewestEntriesWithinLimit() = runTest {
        val log = RefreshLog(file, limit = 3)

        (1L..5L).forEach { log.record(RefreshEvent.STARTED, timestamp = it) }

        assertEquals(listOf(3L, 4L, 5L), log.entries().map { it.timestamp })
    }

    @Test
    fun entries_skipsUnreadableLines() = runTest {
        val log = RefreshLog(file)
        log.record(RefreshEvent.STARTED, timestamp = 1_000)
        file.appendText("not json\n")
        log.record(RefreshEvent.FAILED, detail = "boom", timestamp = 2_000)

        assertEquals(
            listOf(RefreshEvent.STARTED, RefreshEvent.FAILED),
            log.entries().map { it.event },
        )
    }
}
