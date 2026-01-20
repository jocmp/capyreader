package com.capyreader.app.ui.articles

import androidx.compose.runtime.staticCompositionLocalOf
import com.capyreader.app.ui.articles.list.ArticlePagingAdapter
import com.jocmp.capy.Article

class ArticleNavigator(
    private val getAdapter: () -> ArticlePagingAdapter?,
    private val getSelectedArticleId: () -> String?,
    private val onSelectArticle: (String) -> Unit,
) {
    val adapter: ArticlePagingAdapter?
        get() = getAdapter()
    private fun findCurrentIndex(): Int {
        val adapter = getAdapter() ?: return -1
        val selectedId = getSelectedArticleId() ?: return -1
        return (0 until adapter.itemCount).firstOrNull { pos ->
            adapter.getArticle(pos)?.id == selectedId
        } ?: -1
    }

    fun hasPrevious(): Boolean = findCurrentIndex() > 0

    fun hasNext(): Boolean {
        val adapter = getAdapter() ?: return false
        val idx = findCurrentIndex()
        return idx >= 0 && idx < adapter.itemCount - 1
    }

    fun previousArticle(): Article? {
        val adapter = getAdapter() ?: return null
        val idx = findCurrentIndex()
        return if (idx > 0) adapter.getArticle(idx - 1) else null
    }

    fun nextArticle(): Article? {
        val adapter = getAdapter() ?: return null
        val idx = findCurrentIndex()
        return if (idx >= 0 && idx < adapter.itemCount - 1) {
            adapter.getArticle(idx + 1)
        } else null
    }

    fun selectPrevious() {
        previousArticle()?.let { onSelectArticle(it.id) }
    }

    fun selectNext() {
        nextArticle()?.let { onSelectArticle(it.id) }
    }

    fun selectArticle(article: Article) {
        onSelectArticle(article.id)
    }
}

val LocalArticleNavigator = staticCompositionLocalOf<ArticleNavigator?> { null }
