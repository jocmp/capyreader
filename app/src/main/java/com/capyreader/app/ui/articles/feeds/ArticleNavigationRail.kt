package com.capyreader.app.ui.articles.feeds

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.capyreader.app.R
import com.capyreader.app.preferences.BadgeStyle
import com.capyreader.app.ui.LocalBadgeStyle
import com.capyreader.app.ui.articles.icon
import com.capyreader.app.ui.navigationTitle
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.Feed

/**
 * Collapsed navigation for medium and wider windows. Holds the fixed top-level filters; the full
 * feed and folder tree stays in the modal drawer, opened from the rail's menu button.
 */
@Composable
fun ArticleNavigationRail(
    filter: ArticleFilter,
    statusCount: Long,
    todayCount: Long,
    readLaterFeed: Feed?,
    refreshState: AngleRefreshState,
    onOpenDrawer: () -> Unit,
    onFilterSelect: () -> Unit,
    onSelectToday: () -> Unit,
    onSelectFeed: (feed: Feed, folderTitle: String?) -> Unit,
    onRefresh: () -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    val buttonState = rememberRefreshButtonState(refreshState)

    NavigationRail(
        windowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Vertical + WindowInsetsSides.Start
        ),
        header = {
            IconButton(onClick = onOpenDrawer) {
                Icon(
                    imageVector = Icons.Rounded.Menu,
                    contentDescription = stringResource(R.string.feed_nav_drawer_open)
                )
            }
        },
    ) {
        RailItem(
            icon = filter.status.icon,
            label = stringResource(filter.status.navigationTitle),
            count = statusCount,
            selected = filter.hasArticlesSelected(),
            onClick = onFilterSelect,
        )
        RailItem(
            icon = Icons.Rounded.Today,
            label = stringResource(R.string.filter_today),
            count = todayCount,
            selected = filter.hasTodaySelected(),
            onClick = onSelectToday,
        )
        if (readLaterFeed != null) {
            RailItem(
                icon = Icons.Rounded.Bookmark,
                label = stringResource(R.string.filter_read_later),
                count = readLaterFeed.count,
                selected = filter.isFeedSelected(readLaterFeed),
                onClick = { onSelectFeed(readLaterFeed, null) },
            )
        }

        Spacer(Modifier.weight(1f))

        Column {
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = stringResource(R.string.feed_nav_drawer_refresh_all),
                    modifier = Modifier.graphicsLayer {
                        rotationZ = buttonState.iconRotation
                    }
                )
            }
            IconButton(onClick = onNavigateToSettings) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = stringResource(R.string.settings)
                )
            }
        }
    }
}

@Composable
private fun RailItem(
    icon: ImageVector,
    label: String,
    count: Long,
    selected: Boolean,
    onClick: () -> Unit,
) {
    NavigationRailItem(
        selected = selected,
        onClick = onClick,
        label = { Text(text = label, maxLines = 1) },
        icon = {
            BadgedBox(badge = { RailBadge(count = count) }) {
                Icon(imageVector = icon, contentDescription = null)
            }
        },
    )
}

@Composable
private fun RailBadge(count: Long) {
    if (count < 1) {
        return
    }

    when (LocalBadgeStyle.current) {
        BadgeStyle.EXACT -> Badge { Text(if (count > MAX_BADGE_COUNT) "$MAX_BADGE_COUNT+" else count.toString()) }
        BadgeStyle.SIMPLE -> Badge()
        BadgeStyle.HIDDEN -> {}
    }
}

private const val MAX_BADGE_COUNT = 999
