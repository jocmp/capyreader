package com.capyreader.app.ui.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import com.capyreader.app.R
import com.capyreader.app.preferences.AppPreferences
import com.jocmp.capy.Account
import com.jocmp.capy.Article
import com.jocmp.capy.latestArticles
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

class SpotlightWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Responsive(
        setOf(SMALL_SIZE, WIDE_SIZE)
    )

    private val viewModel by lazy { SpotlightViewModel() }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entries = if (viewModel.isLoggedIn) {
            val articles = viewModel.fetchArticles()
            cacheImages(context, articles)
            articles.map { it.toSpotlightEntry() }
        } else {
            emptyList()
        }

        provideContent {
            GlanceTheme {
                if (!viewModel.isLoggedIn) {
                    LoggedOutContent()
                } else {
                    Content(entries)
                }
            }
        }
    }

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        val previewBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.hello_world)

        provideContent {
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
    }

    @Composable
    private fun Content(entries: List<SpotlightEntry>) {
        val context = LocalContext.current
        val prefs = currentState<Preferences>()
        val manualOffset = prefs[CURRENT_INDEX_KEY] ?: 0
        val currentIndex =
            if (entries.isEmpty()) 0 else (lcgIndex(entries.size) + manualOffset).mod(entries.size)
        val entry = entries.getOrNull(currentIndex)
        val imageBitmap = entry?.let { readCachedImage(context, it.id) }

        SpotlightLayout(
            entry = entry,
            imageBitmap = imageBitmap,
        )
    }

    @Composable
    private fun LoggedOutContent() {
        val context = LocalContext.current

        Box(
            contentAlignment = Alignment.Center,
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.background)
        ) {
            Text(
                context.getString(R.string.widget_headlines_account_error),
                style = TextStyle(color = GlanceTheme.colors.onSurface)
            )
        }
    }

    companion object {
        val CURRENT_INDEX_KEY = intPreferencesKey("spotlight_current_index")

        suspend fun refresh(context: Context, glanceId: GlanceId) {
            SpotlightWidget().update(context, glanceId)
        }

        suspend fun navigate(context: Context, glanceId: GlanceId, delta: Int) {
            updateAppWidgetState(context, glanceId) { prefs ->
                val current = prefs[CURRENT_INDEX_KEY] ?: 0
                prefs[CURRENT_INDEX_KEY] = current + delta
            }
            SpotlightWidget().update(context, glanceId)
        }

        private suspend fun cacheImages(context: Context, articles: List<Article>) {
            clearImageCache(context)

            articles.forEach { article ->
                article.imageURL?.let { url ->
                    cacheImage(context, url, article.id)
                }
            }
        }

        private fun imageDir(context: Context): File =
            File(context.cacheDir, "spotlight_images").apply { mkdirs() }

        private fun imageFile(context: Context, id: String): File =
            File(imageDir(context), "spotlight_${safeFileName(id)}.jpg")

        private fun safeFileName(id: String): String =
            id.hashCode().toUInt().toString(16)

        private suspend fun cacheImage(context: Context, url: String, id: String) {
            try {
                val request = ImageRequest.Builder(context)
                    .data(url)
                    .size(720)
                    .allowHardware(false)
                    .build()
                val result = context.imageLoader.execute(request)
                val image = result.image ?: return
                val bitmap = image.toBitmap()
                imageFile(context, id).outputStream().use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
                }
            } catch (_: Exception) {
            }
        }

        fun readCachedImage(context: Context, id: String): Bitmap? {
            val file = imageFile(context, id)
            if (!file.exists()) return null
            return BitmapFactory.decodeFile(file.absolutePath)
        }

        private fun clearImageCache(context: Context) {
            imageDir(context).listFiles()?.forEach { it.delete() }
        }
    }
}

private class SpotlightViewModel : KoinComponent {
    private val appPreferences by inject<AppPreferences>()
    private val account by inject<Account>()

    val isLoggedIn
        get() = appPreferences.isLoggedIn

    suspend fun fetchArticles() =
        account.latestArticles(limit = 10).first()
}

private fun Article.toSpotlightEntry() = SpotlightEntry(
    id = id,
    feedID = feedID,
    feedName = feedName,
    title = title.ifBlank { summary },
    imageURL = imageURL,
    articleURL = url?.toString(),
    openInBrowser = openInBrowser,
)

private val SMALL_SIZE = DpSize(110.dp, 110.dp)
private val WIDE_SIZE = DpSize(220.dp, 110.dp)

private const val LCG_INTERVAL_MILLIS = 30 * 60 * 1000L // 30 minutes

/**
 * Picks a deterministic pseudo-random index using a Linear Congruential Generator (LCG).
 * The current 30-minute time slot seeds the formula: (a * slot + c) mod m.
 *
 * Constants:
 * - a (multiplier): 1103515245 — scales the seed to spread values across the output range
 * - c (increment):  12345 — must be coprime with m
 * - m (modulus):    2^31
 */
private fun lcgIndex(size: Int): Int {
    return lcgIndex(currentTimeMillis = System.currentTimeMillis(), size = size)
}

internal fun lcgIndex(currentTimeMillis: Long, size: Int): Int {
    val slot = currentTimeMillis / LCG_INTERVAL_MILLIS
    val a = 1103515245L
    val c = 12345L
    val m = 1L shl 31
    return ((a * slot + c) % m).toInt().mod(size)
}
