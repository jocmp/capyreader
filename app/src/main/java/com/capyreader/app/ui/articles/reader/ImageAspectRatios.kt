package com.capyreader.app.ui.articles.reader

import androidx.compose.runtime.mutableStateMapOf

object ImageAspectRatios {
    private val ratios = mutableStateMapOf<String, Float>()

    operator fun get(url: String): Float? = ratios[url]

    fun put(url: String, width: Int, height: Int) {
        if (width < 1 || height < 1) {
            return
        }

        ratios[url] = width.toFloat() / height.toFloat()
    }
}
