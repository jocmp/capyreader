package com.jocmp.capy.articles

enum class TextAlignment {
    LEFT,
    CENTER;

    val toCSS: String
        get() = name.lowercase()

    companion object {
        val default = LEFT
    }
}
