package com.capyreader.app.ui.articles

import android.content.res.Configuration
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.capyreader.app.R
import com.capyreader.app.common.ImagePreview
import com.capyreader.app.preferences.AppTheme
import com.capyreader.app.ui.articles.list.ArticleActionMenu
import com.capyreader.app.ui.articles.list.ArticleListItem
import com.capyreader.app.ui.articles.list.ArticleRowSwipeBox
import com.capyreader.app.ui.fixtures.ArticleSample
import com.capyreader.app.ui.fixtures.PreviewKoinApplication
import com.capyreader.app.ui.theme.CapyTheme
import com.capyreader.app.ui.theme.LocalAppTheme
import com.jocmp.capy.Article
import com.jocmp.capy.EnclosureType
import com.jocmp.capy.MarkRead
import com.jocmp.capy.articles.relativeTime
import java.net.URL
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

data class ArticleRowOptions(
    val showIcon: Boolean = true,
    val showSummary: Boolean = true,
    val showFeedName: Boolean = true,
    val imagePreview: ImagePreview = ImagePreview.default,
    val fontScale: ArticleListFontScale = ArticleListFontScale.MEDIUM,
    val shortenTitles: Boolean = true,
)

@Composable
fun ArticleRow(
    article: Article,
    index: Int,
    selected: Boolean,
    onSelect: (articleID: String) -> Unit,
    onMarkAllRead: (range: MarkRead) -> Unit = {},
    currentTime: LocalDateTime,
    options: ArticleRowOptions = ArticleRowOptions(),
) {
    val imageURL = article.imageURL
    val isMonochrome = LocalAppTheme.current == AppTheme.MONOCHROME
    val deEmphasizeFontWeight = article.read && isMonochrome
    val colors = listItemColors(
        selected = selected,
        read = article.read
    )
    val feedNameColor = findFeedNameColor(read = article.read)
    val haptics = LocalHapticFeedback.current
    val (isArticleMenuOpen, setArticleMenuOpen) = remember { mutableStateOf(false) }
    val labelsActions = LocalLabelsActions.current
    val openArticleMenu = {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        setArticleMenuOpen(true)
    }

    StyleProviders(options) {
        ArticleBox(
            onClick = { onSelect(article.id) },
            onLongClick = openArticleMenu,
            article = article,
        ) {
            ArticleListItem(
                headlineContent = {
                    if (article.title.isNotBlank()) {
                        Text(
                            article.title,
                            maxLines = if (options.shortenTitles) 3 else Int.MAX_VALUE,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = if (deEmphasizeFontWeight) FontWeight.Light else FontWeight.Bold,
                        )
                    }
                },
                overlineContent = {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 2.dp)
                    ) {

                        if (options.showFeedName) {
                            Text(
                                text = article.feedName,
                                color = feedNameColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = if (deEmphasizeFontWeight) FontWeight.Light else null,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(16.dp))
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (article.starred) {
                                Icon(
                                    Icons.Rounded.Star,
                                    contentDescription = null,
                                    tint = feedNameColor,
                                    modifier = Modifier
                                        .width(12.dp.relative(options.fontScale))
                                )
                            }
                            if (article.enclosureType == EnclosureType.AUDIO) {
                                Icon(
                                    Icons.Rounded.PlayArrow,
                                    contentDescription = null,
                                    tint = feedNameColor,
                                    modifier = Modifier
                                        .width(16.dp.relative(options.fontScale))
                                        .padding(end = 2.dp)
                                )
                            }
                            Text(
                                text = relativeTime(
                                    time = article.publishedAt,
                                    currentTime = currentTime,
                                ),
                                color = feedNameColor,
                                maxLines = 1,
                            )
                        }
                    }
                },
                supportingContent = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        if (article.summary.isNotBlank() && options.showSummary) {
                            Text(
                                text = article.summary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = if (deEmphasizeFontWeight) FontWeight.Light else null,
                            )
                        }
                        if (imageURL != null && options.imagePreview == ImagePreview.LARGE) {
                            ArticleImage(imageURL = imageURL, imagePreview = options.imagePreview)
                        }
                    }
                },
                leadingContent = if (options.showIcon) {
                    {
                        FaviconBadge(article.faviconURL)
                    }
                } else {
                    null
                },

                trailingContent = if (imageURL != null && options.imagePreview.showInline()) {
                    {
                        ArticleImage(
                            imageURL = imageURL,
                            imagePreview = options.imagePreview
                        )
                    }
                } else {
                    null
                },
                colors = colors
            )

            ArticleActionMenu(
                expanded = isArticleMenuOpen,
                article = article,
                index = index,
                showLabels = labelsActions.showLabels,
                onMarkAllRead = {
                    setArticleMenuOpen(false)
                    onMarkAllRead(it)
                },
                onOpenLabels = {
                    setArticleMenuOpen(false)
                    labelsActions.openSheet(article.id)
                },
                onDismissRequest = {
                    setArticleMenuOpen(false)
                }
            )
        }
    }
}

@Composable
private fun ArticleImage(
    imageURL: String,
    imagePreview: ImagePreview
) {
    val sizeModifier = when (imagePreview) {
        ImagePreview.SMALL -> {
            Modifier.size(SMALL_IMAGE_SIZE)
        }

        ImagePreview.MEDIUM -> {
            Modifier.size(MEDIUM_IMAGE_SIZE)
        }

        else -> {
            Modifier
                .fillMaxWidth()
                .aspectRatio(3 / 2f)
                .clip(RoundedCornerShape(8.dp))
        }
    }

    AsyncImage(
        model = imageURL,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = sizeModifier
            .background(colorScheme.surfaceContainer)
    )
}

@Composable
fun PlaceholderArticleRow(imagePreview: ImagePreview = ImagePreview.NONE) {
    ListItem(
        supportingContent = {
            if (imagePreview == ImagePreview.LARGE) {
                Surface(
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(3 / 2f)
                        .background(colorScheme.surfaceContainer)
                        .clip(RoundedCornerShape(8.dp))
                ) {}
            }
        },
        trailingContent = {
            if (imagePreview.showInline()) {
                Box(
                    Modifier
                        .background(colorScheme.surfaceContainer)
                        .size(
                            when (imagePreview) {
                                ImagePreview.MEDIUM -> MEDIUM_IMAGE_SIZE
                                else -> SMALL_IMAGE_SIZE
                            }
                        )
                )
            }
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
    val isMonochrome = LocalAppTheme.current == AppTheme.MONOCHROME
    val dimColors = read && !isMonochrome

    return ListItemDefaults.colors(
        containerColor = if (selected) colorScheme.surfaceVariant else defaults.containerColor,
        headlineColor = if (dimColors) defaults.disabledContentColor else defaults.headlineColor,
        supportingColor = if (dimColors) defaults.disabledContentColor else defaults.supportingTextColor
    )
}

@Composable
fun findFeedNameColor(read: Boolean): Color {
    val defaults = ListItemDefaults.colors()
    val isMonochrome = LocalAppTheme.current == AppTheme.MONOCHROME

    return if (read && !isMonochrome) {
        defaults.disabledHeadlineColor
    } else {
        defaults.overlineColor
    }
}

@Composable
fun StyleProviders(options: ArticleRowOptions, content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalTextStyle provides LocalTextStyle.current.copy(textDirection = TextDirection.Content),
        LocalDensity provides Density(
            LocalDensity.current.density,
            options.fontScale.withLocaleDensity()
        )
    ) {
        content()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ArticleBox(
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    article: Article,
    content: @Composable () -> Unit
) {
    ArticleRowSwipeBox(article) {
        Box(
            Modifier
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                    onLongClickLabel = stringResource(R.string.article_actions_open_menu)
                ),
        ) {
            content()
        }
    }
}

private val SMALL_IMAGE_SIZE = 56.dp
private val MEDIUM_IMAGE_SIZE = 84.dp

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
        imageURL = "https://example.com",
        summary = "The Galaxy S24 series, while bringing little physical change, packs a lot of AI narrative. One of the biggest Galaxy S24 features is the AI Generative Edit",
        url = URL("https://9to5google.com/?p=605559"),
        updatedAt = ZonedDateTime.of(2024, 2, 11, 8, 33, 0, 0, ZoneOffset.UTC),
        publishedAt = ZonedDateTime.of(2024, 2, 11, 8, 33, 0, 0, ZoneOffset.UTC),
        read = true,
        starred = false,
        enclosureType = EnclosureType.AUDIO,
        feedName = "9to5Google - Google news, Pixel, Android, Home, Chrome OS, more"
    )

    PreviewKoinApplication {
        CapyTheme {
            Column {
                ArticleRow(
                    article = article,
                    index = 0,
                    selected = true,
                    onSelect = {},
                    currentTime = LocalDateTime.now(),
                )
                ArticleRow(
                    article = article.copy(read = false),
                    index = 0,
                    selected = false,
                    onSelect = {},
                    currentTime = LocalDateTime.now(),
                )
            }
        }
    }
}

@Preview
@Composable
fun ArticleRowPreview_Selected(@PreviewParameter(ArticleSample::class) article: Article) {
    PreviewKoinApplication {
        CapyTheme {
            ArticleRow(
                article = article,
                index = 0,
                selected = true,
                onSelect = {},
                currentTime = LocalDateTime.now(),
            )
        }
    }
}

@Preview
@Composable
fun ArticleRowPreview_Large(@PreviewParameter(ArticleSample::class) article: Article) {
    PreviewKoinApplication {
        CapyTheme {
            ArticleRow(
                article = article.copy(imageURL = "http://example.com"),
                index = 0,
                selected = true,
                onSelect = {},
                currentTime = LocalDateTime.now(),
                options = ArticleRowOptions(
                    imagePreview = ImagePreview.LARGE
                )
            )
        }
    }
}


@Preview
@Composable
fun ArticleRowPreview_Medium(@PreviewParameter(ArticleSample::class) article: Article) {
    PreviewKoinApplication {
        CapyTheme {
            ArticleRow(
                article = article.copy(
                    imageURL = "http://example.com",
                    starred = true,
                    enclosureType = EnclosureType.AUDIO
                ),
                index = 0,
                selected = true,
                onSelect = {},
                currentTime = LocalDateTime.now(),
                options = ArticleRowOptions(
                    imagePreview = ImagePreview.MEDIUM,
                )
            )
        }
    }
}

@Preview
@Composable
fun ArticleRowPreview_LargeText(@PreviewParameter(ArticleSample::class) article: Article) {
    PreviewKoinApplication {
        CapyTheme {
            ArticleRow(
                article = article.copy(imageURL = "http://example.com"),
                index = 0,
                selected = true,
                onSelect = {},
                currentTime = LocalDateTime.now(),
                options = ArticleRowOptions(
                    imagePreview = ImagePreview.LARGE,
                    fontScale = ArticleListFontScale.MEDIUM
                ),
            )
        }
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
        imageURL = "http://example.com",
        summary = "Test article here",
        url = URL("https://9to5google.com/?p=605559"),
        updatedAt = ZonedDateTime.of(2024, 2, 11, 8, 33, 0, 0, ZoneOffset.UTC),
        publishedAt = ZonedDateTime.of(LocalDateTime.now().minusHours(1), ZoneOffset.UTC),
        read = false,
        starred = false,
        feedName = "9to5Google"
    )

    Column {
        ArticleRow(
            article = article,
            index = 0,
            selected = false,
            onSelect = {},
            currentTime = LocalDateTime.now(),
        )
        ArticleRow(
            article = article.copy(imageURL = null),
            index = 0,
            selected = false,
            onSelect = {},
            currentTime = LocalDateTime.now(),
        )
        ArticleRow(
            article = article,
            index = 0,
            selected = false,
            onSelect = {},
            currentTime = LocalDateTime.now(),
            options = ArticleRowOptions(showFeedName = false)
        )
        ArticleRow(
            article = article,
            index = 0,
            selected = false,
            onSelect = {},
            currentTime = LocalDateTime.now(),
            options = ArticleRowOptions(showFeedName = false, imagePreview = ImagePreview.NONE)
        )
        ArticleRow(
            article = article,
            index = 0,
            selected = false,
            onSelect = {},
            currentTime = LocalDateTime.now(),
            options = ArticleRowOptions(
                showFeedName = false,
                showIcon = false,
                imagePreview = ImagePreview.NONE
            )
        )
    }
}

private fun Dp.relative(scale: ArticleListFontScale): Dp {
    return this * scale.relative
}

@Preview(locale = "ar")
@Composable
fun ArticleRowPreview_Rtl() {
    val article = Article(
        id = "288",
        feedID = "123",
        title = "شکایت یِلپ (Yelp) از گوگل: نبردی بر سر نتایج جستجوی محلی",
        author = "Andrew Romero",
        contentHTML = "",
        imageURL = "http://example.com",
        summary = "یِلپ (Yelp) اخیراً از گوگل شکایت کرده و مدعی شده که این غول فناوری از تسلط خود بر جستجوی عمومی به نفع خود در خدمات جستجوی محلی سوءاستفاده میکند. این پرونده در دادگاه فدرال سان200Cفرانسیسکو ثبت شده و هدف از آن دریافت غرامت مالی و صدور حکمی برای جلوگیری از آنچه یِلپ به عنوان اقداما",
        url = URL("https://www.1pezeshk.com/feed"),
        updatedAt = ZonedDateTime.of(2024, 2, 11, 8, 33, 0, 0, ZoneOffset.UTC),
        publishedAt = ZonedDateTime.of(LocalDateTime.now().minusHours(1), ZoneOffset.UTC),
        read = false,
        starred = false,
        feedName = "یک پزشک"
    )

    ArticleRow(
        article = article,
        index = 0,
        selected = false,
        onSelect = {},
        currentTime = LocalDateTime.now(),
    )
}

@Preview
@Composable
fun ArticleRowPreviewPlaceholder() {
    CapyTheme {
        PlaceholderArticleRow(ImagePreview.LARGE)
    }
}
