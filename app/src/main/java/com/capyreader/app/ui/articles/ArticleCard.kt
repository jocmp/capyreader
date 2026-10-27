package com.capyreader.app.ui.articles

import androidx.collection.LruCache
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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

val ArticleCardGutter = 8.dp

fun cardColumnCount(availableWidth: Dp): Int {
    val columns = ((availableWidth + ArticleCardGutter) / (MIN_CARD_WIDTH + ArticleCardGutter)).toInt()

    return columns.coerceIn(1, MAX_CARD_COLUMNS)
}

object ArticleCardCells : StaggeredGridCells {
    override fun Density.calculateCrossAxisCellSizes(availableSize: Int, spacing: Int): IntArray {
        val count = cardColumnCount(availableSize.toDp())
        val gridSize = availableSize - spacing * (count - 1)
        val slotSize = gridSize / count
        val remainder = gridSize % count

        return IntArray(count) { slot ->
            if (slot < remainder) {
                slotSize + 1
            } else {
                slotSize
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
            Box {
                ArticleCardSurface(selected = selected) {
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

    Column(modifier = modifier) {
        image?.invoke()

        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(12.dp),
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
    if (imageURL == null) {
        Box(modifier = cardImageModifier(DEFAULT_IMAGE_ASPECT_RATIO))
        return
    }

    var aspectRatio by remember(imageURL) {
        mutableFloatStateOf(imageAspectRatios[imageURL] ?: DEFAULT_IMAGE_ASPECT_RATIO)
    }

    if (aspectRatio == FAILED_IMAGE) {
        return
    }

    AsyncImage(
        model = imageURL,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        onSuccess = { state ->
            naturalAspectRatio(state.painter.intrinsicSize)?.let { ratio ->
                imageAspectRatios.put(imageURL, ratio)
                aspectRatio = ratio
            }
        },
        onError = {
            imageAspectRatios.put(imageURL, FAILED_IMAGE)
            aspectRatio = FAILED_IMAGE
        },
        modifier = cardImageModifier(aspectRatio),
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
                modifier = Modifier.size(12.dp.relative(options.fontScale)),
            )
        }

        if (article.enclosureType == EnclosureType.AUDIO) {
            Icon(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp.relative(options.fontScale)),
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
private fun cardImageModifier(aspectRatio: Float): Modifier {
    return Modifier
        .fillMaxWidth()
        .aspectRatio(aspectRatio)
        .background(colorScheme.surfaceContainerHigh)
}

private fun naturalAspectRatio(size: Size): Float? {
    if (size == Size.Unspecified || size.width <= 0f || size.height <= 0f) {
        return null
    }

    return (size.width / size.height).coerceIn(MIN_IMAGE_ASPECT_RATIO, MAX_IMAGE_ASPECT_RATIO)
}

@Composable
private fun PlaceholderBar(fraction: Float, height: Dp) {
    Box(
        modifier = Modifier
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
private const val MAX_CARD_COLUMNS = 4
private const val DEFAULT_IMAGE_ASPECT_RATIO = 16 / 9f
private const val MIN_IMAGE_ASPECT_RATIO = 3 / 4f
private const val MAX_IMAGE_ASPECT_RATIO = 2f
private const val FAILED_IMAGE = 0f

private val imageAspectRatios = LruCache<String, Float>(500)

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
