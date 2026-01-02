package com.capyreader.app.translation

data class TranslatedArticle(
    val title: String,
    val content: String,
    val sourceLanguage: String,
    val targetLanguage: String
)
