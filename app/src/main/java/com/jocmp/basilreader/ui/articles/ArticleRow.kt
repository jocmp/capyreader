package com.jocmp.basilreader.ui.articles

import android.content.res.Configuration
import android.graphics.drawable.Drawable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.jocmp.basil.Article
import com.jocmp.basilreader.ui.theme.BasilReaderTheme
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.net.URL
import java.time.ZoneId
import java.time.ZonedDateTime

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ArticleRow(
    article: Article,
    selected: Boolean,
    onSelect: (articleID: String) -> Unit
) {
    val thumbnailSize = with(LocalDensity.current) { 56.dp.roundToPx()}
    val imageURL = article.imageURL?.toHttpUrlOrNull()?.toString()
    val colors = listItemColors(
        selected = selected,
        read = article.read
    )

    Box(
        Modifier.clickable {
            onSelect(article.id)
        }
    ) {
        ListItem(
            leadingContent = if (imageURL != null) {
                {
                    val background = ColorPainter(colorScheme.surfaceContainer)

                    GlideImage(
                        model = imageURL,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(56.dp)
                            .aspectRatio(1f),
                        loading = placeholder(background),
                        failure = placeholder(background),
                    ) {
                        it.override(thumbnailSize)
                    }
                }
            } else {
                null
            },
            headlineContent = {
                Text(
                    article.title,
                )
            },
            supportingContent = {
                Text(
                    text = article.summary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            colors = colors
        )
    }
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

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun ArticleRowPreview_Selected_DarkMode() {
    val article = Article(
        id = "288",
        feedID = "123",
        title = "How to use the Galaxy S24's AI photo editing tool",
        contentHTML = "<div>Test</div>",
        imageURL = URL("https://example.com"),
        summary = "Test article here",
        url = URL("https://9to5google.com/?p=605559"),
        arrivedAt = ZonedDateTime.of(2024, 2, 11, 8, 33, 0, 0, ZoneId.systemDefault()),
        read = true,
        starred = false,
    )

    BasilReaderTheme(dynamicColor = false) {
        Column {
            ArticleRow(
                article = article,
                selected = true,
                onSelect = {}
            )
            ArticleRow(
                article = article.copy(read = false),
                selected = false,
                onSelect = {}
            )
        }
    }
}

@Preview
@Composable
fun ArticleRowPreview_Selected() {
    val article = Article(
        id = "288",
        feedID = "123",
        title = "How to use the Galaxy S24's AI photo editing tool",
        contentHTML = "<div>Test</div>",
        imageURL = null,
        summary = "Test article here",
        url = URL("https://9to5google.com/?p=605559"),
        arrivedAt = ZonedDateTime.of(2024, 2, 11, 8, 33, 0, 0, ZoneId.systemDefault()),
        read = true,
        starred = false,
    )

    BasilReaderTheme(dynamicColor = false) {
        ArticleRow(
            article = article,
            selected = true,
            onSelect = {}
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
        contentHTML = "<div>Test</div>",
        imageURL = URL("http://example.com"),
        summary = "Test article here",
        url = URL("https://9to5google.com/?p=605559"),
        arrivedAt = ZonedDateTime.of(2024, 2, 11, 8, 33, 0, 0, ZoneId.systemDefault()),
        read = false,
        starred = false,
    )

    ArticleRow(
        article = article,
        selected = false,
        onSelect = {}
    )
}
