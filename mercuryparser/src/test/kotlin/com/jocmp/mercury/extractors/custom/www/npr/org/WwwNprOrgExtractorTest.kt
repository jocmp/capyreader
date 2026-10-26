package com.jocmp.mercury.extractors.custom.www.npr.org

import com.jocmp.mercury.Mercury
import com.jocmp.mercury.ParseOptions
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WwwNprOrgExtractorTest {
    @Test
    fun `expands Brightspot image templates`() {
        assertFalse(Regex("""\{\w+\}|%7B\w+%7D""", RegexOption.IGNORE_CASE).containsMatchIn(content))
        assertTrue(content.contains("/resize/1200/quality/85/format/webp/?url=http%3A%2F%2Fnpr-brightspot.s3.amazonaws.com%2F06%2F6a%2F46ec36764e5db11da70604db9122%2Fgettyimages-2301229190.jpg"))
    }

    companion object {
        private val content: String by lazy {
            val html =
                WwwNprOrgExtractorTest::class.java
                    .getResource("/fixtures/www.npr.org/republican-redistricting-midterms-competitive-map.html")!!
                    .readText()
            val uri = "https://www.npr.org/2026/10/26/nx-s1-6008167/republican-redistricting-midterms-competitive-map"

            runBlocking { Mercury.parse(uri, ParseOptions(html = html)) }.content.orEmpty()
        }
    }
}
