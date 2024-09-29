package com.capyreader.app.ui.articles

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity

enum class ArticleListFontScale {
    SMALL,
    MEDIUM,
    LARGE,
    X_LARGE,
    XX_LARGE;

    val relative: Float
        get() = when (this) {
            SMALL -> 0.8125f
            MEDIUM -> 1f
            LARGE -> 1.125f
            X_LARGE -> 1.5f
            XX_LARGE -> 2f
        }

    @Composable
    fun withLocaleDensity(): Float {
        val current = LocalDensity.current.fontScale

        return current * relative
    }

    companion object {
        val default = MEDIUM
    }
}
