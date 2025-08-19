package com.capyreader.app.ui.articles

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.jocmp.capy.Article
import com.jocmp.capy.logging.CapyLog

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun <T> ArticleHandler(
    article: Article?,
    scaffoldNavigator: ThreePaneScaffoldNavigator<T>,
    onNavigateToArticle: (article: Article) -> Unit,
) {
    val state = scaffoldNavigator.currentDestination

    LaunchedEffect(article?.id, state) {
        if (article != null && state?.pane != ThreePaneScaffoldRole.Primary) {
            CapyLog.info("launch", mapOf("id" to article.id))
            onNavigateToArticle(article)
        } else {
            CapyLog.info("skip", mapOf("id" to article?.id, "state" to state?.pane))
        }
    }
}
