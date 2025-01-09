package com.jocmp.capy

data class ArticleNotification(
    val id: Int,
    val articleID: String,
    val title: String,
    val feedID: String,
    val feedTitle: String,
    val feedFaviconURL: String?,
)
