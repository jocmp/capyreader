package com.jocmp.capy.articles

object FontSize {
    private const val MIN = 12
    private const val MAX = 32

    val scale: List<Int>
        get() = (MIN..MAX step 2).toList()

    const val DEFAULT = 16
    const val TITLE_DEFAULT = 24
}
