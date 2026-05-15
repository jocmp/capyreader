package com.capyreader.app.ui.widget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.glance.Button
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.material3.ColorProviders
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.capyreader.app.MainActivity
import com.capyreader.app.OpenArticleInBrowserActivity
import com.capyreader.app.OpenArticleInBrowserActivity.Companion.ARTICLE_URL_KEY
import com.capyreader.app.R
import com.capyreader.app.notifications.NotificationHelper.Companion.ARTICLE_ID_KEY
import com.capyreader.app.notifications.NotificationHelper.Companion.FEED_ID_KEY
import com.capyreader.app.notifications.NotificationHelper.Companion.SHOW_ALL_KEY

@Composable
fun SpotlightLayout(
    entry: SpotlightEntry?,
    imageBitmap: Bitmap?,
) {
    val context = LocalContext.current
    val textColor = ColorProvider(Color.White, Color.White)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.primaryContainer)
            .cornerRadius(16.dp)
    ) {
        if (entry == null) {
            EmptyState(context)
        } else {
            val openAction = context.openArticle(entry)

            if (imageBitmap != null) {
                Image(
                    provider = ImageProvider(imageBitmap),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .clickable(openAction)
                )
            }

            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ImageProvider(R.drawable.spotlight_scrim))
            ) {}

            Box(
                contentAlignment = Alignment.BottomStart,
                modifier = GlanceModifier
                    .fillMaxSize()
                    .clickable(openAction)
                    .padding(start = 16.dp, end = 16.dp, top = 48.dp, bottom = 16.dp)
            ) {
                Column(modifier = GlanceModifier.fillMaxWidth()) {
                    Text(
                        entry.feedName,
                        maxLines = 1,
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = textColor,
                        ),
                    )
                    Text(
                        entry.title,
                        maxLines = 3,
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                        ),
                    )
                }
            }

            Box(
                contentAlignment = Alignment.TopStart,
                modifier = GlanceModifier.fillMaxSize()
            ) {
                Image(
                    provider = ImageProvider(R.drawable.capy_icon_small),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(textColor),
                    modifier = GlanceModifier
                        .padding(12.dp)
                        .size(48.dp)
                )
            }

            Box(
                contentAlignment = Alignment.TopEnd,
                modifier = GlanceModifier.fillMaxSize()
            ) {
                NavButtons(modifier = GlanceModifier.padding(8.dp))
            }
        }
    }
}

@Composable
private fun NavButtons(modifier: GlanceModifier = GlanceModifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = GlanceModifier
                .size(40.dp)
                .cornerRadius(20.dp)
                .clickable(actionRunCallback<SpotlightPreviousAction>())
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_chevron_left),
                contentDescription = LocalContext.current.getString(R.string.widget_spotlight_previous),
                modifier = GlanceModifier.size(20.dp)
            )
        }

        Spacer(GlanceModifier.size(4.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = GlanceModifier
                .size(40.dp)
                .cornerRadius(20.dp)
                .clickable(actionRunCallback<SpotlightNextAction>())
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_chevron_right),
                contentDescription = LocalContext.current.getString(R.string.widget_spotlight_next),
                modifier = GlanceModifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun EmptyState(context: Context) {
    Column(
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = GlanceModifier.fillMaxSize()
    ) {
        Text(
            context.getString(R.string.widget_spotlight_empty),
            style = TextStyle(
                color = GlanceTheme.colors.onSurface
            )
        )
        Spacer(GlanceModifier.size(8.dp))
        Button(
            text = context.getString(R.string.widget_spotlight_see_all),
            onClick = context.openAll(),
        )
    }
}

private fun Context.openAll() =
    actionStartActivity(
        Intent(this, MainActivity::class.java).apply {
            putExtra(SHOW_ALL_KEY, true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
    )

private fun Context.openArticle(entry: SpotlightEntry): Action {
    return if (entry.openInBrowser && entry.articleURL != null) {
        actionStartActivity(
            Intent(this, OpenArticleInBrowserActivity::class.java).apply {
                putExtra(ARTICLE_ID_KEY, entry.id)
                putExtra(ARTICLE_URL_KEY, entry.articleURL)
            }
        )
    } else {
        actionStartActivity(
            Intent(this, MainActivity::class.java).apply {
                putExtra(ARTICLE_ID_KEY, entry.id)
                putExtra(FEED_ID_KEY, entry.feedID)
                data = entry.articleURL?.toUri()
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 320, heightDp = 180)
@Composable
fun SpotlightLayoutPreview() {
    val previewBitmap =
        BitmapFactory.decodeResource(LocalContext.current.resources, R.drawable.hello_world)

    GlanceTheme {
        SpotlightLayout(
            entry = SpotlightEntry(
                id = "1",
                feedID = "nasa",
                feedName = "NASA Image of the Day",
                title = "\"Hello, World\"",
                imageURL = null,
                articleURL = "https://www.nasa.gov/image-article/hello-world/",
                openInBrowser = false,
            ),
            imageBitmap = previewBitmap,
        )
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 320, heightDp = 180)
@Composable
fun SpotlightLayoutTextOnlyPreview() {
    GlanceTheme {
        SpotlightLayout(
            entry = SpotlightEntry(
                id = "1",
                feedID = "nasa",
                feedName = "NASA Image of the Day",
                title = "Circular Star Trails Over the Austrian Alps",
                imageURL = null,
                articleURL = "https://www.nasa.gov/image-article/hello-world/",
                openInBrowser = false,
            ),
            imageBitmap = null,
        )
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 320, heightDp = 180)
@Composable
fun SpotlightLayoutDarkPreview() {
    GlanceTheme(
        colors = ColorProviders(
            light = darkColorScheme(),
            dark = darkColorScheme(),
        ),
    ) {
        SpotlightLayout(
            entry = SpotlightEntry(
                id = "1",
                feedID = "nasa",
                feedName = "NASA Image of the Day",
                title = "Circular Star Trails",
                imageURL = null,
                articleURL = "https://www.nasa.gov",
                openInBrowser = false,
            ),
            imageBitmap = null,
        )
    }
}
