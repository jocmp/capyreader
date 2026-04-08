package com.capyreader.app.ui.settings.panels

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.ui.articles.FaviconBadge
import com.jocmp.capy.Feed
import java.net.URI

@Composable
fun SubscriptionsSettingsPanel(
    feeds: List<Feed>,
    onSelectAll: () -> Unit,
    onSelectNone: () -> Unit,
    onToggleNotifications: (feedID: String, enabled: Boolean) -> Unit,
    onNavigateToDetail: (feedID: String) -> Unit,
) {
    val selectedIds = remember { mutableStateListOf<String>() }

    val groupState = remember(selectedIds.size, feeds.size) {
        when {
            selectedIds.isEmpty() -> ToggleableState.Off
            selectedIds.size == feeds.size -> ToggleableState.On
            else -> ToggleableState.Indeterminate
        }
    }

    val onGroupClick: () -> Unit = {
        when (groupState) {
            ToggleableState.On -> selectedIds.clear()
            else -> {
                selectedIds.clear()
                selectedIds.addAll(feeds.map { it.id })
            }
        }
    }

    val selectedFeeds = feeds.filter { it.id in selectedIds }
    val allSelectedEnabled = selectedFeeds.isNotEmpty() && selectedFeeds.all { it.enableNotifications }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = selectedIds.isNotEmpty(),
                enter = slideInVertically { it },
                exit = slideOutVertically { it },
            ) {
                BottomAppBar {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        TextButton(
                            onClick = {
                                selectedIds.forEach { id ->
                                    onToggleNotifications(id, !allSelectedEnabled)
                                }
                                selectedIds.clear()
                            }
                        ) {
                            Text(
                                if (allSelectedEnabled) {
                                    stringResource(R.string.subscriptions_disable_notifications)
                                } else {
                                    stringResource(R.string.subscriptions_enable_notifications)
                                }
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding)
        ) {
            item(key = "select_all") {
                SelectAllHeader(
                    groupState = groupState,
                    onClick = onGroupClick,
                )
            }
            items(feeds, key = { it.id }) { feed ->
                SubscriptionRow(
                    feed = feed,
                    isSelected = feed.id in selectedIds,
                    onToggleSelected = { checked ->
                        if (checked) {
                            selectedIds.add(feed.id)
                        } else {
                            selectedIds.remove(feed.id)
                        }
                    },
                    onNavigateToDetail = { onNavigateToDetail(feed.id) },
                )
            }
            item {
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SelectAllHeader(
    groupState: ToggleableState,
    onClick: () -> Unit,
) {
    Box(
        Modifier.clickable { onClick() }
    ) {
        ListItem(
            headlineContent = {
                val text = when (groupState) {
                    ToggleableState.On -> stringResource(R.string.settings_select_none)
                    else -> stringResource(R.string.settings_select_all)
                }
                Text(text, fontWeight = FontWeight.Medium)
            },
            trailingContent = {
                TriStateCheckbox(
                    state = groupState,
                    onClick = onClick
                )
            }
        )
    }
}

@Composable
private fun SubscriptionRow(
    feed: Feed,
    isSelected: Boolean,
    onToggleSelected: (Boolean) -> Unit,
    onNavigateToDetail: () -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable { onToggleSelected(!isSelected) },
        leadingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = onToggleSelected,
                )
                Spacer(Modifier.width(4.dp))
                FaviconBadge(feed.faviconURL)
            }
        },
        headlineContent = {
            Text(
                feed.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent = {
            Text(
                formatFeedURL(feed.feedURL),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingContent = {
            IconButton(onClick = onNavigateToDetail) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                )
            }
        },
    )
}

private fun formatFeedURL(url: String): String {
    return try {
        val uri = URI(url)
        val host = uri.host?.removePrefix("www.").orEmpty()
        val path = uri.path?.trimStart('/')?.trimEnd('/').orEmpty()
        if (path.isNotEmpty()) {
            "$host \u203A ${path.replace("/", " \u203A ")}"
        } else {
            host
        }
    } catch (_: Exception) {
        url
    }
}
