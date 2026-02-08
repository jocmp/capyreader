package com.capyreader.app.ui.articles

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckBox
import androidx.compose.material.icons.rounded.CheckBoxOutlineBlank
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.capyreader.app.R
import com.capyreader.app.preferences.BadgeStyle
import com.capyreader.app.ui.LocalBadgeStyle
import com.capyreader.app.ui.fixtures.FeedSample
import com.jocmp.capy.Feed

@Composable
fun FeedActionMenuItems(
    feed: Feed,
    onMenuClose: () -> Unit,
    onRemoveRequest: () -> Unit,
    onEdit: () -> Unit,
    onReloadIcon: () -> Unit,
    onToggleOpenInBrowser: () -> Unit,
    onToggleUnreadBadge: () -> Unit,
    showReloadIcon: Boolean = false,
) {
    DropdownMenuItem(
        text = {
            Text(stringResource(R.string.feed_action_edit))
        },
        onClick = {
            onMenuClose()
            onEdit()
        }
    )
    if (showReloadIcon) {
        DropdownMenuItem(
            text = {
                Text(stringResource(R.string.feed_action_reload_icon))
            },
            onClick = {
                onMenuClose()
                onReloadIcon()
            }
        )
    }
    DropdownMenuItem(
        text = {
            Text(stringResource(R.string.feed_action_unsubscribe))
        },
        onClick = {
            onMenuClose()
            onRemoveRequest()
        }
    )
    HorizontalDivider()
    val trailingIcon = @Composable {
        if (feed.openArticlesInBrowser) {
            Icon(Icons.Rounded.CheckBox, contentDescription = null)
        } else {
            Icon(Icons.Rounded.CheckBoxOutlineBlank, contentDescription = null)
        }
    }
    DropdownMenuItem(
        trailingIcon = trailingIcon,
        text = {
            Text(stringResource(R.string.open_articles_in_browser))
        },
        onClick = onToggleOpenInBrowser
    )
    if (LocalBadgeStyle.current == BadgeStyle.SIMPLE) {
        val unreadBadgeIcon = @Composable {
            if (feed.showUnreadBadge) {
                Icon(Icons.Rounded.CheckBox, contentDescription = null)
            } else {
                Icon(Icons.Rounded.CheckBoxOutlineBlank, contentDescription = null)
            }
        }
        DropdownMenuItem(
            trailingIcon = unreadBadgeIcon,
            text = {
                Text(stringResource(R.string.show_unread_badge))
            },
            onClick = onToggleUnreadBadge
        )
    }
}

@Preview
@Composable
fun FeedActionMenuPreview(@PreviewParameter(FeedSample::class) feed: Feed) {
    Column {
        FeedActionMenuItems(
            feed = feed,
            onMenuClose = {},
            onEdit = {},
            onReloadIcon = {},
            onRemoveRequest = {},
            onToggleOpenInBrowser = {},
            onToggleUnreadBadge = {},
        )
    }
}
