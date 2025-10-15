package com.capyreader.app.ui.components

data class ArticleSearch(
    val query: String? = null,
    val start: () -> Unit = {},
    val clear: () -> Unit = {},
    val update: (query: String) -> Unit = {},
    val state: SearchState = SearchState.INACTIVE,
) {
    val isActive
        get() = state == SearchState.ACTIVE
}
