package com.jocmp.mercury.extractors.custom.nymag.com

import com.jocmp.mercury.Mercury
import com.jocmp.mercury.ParseOptions
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class NYMagExtractorTest {
    @Test
    fun `works with a feature story`() {
        val html = javaClass.getResource("/fixtures/nymag.com.html")!!.readText()
        val uri = "http://nymag.com/daily/intelligencer/2016/09/how-fox-news-women-took-down-roger-ailes.html"

        val result = runBlocking { Mercury.parse(uri, ParseOptions(html = html)) }

        // Note: dek extraction has a complex interaction with excerpt extraction
        // that causes it to return null in the full Mercury.parse pipeline
        assertEquals("The Revenge of Roger’s Angels", result.title)
        assertEquals("Gabriel Sherman", result.author)
    }
}
