package com.capyreader.app.ui.articles.feeds

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.runtime.Composable
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
 * Collapsed rail items for medium and wider windows: the fixed top-level filters. The full feed
 * and folder tree shows when the rail expands.
 */
@Composable
fun ArticleNavigationRailItems(
    filter: ArticleFilter,
    statusCount: Long,
    todayCount: Long,
    readLaterFeed: Feed?,
    onFilterSelect: () -> Unit,
    onSelectToday: () -> Unit,
    onSelectFeed: (feed: Feed, folderTitle: String?) -> Unit,
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
}

@Composable
private fun RailItem(
    icon: ImageVector,
    label: String,
    count: Long,
    selected: Boolean,
    onClick: () -> Unit,
) {
    WideNavigationRailItem(
        selected = selected,
        onClick = onClick,
        railExpanded = false,
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
