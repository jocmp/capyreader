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
import com.capyreader.app.R
import com.capyreader.app.preferences.AppPreferences
import com.jocmp.capy.Account
import com.jocmp.capy.latestArticles
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class HeadlinesWidget : GlanceAppWidget() {
    private val viewModel by lazy { HeadlinesViewModel() }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                Content()
            }
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
                Text(context.getString(R.string.widget_headlines_account_error))
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
        get() = account.latestArticles
}
