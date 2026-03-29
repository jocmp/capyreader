package com.jocmp.capyreader.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RssFeed
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.Feed
import com.jocmp.capy.Folder

@Composable
fun FeedSidebar(
    state: ReaderState,
    width: Dp,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val filter by state.filter.collectAsDesktopState()
    val folders by state.foldersWithCounts.collectAsDesktopState()
    val feeds by state.feedsWithCounts.collectAsDesktopState()
    val allUnread by state.allUnreadCount.collectAsDesktopState()
    val refreshing by state.refreshing.collectAsDesktopState()
    var showAddFeed by remember { mutableStateOf(false) }
    val expandedFolders = remember { mutableStateMapOf<String, Boolean>() }

    Surface(
        modifier = modifier.fillMaxHeight().width(width),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 10.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Capy Reader",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Row {
                    IconButton(onClick = { showAddFeed = true }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Rounded.Add, contentDescription = "Add feed", modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = { state.markAllRead() }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = "Mark all read", modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = { state.refresh() }, enabled = !refreshing, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Refresh", modifier = Modifier.size(18.dp))
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
            ) {
                item {
                    SidebarItem(
                        label = "All Articles",
                        icon = Icons.AutoMirrored.Rounded.Notes,
                        count = allUnread,
                        selected = filter.hasArticlesSelected() && filter.status == ArticleStatus.ALL,
                        onClick = { state.selectFilter(ArticleFilter.Articles(ArticleStatus.ALL)) },
                    )
                }

                item {
                    SidebarItem(
                        label = "Unread",
                        icon = Icons.Rounded.Circle,
                        count = allUnread,
                        selected = filter.hasArticlesSelected() && filter.status == ArticleStatus.UNREAD,
                        onClick = { state.selectFilter(ArticleFilter.Articles(ArticleStatus.UNREAD)) },
                    )
                }

                item {
                    SidebarItem(
                        label = "Starred",
                        icon = Icons.Rounded.Star,
                        count = 0,
                        selected = filter.hasArticlesSelected() && filter.status == ArticleStatus.STARRED,
                        onClick = { state.selectFilter(ArticleFilter.Articles(ArticleStatus.STARRED)) },
                    )
                }

                if (folders.isNotEmpty() || feeds.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(4.dp))
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "FEEDS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    }
                }

                items(folders, key = { "folder:${it.title}" }) { folder ->
                    val expanded = expandedFolders[folder.title] ?: folder.expanded
                    SidebarItem(
                        label = folder.title,
                        icon = Icons.Rounded.ArrowDropDown,
                        iconRotation = if (expanded) 0f else -90f,
                        count = folder.count,
                        selected = filter.isFolderSelected(folder),
                        onClick = {
                            state.selectFilter(
                                ArticleFilter.Folders(
                                    folderTitle = folder.title,
                                    folderStatus = filter.status,
                                )
                            )
                        },
                        onIconClick = { expandedFolders[folder.title] = !expanded },
                    )

                    if (expanded) {
                        folder.feeds.forEach { feed ->
                            SidebarItem(
                                label = feed.title,
                                icon = Icons.Rounded.RssFeed,
                                count = feed.count,
                                selected = filter.isFeedSelected(feed),
                                indent = 1,
                                onClick = {
                                    state.selectFilter(
                                        ArticleFilter.Feeds(
                                            feedID = feed.id,
                                            folderTitle = folder.title,
                                            feedStatus = filter.status,
                                        )
                                    )
                                },
                            )
                        }
                    }
                }

                items(feeds, key = { "feed:${it.id}" }) { feed ->
                    SidebarItem(
                        label = feed.title,
                        icon = Icons.Rounded.RssFeed,
                        count = feed.count,
                        selected = filter.isFeedSelected(feed),
                        onClick = {
                            state.selectFilter(
                                ArticleFilter.Feeds(
                                    feedID = feed.id,
                                    folderTitle = null,
                                    feedStatus = filter.status,
                                )
                            )
                        },
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))

            SidebarItem(
                label = "Sign Out",
                icon = Icons.AutoMirrored.Rounded.Logout,
                count = 0,
                selected = false,
                onClick = onSignOut,
            )

            Spacer(Modifier.height(8.dp))

            if (showAddFeed) {
                AddFeedDialog(
                    state = state,
                    onDismiss = { showAddFeed = false },
                )
            }

        }
    }
}

@Composable
private fun SidebarItem(
    label: String,
    icon: ImageVector,
    count: Long,
    selected: Boolean,
    indent: Int = 0,
    iconRotation: Float = 0f,
    onClick: () -> Unit,
    onIconClick: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(6.dp)
    val bgColor = if (selected) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (8 + indent * 16).dp, end = 8.dp, top = 1.dp, bottom = 1.dp)
            .clip(shape)
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .size(16.dp)
                .rotate(iconRotation)
                .then(if (onIconClick != null) Modifier.clickable { onIconClick() } else Modifier),
            tint = if (selected) {
                MaterialTheme.colorScheme.onSecondaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
            color = if (selected) {
                MaterialTheme.colorScheme.onSecondaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )

        if (count > 0) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
