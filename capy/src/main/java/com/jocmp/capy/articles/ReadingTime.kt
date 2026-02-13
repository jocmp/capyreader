package com.jocmp.capy.articles

import org.jsoup.Jsoup

object ReadingTime {
    private const val CJK_CHARACTERS_PER_MINUTE = 265
    private const val CHARACTERS_PER_MINUTE = 500

    fun calculate(contentHtml: String?): Long? {
        if (contentHtml.isNullOrBlank()) return null

        val text = Jsoup.parse(contentHtml).text()

        if (text.isBlank()) return null

        val cpm = if (isCJK(text)) CJK_CHARACTERS_PER_MINUTE else CHARACTERS_PER_MINUTE
        val minutes = (text.length + cpm - 1) / cpm

        return if (minutes > 0) minutes.toLong() else null
    }

    internal fun isCJK(text: String): Boolean {
        var totalCJK = 0
        var totalChecked = 0

        for (c in text) {
            if (totalChecked >= 50) break
            totalChecked++
            val block = Character.UnicodeBlock.of(c)
            if (block in CJK_BLOCKS) totalCJK++
        }

        return totalChecked > 0 && totalCJK * 2 >= totalChecked
    }

    private val CJK_BLOCKS = setOf(
        Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS,
        Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A,
        Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B,
        Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS,
        Character.UnicodeBlock.CJK_COMPATIBILITY_FORMS,
        Character.UnicodeBlock.HIRAGANA,
        Character.UnicodeBlock.KATAKANA,
        Character.UnicodeBlock.HANGUL_SYLLABLES,
        Character.UnicodeBlock.HANGUL_JAMO,
        Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO,
        Character.UnicodeBlock.BOPOMOFO,
        Character.UnicodeBlock.YI_SYLLABLES,
        Character.UnicodeBlock.YI_RADICALS,
    )
}
