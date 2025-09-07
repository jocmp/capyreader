package com.capyreader.app.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.capyreader.app.R
import com.capyreader.app.preferences.AppPreferences
import com.jocmp.capy.Account
import com.jocmp.capy.Article
import com.jocmp.capy.latestArticles
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.net.URL
import java.time.ZoneOffset
import java.time.ZonedDateTime

class HeadlinesWidget : GlanceAppWidget() {
    private val viewModel by lazy { HeadlinesViewModel() }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                Content()
            }
        }
    }

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        provideContent {
            HeadlinesLayout(sampleArticles())
        }
    }

    @Composable
    private fun Content() {
        if (viewModel.isLoggedIn) {
            val articles by viewModel.articles.collectAsState(emptyList())

            HeadlinesLayout(articles)
        } else {
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
    }
}

class HeadlinesViewModel : KoinComponent {
    private val appPreferences by inject<AppPreferences>()
    private val repository by lazy { ArticlesRepository() }

    val isLoggedIn
        get() = appPreferences.isLoggedIn

    val articles
        get() = repository.articles
}

private class ArticlesRepository : KoinComponent {
    private val account by inject<Account>()

    val articles
        get() = account.latestArticles()
}

fun sampleArticles() =
    listOf(
        Article(
            id = "4",
            feedID = "nasa-iotd",
            title = "Circular Star Trails",
            author = "NASA",
            contentHTML = "<p>This long-exposure photograph, taken over 31 minutes from a window inside the International Space Station's Kibo laboratory module...</p>",
            imageURL = "https://www.nasa.gov/wp-content/uploads/2025/08/iss073e0427643orig.jpg",
            summary = "This long-exposure photograph, taken over 31 minutes from a window inside the International Space Station's Kibo laboratory module, captures the graceful arcs of star trails.",
            url = URL("https://www.nasa.gov/image-detail/iss073e0427643/"),
            updatedAt = ZonedDateTime.of(2025, 9, 2, 14, 22, 0, 0, ZoneOffset.UTC),
            publishedAt = ZonedDateTime.of(2025, 9, 2, 14, 22, 0, 0, ZoneOffset.UTC),
            read = false,
            starred = false,
            feedName = "NASA Image of the Day"
        ),
        Article(
            id = "1",
            feedID = "nasa-iotd",
            title = "Orion Mission Evaluation Room",
            author = "NASA",
            contentHTML = "<p>Orion Mission Evaluation Room (MER) team member works during an Artemis II mission simulation...</p>",
            imageURL = "https://www.nasa.gov/wp-content/uploads/2025/09/jsc2025e070711large.jpg",
            summary = "Orion Mission Evaluation Room (MER) team member works during an Artemis II mission simulation on Aug. 19, 2025, from the new Orion MER inside the Mission Control Center at NASA's Johnson Space Center in Houston.",
            url = URL("https://www.nasa.gov/image-detail/jsc2025e070711large/"),
            updatedAt = ZonedDateTime.of(2025, 9, 5, 15, 22, 0, 0, ZoneOffset.UTC),
            publishedAt = ZonedDateTime.of(2025, 9, 5, 15, 22, 0, 0, ZoneOffset.UTC),
            read = false,
            starred = false,
            feedName = "NASA Image of the Day"
        ),
        Article(
            id = "2",
            feedID = "nasa-iotd",
            title = "NASA astronauts Jonny Kim and Zena Cardman pose for a portrait in the Unity module",
            author = "NASA",
            contentHTML = "<p>NASA astronauts Jonny Kim and Zena Cardman, both Expedition 73 Flight Engineers, pose for a portrait...</p>",
            imageURL = "https://images-assets.nasa.gov/image/iss073e0505687/iss073e0505687~large.jpg",
            summary = "NASA astronauts Jonny Kim and Zena Cardman, both Expedition 73 Flight Engineers, pose for a portrait inside the International Space Station's Unity module during a break in weekend housecleaning and maintenance activities.",
            url = URL("https://www.nasa.gov/image-detail/amf-iss073e0505687/"),
            updatedAt = ZonedDateTime.of(2025, 9, 4, 14, 59, 0, 0, ZoneOffset.UTC),
            publishedAt = ZonedDateTime.of(2025, 9, 4, 14, 59, 0, 0, ZoneOffset.UTC),
            read = false,
            starred = false,
            feedName = "NASA Image of the Day"
        ),
        Article(
            id = "3",
            feedID = "nasa-iotd",
            title = "Thinning Arctic Sea Ice",
            author = "NASA",
            contentHTML = "<p>Sea ice is frozen seawater that floats in the ocean...</p>",
            imageURL = "https://www.nasa.gov/wp-content/uploads/2025/08/nasa-september-2025-hd-1920x1080-1.jpg",
            summary = "Sea ice is frozen seawater that floats in the ocean. This photo, taken from NASA's Gulfstream V Research Aircraft on July 21, 2022, shows Arctic sea ice in the Lincoln Sea north of Greenland.",
            url = URL("https://www.nasa.gov/image-detail/nasa-september-2025-hd-1920x1080/"),
            updatedAt = ZonedDateTime.of(2025, 9, 3, 15, 19, 0, 0, ZoneOffset.UTC),
            publishedAt = ZonedDateTime.of(2025, 9, 3, 15, 19, 0, 0, ZoneOffset.UTC),
            read = false,
            starred = false,
            feedName = "NASA Image of the Day"
        ),
    )
