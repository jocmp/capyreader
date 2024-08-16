package com.jocmp.capy

sealed class MarkRead {
    data class Pair(
        val afterArticleID: String? = null,
        val beforeArticleID: String? = null,
        val currentArticleID: String? = null,
    )

    data object All : MarkRead()

    data class Before(val articleID: String) : MarkRead()

    data class After(val articleID: String) : MarkRead()

    data class CurrentArticle(val articleID: String) : MarkRead()

    val toPair: Pair
        get() = when (this) {
            is After -> Pair(afterArticleID = articleID)
            is Before -> Pair(beforeArticleID = articleID)
            else -> Pair()
        }
}
