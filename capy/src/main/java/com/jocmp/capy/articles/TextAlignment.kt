package com.jocmp.capy.articles

enum class TextAlignment {
    LEFT,
    CENTER;

    val toCSS: String
        get() = when (this) {
            LEFT -> "start"
            CENTER -> "center"
        }

    companion object {
        val default = LEFT
    }
}
