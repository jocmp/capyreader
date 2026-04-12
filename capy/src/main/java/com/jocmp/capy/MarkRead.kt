package com.jocmp.capy

sealed class MarkRead {
    data class Pair(
        val afterArticleID: String? = null,
        val beforeArticleID: String? = null,
    )

    data object All : MarkRead()

    data class Before(val articleID: String): MarkRead()

    data class After(val articleID: String): MarkRead()

    fun reversed(): MarkRead {
        return when(this) {
            is All -> All
            is Before -> After(articleID)
            is After -> Before(articleID)
        }
    }

    val toPair: Pair
        get() = when(this) {
            is After -> Pair(afterArticleID = articleID)
            is Before -> Pair(beforeArticleID = articleID)
            All -> Pair()
        }
}
