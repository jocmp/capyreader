package com.jocmp.capy.articles

object FontSize {
    val MIN = 10
    val MAX = 30

    val scale: List<Int>
        get() = (MIN..MAX step 2).toList()

    const val DEFAULT = 16
}
