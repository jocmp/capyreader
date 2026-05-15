package com.capyreader.app.ui.settings.panels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capyreader.app.R
import com.capyreader.app.preferences.label
import com.capyreader.app.ui.articles.FaviconBadge
import com.capyreader.app.ui.components.FormSection
import com.capyreader.app.ui.settings.PreferenceSelect
import com.jocmp.capy.Feed
import com.jocmp.capy.preferences.OfflineCacheSize
import org.koin.androidx.compose.koinViewModel

@Composable
fun OfflineReadingSettingsPanel(
    viewModel: OfflineReadingViewModel = koinViewModel(),
) {
    val feeds by viewModel.feeds.collectAsStateWithLifecycle(emptyList())

    OfflineReadingSettingsPanelView(
        cacheSize = viewModel.cacheSize,
        updateCacheSize = viewModel::updateCacheSize,
        feeds = feeds,
        onToggleFeed = viewModel::toggleFeedOffline,
    )
}

@Composable
private fun OfflineReadingSettingsPanelView(
    cacheSize: OfflineCacheSize,
    updateCacheSize: (OfflineCacheSize) -> Unit,
    feeds: List<Feed>,
    onToggleFeed: (feedID: String, enabled: Boolean) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.verticalScroll(rememberScrollState()),
    ) {
        FormSection(title = stringResource(R.string.settings_section_offline_cache)) {
            PreferenceSelect(
                selected = cacheSize,
                update = updateCacheSize,
                options = OfflineCacheSize.options,
                label = R.string.offline_cache_size_label,
                optionText = { it.label() },
            )
        }

        if (cacheSize != OfflineCacheSize.Off) {
            FormSection(title = stringResource(R.string.offline_feeds_section)) {
                Column {
                    feeds.forEach { feed ->
                        FeedSettingsCheckbox(
                            title = feed.title,
                            checked = feed.offlineEnabled,
                            onToggle = { onToggleFeed(feed.id, it) },
                            leadingContent = { FaviconBadge(feed.faviconURL) },
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}
