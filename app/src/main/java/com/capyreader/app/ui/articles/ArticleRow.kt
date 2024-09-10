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
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.capyreader.app.R
import com.capyreader.app.common.ImagePreview
import com.capyreader.app.ui.articles.list.ArticleActionMenu
import com.capyreader.app.ui.articles.list.ArticleListItem
import com.capyreader.app.ui.fixtures.ArticleSample
import com.capyreader.app.ui.theme.CapyTheme
import com.jocmp.capy.Article
import com.jocmp.capy.MarkRead
import java.net.URL
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

private val THUMBNAIL_SIZE = 56.dp

data class ArticleRowOptions(
    val showIcon: Boolean = true,
    val showSummary: Boolean = true,
    val showFeedName: Boolean = true,
    val imagePreview: ImagePreview = ImagePreview.default,
    val fontScale: ArticleListFontScale = ArticleListFontScale.MEDIUM
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArticleRow(
    article: Article,
    selected: Boolean,
    onSelect: (articleID: String) -> Unit,
    onMarkAllRead: (range: MarkRead) -> Unit = {},
    currentTime: LocalDateTime,
    options: ArticleRowOptions = ArticleRowOptions(),
) {
    val imageURL = article.imageURL
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

    StyleProviders(options) {
        Box(
            Modifier
                .combinedClickable(
                    onClick = { onSelect(article.id) },
                    onLongClick = openArticleMenu,
                    onLongClickLabel = stringResource(R.string.article_actions_open_menu)
                )
        ) {
            ArticleListItem(
                overlineContent = {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
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
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(16.dp))
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
                },
                leadingContent = if (options.showIcon) {
                    {
                        FaviconBadge(article.faviconURL)
                    }
                } else {
                    null
                },
                headlineContent = {
                    Text(
                        article.title,
                        fontWeight = FontWeight.Bold,
                    )
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
                            )
                        }
                        if (imageURL != null && options.imagePreview == ImagePreview.LARGE) {
                            ArticleImage(imageURL = imageURL, imagePreview = options.imagePreview)
                        }
                    }
                },

                trailingContent = if (imageURL != null && options.imagePreview == ImagePreview.SMALL) {
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
}

@Composable
private fun ArticleImage(
    imageURL: String,
    imagePreview: ImagePreview
) {
    val sizeModifier = if (imagePreview == ImagePreview.SMALL) {
        Modifier.size(THUMBNAIL_SIZE)
    } else {
        Modifier
            .fillMaxWidth()
            .aspectRatio(3 / 2f)
            .clip(RoundedCornerShape(8.dp))
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
fun PlaceholderArticleRow(imagePreview: ImagePreview) {
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
            if (imagePreview == ImagePreview.SMALL) {
                Box(
                    Modifier
                        .size(THUMBNAIL_SIZE)
                        .background(colorScheme.surfaceContainer)
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

    return if (read) {
        defaults.disabledHeadlineColor
    } else {
        Color.Unspecified
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
        imageURL = "https://example.com",
        summary = "The Galaxy S24 series, while bringing little physical change, packs a lot of AI narrative. One of the biggest Galaxy S24 features is the AI Generative Edit",
        url = URL("https://9to5google.com/?p=605559"),
        updatedAt = ZonedDateTime.of(2024, 2, 11, 8, 33, 0, 0, ZoneOffset.UTC),
        publishedAt = ZonedDateTime.of(2024, 2, 11, 8, 33, 0, 0, ZoneOffset.UTC),
        read = true,
        starred = false,
        feedName = "9to5Google - Google news, Pixel, Android, Home, Chrome OS, more"
    )

    CapyTheme {
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
    CapyTheme {
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
fun ArticleRowPreview_Large(@PreviewParameter(ArticleSample::class) article: Article) {
    CapyTheme {
        ArticleRow(
            article = article.copy(imageURL = "http://example.com"),
            selected = true,
            onSelect = {},
            currentTime = LocalDateTime.now(),
            options = ArticleRowOptions(
                imagePreview = ImagePreview.LARGE
            )
        )
    }
}

@Preview
@Composable
fun ArticleRowPreview_LargeText(@PreviewParameter(ArticleSample::class) article: Article) {
    CapyTheme {
        ArticleRow(
            article = article.copy(imageURL = "http://example.com"),
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
            selected = false,
            onSelect = {},
            currentTime = LocalDateTime.now(),
        )
        ArticleRow(
            article = article.copy(imageURL = null),
            selected = false,
            onSelect = {},
            currentTime = LocalDateTime.now(),
        )
        ArticleRow(
            article = article,
            selected = false,
            onSelect = {},
            currentTime = LocalDateTime.now(),
            options = ArticleRowOptions(showFeedName = false)
        )
        ArticleRow(
            article = article,
            selected = false,
            onSelect = {},
            currentTime = LocalDateTime.now(),
            options = ArticleRowOptions(showFeedName = false, imagePreview = ImagePreview.NONE)
        )
        ArticleRow(
            article = article,
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

@Preview(locale = "ar")
@Composable
fun ArticleRowPreview_Rtl() {
    val article = Article(
        id = "288",
        feedID = "123",
        title = "شکایت یِلپ (Yelp) از گوگل: نبردی بر سر نتایج جستجوی محلی",
        author = "Andrew Romero",
        contentHTML = "",
        extractedContentURL = null,
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
