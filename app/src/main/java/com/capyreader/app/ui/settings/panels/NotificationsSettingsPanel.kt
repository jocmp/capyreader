package com.capyreader.app.ui.settings.panels

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.capyreader.app.ui.fixtures.FeedSample
import com.capyreader.app.ui.theme.CapyTheme
import com.jocmp.capy.Feed

@Composable
fun NotificationsSettingsPanel(
    onSelectAll: () -> Unit,
    onSelectNone: () -> Unit,
    onToggleNotifications: (feedID: String, enabled: Boolean) -> Unit,
    feeds: List<Feed>
) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
    ) {
        NotificationGroupCheckbox(
            onSelectAll = onSelectAll,
            onSelectNone = onSelectNone,
            feeds = feeds,
        )
        feeds.forEach { feed ->
            NotificationCheckbox(
                onToggleNotifications = {
                    onToggleNotifications(feed.id, it)
                },
                feed = feed
            )
        }
    }
}


@Preview
@Composable
fun NotificationsSettingsPanelPreview() {
    val feeds = FeedSample().values.take(3).toList()

    CapyTheme {
        NotificationsSettingsPanel(
            onSelectNone = {},
            onSelectAll = {},
            onToggleNotifications = { _, _ -> },
            feeds = feeds
        )
    }
}
