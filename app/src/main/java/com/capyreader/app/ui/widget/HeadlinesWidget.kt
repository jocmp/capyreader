package com.capyreader.app.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import com.jocmp.capy.Account
import com.jocmp.capy.latestArticles
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class HeadlinesWidget : GlanceAppWidget() {
    // please sign in first blah blah blah
    private val repository by lazy { HeadlineRepository() }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                Content()
            }
        }
    }

    @Composable
    private fun Content() {
        val articles by repository.articles.collectAsState(emptyList())

        HeadlinesLayout(articles)
    }
}

class HeadlineRepository : KoinComponent {
    private val account by inject<Account>()

    val articles
        get() = account.latestArticles
}
