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
import androidx.glance.appwidget.cornerRadius
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
import java.time.LocalDateTime

@Composable
fun HeadlinesLayout(articles: List<Article>) {
    val context = LocalContext.current
    val currentTime = remember { LocalDateTime.now() }

    Column(
        GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
            .cornerRadius(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = GlanceModifier
                .padding(16.dp)
                .clickable(context.openUnread())
                .fillMaxWidth()
        ) {
            Image(
                provider = ImageProvider(R.drawable.capy_icon_small),
                contentDescription = null,
                colorFilter = ColorFilter.tint(GlanceTheme.colors.secondary),
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
                Box(GlanceModifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
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
                        onClick = context.openUnread(),
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
@Preview(widthDp = 240, heightDp = 180)
@Composable
fun HeadlinesLayoutPreview() {
    GlanceTheme {
        HeadlinesLayout(sampleArticles())
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 240, heightDp = 180)
@Composable
fun HeadlinesLayoutDarkPreview() {
    GlanceTheme(
        colors = ColorProviders(
            light = darkColorScheme(),
            dark = darkColorScheme(),
        ),
    ) {
        HeadlinesLayout(sampleArticles())
    }
}
