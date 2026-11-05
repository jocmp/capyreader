package com.capyreader.app.ui.articles.feeds

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.capyreader.app.R
import com.capyreader.app.ui.articles.icon
import com.capyreader.app.ui.navigationTitle
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.Feed

/**
 * The top-level destinations, as direct children of the Material
 * [androidx.compose.material3.WideNavigationRail], following its samples.
 */
@Composable
fun ArticleNavigationRailItems(
    railExpanded: Boolean,
    filter: ArticleFilter,
    readLaterFeed: Feed?,
    onFilterSelect: () -> Unit,
    onSelectToday: () -> Unit,
    onSelectFeed: (feed: Feed, folderTitle: String?) -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    WideNavigationRailItem(
        railExpanded = railExpanded,
        icon = { Icon(imageVector = filter.status.icon, contentDescription = null) },
        label = { Text(stringResource(filter.status.navigationTitle)) },
        selected = filter.hasArticlesSelected(),
        onClick = onFilterSelect,
    )
    WideNavigationRailItem(
        railExpanded = railExpanded,
        icon = { Icon(imageVector = Icons.Rounded.Today, contentDescription = null) },
        label = { Text(stringResource(R.string.filter_today)) },
        selected = filter.hasTodaySelected(),
        onClick = onSelectToday,
    )
    if (readLaterFeed != null) {
        WideNavigationRailItem(
            railExpanded = railExpanded,
            icon = { Icon(imageVector = Icons.Rounded.Bookmark, contentDescription = null) },
            label = { Text(stringResource(R.string.filter_read_later)) },
            selected = filter.isFeedSelected(readLaterFeed),
            onClick = { onSelectFeed(readLaterFeed, null) },
        )
    }
    WideNavigationRailItem(
        railExpanded = railExpanded,
        icon = { Icon(imageVector = Icons.Rounded.Settings, contentDescription = null) },
        label = { Text(stringResource(R.string.settings_top_bar_title)) },
        selected = false,
        onClick = onNavigateToSettings,
    )
}
