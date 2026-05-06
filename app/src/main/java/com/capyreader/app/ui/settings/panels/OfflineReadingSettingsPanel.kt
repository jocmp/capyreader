package com.capyreader.app.ui.settings.panels

import android.text.format.Formatter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.common.RowItem
import com.capyreader.app.ui.components.FormSection
import com.capyreader.app.ui.components.TextSwitch
import com.capyreader.app.ui.theme.CapyTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun OfflineReadingSettingsPanel(
    viewModel: OfflineReadingViewModel = koinViewModel(),
) {
    OfflineReadingSettingsPanelView(
        offlineStarredArticles = viewModel.offlineStarredArticles,
        updateOfflineStarredArticles = viewModel::updateOfflineStarredArticles,
        cacheUsedBytes = viewModel.cacheUsedBytes,
        cacheLimitBytes = viewModel.cacheLimitBytes,
        onClearCache = viewModel::clearCache,
    )
}

@Composable
fun OfflineReadingSettingsPanelView(
    offlineStarredArticles: Boolean,
    updateOfflineStarredArticles: (Boolean) -> Unit,
    cacheUsedBytes: Long,
    cacheLimitBytes: Long,
    onClearCache: () -> Unit,
) {
    var isClearCacheDialogOpen by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.verticalScroll(rememberScrollState()),
    ) {
        RowItem {
            TextSwitch(
                checked = offlineStarredArticles,
                onCheckedChange = updateOfflineStarredArticles,
                title = stringResource(R.string.settings_offline_starred_title),
                subtitle = stringResource(R.string.settings_offline_starred_subtitle),
            )
        }

        FormSection(
            title = stringResource(R.string.settings_section_offline_reading_storage)
        ) {
            Column {
                CacheUsageRow(
                    usedBytes = cacheUsedBytes,
                    limitBytes = cacheLimitBytes,
                )
                ClearCacheRow(onClick = { isClearCacheDialogOpen = true })
            }
        }
        Spacer(Modifier.height(16.dp))
    }

    if (isClearCacheDialogOpen) {
        AlertDialog(
            onDismissRequest = { isClearCacheDialogOpen = false },
            title = { Text(stringResource(R.string.settings_clear_offline_cache_title)) },
            text = { Text(stringResource(R.string.settings_clear_offline_cache_text)) },
            dismissButton = {
                TextButton(onClick = { isClearCacheDialogOpen = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        isClearCacheDialogOpen = false
                        onClearCache()
                    }
                ) {
                    Text(stringResource(R.string.settings_clear_all_articles_confirm))
                }
            }
        )
    }
}

@Composable
private fun ClearCacheRow(onClick: () -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        FilledTonalButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.settings_clear_offline_cache_button))
        }
        Text(
            text = stringResource(R.string.settings_clear_offline_cache_text),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun CacheUsageRow(usedBytes: Long, limitBytes: Long) {
    val context = LocalContext.current
    val safeLimit = limitBytes.coerceAtLeast(1L)
    val progress = (usedBytes.toFloat() / safeLimit.toFloat()).coerceIn(0f, 1f)
    val usedText = Formatter.formatShortFileSize(context, usedBytes)
    val limitText = Formatter.formatShortFileSize(context, limitBytes)

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = stringResource(R.string.settings_offline_cache_usage, usedText, limitText),
        )
    }
}

@Preview
@Composable
private fun OfflineReadingSettingsPanelPreview() {
    CapyTheme {
        OfflineReadingSettingsPanelView(
            offlineStarredArticles = true,
            updateOfflineStarredArticles = {},
            cacheUsedBytes = 250L * 1024 * 1024,
            cacheLimitBytes = 2L * 1024 * 1024 * 1024,
            onClearCache = {},
        )
    }
}
