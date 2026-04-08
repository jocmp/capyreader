package com.capyreader.app.ui.widget

data class SpotlightEntry(
    val id: String,
    val feedID: String,
    val feedName: String,
    val title: String,
    val imageURL: String?,
    val articleURL: String?,
    val openInBrowser: Boolean,
)
