package com.jocmp.capy.articles

enum class TextSize {
    SMALL,
    MEDIUM,
    LARGE,
    X_LARGE,
    XX_LARGE;

    val slug: String
        get() = when(this) {
            SMALL -> "small"
            MEDIUM -> "medium"
            LARGE -> "large"
            X_LARGE -> "x-large"
            XX_LARGE -> "xx-large"
        }

    companion object {
        val default = MEDIUM

        val sorted: List<TextSize>
            get() = listOf(
                SMALL,
                MEDIUM,
                LARGE,
                X_LARGE,
                XX_LARGE,
            )
    }
}
