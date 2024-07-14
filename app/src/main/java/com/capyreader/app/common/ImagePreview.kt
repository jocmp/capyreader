package com.capyreader.app.common

enum class ImagePreview {
    NONE,
    SMALL,
    LARGE;

    companion object {
        val default = SMALL

        val sorted: List<ImagePreview>
            get() = listOf(
                NONE,
                SMALL,
                LARGE,
            )
    }
}
