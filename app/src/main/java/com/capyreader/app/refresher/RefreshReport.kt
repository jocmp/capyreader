package com.capyreader.app.refresher

import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class RefreshLogRow(
    val entry: RefreshLogEntry,
    val sincePreviousStart: Duration?,
)

fun refreshLogRows(entries: List<RefreshLogEntry>): List<RefreshLogRow> {
    val gaps = entries
        .filter { it.event == RefreshEvent.STARTED }
        .zipWithNext { previous, current ->
            current to Duration.ofMillis(current.timestamp - previous.timestamp)
        }
        .toMap()

    return entries
        .map { RefreshLogRow(entry = it, sincePreviousStart = gaps[it]) }
        .reversed()
}

object RefreshReport {
    fun build(
        status: RefreshStatus,
        rows: List<RefreshLogRow>,
        debugInfo: String,
        generatedAt: Long = System.currentTimeMillis(),
        zone: ZoneId = ZoneId.systemDefault(),
    ): String {
        val formatTime = { millis: Long -> formatter.format(Instant.ofEpochMilli(millis).atZone(zone)) }

        val statusLines = listOf(
            "Refresh interval: ${status.interval.name}",
            "Wi-Fi only: ${status.wifiOnly}",
            "Job state: ${status.jobState?.name ?: "Not scheduled"}",
            "Next run: ${status.nextRunAt?.let(formatTime) ?: "None"}",
            "Last stop reason: ${status.stopReason?.let(::stopReasonName) ?: "None"}",
            "Standby bucket: ${standbyBucketName(status.standbyBucket)}",
            "Ignoring battery optimizations: ${status.ignoringBatteryOptimizations}",
            "Background restricted: ${status.backgroundRestricted}",
            "Notifications enabled: ${status.notificationsEnabled}",
        )

        val historyLines = rows.map { row ->
            listOfNotNull(
                formatTime(row.entry.timestamp),
                row.entry.event.name,
                row.sincePreviousStart?.let { "+${formatGap(it)}" },
                row.entry.notifications?.let { "notifications=$it" },
                row.entry.detail,
            ).joinToString("  ")
        }

        val history = historyLines.ifEmpty { listOf("No background refreshes recorded") }

        return buildString {
            appendLine("Capy Reader background refresh report")
            appendLine("Generated: ${formatTime(generatedAt)}")
            appendLine()
            appendLine(debugInfo)
            appendLine()
            statusLines.forEach(::appendLine)
            appendLine()
            appendLine("History (newest first):")
            history.forEach(::appendLine)
        }.trimEnd()
    }

    fun formatGap(duration: Duration): String {
        val hours = duration.toHours()
        val minutes = duration.toMinutesPart()

        return "%dh%02dm".format(hours, minutes)
    }

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm xxx")
}
