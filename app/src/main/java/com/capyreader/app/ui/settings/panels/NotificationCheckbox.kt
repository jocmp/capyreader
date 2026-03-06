package com.capyreader.app.ui.settings.panels

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.capyreader.app.ui.articles.FaviconBadge
import com.capyreader.app.ui.fixtures.FeedSample
import com.jocmp.capy.Feed

@Composable
fun NotificationCheckbox(
    onToggleNotifications: (enabled: Boolean) -> Unit,
    feed: Feed,
) {
    FeedSettingsCheckbox(
        title = feed.title,
        checked = feed.enableNotifications,
        onToggle = onToggleNotifications,
        leadingContent = { FaviconBadge(feed.faviconURL) },
    )
}

@Preview
@Composable
fun NotificationListItemPreview(@PreviewParameter(FeedSample::class, limit = 1) feed: Feed) {
    NotificationCheckbox(
        onToggleNotifications = {},
        feed = feed.copy(enableNotifications = true),
    )
}
