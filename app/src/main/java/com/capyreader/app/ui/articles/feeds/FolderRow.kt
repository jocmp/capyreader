package com.capyreader.app.ui.articles.feeds

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.ui.articles.CountBadge
import com.capyreader.app.ui.articles.ListTitle
import com.capyreader.app.ui.articles.list.FolderActionMenu
import com.capyreader.app.ui.fixtures.FolderPreviewFixture
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.Feed
import com.jocmp.capy.Folder
import com.jocmp.capy.accounts.Source

@Composable
fun FolderRow(
    filter: ArticleFilter,
    folder: Folder,
    onFolderSelect: (folder: Folder) -> Unit,
    onFeedSelect: (feed: Feed) -> Unit,
    source: Source,
) {
    val actions = LocalFolderActions.current
    val isFolderSelected = filter.isFolderSelected(folder)
    val (expanded, setExpanded) = rememberSaveable(folder.title) { mutableStateOf(folder.expanded) }
    val (showMenu, setShowMenu) = remember { mutableStateOf(false) }

    fun onExpanded(expand: Boolean) {
        setExpanded(expand)
        actions.updateExpanded(folder.title, expand)
    }

    val showFolderBadge = folder.feeds.any { it.showUnreadBadge && it.count > 0 }

    Column {
        Box {
            DrawerItem(
                selected = isFolderSelected,
                onClick = { onFolderSelect(folder) },
                onLongClick = { setShowMenu(true) },
                badge = {
                    CountBadge(count = folder.count, showBadge = showFolderBadge, status = filter.status)
                },
                icon = {
                    IconDropdown(
                        expanded = expanded,
                        onClick = { onExpanded(!expanded) }
                    )
                },
                label = {
                    ListTitle(folder.title)
                },
            )

            FolderActionMenu(
                expanded = showMenu,
                folderTitle = folder.title,
                onDismissMenuRequest = { setShowMenu(false) },
                onRemoveRequest = { title, completion ->
                    actions.removeFolder(title, completion)
                },
                source = source,
            )
        }
        AnimatedVisibility(
            expanded,
            enter = expandVertically(expandFrom = Alignment.Top),
            exit = shrinkVertically(animationSpec = tween()),
        ) {
            Column {
                folder.feeds.forEach { feed ->
                    Row(Modifier.padding(start = 16.dp)) {
                        FeedRow(
                            feed = feed,
                            onSelect = { onFeedSelect(feed) },
                            selected = filter.isFeedSelected(feed),
                            status = filter.status,
                            source = source,
                        )
                    }
                }
            }
        }
    }
}


@Preview
@Composable
fun FolderRowPreview() {
    val folder = FolderPreviewFixture().values.take(1).first()
    val filter = ArticleFilter.Folders(
        folderTitle = folder.title,
        folderStatus = ArticleStatus.ALL
    )

    MaterialTheme {
        FolderRow(
            folder = folder,
            onFolderSelect = {},
            onFeedSelect = {},
            filter = filter,
            source = Source.LOCAL,
        )
    }
}
