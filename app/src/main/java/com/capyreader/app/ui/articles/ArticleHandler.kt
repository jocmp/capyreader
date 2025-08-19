package com.capyreader.app.ui.articles

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.capyreader.app.preferences.AppPreferences
import com.jocmp.capy.Article
import org.koin.compose.koinInject
import com.jocmp.capy.logging.CapyLog

@Composable
fun ArticleHandler(
    article: Article?,
    appPreferences: AppPreferences = koinInject(),
    onRequestArticle: (articleID: String) -> Unit,
) {
    LaunchedEffect(article?.id, state) {
        if (article != null && state?.pane != ThreePaneScaffoldRole.Primary) {
            CapyLog.info("launch", mapOf("id" to article.id))
            onNavigateToArticle(article)
        } else {
            CapyLog.info("skip", mapOf("id" to article?.id, "state" to state?.pane))
        }
    }
}
