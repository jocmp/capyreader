package com.capyreader.app.ui.articles

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.capyreader.app.preferences.AppPreferences
import com.jocmp.capy.logging.CapyLog
import org.koin.compose.koinInject

@Composable
fun ArticleHandler(
    articleID: String?,
    appPreferences: AppPreferences = koinInject(),
    onRequestArticle: (articleID: String) -> Unit,
) {
    LaunchedEffect(articleID) {
        if (!articleID.isNullOrBlank()) {
            return@LaunchedEffect
        }

        val id = appPreferences.articleID.get()

        if (id.isNotBlank()) {
            CapyLog.info("handler", mapOf("article_id" to id))
            appPreferences.articleID.delete()
            onRequestArticle(id)
        }
    }
}
