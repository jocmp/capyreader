package com.capyreader.app.ui.settings.panels

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.capyreader.app.R
import com.capyreader.app.ui.fixtures.FeedSample
import com.jocmp.capy.Feed

@Composable
fun NotificationGroupCheckbox(
    onSelectNone: () -> Unit,
    onSelectAll: () -> Unit,
    feeds: List<Feed>,
) {
    val groupSelection = rememberGroupSelection(feeds)

    val onClick = {
        when (groupSelection) {
            ToggleableState.On -> onSelectNone()
            else -> onSelectAll()
        }
    }

    Box(
        Modifier.clickable {
            onClick()
        }
    ) {
        ListItem(
            headlineContent = {
                val text = when (groupSelection) {
                    ToggleableState.On -> stringResource(R.string.settings_notifications_select_none)
                    else -> stringResource(R.string.settings_notifications_select_all)
                }

                Text(text, fontWeight = FontWeight.Medium)
            },
            trailingContent = {
                TriStateCheckbox(
                    state = groupSelection,
                    onClick = onClick
                )
            }
        )
    }
}

@Composable
private fun rememberGroupSelection(feeds: List<Feed>): ToggleableState {
    return remember(feeds) {
        val count = feeds.count { it.enableNotifications }

        when (count) {
            0 -> ToggleableState.Off
            feeds.size -> ToggleableState.On
            else -> ToggleableState.Indeterminate
        }
    }
}


@Preview
@Composable
fun NotificationGroupCheckboxPreview() {
    val feeds = FeedSample().values.take(2).toList()

    NotificationGroupCheckbox(
        onSelectNone = {},
        onSelectAll = {},
        feeds = feeds,
    )
}
