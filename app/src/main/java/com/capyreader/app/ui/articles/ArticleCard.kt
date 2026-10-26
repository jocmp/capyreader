package com.capyreader.app.ui.articles

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import coil3.compose.AsyncImage
import com.capyreader.app.R
import com.capyreader.app.common.ImagePreview
import com.capyreader.app.preferences.AppTheme
import com.capyreader.app.ui.LocalTimeFormats
import com.capyreader.app.ui.articles.list.ArticleRowSwipeBox
import com.capyreader.app.ui.theme.CapyTheme
import com.capyreader.app.ui.theme.LocalAppTheme
import com.jocmp.capy.Article
import com.jocmp.capy.EnclosureType
import com.jocmp.capy.MarkRead
import com.jocmp.capy.articles.relativeTime
import java.time.LocalDateTime

fun cardColumnCount(width: Dp): Int {
    val available = width - CARD_LIST_PADDING * 2 + CARD_GUTTER
    val columns = (available / (MIN_CARD_WIDTH + CARD_GUTTER)).toInt()

    return columns.coerceIn(1, MAX_CARD_COLUMNS)
}

@Composable
fun ArticleCardRow(
    articles: LazyPagingItems<Article>,
    row: Int,
    columns: ArticleColumns,
    selectedArticleKey: String?,
    onSelect: (article: Article) -> Unit,
    onMarkAllRead: (range: MarkRead) -> Unit,
    currentTime: LocalDateTime,
    options: ArticleRowOptions,
    modifier: Modifier = Modifier,
) {
    val first = columns.firstArticleIn(row)

    Row(
        horizontalArrangement = Arrangement.spacedBy(CARD_GUTTER),
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = CARD_LIST_PADDING, vertical = CARD_GUTTER / 2)
    ) {
        (first..columns.lastArticleIn(row)).forEach { index ->
            val cardModifier = Modifier
                .weight(1f)
                .fillMaxHeight()

            if (index >= articles.itemCount) {
                Spacer(modifier = cardModifier)
                return@forEach
            }

            val item = articles[index]

            if (item == null) {
                PlaceholderArticleCard(
                    showImage = options.imagePreview != ImagePreview.NONE,
                    modifier = cardModifier,
                )
            } else {
                ArticleCard(
                    article = item,
                    index = index,
                    selected = selectedArticleKey == item.id,
                    onSelect = { onSelect(item) },
                    onMarkAllRead = onMarkAllRead,
                    currentTime = currentTime,
                    options = options,
                    modifier = cardModifier,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArticleCard(
    article: Article,
    index: Int,
    selected: Boolean,
    onSelect: (articleID: String) -> Unit,
    onMarkAllRead: (range: MarkRead) -> Unit,
    currentTime: LocalDateTime,
    options: ArticleRowOptions,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    val (isArticleMenuOpen, setArticleMenuOpen) = remember { mutableStateOf(false) }

    StyleProviders(options = options) {
        ArticleRowSwipeBox(
            article = article,
            modifier = modifier.clip(MaterialTheme.shapes.medium),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                ArticleCardSurface(
                    selected = selected,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    ArticleCardContent(
                        article = article,
                        currentTime = currentTime,
                        options = options,
                        image = cardImage(article = article, options = options),
                        modifier = Modifier.combinedClickable(
                            onClick = { onSelect(article.id) },
                            onLongClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                setArticleMenuOpen(true)
                            },
                            onLongClickLabel = stringResource(R.string.article_actions_open_menu),
                        ),
                    )
                }

                ArticleRowActionMenu(
                    article = article,
                    index = index,
                    expanded = isArticleMenuOpen,
                    onDismissRequest = { setArticleMenuOpen(false) },
                    onMarkAllRead = onMarkAllRead,
                )
            }
        }
    }
}

@Composable
fun ArticleCardSurface(
    selected: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val isMonochrome = LocalAppTheme.current.value == AppTheme.MONOCHROME

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = cardContainerColor(selected = selected, isMonochrome = isMonochrome),
        border = cardBorder(selected = selected, isMonochrome = isMonochrome),
        modifier = modifier,
    ) {
        content()
    }
}

@Composable
fun ArticleCardContent(
    article: Article,
    currentTime: LocalDateTime,
    options: ArticleRowOptions,
    image: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val isMonochrome = LocalAppTheme.current.value == AppTheme.MONOCHROME
    val dim = article.read && options.dim
    val deEmphasizeFontWeight = dim && isMonochrome
    val colors = ListItemDefaults.colors()
    val dimColors = dim && !isMonochrome
    val feedNameColor = findFeedNameColor(
        faviconURL = article.faviconURL,
        accentColors = options.accentColors,
        read = dim,
    )

    Column(modifier = modifier.fillMaxSize()) {
        image?.invoke()

        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .weight(1f)
                .padding(12.dp)
        ) {
            if (article.title.isNotBlank()) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = titleFontWeight(deEmphasizeFontWeight),
                    color = if (dimColors) colors.disabledContentColor else colors.contentColor,
                    maxLines = titleMaxLines(options.shortenTitles),
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (article.summary.isNotBlank() && options.showSummary) {
                Text(
                    text = article.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = bodyFontWeight(deEmphasizeFontWeight),
                    color = if (dimColors) colors.disabledContentColor else colors.supportingContentColor,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            CardFooter(
                article = article,
                currentTime = currentTime,
                options = options,
                color = feedNameColor,
                fontWeight = bodyFontWeight(deEmphasizeFontWeight),
            )
        }
    }
}

@Composable
fun ArticleCardImage(imageURL: String?) {
    val modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(CARD_IMAGE_ASPECT_RATIO)
        .background(colorScheme.surfaceContainerHigh)

    if (imageURL == null) {
        Box(modifier = modifier)
        return
    }

    AsyncImage(
        model = imageURL,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier,
    )
}

@Composable
fun PlaceholderArticleCard(
    showImage: Boolean,
    modifier: Modifier = Modifier,
) {
    ArticleCardSurface(selected = false, modifier = modifier) {
        Column {
            if (showImage) {
                ArticleCardImage(imageURL = null)
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(12.dp),
            ) {
                PlaceholderBar(fraction = 0.9f, height = 14.dp)
                PlaceholderBar(fraction = 0.6f, height = 14.dp)
                Spacer(modifier = Modifier.height(8.dp))
                PlaceholderBar(fraction = 0.4f, height = 12.dp)
            }
        }
    }
}

@Composable
private fun CardFooter(
    article: Article,
    currentTime: LocalDateTime,
    options: ArticleRowOptions,
    color: Color,
    fontWeight: FontWeight?,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (options.showIcon) {
            FaviconBadge(url = article.faviconURL)
        }

        if (options.showFeedName) {
            Text(
                text = article.displayFeedName(LocalContext.current),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = fontWeight,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        if (article.starred) {
            Icon(
                imageVector = Icons.Rounded.Star,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(12.dp),
            )
        }

        if (article.enclosureType == EnclosureType.AUDIO) {
            Icon(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp),
            )
        }

        Text(
            text = relativeTime(
                time = article.publishedAt,
                currentTime = currentTime,
                formats = LocalTimeFormats.current,
            ),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = fontWeight,
            color = color,
            maxLines = 1,
        )
    }
}

@Composable
private fun PlaceholderBar(fraction: Float, height: Dp) {
    Box(
        Modifier
            .fillMaxWidth(fraction)
            .height(height)
            .clip(RoundedCornerShape(4.dp))
            .background(colorScheme.surfaceContainerHigh)
    )
}

private fun cardImage(article: Article, options: ArticleRowOptions): (@Composable () -> Unit)? {
    val imageURL = article.imageURL

    if (imageURL == null || options.imagePreview == ImagePreview.NONE) {
        return null
    }

    return { ArticleCardImage(imageURL = imageURL) }
}

@Composable
private fun cardContainerColor(selected: Boolean, isMonochrome: Boolean): Color {
    if (selected && !isMonochrome) {
        return colorScheme.secondaryContainer
    }

    return colorScheme.surfaceContainerLow
}

@Composable
private fun cardBorder(selected: Boolean, isMonochrome: Boolean): BorderStroke? {
    if (!isMonochrome) {
        return null
    }

    if (selected) {
        return BorderStroke(width = 2.dp, color = colorScheme.onSurface)
    }

    return BorderStroke(width = 1.dp, color = colorScheme.outlineVariant)
}

private fun titleFontWeight(deEmphasize: Boolean): FontWeight {
    if (deEmphasize) {
        return FontWeight.Light
    }

    return FontWeight.SemiBold
}

private fun bodyFontWeight(deEmphasize: Boolean): FontWeight? {
    if (deEmphasize) {
        return FontWeight.Light
    }

    return null
}

private fun titleMaxLines(shortenTitles: Boolean): Int {
    if (shortenTitles) {
        return 4
    }

    return Int.MAX_VALUE
}

private val MIN_CARD_WIDTH = 180.dp
private val CARD_GUTTER = 8.dp
private val CARD_LIST_PADDING = 8.dp
private const val MAX_CARD_COLUMNS = 4
private const val CARD_IMAGE_ASPECT_RATIO = 16 / 9f

@Preview
@Composable
private fun ArticleCardPreview() {
    CapyTheme {
        PlaceholderArticleCard(
            showImage = true,
            modifier = Modifier.width(200.dp),
        )
    }
}
