package com.capyreader.app.ui.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.Text
import com.jocmp.capy.Article
import java.net.URL
import java.time.ZoneOffset
import java.time.ZonedDateTime

@Composable
fun HeadlinesLayout(articles: List<Article>) {
    Column(
        GlanceModifier
            .background(GlanceTheme.colors.background)
    ) {
        Row(
            modifier = GlanceModifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text("Latest Headlines")
        }
        LazyColumn {
            items(articles) { article ->
                ArticleHeadline(article)
            }
        }
    }
}

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
        updatedAt = ZonedDateTime.of(2024, 2, 11, 8, 33, 0, 0, ZoneOffset.UTC),
        publishedAt = ZonedDateTime.of(2024, 2, 11, 8, 33, 0, 0, ZoneOffset.UTC),
        read = true,
        starred = false,
        feedName = "9to5Google - Google news, Pixel, Android, Home, Chrome OS, more"
    )

    GlanceTheme {
        HeadlinesLayout(listOf(article))
    }
}
