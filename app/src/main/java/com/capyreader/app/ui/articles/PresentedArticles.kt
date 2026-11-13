package com.capyreader.app.ui.articles

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.articles.SortOrder
import kotlinx.coroutines.flow.first

class PresentedArticles(
    val filter: ArticleFilter,
    val sortOrder: SortOrder,
    val items: ArticleItems,
)

/** Keeps showing the previous list until [page] has loaded, so switching lists doesn't flash empty. */
@Composable
fun rememberPresentedArticles(page: ArticleListPage): PresentedArticles {
    var presented by remember { mutableStateOf(page.present()) }

    LaunchedEffect(page) {
        snapshotFlow { page.items.isLoaded }.first { it }
        presented = page.present()
    }

    return presented
}

private fun ArticleListPage.present() = PresentedArticles(filter, sortOrder, items)
