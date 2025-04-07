package com.capyreader.app.ui.widget

import android.content.Context
import android.content.Intent
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.material3.ColorProviders
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.capyreader.app.MainActivity
import com.capyreader.app.R
import com.capyreader.app.notifications.NotificationHelper.Companion.UNREAD_ONLY_KEY
import com.jocmp.capy.Article
import java.net.URL
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

@Composable
fun HeadlinesLayout(articles: List<Article>) {
    val context = LocalContext.current
    val currentTime = remember { LocalDateTime.now() }

    Column(
        GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = GlanceModifier
                .padding(16.dp)
                .clickable(context.openUnread())
                .fillMaxWidth()
        ) {
            Image(
                provider = ImageProvider(R.drawable.capy_icon_inline),
                contentDescription = null,
                colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface),
                modifier = GlanceModifier
                    .size(24.dp)
            )
            Spacer(GlanceModifier.width(16.dp))
            Text(
                context.getString(R.string.widget_headlines_title),
                style = TextStyle(
                    fontSize = 18.sp,
                    color = GlanceTheme.colors.onSurface
                )
            )
        }
        LazyColumn {
            items(articles) { article ->
                Box(GlanceModifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                    ArticleHeadline(article, currentTime = currentTime)
                }
            }
            item {
                Row(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Button(
                        context.getString(R.string.widget_headlines_see_more_button),
                        onClick = context.openUnread()
                    )
                }
            }
        }
    }
}

private fun Context.openUnread() =
    actionStartActivity(
        Intent(this, MainActivity::class.java).apply {
            putExtra(UNREAD_ONLY_KEY, true)
            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        })

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview
@Composable
fun HeadlinesLayoutPreview() {
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
        updatedAt = ZonedDateTime.of(2025, 4, 6, 8, 33, 0, 0, ZoneOffset.UTC),
        publishedAt = ZonedDateTime.of(2025, 4, 6, 8, 33, 0, 0, ZoneOffset.UTC),
        read = true,
        starred = false,
        feedName = "9to5Google - Google news, Pixel, Android, Home, Chrome OS, more"
    )

    GlanceTheme {
        HeadlinesLayout(listOf(article))
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview
@Composable
fun HeadlinesLayoutDarkPreview() {
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
        updatedAt = ZonedDateTime.of(2025, 4, 6, 8, 33, 0, 0, ZoneOffset.UTC),
        publishedAt = ZonedDateTime.of(2025, 4, 6, 8, 33, 0, 0, ZoneOffset.UTC),
        read = true,
        starred = false,
        feedName = "9to5Google - Google news, Pixel, Android, Home, Chrome OS, more"
    )

    GlanceTheme(
        colors = ColorProviders(
            light = darkColorScheme(),
            dark = darkColorScheme(),
        ),
    ) {
        HeadlinesLayout(listOf(article))
    }
}
