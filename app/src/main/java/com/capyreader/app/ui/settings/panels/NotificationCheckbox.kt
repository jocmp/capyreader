package com.capyreader.app.ui.settings.panels

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    Box(
        Modifier.clickable {
            onToggleNotifications(!feed.enableNotifications)
        }
    ) {
        ListItem(
            leadingContent = {
                FaviconBadge(feed.faviconURL)
            },
            headlineContent = {
                Text(feed.title)
            },
            trailingContent = {
                Checkbox(
                    feed.enableNotifications,
                    onCheckedChange = {
                        onToggleNotifications(it)
                    }
                )
            }
        )
    }
}

@Preview
@Composable
fun NotificationListItemPreview(@PreviewParameter(FeedSample::class, limit = 1) feed: Feed) {
    NotificationCheckbox(
        onToggleNotifications = {},
        feed = feed.copy(enableNotifications = true),
    )
}
