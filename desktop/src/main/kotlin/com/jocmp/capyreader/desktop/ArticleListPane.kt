package com.jocmp.capyreader.desktop

import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jocmp.capy.Article
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

private val timeFormatter = DateTimeFormatter.ofPattern("MMM d")

@Composable
fun ArticleListPane(
    state: ReaderState,
    width: Dp,
    sidebarCollapsed: Boolean = false,
    onToggleSidebar: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val articles by state.articles.collectAsDesktopState()
    val selectedArticle by state.selectedArticle.collectAsDesktopState()
    val canLoadMore by state.canLoadMore.collectAsDesktopState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            canLoadMore && lastVisible >= totalItems - 10
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            state.loadMore()
        }
    }

    Surface(
        modifier = modifier.fillMaxHeight().width(width),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column {
            if (sidebarCollapsed) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onToggleSidebar, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Rounded.Menu, contentDescription = "Show sidebar", modifier = Modifier.size(20.dp))
                    }
                }
                HorizontalDivider()
            }

            if (articles.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No articles",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(state = listState, modifier = Modifier.weight(1f)) {
                    items(articles, key = { it.id }) { article ->
                        ContextMenuArea(
                        items = {
                            buildList {
                                add(ContextMenuItem(
                                    label = if (article.read) "Mark as Unread" else "Mark as Read",
                                ) {
                                    scope.launch {
                                        if (article.read) {
                                            state.account.markUnread(article.id)
                                        } else {
                                            state.account.markRead(article.id)
                                        }
                                        state.loadArticles()
                                    }
                                })
                                add(ContextMenuItem(
                                    label = if (article.starred) "Unstar" else "Star",
                                ) {
                                    scope.launch {
                                        if (article.starred) {
                                            state.account.removeStar(article.id)
                                        } else {
                                            state.account.addStar(article.id)
                                        }
                                        state.loadArticles()
                                    }
                                })
                                article.url?.let { url ->
                                    add(ContextMenuItem("Open in Browser") {
                                        try {
                                            java.awt.Desktop.getDesktop().browse(java.net.URI(url.toString()))
                                        } catch (_: Exception) {}
                                    })
                                }
                            }
                        },
                    ) {
                        ArticleRow(
                            article = article,
                            selected = selectedArticle?.id == article.id,
                            onClick = { state.selectArticle(article) },
                        )
                    }
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    )
                }
            }
            }
        }
    }
}

@Composable
private fun ArticleRow(
    article: Article,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val bgColor = when {
        selected -> MaterialTheme.colorScheme.secondaryContainer
        !article.read -> MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
    }
    val textAlpha = if (article.read && !selected) 0.65f else 1f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (!article.read) {
            Icon(
                imageVector = Icons.Rounded.Circle,
                contentDescription = "Unread",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(8.dp).padding(top = 6.dp),
            )
        } else {
            Box(modifier = Modifier.size(8.dp))
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = article.feedName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = textAlpha),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (article.starred) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = "Starred",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp),
                        )
                    }
                    Text(
                        text = article.publishedAt.format(timeFormatter),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = textAlpha),
                    )
                }
            }

            Text(
                text = article.title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (!article.read) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = textAlpha),
            )

            if (article.summary.isNotBlank()) {
                Text(
                    text = article.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = textAlpha * 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
