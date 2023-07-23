package com.jocmp.feedbinclient

data class Article(
    val id: Long,
    val feedID: Long,
    val title: String,
    val summary: String
)
