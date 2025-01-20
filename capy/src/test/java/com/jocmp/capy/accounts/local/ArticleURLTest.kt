package com.jocmp.capy.accounts.local

import java.net.URL
import kotlin.test.Test
import kotlin.test.assertEquals

class ArticleURLTest {
    @Test
    fun `entry URL is the same as article URL`() {
        val entryURL = URL("https://www.theverge.com/2025/1/12/24340818/robot-vacuum-innovations-roborock-dreame-ecovacs-ces2025")

        val url = ArticleURL.parse(url = entryURL)

        assertEquals(expected = entryURL.toString(), actual = url.toString())
    }

    @Test
    fun `with a Google Alert entry URL it returns the url param`() {
        val entryURL = URL("https://www.google.com/url?rct=j&sa=t&url=https://www.androidheadlines.com/2025/01/meta-sued-for-allegedly-training-ai-with-content-from-pirated-books.html&ct=ga&cd=CAIyGjQ2MjY4NTIwYjAzMGNkMzc6Y29tOmVuOlVT&usg=AOvVaw2Ez54Yz16bwLLLX_YLfwA2")
        val articleURL = URL("https://www.androidheadlines.com/2025/01/meta-sued-for-allegedly-training-ai-with-content-from-pirated-books.html")

        val url = ArticleURL.parse(url = entryURL)

        assertEquals(expected = articleURL.toString(), actual = url.toString())
    }
}
