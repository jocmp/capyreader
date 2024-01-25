package com.jocmp.basilreader.ui.articles

import com.jocmp.basil.Article

class ArticleRenderer(private val article: Article) {
    private val body = article.contentHTML.ifBlank {
        article.summary
    }

    fun render(): String {
        return body
    }
}
