package com.capyreader.app.translation

import java.util.Locale

data class DetectedLanguage(
    val languageCode: String,
    val confidence: Float
) {
    val displayName: String
        get() = Locale.forLanguageTag(languageCode).displayLanguage

    companion object {
        val UNDETERMINED = DetectedLanguage("und", 0f)
    }
}
