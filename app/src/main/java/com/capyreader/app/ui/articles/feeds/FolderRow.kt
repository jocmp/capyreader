package com.capyreader.app.ui.articles.feeds

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.ui.articles.CountBadge
import com.capyreader.app.ui.articles.ListTitle
import com.capyreader.app.ui.fixtures.FolderPreviewFixture
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.Feed
import com.jocmp.capy.Folder

@Composable
fun FolderRow(
    filter: ArticleFilter,
    folder: Folder,
    onFolderSelect: (folder: Folder) -> Unit,
    onFeedSelect: (feed: Feed) -> Unit,
) {
    val isFolderSelected = filter.isFolderSelect(folder)
    val (expanded, setExpanded) = rememberSaveable(folder.title) { mutableStateOf(false) }

    Column {
        NavigationDrawerItem(
            selected = isFolderSelected,
            onClick = { onFolderSelect(folder) },
            badge = { CountBadge(count = folder.count) },
            icon = {
                DropdownButton(
                    expanded = expanded,
                    onExpanded = setExpanded,
                )
            },
            label = {
                ListTitle(folder.title)
            }
        )
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
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DropdownButton(
    expanded: Boolean,
    onExpanded: (expanded: Boolean) -> Unit,
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 0f else -90f,
        label = "DropdownRotation"
    )

    FolderIconButton(onClick = { onExpanded(!expanded) }) {
        Icon(
            imageVector = Icons.Rounded.ArrowDropDown,
            contentDescription = null,
            modifier = Modifier
                .size(30.dp)
                .rotate(rotation)
        )
    }
}

@Composable
fun FolderIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
    val size = 40.dp
    Box(
        modifier = modifier
            .background(color = colors.containerColor)
            .clickable(
                onClick = onClick,
                enabled = enabled,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = ripple(
                    bounded = false,
                    radius = size / 2
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
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
            filter = filter
        )
    }
}
