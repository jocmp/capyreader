package com.capyreader.app.ui.articles

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.capyreader.app.common.AppPreferences
import org.koin.compose.koinInject

@Composable
fun ArticleHandler(
    appPreferences: AppPreferences = koinInject(),
    onRequestArticle: (articleID: String) -> Unit,
) {
    LaunchedEffect(Unit) {
        val articleID = appPreferences.articleID.get()

        if (articleID.isNotBlank()) {
            appPreferences.articleID.delete()
            onRequestArticle(articleID)
        }
    }
}
