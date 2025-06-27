package com.capyreader.app.ui.articles.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capyreader.app.ui.articles.LocalArticleLookup
import com.jocmp.capy.ArticlePages

data class ArticlePagination(
    val pages: ArticlePages,
    val onSelectArticle: (index: Int, id: String) -> Unit,
) {
    val hasPrevious = pages.previous > -1
    val hasNext = pages.next > -1 && pages.next < pages.size
    val index = pages.current

    fun selectPrevious() {
        selectArticle(pages.previous, pages.previousID)
    }

    fun selectNext() {
        selectArticle(pages.next, pages.nextID)
    }

    private fun selectArticle(index: Int, id: String?) {
        if (index > -1 && index < pages.size && id != null) {
            onSelectArticle(index, id)
        }
    }
}

@Composable
fun rememberArticlePagination(
    articleID: String,
    onSelectArticle: (index: Int, id: String) -> Unit,
): ArticlePagination {
    val lookup = LocalArticleLookup.current
    val pages by remember(articleID) {
        lookup.findArticlePages(articleID)
    }.collectAsStateWithLifecycle(null)

    return remember(pages, onSelectArticle) {
        val pagesWithDefault = pages ?: ArticlePages(current = -2, size = 0)

        ArticlePagination(
            pagesWithDefault,
            onSelectArticle,
        )
    }
}
