package com.capyreader.app.ui.articles

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jocmp.capy.Article
import com.jocmp.capy.MarkRead
import com.capyreader.app.R
import com.capyreader.app.ui.articles.list.ArticleActionMenu
import com.capyreader.app.ui.fixtures.ArticleSample
import com.capyreader.app.ui.theme.CapyTheme
import java.net.URL
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime


private val THUMBNAIL_SIZE = 56.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArticleRow(
    article: Article,
    selected: Boolean,
    onSelect: (articleID: String) -> Unit,
    onMarkAllRead: (range: MarkRead) -> Unit = {},
    currentTime: LocalDateTime,
) {
    val imageURL = article.imageURL?.toString()
    val colors = listItemColors(
        selected = selected,
        read = article.read
    )
    val feedNameColor = findFeedNameColor(read = article.read)
    val haptics = LocalHapticFeedback.current
    val (isArticleMenuOpen, setArticleMenuOpen) = remember { mutableStateOf(false) }

    val openArticleMenu = {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        setArticleMenuOpen(true)
    }

    Box(
        Modifier
            .combinedClickable(
                onClick = { onSelect(article.id) },
                onLongClick = openArticleMenu,
                onLongClickLabel = stringResource(R.string.article_actions_open_menu)
            )
    ) {
        ListItem(
            leadingContent = {
                Box(Modifier.padding(top = 6.dp)) {
                    FaviconBadge(article.faviconURL)
                }
            },
            headlineContent = {
                Text(
                    article.title,
                )
            },
            supportingContent = {
                Column {
                    Text(
                        article.feedName,
                        color = feedNameColor,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    if (article.summary.isNotBlank()) {
                        Text(
                            text = article.summary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        text = relativeTime(
                            time = article.publishedAt,
                            currentTime = currentTime,
                        ),
                        maxLines = 1,
                    )
                }
            },
            trailingContent = if (imageURL != null) {
                {
                    AsyncImage(
                        model = imageURL,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(THUMBNAIL_SIZE)
                            .background(colorScheme.surfaceContainer)
                    )
                }
            } else {
                null
            },
            colors = colors
        )

        ArticleActionMenu(
            expanded = isArticleMenuOpen,
            articleID = article.id,
            onMarkAllRead = {
                setArticleMenuOpen(false)
                onMarkAllRead(it)
            },
            onDismissRequest = {
                setArticleMenuOpen(false)
            }
        )
    }
}

@Composable
fun PlaceholderArticleRow() {
    ListItem(
        trailingContent = {
            Box(
                Modifier
                    .size(THUMBNAIL_SIZE)
                    .background(colorScheme.surfaceContainer)
            )
        },
        headlineContent = {}
    )
}


@Composable
@Stable
private fun listItemColors(
    selected: Boolean,
    read: Boolean,
): ListItemColors {
    val defaults = ListItemDefaults.colors()
    val colorScheme = MaterialTheme.colorScheme

    return ListItemDefaults.colors(
        containerColor = if (selected) colorScheme.surfaceVariant else defaults.containerColor,
        headlineColor = if (read) defaults.disabledHeadlineColor else defaults.headlineColor,
        supportingColor = if (read) defaults.disabledHeadlineColor else defaults.supportingTextColor
    )
}

@Composable
fun findFeedNameColor(read: Boolean): Color {
    val defaults = ListItemDefaults.colors()
    val colorScheme = MaterialTheme.colorScheme

    return if (read) {
        defaults.disabledHeadlineColor
    } else {
        colorScheme.onSurface
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun ArticleRowPreview_Selected_DarkMode() {
    val article = Article(
        id = "288",
        feedID = "123",
        title = "How to use the Galaxy S24's AI photo editing tool",
        author = "Andrew Romero",
        contentHTML = "<div>Test</div>",
        extractedContentURL = null,
        imageURL = URL("https://example.com"),
        summary = "Test article here",
        url = URL("https://9to5google.com/?p=605559"),
        updatedAt = ZonedDateTime.of(2024, 2, 11, 8, 33, 0, 0, ZoneOffset.UTC),
        publishedAt = ZonedDateTime.of(2024, 2, 11, 8, 33, 0, 0, ZoneOffset.UTC),
        read = true,
        starred = false,
        feedName = "9to5Google"
    )

    CapyTheme(dynamicColor = false) {
        Column {
            ArticleRow(
                article = article,
                selected = true,
                onSelect = {},
                currentTime = LocalDateTime.now(),
            )
            ArticleRow(
                article = article.copy(read = false),
                selected = false,
                onSelect = {},
                currentTime = LocalDateTime.now(),
            )
        }
    }
}

@Preview
@Composable
fun ArticleRowPreview_Selected(@PreviewParameter(ArticleSample::class) article: Article) {
    CapyTheme(dynamicColor = false) {
        ArticleRow(
            article = article,
            selected = true,
            onSelect = {},
            currentTime = LocalDateTime.now(),
        )
    }
}

@Preview
@Composable
fun ArticleRowPreview_Unread() {
    val article = Article(
        id = "288",
        feedID = "123",
        title = "How to use the Galaxy S24's AI photo editing tool",
        author = "Andrew Romero",
        contentHTML = "<div>Test</div>",
        extractedContentURL = null,
        imageURL = URL("http://example.com"),
        summary = "Test article here",
        url = URL("https://9to5google.com/?p=605559"),
        updatedAt = ZonedDateTime.of(2024, 2, 11, 8, 33, 0, 0, ZoneOffset.UTC),
        publishedAt = ZonedDateTime.of(2024, 2, 11, 8, 33, 0, 0, ZoneOffset.UTC),
        read = false,
        starred = false,
        feedName = "9to5Google"
    )

    ArticleRow(
        article = article,
        selected = false,
        onSelect = {},
        currentTime = LocalDateTime.now(),
    )
}
