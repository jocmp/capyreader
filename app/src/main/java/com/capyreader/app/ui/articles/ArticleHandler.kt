package com.capyreader.app.ui.articles

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.capyreader.app.common.AppPreferences
import com.jocmp.capy.Article
import com.jocmp.capy.logging.CapyLog
import org.koin.compose.koinInject

@Composable
fun ArticleHandler(
    article: Article?,
    appPreferences: AppPreferences = koinInject(),
    onRequestArticle: (articleID: String) -> Unit,
) {
    LaunchedEffect(article?.id) {
        if (article != null) {
            return@LaunchedEffect
        }

        val articleID = appPreferences.articleID.get()

        CapyLog.info("handler", mapOf("article_id" to articleID))

        if (articleID.isNotBlank()) {
            appPreferences.articleID.delete()
            onRequestArticle(articleID)
        }
    }
}
