package com.capyreader.app.ui.articles

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity

enum class ArticleListFontScale {
    X_SMALL,
    SMALL,
    MEDIUM,
    LARGE,
    X_LARGE,
    XX_LARGE,
    XXX_LARGE,
    XXXX_LARGE;

    val relative: Float
        get() = when (this) {
            X_SMALL -> 0.8125f
            SMALL -> 0.875f
            MEDIUM -> 1f
            LARGE -> 1.125f
            X_LARGE -> 1.375f
            XX_LARGE -> 1.5f
            XXX_LARGE -> 1.75f
            XXXX_LARGE -> 2f
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
