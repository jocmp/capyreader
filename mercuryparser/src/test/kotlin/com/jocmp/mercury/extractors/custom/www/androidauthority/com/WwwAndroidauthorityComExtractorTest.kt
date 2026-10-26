package com.jocmp.mercury.extractors.custom.www.androidauthority.com

import com.jocmp.mercury.Mercury
import com.jocmp.mercury.ParseOptions
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WwwAndroidauthorityComExtractorTest {
    @Test
    fun `cleans the add on Google badges`() {
        assertFalse(content.contains("Add Android Authority on Google"))
        assertFalse(content.contains("Google_Symbol_0.svg"))
        assertFalse(content.contains("Google_Discover-1.svg"))
    }

    @Test
    fun `cleans the affiliate disclaimer`() {
        assertFalse(content.contains("Affiliate links on Android Authority"))
    }

    @Test
    fun `keeps the article body`() {
        assertTrue(content.contains("on a mission to replace as many Android apps"))
    }

    companion object {
        private val content: String by lazy {
            val html =
                WwwAndroidauthorityComExtractorTest::class.java
                    .getResource("/fixtures/www.androidauthority.com/popular-android-apps-open-source-alternatives.html")!!
                    .readText()
            val uri = "https://www.androidauthority.com/popular-android-apps-open-source-alternatives-3725412/"

            runBlocking { Mercury.parse(uri, ParseOptions(html = html)) }.content.orEmpty()
        }
    }
}
