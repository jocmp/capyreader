package com.capyreader.app.translation

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode

class MLKitArticleTranslator : ArticleTranslator {
    override val isAvailable: Boolean = true

    override suspend fun detectLanguage(text: String): DetectedLanguage {
        return withContext(Dispatchers.IO) {
            try {
                val identifier = LanguageIdentification.getClient()
                val plainText = extractPlainText(text)
                val languageCode = identifier.identifyLanguage(plainText).await()

                if (languageCode == "und") {
                    DetectedLanguage.UNDETERMINED
                } else {
                    DetectedLanguage(languageCode, 1f)
                }
            } catch (e: Exception) {
                DetectedLanguage.UNDETERMINED
            }
        }
    }

    override suspend fun isModelDownloaded(
        sourceLanguage: String,
        targetLanguage: String
    ): Boolean {
        val source = TranslateLanguage.fromLanguageTag(sourceLanguage) ?: return false
        val target = TranslateLanguage.fromLanguageTag(targetLanguage) ?: return false

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(source)
            .setTargetLanguage(target)
            .build()

        val translator = Translation.getClient(options)

        return try {
            val conditions = DownloadConditions.Builder().build()
            translator.downloadModelIfNeeded(conditions).await()
            true
        } catch (e: Exception) {
            false
        } finally {
            translator.close()
        }
    }

    override suspend fun downloadModel(
        sourceLanguage: String,
        targetLanguage: String,
        requireWifi: Boolean
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val source = TranslateLanguage.fromLanguageTag(sourceLanguage)
            ?: return@withContext Result.failure(IllegalArgumentException("Unsupported source language: $sourceLanguage"))
        val target = TranslateLanguage.fromLanguageTag(targetLanguage)
            ?: return@withContext Result.failure(IllegalArgumentException("Unsupported target language: $targetLanguage"))

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(source)
            .setTargetLanguage(target)
            .build()

        val translator = Translation.getClient(options)

        try {
            val conditionsBuilder = DownloadConditions.Builder()
            if (requireWifi) {
                conditionsBuilder.requireWifi()
            }
            translator.downloadModelIfNeeded(conditionsBuilder.build()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            translator.close()
        }
    }

    override suspend fun translate(
        title: String,
        content: String,
        sourceLanguage: String,
        targetLanguage: String
    ): Result<TranslatedArticle> = withContext(Dispatchers.IO) {
        val source = TranslateLanguage.fromLanguageTag(sourceLanguage)
            ?: return@withContext Result.failure(IllegalArgumentException("Unsupported source language: $sourceLanguage"))
        val target = TranslateLanguage.fromLanguageTag(targetLanguage)
            ?: return@withContext Result.failure(IllegalArgumentException("Unsupported target language: $targetLanguage"))

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(source)
            .setTargetLanguage(target)
            .build()

        val translator = Translation.getClient(options)

        try {
            val translatedTitle = translator.translate(title).await()
            val translatedContent = translateHtml(content, translator)

            Result.success(
                TranslatedArticle(
                    title = translatedTitle,
                    content = translatedContent,
                    sourceLanguage = sourceLanguage,
                    targetLanguage = targetLanguage
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            translator.close()
        }
    }

    private suspend fun translateHtml(
        html: String,
        translator: com.google.mlkit.nl.translate.Translator
    ): String {
        val document = Jsoup.parseBodyFragment(html)
        translateNodes(document.body().childNodes(), translator)
        return document.body().html()
    }

    private suspend fun translateNodes(
        nodes: List<Node>,
        translator: com.google.mlkit.nl.translate.Translator
    ) {
        for (node in nodes) {
            when (node) {
                is TextNode -> {
                    val text = node.wholeText
                    if (text.isNotBlank()) {
                        val translated = translator.translate(text).await()
                        node.text(translated)
                    }
                }
                else -> {
                    translateNodes(node.childNodes(), translator)
                }
            }
        }
    }

    private fun extractPlainText(html: String): String {
        return Jsoup.parse(html).text()
    }
}
