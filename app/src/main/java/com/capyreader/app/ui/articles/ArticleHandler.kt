package com.capyreader.app.ui.articles

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.capyreader.app.common.AppPreferences
import com.jocmp.capy.Article
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

        if (articleID.isNotBlank()) {
            appPreferences.articleID.delete()
            onRequestArticle(articleID)
        }
    }
}
