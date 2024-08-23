package com.capyreader.app.ui.components

data class ArticleSearch(
    val query: String? = null,
    val clear: () -> Unit = {},
    val update: (query: String) -> Unit = {},
) {
    val isActive = query != null

    val isInitialized = query != null && query.isBlank()
}
