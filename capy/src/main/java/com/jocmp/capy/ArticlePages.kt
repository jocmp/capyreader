package com.jocmp.capy

data class ArticlePages(
    val previousID: String? = null,
    val current: Int = 0,
    val nextID: String? = null,
    val size: Int = 0,
) {
    val previous = current - 1
    val next = current + 1
}
