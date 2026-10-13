package com.capyreader.app.refresher

import androidx.work.WorkInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.ZoneOffset

class RefreshReportTest {
    private val hour = Duration.ofHours(1).toMillis()

    private val status = RefreshStatus(
        interval = RefreshInterval.EVERY_TWO_HOURS,
        wifiOnly = false,
        jobState = WorkInfo.State.ENQUEUED,
        nextRunAt = null,
        stopReason = WorkInfo.STOP_REASON_QUOTA,
        standbyBucket = 40,
        ignoringBatteryOptimizations = true,
        backgroundRestricted = false,
        notificationsEnabled = true,
    )

    @Test
    fun refreshLogRows_newestFirstWithGapsBetweenStarts() {
        val entries = listOf(
            RefreshLogEntry(timestamp = 0, event = RefreshEvent.STARTED),
            RefreshLogEntry(timestamp = 1_000, event = RefreshEvent.REFRESHED, notifications = 1),
            RefreshLogEntry(timestamp = hour * 5, event = RefreshEvent.STARTED),
            RefreshLogEntry(timestamp = hour * 5, event = RefreshEvent.SKIPPED_WIFI_ONLY),
        )

        val rows = refreshLogRows(entries)

        assertEquals(
            listOf(
                RefreshEvent.SKIPPED_WIFI_ONLY,
                RefreshEvent.STARTED,
                RefreshEvent.REFRESHED,
                RefreshEvent.STARTED,
            ),
            rows.map { it.entry.event },
        )
        assertEquals(Duration.ofHours(5), rows[1].sincePreviousStart)
        assertNull(rows[2].sincePreviousStart)
        assertNull(rows[3].sincePreviousStart)
    }

    @Test
    fun isLate_moreThanTwiceTheInterval() {
        assertFalse(status.isLate(Duration.ofHours(4)))
        assertTrue(status.isLate(Duration.ofHours(5)))
    }

    @Test
    fun isLate_manualRefresh() {
        val manual = status.copy(interval = RefreshInterval.MANUALLY_ONLY)

        assertFalse(manual.isLate(Duration.ofDays(3)))
    }

    @Test
    fun build_includesStatusAndHistory() {
        val rows = refreshLogRows(
            listOf(
                RefreshLogEntry(timestamp = 0, event = RefreshEvent.STARTED),
                RefreshLogEntry(timestamp = hour * 3 + 60_000, event = RefreshEvent.STARTED),
                RefreshLogEntry(
                    timestamp = hour * 3 + 60_000,
                    event = RefreshEvent.STOPPED,
                    detail = stopReasonName(WorkInfo.STOP_REASON_QUOTA),
                ),
            )
        )

        val report = RefreshReport.build(
            status = status,
            rows = rows,
            debugInfo = "App version: test",
            generatedAt = 0,
            zone = ZoneOffset.UTC,
        )

        val lines = report.lines()

        assertTrue(lines.contains("App version: test"))
        assertTrue(lines.contains("Refresh interval: EVERY_TWO_HOURS"))
        assertTrue(lines.contains("Next run: None"))
        assertTrue(lines.contains("Last stop reason: Quota (10)"))
        assertTrue(lines.contains("Standby bucket: Rare (40)"))
        assertTrue(lines.contains("1970-01-01 03:01 +00:00  STOPPED  Quota (10)"))
        assertTrue(lines.contains("1970-01-01 03:01 +00:00  STARTED  +3h01m"))
        assertEquals("1970-01-01 00:00 +00:00  STARTED", lines.last())
    }

    @Test
    fun build_withoutHistory() {
        val report = RefreshReport.build(
            status = status.copy(jobState = null),
            rows = emptyList(),
            debugInfo = "",
            generatedAt = 0,
            zone = ZoneOffset.UTC,
        )

        val lines = report.lines()

        assertTrue(lines.contains("Job state: Not scheduled"))
        assertEquals("No background refreshes recorded", lines.last())
    }
}
