package com.capyreader.app.ui.widget

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.glance.ExperimentalGlanceApi
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.background
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.Text
import com.capyreader.app.MainActivity
import com.capyreader.app.notifications.NotificationHelper.Companion.ARTICLE_ID_KEY
import com.capyreader.app.notifications.NotificationHelper.Companion.FEED_ID_KEY
import com.jocmp.capy.Article
import java.net.URL
import java.time.ZoneOffset
import java.time.ZonedDateTime

//
//private val articleKey = ActionParameters.Key<String>(
//    ARTICLE_ID_KEY
//)
//
//private val feedKey = ActionParameters.Key<String>(
//    FEED_ID_KEY
//)

@OptIn(ExperimentalGlanceApi::class)
@Composable
fun ArticleHeadline(article: Article) {
    val context = LocalContext.current
    Row(
        modifier = GlanceModifier
            .background(GlanceTheme.colors.background)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(
                actionStartActivity(
                    Intent(context, MainActivity::class.java).apply {
                        putExtra(ARTICLE_ID_KEY, article.id)
                        putExtra(FEED_ID_KEY, article.feedID)
                        data = uniqueUri(article)
                        setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    })
            )
            .fillMaxWidth()
    ) {
        Text(article.title)
    }
}

fun uniqueUri(article: Article): Uri {
    val fallbackUri = "https://capyreader.com/${article.id}".toUri()
    val url = article.url

    return try {
        url.toString().toUri()
    } catch (e: Throwable) {
        fallbackUri
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview
@Composable
fun ArticleHeadlinePreview() {
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
        ArticleHeadline(article)
    }
}
