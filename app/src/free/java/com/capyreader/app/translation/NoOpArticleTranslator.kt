package com.capyreader.app.translation

class NoOpArticleTranslator : ArticleTranslator {
    override val isAvailable: Boolean = false

    override suspend fun detectLanguage(text: String): DetectedLanguage {
        return DetectedLanguage.UNDETERMINED
    }

    override suspend fun isModelDownloaded(
        sourceLanguage: String,
        targetLanguage: String
    ): Boolean = false

    override suspend fun downloadModel(
        sourceLanguage: String,
        targetLanguage: String,
        requireWifi: Boolean
    ): Result<Unit> = Result.failure(UnsupportedOperationException("Translation not available"))

    override suspend fun translate(
        title: String,
        content: String,
        sourceLanguage: String,
        targetLanguage: String
    ): Result<TranslatedArticle> = Result.failure(UnsupportedOperationException("Translation not available"))
}
