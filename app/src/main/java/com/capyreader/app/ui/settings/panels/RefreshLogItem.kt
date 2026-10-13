package com.capyreader.app.ui.settings.panels

import android.icu.text.MeasureFormat
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.work.WorkInfo
import com.capyreader.app.R
import com.capyreader.app.refresher.RefreshEvent
import com.capyreader.app.refresher.RefreshInterval
import com.capyreader.app.refresher.RefreshLogEntry
import com.capyreader.app.refresher.RefreshLogRow
import com.capyreader.app.refresher.RefreshStatus
import com.capyreader.app.refresher.refreshLogRows
import com.capyreader.app.refresher.standbyBucketName
import com.capyreader.app.refresher.stopReasonName
import com.capyreader.app.ui.components.DialogCard
import com.capyreader.app.ui.components.FormSection
import com.capyreader.app.ui.components.buildCopyToClipboard
import com.capyreader.app.ui.theme.CapyTheme
import org.koin.androidx.compose.koinViewModel
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun RefreshLogItem() {
    val (isOpen, setOpen) = remember { mutableStateOf(false) }

    Box(Modifier.clickable { setOpen(true) }) {
        ListItem(
            headlineContent = { Text(stringResource(R.string.refresh_log_title)) },
            supportingContent = { Text(stringResource(R.string.refresh_log_subtitle)) }
        )
    }

    if (isOpen) {
        RefreshLogDialog(onDismissRequest = { setOpen(false) })
    }
}

@Composable
private fun RefreshLogDialog(
    onDismissRequest: () -> Unit,
    viewModel: RefreshLogViewModel = koinViewModel(),
) {
    LaunchedEffect(Unit) {
        viewModel.load()
    }

    Dialog(onDismissRequest = onDismissRequest) {
        DialogCard {
            RefreshLogView(
                status = viewModel.status,
                rows = viewModel.rows,
                report = viewModel.report,
                onNavigateBack = onDismissRequest,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RefreshLogView(
    status: RefreshStatus?,
    rows: List<RefreshLogRow>,
    report: String?,
    onNavigateBack: () -> Unit,
) {
    val copyReport = buildCopyToClipboard(report.orEmpty())

    Surface {
        Column {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.refresh_log_title))
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(android.R.string.cancel)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = copyReport,
                        enabled = report != null,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = stringResource(R.string.refresh_log_copy_report)
                        )
                    }
                }
            )
            LazyColumn {
                if (status != null) {
                    item {
                        FormSection(title = stringResource(R.string.refresh_log_section_status)) {
                            RefreshStatusRows(status = status)
                        }
                    }
                }

                item {
                    FormSection(
                        title = stringResource(R.string.refresh_log_section_history),
                        modifier = Modifier.padding(top = 16.dp),
                    ) {
                        if (rows.isEmpty()) {
                            Text(
                                text = stringResource(R.string.refresh_log_empty),
                                style = typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                items(rows) { row ->
                    val gap = row.sincePreviousStart
                    val isLate = status != null && gap != null && status.isLate(gap)

                    RefreshLogRowItem(
                        row = row,
                        isLate = isLate,
                    )
                }
            }
        }
    }
}

@Composable
private fun RefreshStatusRows(status: RefreshStatus) {
    val context = LocalContext.current
    val yes = stringResource(R.string.refresh_log_yes)
    val no = stringResource(R.string.refresh_log_no)
    val none = stringResource(R.string.refresh_log_none)
    val jobState = status.jobState?.name ?: stringResource(R.string.refresh_log_not_scheduled)

    val yesNo = { value: Boolean ->
        if (value) {
            yes
        } else {
            no
        }
    }

    val rows = listOf(
        stringResource(R.string.refresh_feeds_menu_label) to context.translationKey(status.interval),
        stringResource(R.string.settings_refresh_on_wifi_only) to yesNo(status.wifiOnly),
        stringResource(R.string.refresh_log_job_state) to jobState,
        stringResource(R.string.refresh_log_next_run) to (status.nextRunAt?.let(::formatTimestamp) ?: none),
        stringResource(R.string.refresh_log_stop_reason) to (status.stopReason?.let(::stopReasonName) ?: none),
        stringResource(R.string.refresh_log_standby_bucket) to standbyBucketName(status.standbyBucket),
        stringResource(R.string.refresh_log_battery_optimization) to yesNo(status.ignoringBatteryOptimizations),
        stringResource(R.string.refresh_log_background_restricted) to yesNo(status.backgroundRestricted),
        stringResource(R.string.refresh_log_notifications_enabled) to yesNo(status.notificationsEnabled),
    )

    Column {
        rows.forEach { (label, value) ->
            ListItem(
                headlineContent = { Text(label) },
                supportingContent = { Text(value) },
            )
        }
    }
}

@Composable
private fun RefreshLogRowItem(row: RefreshLogRow, isLate: Boolean) {
    val entry = row.entry

    val details = listOfNotNull(
        formatTimestamp(entry.timestamp),
        row.sincePreviousStart?.let {
            stringResource(R.string.refresh_log_since_previous, formatDuration(it))
        },
        entry.notifications?.let {
            pluralStringResource(R.plurals.refresh_log_notifications_sent, it, it)
        },
        entry.detail,
    )

    val detailColor = if (isLate) {
        colorScheme.error
    } else {
        colorScheme.onSurfaceVariant
    }

    ListItem(
        headlineContent = { Text(stringResource(entry.event.title)) },
        supportingContent = {
            Text(
                text = details.joinToString("\n"),
                color = detailColor,
            )
        },
    )
}

private val RefreshEvent.title: Int
    get() = when (this) {
        RefreshEvent.STARTED -> R.string.refresh_log_event_started
        RefreshEvent.REFRESHED -> R.string.refresh_log_event_refreshed
        RefreshEvent.SKIPPED_WIFI_ONLY -> R.string.refresh_log_event_skipped_wifi_only
        RefreshEvent.FAILED -> R.string.refresh_log_event_failed
        RefreshEvent.STOPPED -> R.string.refresh_log_event_stopped
    }

private val timestampFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)

private fun formatTimestamp(millis: Long): String {
    return timestampFormatter.format(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()))
}

private fun formatDuration(duration: Duration): String {
    val format = MeasureFormat.getInstance(Locale.getDefault(), MeasureFormat.FormatWidth.SHORT)

    return format.formatMeasures(
        Measure(duration.toHours(), MeasureUnit.HOUR),
        Measure(duration.toMinutesPart(), MeasureUnit.MINUTE),
    )
}

@Preview
@Composable
private fun RefreshLogViewPreview() {
    val now = System.currentTimeMillis()
    val hour = Duration.ofHours(1).toMillis()

    val entries = listOf(
        RefreshLogEntry(timestamp = now - hour * 9, event = RefreshEvent.STARTED),
        RefreshLogEntry(timestamp = now - hour * 9, event = RefreshEvent.REFRESHED, notifications = 2),
        RefreshLogEntry(timestamp = now - hour * 7, event = RefreshEvent.STARTED),
        RefreshLogEntry(timestamp = now - hour * 7, event = RefreshEvent.SKIPPED_WIFI_ONLY),
        RefreshLogEntry(timestamp = now - hour, event = RefreshEvent.STARTED),
        RefreshLogEntry(
            timestamp = now - hour,
            event = RefreshEvent.STOPPED,
            detail = stopReasonName(WorkInfo.STOP_REASON_QUOTA),
        ),
    )

    CapyTheme {
        DialogCard {
            RefreshLogView(
                status = RefreshStatus(
                    interval = RefreshInterval.EVERY_TWO_HOURS,
                    wifiOnly = true,
                    jobState = WorkInfo.State.ENQUEUED,
                    nextRunAt = now + hour,
                    stopReason = null,
                    standbyBucket = 40,
                    ignoringBatteryOptimizations = true,
                    backgroundRestricted = false,
                    notificationsEnabled = true,
                ),
                rows = refreshLogRows(entries),
                report = "",
                onNavigateBack = {},
            )
        }
    }
}
