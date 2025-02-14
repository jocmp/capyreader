package com.jocmp.capy

data class ArticlePages(
    val previousID: String? = null,
    val current: Int,
    val nextID: String? = null,
    val size: Int,
) {
    val previous = current - 1
    val next = current + 1
}
