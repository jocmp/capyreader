package com.capyreader.app.translation

import java.util.Locale

interface ArticleTranslator {
    val isAvailable: Boolean

    suspend fun detectLanguage(text: String): DetectedLanguage

    suspend fun isModelDownloaded(
        sourceLanguage: String,
        targetLanguage: String = deviceLanguage
    ): Boolean

    suspend fun downloadModel(
        sourceLanguage: String,
        targetLanguage: String = deviceLanguage,
        requireWifi: Boolean = true
    ): Result<Unit>

    suspend fun translate(
        title: String,
        content: String,
        sourceLanguage: String,
        targetLanguage: String = deviceLanguage
    ): Result<TranslatedArticle>

    companion object {
        val deviceLanguage: String
            get() = Locale.getDefault().language
    }
}
