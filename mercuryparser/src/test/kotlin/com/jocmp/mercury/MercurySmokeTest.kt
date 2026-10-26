package com.jocmp.mercury

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MercurySmokeTest {
    @Test
    fun `parses an abcnews article end-to-end`() {
        val html = javaClass.getResource("/fixtures/abcnews.go.com.html")!!.readText()
        val result =
            runBlocking {
                Mercury.parse(
                    "https://abcnews.go.com/Politics/article",
                    ParseOptions(html = html),
                )
            }
        assertNotNull(result.title, "title should not be null")
        assertNotNull(result.content, "content should not be null")
        assertTrue(result.content!!.isNotEmpty(), "content should not be empty")
        assertTrue(result.wordCount > 0, "wordCount should be > 0")
    }
}
