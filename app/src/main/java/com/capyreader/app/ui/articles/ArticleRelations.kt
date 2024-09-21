package com.capyreader.app.ui.articles

import androidx.paging.ItemSnapshotList
import com.jocmp.capy.Article

data class ArticleRelations(
    val index: Int,
    val next: Int,
    val previous: Int,
    val total: Int,
) {
    fun hasPrevious(): Boolean {
        return previous > -1
    }

    fun hasNext(): Boolean {
        return next < total
    }

    companion object {
        fun from(
            article: Article,
            articles: List<Article?>
        ): ArticleRelations {
            val index = articles.indexOfFirst { it?.id == article.id }

            return ArticleRelations(
                index = index,
                previous = index - 1,
                next = index + 1,
                total = articles.size
            )
        }
    }
}
