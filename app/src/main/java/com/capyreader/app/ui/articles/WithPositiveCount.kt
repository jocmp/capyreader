package com.capyreader.app.ui.articles

import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.Countable

fun <T : Countable> List<T>.withPositiveCount(status: ArticleStatus): List<T> {
    return filter { status == ArticleStatus.ALL || it.count > 0 }
}
